package coflow

import java.net.InetAddress

import akka.actor._
import akka.remote.DisassociatedEvent
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

private[coflow] object CoflowMaster {

    val REMOTE_SYNC_PERIOD_MILLIS = Option(System.getenv("COFLOW_SYNC_PERIOD_MS")).getOrElse("1000").toInt

    val host = Option(System.getenv("COFLOW_MASTER_IP")).getOrElse(InetAddress.getLocalHost.getHostAddress)
    val port = 1606

    private val logger = LoggerFactory.getLogger(CoflowMaster.getClass)

    private val ipToBandwidth = TrieMap[String, Long]()
    private val ipToSlave = TrieMap[String, ActorRef]()
    private val ipToCoflows = TrieMap[String, SlaveCoflows]()

    def main(args: Array[String]) = {

        val conf = ConfigFactory.parseString(
            s"""
            akka.remote.netty.tcp {
                hostname = "$host",
                port = $port
            }
            """.stripMargin).withFallback(ConfigFactory.load())

        val actorSystem = ActorSystem("coflowMaster", conf)
        val master = actorSystem.actorOf(Props[MasterActor], "master")

        actorSystem.eventStream.subscribe(master, classOf[DisassociatedEvent])
        actorSystem.awaitTermination()
    }

    def clustering(flows: Array[Flow]): Array[String] = {

        val epsilon = 5000L

        val sortedFlowWithIndex = flows.zipWithIndex.sortBy(_._1.startTime)
        var lastStartTime = 0L
        var currentCluster = -1

        val clusterIds = sortedFlowWithIndex.map({
            case (flow, index) =>
                if (lastStartTime + epsilon < flow.startTime) {
                    currentCluster += 1
                }
                lastStartTime = flow.startTime
                currentCluster.toString
        })

        clusterIds.toArray
    }

    def getSchedule(flowSizes: Array[Long], coflowIds: Array[String]): Array[Int] = {

        val threshold = (0 to 9).map(Math.pow(10, _).asInstanceOf[Long] * 10 * 1024 * 1024)
        val coflowSizes = coflowIds.zip(flowSizes).groupBy(_._1).mapValues(_.map(_._2).sum)
        val coflowPriority = coflowSizes.mapValues(size => threshold.zipWithIndex.find(_._1 < size).map(_._2).getOrElse(10))
        coflowIds.map(coflowPriority(_))
    }

    def getRate(flows: Array[Flow], priorities: Array[Int]): Array[Long] = {

        val priorityQueue = priorities.zip(flows).groupBy(_._1).toArray.sortBy(_._1).map(_._2.map(_._2))

        val egressFree = mutable.Map[String, Long]() ++ ipToBandwidth
        val ingressFree = mutable.Map[String, Long]() ++ ipToBandwidth

        priorityQueue.flatMap(queue => {
            val egressNumFlows = queue.groupBy(_.srcIp).mapValues(_.length)
            val ingressNumFlows = queue.groupBy(_.dstIp).mapValues(_.length)
            queue.map(flow => {
                val rate = math.min(egressFree.getOrElse(flow.srcIp, 0L) / egressNumFlows(flow.srcIp),
                    ingressFree.getOrElse(flow.dstIp, 0L) / ingressNumFlows(flow.dstIp))
                if (rate > 0) {
                    egressFree(flow.srcIp) -= rate
                    ingressFree(flow.dstIp) -= rate
                }
                rate
            })
        })
    }


    private[coflow] class MasterActor extends Actor {

        override def preStart = {
            context.system.scheduler.schedule(0.second, REMOTE_SYNC_PERIOD_MILLIS.millis, self, MergeAndSync)
        }

        override def receive = {

            case SlaveRegister(bandwidth) =>
                logger.info(s"slave from ${sender.path.address} registered with $bandwidth byte/s bandwidth")
                for (ip <- sender.path.address.host) {
                    ipToBandwidth(ip) = bandwidth
                    ipToSlave(ip) = sender
                }

            case SlaveCoflows(coflows) =>
                if (coflows.nonEmpty) {
                    logger.debug(s"slave from ${sender.path.address} report coflows with ${coflows.length} flows")
                }
                for (ip <- sender.path.address.host) {
                    ipToCoflows(ip) = SlaveCoflows(coflows)
                }

            case MergeAndSync =>

                val coflows = ipToCoflows.values.flatMap(_.coflows)

                if (coflows.nonEmpty) {

                    val start = System.currentTimeMillis

                    /*
                    val clusterIds = clustering(coflows.map(_.flow).toArray)
                    */

                    val phase1 = System.currentTimeMillis

                    val priorities = getSchedule(coflows.map(_.bytesWritten).toArray, coflows.map(_.coflowId).toArray)

                    val phase2 = System.currentTimeMillis

                    val rates = getRate(coflows.map(_.flow).toArray, priorities)

                    val phase3 = System.currentTimeMillis

                    for ((srcIp, flowRates) <- coflows.map(_.flow).zip(rates).groupBy(_._1.srcIp)) {
                        for (actor <- ipToSlave.get(srcIp)) {
                            if (flowRates.nonEmpty) {
                                actor ! FlowRateLimit(flowRates.toMap)
                            }
                        }
                    }

                    val phase4 = System.currentTimeMillis

                    logger.debug(s"For ${coflows.size} flows, " +
                        s"clustering: ${phase1 - start} ms, " +
                        s"schedule: ${phase2 - phase1} ms, " +
                        s"calculate rate: ${phase3 - phase2} ms, " +
                        s"send to slaves: ${phase4 - phase3} ms")
                }

            case DisassociatedEvent(localAddress, remoteAddress, inbound) =>

                for (ip <- remoteAddress.host) {
                    ipToBandwidth -= ip
                    ipToCoflows -= ip
                    ipToSlave -= ip
                }

        }
    }

}

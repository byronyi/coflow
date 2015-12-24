package coflow

import java.net.InetAddress

import akka.actor._
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

private[coflow] object CoflowMaster {

    val REMOTE_SYNC_PERIOD_MILLIS = 1000

    val logger = LoggerFactory.getLogger(CoflowMaster.getClass)

    val host = InetAddress.getLocalHost.getHostAddress
    val port = 1606

    val ipToSlave = TrieMap[String, ActorRef]()
    val slaveToCoflows = TrieMap[ActorRef, LocalCoflows]()

    def main(args: Array[String]) {

        val conf = ConfigFactory.parseString(
            "akka.remote.netty.tcp.port=%s".format(port)
        ).withFallback(ConfigFactory.load())

        val actorSystem = ActorSystem("coflowMaster", conf)
        actorSystem.actorOf(Props[MasterActor], "master")

        actorSystem.awaitTermination()
        // Await.result(actorSystem.whenTerminated, Duration.Inf)
    }

    def clustering(flows: Array[Flow]): Map[String, Array[Flow]] = {

        val epsilon = 100L

        val sortedIndices = flows.indices.sortBy(flows(_).startTime)
        var lastStartTime = 0L
        var currentCluster = -1

        val clusterIds = sortedIndices.map(i => {
            if (lastStartTime + epsilon < flows(i).startTime) {
                currentCluster += 1
            }
            lastStartTime = flows(i).startTime
            currentCluster.toString
        })

        // Perform an group by on indices, with key as cluster id
        clusterIds.zip(sortedIndices).groupBy(_._1).mapValues {
            indices => indices.map(idx => flows(idx._2)).toArray
        }

    }

    def getSchedule(slaves: Array[String],
                    coflows: Map[String, Map[Flow, Long]]): Map[String, Array[Flow]] = {

        // TODO: sorted on specific keys to customize the ordering of coflow
        val sortedCoflow = coflows.mapValues(_.values.sum).toArray.sortBy(_._2).map(_._1)

        val slaveFlows = slaves.map(_ -> ArrayBuffer[Flow]()).toMap

        for (coflowId <- sortedCoflow) {
            for (flow <- coflows(coflowId).toArray.sortBy(_._2).map(_._1)) {
                slaveFlows.get(flow.srcIp).foreach(_ += flow)
            }
        }
        slaveFlows.map({ case (slave, flows) => (slave, flows.toArray) })
    }

    private[coflow] class MasterActor extends Actor {

        override def preStart = {

            context.system.scheduler.schedule(1.second,
                REMOTE_SYNC_PERIOD_MILLIS.millis) {
                self ! MergeAndSync
            }
        }

        override def receive = {
            case LocalCoflows(ip, coflows) =>
                logger.trace(s"Received $coflows from $sender at $ip")
                ipToSlave(ip) = sender
                slaveToCoflows(sender) = LocalCoflows(ip, coflows)
                context.watch(sender)

            case Terminated(actor) =>
                disconnect(actor)

            case MergeAndSync =>

                val phase1 = System.currentTimeMillis

                val coflows = slaveToCoflows.values.flatMap(_.coflows).groupBy(_._1).mapValues {
                    _.map(_._2).fold(mutable.Map[Flow, Long]())(_ ++ _).toMap
                }
                val flowSize = coflows.values.fold(mutable.Map[Flow, Long]())(_ ++ _).toMap

                val cluster = clustering(flowSize.keys.toArray)

                val phase2 = System.currentTimeMillis

                val scores = getScore(coflows, cluster)
                logger.trace(s"Scores: $scores")

                val phase3 = System.currentTimeMillis

                val flowClusters = cluster.mapValues {
                    _.map(f => (f, flowSize(f))).toMap
                }
                val schedule = getSchedule(ipToSlave.keys.toArray, flowClusters)

                schedule.foreach {
                    case (ip, flows) => ipToSlave.get(ip).foreach {
                        slave => {
                            logger.trace(s"Sending ${flows.mkString(",")} to $slave")
                            slave ! StartSome(flows)
                        }
                    }
                }

                val phase4 = System.currentTimeMillis

                logger.trace(s" ${flowSize.size} flows, " +
                    s"clustering: ${phase2 - phase1} ms, " +
                    s"scoring: ${phase3 - phase2} ms, " +
                    s"schedule: ${phase4 - phase3} ms")
        }
    }

    def getScore(coflows: Map[String, Map[Flow, Long]],
                 cluster: Map[String, Array[Flow]]): Map[String, Map[String, Double]] = {

        val scores = coflows.map({ case (coflowId, flowSizes) =>
            val trueCluster = flowSizes.keys.toSet
            var precision = 0.0
            var recall = 0.0
            cluster.values.foreach(flows => {
                val predictedCluster = flows.toSet
                val intersection = trueCluster & predictedCluster
                val p = intersection.size.toDouble / predictedCluster.size
                val r = intersection.size.toDouble / trueCluster.size
                if (precision < p) {
                    precision = p
                }
                if (recall < r) {
                    recall = r
                }
            })
            (coflowId, Map("precision" -> precision, "recall" -> recall))
        })
        scores
    }

    def disconnect(actor: ActorRef) = {
        slaveToCoflows.get(actor).foreach {
            ipToSlave -= _.host
        }
        slaveToCoflows -= actor
    }

}

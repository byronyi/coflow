package coflow

import java.net.InetAddress
import java.util.concurrent.ConcurrentLinkedQueue

import akka.actor._
import akka.remote.DisassociatedEvent
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, TimeoutException}

private[coflow] object CoflowSlave {

    val REMOTE_SYNC_PERIOD_MILLIS = 1000

    val logger = LoggerFactory.getLogger(CoflowSlave.getClass)

    val host = InetAddress.getLocalHost.getHostAddress
    val port = 1607

    val masterIp = Option(System.getenv("VARYS_MASTER_IP")).getOrElse(host)
    val masterPort = 1606
    val masterUrl = "akka.tcp://coflowMaster@%s:%s/user/master".format(masterIp, masterPort)

    val addressToClient = TrieMap[Address, ActorRef]()
    val clientToCoflows = TrieMap[ActorRef, ClientCoflows]()

    val flowToClient = TrieMap[Flow, ActorRef]()
    val dstFlowQueue = TrieMap[String, ConcurrentLinkedQueue[Flow]]()

    def main(argStrings: Array[String]): Unit = {

        val conf = ConfigFactory.parseString(
            "akka.remote.netty.tcp.port=%s".format(port)
        ).withFallback(ConfigFactory.load())

        val actorSystem = ActorSystem("coflowSlave", conf)
        val slave = actorSystem.actorOf(Props[SlaveActor], "slave")

        actorSystem.eventStream.subscribe(slave, classOf[DisassociatedEvent])

        actorSystem.awaitTermination()
    }

    private[coflow] class SlaveActor extends Actor {

        val master = {
            try {
                implicit val timeout = Timeout(1.second)
                val reply = context.actorSelection(masterUrl).resolveOne()
                Some(Await.result(reply, timeout.duration))
            } catch {
                case e: TimeoutException =>
                    logger.warn("Cannot connect to master; fallback to local mode")
                    None
                case e: ActorNotFound =>
                    logger.warn("Cannot connect to master; fallback to local mode")
                    None
            }
        }

        override def preStart = {
            master.foreach(actor =>
                context.system.scheduler.schedule(1.second,
                    REMOTE_SYNC_PERIOD_MILLIS.millis) {
                    self ! MergeAndSync
                })
        }

        override def receive = {

            case ClientCoflows(coflows) =>

                coflows.values.flatMap(_.keys).foreach {
                    flowToClient(_) = sender
                }
                clientToCoflows(sender) = ClientCoflows(coflows)
                addressToClient(sender.path.address) = sender

            case d: DisassociatedEvent =>

                addressToClient.get(d.remoteAddress).foreach(disconnect)
                addressToClient -= d.remoteAddress

            case FlowPriorityQueue(flows) =>

                clientToCoflows.keys.foreach(_ ! PauseAll)

                dstFlowQueue.clear()
                for (f <- flows) {
                    dstFlowQueue(f.dstIp) = new ConcurrentLinkedQueue[Flow]
                    dstFlowQueue(f.dstIp).add(f)
                }

                for (flowQueue <- dstFlowQueue.values) {
                    Option(flowQueue.poll).foreach(startOne)
                }

            case MergeAndSync =>

                master.foreach(actor => {
                    val coflows = clientToCoflows.values.flatMap(_.coflows)
                        .groupBy(_._1).mapValues {
                        _.map(_._2).fold(mutable.Map[Flow, Long]())(_ ++ _).toMap
                    }.map(identity)
                    actor ! SlaveCoflows(host, coflows)
                })
        }

        def startOne(f: Flow) {
            flowToClient.get(f).foreach(_ ! Start(f))
        }

        def disconnect(actor: ActorRef) = {

            clientToCoflows.get(actor).foreach {
                case ClientCoflows(coflows) =>
                    coflows.values.flatMap(_.keys).foreach(flow => {
                        flowToClient -= flow
                        dstFlowQueue.get(flow.dstIp).foreach {
                            queue => Option(queue.poll).foreach(startOne)
                        }
                    })
            }

            clientToCoflows -= actor
        }

    }

}

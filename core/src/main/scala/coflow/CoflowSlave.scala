package coflow

import java.net.InetAddress
import java.util.concurrent.ConcurrentLinkedQueue

import akka.actor._
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

    val flowToClient = TrieMap[Flow, ActorRef]()
    val dstFlowQueue = TrieMap[String, ConcurrentLinkedQueue[Flow]]()
    val clientToCoflows = TrieMap[ActorRef, ClientCoflows]()

    def main(argStrings: Array[String]): Unit = {

        val conf = ConfigFactory.parseString(
            "akka.remote.netty.tcp.port=%s".format(port)
        ).withFallback(ConfigFactory.load())

        val actorSystem = ActorSystem("coflowSlave", conf)
        actorSystem.actorOf(Props[SlaveActor], "slave")

        Await.result(actorSystem.whenTerminated, Duration.Inf)
    }

    private[coflow] class SlaveActor extends Actor {

        val masterIp = Option(System.getenv("VARYS_MASTER_IP")).getOrElse(host)
        val masterPort = 1606
        val masterUrl = "akka.tcp://coflowMaster@%s:%s/user/master".format(masterIp, masterPort)

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

                coflows.foreach {
                    case (coflowId, flowSize) =>
                        flowSize.keys.foreach(flowToClient(_) = sender)
                }
                clientToCoflows(sender) = ClientCoflows(coflows)
                context.watch(sender)

            case Terminated(actor) =>
                disconnect(actor)

            case StartSome(flows) =>

                logger.trace("Received StartSome for {}", flows)

                dstFlowQueue.clear()

                for (f <- flows) {
                    flowToClient.get(f).foreach(actor => {
                        actor ! Pause(f)
                        dstFlowQueue.putIfAbsent(f.dstIp,
                            new ConcurrentLinkedQueue[Flow])
                        dstFlowQueue(f.dstIp).add(f)
                    })
                }

                for (flowQueue <- dstFlowQueue.values) {
                    Option(flowQueue.poll).foreach(startOne)
                }

            case MergeAndSync =>
                master.foreach(actor => {
                    val coflows = clientToCoflows.values
                        .flatMap(_.coflows)
                        .groupBy(_._1)
                        .map {
                            case (k, vs) =>
                                (k, vs.map(_._2).fold(
                                    mutable.Map[Flow, Long]())(_ ++ _).toMap)
                        }
                    actor ! LocalCoflows(host, coflows)
                    logger.trace("Sending LocalCoflows with {} coflows", coflows.size)
                })
        }

        def startOne(f: Flow) {
            flowToClient.get(f).foreach(_ ! Start(f))
        }

        def disconnect(actor: ActorRef) = {

            clientToCoflows.get(actor).foreach(message => {
                message.coflows.values.flatMap(_.keys).foreach(flow => {
                    flowToClient -= flow

                    dstFlowQueue.get(flow.dstIp).foreach {
                        queue => Option(queue.poll()).foreach(startOne)
                    }
                })
            })

            clientToCoflows -= actor
            logger.trace("Client {} disconnected", actor)
        }

    }

}

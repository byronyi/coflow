package coflow

import java.net.{InetAddress, InetSocketAddress, SocketAddress}

import akka.actor._
import akka.util.Timeout
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, TimeoutException}

object CoflowClient {

    private val REMOTE_SYNC_PERIOD_MILLIS = 1000

    private val logger = LoggerFactory.getLogger(CoflowClient.getClass)

    val host = InetAddress.getLocalHost.getHostAddress
    val slavePort = CoflowSlave.port
    val slaveUrl = "akka.tcp://coflowSlave@%s:%s/user/slave".format(host, slavePort)

    private val flowToChannel = TrieMap[Flow, CoflowChannel]()
    private val flowToCoflow = TrieMap[Flow, String]()

    ActorSystem("coflowClient").actorOf(Props[ClientActor])

    def register(source: SocketAddress,
                 destination: SocketAddress,
                 coflowId: String) = {
        val src = source.asInstanceOf[InetSocketAddress]
        val dst = destination.asInstanceOf[InetSocketAddress]
        val flow = Flow(src.getAddress.getHostAddress, src.getPort,
            dst.getAddress.getHostAddress, dst.getPort)
        flowToCoflow(flow) = coflowId
        logger.trace(s"$flow registered with coflow id $coflowId")
    }

    private[coflow] def open(channel: CoflowChannel) {
        if (channel.flow.srcPort == CoflowSlave.port ||
            channel.flow.dstPort == CoflowSlave.port ||
            channel.flow.srcPort == CoflowMaster.port ||
            channel.flow.dstPort == CoflowMaster.port) {
        }
        else {
            flowToChannel(channel.flow) = channel
        }
    }

    private[coflow] def close(channel: CoflowChannel) {
        flowToChannel -= channel.flow
        flowToCoflow -= channel.flow
    }

    private[coflow] class ClientActor extends Actor {

        val slave = {
            try {
                implicit val timeout = Timeout(1.second)
                val reply = context.actorSelection(slaveUrl).resolveOne()
                Some(Await.result(reply, timeout.duration))
            } catch {
                case e: TimeoutException =>
                    logger.warn("Cannot connect to slave; fallback to nonblocking mode")
                    None
                case e: ActorNotFound =>
                    logger.warn("Cannot connect to slave; fallback to nonblocking mode")
                    None
            }
        }

        override def preStart = {
            slave.foreach(actor =>
                context.system.scheduler.schedule(1.second,
                    REMOTE_SYNC_PERIOD_MILLIS.millis) {
                    self ! MergeAndSync
                }
            )
        }

        override def receive = {
            case Start(flow) =>
                flowToChannel.get(flow).foreach(_.start())
            case Pause(flow) =>
                flowToChannel.get(flow).foreach(_.pause())
            case MergeAndSync =>
                slave.foreach(actor => {
                    val coflows = flowToCoflow.groupBy(_._2).mapValues {
                        case flows =>
                            flows.keys.map { flow =>
                                (flow, flowToChannel.get(flow).map(_.getBytesSent).sum)
                            }.toMap
                    }.map(identity) // Workaround for https://issues.scala-lang.org/browse/SI-7005
                    actor ! ClientCoflows(coflows)
                })
        }
    }

}

package coflow

import java.net.InetAddress

import akka.actor._
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object CoflowClient {

    val REMOTE_SYNC_PERIOD_MILLIS = Option(System.getenv("COFLOW_SYNC_PERIOD_MS")).getOrElse("1000").toInt

    val MIN_BYTES_SENT_REPORTING_THRESHOLD = 10 * 1024 * 1024

    val host = InetAddress.getLocalHost.getHostAddress
    val slavePort = 1607
    val slaveUrl = s"akka.tcp://coflowSlave@$host:$slavePort/user/slave"
    val client = Future {
        ActorSystem("coflowClient").actorOf(Props[ClientActor])
    }
    private val logger = LoggerFactory.getLogger(CoflowClient.getClass)
    private val flowToChannel = TrieMap[Flow, CoflowChannel]()
    private val flowToCoflow = TrieMap[Flow, String]()

    private[coflow] def register(flow: Flow, coflowId: String) = {
        flowToCoflow(flow) = coflowId
        logger.trace(s"$flow registered with coflow id $coflowId")
        client.andThen {
            case Success(actor) => actor ! FlowRegister(flow)
            case Failure(e) => logger.warn("cannot send flow started notification to client actor", e)
        }
    }

    private[coflow] def getChannel(flow: Flow): CoflowChannel = {
        flowToChannel.get(flow).orNull
    }

    private[coflow] def open(channel: CoflowChannel) = {

        val flow = channel.flow
        logger.trace(s"$flow started writing")

        flowToChannel(flow) = channel
    }

    private[coflow] def close(channel: CoflowChannel) = {

        val flow = channel.flow
        logger.trace(s"$flow finishes with size ${channel.getBytesSent} bytes")

        flowToChannel -= flow
        flowToCoflow -= flow
        client.andThen {
            case Success(actor) => actor ! FlowEnd(flow)
            case Failure(e) => logger.warn("cannot send flow ended notification to client actor", e)
        }
    }

    private[coflow] class ClientActor extends Actor {

        val slave = context.actorSelection(slaveUrl).resolveOne(1.second)

        override def preStart = {
            context.system.scheduler.schedule(0.second, REMOTE_SYNC_PERIOD_MILLIS.millis, self, MergeAndSync)
        }

        override def receive = {

            case FlowRegister(flow) =>
                slave.andThen {
                    case Success(actor) => actor ! FlowRegister(flow)
                    case _ =>
                }

            case FlowEnd(flow) =>
                slave.andThen {
                    case Success(actor) => actor ! FlowEnd(flow)
                    case _ =>
                }

            case MergeAndSync =>
                for (actor <- slave) {
                    val clientCoflows = flowToCoflow.flatMap({
                        case (flow, coflowId) =>
                            val channel = flowToChannel.get(flow)
                            val bytesSent = channel.map(_.getBytesSent).filter(_ > MIN_BYTES_SENT_REPORTING_THRESHOLD)
                            bytesSent.map(bytesWritten => Coflow(flow, coflowId, bytesWritten))
                    })
                    actor ! ClientCoflows(clientCoflows.toArray)
                }
        }
    }

}

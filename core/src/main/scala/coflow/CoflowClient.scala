package coflow

import java.net.{InetAddress, InetSocketAddress, SocketAddress}

import akka.actor._
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object CoflowClient {

    val REMOTE_SYNC_PERIOD_MILLIS = 1000

    val host = InetAddress.getLocalHost.getHostAddress
    val slavePort = CoflowSlave.port
    val slaveUrl = s"akka.tcp://coflowSlave@$host:$slavePort/user/slave"

    private val logger = LoggerFactory.getLogger(CoflowClient.getClass)

    private val flowToChannel = TrieMap[Flow, CoflowChannel]()
    private val flowToCoflow = TrieMap[Flow, String]()

    private val actorSystem = ActorSystem("coflowClient")
    actorSystem.actorOf(Props[ClientActor])

    private[coflow] def register(source: SocketAddress, destination: SocketAddress, coflowId: String) = {
        val src = source.asInstanceOf[InetSocketAddress]
        val dst = destination.asInstanceOf[InetSocketAddress]
        val flow = Flow(src.getAddress.getHostAddress, src.getPort, dst.getAddress.getHostAddress, dst.getPort)

        flowToCoflow(flow) = coflowId
        logger.trace(s"$flow registered with coflow id $coflowId")
    }

    private[coflow] def open(channel: CoflowChannel) = {
        val flow = channel.flow()
        if (flow.srcPort == CoflowSlave.port || flow.dstPort == CoflowSlave.port ||
            flow.srcPort == CoflowMaster.port || flow.dstPort == CoflowMaster.port) {
        } else {
            flowToChannel(flow) = channel
        }
        logger.trace(s"$flow started writing")
    }

    private[coflow] def close(channel: CoflowChannel) = {
        val flow = channel.flow
        flowToChannel -= flow
        flowToCoflow -= flow
        logger.trace(s"$flow finishes with size ${channel.getBytesSent} bytes")
    }

    private[coflow] class ClientActor extends Actor {

        val slave = context.actorSelection(slaveUrl).resolveOne(1.second)

        override def preStart = {
            context.system.scheduler.schedule(0.second, REMOTE_SYNC_PERIOD_MILLIS.millis, self, MergeAndSync)
        }

        override def receive = {
            case Start(flow) =>
                flowToChannel.get(flow).foreach(_.start())

            case PauseAll =>
                // Only those registered flows may be paused
                flowToCoflow.keys.foreach {
                    flowToChannel.get(_).foreach(_.pause())
                }

            case MergeAndSync =>
                slave.foreach(actor => {
                    val coflows = flowToCoflow.groupBy(_._2).mapValues {
                        _.keys.map { flow =>
                            (flow, flowToChannel.get(flow).map(_.getBytesSent).sum)
                        }.toMap
                    }.map(identity) // Workaround for https://issues.scala-lang.org/browse/SI-7005
                    actor ! ClientCoflows(coflows)
                })
        }
    }

}

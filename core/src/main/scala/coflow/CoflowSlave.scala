package coflow

import java.net.InetAddress
import java.util.concurrent.atomic.AtomicInteger

import akka.actor._
import akka.remote.DisassociatedEvent
import com.typesafe.config.ConfigFactory
import netlink.{NetlinkSocket, NetlinkUtils}
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

private[coflow] object CoflowSlave {

    val REMOTE_SYNC_PERIOD_MILLIS = Option(System.getenv("COFLOW_SYNC_PERIOD_MS")).getOrElse("1000").toInt

    val host = InetAddress.getLocalHost.getHostAddress
    val port = 1607
    val masterIp = Option(System.getenv("COFLOW_MASTER_IP")).getOrElse(host)
    val masterPort = 1606
    val masterUrl = s"akka.tcp://coflowMaster@$masterIp:$masterPort/user/master"

    val tcInterface = Option(System.getenv("COFLOW_TC_INTERFACE")).getOrElse("eth0")
    val tcParent = Option(System.getenv("COFLOW_TC_PARENT_CLASS")).getOrElse("1:1")
    val tcBandwidth = Option(System.getenv("COFLOW_TC_BANDWIDTH_BYTES")).getOrElse("131072000").toInt

    private val logger = LoggerFactory.getLogger(CoflowSlave.getClass)

    private val addressToClient = TrieMap[Address, ActorRef]()
    private val addressToCoflows = TrieMap[Address, ClientCoflows]()

    def main(argStrings: Array[String]) = {

        val conf = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").withFallback(ConfigFactory.load())

        val actorSystem = ActorSystem("coflowSlave", conf)
        val slave = actorSystem.actorOf(Props(classOf[SlaveActor]), "slave")

        actorSystem.eventStream.subscribe(slave, classOf[DisassociatedEvent])
        actorSystem.awaitTermination()
    }

    private[coflow] class SlaveActor extends Actor {

        Try {
            System.loadLibrary("nl4j")
        } recover {
            case e: UnsatisfiedLinkError =>
                logger.error("cannot init nl4j; no rate enforcement")
                sys.exit(-1)
        }

        val master = context.actorSelection(masterUrl).resolveOne(1.second)

        val tcIfIndex = NetlinkUtils.getIfindexByName(tcInterface).asInstanceOf[Int]
        val tcParentClassId = NetlinkUtils.stringToClass(tcParent)

        val flowToEnforcer = mutable.Map[Flow, HTBRateEnforcer]()
        val parentFlowId = tcParentClassId & 0xffff
        val flowId = new AtomicInteger(parentFlowId)

        override def preStart = {

            val nlSocket = new NetlinkSocket
            nlSocket.connect()

            val registration = master.andThen({
                case Success(actor) => actor ! SlaveRegister(tcBandwidth)
                case Failure(e) =>
                    logger.warn("cannot connect to master")
                    sys.exit(-1)
            })
            Await.result(registration, 1.seconds)

            context.system.scheduler.schedule(0.second, REMOTE_SYNC_PERIOD_MILLIS.millis, self, MergeAndSync)

        }

        override def receive = {

            case FlowRegister(flow) =>
                if (!flowToEnforcer.contains(flow)) {
                    val tcFlowId = {
                        val first = flowId.incrementAndGet() & 0xffff
                        if (first != parentFlowId) {
                            first
                        } else {
                            flowId.incrementAndGet() & 0xffff
                        }
                    }
                    val enforcer = new HTBRateEnforcer(flow, tcIfIndex, tcParentClassId, tcFlowId, tcBandwidth)
                    flowToEnforcer(flow) = enforcer
                    enforcer.start()
                }

            case FlowEnd(flow) =>
                for (enforcer <- flowToEnforcer.get(flow)) {
                    enforcer.stop()
                }
                flowToEnforcer -= flow

            case ClientCoflows(coflows) =>
                if (coflows.nonEmpty) {
                    logger.debug(s"received client coflows with ${coflows.length} flows")
                }
                val address = sender.path.address
                addressToCoflows(address) = ClientCoflows(coflows)
                addressToClient(address) = sender

            case FlowRateLimit(flowToRate) =>
                logger.debug(s"received flow rate limits with ${flowToRate.size} flows")
                for ((flow, rate) <- flowToRate;
                     enforcer <- flowToEnforcer.get(flow)) {
                    enforcer.setRate(rate)
                }

            case MergeAndSync =>
                for (actor <- master) {
                    val coflows = addressToCoflows.values.flatMap(_.coflows)
                    actor ! SlaveCoflows(coflows.toArray)
                }

            case DisassociatedEvent(localAddress, remoteAddress, inbound) =>
                for (clientCoflows <- addressToCoflows.get(remoteAddress);
                     Coflow(flow, _, _) <- clientCoflows.coflows) {
                    for (enforcer <- flowToEnforcer.get(flow)) {
                        enforcer.stop()
                    }
                    flowToEnforcer -= flow
                }
                addressToClient -= remoteAddress
                addressToCoflows -= remoteAddress

        }
    }

}


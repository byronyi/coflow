package coflow

import java.io.IOException
import java.net.InetSocketAddress

import netlink.{HTBTcClass, NetlinkSocket, U32TcFilter}
import org.slf4j.LoggerFactory

import scala.util.Try

private[coflow] class HTBRateEnforcer(flow: Flow, tcIfIndex: Int, tcParentClassId: Int, tcFlowId: Int, tcCeilRate: Long) {

    private val nlSocket = new NetlinkSocket
    private val tcFilter = new U32TcFilter
    private val tcClass = new HTBTcClass
    private val tcQdiscId = tcParentClassId & 0xffff0000

    private val logger = LoggerFactory.getLogger(classOf[HTBRateEnforcer])

    def start() = {
        tcFilter.asTcObject().setIfindex(tcIfIndex)
        tcFilter.addKeySrcAddr(new InetSocketAddress(flow.srcIp, flow.srcPort))
            .addKeyDestAddr(new InetSocketAddress(flow.dstIp, flow.dstPort))
            .setPriority(tcFlowId)
        tcFilter.setClass(tcQdiscId | tcFlowId).setTerminal()

        tcClass.asTcObject().setIfindex(tcIfIndex).setParent(tcParentClassId).setHandle(tcQdiscId | tcFlowId)
        tcClass.setCeil(tcCeilRate)

        nlSocket.connect()
        Try {
            tcFilter.add(nlSocket, 0x400)
            logger.trace(s"added tc_filter for $flow")
        } recover {
            case e: IOException => logger.warn("cannot add tc_filter", e)
        }
        setRate(tcCeilRate)
    }

    def setRate(rate: Long) = {
        tcClass.setRate(rate)
        Try {
            tcClass.add(nlSocket, 0x400)
            logger.trace(s"changed $flow rate limit to $rate byte/s")
        } recover {
            case e: IOException => logger.warn("cannot modify tc_class during setRate", e)
        }
    }

    def stop() = {
        Try {
            tcFilter.delete(nlSocket, 0)
            logger.trace(s"deleted tc_filter for $flow")
        } recover {
            case e: IOException => logger.warn("cannot delete tc_filter", e)
        }
        try {
            tcClass.delete(nlSocket)
            logger.trace(s"deleted tc_class for $flow")
        } catch {
            case e: IOException => logger.warn("cannot delete tc_class", e)
        }
    }
}

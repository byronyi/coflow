package coflow

private[coflow] case class Flow(srcIp: String, srcPort: Int, dstIp: String, dstPort: Int) extends Serializable {
    val startTime = System.currentTimeMillis
}

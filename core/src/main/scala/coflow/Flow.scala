package coflow

case class Flow(srcIp: String, srcPort: Int,
                dstIp: String, dstPort: Int) extends Serializable {
    val startTime = System.currentTimeMillis

    def lifetime = System.currentTimeMillis - startTime
}

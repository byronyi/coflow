package coflow

private[coflow] case class Coflow(flow: Flow, coflowId: String, bytesWritten: Long) extends Serializable

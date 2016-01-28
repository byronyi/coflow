package coflow

private[coflow] sealed trait CoflowMessage extends Serializable

// Internal
private[coflow] object MergeAndSync extends CoflowMessage

// Slave <-> Master
private[coflow] case class SlaveRegister(bandwidth: Long) extends CoflowMessage

private[coflow] case class SlaveCoflows(coflows: Array[Coflow]) extends CoflowMessage

private[coflow] case class FlowRateLimit(flowToRate: Map[Flow, Long]) extends CoflowMessage

// Client <-> Slave
private[coflow] case class ClientCoflows(coflows: Array[Coflow]) extends CoflowMessage

private[coflow] case class FlowRegister(flow: Flow) extends CoflowMessage

private[coflow] case class FlowEnd(flow: Flow) extends CoflowMessage


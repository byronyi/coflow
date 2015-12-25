package coflow

private[coflow] sealed trait CoflowMessage extends Serializable

// Internal
private[coflow] object MergeAndSync extends CoflowMessage

// Slave <-> Master
private[coflow] case class SlaveCoflows(host: String,
                                        coflows: Map[String, Map[Flow, Long]])
    extends CoflowMessage

private[coflow] case class FlowPriorityQueue(flows: Array[Flow])
    extends CoflowMessage

// Client <-> Slave
private[coflow] case class ClientCoflows(coflows: Map[String, Map[Flow, Long]])
    extends CoflowMessage

private[coflow] object PauseAll extends CoflowMessage

private[coflow] case class Start(flow: Flow) extends CoflowMessage

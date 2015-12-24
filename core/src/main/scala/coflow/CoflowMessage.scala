package coflow

private[coflow] sealed trait CoflowMessage extends Serializable

// Internal
private[coflow] object MergeAndSync

// Slave <-> Master
private[coflow] case class LocalCoflows(host: String,
                                        coflows: Map[String, Map[Flow, Long]])
    extends CoflowMessage

private[coflow] case class StartSome(flows: Array[Flow])
    extends CoflowMessage

// Client <-> Slave
private[coflow] case class ClientCoflows(coflows: Map[String, Map[Flow, Long]])
    extends CoflowMessage

private[coflow] case class Pause(flow: Flow)

private[coflow] case class Start(flow: Flow)

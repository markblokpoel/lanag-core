package com.markblokpoel.lanag.core

import com.markblokpoel.lanag.util.InteractionIdentifier

/** Defines agent interactions.
  *
  * Implementations of this class specify the agent types, including the intention and signal type they use.
  *
  * @param agent1     One of two agents that is part of this interaction, must be a subtype of [[com.markblokpoel.lanag.core.Agent]].
  * @param agent2     One of two agents that is part of this interaction, must be a subtype of [[com.markblokpoel.lanag.core.Agent]].
  * @param originData The data specifies the agents origin, must be a subtype of [[com.markblokpoel.lanag.core.Data]].
  *                   Default value is [[com.markblokpoel.lanag.core.NoData]].
  * @tparam I The intention type used by the agents, must be a subtype of [[com.markblokpoel.lanag.core.Intention]].
  * @tparam S The signal type used by the agents, must be a subtype of [[com.markblokpoel.lanag.core.Signal]].
  * @tparam A The type of agents, must be a subtype of [[com.markblokpoel.lanag.core.Agent]].
  * @author Mark Blokpoel
  */
@SerialVersionUID(100L)
abstract class Interaction[I <: Intention, S <: Signal, A <: Agent[I, S]](
    val agent1: A,
    val agent2: A,
    val originData: Data = NoData())
    extends Serializable {

  /** The unique ID number of this pair / interaction. */
  val pairId: Long = InteractionIdentifier.nextId

  /** Specifies what type of speakers this interaction uses. Must be a subtype of the base agent type. */
  type SpeakerType <: Speaker[I, S] with A

  /** Specifies what type of listeners this interaction uses. Must be a subtype of the base agent type. */
  type ListenerType <: Listener[I, S] with A

  /** Specifies agent1 in speaker mode. */
  val agent1AsSpeaker: SpeakerType

  /** Specifies agent2 in speaker mode. */
  val agent2AsSpeaker: SpeakerType

  /** Specifies agent1 in listener mode. */
  val agent1AsListener: ListenerType

  /** Specifies agent2 in listener mode. */
  val agent2AsListener: ListenerType

  /** The current speaker. */
  protected var currentSpeaker: SpeakerType

  /** The current listener. */
  protected var currentListener: ListenerType

  /** Switches the roles of the agents. Speaker becomes listener and listener becomes speaker.
    *
    */
  protected def switchRoles(): Unit = {
    if (currentSpeaker == agent1AsSpeaker) {
      currentSpeaker = agent2AsSpeaker
      currentListener = agent1AsListener
    } else {
      currentSpeaker = agent1AsSpeaker
      currentListener = agent2AsListener
    }
  }

  /** Executes a turn in the interaction.
    *
    * @return Data reflecting the results of this turn.
    */
  def turn: Data

  /** Specifies when the interaction is over.
    *
    * @return True when the interaction is finished.
    */
  def stoppingCriterion: Boolean

  /** Simulates all turns in the interaction and returns a collection (possibly a summary)
    * of all the data gathered.
    */
  def runAndCollectData: Data

}

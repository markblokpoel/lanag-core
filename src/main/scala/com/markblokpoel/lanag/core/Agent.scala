package com.markblokpoel.lanag.core

/** Defines the Agent class architecture.
  *
  * This is an immutable class. Agents are parameterized by the type
  * of [[com.markblokpoel.lanag.core.Intention]] and [[com.markblokpoel.lanag.core.Signal]] they use.
  *
  * @tparam I The intention type must be a subtype of [[com.markblokpoel.lanag.core.Intention]].
  * @tparam S The signal type must be a subtype of [[com.markblokpoel.lanag.core.Signal]].
  * @author Mark Blokpoel
  */
@SerialVersionUID(100L)
abstract class Agent[I <: Intention, S <: Signal] extends Serializable {

  /** Returns an immutable copy of the agent, but as a speaker. */
  def asSpeaker: Speaker[I, S]

  /** Returns an immutable copy of the agent, but as a listener. */
  def asListener: Listener[I, S]
}

/** A special case of [[Agent]] where it is in speaker mode.
  *
  * This is an immutable class. Speakers are parameterized by the type
  * of [[com.markblokpoel.lanag.core.Intention]] and [[com.markblokpoel.lanag.core.Signal]] they use.
  *
  * @tparam I The intention type must be a subtype of [[com.markblokpoel.lanag.core.Intention]].
  * @tparam S The signal type must be a subtype of [[com.markblokpoel.lanag.core.Signal]].
  * @author Mark Blokpoel
  */
trait Speaker[I <: Intention, S <: Signal] extends Agent[I, S] {

  /** Selects an intention for the agent to communicate. */
  def selectIntention: I

  /** Produces a signal, based on an intention.
    *
    * @param intention The [[com.markblokpoel.lanag.core.Intention]] to be communicated.
    * @return A tuple that consisting of the [[com.markblokpoel.lanag.core.Signal]] selected by
    *         the communicator and some [[com.markblokpoel.lanag.core.Data]] that contains
    *         information of the signal production process.
    */
  def produceSignal(intention: I): (S, Data)
}

/** A special case of [[Agent]] where it is in listener mode.
  *
  * This is an immutable class. Listeners are parameterized by the type
  * of [[com.markblokpoel.lanag.core.Intention]] and [[com.markblokpoel.lanag.core.Signal]] they use.
  *
  * @tparam I The intention type must be a subtype of [[com.markblokpoel.lanag.core.Intention]].
  * @tparam S The signal type must be a subtype of [[com.markblokpoel.lanag.core.Signal]].
  * @author Mark Blokpoel
  */
trait Listener[I <: Intention, S <: Signal] extends Agent[I, S] {

  /** Interprets a signal.
    *
    * @param signal The [[com.markblokpoel.lanag.core.Signal]] to be interpreted.
    * @return A tuple that consisting of the [[com.markblokpoel.lanag.core.Intention]] selected
    *         by the listener and some [[com.markblokpoel.lanag.core.Data]] that contains
    *         information of the signal interpretation process.
    */
  def interpretSignal(signal: S): (I, Data)
}

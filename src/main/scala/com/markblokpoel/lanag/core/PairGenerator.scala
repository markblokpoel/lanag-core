package com.markblokpoel.lanag.core

/** Defines a pair generator.
  *
  * Implementations of this specify a parameter space and how, for any value in the parameter space,
  * a pair of agents can be randomly generated. This class is parameterized by the parameter type, intention
  * type, signal type, agent type and data type.
  *
  * @param sampleSize The number of agent pairs that can be sampled from each point in the parameter space.
  * @tparam P Specifies the type of parameters in the parameter space, must be a subtype of [[com.markblokpoel.lanag.core.Parameters]].
  * @tparam I Specifies the type of intention the agents generated use, must be a subtype of [[com.markblokpoel.lanag.core.Intention]].
  * @tparam S Specifies the type of signal the agents generated use, must be a subtype of [[com.markblokpoel.lanag.core.Signal]].
  * @tparam A Specifies the type agents generated, must be a subtype of [[com.markblokpoel.lanag.core.Agent]].
  * @tparam D Specifies the data generated, must be a subtype of [[com.markblokpoel.lanag.core.Data]].
  * @author Mark Blokpoel
  */
@SerialVersionUID(100L)
abstract class PairGenerator[P <: Parameters, I <: Intention, S <: Signal,
A <: Agent[I, S], D <: Data](sampleSize: Int)
    extends Serializable {

  /** Generates the parameter space, specifying the full domain of parameters used to generate pairs of agents.
    *
    * @return A sequence of parameters.
    */
  def generateParameterSpace: Seq[P]

  /** Generates a pair of agents randomly, given the parameters.
    *
    * @param parameters Parameters from [[generateParameterSpace]].
    * @return An [[com.markblokpoel.lanag.core.AgentPair]], containing a pair of agents of the specified type
    *         and [[com.markblokpoel.lanag.core.Data]] reflecting the pair's origin parameters.
    */
  def generatePair(parameters: P): AgentPair[I, S, A, D]

  /** Returns an iterator that, given a point in [[generateParameterSpace]], generates <code>sampleSize</code>
    * pairs of agents.
    *
    * @param parameters from [[generateParameterSpace]].
    */
  def sampleGenerator(parameters: P): Iterator[AgentPair[I, S, A, D]] = {
    new Iterator[AgentPair[I, S, A, D]] {
      private var amountSampled = 0

      override def hasNext: Boolean = amountSampled < sampleSize

      override def next(): AgentPair[I, S, A, D] = {
        amountSampled += 1
        generatePair(parameters)
      }
    }
  }
}

/** Data structure for storing pairs of agents, their type and their origin.
  *
  * @author Mark Blokpoel
  */
case class AgentPair[I <: Intention, S <: Signal, A <: Agent[I, S], D <: Data](
    agent1: A,
    agent2: A,
    originData: D)

/** Prototype type for parameters. All implementations of [[PairGenerator]] should also specify
  * a case class that implements this.
  *
  * @author Mark Blokpoel
  */
trait Parameters {}

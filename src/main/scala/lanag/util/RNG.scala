package lanag.core.util

/** A global Random Number Generator.
  *
  * Can be used to generate random numbers of various types:
  * {{{
  *   val p = RNG.nextProbability
  *   val i = RNG.nextInt(10)
  *   val b = RNG.nextBoolean
  * }}}
  *
  * You can set the seed of the random number generator using:
  * {{{
  *   RNG.setSeed(1024)
  * }}}
  * If simulations use RNG exclusively, the results are the same if all parameters and the seed are the same.
  *
  * @author Mark Blokpoel
  */
object RNG {
  private val r = scala.util.Random

  /** Sets the global random number generator's seed number. */
  def setSeed(seed: Long) { r.setSeed(seed) }

  /** Returns a random probability. */
  def nextProbability : Double = { r.nextDouble() }

  /** Returns a random integer between 0 and upperbound (inclusive). */
  def nextInt(upperbound: Int) : Int = { r.nextInt(upperbound) }

  /** Returns a random boolean value. */
  def nextBoolean: Boolean = { r.nextBoolean() }

  /** Returns true with probability p or false otherwise. */
  def nextBoolean(p: Double): Boolean = { if(nextProbability < p) true else false }
}

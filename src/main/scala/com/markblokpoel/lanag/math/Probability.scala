package com.markblokpoel.lanag.math

import scala.math._
import scala.util.Random

/** A collection of utility functions related to probability theory.
  *
  * @author Mark Blokpoel
  */
object Probability {
  /** Returns an index to the vector, pointing to the highest value in the vector.
    * If multiple maxima exist, it returns an index to one of those at random.
    * If the vector values is empty, it returns None.
    *
    * @param values Values represented in a vector of doubles (e.g., a probability distribution).
    */
  def argMax(values: Vector[Double]): Option[Int] = {
    if (values.isEmpty)
      None
    else {
      val maxValue = values.max
      val maxValSet = values.zipWithIndex filter (_._1 == maxValue)
      // If one or more maxima exist return random
      Some(maxValSet(Random.nextInt(maxValSet.length))._2)
    }
  }

  /**
    * Returns an index to the distribution according to soft argMax with parameter beta. If beta -> Inf,
    * this function is equivalent to argMax.  If the vector values is empty, it returns None.
    *
    * @param values Values represented in a vector of doubles (e.g., a probability distribution).
    * @param beta   The beta parameter, >=0. Soft argmax is ill-defined for negative beta values.
    * @return An index pointing to the value in the distribution
    * @see See this Wikipedia page for a mathmatical definition of soft argmax
    *      [[https://en.wikipedia.org/wiki/Softmax_function]].
    */
  def softArgMax(values: Vector[Double], beta: Double): Option[Int] = {
    if (values.isEmpty)
      None
    else {
      val softenedDistribution = values.map {
        p => exp(beta * p) / values.map(q => exp(q * beta)).sum
      }

      if (softenedDistribution.exists(_.isNaN))
        argMax(values)
      else {
        val arrow = Random.nextDouble

        def findTarget(d: List[Double], ind: Int, acc: Double): Int = {
          d match {
            case head :: tail => if (arrow < acc) ind - 1 else findTarget(tail, ind + 1, acc + head)
            case _ => ind - 1
          }
        }

        Some(findTarget(softenedDistribution.toList, 0, 0))
      }
    }
  }

  /** Returns the Shannon information entropy of a distribution.
    *
    * For distributions that deviate from probability assumptions (i.e., the sum of the values
    * equals 1.0), Shannon information entropy is ill-defined.
    *
    * @param distribution The probability distribution, the values in this list should add to 1.0.
    */
  def entropy(distribution: Vector[Double]): Double = {
    distribution.fold(0.0) { (e, p) => e - (if (p > 0) p * math.log(p) / math.log(2) else 0) }
  }
}

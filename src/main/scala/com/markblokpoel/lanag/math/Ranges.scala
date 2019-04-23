package com.markblokpoel.lanag.math

/** A collection of utility functions related to ranges.
  *
  * @author Mark Blokpoel
  */
object Ranges {

  /** Returns a sequence of doubles representing (explicitly) a range of values.
    *
    * @param step       The step size of the range, should not be equal to 0.
    * @param lowerbound The lower bound of the range, default = 0.
    * @param upperbound The upper bound of the range, default = 1.
    * @throws scala.IllegalArgumentException when <code>step</code> equals 0.
    */
  def range(step: Double,
            lowerbound: Double = 0,
            upperbound: Double = 1): Seq[Double] = {
    require(step != 0,
            "Step size in range equals zero, cannot compute infinite range.")

    if (lowerbound < upperbound)
      lowerbound +: range(step, lowerbound + step, upperbound)
    else if (lowerbound == upperbound) Seq(upperbound)
    else Seq.empty[Double]
  }
}

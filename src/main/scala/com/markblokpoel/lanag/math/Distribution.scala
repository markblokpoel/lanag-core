package com.markblokpoel.lanag.math

import com.markblokpoel.lanag.util.RNG

/** A probability distribution over a domain of objects. If the initial distribution does not add
  * up to 1.0, then it will be normalized.
  *
  * @param domain A vector of objects that makes up the domain of this distribution.
  * @param notNormDistribution A (possibly not normalized) distribution.
  * @tparam A The type of the domain this distribution is defined over.
  */
case class Distribution[A](
    domain: Vector[A],
    protected val notNormDistribution: Vector[BigDecimal]) {
  assert(domain.length == notNormDistribution.length)

  /** The (normalized) probability distribution.
    */
  val distribution: Vector[BigDecimal] = {
    val sum = notNormDistribution.sum
    if (sum > 0) notNormDistribution.map(_ / sum)
    else notNormDistribution
  }

  /** Request the probability of elem.
    *
    * @param elem The element in the domain for which the probability is requested.
    * @return Probability of elem.
    */
  def apply(elem: A): Option[BigDecimal] = {
    val i = domain.indexOf(elem)
    if (i >= 0) Some(distribution(i))
    else None
  }

  /** Returns the number of elements in the distribution.
    *
    * @return
    */
  def length: Int = domain.length

  /** Draws a sample from the distribution, proportionate to the probabilities.
    *
    * @return
    */
  def sample: A = {
    val pt = RNG.nextProbability

    @scala.annotation.tailrec
    def sample(acc: BigDecimal,
               dom: Vector[A],
               distr: Vector[BigDecimal]): A = {
      if (dom.length == 1) dom.head
      else if (acc <= pt && pt < acc + distr.head) dom.head
      else sample(acc + distr.head, dom.tail, distr.tail)
    }

    sample(0.0, domain, distribution)
  }

  /** Draws {{{n}}} samples from the distribution.
    *
    * @param n The number of samples to draw.
    * @return A list of {{{n}}} samples from this distribution
    */
  def sample(n: Int): List[A] = (for (_ <- 0 until n) yield sample).toList

  /** Returns an index to the vector, pointing to the highest value in the vector.
    * If multiple maxima exist, it returns an index to one of those at random.
    * If the vector distribution is empty, it returns None.
    */
  def argMax: Option[A] = {
    if (distribution.isEmpty)
      None
    else {
      val maxValue = distribution.max
      val maxValSet = distribution.zipWithIndex filter (_._1 == maxValue)
      // If one or more maxima exist return random
      Some(domain(maxValSet(scala.util.Random.nextInt(maxValSet.length))._2))
    }
  }

  /** Returns the distribution multiplied by a value. Note that the normalized distribution will not change.
    *
    * @param value A number with which the distribution should be multiplied.
    * @return
    */
  def prodNotNorm(value: BigDecimal): Distribution[A] =
    Distribution[A](domain, notNormDistribution.map(_ * value))

  /** Adds two not normalized distributions and returns the resulting distribution.
    *
    * @param that The distribution to add to this one.
    * @return
    */
  def addNotNorm(that: Distribution[A]): Distribution[A] = {
    require(this.domain == that.domain,
            "Cannot add distributions with different domains.")
    val d = (this.notNormDistribution zip that.notNormDistribution).map(d =>
      d._1 + d._2)
    Distribution[A](this.domain, d)
  }

  /**
    * Returns an index to the distribution according to soft argMax with parameter beta. If beta -> Inf,
    * this function is equivalent to argMax.  If the vector values is empty, it returns None.
    *
    * @param beta   The beta parameter, >=0. Soft argmax is ill-defined for negative beta values.
    * @return An index pointing to the value in the distribution
    * @see See this Wikipedia page for a mathmatical definition of soft argmax
    *      [[https://en.wikipedia.org/wiki/Softmax_function]].
    */
  def softArgMax(beta: BigDecimal): Option[A] = {
    if (distribution.isEmpty)
      None
    else {
      val softenedDistribution = distribution.map { p =>
        scala.math.exp(beta.doubleValue() * p.doubleValue()) / distribution
          .map(q => scala.math.exp(q.doubleValue() * beta.doubleValue()))
          .sum
      }

      if (softenedDistribution.exists(_.isNaN))
        argMax
      else {
        val arrow = scala.util.Random.nextDouble

        @scala.annotation.tailrec
        def findTarget(d: List[Double], ind: Int, acc: Double): Int = {
          d match {
            case head :: tail =>
              if (arrow < acc) ind - 1
              else findTarget(tail, ind + 1, acc + head)
            case _ => ind - 1
          }
        }

        Some(domain(findTarget(softenedDistribution.toList, 0, 0)))
      }
    }
  }

  /** Returns the Shannon information entropy of a distribution.
    *
    * For distributions that deviate from probability assumptions (i.e., the sum of the values
    * equals 1.0), Shannon information entropy is ill-defined.
    */
  def entropy: BigDecimal = {
    distribution.foldLeft(BigDecimal(0.0)) { (acc, p) =>
      acc - (if (p > 0) p * math.log(p.doubleValue()) / math.log(2) else 0)
    }
  }

  override def toString: String =
    (domain zip distribution)
      .map(p => s"P(${p._1})=${p._2}")
      .mkString(", ")
}

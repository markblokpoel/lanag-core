package com.markblokpoel.lanag.rsa

import com.markblokpoel.lanag.util.RNG

import scala.math.min

/** A special type of [[Lexicon]] where signals and referents are represented by binary strings.
  * Signal-referent relations are based on a measure of representational similarity.
  *
  * @param vocabularyRepresentations A vector containing binary string representations of the signals in the vocabulary.
  * @param contextRepresentations    A vector containing binary string representations of the referents in the context.
  * @param mappingFunction           A function that takes two binary string representations and computes the
  *                                  relationship between them.
  * @param data                      A 1-dimensional vector representing the relations between the vocabulary and context.
  * @param mappingThreshold          Optionally, for binary lexicons this specifies that when
  *                                  <code>mappingFunction</code> >= mappingThreshold, there is a 1.0-value
  *                                  relationship or 0.0 otherwise.
  * @author Mark Blokpoel
  */
@SerialVersionUID(100L)
class StructuredLexicon(val vocabularyRepresentations: Vector[Vector[Boolean]],
                        val contextRepresentations: Vector[Vector[Boolean]],
                        val mappingFunction: StructuredMappingFunction,
                        override protected val data: Vector[Double],
                        val mappingThreshold: Option[Double] = None)
    extends Lexicon(vocabularyRepresentations.length,
                    contextRepresentations.length,
                    data) {
  require(
    vocabularyRepresentations.forall(
      _.length == vocabularyRepresentations.head.length)
      && contextRepresentations.forall(
        _.length == contextRepresentations.head.length)
      && vocabularyRepresentations.head.length == contextRepresentations.head.length,
    "Vocabulary and context binary representations are not of equal length."
  )

  /**
    * This function removed all information about the underlying representations
    * in the structured lexicon.
    *
    * @return A default graded lexicon representation of the structured lexicon.
    */
  def getLexicon: Lexicon = Lexicon(vocabularySize, contextSize, data)

  val representationLength: Int = vocabularyRepresentations.head.length
}

/** Companion object to [[StructuredLexicon]] containing functions to generate graded or binary structured lexicons.
  *
  * @author Mark Blokpoel
  */
object StructuredLexicon {
  private def randomBinaryString(representationLength: Int): Vector[Boolean] =
    (for (_ <- 1 to representationLength) yield RNG.nextBoolean).toVector

  private def generateBinaryStrings(
      representationLength: Int,
      vocabularySize: Int): Vector[Vector[Boolean]] =
    (for (_ <- 1 to vocabularySize)
      yield randomBinaryString(representationLength)).toVector

  /** Returns a mutated (but immutable) vector of binary string representations. Each bit in each binary
    * string is mutated with probability <code>changeRate</code>.
    *
    * @param listOfRepresentations The original vector of binary string representations.
    * @param changeRate            The probability with which each bit in each binary string is mutated.
    */
  def mutateStructuredRepresentations(
      listOfRepresentations: Vector[Vector[Boolean]],
      changeRate: Double): Vector[Vector[Boolean]] = {
    for { representation <- listOfRepresentations } yield
      for { bit <- representation } yield
        if (RNG.nextBoolean(changeRate)) !bit else bit
  }

  /** Generates a graded [[StructuredLexicon]] from scratch, randomly generating also the binary string representations
    * for the signals in the vocabulary and referents in the context. It then computes for each signal-referent
    * pair their representational similarity, which is used as the signal-referent graded value.
    *
    * @note It is not recommended to set <code>representationLength</code> lower than <code>vocabularySize</code>
    *       or <code>contextSize</code>.
    * @param representationLength The length of the binary string representations.
    * @param mappingFunction      The mapping function that computes the similarity between two binary string
    *                             representations.
    * @param vocabularySize       The size of the vocabulary of the lexicon.
    * @param contextSize          The size of the context of the lexicon.
    * @return A randomly generated graded structured lexicon.
    */
  def generateGradedStructuredLexicon(
      representationLength: Int,
      mappingFunction: StructuredMappingFunction,
      vocabularySize: Int,
      contextSize: Int): StructuredLexicon = {
    val vocabularyRepresentations =
      generateBinaryStrings(representationLength, vocabularySize)
    val contextRepresentations =
      generateBinaryStrings(representationLength, contextSize)

    generateGradedStructuredLexicon(
      vocabularyRepresentations,
      contextRepresentations,
      mappingFunction
    )
  }

  /** Generates a graded [[StructuredLexicon]] based on predefined binary string representations
    * for the signals in the vocabulary and referents in the context. It computes for each signal-referent
    * pair their representational similarity, which is used as the signal-referent graded value.
    *
    * @param vocabularyRepresentations A vector of binary string representations of the signals in the vocabulary.
    * @param contextRepresentations    A vector of binary string representations of the referents in the context.
    * @param mappingFunction           The mapping function that computes the similarity between two binary string
    *                                  representations.
    * @return A graded structured lexicon.
    */
  def generateGradedStructuredLexicon(
      vocabularyRepresentations: Vector[Vector[Boolean]],
      contextRepresentations: Vector[Vector[Boolean]],
      mappingFunction: StructuredMappingFunction): StructuredLexicon = {
    val data =
      for (signalRepresentation <- vocabularyRepresentations;
           referentRepresentation <- contextRepresentations)
        yield {
          val a: Double =
            mappingFunction(signalRepresentation, referentRepresentation)
          a
        }

    new StructuredLexicon(
      vocabularyRepresentations,
      contextRepresentations,
      mappingFunction,
      data
    )
  }

  /** Generates a binary [[StructuredLexicon]] from scratch, randomly generating also the binary string representations
    * for the signals in the vocabulary and referents in the context. It then computes for each signal-referent
    * pair their representational similarity, and when that similarity is >= <code>mappingThreshold</code>, the
    * signal-referent relationship is set to 1.0 and otherwise it is set to 0.0.
    *
    * @param representationLength The length of the binary string representations.
    * @param mappingFunction      The mapping function that computes the similarity between two binary string
    *                             representations.
    * @param mappingThreshold     This threshold is used to decide if a signal and referent are representationally similar.
    * @param vocabularySize       The size of the vocabulary of the lexicon.
    * @param contextSize          The size of the context of the lexicon.
    * @return A randomly generated binary structured lexicon.
    */
  def generateBinaryStructuredLexicon(
      representationLength: Int,
      mappingFunction: StructuredMappingFunction,
      mappingThreshold: Double,
      vocabularySize: Int,
      contextSize: Int): StructuredLexicon = {
    val vocabularyRepresentations =
      generateBinaryStrings(representationLength, vocabularySize)
    val contextRepresentations =
      generateBinaryStrings(representationLength, contextSize)

    generateBinaryStructuredLexicon(
      vocabularyRepresentations,
      contextRepresentations,
      mappingFunction,
      mappingThreshold
    )
  }

  /** Generates a [[StructuredLexicon]] based on predefined binary string representations
    * for the signals in the vocabulary and referents in the context. It computes for each signal-referent
    * pair their representational similarity, which is used as the signal-referent graded value.
    *
    * @param vocabularyRepresentations A vector of binary string representations of the signals in the vocabulary.
    * @param contextRepresentations    A vector of binary string representations of the referents in the context.
    * @param mappingFunction           The mapping function that computes the similarity between two binary string
    *                                  representations.
    * @param mappingThreshold          This threshold is used to decide if a signal and referent are representationally similar.
    * @return A binary structured lexicon.
    */
  def generateBinaryStructuredLexicon(
      vocabularyRepresentations: Vector[Vector[Boolean]],
      contextRepresentations: Vector[Vector[Boolean]],
      mappingFunction: StructuredMappingFunction,
      mappingThreshold: Double): StructuredLexicon = {
    val data =
      for (signalRepresentation <- vocabularyRepresentations;
           referentRepresentation <- contextRepresentations)
        yield {
          val a: Double =
            mappingFunction(signalRepresentation, referentRepresentation)
          if (a >= mappingThreshold) 1.0 else 0.0
        }

    new StructuredLexicon(
      vocabularyRepresentations,
      contextRepresentations,
      mappingFunction,
      data,
      Some(mappingThreshold)
    )
  }
}

sealed trait StructuredMappingFunction {
  def apply(r1: Vector[Boolean], r2: Vector[Boolean]): Double

  def name: String
}

/** Helper object that returns a function computing the hamming distance between two binary string representations.
  *
  * @see See this Wikipedia entry on Hamming Distance [[https://en.wikipedia.org/wiki/Hamming_distance]].
  */
case object HAMMING_DISTANCE extends StructuredMappingFunction {
  override def apply(r1: Vector[Boolean], r2: Vector[Boolean]): Double = {
    val distance = ((0 to r2.size).toList /: r1)((prev, x) =>
      (prev zip prev.tail zip r2).scanLeft(prev.head + 1) {
        case (h, ((d, v), y)) =>
          min(min(h + 1, v + 1), d + (if (x == y) 0 else 1))
    }).last
    distance / r1.length.toDouble
  }

  override def name: String = "hamming distance"
}

/** Helper object that returns a function computing the edit distance between two binary string representations.
  *
  * @see See this Wikipedia entry on Edit Distance [[https://en.wikipedia.org/wiki/Edit_distance]].
  */
case object EDIT_DISTANCE extends StructuredMappingFunction {
  private def hammingDist(a: Vector[Boolean], b: Vector[Boolean]): Int =
    if (a.isEmpty || b.isEmpty) 0
    else if (a.head != b.head) 1 + hammingDist(a.tail, b.tail)
    else hammingDist(a.tail, b.tail)

  override def apply(r1: Vector[Boolean], r2: Vector[Boolean]): Double =
    hammingDist(r1, r2) / r1.length.toDouble

  override def name: String = "edit distance"
}

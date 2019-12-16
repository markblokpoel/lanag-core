package com.markblokpoel.lanag.rsa

import com.markblokpoel.lanag.util.RNG

/** A lexicon as defined in the Rational Speech Act theory (Frank & Goodman, 2012). This implementation
  * supports both binary and graded lexicons. It also implements functions that constitute the Rational
  * Speech Act model, i.e., to compute n-th order lexicons. The relationships between vocabulary and
  * context are stored in a 1-dimensional vector. The following mapping:
  *
  * |  | R<sub>1</sub> | R<sub>2</sub> | R<sub>3</sub> |
  * | ---: | :---: | :---: | :---: |
  * | S<sub>1</sub> | 0.8 | 0.2 | 0.0 |
  * | S<sub>2</sub> | 0.0 | 0.6 | 0.4 |
  *
  * Is represented in data as a concatenation of the rows or length contextSize:
  * {{{
  *   Vector[Double](0.8, 0.2, 0.0, 0.0, 0.6, 0.4)
  * }}}
  *
  * Lexicons are immutable.
  *
  * @param vocabularySize The size of the vocabulary of this lexicon.
  * @param contextSize The size of the context of this lexicon.
  * @param data A 1-dimensional vector representing the relations between the vocabulary and context.
  * @author Mark Blokpoel
  */
@SerialVersionUID(100L)
case class Lexicon(vocabularySize: Int,
                   contextSize: Int,
                   data: Vector[Double],
                   speakerDefinition: (Lexicon, Int) => Lexicon =
                     Lexicon.asBlokpoeletalSpeaker,
                   listenerDefinition: (Lexicon, Int) => Lexicon =
                     Lexicon.asBlokpoelEtalListener)
    extends Serializable {
  require(data.length == vocabularySize * contextSize)

  /** Returns the relation value for signal <code>i</code> and referent <code>j</code>.
    *
    * @param i Index of the signal.
    * @param j Index of the referent.
    */
  def apply(i: Int, j: Int): Double = {
    require(i >= 0 && i < vocabularySize && j >= 0 && j < contextSize,
            "Mat index out of bounds.")
    data(j + i * contextSize)
  }

  /** Returns a new immutable lexicon with the relation between signal i and referent j
    * updated to value v.
    */
  def update(i: Int, j: Int, v: Double): Lexicon = {
    require(i >= 0 && i < vocabularySize && j >= 0 && j < contextSize,
            "Mat index out of bounds.")
    new Lexicon(vocabularySize,
                contextSize,
                data.updated(j + i * contextSize, v))
  }

  /** Returns the row for signal <code>i</code>, containing all mapping values between signal
    * <code>i</code> and all referents.
    */
  def getRow(i: Int): Vector[Double] = {
    require(i >= 0 && i < vocabularySize, s"0 > row = $i > $vocabularySize")
    data.slice(i * contextSize, (1 + i) * contextSize)
  }

  /** Returns the column for referent <code>j</code>, containing all mapping values between referent
    * <code>j</code> and all signals. This function is of quadratic time complexity, use sparingly.
    */
  def getColumn(j: Int): Vector[Double] = {
    require(j >= 0 && j < contextSize, s"0 > col = $j > $contextSize")

    def getColumn(partialData: Vector[Double]): Vector[Double] =
      partialData match {
        case Vector() => Vector[Double]()
        case _ =>
          val (row, newPartialData) = partialData.splitAt(contextSize)
          row(j) +: getColumn(newPartialData)
      }

    getColumn(data)
  }

  /** Returns the index of the row in terms of the 2-dimensional array representation, given a 1-dimensional
    * index of a value in <code>data</code>.
    */
  private def rowFromIndex(i: Int): Int = (i - (i % contextSize)) / contextSize

  /** Returns the index of the column in terms of the 2-dimensional array representation, given a 1-dimensional
    * index of a value in <code>data</code>.
    */
  private def colFromIndex(i: Int): Int = i % contextSize

  //noinspection FoldTrueAnd
  def isConsistent: Boolean =
    (for (c <- 0 until contextSize)
      yield getColumn(c).sum > 0.0).foldLeft(true)(_ && _)

  /** Computes the dot product between the graded lexicon and a vector of length <code>contextSize</code>.
    *
    * @throws scala.IllegalArgumentException when <code>vector.length</code> is not equal to <code>contextSize</code>
    *                                  of this lexicon.
    * @param vector A vector of length <code>contextSize</code>.
    * @return A vector of length <code>vocabularySize</code> with the dot product.
    */
  def dot(vector: Vector[Double]): Vector[Double] = {
    require(
      vector.length == contextSize,
      s"Mat.dot dimensions incompatible: vector.length=${vector.length}!=ncol=$contextSize")

    val result = new Array[Double](vocabularySize)
    for (row <- 0 until vocabularySize)
      for (col <- 0 until contextSize)
        result(row) += data(col + row * contextSize) * vector(col)
    result.toVector
  }

  /** Computes the dot product between the transposed graded lexicon and a vector of <code>vocabularySize</code>.
    *
    * @throws scala.IllegalArgumentException when <code>vector.length</code> is not equal to <code>vocabularySize</code>
    *                                  of this lexicon.
    * @param vector A vector of length <code>vocabularySize</code>.
    * @return A vector of length <code>contextSize</code> with the dot product.
    */
  def dotT(vector: Vector[Double]): Vector[Double] = {
    require(
      vector.length == vocabularySize,
      s"Mat.dotT dimensions incompatible: vector.length=${vector.length}!=nrow=$contextSize")

    val result = new Array[Double](contextSize)
    for (col <- 0 until contextSize)
      for (row <- 0 until vocabularySize)
        result(col) += data(col + row * contextSize) * vector(row)
    result.toVector
  }

  /** Returns a normalization of this lexicon across columns (i.e., divides each cell by the sum of its column). */
  def normalizeColumns(): Lexicon = {
    val colSums = new Array[Double](contextSize)
    for (i <- data.indices)
      colSums(colFromIndex(i)) += data(i)

    def normalizeColumns(index: Int,
                         partialData: Vector[Double]): Vector[Double] =
      partialData match {
        case Vector() => Vector[Double]()
        case _ =>
          val normalizedValue = partialData.head / colSums(colFromIndex(index))
          if (!normalizedValue.isNaN)
            normalizedValue +: normalizeColumns(index + 1, partialData.tail)
          else 0.0 +: normalizeColumns(index + 1, partialData.tail)
      }

    new Lexicon(vocabularySize, contextSize, normalizeColumns(0, data))
  }

  /** Returns a normalization of this lexicon across rows (i.e., divides each cell by the sum of its row). */
  def normalizeRows(): Lexicon = {
    val rowSums = new Array[Double](vocabularySize)
    for (i <- data.indices)
      rowSums(rowFromIndex(i)) += data(i)

    def normalizeRows(index: Int, partialData: Vector[Double]): Vector[Double] =
      partialData match {
        case Vector() => Vector[Double]()
        case _ =>
          val normalizedValue = partialData.head / rowSums(rowFromIndex(index))
          if (!normalizedValue.isNaN)
            normalizedValue +: normalizeRows(index + 1, partialData.tail)
          else 0.0 +: normalizeRows(index + 1, partialData.tail)
      }

    new Lexicon(vocabularySize, contextSize, normalizeRows(0, data))
  }

  /** Returns a transformation of this lexicon corresponding to a <code>n</code><sup>th</sup> order speaker
    * as defined by the Rational Speech Act model.
    *
    * @param n The order of pragmatic reasoning.
    */
  def setOrderAsSpeaker(n: Int): Lexicon = speakerDefinition(this, n)

  /** Returns a transformation of this lexicon corresponding to a <code>n</code><sup>th</sup> order listener
    * as defined by the Rational Speech Act model.
    *
    * @param n The order of pragmatic reasoning.
    */
  def setOrderAsListener(n: Int): Lexicon = listenerDefinition(this, n)

  /** Returns the asymmetry between this lexicon and that lexicon. Asymmetry is computed relative to the
    * similarity threshold, i.e., it is the mean number of signal-referent relations that are more than
    * similarity-value apart.
    *
    * @param that The other lexicon against which asymmetry is computed.
    * @param similarity Optional argument specifying the threshold within which a signal-referent relation
    *                   is considered 'same'.
    * @return The asymmetry between this and that.
    */
  def asymmetryWith(that: Lexicon, similarity: Double = 0): Double = {
    val zipped = this.data zip that.data
    val asymmetry =
      zipped.map(a => Math.abs(a._1 - a._2) > similarity).count(b => b)
    asymmetry / data.length.toDouble
  }

  /** Returns the mean ambiguity of this graded lexicon, where ambiguity is defined as the mean number of
    * referents for which signals have a relation higher than the threshold.
    *
    * @param threshold Optional parameter specifying the threshold within which a signal-referent relation
    *                  is considered relevant.
    */
  def meanAmbiguity(threshold: Double = 1): Double = {
    var ambiguity: Int = 0
    for (i <- 0 until vocabularySize) {
      var signalAmbiguity: Int = 0
      for (j <- 0 until contextSize) {
        if (this(i, j) >= threshold)
          signalAmbiguity += 1
      }
      ambiguity = ambiguity + signalAmbiguity
    }
    ambiguity / data.length.toDouble
  }

  /** Returns a tuple containing the mean (._1) and variance (._2) ambiguity of this graded lexicon. Ambiguity
    * is defined as the mean number of referents for which signals have a relation higher than the threshold.
    *
    * @param threshold Optional parameter specifying the threshold within which a signal-referent relation
    *                  is considered relevant.
    */
  def meanAndVarianceAmbiguity(threshold: Double = 1): (Double, Double) = {
    val rowSums = new Array[Double](vocabularySize)
    for (i <- 0 until vocabularySize)
      for (j <- 0 until contextSize)
        if (this(i, j) >= threshold) rowSums(i) += 1

    val mean = rowSums.sum / rowSums.length
    val variance = rowSums.fold(0.0) { (acc, v) =>
      acc + (v - mean) * (v - mean)
    } / rowSums.length
    (mean, variance)
  }

  /** Returns a 2-dimensional vector representation of the lexicon. Inefficient function, avoid usage. */
  def to2DVector: Vector[Vector[Double]] = {
    val array2d = new Array[Array[Double]](vocabularySize)
    for (i <- 0 until vocabularySize) {
      array2d(i) = new Array[Double](contextSize)
      for (j <- 0 until contextSize)
        array2d(i)(j) = this(i, j)
    }
    array2d.toVector.map(_.toVector)
  }

  /** Returns a mutated (but immutable) copy of this lexicon. Each word-referent relationship with graded
    * value v has probability <code>P(mutationRate)</code> of flipping according to <code>Math.abs(v-1)</code>.
    * In binary lexicons, a 1 flips to a 0 and vice versa. In graded lexicons a 0.9 flips to a 0.1 and vice versa.
    * This is a non-deterministic transformation.
    *
    * @param mutationRate The probability of a word-referent mapping flipping.
    * @return A mutated (but immutable) mapping.
    */
  def mutate(mutationRate: Double): Lexicon = {
    val newLexiconData = this.data.map(v =>
      if (RNG.nextBoolean(mutationRate)) Math.abs(v - 1) else v)
    Lexicon(this.vocabularySize, this.contextSize, newLexiconData)
  }

  /** Returns an immutable copy of this lexicon where, based on the <code>swapRate</code>, signal-referent
    * relations are swapped around with across the mapping's central axis. That is, if swapped, <code>(i)(j)</code>
    * and <code>(i)(contextSize - j)</code> are swapped.
    *
    * @param mixRate The rate at which referents are swapped.
    * @return A referent-swapped signal-referent mapping.
    */
  def mixReferents(mixRate: Double): Lexicon = {
    val mutableMapping = this.to2DVector
    val mapping2: Array[Array[Double]] =
      new Array[Array[Double]](mutableMapping.length)
    for (i <- mutableMapping.indices) {
      var nrSwapsNeeded = (mixRate * (mutableMapping(i).length / 2)).toInt
      mapping2(i) = new Array[Double](mutableMapping(i).length)

      for (j <- 0 until mutableMapping(i).length / 2) {
        val swapOptionsLeft = mutableMapping(i).length / 2 - j
        if (RNG.nextBoolean(nrSwapsNeeded.toDouble / swapOptionsLeft.toDouble)) {
          mapping2(i)(j) = mutableMapping(i)(mutableMapping(i).length - j - 1)
          mapping2(i)(mutableMapping(i).length - j - 1) = mutableMapping(i)(j)
          nrSwapsNeeded -= 1
        } else {
          mapping2(i)(j) = mutableMapping(i)(j)
          mapping2(i)(mutableMapping(i).length - j - 1) =
            mutableMapping(i)(mutableMapping(i).length - j - 1)
        }
      }
      if (mutableMapping(i).length % 2 == 1) {
        mapping2(i)(mutableMapping(i).length / 2) =
          mutableMapping(i)(mutableMapping(i).length / 2)
      }
    }
    Lexicon(mapping2)
  }

  /** Returns a copy of this lexicon where, based on <code>additionRate</code>, a number of positive
    * signal-referent relations of weight 1.0 will be added to the lexicon.
    *
    * @param additionRate The ratio of 0-valued signal-referent mappings that will be converted to 1.0 (from 0 to 1).
    */
  def additiveBinaryMutation(additionRate: Double): Lexicon = {
    val mapping = this.to2DVector
    val mapping2: Array[Array[Double]] =
      new Array[Array[Double]](mapping.length)
    for (i <- mapping.indices) {
      mapping2(i) = new Array[Double](mapping(i).length)
      val nrFalses = mapping(i).foldLeft(0) { (acc, i) =>
        if (i == 0) acc + 1 else acc
      }
      var nrAdditionsNeeded =
        Math.max(0, Math.min(nrFalses, (additionRate * nrFalses).toInt))
      for (j <- mapping(i).indices) {
        mapping2(i)(j) = mapping(i)(j)
        var nrAdditionsLeft = 0
        for (ind <- mapping(i).indices if ind >= j)
          if (mapping(i)(ind) == 0.0) nrAdditionsLeft += 1
        if (mapping(i)(j) == 0.0) {
          if (RNG.nextBoolean(nrAdditionsNeeded / nrAdditionsLeft.toDouble)) {
            mapping2(i)(j) = 1.0
            nrAdditionsNeeded -= 1
          }
        }
      }
    }
    Lexicon(mapping2)
  }

  /** Returns a copy of this lexicon where, based on <code>additionRate</code>, a number
    * signal-referent relations of weight > <code>threshold</code> will be removed to the lexicon.
    *
    * @param removalRate The ratio of 0-valued signal-referent mappings that will be converted to 1.0 (from 0 to 1).
    * @param threshold Optional parameter specifying the threshold above which relations can be removed.
    */
  def removalBinaryMutation(removalRate: Double,
                            threshold: Double = 1): Lexicon = {
    val mapping = this.to2DVector
    val mapping2: Array[Array[Double]] =
      new Array[Array[Double]](mapping.length)
    for (i <- mapping.indices) {
      mapping2(i) = new Array[Double](mapping(i).length)
      val nrTrues = mapping(i).foldLeft(0) { (acc, i) =>
        if (i >= threshold) acc + 1 else acc
      }
      var nrSubtractionsNeeded =
        Math.max(0, Math.min(nrTrues, (removalRate * nrTrues).toInt))
      for (j <- mapping(i).indices) {
        mapping2(i)(j) = mapping(i)(j)
        var nrSubtractionsLeft = 0
        for (ind <- mapping(i).indices if ind >= j)
          if (mapping(i)(ind) >= threshold) nrSubtractionsLeft += 1
        if (mapping(i)(j) >= threshold) {
          if (RNG.nextBoolean(
                nrSubtractionsNeeded / nrSubtractionsLeft.toDouble)) {
            mapping2(i)(j) = 1.0
            nrSubtractionsNeeded -= 1
          }
        }
      }
    }
    Lexicon(mapping2)
  }

  /** Returns a multi-line formatted string representation of the lexicon. */
  override def toString: String = {
    var out = ""
    for (i <- 0 until vocabularySize) {
      for (j <- 0 until contextSize) {
        out += this(i, j) + "\t"
      }
      out += "\n"
    }
    out
  }
}

/** Companion object to [[Lexicon]] containing alternative constructors and generation functions. */
object Lexicon {

  /** Constructor for [[Lexicon]].
    *
    * @param matrix A 2-dimensional vector representation of a graded lexicon. Rows represent the
    *               vocabulary, columns represent the context.
    * @return A (graded) lexicon.
    */
  def apply(matrix: Vector[Vector[Double]]): Lexicon = {
    require(matrix.nonEmpty && matrix.forall(_.length == matrix.head.length),
            "Matrix is not well-formed.")
    Lexicon(matrix.length, matrix.head.length, matrix.flatten)
  }

  /** Constructor for [[Lexicon]].
    *
    * @param matrix A 2-dimensional array representation of a graded lexicon. Rows represent the
    *               vocabulary, columns represent the context.
    * @return A (graded) lexicon.
    */
  def apply(matrix: Array[Array[Double]]): Lexicon =
    Lexicon(matrix.toVector.map(_.toVector))

  /** Returns a transformation of this lexicon corresponding to a <code>n</code><sup>th</sup> order speaker
    * as defined by Frank and Goodman (2012).
    *
    * @param lexicon The lexicon to be transformed.
    * @param n The order of pragmatic reasoning.
    */
  def asFrankGoodmanSpeaker(lexicon: Lexicon, n: Int): Lexicon =
    if (n == 0) lexicon.normalizeColumns()
    else asFrankGoodmanListener(lexicon, n - 1).normalizeColumns()

  /** Returns a transformation of this lexicon corresponding to a <code>n</code><sup>th</sup> order listener
    * as defined by Frank and Goodman (2012).
    *
    * @param lexicon The lexicon to be transformed.
    * @param n The order of pragmatic reasoning.
    */
  def asFrankGoodmanListener(lexicon: Lexicon, n: Int): Lexicon =
    if (n == 0) lexicon.normalizeRows()
    else asFrankGoodmanSpeaker(lexicon, n).normalizeRows()

  /** Returns a transformation of this lexicon corresponding to a <code>n</code><sup>th</sup> order speaker
    * as defined by Blokpoel et al. (2020).
    *
    * @param lexicon The lexicon to be transformed.
    * @param n The order of pragmatic reasoning.
    */
  def asBlokpoeletalSpeaker(lexicon: Lexicon, n: Int): Lexicon =
    if (n == 0) lexicon.normalizeColumns()
    else
      asBlokpoeletalSpeaker(lexicon, n - 1).normalizeRows().normalizeColumns()

  /** Returns a transformation of lexicon corresponding to a <code>n</code><sup>th</sup> order listener
    * as defined by Blokpoel et al. (2020).
    *
    * @param lexicon The lexicon to be transformed.
    * @param n The order of pragmatic reasoning.
    */
  def asBlokpoelEtalListener(lexicon: Lexicon, n: Int): Lexicon =
    if (n == 0) lexicon.normalizeRows()
    else
      asBlokpoelEtalListener(lexicon, n - 1).normalizeColumns().normalizeRows()

  /** Returns a transformation of lexicon corresponding to a <code>n</code><sup>th</sup> order speaker
    * as defined by Franke and Degen (2016).
    *
    * @param lexicon The lexicon to be transformed.
    * @param n The order of pragmatic reasoning.
    */
  def asFrankeDegenSpeaker(lexicon: Lexicon, n: Int): Lexicon =
    if (n == 0) lexicon.normalizeColumns()
    else asFrankeDegenListener(lexicon, n - 1).normalizeColumns()

  /** Returns a transformation of lexicon corresponding to a <code>n</code><sup>th</sup> order listener
    * as defined by Franke and Degen (2016).
    *
    * @param lexicon The lexicon to be transformed.
    * @param n The order of pragmatic reasoning.
    */
  def asFrankeDegenListener(lexicon: Lexicon, n: Int): Lexicon =
    if (n == 0) lexicon.normalizeRows()
    else asFrankeDegenSpeaker(lexicon, n - 1)

  /** Generates a randomized binary lexicon (i.e., each word-referent pair has <code>probability</code>
    * to be 1.0).
    *
    * @note When probability is below 0 this returns an empty lexicon, when it is above 1 result a full lexicon.
    * @param probability    The density parameter ranging from 0 to 1.
    * @param vocabularySize The size of the vocabulary.
    * @param contextSize    The size of the context.
    * @return A randomly generated lexicon.
    */
  def generateRandomBinaryLexicon(probability: Double,
                                  vocabularySize: Int,
                                  contextSize: Int): Lexicon = {

    val lexicon: Vector[Double] = (for (_ <- 1 to vocabularySize * contextSize)
      yield if (RNG.nextBoolean(probability)) 1.0 else 0.0).toVector
    Lexicon(vocabularySize, contextSize, lexicon)
  }

  /** Generates a lexicon that is consistent (each referent has at least 1 associated word)
    * and has a fixed ambiguity (viz., each word is associated with ambiguity referents).
    *
    * @throws scala.IllegalArgumentException when <code>contextSize</code> < <code>vocabularySize</code>.
    * @note Ambiguity <= 0 generates the same type of lexicon as ambiguity = 1, and ambiguity > contextSize
    * generates a lexicon of ambiguity = contextSize.
    * @param ambiguity      The number of referents each signal refers to (ranging from 0 to contextSize).
    * @param vocabularySize The size of the vocabulary.
    * @param contextSize    The size of the context.
    * @return A lexicon.
    */
  def generateConsistentAmbiguityMapping(ambiguity: Int,
                                         vocabularySize: Int,
                                         contextSize: Int): Lexicon = {
    require(vocabularySize >= contextSize,
            "Vocabulary is smaller than the context," +
              "cannot create consistent lexicon.")

    val lexicon: Array[Array[Double]] = new Array[Array[Double]](vocabularySize)
    val mappingsNeeded = Math.max(1, Math.min(ambiguity, contextSize))
    val mappingsNeededPerWord = new Array[Int](vocabularySize)
    for (i <- mappingsNeededPerWord.indices)
      mappingsNeededPerWord(i) = mappingsNeeded

    val signalsNotPicked = scala.collection.mutable.ArrayBuffer[Int]()
    for (i <- 0 until vocabularySize)
      signalsNotPicked.append(i)

    for (i <- lexicon.indices)
      lexicon(i) = new Array[Double](contextSize)

    for (j <- 0 until contextSize) {
      // Fill in one lexicon per referent to make the lexicon consistent
      val sig = signalsNotPicked.remove(RNG.nextInt(signalsNotPicked.size))
      lexicon(sig)(j) = 1.0
      mappingsNeededPerWord(sig) -= 1
    }

    for (i <- lexicon.indices) {
      for (j <- lexicon(i).indices) {
        if (lexicon(i)(j) == 0) {
          var mappingsLeft = 0
          for (l <- j until contextSize)
            if (lexicon(i)(l) == 0) mappingsLeft += 1

          if (mappingsLeft <= 0 || RNG.nextBoolean(
                mappingsNeededPerWord(i).toDouble / mappingsLeft.toDouble)) {
            lexicon(i)(j) = 1.0
            mappingsNeededPerWord(i) -= 1
          } else {
            lexicon(i)(j) = 0.0
          }
        }
      }
    }
    Lexicon(lexicon)
  }

}

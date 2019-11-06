package com.markblokpoel.lanag.core

/** Signals that implement this trait consist of repair information.
  *
  * @author Mark Blokpoel
  */
trait Repair

/** The prototype signal type. All signal types are derivatives of this trait.
  *
  * @author Mark Blokpoel
  */
trait Signal

/** The prototype signal that has some content of type T.
  *
  * @tparam T The type of the content.
  * @author Mark Blokpoel
  */
abstract class ContentSignalPrototype[T] extends Signal {

  /** Access the content. */
  val content: T

  /** Returns whether or not the signal is defined. */
  def isDefined: Boolean
}

/** A basic content signal.
  *
  * The content of this signal type is an index referring to a particular signal,
  * for example, in a [[com.markblokpoel.lanag.rsa.Lexicon]].
  *
  * @param content The optional signal index.
  * @author Mark Blokpoel
  */
case class ContentSignal(override val content: Option[Int])
    extends ContentSignalPrototype[Option[Int]] {
  override def isDefined: Boolean = content.isDefined
}

/**
  * Helper object to create instance of ContentSignal.
  */
case object ContentSignal {
  def apply(signal: Int): ContentSignal = ContentSignal(Some(signal))
}
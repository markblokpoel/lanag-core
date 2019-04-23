package com.markblokpoel.lanag.core

/** The prototype intention type. All intention types are derivatives of this trait. */
trait Intention

/** The prototype intention that has some content of type T.
  *
  * @tparam T The type of the content.
  *
  * @author Mark Blokpoel
  */
abstract class IntentionPrototype[T] extends Intention {

  /** Access the content. */
  val content: T

  /** Returns whether or not the intention is defined. */
  def isDefined: Boolean
}

/** A basic referential intention.
  *
  * The content of this intention type is an index referring to a particular referent,
  * for example, in a [[com.markblokpoel.lanag.rsa.Lexicon]].
  *
  * @param content The optional referent index.
  * @author Mark Blokpoel
  */
case class ReferentialIntention(override val content: Option[Int])
    extends IntentionPrototype[Option[Int]] {
  override def isDefined: Boolean = content.isDefined
}

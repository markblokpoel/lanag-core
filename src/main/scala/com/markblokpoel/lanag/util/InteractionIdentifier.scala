package com.markblokpoel.lanag.util

/** Global parameter for generating unique identifier numbers for interactions.
  *
  * @author Mark Blokpoel
  */
case object InteractionIdentifier {
  private var idCounter: Long = 0

  /** Returns the next identifier number. */
  def nextId: Long = {
    idCounter += 1
    idCounter
  }
}

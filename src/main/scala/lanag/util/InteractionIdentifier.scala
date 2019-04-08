package lanag.core.util

/** Global parameter for generating unique identifier numbers for interactions.
  *
  * @author Mark Blokpoel
  */
case object InteractionIdentifier {
  private var idCounter: Int = 0

  /** Returns the next identifier number. */
  def nextId: Int = {
    idCounter += 1
    idCounter
  }
}

package com.markblokpoel.lanag.core

/** A prototype type for storing simulation data in case classes.
  * All data case classes are derivatives of this trait.
  *
  * @author Mark Blokpoel
  */
trait Data {
}

/** Use this class in your agent simulation implementation when you have no
  * data to collect.
  *
  * @author Mark Blokpoel
  */
case class NoData() extends Data {
}
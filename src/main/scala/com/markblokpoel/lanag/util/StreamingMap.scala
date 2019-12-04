package com.markblokpoel.lanag.util

/** A lazy implementation of Map, where the values for each key of type {{{K}}} are specified by a function
  * {{{valueFunc}}} of type {{{K => V}}}. This implementation is mutable. If an element {{{key}}} is requested and
  * its value is not yet computed, [[StreamingMap]] computes {{{valueFunc(key)}}} and stores the result. If the
  * value has already been computed, then its simply returns the stored value.
  *
  * @param keys A list of keys.
  * @param valueFunc The function that describes the value for each key. This is assumed to be complete for all keys
  *                  in {{{keys}}}.
  * @tparam K The parameter type of the keys.
  * @tparam V The parameter type of the values.
  */
@SerialVersionUID(100L)
class StreamingMap[K, V](val keys: IndexedSeq[K], valueFunc: K => V)
    extends Serializable {
  private val cache: scala.collection.mutable.Map[K, V] =
    scala.collection.mutable.Map.empty

  /** Returns the value associated with the key.
    *
    * @param key The key entry for which the value should be returned.
    * @return
    */
  def apply(key: K): V =
    if (cache.contains(key)) cache(key)
    else {
      val value = valueFunc(key)
      cache.update(key, value)
      value
    }
}

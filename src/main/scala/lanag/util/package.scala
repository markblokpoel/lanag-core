package lanag.core

/** Provides classes and objects for dealing with the simulation infrastructure.
  *
  * ==Overview==
  * ===Spark simulations===
  * Use [[lanag.core.util.LocalSparkSimulation]] to create an instance of Spark
  * running locally on your machine:
  * {{{
  *   import lanag.lanag.coreg.util.LocalSparkSimulation
  *   import LocalSparkSimulation.spark.implicits._
  *
  *   val sequenceToBeParallelized = Seq(...)
  *   val rdd = LocalSparkSimulation.parallelize(sequenceToBeParallelized)
  * }}}
  *
  * You can now RDD functions like [[org.apache.spark.rdd.RDD.flatMap()]] and
  * [[org.apache.spark.rdd.RDD.map()]] to apply transformations to the sequence.
  * {{{
  *   rdd.map(a => a +1)
  * }}}
  *
  * These will be executed lazily and in parallel. Until you write the results to file, or otherwise
  * collect them, nothing will be executed. For a tutorial on Spark see the Apache Spark website
  * [[https://spark.apache.org/docs/latest/quick-start.html]].
  *
  * ===Global number generators===
  * The object [[lanag.core.util.InteractionIdentifier]] is used in [[lanag.core.Interaction]]
  * to identify pairs of agents.
  *
  * The object [[lanag.core.util.RNG]] specifies a global random number generator. Implementations
  * of [[lanag.core.Interaction]] that use this object exclusive will be repeatable for specific RNG seeds.
  *
  * @author Mark Blokpoel
  */
package object util {
}

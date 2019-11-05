package com.markblokpoel.lanag.util

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.reflect.ClassTag

/** A Spark session utility wrapper.
  *
  * This session can be used to parallelize simulations. It requires the local machine to have
  * a Spark driver installed, if used with flag <code>local = true</code>. See the Apache Spark
  * website [[https://spark.apache.org/downloads.html]] for installation instructions.
  *
  * @param local Flag to specify whether or not should run in local mode.
  * @param cores Number of cores to use in local mode, default is 4. Set to 0 to request maximum number of cores.
  * @author Mark Blokpoel
  */
case class SparkSimulation(local: Boolean = false, cores: Int = 4) {
  val spark: SparkSession =
    if (local) {
      if (cores > 0)
        SparkSession.builder
          .master(s"local[$cores]")
          .appName("Lanag2")
          .getOrCreate()
      else
        SparkSession.builder.master(s"local[*]").appName("Lanag2").getOrCreate()
    } else
      SparkSession.builder.appName("Lanag2").getOrCreate()

  /** Returns the local spark context. */
  def context: SparkContext = spark.sparkContext

  /** Parallelizes a sequence of type T.
    *
    * Use this to parallelize simulations and run them in a Spark session. All transformation
    * applied to the resulting <code>org.apache.spark.rdd.RDD</code> are lazily executed in parallel.
    *
    * @param seq Sequence of type T to be parallelized.
    * @tparam T Optional type parameter.
    * @return A resilient distributed data structure <code>org.apache.spark.rdd.RDD</code>.
    */
  def parallelize[T: ClassTag](seq: Seq[T]): RDD[T] = context.parallelize(seq)

  /** Shuts down the Spark session.
    *
    * Use this to cleanly close the Spark session and ensure all jobs are finished properly.
    * No further sequences can be parallelized.
    */
  def shutdown(): Unit = spark.close()

}

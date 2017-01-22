package com.fyber.core

import com.fyber.model.{BaseMetric, Metric}

import scala.collection.mutable
import scala.reflect.ClassTag

/**
  * Created by malam on 1/21/17.
  */
trait Analyzer[A <: BaseMetric] {

  def orderBy: Ordering[A]

  protected val priorityQueue: mutable.PriorityQueue[A]

  protected val window: Int

  def readData(fileName: String)(block: String => A): List[A] = {
    val lines = scala.io.Source.fromFile(fileName, "UTF-8")
      .getLines()

    lines.map(line => {
      block(line)
    }).toList
  }

  def analyzeData(fileName: String)

  def timed[A](block: => A) = {
    val start = System.currentTimeMillis()
    val result = block
    println(s"time taken = ${System.currentTimeMillis() - start} ms")
    result
  }

}

trait MetricAnalyzer extends Analyzer[Metric] {

  def orderBy = new Ordering[Metric] {
    def compare(a: Metric, b: Metric) = b.time compareTo(a.time)
  }

  val priorityQueue = new scala.collection.mutable.PriorityQueue[Metric]()(orderBy)

  val window = 60

  def printLine(r: Metric) = {
    val sum: Double = priorityQueue.foldLeft[Double](0.0d)(_ + _.value)
    println(f"${r.time}\t${r.value}\t${priorityQueue.size}\t${priorityQueue.minBy(_.value).value}\t${priorityQueue.maxBy(_.value).value}\t$sum%.5f")
  }

  def analyzeData(fileName: String) = {
    val metricList = readData(fileName){ line =>
      val tokens = line.split("\t")
      Metric(tokens(0).toLong, tokens(1).toDouble)
    }

    metricList.foreach(r => {
        priorityQueue.filter(_.time < (r.time - window)).foreach(m => priorityQueue.dequeue())
        priorityQueue.enqueue(r)
        printLine(r)
    })
  }

}

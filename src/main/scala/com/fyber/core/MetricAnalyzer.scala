package com.fyber.core

import com.fyber.model.{Analysis, BaseMetric, Metric}

import scala.collection.mutable

/**
  * Created by malam on 1/21/17.
  */
trait Analyzer[A <: BaseMetric] {

  protected def orderBy: Ordering[A]

  protected val priorityQueue: mutable.PriorityQueue[A]

  protected val window: Int

  def readData(fileName: String)(block: String => A): List[A] = {
    val lines = scala.io.Source.fromFile(fileName, "UTF-8")
      .getLines()

    lines.map(line => {
      block(line)
    }).toList
  }

  protected def analyzeData[B](fileName: String)(action: Analysis => B)

  def timed[A](block: => A) = {
    val start = System.currentTimeMillis()
    val result = block
    println(s"time taken = ${System.currentTimeMillis() - start} ms")
    result
  }

}

trait MetricAnalyzer extends Analyzer[Metric] {

  implicit def orderBy = new Ordering[Metric] {
    def compare(a: Metric, b: Metric) = b.time compareTo(a.time)
  }

  val priorityQueue: mutable.PriorityQueue[Metric] = new mutable.PriorityQueue[Metric]()(orderBy)

  def printLine(r: Analysis) = {
    println(f"${r.metric.time}\t${r.metric.value}\t${r.n}\t${r.minValue}\t${r.maxValue}\t${r.rollingSum}%.5f")
  }

  def analyzeData[B](fileName: String)(action: Analysis => B) = {
    val metricList = readData(fileName){ line =>
      val tokens = line.split("\t")
      Metric(tokens(0).toLong, tokens(1).toDouble)
    }

    metricList.foreach(r => {
        priorityQueue.filter(_.time < (r.time - window + 1)).foreach(m => priorityQueue.dequeue())
        priorityQueue.enqueue(r)

        val sum: Double = priorityQueue.foldLeft[Double](0.0d)(_ + _.value)
        action(Analysis(r, priorityQueue.size, sum, priorityQueue.minBy(_.value).value, priorityQueue.maxBy(_.value).value))
    })
  }

}

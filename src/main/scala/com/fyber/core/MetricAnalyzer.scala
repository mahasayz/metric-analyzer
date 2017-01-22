package com.fyber.core

import com.fyber.model.{Analysis, BaseMetric, Metric}

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

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

  def printHeader = {
    printf("%-10s %-10s %-3s %-10s %-10s %-10s\n", "T", "V", "N", "RS", "MinV", "MaxV")
    printf("%-10s-%-10s-%-3s-%-10s-%-10s-%-10s\n",
      "%10s".format("").replace(' ', '-'),
      "%10s".format("").replace(' ', '-'),
      "%3s".format("").replace(' ', '-'),
      "%10s".format("").replace(' ', '-'),
      "%10s".format("").replace(' ', '-'),
      "%10s".format("").replace(' ', '-'))
  }

  def printLine(r: Analysis) = {
    printf("%-10s %-10s %-3s %-10s %-10s %-10s\n",
      r.metric.time,
      "%.5f".format(r.metric.value),
      r.n,
      "%.5f".format(r.minValue),
      "%.5f".format(r.maxValue),
      "%.5f".format(r.rollingSum))
  }

  def analyzeData[B](fileName: String)(action: Analysis => B) = {
    Try(readData(fileName){ line =>
      val tokens = line.split("\t")
      Try(Metric(tokens(0).toLong, tokens(1).toDouble)) match {
        case Success(v) => v
        case Failure(e) =>
          println(s"ERROR: Cannot parse line - ${line}")
          Metric(-1, 0.0)
      }
    }) match {
      case Success(metricList) =>
        metricList.foreach(r => {
          if (r.time > -1) {
            priorityQueue.filter(_.time < (r.time - window + 1)).foreach(m => priorityQueue.dequeue())
            priorityQueue.enqueue(r)

            val sum: Double = priorityQueue.foldLeft[Double](0.0d)(_ + _.value)
            action(Analysis(r, priorityQueue.size, sum, priorityQueue.minBy(_.value).value, priorityQueue.maxBy(_.value).value))
          }
        })
      case Failure(e) =>
        println(s"EXCEPTION: ${e.getMessage}")
    }
  }

}

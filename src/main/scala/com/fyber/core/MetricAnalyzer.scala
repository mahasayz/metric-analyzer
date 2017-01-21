package com.fyber.core

import com.fyber.model.Metric

/**
  * Created by malam on 1/21/17.
  */
trait MetricAnalyzer {

  def orderByTime = new Ordering[Metric] {
    def compare(a: Metric, b: Metric) = b.time compareTo(a.time)
  }

  private val priorityQueue = new scala.collection.mutable.PriorityQueue[Metric]()(orderByTime)

  def readData: List[Metric] = {
    val lines = scala.io.Source.fromFile("/Users/malam/dev/Learn/Scala/metric-analyzer/src/main/resources/data_scala.txt", "UTF-8")
      .getLines().toList

    lines.map(line => {
      val tokens = line.split("\t")
      Metric(tokens(0).toLong, tokens(1).toDouble)
    })
  }

  def printLine(r: Metric) = {
    val sum: Double = priorityQueue.foldLeft[Double](0.0d)(_ + _.value)
    println(f"${r.time}\t${r.value}\t${priorityQueue.size}\t${priorityQueue.minBy(_.value).value}\t${priorityQueue.maxBy(_.value).value}\t$sum%.5f")
  }

  def analyzeData = {
    val metricList = readData

    metricList.foreach(r => {
        priorityQueue.filter(_.time < (r.time - 60)).foreach(m => priorityQueue.dequeue())
        priorityQueue.enqueue(r)
        printLine(r)
    })

  }

}

object MetricAnalyzer extends App with MetricAnalyzer {
  override def main(args: Array[String]) = {
    val start = System.currentTimeMillis()
    analyzeData
    println(s"Time taken : ${System.currentTimeMillis() - start} ms")
  }
}

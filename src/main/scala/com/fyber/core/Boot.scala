package com.fyber.core

/**
  * Created by Mahbub on 1/22/2017.
  */

class BasicMetricAnalyzer(val window: Int) extends MetricAnalyzer {}

object Boot extends App {
  override def main(args: Array[String]) = {
    if (args.length != 1)
      throw new IllegalArgumentException("Usage: sbt \"run <file-name>\"")

    val analyzer = new BasicMetricAnalyzer(60)
    analyzer.printHeader
    analyzer.analyzeData(args(0)){ metric =>
      analyzer.printLine(metric)
    }
  }
}

package com.fyber.core

/**
  * Created by Mahbub on 1/22/2017.
  */
object Boot extends App with MetricAnalyzer {
  override def main(args: Array[String]) = {
    if (args.length != 1)
      throw new IllegalArgumentException("Usage: scala Boot <file-name>")
    analyzeData(args(0))
  }
}

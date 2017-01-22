package com.fyber

import java.io.{File, FileNotFoundException}

import com.fyber.core.MetricAnalyzer
import com.fyber.model.{Analysis, Metric}
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable.ListBuffer

/**
  * Created by Mahbub on 1/22/2017.
  */

abstract class TestAnalyzer(val filename: String) extends MetricAnalyzer {
  val window = 60

  def expectedAnalysis: List[Analysis]
  def getMaxValue = priorityQueue.max.value
  def getMinValue = priorityQueue.min.value
  def resetQueue = priorityQueue.clear()
}

trait AnalyzerBehaviors {this: FlatSpec =>

  def sixtySecAnalyzer[B <: TestAnalyzer](analyzer: B): Unit = {
    it should "not have any elements in heap whose time differ by more than 60 secs" in {
      analyzer.analyzeData(analyzer.filename){ analysis =>
        assert((analyzer.getMaxValue - analyzer.getMinValue) <= 60)
      }
      analyzer.resetQueue
    }

    it should "print results" in {
      analyzer.printHeader
      analyzer.timed(analyzer.analyzeData(analyzer.filename){ analysis =>
        analyzer.printLine(analysis)
      })
      analyzer.resetQueue
    }

    it should "meet expected results" in {
      val actualList = new ListBuffer[Analysis]
      analyzer.analyzeData(analyzer.filename){ analysis =>
        actualList.append(analysis)
      }

      for (i <- 0 until actualList.size) {
        assert(actualList(i).equals(analyzer.expectedAnalysis(i)))
      }
    }
  }

}

class MetricAnalyzerSpec extends FlatSpec with Matchers with AnalyzerBehaviors {

  def allMetricsInOneWindow = new TestAnalyzer("") {
    override def readData(fileName: String)(block: (String) => Metric): List[Metric] = List(
      Metric(1355270609,	1.80215),
      Metric(1355270621,	1.80185)
    )
    def expectedAnalysis = List(
      Analysis(Metric(1355270609,	1.80215), 1, 1.80215, 1.80215, 1.80215),
      Analysis(Metric(1355270621,	1.80185), 2, 3.604, 1.80185, 1.80215)
    )
  }

  def oneMetricPerWindow = new TestAnalyzer("") {
    override def readData(fileName: String)(block: (String) => Metric): List[Metric] = List(
      Metric(1355270600,	1.80215),
      Metric(1355270660,	1.80185)
    )
    def expectedAnalysis = List(
      Analysis(Metric(1355270600,	1.80215), 1, 1.80215, 1.80215, 1.80215),
      Analysis(Metric(1355270660,	1.80185), 1, 1.80185, 1.80185, 1.80185)
    )
  }

  def borderlineMetrics = new TestAnalyzer("") {
    override def readData(fileName: String)(block: (String) => Metric): List[Metric] = List(
      Metric(1355270601,	1.80215),
      Metric(1355270660,	1.80185)
    )
    def expectedAnalysis = List(
      Analysis(Metric(1355270601,	1.80215), 1, 1.80215, 1.80215, 1.80215),
      Analysis(Metric(1355270660,	1.80185), 2, 3.604, 1.80185, 1.80215)
    )
  }

  def metricsFromFile = new TestAnalyzer(getClass.getResource("/test_data_scala.txt").getPath) {
    override def expectedAnalysis: List[Analysis] = List(
      Analysis(Metric(1355270609,1.80215),1,1.80215,1.80215,1.80215),
      Analysis(Metric(1355270621,1.80185),2,3.604,1.80185,1.80215),
      Analysis(Metric(1355270646,1.80195),3,5.40595,1.80185,1.80215),
      Analysis(Metric(1355270702,1.80225),2,3.6042,1.80195,1.80225),
      Analysis(Metric(1355270702,1.80215),3,5.40635,1.80195,1.80225),
      Analysis(Metric(1355270829,1.80235),1,1.80235,1.80235,1.80235),
      Analysis(Metric(1355270854,1.80205),2,3.6044,1.80205,1.80235),
      Analysis(Metric(1355270868,1.80225),3,5.40665,1.80205,1.80235),
      Analysis(Metric(1355271000,1.80245),1,1.80245,1.80245,1.80245),
      Analysis(Metric(1355271023,1.80285),2,3.6053,1.80245,1.80285),
      Analysis(Metric(1355271024,1.80275),3,5.40805,1.80245,1.80285),
      Analysis(Metric(1355271026,1.80285),4,7.2109,1.80245,1.80285),
      Analysis(Metric(1355271027,1.80265),5,9.01355,1.80245,1.80285),
      Analysis(Metric(1355271056,1.80275),6,10.8163,1.80245,1.80285),
      Analysis(Metric(1355271428,1.80265),1,1.80265,1.80265,1.80265),
      Analysis(Metric(1355271466,1.80275),2,3.6054,1.80265,1.80275),
      Analysis(Metric(1355271471,1.80295),3,5.40835,1.80265,1.80295),
      Analysis(Metric(1355271507,1.80265),3,5.40835,1.80265,1.80295),
      Analysis(Metric(1355271562,1.80275),2,3.6054,1.80265,1.80275),
      Analysis(Metric(1355271588,1.80295),2,3.6057,1.80275,1.80295)
    )
  }

  def fileWithInvalidMetric = new TestAnalyzer(getClass.getResource("/faulty_data_scala.txt").getPath) {
    override def expectedAnalysis: List[Analysis] = List(
      Analysis(Metric(1355270609,1.80215),1,1.80215,1.80215,1.80215),
      Analysis(Metric(1355270621,1.80185),2,3.604,1.80185,1.80215)
    )
  }

  def metricsFromInvalidFile = new TestAnalyzer("bad_file.txt") {
    override def expectedAnalysis: List[Analysis] = List()
  }

  "An analyzer with all metrics within same window" should behave like sixtySecAnalyzer(allMetricsInOneWindow)

  "An analyzer with one metric per window" should behave like sixtySecAnalyzer(oneMetricPerWindow)

  "An analyzer with metrics at the borders of the window" should behave like sixtySecAnalyzer(borderlineMetrics)

  "An analyzer with metrics read from file" should behave like sixtySecAnalyzer(metricsFromFile)

  "An analyzer with faulty metric read from file" should behave like sixtySecAnalyzer(fileWithInvalidMetric)

  "An analyzer" should "throw FileNotFoundException if invalid file-name specified" in {
    a [FileNotFoundException] should be thrownBy {
      metricsFromInvalidFile.readData(metricsFromInvalidFile.filename){ line =>
        val tokens = line.split("\t")
        Metric(tokens(0).toLong, tokens(1).toDouble)
      }
    }
  }
}

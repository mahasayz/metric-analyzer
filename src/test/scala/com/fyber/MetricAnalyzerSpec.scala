package com.fyber

import java.io.FileNotFoundException

import com.fyber.core.MetricAnalyzer
import com.fyber.model.Metric
import org.scalatest.{FlatSpec, FunSuite, Matchers}

/**
  * Created by Mahbub on 1/22/2017.
  */
class MetricAnalyzerSpec extends FlatSpec with Matchers with MetricAnalyzer {

  it should "throw FileNotFoundException if invalid file-name specified" in {
    a [FileNotFoundException] should be thrownBy {
      readData("bad_dir/file.txt"){ line =>
        val tokens = line.split("\t")
        Metric(tokens(0).toLong, tokens(1).toDouble)
      }
    }
  }

  "MetricAnalyzer" should "print results" in {
    timed(analyzeData("C:\\Users\\Mahbub\\Documents\\metric-analyzer\\src\\main\\resources\\data_scala.txt"))
  }

}

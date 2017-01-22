package com.fyber.model

/**
  * Created by malam on 1/21/17.
  */

trait BaseMetric

case class Metric(time: Long, value: Double) extends BaseMetric

case class Analysis(metric: Metric, n: Int, rollingSum: Double, minValue: Double, maxValue: Double) {
  override def equals(o: Any) = {
    o match {
      case that: Analysis =>
        val res = (that.metric.value == this.metric.value &&
          that.metric.time == this.metric.time &&
          that.rollingSum == "%.5f".format(this.rollingSum).toDouble  &&
          that.maxValue == this.maxValue &&
          that.minValue == this.minValue &&
          that.n == this.n)
        if (!res)
          println(s"got : ${this.toString}\nexpected : ${that.toString}")
        res
      case _ => false
    }
  }

  override def toString: String = s"Metric(time=${this.metric.time}, value=${this.metric.value}), " +
    s"n=${this.n}, rs=${this.rollingSum}, min=${this.minValue}, max=${this.maxValue}"
}

package com.fyber.model

/**
  * Created by malam on 1/21/17.
  */

trait BaseMetric
case class Metric(time: Long, value: Double) extends BaseMetric

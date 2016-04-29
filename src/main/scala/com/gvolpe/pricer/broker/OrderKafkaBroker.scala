package com.gvolpe.pricer.broker

import com.gvolpe.pricer.{Order, ProcessT}

import scalaz.concurrent.Task
import scalaz.stream.async

object OrderKafkaBroker extends Broker {

  val ordersTopic = async.topic[Order]()

  override def consume: ProcessT[Order] = ordersTopic.subscribe

  override def produce(order: Order): Task[Unit] = ordersTopic.publishOne(order)

}

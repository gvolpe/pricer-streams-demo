package com.gvolpe.pricer.broker

import com.gvolpe.pricer._

import scalaz.concurrent.Task
import scalaz.stream.async

object OrderRabbitMqBroker extends Broker {

  val orderQ = async.boundedQueue[Order](100)

  override def consume: ProcessT[Order] = orderQ.dequeue

  override def publish(order: Order): Task[Unit] = {
    showOrder("Publishing", order) flatMap (_ => orderQ.enqueueOne(order))
  }

}

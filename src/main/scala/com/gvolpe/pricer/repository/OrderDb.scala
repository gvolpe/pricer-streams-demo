package com.gvolpe.pricer.repository

import com.gvolpe.pricer.Order

import scalaz.concurrent.Task
import scalaz.stream.async

object OrderDb {

  val orderQ = async.boundedQueue[Order](100)

  def persist(order: Order): Task[Unit] = orderQ.enqueueOne(order)

}

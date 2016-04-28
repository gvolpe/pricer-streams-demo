package com.gvolpe.pricer.broker

import com.gvolpe.pricer.{Order, _}

import scalaz.concurrent.Task

trait Broker {
  def consume: ProcessT[Order]
  def publish(order: Order): Task[Unit]
}

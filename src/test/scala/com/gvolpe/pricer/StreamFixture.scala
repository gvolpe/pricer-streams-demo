package com.gvolpe.pricer

import scalaz.concurrent.Task
import scalaz.stream.{Exchange, async, sink}

trait StreamFixture {

  def createOrderQ  = async.unboundedQueue[Order]

  def createOrderEx(read: ProcessT[Order], write: Order => Task[Unit]) =
    Exchange[Order, Order](read, sink.lift[Task, Order]( order => write(order) ))

}

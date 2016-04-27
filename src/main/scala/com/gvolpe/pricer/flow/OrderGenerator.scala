package com.gvolpe.pricer.flow

import com.gvolpe.pricer.{Item, Order, _}

import scala.util.Random
import scalaz.Scalaz.{merge => _}
import scalaz.concurrent.Task
import scalaz.stream._

object OrderGenerator {

  val orderEx = {
    val orderQ = async.boundedQueue[OrderId](100)
    Exchange[OrderId, OrderId](
      orderQ.dequeue,
      sink.lift[Task, OrderId]( orderId =>
        orderQ.enqueueOne(orderId)
      )
    )
  }

  val orderGenCh: Channel[Task, Int, Order] = {
    val pf: Int => Task[Order] = { orderId =>
      Thread.sleep(2000)
      val itemId    = Random.nextInt(500).toLong
      val itemPrice = Random.nextInt(10000).toDouble
      Task.delay(Order(orderId.toLong, List(Item(itemId, s"laptop-$orderId", itemPrice))))
    }
    channel.lift(pf)
  }

  def flow(source: SinkT[Order]) = {
    Process.range(1, 10) through orderGenCh to source
  }

}

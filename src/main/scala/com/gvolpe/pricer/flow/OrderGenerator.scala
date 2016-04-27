package com.gvolpe.pricer.flow

import com.gvolpe.pricer.{Item, Order, _}

import scala.util.Random
import scalaz.Scalaz.{merge => _}
import scalaz.concurrent.Task
import scalaz.stream._

object OrderGenerator {

  private val defaultOrderGen: ChannelT[Int, Order] = {
    val pf: Int => Task[Order] = { orderId =>
      Thread.sleep(2000)
      val itemId    = Random.nextInt(500).toLong
      val itemPrice = Random.nextInt(10000).toDouble
      Task.delay(Order(orderId.toLong, List(Item(itemId, s"laptop-$orderId", itemPrice))))
    }
    channel.lift(pf)
  }

  def flow(source: SinkT[Order], orderGen: ChannelT[Int, Order] = defaultOrderGen) = {
    Process.range(1, 10) through orderGen to source
  }

}

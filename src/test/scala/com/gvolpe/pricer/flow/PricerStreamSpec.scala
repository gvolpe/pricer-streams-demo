package com.gvolpe.pricer.flow

import java.util.concurrent.Executors

import com.gvolpe.pricer.{Item, Order, _}

import scalaz.concurrent.{Strategy, Task}
import scalaz.stream.{Exchange, async, channel, sink}

import scala.concurrent.duration._

class PricerStreamSpec extends StreamSpec with PricerStreamFixture {

  behavior of "PricerStream"

  // TODO: Create the proper unit tests
//  it should "Generate ten orders" in {
//    implicit val pool = Executors.newFixedThreadPool(6)
//    implicit val strategy = Strategy.Executor(pool)
//    val (orderGenChannel, consumerEx) = createStreams
//    val result = for {
//      _         <-  OrderGenerator.flow(consumerEx.write, orderGenChannel)
//      updates   <-  consumerEx.read.take(10)
//    } yield {
//      updates shouldBe an [Order]
//    }
//    result.take(1).runLast.timed(5.seconds).run.get
//  }

}

trait PricerStreamFixture {

  def createStreams = {

    // TODO: Use generators to create Orders (org.scalacheck.Gen)
    val orderGenChannel: ChannelT[Int, Order] = {
      val pf: Int => Task[Order] = { orderId =>
        Task.delay { Order(orderId.toLong, List(Item(5L, s"laptop-$orderId", 250.00))) }
      }
      channel.lift(pf)
    }

    val kafkaQ = async.unboundedQueue[Order]

    val consumerEx = Exchange[Order, Order](
      kafkaQ.dequeue,
      sink.lift[Task, Order]( order =>
        kafkaQ.enqueueOne(order)
      )
    )

    (orderGenChannel, consumerEx)
  }

}
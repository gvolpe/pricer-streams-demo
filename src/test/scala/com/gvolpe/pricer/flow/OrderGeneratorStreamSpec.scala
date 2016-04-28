package com.gvolpe.pricer.flow

import java.util.concurrent.Executors

import com.gvolpe.pricer.generator.OrderTestGenerator
import com.gvolpe.pricer.{Order, _}

import scalaz.concurrent.{Strategy, Task}
import scalaz.stream.{async, channel}
import scala.concurrent.duration._

class OrderGeneratorStreamSpec extends StreamSpec with OrderGeneratorStreamFixture {

  behavior of "OrderGenerator"

  it should "generate ten orders" in {
    implicit val pool = Executors.newFixedThreadPool(6)
    implicit val strategy = Strategy.Executor(pool)
    val (orderGenChannel, consumerEx) = createStreams
    val result = for {
      _         <-  OrderGeneratorStream.flow(consumerEx.write, orderGenChannel)
      updates   <-  consumerEx.read.take(10)
    } yield {
      updates shouldBe an [Order]
    }
    result.take(1).runLast.timed(3.seconds).run.get
  }

}

trait OrderGeneratorStreamFixture extends StreamFixture {

  def createStreams = {

    val orderGenChannel: ChannelT[Int, Order] = {
      val pf: Int => Task[Order] = { orderId =>
        Task.delay { OrderTestGenerator.createRandomOrder.sample.get }
      }
      channel.lift(pf)
    }

    val kafkaQ      = async.unboundedQueue[Order]
    val consumerEx  = createOrderEx(kafkaQ.dequeue, kafkaQ.enqueueOne)

    (orderGenChannel, consumerEx)
  }

}
package com.gvolpe.pricer.flow

import java.util.concurrent.Executors

import com.gvolpe.pricer.service.PricerService
import com.gvolpe.pricer.{Order, _}

import scala.concurrent.duration._
import scalaz.concurrent.{Strategy, Task}
import scalaz.stream.{channel, sink}
import scalaz.stream.Process._

class PricerStreamSpec extends StreamSpec with PricerStreamFixture {

  behavior of "PricerStream"

  it should "publish an updated order downstream" in {
    implicit val pool = Executors.newFixedThreadPool(6)
    implicit val strategy = Strategy.Executor(pool)
    val (consumer, logger, storageEx, pricer, publisherEx, kafkaQ) = createStreams
    val result = for {
      _           <-  eval(kafkaQ.enqueueOne(testOrder))
      _           <-  PricerStream.flow(consumer, logger, storageEx, pricer, publisherEx.write)
      update      <-  publisherEx.read.take(1)
    } yield {
      update.items should be (List(Item(1L, "test", 2.1)))
    }
    result.take(1).runLast.timed(3.seconds).run.get
  }

}

trait PricerStreamFixture extends StreamFixture {

  val testOrder = Order(5L, List(Item(1L, "test", 2.0)))

  def createStreams = {
    val pricer: ChannelT[Order, Order] = {
      val pf: Order => Task[Order] = { order =>
        PricerService.updatePrices(order)
      }
      channel.lift(pf)
    }

    val kafkaQ      = createOrderQ
    val storageQ    = createOrderQ
    val downstreamQ = createOrderQ

    val consumerEx  = createOrderEx(kafkaQ.dequeue, kafkaQ.enqueueOne)
    val storageEx   = createOrderEx(storageQ.dequeue, storageQ.enqueueOne)

    val publisherEx = createOrderEx(downstreamQ.dequeue, (o: Order) =>
      showOrder("Publishing", o) flatMap (_ => downstreamQ.enqueueOne(o))
    )

    val logger      = sink.lift[Task, Order] { (order: Order) => showOrder("Consuming ", order) }

    (consumerEx.read, logger, storageEx, pricer, publisherEx, kafkaQ)
  }

}
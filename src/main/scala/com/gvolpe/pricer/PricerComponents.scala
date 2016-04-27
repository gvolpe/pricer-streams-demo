package com.gvolpe.pricer

import com.gvolpe.pricer.service.PricerService

import scalaz.{Order => _, _}
import scalaz.Scalaz.{merge => _}
import scalaz.concurrent.Task
import stream.{channel, _}

trait PricerComponents {

  val consumerEx: Exchange[Order, Order] = {
    val kafkaT = async.topic[Order]()
    Exchange[Order, Order](
      kafkaT.subscribe,
      sink.lift[Task, Order]( order =>
        kafkaT.publishOne(order)
      )
    )
  }

  val pricer: ChannelT[Order, Order] = {
    val pf: Order => Task[Order] = { order =>
      PricerService.updatePrices(order)
    }
    channel.lift(pf)
  }

  val storageEx: Exchange[Order, Order] = {
    val storageQ = async.boundedQueue[Order](100)
    Exchange[Order, Order](
      storageQ.dequeue,
      sink.lift[Task, Order]( order =>
        storageQ.enqueueOne(order)
      )
    )
  }

  val publisher: SinkT[Order] = sink.lift[Task, Order]( order =>
    showOrder("Publishing", order)
  )

  val logger: SinkT[Order] = sink.lift[Task, Order](order =>
    showOrder("Consuming ", order)
  )

}

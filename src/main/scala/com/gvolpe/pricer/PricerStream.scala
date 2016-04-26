package com.gvolpe.pricer

import scalaz._, Scalaz._
import scalaz.concurrent.Task
import stream.{channel, _}

object PricerStream {

  type ItemId   = Long
  type OrderId  = Long

  case class Item(id: ItemId, name: String, price: Double)
  case class Order(id: OrderId, items: List[Item])

  val consumerEx = {
    val kafkaT = async.topic[Order]()
    Exchange[Order, Order](
      kafkaT.subscribe,
      sink.lift[Task, Order]( order =>
        kafkaT.publishOne(order)
      )
    )
  }

  val pricerCh: Channel[Task, Order, Order] = {
    val pf: Order => Task[Order] = { order =>
      Task.now(order) //TODO: update prices
    }
    channel.lift(pf)
  }

  val storageEX = {
    val storageQ = async.boundedQueue[Order](100)
    Exchange[Order, Order](
      storageQ.dequeue,
      sink.lift[Task, Order]( order =>
        storageQ.enqueueOne(order)
      )
    )
  }

  val publisher: Sink[Task, Order] = {
    val downstreamQ = async.boundedQueue[Order](100)
    sink.lift[Task, Order]( order =>
      downstreamQ.enqueueOne(order)
    )
  }

  val logger = sink.lift[Task, Order](order =>
    Task.now(println(s"Consuming order: ${order.id}"))
  )

  def flow = {
    merge.mergeN(4)(
      Process(
        consumerEx.read   observe   logger    to  storageEX.write,
        storageEX.read    through   pricerCh  to publisher
      )
    )
  }

}

package com.gvolpe.pricer

import com.gvolpe.pricer.broker.{OrderKafkaBroker, OrderRabbitMqBroker}
import com.gvolpe.pricer.repository.OrderDb
import com.gvolpe.pricer.service.PricerService

import scalaz.{Order => _, _}
import scalaz.Scalaz.{merge => _}
import scalaz.concurrent.Task
import stream.{channel, _}

trait PricerComponents {

  private def createOrderEx(read: ProcessT[Order], write: Order => Task[Unit]) =
    Exchange[Order, Order](read, sink.lift[Task, Order]( order => write(order) ))

  val consumerEx = createOrderEx(OrderKafkaBroker.consume, OrderKafkaBroker.publish)

  val pricer: ChannelT[Order, Order] = {
    val pf: Order => Task[Order] = { order =>
      PricerService.updatePrices(order)
    }
    channel.lift(pf)
  }

  val storageQ = async.boundedQueue[Order](100)
  val storageEx = createOrderEx(storageQ.dequeue, (order: Order) => OrderDb.persist(order) flatMap (_ => storageQ.enqueueOne(order)))

  val publisher: SinkT[Order] = sink.lift[Task, Order]( order =>
    OrderRabbitMqBroker.publish(order)
  )

  val logger: SinkT[Order] = sink.lift[Task, Order](order =>
    showOrder("Consuming ", order)
  )

}

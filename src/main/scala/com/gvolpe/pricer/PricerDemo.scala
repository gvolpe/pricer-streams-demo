package com.gvolpe.pricer

import java.util.concurrent.Executors

import com.gvolpe.pricer.flow.{OrderGenerator, PricerStream}

import scalaz.{Order => _, _}
import scalaz.Scalaz.{merge => _, _}
import scalaz.concurrent.{Strategy, Task}
import stream.{channel, _}
import scala.concurrent.duration._
import scala.util.control.NonFatal

object PricerDemo extends App {

  val MarginProfit = 1.05

  val consumerEx = {
    val kafkaT = async.topic[Order]()
    Exchange[Order, Order](
      kafkaT.subscribe,
      sink.lift[Task, Order]( order =>
        kafkaT.publishOne(order)
      )
    )
  }

  private def updatePrices(order: Order): Task[Order] = Task.now {
    val updatedItems = order.items.map(i => i.copy(price = i.price * MarginProfit))
    order.copy(items = updatedItems)
  }

  val pricerCh: Channel[Task, Order, Order] = {
    val pf: Order => Task[Order] = { order =>
      updatePrices(order)
    }
    channel.lift(pf)
  }

  val storageEx = {
    val storageQ = async.boundedQueue[Order](100)
    Exchange[Order, Order](
      storageQ.dequeue,
      sink.lift[Task, Order]( order =>
        storageQ.enqueueOne(order)
      )
    )
  }

  def showOrder(action: String, order: Order) = Task.now {
    println(s"$action order ${order.id} with items ${order.items.map(_.toString).mkString(" | ")}")
  }

  val publisher: Sink[Task, Order] = {
    sink.lift[Task, Order]( order =>
      showOrder("Publishing", order)
    )
  }

  val logger = sink.lift[Task, Order](order =>
    showOrder("Consuming ", order)
  )

  implicit val pool = Executors.newFixedThreadPool(6)
  implicit val strategy = Strategy.Executor(pool)

  program.runAsync(_ => ())

  def program: Task[Unit] = {
    val streaming = Process(
      PricerStream.flow(consumerEx.read, logger, storageEx, pricerCh, publisher),
      OrderGenerator.flow(consumerEx.write)
    )
    merge.mergeN(maxOpen = 10)(streaming)
      .run
      .retry(List(10.seconds))
      .onFinish(restartProgram)
  }

  def restartProgram(error: Option[Throwable]) = error match {
    case Some(NonFatal(reason)) =>
      println(s"$reason, restarting the program.")
      program
    case None =>
      println("Unexpected error, restarting the program.")
      program
  }

}

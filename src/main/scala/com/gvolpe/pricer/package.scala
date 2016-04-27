package com.gvolpe

import scalaz.concurrent.Task
import scalaz.stream.{Channel, Process, Sink}

package object pricer {

  type ItemId           = Long
  type OrderId          = Long
  type ProcessT[A]      = Process[Task, A]
  type ChannelT[A, B]   = Channel[Task, A, B]
  type SinkT[A]         = Sink[Task, A]

  case class Item(id: ItemId, name: String, price: Double) {
    override def toString = s"$name : $price"
  }
  case class Order(id: OrderId, items: List[Item])

  def showOrder(action: String, order: Order) = Task.now {
    println(s"$action order ${order.id} with items ${order.items.map(_.toString).mkString(" | ")}")
  }

}

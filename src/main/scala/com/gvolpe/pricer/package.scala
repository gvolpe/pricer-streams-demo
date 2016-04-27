package com.gvolpe

import scalaz.concurrent.Task
import scalaz.stream.{Process, Sink}

package object pricer {

  type ItemId       = Long
  type OrderId      = Long
  type ProcessT[A]  = Process[Task, A]
  type SinkT[A]     = Sink[Task, A]

  case class Item(id: ItemId, name: String, price: Double) {
    override def toString = s"$name : $price"
  }
  case class Order(id: OrderId, items: List[Item])

}

package com.gvolpe.pricer.service

import com.gvolpe.pricer.Order

import scalaz.concurrent.Task

object PricerService {

  private val MarginProfit = 1.05

  def updatePrices(order: Order): Task[Order] = Task.now {
    val updatedItems = order.items.map(i => i.copy(price = i.price * MarginProfit))
    order.copy(items = updatedItems)
  }

}

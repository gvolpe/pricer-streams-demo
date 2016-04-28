package com.gvolpe.pricer.generator

import com.gvolpe.pricer.{Item, Order}
import org.scalacheck.Gen

object OrderTestGenerator {

  val FixedPrice        = 2.0
  val FixedUpdatedPrice = 2.1

  def createRandomOrder = for {
    orderId <- Gen.choose(1L, 100L)
    items   <- createRandomItems
  } yield Order(orderId, items)

  def createRandomItems = for {
    itemId  <- Gen.choose(1L, 500L)
    name    <- Gen.alphaStr
    price   <- Gen.choose(100.00, 1500.00)
  } yield List(Item(itemId, name, price))

  def createOrder = for {
    orderId <- Gen.choose(1L, 100L)
    items   <- createItemList
  } yield Order(orderId, items)

  def createItem  = for {
    itemId  <- Gen.choose(1L, 500L)
    name    <- Gen.alphaStr
  } yield Item(itemId, s"item-$name", FixedPrice)

  val createItemList = for {
    item1 <- createItem
    item2 <- createItem
    item3 <- createItem
  } yield List(item1, item2, item3)

}

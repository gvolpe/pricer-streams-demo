package com.gvolpe.pricer.generator

import com.gvolpe.pricer.{Item, Order}
import org.scalacheck.Gen

object OrderTestGenerator {

  val FixedPrice        = 2.0
  val FixedUpdatedPrice = 2.1

  def createRandomOrder = for {
    orderId <- Gen.choose(1L, 100L)
    items   <- createRandomItemList
  } yield Order(orderId, items)

  def createRandomItem = for {
    itemId  <- Gen.choose(1L, 500L)
    name    <- Gen.alphaStr
    price   <- Gen.choose(100.00, 1500.00)
  } yield Item(itemId, name, price)

  def createRandomItemList = Gen.containerOfN[List, Item](3, createRandomItem)

  def createOrder = for {
    orderId <- Gen.choose(1L, 100L)
    items   <- createItemList
  } yield Order(orderId, items)

  def createItem  = for {
    itemId  <- Gen.choose(1L, 500L)
    name    <- Gen.alphaStr
  } yield Item(itemId, s"item-$name", FixedPrice)

  def createItemList = Gen.containerOfN[List, Item](5, createItem)

}

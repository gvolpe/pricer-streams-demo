package com.gvolpe.pricer.flow

import com.gvolpe.pricer.{Order, _}

import scalaz.Scalaz.{merge => _}
import scalaz.concurrent.{Strategy, Task}
import scalaz.stream._

object PricerStream {

  def flow(consumer: ProcessT[Order],
           logger: Sink[Task, Order],
           storageEx: Exchange[Order, Order],
           pricerCh: Channel[Task, Order, Order],
           publisher: Sink[Task, Order])
          (implicit S: Strategy)= {
    merge.mergeN(
      Process(
        consumer          observe   logger    to  storageEx.write,
        storageEx.read    through   pricerCh  to  publisher
      )
    )
  }

}

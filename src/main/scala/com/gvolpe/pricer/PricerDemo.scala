package com.gvolpe.pricer

import java.util.concurrent.Executors

import com.gvolpe.pricer.flow.{OrderGeneratorStream, PricerStream}

import scalaz.{Order => _, _}
import scalaz.Scalaz.{merge => _, _}
import scalaz.concurrent.{Strategy, Task}
import stream._
import scala.concurrent.duration._
import scala.util.control.NonFatal

object PricerDemo extends App with PricerComponents {

  implicit val pool = Executors.newFixedThreadPool(6)
  implicit val strategy = Strategy.Executor(pool)

  program.runAsync(_ => ())

  def program: Task[Unit] = {
    val streaming = Process(
      PricerStream.flow(consumerEx.read, logger, storageEx, pricer, publisher),
      OrderGeneratorStream.flow(consumerEx.write)
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

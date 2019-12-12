package twtutorial

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

// Documentation: http://www.scala-lang.org/api/current/scala/concurrent/index.html

object Futures1 extends App {

  // 1. Create a Future - asynchronous computation
  val f = Future {
      Thread.sleep(500)
      println(Thread.currentThread.getName)
      1 + 1
  }

  // 2. Await for its completion. Blocking -- SHOULD NOT be used!
  val result = Await.result(f, 1 second) // 1 second == 1.second() + implicit conversion
  println(result)
  println(Thread.currentThread.getName)
  Thread.sleep(1000)
}

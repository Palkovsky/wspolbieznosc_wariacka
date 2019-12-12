package twtutorial

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import scala.util.Random

object Futures3 extends App {

  def asyncPrint(n: Int): Future[Int] = Future {
    blocking { Thread.sleep(Random.nextInt(500)) }
    println(n)
    n
  }

  /*
  val f1 = asyncPrint(1)
  val f2 = asyncPrint(2)
  val f3 = asyncPrint(3)
   */

  // Exercise: ensure that 1,2,3 are written in order using combinators (see Futures4 and Futures5.scala)
  asyncPrint(1) flatMap {_ => asyncPrint(2)} flatMap {_ => asyncPrint(3)}


  Thread.sleep(2000)
}

package twtutorial

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scala.util.{ Success, Failure }
import scala.util.Random


object Futures4 extends App {

  def asyncWork(n: Int): Future[Int] = Future {
    blocking { Thread.sleep(Random.nextInt(500)) }
    n
  }

  // map
  val stringF1 = asyncWork(3).map(n => n.toString)
  stringF1 onComplete {
    case Success(value) => println(s"stringF1 = $value")
    case Failure(e)     => e.printStackTrace
  }

  // map vs. flatMap

  // val stringF: Future[Future[String]]
  val stringF = asyncWork(4).map(n => Future(n.toString))
  stringF onComplete {
    case Success(value) => println(s"stringF = $value")
    case Failure(e)     => e.printStackTrace
  }

  // val flatStringF: Future[String]
  val flatStringF = asyncWork(5).flatMap(n => Future(n.toString))
  flatStringF onComplete {
    case Success(value) => println(s"flatStringF = $value")
    case Failure(e)     => e.printStackTrace
  }

  Thread.sleep(2000)
}

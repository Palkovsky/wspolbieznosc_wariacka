package twtutorial

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scala.util.{ Success, Failure }
import scala.util.Random

// Further reading:
// 1. Utilities from Future companion object: https://www.scala-lang.org/api/current/scala/concurrent/Future$.html
//    - E.g. 'Future.sequence' converts a collection of futures into one future for that collection
// 2. More about composing futures: http://rerun.me/2016/06/12/scala-futures-3-combinators-and-async/

object Futures5 extends App {


  def asyncWork(n: Int): Future[Int] = Future {
    blocking { Thread.sleep(Random.nextInt(500)) }
    println(n)
    n
  }

  val result1 = asyncWork(1)
  val result2 = asyncWork(2)
  val result3 = asyncWork(3)


  // for comprehension is equivalent to
  // result1.flatMap(r1 => result2.flatMap(r2 => result3.map(r3 => r1 + r2 + r3) ) )
  val sum = for {
    r1 <- result1
    r2 <- result2
    r3 <- result3
  } yield (r1 + r2 + r3)

  val sqr = sum map { s => s * s }
  sqr onComplete {
    case Success(value) => println(s"Result = $value")
    case Failure(e)     => e.printStackTrace
  }

  Thread.sleep(2000)
}

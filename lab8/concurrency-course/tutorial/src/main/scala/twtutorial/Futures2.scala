package twtutorial

import scala.concurrent.{Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.util.Random
import scala.language.postfixOps

object Futures2 extends App {

    val foo = Future {
        Thread.sleep(Random.nextInt(500))
        42
    }

    println("before onComplete")
    
    foo.onComplete {
        case Success(value) => println(s"Got the callback, meaning = $value")
        case Failure(e) => e.printStackTrace
    }
    
    // other work
    println("A ..."); Thread.sleep(100)
    println("B ..."); Thread.sleep(100)
    println("C ..."); Thread.sleep(100)
    
    Thread.sleep(2000)
}

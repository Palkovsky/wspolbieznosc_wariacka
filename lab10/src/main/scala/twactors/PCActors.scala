package twactors

import java.lang.Math
import java.util.Random

import akka.actor.Actor
import akka.actor.Stash
import akka.actor.ActorRef
import akka.actor.Props
import akka.event.Logging
import akka.event.LoggingReceive
import akka.actor.ActorSystem
import scala.concurrent.duration._
import scala.concurrent.Await

// Assignment:
// - implement solution to the producers/consumers problem
//   using the actor model / Akka
// - test the correctness of the created solution for multiple
//   producers and consumers
// Hint: use akka.actor.Stash

// object PC contains messages for all actors -- add new if you need them
object PC {
  case class Init()

  // Producer/Consumer requests
  case class Put(x: Long)
  case class Get()

  // Buffer responses
  case class PutSuccess(x: Long)
  case class GetSuccess(x: Long)
}

class Producer(val name: String, buf: ActorRef) extends Actor {
  import PC._

  def receive =  {
    case Init() => {
      println(s"$name: Init")
      self ! Put(0)
    }

    case Put(x) => {
      println(s"$name: Requesting PUT of $x")
      buf ! Put(x)
    }

    case PutSuccess(x) => {
      println(s"$name: PUT of $x successful")
      self ! Put(x+1)
    }

    // Ignore unsuported
    case _ => {}
  }
}

class Consumer(val name: String, buf: ActorRef) extends Actor {
  import PC._

  def receive =  {
    case Init() => {
      println(s"$name: Init")
      self ! Get()
    }

    case Get() => {
      println(s"$name: Sending GET request...")
      buf ! Get()
    }

    case GetSuccess(x) => {
      println(s"$name: GET successful -  $x")
      self ! Get()
    }

    // Ignore unsuported
    case _ => {}
  }

}

class Buffer(val n: Int) extends Actor with Stash {
  import PC._

  private var buf = new Array[Long](n)
  private var count = 0

  def receive =  {
    case Put(x) if (count < n) => {
      println(s"buffer: PUT request for $x. Store at position $count")
      buf(count) = x
      count += 1
      sender() ! PutSuccess(x)
      unstashAll()
    }

    case Get() if (count > 0) => {
      println(s"buffer: GET request. Take from positon ${count-1}")
      count -= 1
      sender() ! GetSuccess(buf(count))
      unstashAll()
    }

    // When no space in buffer
    case Put(x) if (count == n) => {
      println(s"buffer: PUT request for $x. No free space. Stashing...")
      stash()
    }

    // When buffer empty
    case Get() if (count == 0) => {
      println(s"buffer: GET request. No items available. Stashing...")
      stash()
    }

    // Ignore unsuported
    case _ => {}
  }
}


object ProdConsMain extends App {
  import PC._

  val simulation_time = 5
  val (buffer_capacity, consumer_count, producer_count) = (10, 10, 10)

  val system = ActorSystem("ProdKons")

  val buffer: ActorRef = system.actorOf(Props(new Buffer(buffer_capacity)), name = "buffer")

  // TODO: create Consumer actors. Use "p ! Shutdown" to kick them off
  val consumers: Seq[ActorRef] = (1 to consumer_count)
    .map { i => system.actorOf(Props(new Consumer("consumer_" + i, buffer)), "consumer_" + i)  }

  // TODO: create Producer actors. Use "p ! Shutdown" to kick them off
  val producers: Seq[ActorRef] = (1 to producer_count)
    .map { i => system.actorOf(Props(new Producer("producer_" + i, buffer)), "producer_" + i)  }

  val all = producers ++ consumers ++ List(buffer)

  // Init 'em all
  all foreach { actor => actor ! Init() }
  // Let simulation run for some time
  Thread.sleep(simulation_time * 1000)

  system.terminate()
  Await.result(system.whenTerminated, Duration.Inf)
}

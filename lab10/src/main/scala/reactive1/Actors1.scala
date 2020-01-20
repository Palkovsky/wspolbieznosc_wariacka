package reactive1

import akka.actor._
import scala.concurrent.duration._
import scala.concurrent.Await
//////////////////////////////////////////
// Introduction to Scala (Akka) Actors  //
//////////////////////////////////////////


/**
 * Actor:
 * - an object with identity
 * - with a behavior
 * - interacting only via asynchronous messages
 *
 * Consequently: actors are fully encapsulated / isolated from each other
 * - the only way to exchange state is via messages (no global synchronization)
 * - all actors run fully concurrently 
 *
 * Messages:
 * - are received sequentially and enqueued
 * - processing one message is atomic
 *
**/


/**
 * type Receive = PartialFunction[Any, Unit]
 *           Any  -> any message can arrive
 *           Unit -> the actor can do something, but does not return anything
 *            
 * trait Actor {
 *     implicit val self: ActorRef
 *     def receive: Receive
 *     ...
 * }
 *
 * API documentation: https://akka.io/docs/
 * 
**/


/**
 * Logging options: read article
 * https://doc.akka.io/docs/akka/current/scala/logging.html
 *
 * a) ActorLogging
 * class MyActor extends Actor with akka.actor.ActorLogging {
 *  ...
 * }
 *
 * b) LoggingReceive
 *
 *  def receive = LoggingReceive {
 *     ....
 *  }
 * 
 *
**/


class Counter extends Actor {
  var count = 0
  def receive: Receive = {
    case "incr" => count += 1; println(Thread.currentThread.getName + ".")
    case "get"  => sender ! count // "!" operator is pronounced "tell" in Akka
  }
}
 
/**
 * Sending messages: "tell" method
 *
 *  abstract class ActorRef {
 *    def !(message: Any)(implicit sender: ActorRef = Actor.noSender): Unit
 *    ...
 *  }
 * 
**/

class CounterMain extends Actor {
  
  def receive: Receive = {
    case "init" =>
      val counter = context.actorOf(Props[Counter], "counter")
      counter ! "incr"
      counter ! "incr"
      counter ! "incr"
      counter ! "get"
     
    case count: Int =>
      println(s"count received: $count" )
      println(Thread.currentThread.getName + ".")
      context.system.terminate
  }
}


object ApplicationMain extends App {
  val system = ActorSystem("Reactive1")
  val mainActor = system.actorOf(Props[CounterMain], "mainActor")

  mainActor ! "init"

  Await.result(system.whenTerminated, Duration.Inf)
}

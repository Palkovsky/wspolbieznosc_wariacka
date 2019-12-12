import webcrawler._

import scala.concurrent._
import ExecutionContext.Implicits.global

import scala.util.{Success, Failure}

object Main extends App {

  val url = "https://www.icsr.agh.edu.pl/~balis/"
  val future  = Crawler(url).get
   future foreach {
     (parsed) => {
       parsed.childrenUrls.foreach(println)
     }
   }

  Thread.sleep(5000)
}

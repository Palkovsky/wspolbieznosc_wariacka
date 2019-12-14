import webcrawler._

import scala.concurrent._
import ExecutionContext.Implicits.global

import scala.util.{Success, Failure}
import scala.concurrent.duration._

object Main extends App {

  val url = "https://www.icsr.agh.edu.pl/~balis/"
  val crawler = CrawlerManager(url, 1)

  crawler.start()

  Thread.sleep(10000)

  for(future <- crawler.futurePool) {
    if(future.isCompleted) {
      future.value match {
        case Some(Success(Some(x))) => {
          println("Done " + x.url)
        }
        case _ => "Failed"
      }
    } else {
      println("Unfinished")
    }
  }
}

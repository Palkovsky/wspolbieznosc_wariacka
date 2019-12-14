import webcrawler._

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Try
import scala.util.{Success, Failure}
import scala.collection.mutable._;

import java.net.URL

class CrawlerManager private(val rootUrl: String, val maxDepth: Int) {
  private type MParsed = Option[Parsed]
  private type FParsed = Future[MParsed]
  private type Depth = Int

  val futurePool = Set[FParsed]()

  def start(): Option[FParsed] = {
    addFuture(rootUrl, 1)
  }

  private def addFuture(rootUrl: String, depth: Depth): Option[FParsed] = {
    if(depth-1 > maxDepth) {
      return None
    }

    CrawlerManager.strToURL(rootUrl) map {
      (url) => {
        val future = Crawler(url)
        future foreach {
          (futureResult) => {
            futureResult map {
              (result) => {
                val next = result.childrenUrls map {
                  (url) => {
                    addFuture(url, depth+1)
                  }
                }
              }
            }
          }
        }

        futurePool.add(future)
        future
      }
    }
  }
}

object CrawlerManager {
  def apply(rootUrl: String, maxDepth: Int): CrawlerManager = {
    new CrawlerManager(rootUrl, maxDepth)
  }

  def strToURL(strUrl: String): Option[URL] = {
    Try(new URL(strUrl)) map {
      (url) => Some(url)
    } getOrElse {
      None
    }
  }
}

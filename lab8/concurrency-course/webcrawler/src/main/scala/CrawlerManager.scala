import webcrawler._

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Try
import scala.util.{Success, Failure}
import scala.collection.mutable._;

import java.net.URL
import java.util.concurrent.{ConcurrentHashMap, Executors, ThreadFactory}
import java.lang.{Runnable, Thread}
import java.util.concurrent.atomic.AtomicInteger
import java.util.Collections

class CrawlerManager private(val rootUrl: String, val maxDepth: Int) {
  private type MParsed = Option[Parsed]
  private type MParsedD = Option[(Depth, Parsed)]
  private type FParsed = Future[MParsed]
  private type Depth = Int

  // URLs that no longer will be traversed
  private val doneUrls = Set[String]()

  /**
    *  Called on singe future end.
    *  Allows handling results more dynamically, before everyting finishes.
    */
  private var walkCallback: Option[MParsedD => Unit] = None

  def start(): Option[Future[Set[(Depth, Parsed)]]] = {
    newTask(rootUrl, 1)
  }

  /**
    * Sets user callback for on-the-fly crawling.
    */
  def onParsed(f: MParsedD => Unit): Unit = {
    this.walkCallback = Some(f)
  }

  /**
    * Adds new future, chains it with children futures and adds it to the futurePool.
    */
  private def newTask(rootUrl: String, depth: Depth): Option[Future[Set[(Depth, Parsed)]]] = {
    if(depth > maxDepth || doneUrls.contains(rootUrl)) {
      return None
    }

    doneUrls.add(rootUrl)

    CrawlerManager.strToURL(rootUrl) map {
      url => {
        val future = Future {
          import webcrawler._

          val maybeResult = Crawler.parse(url)
          // Apply user callback
          walkCallback map {
            f => {
              Try(f(maybeResult map { result => (depth, result) }))
            }
          }

          maybeResult map {
            result => {
              val futures = result.childrenUrls map {
                url => newTask(url, depth+1)
              } collect {
                case Some(fut) => fut
              }

              val future = Future.sequence(futures.toList)
              future map {
                futureSet => {
                  val flatten = Set(futureSet).flatten.flatten
                  flatten.add((depth, result))
                  flatten
                }
              }
            }
          } getOrElse {
            Future { Set[(Depth, Parsed)]() }
          }
        }

        for {
          nextFuture <- future
          results <- nextFuture
        } yield (results)
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

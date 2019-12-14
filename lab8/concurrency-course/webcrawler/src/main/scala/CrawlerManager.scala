import webcrawler._

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Try
import scala.util.{Success, Failure}
import scala.collection.mutable._;

import java.net.URL
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.ConcurrentHashMap
import java.util.Collections

class CrawlerManager private(val rootUrl: String, val maxDepth: Int) {
  private type MParsed = Option[Parsed]
  private type MParsedD = Option[(Depth, Parsed)]
  private type FParsed = Future[MParsed]
  private type Depth = Int

  // Set of all futures.
  private val futurePool: Set[(Depth, FParsed)] = {
    import scala.collection.JavaConverters._

    val map = new ConcurrentHashMap[(Depth, FParsed), java.lang.Boolean]
    val javaSet = Collections.newSetFromMap(map)

    javaSet.asScala
  }
  // Counter to keep track of finished futures.
  private val finishedCounter = new AtomicInteger(0)

  // URLs that no longer will be traversed
  private val doneUrls = Set[String]()

  /**
    *  Called on singe future end.
    *  Allows handling results more dynamically, before everyting finishes.
    */
  private var walkCallback: Option[MParsedD => Unit] = None

  def start(): Unit = {
    newTask(rootUrl, 1)
  }

  /**
    *  Creates Future awaiting end of all futures from futurePool.
    */
  def awaitable(): Future[Set[(Depth, Parsed)]] = Future {
    while (futurePool.size != finishedCounter.get) {
      Thread.sleep(100)
    }

    val result = Set[(Depth, Parsed)]()
    for ((depth, future) <- futurePool) {
      future.value match {
        case Some(Success(Some(x))) =>
          result.add((depth, x))
        case _ => {}
      }
    }

    result
  }

  def onParsed(f: MParsedD => Unit): Unit = {
    this.walkCallback = Some(f)
  }

  /**
    * Adds new future, chains it with children futures and adds it to the futurePool.
    */
  private def newTask(rootUrl: String, depth: Depth): Option[FParsed] = {
    if(depth > maxDepth || doneUrls.contains(rootUrl)) {
      return None
    }

    doneUrls.add(rootUrl)
    CrawlerManager.strToURL(rootUrl) map {
      (url) => {
        val future = Crawler(url)

        future foreach {
          (futureResult) => {

            finishedCounter.incrementAndGet()
            walkCallback map {
              f => {
                Try(f(futureResult map { result => (depth, result) }))
              }
            }

            futureResult map {
              (result) => {
                result.childrenUrls foreach { (url) => newTask(url, depth+1) }
              }
            }
          }
        }

        futurePool.add((depth, future))
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

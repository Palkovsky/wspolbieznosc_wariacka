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
  private type Depth = Int

  // Keeps track of already traversed URLs.
  private val doneUrls = Set[String]()

  /**
    *  Called when page parsed.
    *  Allows performing some arbitrary logic on-the-fly.
    */
  private var walkCallback: Option[(Depth, Parsed) => Unit] = None

  def start(): Option[Future[Set[(Depth, Parsed)]]] = {
    newTask(rootUrl, 1)
  }

  /**
    * Sets user callback for on-the-fly crawling.
    */
  def onParsed(f: (Depth, Parsed) => Unit): Unit = {
    this.walkCallback = Some(f)
  }

  private def newTask(rootUrl: String, depth: Depth): Option[Future[Set[(Depth, Parsed)]]] = {
    // Check if maxDepth reached or url excluded.
    if(depth > maxDepth || doneUrls.contains(rootUrl)) {
      return None
    }

    // Make url excluded in future.
    doneUrls.add(rootUrl)

    // Check if URL is valid one.
    val maybeUrl = CrawlerManager.strToURL(rootUrl)
    if(maybeUrl.isEmpty) {
      return None
    }

    val url = maybeUrl.get
    val future = Future {
       blocking { Crawler(url) }
    } flatMap {
      joinResult(depth)
    }
    Some(future)
  }

  /*
   * Takes parsing result and returns future returning all joined results from children branches.
   */
  private def joinResult(depth: Depth)(maybeResult: Option[Parsed]): Future[Set[(Depth, Parsed)]] = {
    // If failed, return neutral thing.
    if(maybeResult.isEmpty) {
      return Future { Set[(Depth, Parsed)]() }
    }

    val result = maybeResult.get
    // Apply user-provided callback
    walkCallback map {
      f => blocking { Try(f(depth, result)) }
    }

    // Create children futures
    val childrenFutures = result.childrenUrls map {
      url => newTask(url, depth+1)
    } collect {
      case Some(fut) => fut
    }

    // Merge children futures using Future.sequence
    val joined = Future.sequence(childrenFutures.toList)

    // Flatten results from all children futures.
    // Add current result to the result set.
    joined map {
      futureSet => {
        val flatten = Set(futureSet).flatten.flatten
        flatten.add((depth, result))
        flatten
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

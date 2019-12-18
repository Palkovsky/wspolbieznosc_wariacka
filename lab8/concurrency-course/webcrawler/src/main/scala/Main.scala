import webcrawler._

import scala.concurrent._
import ExecutionContext.Implicits.global

import scala.util.{Success, Failure}
import scala.concurrent.duration._
import scala.util.Try

import java.io._
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

object Main extends App {
  // Run parameters
  val url = "https://www.icsr.agh.edu.pl/~balis/"
  val depth = 4

  // Prepare working directory
  prepareDirectory()
  var counter = 0

  // Start  page traversal
  val crawler = CrawlerManager(url, depth)

  // While traversing, save to file
  crawler onParsed {
    (depth, parsed) => {
      counter += 1

      val value = counter
      val path = "crawled/" + value + ".html"

      println(s"[onParsed][$depth]: ${parsed.url} as $path")
      parsed.save(path)
    }
  }

  // Wait for finish and print summary
  val future = crawler.start().get
  // val future = crawler.awaitable()
  val results = Await.result(future, Duration.Inf)

  // URLs done parsing
  println(s"Parsed URLs: ")
  results foreach {
    case (depth, result) =>
      println(s"[*][$depth] ${result.url}")
  }

  // Seen URLs
  println(s"Seen URLs:")
  results map {
    case (depth, result) => (depth, result.childrenUrls)
  } flatMap {
    case (depth, urls) => urls map { url => (depth, url) }
  } foreach {
    case (depth, url) =>
      println(s"[-][$depth] $url")
  }

  def prepareDirectory(): Unit = {
    val outDir = new File("./crawled")
    if(outDir.exists() && !outDir.isDirectory()){
      outDir.delete()
    }
    if(!outDir.exists()){
      outDir.mkdir()
    }
    outDir.listFiles foreach {
      (file) => file.delete()
    }
  }
}

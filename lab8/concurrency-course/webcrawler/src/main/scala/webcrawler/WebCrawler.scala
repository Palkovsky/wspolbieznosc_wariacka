package webcrawler

import org.htmlcleaner.{TagNode, HtmlCleaner}
import java.net.URL

import scala.io.Source
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Try

class ParseResult(val url: URL, root: TagNode) {
  val childrenUrls = {
    val elements = root.getElementsByName("a", true)
    elements map { elem =>
      elem.getAttributeByName("href")
    }
  }
}

object Crawler {
  val cleaner = new HtmlCleaner
  val props = cleaner.getProperties

  /**
    * Returns Option with Futre containing crawled data.
    *
    * None returned when URL exception caught.
    */
  def apply(urlStr: String): Option[Future[ParseResult]] = {
    val urlT = Try(new URL(urlStr))
    val futureT = urlT map { url => Some(Future { parse(url) }) }
    futureT.getOrElse(None)
  }

  private def parse(url: URL): ParseResult = {
    val root = cleaner.clean(url)
    new ParseResult(url, root)
  }
}

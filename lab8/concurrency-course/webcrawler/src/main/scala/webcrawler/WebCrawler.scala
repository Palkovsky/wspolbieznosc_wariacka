package webcrawler

import org.htmlcleaner.{TagNode, HtmlCleaner}
import java.net.URL

import scala.io.Source
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Try

class Parsed(val url: URL, root: TagNode) {
  val childrenUrls = {
    val elements = root.getElementsByName("a", true)
    elements map {
      (elem) => elem.getAttributeByName("href")
    }
  }
}

object Crawler {
  val cleaner = new HtmlCleaner
  val props = cleaner.getProperties

  def apply(url: URL): Future[Option[Parsed]] = {
    Future { parse(url) }
  }

  private def parse(url: URL): Option[Parsed] = {
    val rootT = Try(cleaner.clean(url))
    val resultT = rootT map {
      (root) => Some(new Parsed(url, root))
    }
    resultT getOrElse { None }
  }
}

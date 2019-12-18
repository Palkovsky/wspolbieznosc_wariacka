package webcrawler

import scala.concurrent._
import ExecutionContext.Implicits.global

import org.htmlcleaner.{TagNode, HtmlCleaner, PrettyXmlSerializer}
import java.net.URL

import scala.io.Source
import scala.util.Try

class Parsed(val url: URL, val root: TagNode) {
  val childrenUrls = {
    val elements = root.getElementsByName("a", true)
    elements map {
      (elem) => elem.getAttributeByName("href")
    }
  }

  def save(path: String): Boolean = {
    val cleaner = new HtmlCleaner
    val props = cleaner.getProperties

    Try(new PrettyXmlSerializer(props)) map {
      serializer => serializer.writeToFile(root, path, "utf-8")
    } map {
      _ => true
    } getOrElse {
      false
    }
  }
}

object Crawler {
  val cleaner = new HtmlCleaner
  val props = cleaner.getProperties

  def apply(url: URL): Option[Parsed] = {
    // println(s"Starting ${url.toString}...")
    val rootT = Try(cleaner.clean(url))
    val resultT = rootT map {
      (root) => Some(new Parsed(url, root))
    }
    resultT getOrElse { None }
  }
}

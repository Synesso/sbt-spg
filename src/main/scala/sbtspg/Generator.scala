package sbtspg

import java.io.File
import java.nio.file.Path

import com.tristanhunt.knockoff.DefaultDiscounter._
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.language.postfixOps
import scala.util.Try
import scala.xml.Node

case class MarkupSource(source: Source, relativeName: Path) {

  def parseMatter: MarkupWithConfig = {
    val lines = source.getLines().toStream
    def loop(remaining: Stream[String], started: Boolean, consumed: Seq[String]): (Config, Stream[String]) = {
      (remaining, started) match {
        case ("---" #:: xs, false) => loop(xs, started = true, consumed)
        case ("---" #:: xs, true) => (ConfigFactory.parseString(consumed.reverse.mkString("\n")), xs)
        case (x #:: xs, true) => loop(xs, started, x +: consumed)
        case (Stream.Empty, _) => (ConfigFactory.empty, lines)
        case (_, false) => (ConfigFactory.empty, remaining)
      }
    }
    val (conf, stream) = loop(lines, started = false, Seq.empty)
    MarkupWithConfig(conf, stream, relativeName)
  }
}

case class SiteData(tags: Set[String] = Set.empty) {
  def include(pageConfig: Config) = {
    val incTags =
      if (pageConfig.hasPath("tags")) pageConfig.getStringList("tags").toSet
      else Set.empty[String]
    copy(tags = tags ++ incTags)
  }
  def tagString: Option[String] = if (tags.isEmpty) None else Some(tags.toSeq.sorted.mkString(", "))
}

case class MarkupWithConfig(conf: Config, stream: Stream[String], relativeName: Path) {
  // todo - parse the template and apply them - NEXT, a test for this.
  // todo - substitute all site and page data

  val template: Future[Stream[String]] = Future {
    Try(conf.getString("template"))
      .map(fn => Source.fromFile(fn).getLines().toStream)
      .getOrElse("{{content}}" #:: Stream.empty[String])
  }

  def preProcess(data: SiteData): Page = {
    // todo - this doesn't have a test!
    // todo - either it's in the dictionary or it's scala to be interpreted
    val stream_ = stream.map(_.replaceAll("\\{\\{tags\\}\\}", data.tagString.getOrElse("")))
    // todo - plus all other special sitedata + all meta
    // todo - how to cater for tagString being optional in template
    val content = toXHTML(knockoff(stream_.mkString("\n")))
    Page(content, relativeName)
  }
}

case class Page(content: Node, relativeName: Path) {

  def write(baseDir: File)(implicit pathToName: (Path) => String): File = {
    val file = new File(baseDir, pathToName(relativeName))
    file.getParentFile.mkdirs
    val pw = new java.io.PrintWriter(file)
    try pw.write(Generator.printer.format(content)) finally pw.close()
    file
  }
}

class Generator(articlesDir: File, draftsDir: File, layoutsDir: File, targetDir: File) {

  def generate: Set[File] = Await.result(transformed, 1 minute)

  // source & relative path (MarkupSource) ->
  // map, stream, relativePath (MarkupWithConfig) ->
  // (create SiteData from maps)
  // node & relative path (Page) ->
  // IO write

  private val output: Future[Set[Page]] = for {
    sources <- Generator.sources(articlesDir).map(_.map(_.parseMatter))
  } yield {
    val siteData = sources.foldLeft(SiteData()){(sd, src) => sd.include(src.conf)}
    sources.map(_.preProcess(siteData))
  }

  implicit val pathToName = Generator.replaceExtensionWithHtml _

  private def transformed: Future[Set[File]] = output.map{_.map{_.write(targetDir)}}
}

object Generator {

  val printer = new scala.xml.PrettyPrinter(140, 2)
  private val allowedExtensions = Set(".md", ".markdown")

  def sources(dir: File): Future[Set[MarkupSource]] = Future {
    def loop(f: File): Set[MarkupSource] = {
      if (f.isDirectory) f.listFiles.toSet.flatMap(loop)
      else if (f.isFile && allowedExtensions.contains(extension(f)))
        Set(MarkupSource(Source.fromFile(f), dir.toPath.relativize(f.toPath)))
      else Set.empty
    }
    if (dir.isDirectory) loop(dir) else Set.empty
  }

  def replaceExtensionWithHtml(s: Path) = s.toString.replaceFirst("""(?s)(.*)\.[^/\\]+$""", "$1.html")

  private def extension(f: File) = if (f.getName.contains(".")) f.getName.substring(f.getName.lastIndexOf(".")) else ""

}

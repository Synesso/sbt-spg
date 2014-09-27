package sbtspg

import java.io.File
import java.nio.file.Path

import com.tristanhunt.knockoff.DefaultDiscounter._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.language.postfixOps
import scala.xml.Node

case class MarkupSource(source: Source, relativeName: Path) {
  def parseFrontMatter: MarkupWithMeta = {
    val lines = source.getLines().toStream
    def loop(remaining: Stream[String], started: Boolean, matter: Map[String, String]): (Map[String, String], Stream[String]) = {
      (remaining, started) match {
        case (Stream.Empty, _) => (Map.empty, lines)
        case ("---" #:: xs, true) => (matter, remaining.tail)
        case ("---" #:: xs, false) => loop(xs, started = true, matter)
        case (x #:: xs, true) => x.split(":", 2) match {
          case Array(k, v) => loop(xs, started, matter + (k.trim -> v))
          case _ => loop(xs, started, matter)
        }
        case _ => (matter, lines)
      }
    }
    val (meta, stream) = loop(lines, started = false, Map.empty)
    MarkupWithMeta(meta, stream, relativeName)
  }
}

case class SiteData(tags: Set[String] = Set.empty) {
  def include(source: MarkupWithMeta) = SiteData(
    tags = source.meta.get("tags").map(_.split(",").toSet ++ tags).getOrElse(tags)
  )
  def tagString: Option[String] = if (tags.isEmpty) None else Some(tags.toSeq.sorted.mkString(", "))
}

case class MarkupWithMeta(meta: Map[String, String], stream: Stream[String], relativeName: Path) {
  // todo - parse the templates and apply them

  def preProcess(data: SiteData): Page = {
    val stream_ = stream.map(_.replaceAll("\\{tags\\}", data.tagString.getOrElse("")))
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
  // map, stream, relativePath (MarkupWithMeta) ->
  // (create SiteData from maps)
  // node & relative path (Page) ->
  // IO write

  private val output: Future[Set[Page]] = for {
    sources <- Generator.sources(articlesDir).map(_.map(_.parseFrontMatter))
  } yield {
    val siteData = sources.foldLeft(SiteData()){(sd, src) => sd.include(src)}
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


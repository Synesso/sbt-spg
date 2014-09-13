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

class Generator(articlesDir: File, draftsDir: File, layoutsDir: File, targetDir: File) {

  def generate: Set[File] = Await.result(transformedFiles, 1 minute)

  private lazy val sources = Generator.sources(articlesDir)

  private lazy val transformedFiles: Future[Set[File]] = {

    /*
    todo 's
    find all sources - sources
    map to extract matter and remainder - frontMatterAndContent
    merge the matter to get all site data - todo
    process remainder with site data - todo
    knockoff transform remainders - todo
    save to file - write
     */

    Future.sequence(sources.map { ms =>
      Future {
        val xhtml = toXHTML(knockoff(ms.source.mkString))
        val file = Generator.htmlExt(new File(targetDir, ms.relativeName.toString)) // todo - creating file just to get name and create new file. fixit.
        write(xhtml, file)
      }
    })
  }

  private def write(node: Node, file: File): File = {
    file.getParentFile.mkdirs
    val pw = new java.io.PrintWriter(file)
    try pw.write(Generator.printer.format(node)) finally pw.close()
    file
  }

}

object Generator {

  private val allowedExtensions = Set(".md", ".markdown")
  private val printer = new scala.xml.PrettyPrinter(140, 2)

  def sources(dir: File): Set[MarkupSource] = {
    def loop(f: File): Set[MarkupSource] = {
      if (f.isDirectory) f.listFiles.toSet.flatMap(loop)
      else if (f.isFile && allowedExtensions.contains(extension(f)))
        Set(MarkupSource(Source.fromFile(f), dir.toPath.relativize(f.toPath)))
      else Set.empty
    }
    if (dir.isDirectory) loop(dir) else Set.empty
  }

  def htmlExt(f: File): File = new File(f.getAbsolutePath.replaceFirst("\\..+$", ".html")) // todo - spec

  def frontMatterAndContent(source: Source) = {
    val lines = source.getLines().toStream
    def loop(remaining: Stream[String], started: Boolean, matter: Map[String, String]): (Map[String, String], Stream[String]) = {
      (remaining, started) match {
        case (Stream.Empty, _) => (Map.empty, lines)
        case ("---" #:: xs, true) => (matter, remaining.tail)
        case ("---" #:: xs, false) => loop(xs, started = true, matter)
        case (x #:: xs, true) => x.split(":", 2) match {
          case Array(k, v) => loop(xs, started, matter + (k -> v))
          case _ => loop(xs, started, matter)
        }
        case _ => (matter, lines)
      }
    }
    loop(lines, started = false, Map.empty)
  }

  private def extension(f: File) = if (f.getName.contains(".")) f.getName.substring(f.getName.lastIndexOf(".")) else ""

}

case class MarkupSource(source: Source, relativeName: Path)
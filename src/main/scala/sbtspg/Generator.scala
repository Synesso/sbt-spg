package sbtspg

import java.io.File

import com.tristanhunt.knockoff.DefaultDiscounter._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps
import scala.xml.Node

class Generator(articlesDir: File, draftsDir: File, layoutsDir: File, targetDir: File) {

  def generate: Set[File] = Await.result(transformedFiles, 1 minute)

  private lazy val sources = Generator.sources(articlesDir)

  private lazy val transformedFiles: Future[Set[File]] = {
    Future.sequence(sources.map { f =>
      Future {
        val xhtml = toXHTML(knockoff(Source.fromFile(f).mkString))
        val file = Generator.targetFor(articlesDir, f, targetDir)
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

  def sources(f: File): Set[File] =
    if (f.isDirectory) f.listFiles.toSet.flatMap(sources)
    else if (f.isFile && allowedExtensions.contains(extension(f))) Set(f)
    else Set.empty

  def targetFor(sourceDir: File, source: File, targetDir: File): File = {
    val relName = sourceDir.toPath.relativize(source.toPath).toString
    val name = relName.replaceFirst("\\..+$", ".html")
    new File(targetDir, name)
  }

  private def extension(f: File) = if (f.getName.contains(".")) f.getName.substring(f.getName.lastIndexOf(".")) else ""

}

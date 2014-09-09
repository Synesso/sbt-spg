package sbtspg

import java.io.{FileWriter, FilenameFilter, File}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.tristanhunt.knockoff.DefaultDiscounter._
import com.tristanhunt.knockoff._

import scala.io.Source
import scala.xml.{XML, Node}

object Generator {

  def sources(f: File): Set[File] =
    if (f.isDirectory) f.listFiles.toSet.flatMap(sources)
    else if (f.isFile && allowedExtensions.contains(extension(f))) Set(f)
    else Set.empty

  def transform(sources: Set[File], target: File): Future[Set[File]] = {
    Future.sequence(sources.map { f =>
      Future {
        // todo - does knockoff actually support OL?
        val xhtml = toXHTML(knockoff(Source.fromFile(f).getLines().mkString))
        val file = new File(target, f.getName.substring(0, f.getName.lastIndexOf('.')) + ".html")
        write(xhtml, file)
      }
    })
  }

  private val printer = new scala.xml.PrettyPrinter(140, 2)

  private def write(node: Node, file: File): File = {
    val pw = new java.io.PrintWriter(file)
    try pw.write(printer.format(node)) finally pw.close()
    file
  }




  def template: File = ???

  private def extension(f: File) = if (f.getName.contains(".")) f.getName.substring(f.getName.lastIndexOf(".")) else ""
  private val allowedExtensions = Set(".md", ".markdown")

}

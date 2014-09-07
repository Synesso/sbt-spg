package sbtspg

import java.io.{FilenameFilter, File}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Generator {

  def sources(f: File): Set[File] =
    if (f.isDirectory) f.listFiles.toSet.flatMap(sources)
    else if (f.isFile && allowedExtensions.contains(extension(f))) {
      println(f.getAbsolutePath)
      Set(f)
    }
    else Set.empty

  def template: File = ???

  private def extension(f: File) = if (f.getName.contains(".")) f.getName.substring(f.getName.lastIndexOf(".")) else ""
  private val allowedExtensions = Set(".md", ".markdown")

}

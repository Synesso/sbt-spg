package sbtspg

import java.io.File

import org.specs2.Specification

import scala.concurrent.Future

class GeneratorSpec extends Specification { def is = s2"""

  The sources method should
    find nothing when the directory does not exist $sources1
    find only & all *.md or *.markdown files $sources2
    should recursively find files $sources3
    find nothing when the directory is empty $sources4
    find the file when given a file that matches $sources5
    find nothing when given a file that doesn't match $sources6

  The targetFor method should
    mirror a file in the base directory and replace the extension with .html $targetFor1
    mirror a file in a subdirectory and replace the extension with .html $targetFor2

"""

  import Generator._

  def sources1 = sources(file("invalid")) must beEqualTo(Set.empty[File])

  def sources2 = sources(file("sources2")) must beEqualTo(Set(
    file("sources2/first.md"), file("sources2/second.markdown")
  ))

  def sources3 = sources(file("sources3")) must beEqualTo(Set(
    file("sources3/this.md"), file("sources3/more/another.md")
  ))

  def sources4 = sources(file("sources4")) must beEmpty

  def sources5 = sources(file("sources2/first.md")) must beEqualTo(Set(file("sources2/first.md")))

  def sources6 = sources(file("sources2/noextension")) must beEmpty

  def targetFor1 = targetFor(new File("sourceBase"), new File("sourceBase/first.md"), new File("target")) must
    beEqualTo(new File("target/first.html"))

  def targetFor2 = targetFor(new File("sourceBase"), new File("sourceBase/subdir/thing.md"), new File("target")) must
    beEqualTo(new File("target/subdir/thing.html"))

  private def file(name: String) = new File("src/test/resources/generator", name)

}

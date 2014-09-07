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

"""

  import Generator._

  def sources1 = sources(new File("src/test/resources/generator/invalid")) must beEqualTo(Set.empty[File])

  def sources2 = sources(new File("src/test/resources/generator/sources2")) must beEqualTo(Set(
    file("sources2/first.md"), file("sources2/second.markdown")
  ))

  def sources3 = sources(new File("src/test/resources/generator/sources3")) must beEqualTo(Set(
    file("sources3/this.md"), file("sources3/more/another.md")
  ))

  def sources4 = sources(new File("src/test/resources/generator/sources4")) must beEmpty

  def sources5 = sources(new File("src/test/resources/generator/sources2/first.md")) must beEqualTo(Set(
    file("sources2/first.md")
  ))

  def sources6 = sources(new File("src/test/resources/generator/sources2/noextension")) must beEmpty

  private def file(name: String) = new File("src/test/resources/generator", name)

}

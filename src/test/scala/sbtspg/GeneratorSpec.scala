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

  The frontMatter method should
    resolve to an empty map for empty content $frontMatter01
    resolve to an empty map for empty frontmatter $frontMatter02
    resolve to an empty map for no frontmatter section $frontMatter03
    resolve to an empty map when frontmatter section is preceded by anything $frontMatter04
    resolve to an empty map when frontmatter section has no closing dashes $frontMatter05
    interpret empty lines as nothing $frontMatter06
    interpret lines with no colon as nothing $frontMatter07
    interpret lines with one colon as a kvp $frontMatter08
    interpret lines with two or more colons as a kvp, where the value has all trailing colons $frontMatter09
    give precedence to later properties when duplicated $frontMatter10

"""

  import Generator._

  def frontMatter01 = frontMatter(Seq.empty[String]) must beEqualTo(Map.empty[String, String])
  def frontMatter02 = frontMatter(Seq("---","---","content")) must beEqualTo(Map.empty[String, String])
  def frontMatter03 = frontMatter(Seq("content")) must beEqualTo(Map.empty[String, String])
  def frontMatter04 = frontMatter(Seq("", "---", "key:value", "---")) must beEqualTo(Map.empty[String, String])
  def frontMatter05 = frontMatter(Seq("---", "key:value")) must beEqualTo(Map.empty[String, String])
  def frontMatter06 = frontMatter(Seq("---", "\t", "---")) must beEqualTo(Map.empty[String, String])
  def frontMatter07 = frontMatter(Seq("---", "key", "---")) must beEqualTo(Map.empty[String, String])
  def frontMatter08 = frontMatter(Seq("---", "key:value", "key2:value", "---")) must beEqualTo(
    Map("key" -> "value", "key2" -> "value")
  )
  def frontMatter09 = frontMatter(Seq("---", "key:value:pair", "---")) must beEqualTo(Map("key" -> "value:pair"))
  def frontMatter10 = frontMatter(Seq("---", "key:value1", "key:value2", "---")) must beEqualTo(Map("key" -> "value2"))


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

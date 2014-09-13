package sbtspg

import java.io.File

import org.specs2.Specification

import scala.io.Source

class GeneratorSpec extends Specification { def is = s2"""

  The sources method should
    find nothing when the directory does not exist $sources1
    find only & all *.md or *.markdown files $sources2
    should recursively find files $sources3
    find nothing when the directory is empty $sources4
    find nothing when given a file that matches $sources5
    find nothing when given a file that doesn't match $sources6

  The frontMatterAndContent method should
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

  import sbtspg.Generator._

  /* sources method */

  def sources1 = sources(file("invalid")) must beEqualTo(Set.empty)
  def sources2 = parsed(sources(file("sources2"))) must beEqualTo(Set(
    parsed("sources2", "first.md"), parsed("sources2", "second.markdown")
  ))
  def sources3 = parsed(sources(file("sources3"))) must beEqualTo(Set(
    parsed("sources3", "this.md"), parsed("sources3", "more/another.md")
  ))
  def sources4 = sources(file("sources4")) must beEmpty
  def sources5 = sources(file("sources2/first.md")) must beEmpty
  def sources6 = sources(file("sources2/noextension")) must beEmpty

  private def parsed(ms: Set[MarkupSource]) = ms.map { case MarkupSource(s, p) => (s.mkString, p) }
  private def parsed(dir: String, name: String) = {
    val d = file(dir)
    val f = file(s"$dir/$name")
    (Source.fromFile(f).mkString, d.toPath.relativize(f.toPath))
  }

  /* htmlExt method */

  // todo

  /* frontMatterAndContent method */

  val noMatter = Map.empty[String, String]
  val noMarkdown = Stream.empty[String]
  val noNothing = (noMatter, noMarkdown)

  def frontMatter01 = frontMatterAndContent(source()) must beEqualTo(noNothing)
  def frontMatter02 = frontMatterAndContent(source("---","---","content")) must beEqualTo(noMatter, Stream("content"))
  def frontMatter03 = frontMatterAndContent(source("content")) must beEqualTo(noMatter, Stream("content"))
  def frontMatter04 = frontMatterAndContent(source("", "---", "key:value", "---")) must
    beEqualTo(noMatter, Stream("", "---", "key:value", "---"))
  def frontMatter05 = frontMatterAndContent(source("---", "key:value")) must
    beEqualTo(noMatter, Stream("---", "key:value"))
  def frontMatter06 = frontMatterAndContent(source("---", "\t", "---")) must beEqualTo(noNothing)
  def frontMatter07 = frontMatterAndContent(source("---", "key", "---")) must beEqualTo(noNothing)
  def frontMatter08 = frontMatterAndContent(source("---", "key:value", "key2:value", "---")) must beEqualTo(
    Map("key" -> "value", "key2" -> "value"), noMarkdown
  )
  def frontMatter09 = frontMatterAndContent(source("---", "key:value:pair", "---")) must
    beEqualTo(Map("key" -> "value:pair"), noMarkdown)
  def frontMatter10 = frontMatterAndContent(source("---", "key:value1", "key:value2", "---")) must
    beEqualTo(Map("key" -> "value2"), noMarkdown)

  private def file(name: String) = new File("src/test/resources/generator", name)
  private def source(s: String*) = Source.fromString(s.mkString("\n"))

}

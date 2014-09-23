package sbtspg

import java.nio.file.Path

import org.specs2.{ScalaCheck, Specification}

import scala.io.Source

class MarkupSourceSpec extends Specification with ScalaCheck with ArbitraryInput { def is = s2"""

  On splitting to MarkupWithMeta
   it must contain all frontMatter $allFrontMatter
   it must contain a stream of the remaining source $remainingSource
   it must have exactly the same path $samePath

"""

  def allFrontMatter = (arbFrontMatter, arbSourceString, arbPath){(fm: Map[String, String], content: String, path: Path) =>
    markupWithMeta(fm, content, path).meta must beEqualTo(fm)
  }

  def remainingSource = (arbFrontMatter, arbSourceString, arbPath){(fm: Map[String, String], content: String, path: Path) =>
    val actual = markupWithMeta(fm, content, path).stream.mkString("\n").trim
    actual must beEqualTo(content.trim)
  }

  def samePath = (arbFrontMatter, arbSourceString, arbPath){(fm: Map[String, String], content: String, path: Path) =>
    markupWithMeta(fm, content, path).relativeName must beEqualTo(path)
  }

  private def markupWithMeta(fm: Map[String, String], content: String, path: Path) = {
    val sourceString = s"---\n${fm.map{case (k,v) => s"$k:$v"}.mkString("\n")}\n---\n$content"
    val fullSource = Source.fromString(sourceString)
    MarkupSource(fullSource, path).parseFrontMatter
  }

  /*  val noMatter = Map.empty[String, String]
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

    private def source(s: String*) = Source.fromString(s.mkString("\n"))
    */

}

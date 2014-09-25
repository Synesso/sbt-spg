package sbtspg

import java.nio.file.Path

import org.specs2.{ScalaCheck, Specification}

import scala.io.Source

class MarkupSourceSpec extends Specification with ScalaCheck with ArbitraryInput { def is = s2"""

  On splitting to MarkupWithMeta
   it must contain all frontMatter $allFrontMatter
   it must contain a stream of the remaining source $remainingSource
   it must have exactly the same path $samePath
   it must ignore whitespace in frontMatter $ignoreWhitespaceFM
   it must ignore no value entries in frontMatter $ignoreNoValueFM
   it must have no frontMatter when first line is not --- $firstLineNotDashes
   it must have no frontMatter when trailing --- is not present $noTrailingDashes
   it must allow colons in the values for frontMatter $colonsInValues
   it must overwrite older values with newer values on frontMatter key clash $overwriteNewerValues

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

  def ignoreWhitespaceFM = parseFrontMatter("---\n\t\none:two\n---\nend") must beEqualTo(Map("one" -> "two"))

  def ignoreNoValueFM = parseFrontMatter("---\nnovalue\n---") must beEmpty

  def firstLineNotDashes = parseFrontMatter("\n---\nkey:value\n---\ncontent") must beEmpty

  def noTrailingDashes = parseFrontMatter("---\nkey:value\ncontent") must beEmpty

  def colonsInValues = parseFrontMatter("---\nkey:and:value\n---\n") must beEqualTo(Map("key" -> "and:value"))

  def overwriteNewerValues = parseFrontMatter("---\nkey:val1\nkey:val2\n---\n") must beEqualTo(Map("key" -> "val2"))

  private def markupWithMeta(fm: Map[String, String], content: String, path: Path) = {
    val sourceString = s"---\n${fm.map{case (k,v) => s"$k:$v"}.mkString("\n")}\n---\n$content"
    val fullSource = Source.fromString(sourceString)
    MarkupSource(fullSource, path).parseFrontMatter
  }

  private def parseFrontMatter(s: String) = MarkupSource(Source.fromString(s), null).parseFrontMatter.meta
}

package sbtspg

import java.nio.file.Path

import com.typesafe.config.{ConfigFactory, ConfigRenderOptions, Config}
import org.specs2.{ScalaCheck, Specification}

import scala.io.Source
import scala.util.Try

class MarkupSourceSpec extends Specification with ScalaCheck with ArbitraryInput { def is = s2"""

  // todo - splits with HOCON

  On splitting to MarkupWithMeta it must
    contain all frontMatter $allFrontMatter
    contain a stream of the remaining source $remainingSource
    have exactly the same path $samePath
    ignore whitespace in frontMatter $ignoreWhitespaceFM
    ignore no value entries in frontMatter $ignoreNoValueFM
    have no frontMatter when first line is not --- $firstLineNotDashes
    have no frontMatter when trailing --- is not present $noTrailingDashes
    allow colons in the values for frontMatter $colonsInValues
    overwrite older values with newer values on frontMatter key clash $overwriteNewerValues

    return failure when frontMatter is invalid HOCON $withInvalidConfig
    return success with Config when frontMatter is valid HOCON $withValidConfig
    return success with empty Config when frontMatter is not present $withNoConfig

"""

  def withInvalidConfig = (notEmpty(arbSourceString), arbPath){(invalidHocon: String, path: Path) =>
    val src = Source.fromString(s"---\n$invalidHocon\n---\n")
    Try { MarkupSource(src, path).parseMatter } must beFailedTry
  }

  def withValidConfig = (arbConfig, arbSourceString, arbPath) { (conf: Config, content: String, path: Path) =>
    val src = Source.fromString(s"---\n${conf.root.render}\n---\n$content")
    MarkupSource(src, path).parseMatter.conf must beEqualTo(conf)
  }

  def withNoConfig = (arbSourceString, arbPath) {(content: String, path: Path) =>
    MarkupSource(Source.fromString(content), path).parseMatter.conf must beEqualTo(ConfigFactory.empty)
  }

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

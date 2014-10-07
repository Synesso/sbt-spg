package sbtspg

import java.nio.file.Path

import com.typesafe.config.{Config, ConfigFactory}
import org.specs2.{ScalaCheck, Specification}

import scala.io.Source
import scala.util.Try

class MarkupSourceSpec extends Specification with ScalaCheck with ArbitraryInput { def is = s2"""

  // todo - splits with HOCON

  On parsing matter it must return
    failure when matter is invalid HOCON $withInvalidConfig
    success with Config when matter is valid HOCON $withValidConfig
    success with empty Config when matter is not present $withEmptyConfig
    a stream of the remaining source $remainingSource
    exactly the same path $samePath
    success with empty Config when first line is not --- $firstLineNotDashes
    success with empty Config when trailing --- is not present $noTrailingDashes

"""

  def withInvalidConfig = (notEmpty(arbSourceString), arbPath){(invalidHocon: String, path: Path) =>
    val src = Source.fromString(s"---\n$invalidHocon\n---\n")
    Try { MarkupSource(src, path).parseMatter } must beFailedTry
  }

  def withValidConfig = (arbConfig, arbSourceString, arbPath) { (conf: Config, content: String, path: Path) =>
    MarkupSource(source(conf, content), path).parseMatter.conf must beEqualTo(conf)
  }

  def withEmptyConfig = (arbSourceString, arbPath) {(content: String, path: Path) =>
    MarkupSource(Source.fromString(content), path).parseMatter.conf must beEqualTo(ConfigFactory.empty)
  }

  def remainingSource = (arbConfig, arbSourceString, arbPath){(conf: Config, content: String, path: Path) =>
    val actual = MarkupSource(source(conf, content), path).parseMatter.stream.mkString("\n").trim
    actual must beEqualTo(content.trim)
  }

  def samePath = (arbConfig, arbSourceString, arbPath){(conf: Config, content: String, path: Path) =>
    MarkupSource(source(conf, content), path).relativeName must beEqualTo(path)
  }

  def firstLineNotDashes = (arbId, arbConfig, arbSourceString, arbPath) {
      (firstLine: String, conf: Config, content: String, path: Path) =>

    val src = Source.fromString(s"$firstLine\n---\n${conf.root.render}\n---\n$content")
    val mwc = MarkupSource(src, path).parseMatter
    (mwc.conf must beEqualTo(ConfigFactory.empty)) and
      (mwc.stream.headOption must beSome(firstLine))
  }

  def noTrailingDashes = (arbConfig, arbSourceString, arbPath) { (conf: Config, content: String, path: Path) =>
    val src = Source.fromString(s"---\n${conf.root.render}\n$content\n")
    val mwc = MarkupSource(src, path).parseMatter
    (mwc.conf must beEqualTo(ConfigFactory.empty)) and
      (mwc.stream.headOption must beSome("---"))
  }

  private def source(conf: Config, content: String) = Source.fromString(s"---\n${conf.root.render}\n---\n$content")
}

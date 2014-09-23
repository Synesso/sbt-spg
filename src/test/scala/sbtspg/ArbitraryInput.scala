package sbtspg

import java.io.File

import org.scalacheck.{Arbitrary, Gen}

import scala.io.Source

trait ArbitraryInput {

  implicit def arbSourceString = Arbitrary {
    for {
      i: Int <- Gen.choose(1, 100)
      ss: List[String] <- Gen.listOfN(i, Gen.alphaStr)
    } yield ss.mkString("\n")
  }

  implicit def arbPath = Arbitrary {
    for {
      folders: Int <- Gen.choose(1, 12)
      names: List[String] <- Gen.listOfN(folders, Gen.identifier)
    } yield path(names.mkString("/"))
  }

  implicit def arbFrontMatter = Arbitrary {
    for {
      entries: Int <- Gen.choose(0, 30)
      keys: List[String] <- Gen.listOfN(entries, Gen.identifier)
      values: List[String] <- Gen.listOfN(entries, Gen.identifier)
    } yield keys.zip(values).toMap
  }

  implicit def arbMarkupSource = Arbitrary {
    for {
      fm <- arbFrontMatter.arbitrary
      src <- arbSourceString.arbitrary
      path <- arbPath.arbitrary
    } yield {
      val sourceString = s"---\n${fm.map{case (k,v) => s"$k:$v"}.mkString("\n")}\n---\n}$src"
      val fullSource = Source.fromString(sourceString)
      MarkupSource(fullSource, path)
    }
  }

  private def path(s: String) = new File(".").toPath.relativize(new File(s"./$s").toPath)


}

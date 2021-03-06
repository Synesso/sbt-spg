package sbtspg

import java.io.File

import com.typesafe.config.{ConfigFactory, Config}
import org.scalacheck.{Arbitrary, Gen}

import scala.io.Source
import scala.collection.JavaConversions._

trait ArbitraryInput {

  def arbSourceString = Arbitrary {
    for {
      i: Int <- Gen.choose(1, 100)
      ss: List[String] <- Gen.listOfN(i, Gen.alphaStr)
    } yield ss.mkString("\n")
  }

  def notEmpty(arbStr: Arbitrary[String]) = Arbitrary(arbStr.arbitrary.filter(!_.trim.isEmpty))

  def arbPath = Arbitrary {
    for {
      folders: Int <- Gen.choose(1, 12)
      names: List[String] <- Gen.listOfN(folders, Gen.identifier)
    } yield path(names.mkString("/"))
  }

  def arbMarkupSource = Arbitrary {
    for {
      entries: Int <- Gen.choose(0, 30)
      keys: List[String] <- Gen.listOfN(entries, Gen.identifier)
      values: List[String] <- Gen.listOfN(entries, Gen.identifier)
      src <- arbSourceString.arbitrary
      path <- arbPath.arbitrary
    } yield {
      val fm = keys zip values
      val sourceString = s"---\n${fm.map { case (k, v) => s"$k:$v"}.mkString("\n")}\n---\n}$src"
      val fullSource = Source.fromString(sourceString)
      MarkupSource(fullSource, path)
    }
  }

  def arbConfig: Arbitrary[Config] = Arbitrary{
    for {
      entries: Int <- Gen.choose(0, 30)
      keys: List[String] <- Gen.listOfN(entries, Gen.identifier)
      values: List[String] <- Gen.listOfN(entries, Gen.identifier)
    } yield ConfigFactory.parseMap(keys.zip(values).toMap[String, String])
  }

  def arbId: Arbitrary[String] = Arbitrary(Gen.identifier)
  def arbIdSet: Arbitrary[Set[String]] = Arbitrary(Gen.containerOf(Gen.identifier).map(_.toSet))
  def arbOptIdSet: Arbitrary[Option[Set[String]]] = Arbitrary(Gen.option(Gen.containerOf(Gen.identifier).map(_.toSet)))

  private def path(s: String) = new File(".").toPath.relativize(new File(s"./$s").toPath)


}

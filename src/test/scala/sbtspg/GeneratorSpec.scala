package sbtspg

import java.io.File
import java.nio.file.Path

import org.specs2.matcher.ContentMatchers
import org.specs2.{ScalaCheck, Specification}

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.io.Source

class GeneratorSpec extends Specification with ScalaCheck with ArbitraryInput with ContentMatchers { def is = s2"""

  The sources method should
    find nothing when the directory does not exist $sources1
    find only & all *.md or *.markdown files $sources2
    should recursively find files $sources3
    find nothing when the directory is empty $sources4
    find nothing when given a file that matches $sources5
    find nothing when given a file that doesn't match $sources6

  The replaceExtensionWithHtml method should return a string
    where a file with extension is changed to html $replaceFileExt
    where a path & file with extension is changed to html $replacePathExt
    where a file with no extension is unchanged $replaceFileNoExt
    where a path & file with no extension is unchanged $replacePathNoExt
    where a path including a dot & file with no extension is unchanged $replacePathWithDotNoExt
    where a path including a dot & file with extension changes the extension only $replacePathWithDotAndExt
    where a file including a dot with extension changes the extension only $replaceFileWithDotAndExt
    where a path including a dot & file including a dot with extension changes the extension only $replacePathAndFileWithDotAndExt

  The generator should generate a site smoke $pending

"""

  import sbtspg.Generator._

  def replaceFileExt = (arbId, arbId){(fn, ext) => replaceExtensionWithHtml(path(s"$fn.$ext")) must beEqualTo(s"$fn.html")}
  def replacePathExt = (arbPath, arbId){(p, ext) => replaceExtensionWithHtml(path(s"$p.$ext")) must beEqualTo(s"$p.html")}
  def replaceFileNoExt = arbId{fn => replaceExtensionWithHtml(path(fn)) must beEqualTo(fn)}
  def replacePathNoExt = arbPath{p => replaceExtensionWithHtml(p) must beEqualTo(p.toString)}
  def replacePathWithDotNoExt = (arbPath, arbPath){(p1, p2) => replaceExtensionWithHtml(path(s"$p1.$p2/$p1")) must
    beEqualTo(s"$p1.$p2/$p1")}
  def replacePathWithDotAndExt = (arbPath, arbPath, arbId){(p1, p2, ext) =>
    replaceExtensionWithHtml(path(s"$p1.$p2/$p1.$ext")) must beEqualTo(s"$p1.$p2/$p1.html")}
  def replaceFileWithDotAndExt = (arbId, arbId, arbId){(fn1, fn2, ext) =>
    replaceExtensionWithHtml(path(s"$fn1.$fn2.$ext")) must beEqualTo(s"$fn1.$fn2.html")}
  def replacePathAndFileWithDotAndExt = (arbPath, arbPath, arbId, arbId, arbId){(p1, p2, fn1, fn2, ext) =>
    val input = s"$p1.$p2/$p1/$fn1.$fn2"
    replaceExtensionWithHtml(path(s"$input.$ext")) must beEqualTo(s"$input.html")}

  def sources1 = sources(file("invalid")) must beEmpty[Set[MarkupSource]].await 
  def sources2 = parsed(sources(file("sources2"))) must beEqualTo(Set(
    parsed("sources2", "first.md"), parsed("sources2", "second.markdown")
    )).await
  def sources3 = parsed(sources(file("sources3"))) must beEqualTo(Set(
      parsed("sources3", "this.md"), parsed("sources3", "more/another.md")
    )).await
  def sources4 = sources(file("sources4")) must beEmpty[Set[MarkupSource]].await
  def sources5 = sources(file("sources2/first.md")) must beEmpty[Set[MarkupSource]].await
  def sources6 = sources(file("sources2/noextension")) must beEmpty[Set[MarkupSource]].await

  def smoke = {
    val target = new File("target/generator-smoke-test")
    val articles = new File("src/test/resources/generator/smoke")
    val expected = new File("src/test/resources/generator/smoke-expected")
    target.delete()

    val files = new Generator(articles, draftsDir = null, layoutsDir = null, target).generate

    val actualFiles = filesFrom(target)
    val expectedFiles = filesFrom(expected)
    val expectedPaths = pathsFrom(expected)

    val result = files.toSeq.map(_.toPath).map(target.toPath.relativize) must beEqualTo(expectedPaths)
    val sideEffect = actualFiles.zip(expectedFiles).map(_ must haveSameMD5).reduceLeft(_ and _)

    result and sideEffect
  }

  private def filesFrom(f: File): Seq[File] =
    if (f.isFile) f :: Nil
    else f.listFiles.flatMap(filesFrom)

  private def pathsFrom(dir: File): Seq[Path] =
    filesFrom(dir).sortBy(_.getName).map(_.toPath).map(dir.toPath.relativize)

  private def parsed(fms: Future[Set[MarkupSource]]) = fms.map{_.map { case MarkupSource(s, p) => (s.mkString, p) }}
  private def parsed(dir: String, name: String) = {
    val d = file(dir)
    val f = file(s"$dir/$name")
    (Source.fromFile(f).mkString, d.toPath.relativize(f.toPath))
  }

  private def file(name: String) = new File("src/test/resources/generator", name)
  private def path(s: String) = new File(".").toPath.relativize(new File(s"./$s").toPath)
}

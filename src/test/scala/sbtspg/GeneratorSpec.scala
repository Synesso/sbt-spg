package sbtspg

import java.io.File

import org.specs2.{ScalaCheck, Specification}

import scala.concurrent.Future
import scala.io.Source

class GeneratorSpec extends Specification with ScalaCheck with ArbitraryInput { def is = s2"""

  The sources method should
    find nothing when the directory does not exist _dollar_sources1
    find only & all *.md or *.markdown files _dollar_sources2
    should recursively find files _dollar_sources3
    find nothing when the directory is empty _dollar_sources4
    find nothing when given a file that matches _dollar_sources5
    find nothing when given a file that doesn't match _dollar_sources6

  The frontMatterAndContent method should
    resolve to an empty map for empty content _dollar_frontMatter01
    resolve to an empty map for empty frontmatter _dollar_frontMatter02
    resolve to an empty map for no frontmatter section _dollar_frontMatter03
    resolve to an empty map when frontmatter section is preceded by anything _dollar_frontMatter04
    resolve to an empty map when frontmatter section has no closing dashes _dollar_frontMatter05
    interpret empty lines as nothing _dollar_frontMatter06
    interpret lines with no colon as nothing _dollar_frontMatter07
    interpret lines with one colon as a kvp _dollar_frontMatter08
    interpret lines with two or more colons as a kvp, where the value has all trailing colons _dollar_frontMatter09
    give precedence to later properties when duplicated _dollar_frontMatter10

  The replaceExtensionWithHtml method should return a string
    where a file with extension is changed to html $replaceFileExt
    where a path & file with extension is changed to html $replacePathExt
    where a file with no extension is unchanged $replaceFileNoExt
    where a path & file with no extension is unchanged $replacePathNoExt
    where a path including a dot & file with no extension is unchanged $replacePathWithDotNoExt
    where a path including a dot & file with extension changes the extension only $replacePathWithDotAndExt
    where a file including a dot with extension changes the extension only $replaceFileWithDotAndExt
    where a path including a dot & file including a dot with extension changes the extension only $replacePathAndFileWithDotAndExt

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


  /* sources method */

/*
  def sources1 = sources(file("invalid")) must beEmpty.await
  def sources2 = parsed(sources(file("sources2"))) must beEqualTo(Set(
    parsed("sources2", "first.md"), parsed("sources2", "second.markdown")
  )).await
  def sources3 = parsed(sources(file("sources3"))) must beEqualTo(Set(
    parsed("sources3", "this.md"), parsed("sources3", "more/another.md")
  )).await
  def sources4 = sources(file("sources4")) must beEmpty.await
  def sources5 = sources(file("sources2/first.md")) must beEmpty.await
  def sources6 = sources(file("sources2/noextension")) must beEmpty.await

  private def parsed(fms: Future[Set[MarkupSource]]) = fms.map{_.map { case MarkupSource(s, p) => (s.mkString, p) }}
  private def parsed(dir: String, name: String) = {
    val d = file(dir)
    val f = file(s"$dir/$name")
    (Source.fromFile(f).mkString, d.toPath.relativize(f.toPath))
  }
*/

  /* helpers */

  private def file(name: String) = new File("src/test/resources/generator", name)
  private def source(s: String*) = Source.fromString(s.mkString("\n"))
  private def path(s: String) = new File(".").toPath.relativize(new File(s"./$s").toPath)

}

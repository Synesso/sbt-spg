package sbtspg

import sbt.Keys._
import sbt._

object SPGPlugin extends AutoPlugin {

  object autoImport {

    import java.io.File

    val siteSource = settingKey[File]("Base directory for the site files")
    val siteArticles = settingKey[File]("Articles folder")
    val siteDrafts = settingKey[File]("Draft articles folder")
    val siteLayouts = settingKey[File]("Layouts folder")
    val spgGenerate = taskKey[Set[File]]("Generates static pages from markdown files")

    lazy val baseSpgGenerateSettings: Seq[sbt.Def.Setting[_]] = Seq(
      siteSource := new File("src/main/site"),
      siteArticles := new File(siteSource.value, "_articles"),
      siteDrafts := new File(siteSource.value, "_drafts"),
      siteLayouts := new File(siteSource.value, "_layouts"),
      spgGenerate := new Generator(siteArticles.value, siteDrafts.value, siteLayouts.value, target.value).generate
    )
  }

  import sbtspg.SPGPlugin.autoImport._

  override val projectSettings = baseSpgGenerateSettings

  override val trigger = allRequirements

}

// "I could murder a curry!" - SPG

package sbtspg

import sbt._
import Keys._

object SPGPlugin extends AutoPlugin {

  object autoImport {

    val spgGenerate = taskKey[Set[File]]("Generates static pages from markdown files")

    lazy val baseSpgGenerateSettings: Seq[sbt.Def.Setting[_]] = Seq(
      spgGenerate := Generator.sources(new File("src/test/resources/generator"))
    )

  }

  import autoImport._

  override val projectSettings = inConfig(Compile)(baseSpgGenerateSettings)

  override val trigger = allRequirements

}

// "I could murder a curry!" - SPG

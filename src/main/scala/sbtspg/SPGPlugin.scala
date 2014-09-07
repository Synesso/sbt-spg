package sbtspg

import sbt._
import Keys._

object SPGPlugin extends AutoPlugin {

  override lazy val projectSettings = Seq(commands += siteCommand)

  lazy val siteCommand = Command.command("site") {state: State =>
    println("this is the thing~!")
    state
  }
}

// "I could murder a curry!" - SPG

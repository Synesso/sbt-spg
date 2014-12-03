scalaVersion := "2.10.3"

version := "0.1.0-SNAPSHOT"

name := "sbt-spg"

organization := "com.github.synesso"

scalacOptions ++= Seq("-feature")

scalacOptions in Test ++= Seq("-feature", "-Yrangepos")

sbtPlugin := true

resolvers += "Twitter Maven" at "http://maven.twttr.com/"

libraryDependencies ++= Seq(
  "com.tristanhunt" %% "knockoff" % "0.8.2", // markdown
  "com.twitter" %% "util-eval" % "6.5.0", // eval
  "com.typesafe" % "config" % "1.2.1", // hocon
  "org.specs2" %% "specs2-core" % "3.0-M0" % "test",
  "org.specs2" %% "specs2-scalacheck" % "3.0-M0" % "test",
  "org.specs2" %% "specs2-matcher-extra" % "3.0-M0" % "test"
)


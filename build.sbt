scalaVersion := "2.10.3"

version := "0.1.0-SNAPSHOT"

name := "sbt-spg"

organization := "com.github.synesso"

scalacOptions ++= Seq("-feature")

sbtPlugin := true

resolvers += "Twitter Maven" at "http://maven.twttr.com/"

libraryDependencies ++= Seq(
  "com.tristanhunt" %% "knockoff" % "0.8.2", // markdown
  "com.twitter" %% "util-eval" % "6.5.0", // eval
  "com.typesafe" % "config" % "1.2.1", // hocon
  "org.specs2" %% "specs2" % "2.4.2" % "test"
)


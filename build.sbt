scalaVersion := "2.10.3"

version := "0.1.0-SNAPSHOT"

name := "sbt-spg"

organization := "com.github.synesso"

scalacOptions ++= Seq("-feature")

sbtPlugin := true

libraryDependencies ++= Seq(
  "com.tristanhunt" %% "knockoff" % "0.8.2",
  "org.specs2" %% "specs2" % "2.4.2" % "test"
)


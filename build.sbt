scalaVersion := "2.10.3"

version := "0.1.0"

name := "sbt-spg"

organization := "com.github.synesso"

scalacOptions ++= Seq("-feature")

sbtPlugin := true

libraryDependencies ++= Seq("org.pegdown" % "pegdown" % "1.4.2")



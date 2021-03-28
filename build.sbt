name := """monetary-accounts"""
organization := "com.ramomar"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.1" % Test
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"
libraryDependencies += ("org.scala-stm" %% "scala-stm" % "0.8")
libraryDependencies += "joda-time" % "joda-time" % "2.10.1"
libraryDependencies += "com.typesafe.play" %% "play-json-joda" % "2.7.1"

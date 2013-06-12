name := "lixp"

organization := "ro.igstan"

version := "0.1.0"

scalaVersion := "2.10.2"

scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked")

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

net.virtualvoid.sbt.graph.Plugin.graphSettings

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.9.1" % "test"
)
name := "lixp"

organization := "ro.igstan"

version := "0.1.0"

scalaVersion := "2.10.2"

scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked")

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

sourceDirectory in Compile <<= baseDirectory(_ / "src")

sourceDirectory in Test <<= baseDirectory(_ / "test")

net.virtualvoid.sbt.graph.Plugin.graphSettings

sbtassembly.Plugin.assemblySettings

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.9.1" % "test"
)

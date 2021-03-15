scalaVersion := "2.12.13"
githubOwner := "zero-deps"
githubRepository := "sbt-git"
organization := "io.github.zero-deps"
version := zero.git.version()
scalacOptions ++= Vector(
  "-feature",
  "-deprecation",
  "-Ywarn-unused:imports",
)
isSnapshot := true // override local artifacts

turbo := true
useCoursier := true
Global / onChangedBuildSource := ReloadOnSourceChanges

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "latest.integration"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0-SNAP13" % Test

enablePlugins(SbtPlugin)

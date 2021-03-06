scalaVersion := "2.12.14"
version := zero.git.version()
scalacOptions ++= Vector(
  "-feature",
  "-deprecation",
  "-Ywarn-unused:imports",
)

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "latest.integration"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0-SNAP13" % Test

enablePlugins(SbtPlugin)

/* publishing */
organization := "io.github.zero-deps"
homepage := Some(url("https://github.com/zero-deps/sbt-git"))
scmInfo := Some(ScmInfo(url("https://github.com/zero-deps/sbt-git"), "git@github.com:zero-deps/sbt-git.git"))
developers := List(Developer("Zero", "Deps", "zerodeps.org@gmail.com", url("https://github.com/zero-deps")))
licenses += ("MIT" -> url("http://opensource.org/licenses/MIT"))
publishMavenStyle := true
versionScheme := Some("pvp")
publishTo := Some(Opts.resolver.sonatypeStaging)
usePgpKeyHex("F68F0EADDB81EF533C4E8E3228C90422E5A0DB21")
/* local */
isSnapshot := true
/* publishing */

turbo := true
useCoursier := true
Global / onChangedBuildSource := ReloadOnSourceChanges
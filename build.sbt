scalaVersion := "2.12.20"
version := zero.git.version()
scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Ywarn-unused:imports",
)

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "7.0.0.202409031743-r"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test

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
Global / onChangedBuildSource := ReloadOnSourceChanges

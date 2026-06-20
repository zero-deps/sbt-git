version := zero.git.version()
scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Ywarn-unused:imports",
)

val zioVersion = "2.1.20"

libraryDependencies ++= Seq(
  "org.eclipse.jgit" % "org.eclipse.jgit" % "7.6.0.202603022253-r",
  "dev.zio" %% "zio-test" % zioVersion % Test,
  "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

enablePlugins(SbtPlugin)

/* publishing */
organization := "io.github.zero-deps"
homepage := Some(url("https://github.com/zero-deps/sbt-git"))
scmInfo := Some(ScmInfo(url("https://github.com/zero-deps/sbt-git"), "git@github.com:zero-deps/sbt-git.git"))
developers := List(Developer("Zero", "Deps", "zerodeps.org@gmail.com", url("https://github.com/zero-deps")))
licenses += ("MIT" -> url("http://opensource.org/licenses/MIT"))
publishMavenStyle := true
versionScheme := Some("pvp")
publishTo := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}
usePgpKeyHex("F68F0EADDB81EF533C4E8E3228C90422E5A0DB21")
/* local */
isSnapshot := true
/* publishing */

Global / onChangedBuildSource := ReloadOnSourceChanges

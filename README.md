# sbt-git

![Production Ready](https://img.shields.io/badge/Project%20Stage-Production%20Ready-brightgreen.svg)

Use last tag as a version and run git commands inside SBT shell.

```scala
// project/plugins.sbt:
addSbtPlugin("io.github.zero-deps" % "sbt-git" % "latest.integration")

// build.sbt:
version := zero.git.version()

// run git commands inside sbt shell
sbt> git status // can be shorted
sbt> git add [<pathspec>…]...
sbt> git commit -m <msg>
sbt> git help
```

## Publishing

This build publishes through SBT's built-in Central Portal support. Keep Central
Portal credentials outside the repository, then stage and upload signed artifacts:

```scala
sbt> publishSigned
sbt> sonaUpload
```

Use `sonaRelease` instead of `sonaUpload` to upload and release in one step.

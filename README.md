# sbt-git

![Production Ready](https://img.shields.io/badge/Project%20Stage-Production%20Ready-brightgreen.svg)

Use git-describe as a version and run git commands inside SBT shell.

```scala
// project/plugins.sbt:
addSbtPlugin("io.github.zero-deps" % "sbt-git" % "latest.integration")

// build.sbt:
version := zero.git.version() /* git-describe */

// run git commands inside sbt shell
sbt> git status // can be shorted
sbt> git add [<pathspec>…]...
sbt> git commit -m <msg>
sbt> git help
```

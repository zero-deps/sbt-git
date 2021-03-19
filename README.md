# sbt-git

![ci](https://github.com/zero-deps/ext/workflows/ci/badge.svg)

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

# Publishing

```
export GPG_TTY=$(tty)
sbt publishSigned
open https://oss.sonatype.org/#stagingRepositories
```

# sbt-git

![ci](https://github.com/zero-deps/ext/workflows/ci/badge.svg)

```scala
// project/plugins.sbt:
resolvers += Resolver.githubPackages("zero-deps")
addSbtPlugin("io.github.zero-deps" % "sbt-git" % "latest.integration")

// use git-describe as a version
// build.sbt:
version := zero.git.version()

// run git commands inside sbt shell
sbt> git status // can be shorted
sbt> git add [<pathspec>…]...
sbt> git commit -m <msg>
sbt> git help
```

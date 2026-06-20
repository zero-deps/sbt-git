# sbt-git

Small SBT AutoPlugin for Git-backed project versions and a few everyday Git
commands inside the SBT shell.

The plugin has two jobs:

- derive `version` from the current Git repository
- expose a `git` input task for common repository operations

## Installation

Add the plugin to `project/plugins.sbt`:

```scala
addSbtPlugin("io.github.zero-deps" % "sbt-git" % "<version>")
```

Then use it from `build.sbt`:

```scala
version := zero.git.version()
```

The plugin is an AutoPlugin with `allRequirements`, so it is enabled
automatically after it is added to the build.

## Version Derivation

`zero.git.version()` calls JGit `describe` for the repository rooted at the
current directory. By default it:

- uses annotated tags, not lightweight tags
- strips a leading `v` from the described tag
- converts Git describe separators from `-` to `.`
- pads the version to at least three dot-separated parts
- appends `-dirty` when the working tree has uncommitted changes

Example outputs:

```text
1.2.3
1.2.3.4.gabcdef0
gabcdef0.0.0
1.2.3-dirty
```

The helper can be configured when a build needs different formatting:

```scala
version := zero.git.version(
  dir = ".",
  tags = false,
  stripPrefix = "v",
  dotted = true,
  parts = Some(3)
)
```

Parameters:

- `dir`: repository directory to open
- `tags`: pass `true` to include lightweight tags in describe results
- `stripPrefix`: prefix removed from the described tag before formatting
- `dotted`: pass `false` to keep Git describe `-` separators
- `parts`: minimum number of dot-separated parts, or `None` to avoid padding

If no tag can describe `HEAD`, the version falls back to the abbreviated commit
hash.

## SBT Shell Commands

After the plugin is loaded, run `git` commands from the SBT shell:

```text
sbt> git status
sbt> git s
sbt> git add <pathspec>...
sbt> git commit -m <message>
sbt> git diff [--staged|--cached] [<pathspec>...]
sbt> git log [-n <count>]
sbt> git log --max-count <count>
sbt> git branch
sbt> git fetch
sbt> git pull
sbt> git push
sbt> git tag
sbt> git help
```

Notes:

- `git s` is a shortcut for `git status`.
- `git add` stages new, modified, and removed files for the given pathspecs.
- `git commit` currently supports `-m` messages.
- `git diff --staged` and `git diff --cached` both show staged changes.
- `git log` defaults to 10 commits.

The command surface is intentionally small. For advanced Git operations, use
the normal Git CLI.

## Development

Compile the plugin:

```sh
sbt compile
```

Run tests:

```sh
sbt test
```

Publish to the local Ivy/Maven cache for testing in another build:

```sh
sbt publishLocal
```

The project uses Scala compiler warnings for features, deprecations, and unused
imports. Keep new code warning-free.

## Publishing

Release publishing uses SBT's Central Portal support plus PGP signing. Keep
Central Portal credentials and local key material outside the repository.

Stage and upload signed artifacts:

```text
sbt> publishSigned
sbt> sonaUpload
```

Upload and release in one step:

```text
sbt> sonaRelease
```

## License

Published under the MIT license.

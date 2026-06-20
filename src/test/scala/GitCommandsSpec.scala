package zero.git

import org.eclipse.jgit.api.Git
import sbt.util.Level
import sbt.util.Logger
import zio.test._

import java.nio.charset.StandardCharsets
import java.nio.file.Files

object GitCommandsSpec extends ZIOSpecDefault {
  def spec: Spec[Any, Any] =
    suite("GitCommands")(
      suite("parse")(
        test("keeps status shortcuts") {
          assertTrue(
            GitCommands.parse(List("status")) == Right(GitCommands.Status),
            GitCommands.parse(List("s")) == Right(GitCommands.Status)
          )
        },
        test("parses add, commit, diff, and log") {
          assertTrue(
            GitCommands.parse(List("add", "src/main/scala/git.scala")) ==
              Right(GitCommands.Add(List("src/main/scala/git.scala"))),
            GitCommands.parse(List("commit", "-m", "\"Add", "commands\"")) ==
              Right(GitCommands.Commit("Add commands")),
            GitCommands.parse(List("diff", "--staged", "README.md")) ==
              Right(GitCommands.Diff(staged = true, List("README.md"))),
            GitCommands.parse(List("log", "-n", "3")) == Right(GitCommands.Log(3))
          )
        },
        test("rejects invalid log counts") {
          assertTrue(
            GitCommands.parse(List("log", "-n", "0")).isLeft,
            GitCommands.parse(List("log", "-n", "many")).isLeft
          )
        },
        test("help lists expanded command surface") {
          assertTrue(
            GitCommands.help.exists(_.contains("git diff")),
            GitCommands.help.exists(_.contains("git log")),
            GitCommands.help.exists(_.contains("git push"))
          )
        }
      ),
      suite("run")(
        test("adds and commits files in a repository") {
          val dir = Files.createTempDirectory("sbt-git-test")
          val repo = Git.init.setDirectory(dir.toFile).call
          try {
            repo.getRepository.getConfig.setString("user", null, "name", "Test User")
            repo.getRepository.getConfig.setString("user", null, "email", "test@example.com")
            repo.getRepository.getConfig.save

            Files.write(dir.resolve("README.md"), "hello\n".getBytes(StandardCharsets.UTF_8))
            GitCommands.run(repo, GitCommands.Add(List("README.md")), TestLogger)
            GitCommands.run(repo, GitCommands.Commit("Initial commit"), TestLogger)

            val status = repo.status.call
            val commits = repo.log.call.iterator
            assertTrue(status.isClean, commits.hasNext)
          } finally {
            repo.close
          }
        },
        test("prints unstaged diffs") {
          val dir = Files.createTempDirectory("sbt-git-diff-test")
          val repo = Git.init.setDirectory(dir.toFile).call
          val log = new CapturingLogger
          try {
            repo.getRepository.getConfig.setString("user", null, "name", "Test User")
            repo.getRepository.getConfig.setString("user", null, "email", "test@example.com")
            repo.getRepository.getConfig.save

            Files.write(dir.resolve("notes.txt"), "old\n".getBytes(StandardCharsets.UTF_8))
            GitCommands.run(repo, GitCommands.Add(List("notes.txt")), TestLogger)
            GitCommands.run(repo, GitCommands.Commit("Add notes"), TestLogger)
            Files.write(dir.resolve("notes.txt"), "new\n".getBytes(StandardCharsets.UTF_8))

            GitCommands.run(repo, GitCommands.Diff(staged = false, Nil), log)

            assertTrue(log.messages.exists(_.contains("-old")), log.messages.exists(_.contains("+new")))
          } finally {
            repo.close
          }
        }
      )
    )

  object TestLogger extends CapturingLogger

  class CapturingLogger extends Logger {
    private var entries = Vector.empty[String]

    def messages: Vector[String] = entries

    override def trace(t: => Throwable): Unit =
      entries = entries :+ t.toString

    override def success(message: => String): Unit =
      entries = entries :+ message

    override def log(level: Level.Value, message: => String): Unit =
      entries = entries :+ message
  }
}

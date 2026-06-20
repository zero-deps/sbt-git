package zero.git

import sbt._, Keys._
import sbt.complete.DefaultParsers._
import org.eclipse.jgit.api._
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.treewalk.filter.PathFilterGroup
import java.io.ByteArrayOutputStream
import scala.collection.JavaConverters._

object GitPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    val git = inputKey[Unit]("Git support")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    git := {
      val s = streams.value
      // https://download.eclipse.org/jgit/site/5.7.0.202003110725-r/apidocs/index.html
      val repo = Git.open(file("."))
      try {
        GitCommands.parse(spaceDelimited("<arg>").parsed.toList) match {
          case Right(cmd) => GitCommands.run(repo, cmd, s.log)
          case Left(msg) =>
            if (msg.nonEmpty) s.log.warn(msg)
            GitCommands.help.foreach(line => s.log.info(line))
        }
      } finally {
        repo.close
      }
    }
  )
}

object GitCommands {
  sealed trait Command
  case object Status extends Command
  final case class Add(pathspecs: List[String]) extends Command
  final case class Commit(message: String) extends Command
  final case class Diff(staged: Boolean, pathspecs: List[String]) extends Command
  final case class Log(maxCount: Int) extends Command
  case object Branch extends Command
  case object Fetch extends Command
  case object Pull extends Command
  case object Push extends Command
  case object Tag extends Command

  val help: List[String] = List(
    "Available commands:",
    "  git status",
    "  git add <pathspec>...",
    "  git commit -m <msg>",
    "  git diff [--staged] [<pathspec>...]",
    "  git log [-n <count>]",
    "  git branch",
    "  git fetch",
    "  git pull",
    "  git push",
    "  git tag"
  )

  def parse(args: List[String]): Either[String, Command] =
    args match {
      case cmd :: Nil if cmd.startsWith("s") => Right(Status)
      case "add" :: xs if xs.nonEmpty => Right(Add(xs))
      case "commit" :: "-m" :: msg if msg.nonEmpty =>
        Right(Commit(stripQuotes(msg.mkString(" "))))
      case "diff" :: xs => Right(parseDiff(xs))
      case "log" :: xs => parseLog(xs)
      case "branch" :: Nil => Right(Branch)
      case "fetch" :: Nil => Right(Fetch)
      case "pull" :: Nil => Right(Pull)
      case "push" :: Nil => Right(Push)
      case "tag" :: Nil => Right(Tag)
      case Nil | "help" :: Nil | "-h" :: Nil | "--help" :: Nil => Left("")
      case _ => Left("Unknown or incomplete git command.")
    }

  def run(git: Git, command: Command, log: Logger): Unit =
    command match {
      case Status => status(git, log)
      case Add(pathspecs) => add(git, pathspecs)
      case Commit(message) => commit(git, message, log)
      case Diff(staged, pathspecs) => diff(git, staged, pathspecs, log)
      case Log(maxCount) => history(git, maxCount, log)
      case Branch => branch(git, log)
      case Fetch => fetch(git, log)
      case Pull => pull(git, log)
      case Push => push(git, log)
      case Tag => tag(git, log)
    }

  private def parseDiff(args: List[String]): Diff = {
    val (flags, pathspecs) = args.partition(a => a == "--staged" || a == "--cached")
    Diff(flags.nonEmpty, pathspecs)
  }

  private def parseLog(args: List[String]): Either[String, Log] =
    args match {
      case Nil => Right(Log(10))
      case "-n" :: count :: Nil => parseCount(count).map(Log)
      case "--max-count" :: count :: Nil => parseCount(count).map(Log)
      case _ => Left("Usage: git log [-n <count>]")
    }

  private def parseCount(s: String): Either[String, Int] =
    try {
      val count = s.toInt
      if (count > 0) Right(count)
      else Left("Log count must be positive.")
    } catch {
      case _: NumberFormatException => Left("Log count must be a number.")
    }

  private def stripQuotes(s: String): String =
    s.stripPrefix("\"").stripSuffix("\"")

  private def status(git: Git, log: Logger): Unit = {
    def formatLine(c: String): String =
      " ".repeat(4)+c+" ".repeat(math.max(0, 80-4-c.length))

    val st = git.status.call
    List(st.getChanged, st.getAdded, st.getRemoved).flatMap(_.asScala).toList.sorted match {
      case Nil =>
      case xs =>
        log.info(" ")
        log.info("Changes to be committed:")
        xs.map(formatLine).map("\u001B[32m"+_+"\u001B[0m").foreach(log.info(_))
        log.info(" ")
    }
    List(st.getModified, st.getMissing).flatMap(_.asScala).toList.sorted match {
      case Nil =>
      case xs =>
        log.info(" ")
        log.info("Changes not staged for commit:")
        xs.map(formatLine).map("\u001B[31m"+_+"\u001B[0m").foreach(log.info(_))
        log.info(" ")
    }
    st.getUntracked.asScala.toList.sorted match {
      case Nil =>
      case xs =>
        log.info(" ")
        log.info("Untracked files:")
        xs.map(formatLine).map("\u001B[31m"+_+"\u001B[0m").foreach(log.info(_))
        log.info(" ")
    }
    if (st.isClean) log.info("Working tree clean.")
    log.info(" ")
  }

  private def add(git: Git, pathspecs: List[String]): Unit = {
    val add = git.add
    pathspecs.foreach(add.addFilepattern)
    add.call
    val add2 = git.add.setUpdate(true)
    pathspecs.foreach(add2.addFilepattern)
    add2.call
  }

  private def commit(git: Git, message: String, log: Logger): Unit = {
    val result = git.commit.setMessage(message).call
    log.info(s"[${result.abbreviate(7).name}] ${result.getShortMessage}")
  }

  private def diff(git: Git, staged: Boolean, pathspecs: List[String], log: Logger): Unit = {
    val out = new ByteArrayOutputStream
    val cmd = git.diff.setCached(staged).setOutputStream(out)
    if (pathspecs.nonEmpty) cmd.setPathFilter(PathFilterGroup.createFromStrings(pathspecs.asJava))
    cmd.call
    val text = new String(out.toByteArray, "UTF-8")
    if (text.isEmpty) log.info("No differences.")
    else text.linesIterator.foreach(line => log.info(line))
  }

  private def history(git: Git, maxCount: Int, log: Logger): Unit =
    git.log.setMaxCount(maxCount).call.asScala.foreach { commit =>
      val id = commit.abbreviate(7).name
      val author = commit.getAuthorIdent.getName
      log.info(s"$id $author ${commit.getShortMessage}")
    }

  private def branch(git: Git, log: Logger): Unit = {
    val current = git.getRepository.getBranch
    git.branchList.call.asScala
      .map(ref => Repository.shortenRefName(ref.getName))
      .toList
      .sorted
      .foreach { name =>
        val prefix = if (name == current) "*" else " "
        log.info(s"$prefix $name")
      }
  }

  private def fetch(git: Git, log: Logger): Unit = {
    val result = git.fetch.call
    val updates = result.getTrackingRefUpdates.asScala.toList
    if (updates.isEmpty) log.info("Already up to date.")
    else updates.foreach(u => log.info(s"${u.getLocalName}: ${u.getResult}"))
  }

  private def pull(git: Git, log: Logger): Unit = {
    val result = git.pull.call
    if (result.isSuccessful) log.info("Pull successful.")
    else sys.error("Pull failed.")
  }

  private def push(git: Git, log: Logger): Unit =
    git.push.call.asScala.foreach { result =>
      result.getRemoteUpdates.asScala.foreach { update =>
        log.info(s"${update.getRemoteName}: ${update.getStatus}")
      }
    }

  private def tag(git: Git, log: Logger): Unit =
    git.tagList.call.asScala
      .map(ref => Repository.shortenRefName(ref.getName))
      .toList
      .sorted
      .foreach(name => log.info(name))
}

object version {
  def apply(
      dir: String = "."
    , tags: Boolean = false
    , stripPrefix: String = "v"
    , dotted: Boolean = true
    , parts: Option[Int] = Some(3)
    ): String = {
    val git = Git.open(file(dir))
    val base = {
      val repo = git.getRepository
      val desc = Option(git.describe.setTags(tags).call).map(_.stripPrefix(stripPrefix))
      val desc1 = desc.getOrElse{
        repo.newObjectReader.abbreviate(repo.resolve("HEAD")).name
      }
      val desc2 = if (dotted) desc1.replace('-','.') else desc1
      parts match {
        case None => desc2
        case Some(i) =>
          val count = desc2.split('.').length
          if (count >= i) desc2
          else desc2 + ".0".repeat(i-count)
      }
    }
    val dirty = if (git.status.call.getUncommittedChanges.isEmpty) "" else "-dirty"
    base + dirty
  }
}

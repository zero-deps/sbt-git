package zero.git

import sbt._, Keys._
import sbt.complete.DefaultParsers._
import org.eclipse.jgit.api._
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
      val git = Git.open(file("."))
      spaceDelimited("<arg>").parsed.toList match {
        case cmd :: Nil if cmd startsWith "s" =>
          def formatLine(c: String): String = " ".repeat(4)+c+" ".repeat(80-4-c.length)
          val st = git.status.call
          List(st.getChanged, st.getAdded).flatMap(_.asScala).toList.sorted match {
            case Nil =>
            case xs =>
              s.log.info(" ")
              s.log.info("Changes to be committed:")
              xs.map(formatLine).map("\u001B[32m"+_+"\u001B[0m").foreach(s.log.info(_))
              s.log.info(" ")
          }
          st.getModified.asScala.toList.sorted match {
            case Nil =>
            case xs =>
              s.log.info(" ")
              s.log.info("Changes not staged for commit:")
              xs.map(formatLine).map("\u001B[31m"+_+"\u001B[0m").foreach(s.log.info(_))
              s.log.info(" ")
          }
          List(st.getUntracked).flatMap(_.asScala).toList.sorted match {
            case Nil =>
            case xs =>
              s.log.info(" ")
              s.log.info("Untracked files:")
              xs.map(formatLine).map("\u001B[31m"+_+"\u001B[0m").foreach(s.log.info(_))
              s.log.info(" ")
          }
          s.log.info(" ")
        case "add" :: xs if xs.nonEmpty =>
          val add = git.add
          xs.foreach(add.addFilepattern)
          add.call
          val add2 = git.add.setUpdate(true)
          xs.foreach(add2.addFilepattern)
          add2.call
        case "commit" :: "-m" :: msg if msg.nonEmpty =>
          git.commit.setMessage(msg.mkString(" ").stripPrefix("\"").stripSuffix("\"")).call
        case _ =>
          s.log.info("Available commands:")
          s.log.info("  git status")
          s.log.info("  git add [<pathspec>…]")
          s.log.info("  git commit -m <msg>")
      }
    }
  )
}

object version {
  def apply(
      dir: String = "."
    , tags: Boolean = false
    , stripPrefix: String = "v"
    , dotted: Boolean = true
    , always: Boolean = false
    , parts: Option[Int] = Some(3)
    ): String = {
    if (file(s"$dir/.git").isFile) {
      "0.1.0-SNAPSHOT"
    } else {
      val git = Git.open(file(dir))
      val base = {
        val repo = git.getRepository
        val desc = Option(git.describe.setTags(tags).call).map(_.stripPrefix(stripPrefix))
        val desc1 = desc.getOrElse(
          if (always) repo.newObjectReader.abbreviate(repo.resolve("HEAD")).name
          else throw new Exception("no tags. please add tag with `git tag -a <name> -m <name>` or use `zero.git.version(tags=true)`.")
        )
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
}

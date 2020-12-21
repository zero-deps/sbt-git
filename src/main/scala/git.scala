package zero.git

import java.io.File
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
          val emptyLine = " ".repeat(80)
          val st = git.status.call
          print(List(st.getChanged, st.getAdded).flatMap(_.asScala).toList.sorted match {
            case Nil => ""
            case xs => s"""
            |Changes to be committed:
            |\u001B[32m${xs.map(formatLine).mkString("\n")}\u001B[0m
            |$emptyLine""".stripMargin
          })
          print(st.getModified.asScala.toList.sorted match {
            case Nil => ""
            case xs => s"""
            |Changes not staged for commit:
            |\u001B[31m${xs.map(formatLine).mkString("\n")}\u001B[0m
            |$emptyLine""".stripMargin
          })
          print(List(st.getUntracked).flatMap(_.asScala).toList.sorted match {
            case Nil => ""
            case xs => s"""
            |Untracked files:
            |\u001B[31m${xs.map(formatLine).mkString("\n")}\u001B[0m
            |$emptyLine""".stripMargin
          })
          s.log.info(emptyLine)
        case "add" :: xs if xs.nonEmpty =>
          val add = git.add
          xs.foreach(add.addFilepattern)
          add.call
          val add2 = git.add.setUpdate(true)
          xs.foreach(add2.addFilepattern)
          add2.call
        case "commit" :: "-m" :: msg :: Nil if msg.nonEmpty =>
          git.commit.setMessage(msg).call
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
    val git = org.eclipse.jgit.api.Git.open(new File(dir))
    val base = {
      val repo = git.getRepository
      val desc = Option(git.describe.setTags(tags).call).map(_.stripPrefix(stripPrefix))
      val desc1 = desc.getOrElse(
        if (always) repo.newObjectReader.abbreviate(repo.resolve("HEAD")).name
        else throw new Exception("no tags")
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

package radium.dockerfile.task

import java.nio.file.Path

import cats.data._
import cats.implicits._
import radium.dockerfile.{Config, Distro, statement}
import radium.dockerfile.statement._
import radium.dockerfile.implicits._

case class ManageFile(filePath: Path, user: Option[User], mode: Option[Mode], state: State) extends Task {

  import ManageFile._

  override def generateStatements: Function[Distro, Seq[Statement]] = {
    case _ =>
      (state match {
        case Directory =>
          Seq(RunStatement(s"mkdir -p ${filePath.toString}"))
        case Absent =>
          Seq(RunStatement(s"rm -Rf ${filePath.toString}"))
      }) ++ mode.map({ mode =>
        RunStatement(s"chmod ${mode} ${filePath.toString}")
      }).toSeq ++ user.map({ user =>
        RunStatement(s"chown ${user} ${filePath.toString}")
      }).toSeq
  }
}

object ManageFile extends TaskCreator {

  val Directory: State = "directory"

  val Present: State = "present"

  val Absent: State = "absent"

  def filePath = arg[Path]("path").required

  def user = arg[User]("user")

  def mode = arg[Mode]("mode")

  def state = arg[State]("state").required

  override def supportedTaskNames: Seq[TaskName] = Seq("file")

  override def createTask(implicit config: Config) = renderedTemplates { yaml =>
    (filePath, user, mode, state).parse(yaml).mapN(ManageFile.apply)
  }
}

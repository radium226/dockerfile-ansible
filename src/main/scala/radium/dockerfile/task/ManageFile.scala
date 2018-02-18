package radium.dockerfile.task

import java.nio.file.Path

import radium.dockerfile._
import radium.dockerfile.Config
import radium.dockerfile.yaml._
import radium.dockerfile.arg._
import radium.dockerfile.statement._
import radium.dockerfile.implicits._
import radium.dockerfile.transpilation._

case class ManageFile(filePath: Path, user: Option[User], mode: Option[Mode], state: State) extends Task {

  import ManageFile._

  override def generateStatements = generic {
    (state match {
      case Directory =>
        Seq(Run.single(s"mkdir -p ${filePath.toString}"))
      case Absent =>
        Seq(Run.single(s"rm -Rf ${filePath.toString}"))
    }) ++ mode.map({ mode =>
      Run.single(s"chmod ${mode} ${filePath.toString}")
    }).toSeq ++ user.map({ user =>
      Run.single(s"chown ${user} ${filePath.toString}")
    }).toSeq
  }
}

object ManageFile extends TaskParser {

  val Directory: State = "directory"

  val Present: State = "present"

  val Absent: State = "absent"

  def filePath = Arg.byKey[Path]("path").required

  def user = Arg.byKey[User]("user")

  def mode = Arg.byKey[Mode]("mode")

  def state = Arg.byKey[State]("state").required

  override def supportedTaskNames: Seq[TaskName] = Seq("file")

  override def parse(config: Config) = expandVars { yaml =>
    import cats.implicits._
    (filePath, user, mode, state).parse(yaml).mapN(ManageFile.apply)
  }
}

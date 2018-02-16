package radium.dockerfile.task

import java.nio.file.{Files, Path, Paths}

import radium.dockerfile._
import radium.dockerfile.statement._
import radium.dockerfile.implicits._
import com.roundeights.hasher.Implicits._
import scala.language.postfixOps

case class Shell(val content: Code) extends Task with GenerateGenericStatements with GenerateGenericFile {

  val fileName: Path = Paths.get(s"shell-${content.sha1}.sh")
  val localFilePath: Path = fileName
  val remoteFilePath: Path = Paths.get("/tmp").resolve(fileName)

  override def fileSpec: FileSpec = FileSpec(Some(localFilePath), content)

  override def statements = Seq(
    CopyStatement(localFilePath, remoteFilePath),
    RunStatement(Seq(s"chmod +x ${remoteFilePath}", s"${remoteFilePath}"))
  )

}

object Shell extends TaskCreator {

  override def supportedTaskNames: Seq[TaskName] = Seq("shell")

  def content = Arg.whole[Code].required

  override def createTask(implicit config: Config) = renderedTemplates { yaml =>
    content.parse(yaml).map(Shell.apply)
  }

}

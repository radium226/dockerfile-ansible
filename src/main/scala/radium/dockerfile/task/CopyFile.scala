package radium.dockerfile.task

import java.nio.file.Path

import cats.data._
import cats.implicits._
import radium.dockerfile.{Config, Vars}
import radium.dockerfile.statement._
import radium.dockerfile.implicits._

case class CopyFile(val localFilePath: Path, val remoteFilePath: Path) extends Task with GenerateGenericStatement {

  override def statement = CopyStatement(localFilePath, remoteFilePath)

}



object CopyFile extends TaskCreator {

  override def supportedTaskNames: Seq[TaskName] = Seq("copy")

  def localFilePath = arg[Path]("src").required

  def remoteFilePath = arg[Path]("dest").required

  override def createTask(implicit config: Config) = renderedTemplates { yaml: Yaml =>
    (localFilePath, remoteFilePath).parse(yaml).mapN(CopyFile.apply)
  }


}



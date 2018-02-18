package radium.dockerfile.task

import java.nio.file.Path

import radium.dockerfile._
import radium.dockerfile.yaml._
import radium.dockerfile.arg._
import radium.dockerfile.{ statement => s }
import radium.dockerfile.implicits._
import radium.dockerfile.transpilation._

case class CopyFile(val localFilePath: Path, val remoteFilePath: Path) extends Task with GenerateGenericStatement {

  override def statement = s.Copy(localFilePath, remoteFilePath)

}



object CopyFile extends TaskParser {

  override def supportedTaskNames: Seq[TaskName] = Seq("copy")

  def localFilePath = Arg.byKey[Path]("src").required

  def remoteFilePath = Arg.byKey[Path]("dest").required

  override def parse(config: Config) = expandVars { yaml =>
    import cats.implicits._
    (localFilePath, remoteFilePath).parse(yaml).mapN(CopyFile.apply)
  }

}



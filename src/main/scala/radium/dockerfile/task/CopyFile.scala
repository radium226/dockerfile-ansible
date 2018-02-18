package radium.dockerfile.task

import java.nio.file.Path

import radium.dockerfile._
import radium.dockerfile.yaml._
import radium.dockerfile.{ statement => s }
import radium.dockerfile.implicits._
import radium.dockerfile.transpilation._

case class CopyFile(val localFilePath: Option[Path], content: Option[Source], val remoteFilePath: Path) extends Task with GenerateGenericStatement {

  override def statement = {
    (localFilePath, content) match {
      case (Some(localFilePath), None) =>
        s.Copy(localFilePath, remoteFilePath)

      case (None, Some(content)) =>
        s.Run(
          Left(s"> ${remoteFilePath}") +: content.split("\n")
            .toSeq
            .map({ line =>
              Left(s"echo ${line} >>${remoteFilePath}")
          })
        )

    }
  }

}



object CopyFile extends TaskParser {

  override def supportedTaskNames: Seq[TaskName] = Seq("copy")

  def localFilePath = Binding.byKey[Path]("src")

  def source = Binding.byKey[Source]("content")

  def remoteFilePath = Binding.byKey[Path]("dest").required

  override def parse(config: Config) = expandVars { yaml =>
    import cats.implicits._
    (localFilePath, source, remoteFilePath).bind(yaml).mapN(CopyFile.apply)
  }

}



package radium.dockerfile.task

import java.net.URL
import java.nio.file.Path

import cats.data._
import cats.implicits._
import radium.dockerfile.Config
import radium.dockerfile.statement._
import radium.dockerfile.implicits._

case class DownloadFile(fromUrl: URL, toFilePath: Path) extends Task with GenerateRunStatement with GenericCommand {

  override def dependsOf: Seq[DependencyName] = super.dependsOf ++ Seq("curl")

  override def command: Command = s"curl ${fromUrl.toString} -O ${toFilePath.toString}"

}

object DownloadFile extends TaskCreator {
  override def supportedTaskNames = Seq("get_url", "download")

  def url = arg[URL]("url").required

  def filePath = arg[Path]("dest").required

  override def createTask(yaml: Yaml)(implicit config: Config): ValidatedTask = {
    (url, filePath).parse(yaml).mapN(DownloadFile.apply)
  }

}
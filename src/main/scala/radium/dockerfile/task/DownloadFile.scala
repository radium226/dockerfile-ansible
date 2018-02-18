package radium.dockerfile.task

import java.net.URL
import java.nio.file.Path

import radium.dockerfile._
import radium.dockerfile.yaml._

import radium.dockerfile.{ statement => s }
import radium.dockerfile.implicits._
import radium.dockerfile.transpilation._

case class DownloadFile(fromUrl: URL, toFilePath: Path) extends Task with GenerateRunStatement {

  override def dependsOf: Seq[DependencyName] = super.dependsOf ++ Seq("curl")

  override def provideCommand = { distro: Distro =>
    s"curl ${fromUrl.toString} -O ${toFilePath.toString}"
  }

}

object DownloadFile extends TaskParser {
  override def supportedTaskNames = Seq("get_url", "download")

  def url = Binding.byKey[URL]("url").required

  def filePath = Binding.byKey[Path]("dest").required

  override def parse(config: Config) = expandVars { yaml =>
    import cats.implicits._
    (url, filePath).parse(yaml).mapN(DownloadFile.apply)
  }

}
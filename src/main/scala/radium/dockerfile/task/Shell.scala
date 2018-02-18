package radium.dockerfile.task

import java.nio.file.{Files, Path, Paths}

import radium.dockerfile._
import radium.dockerfile.yaml._
import radium.dockerfile.arg._
import radium.dockerfile.statement._
import radium.dockerfile.implicits._
import radium.dockerfile.transpilation._
import com.roundeights.hasher.Implicits._
import scala.language.postfixOps

case class Shell(val source: Source) extends Task with GenerateGenericStatements with GenerateGenericFile {

  val fileName: Path = Paths.get(s"shell-${source.sha1.hash.substring(0, 7)}.sh")
  val localFilePath: Path = fileName
  val remoteFilePath: Path = Paths.get("/tmp").resolve(fileName)

  override def fileSpec = FileSpec(localFilePath, source)

  override def statements = Seq(
    Copy(localFilePath, remoteFilePath),
    Run.single(s"chmod +x ${remoteFilePath}"),
    Run.single(s"${remoteFilePath}")
  )

}

object Shell extends TaskParser {

  override def supportedTaskNames = Seq("shell")

  def source = Arg.whole[Source].required

  override def parse(config: Config) = expandVars { yaml =>
    source.parse(yaml).map(Shell.apply)
  }

}

package radium.dockerfile.execute

import java.nio.file.Path

import radium.dockerfile._
import radium.dockerfile.arg._
import radium.dockerfile.yaml._
import radium.dockerfile.{ statement => s }
import radium.dockerfile.implicits._

case class Java(jarFilePath: Path) extends Execute {


  override def provideFileSpecs = no

  override def generateStatements = single {
    generic {
      s.Command(Seq("java", "-jar", s"${jarFilePath}"))
    }
  }
}

object Java extends ExecuteParser with Keyed {

  override def keyName = "java"

  def jarFilePath = Arg.byKey[Path]("jar").required

  override def parse(config: Config) = expandVars { yaml =>
    jarFilePath.parse(yaml).map(Java.apply)
  }

}
package radium.dockerfile.execute

import java.nio.file.Path

import radium.dockerfile.implicits._

import cats.data._
import radium.dockerfile.statement._
import radium.dockerfile.task._

case class Java(jarFilePath: Path) extends Execute {


  override def provideFileSpecs = no

  override def generateStatements = single {
    generic {
      CommandStatement(Seq("java", "-jar", s"${jarFilePath}"))
    }
  }
}

object Java extends ExecuteParser with Keyed {

  override def keyName = "java"

  def jarFilePath = Arg.byKey[Path]("java").required

  def parse(yaml: Yaml): ValidatedNel[Cause, Execute] = {
    jarFilePath.parse(yaml).map(Java.apply)
  }

}
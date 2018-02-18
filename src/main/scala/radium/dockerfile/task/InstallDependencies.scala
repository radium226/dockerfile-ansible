package radium.dockerfile.task

import radium.dockerfile._
import radium.dockerfile.yaml._
import radium.dockerfile.arg._
import radium.dockerfile.{ statement => s }
import radium.dockerfile.implicits._
import radium.dockerfile.transpilation._

case class InstallDependencies(dependencyNames: Seq[DependencyName]) extends Task with GenerateRunStatement {

  private val args = dependencyNames
      .mkString(" ")

  override def provideCommand = {
    case Ubuntu =>
      s"apt-get -y install ${args}"

    case Alpine =>
      s"apk add ${args}"
  }

}

object InstallDependencies extends TaskParser {

  override def supportedTaskNames: Seq[TaskName] = Seq("package")

  def dependencyName = Arg.byKey[String]("name").required

  def apply(dependencyName: DependencyName): InstallDependencies = {
    new InstallDependencies(Seq(dependencyName))
  }

  override def parse(config: Config) = expandVars { yaml =>
    dependencyName.parse(yaml).map(InstallDependencies.apply)
  }
}
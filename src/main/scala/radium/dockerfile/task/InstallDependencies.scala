package radium.dockerfile.task

import cats.data.ValidatedNel
import radium.dockerfile.task.Include.renderedTemplates
import radium.dockerfile.{Alpine, Config, Ubuntu}

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

object InstallDependencies extends TaskCreator {

  override def supportedTaskNames: Seq[TaskName] = Seq("package")

  def dependencyName = arg[String]("name").required

  def apply(dependencyName: DependencyName): InstallDependencies = {
    new InstallDependencies(Seq(dependencyName))
  }

  override def createTask(implicit config: Config) = renderedTemplates { yaml =>
    dependencyName.parse(yaml).map(InstallDependencies.apply)
  }
}
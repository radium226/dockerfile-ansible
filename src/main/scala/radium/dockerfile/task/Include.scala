package radium.dockerfile.task

import java.nio.file.{Files, Path, Paths}

import cats.implicits._

import radium.dockerfile._
import radium.dockerfile.statement._

case class Include(val filePath: Path, val config: Config) extends Task {

  private def includedTasks: Seq[Task] = Dockerfile.parseTasks(filePath, Map())(config).getOrElse(Seq()) // TODO

  override def dependsOf: Seq[DependencyName] = Task.dependsOf(includedTasks)

  override def generateStatements: Function[Distro, Seq[Statement]] = Task.generateStatements(includedTasks)

}

object Include extends TaskCreator {

  def resolveFilePath(filePath: Path)(implicit config: Config): ValidatedValue[Path] = filePath match {
    case filePath if filePath.isAbsolute =>
      filePath.validNel[Cause]
    case filePath =>
      config.includeFolderPaths
          .map(_.resolve(filePath))
          .filter({ resolvedFilePath =>
            println(s"resolvedFilePath = ${resolvedFilePath}")
            Files.exists(resolvedFilePath)
          })
          .headOption.toValidNel[Cause](s"${filePath.toString} has not been found")
  }

  override def supportedTaskNames: Seq[TaskName] = Seq("include")

  def yamlFilePath = arg[Path].required

  override def createTask(implicit config: Config) = renderedTemplates { yaml =>
    yamlFilePath
      .transform(resolveFilePath)
      .parse(yaml)
      .map({ filePath =>
        Include.apply(filePath, config)
      })
  }
}


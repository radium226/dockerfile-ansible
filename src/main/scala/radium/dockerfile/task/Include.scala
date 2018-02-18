package radium.dockerfile.task

import java.nio.file.{ Files, Path }

import radium.dockerfile._
import radium.dockerfile.arg._
import radium.dockerfile.yaml._
import radium.dockerfile.{ statement => s }
import s.Statement
import radium.dockerfile.transpilation._
import radium.dockerfile.implicits._

case class Include(tasks: Seq[Task]) extends Task {

  override def dependsOf: Seq[DependencyName] = Task.dependsOf(tasks)

  override def generateStatements = Statement.generateStatements(tasks)

}

object Include extends TaskParser {

  def resolveFilePath(config: Config): Path => Validated[Path] = {
    case filePath if filePath.isAbsolute =>
      filePath.valid
    case filePath =>
      config.includedFolderPaths
        .map(_.resolve(filePath))
        .filter({ resolvedFilePath =>
          Files.exists(resolvedFilePath)
        })
        .headOption.toValidated(s"${filePath.toString} has not been found")
  }

  override def supportedTaskNames: Seq[TaskName] = Seq("include")

  def filePath = Arg.whole[Path].required

  override def parse(config: Config) = { (yaml, vars) =>
    filePath.transform(resolveFilePath(config)).parse(yaml) andThen Yaml.parse andThen { Task.parse(config)(_, vars) } map Include.apply
  }
}


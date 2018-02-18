package radium.dockerfile

import java.nio.file.{Files, Path}

import scala.collection.JavaConverters._

import org.yaml.snakeyaml.{ Yaml => SnakeYaml }
import java.util.{List => JavaList, Map => JavaMap}

import radium.dockerfile._
import radium.dockerfile.task._
import radium.dockerfile.yaml._
import radium.dockerfile.statement._
import radium.dockerfile.execute._
import radium.dockerfile.transpilation._

import radium.dockerfile.implicits._

case class Dockerfile(distro: Distro, tasks: Seq[Task], executes: Seq[Execute], vars: Vars)

object Dockerfile {

  def parse(config: Config): (Yaml, Vars) => Validated[Dockerfile] = {
    case (yaml: Map[String, Yaml], vars) =>
      Vars.parse(config)(yaml.get("vars"), vars) andThen { vars =>
        Distro.parse(config)(yaml.get("from"), vars) andThen { distro =>
          Task.parse(config)(yaml.get("tasks"), vars) andThen { tasks =>
            Execute.parse(config)(yaml.get("execute"), vars) map { executes =>
              Dockerfile(distro, tasks, executes, vars)
            }
          }
        }
      }
    case _ =>
      "The YAML has a bad format".invalid
  }

  def transpile(config: Config): Dockerfile => Seq[FileSpec] = { dockerfile =>
    val fileSpecs = FileSpec.collectFileSpecs(dockerfile)(dockerfile.distro)

    val dependencyNames = Task.dependsOf(dockerfile.tasks)
    val generateStatements = Statement.generateStatements(InstallDependencies(dependencyNames) +: dockerfile.tasks)
    val statements = Distro.generateStatements(config)(dockerfile.distro) ++ generateStatements(dockerfile.distro) ++ Statement.generateStatements(dockerfile.executes)(dockerfile.distro)
    val source = Statement.toSource(statements)
    FileSpec("Dockerfile", source) +: fileSpecs
  }

}

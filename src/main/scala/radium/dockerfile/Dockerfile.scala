package radium.dockerfile

import java.nio.file.{Files, Path}

import org.yaml.snakeyaml.Yaml
import task._
import java.util.{List => JavaList, Map => JavaMap}

import radium.dockerfile.statement.Statement

import scala.collection.JavaConverters._

import cats.implicits._
import radium.dockerfile.implicits._

case class Dockerfile(distro: Distro, tasks: Seq[Task], vars: Vars)

object Dockerfile {

  implicit class AnyRefWithAs(anyRef: AnyRef) {

    def as[T]: T = {
      (anyRef match {
        case null =>
          Map[String, AnyRef]() // TODO ?

        case javaList: JavaList[AnyRef] =>
          javaList.asScala.map({ anyRef =>
            anyRef.as[AnyRef]
          })

        case javaMap: JavaMap[String, AnyRef] =>
          Map(javaMap.asScala.toList: _*).mapValues({ anyRef =>
            anyRef.as[AnyRef]
          })

        case anyRef =>
          anyRef

      }).asInstanceOf[T]
    }
  }

  def parseTasks(yamlFilePath: Path, vars: Vars)(implicit config: Config): ValidatedTasks = {
    val yaml = new Yaml().load[AnyRef](Files.newInputStream(yamlFilePath)).as[Seq[Map[String, AnyRef]]]
    parseTasks(Some(yaml), vars)
  }

  private def parseTasks(yaml: Option[AnyRef], vars: Vars)(implicit config: Config): ValidatedTasks = {
    yaml.toValidNel[Cause]("No YAML found :(") map { yaml: AnyRef =>
      yaml.as[Seq[Map[String, AnyRef]]]
        .flatMap({ map =>
          val (taskName, yaml) = map.head
          map.last match {
            case ("with_items", items: Seq[String]) =>
              items.map({ item =>
                ((taskName, yaml), vars + ("item" -> item))
              })
            case _ =>
              Seq(((taskName, yaml), Map[String, AnyRef]()))
          }
        })
      } andThen Task.evaluate
  }

  private def parseDistro(yaml: Option[AnyRef]): ValidatedDistro = {
    yaml
      .map(_.as[String])
      .flatMap(Distro.byName)
      .toValidNel(s"The distro has not been found")
  }

  private def parseVars(yaml: Option[AnyRef]): ValidatedVars = {
    yaml
      .flatMap({ Option(_) })
      .map({ t => println(t) ; t})
      .map(_.as[Vars])
      .getOrElse(Map[String, AnyRef]())
      .validNel[Cause]
  }

  def parse(yamlFilePath: Path)(implicit config: Config): ValidatedDockerfile = {
    val yaml = new Yaml().load[AnyRef](Files.newInputStream(yamlFilePath)).as[Map[String, AnyRef]]
    parseVars(yaml.get("vars")) andThen { vars =>
      parseDistro(yaml.get("from")) andThen { distro =>
        parseTasks(yaml.get("tasks"), vars) map { tasks =>
          Dockerfile(distro, tasks, vars)
        }
      }
    }
  }

  def transpile(dockerfile: Dockerfile)(implicit config: Config): Seq[FileSpec] = {
    val fileSpecs = FileSpec.collectFileSpecs(dockerfile.tasks)(dockerfile.distro)

    val dependencyNames = Task.dependsOf(dockerfile.tasks)
    val generateStatements = Task.generateStatements(InstallDependencies(dependencyNames) +: dockerfile.tasks)
    val statements = generateStatements(dockerfile.distro)
    val source = Statement.toSource(statements)
    FileSpec("Dockerfile", source) +: fileSpecs
  }

}

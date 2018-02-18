package radium.dockerfile.task

import java.nio.file.Path

import radium.dockerfile._
import com.hubspot.jinjava.Jinjava
import radium.dockerfile.{Validated, Vars}
import radium.dockerfile.transpilation.GenerateStatements
import radium.dockerfile.yaml.Yaml
import radium.dockerfile.implicits._

import scala.collection.JavaConverters._

trait Task extends GenerateStatements {

  def dependsOf: Seq[DependencyName] = Seq()

}

object Task extends Parser[Seq[Task]] {

  def availableParsers: Seq[TaskParser] = Seq(
    CopyFile,
    DownloadFile,
    Echo,
    Include,
    InstallDependencies,
    ManageFile,
    Shell
  )

  def dependsOf(tasks: Seq[Task]): Seq[DependencyName] = {
    tasks.foldLeft(Seq[DependencyName]())(_ ++ _.dependsOf)
  }

  override def parse(config: Config): (Yaml, Vars) => Validated[Seq[Task]] = {
    case (Some(yaml: Yaml), vars) =>
      parse(config)(yaml, vars)

    case (yamlSeq: Seq[Yaml], vars) =>
      yamlSeq
        .map({ yaml =>
          parse(config)(yaml, vars)
        })
        .traverse
        .map(_.flatten)

    case (yamlMap: Map[String, Yaml], vars) if yamlMap.contains("with_items") =>
      yamlMap.get("with_items") match {
        case Some(items: Seq[Yaml]) =>
          items.map({ item =>
            parse(config)(yamlMap - "with_items", vars + ("item" -> item))
          })
          .traverse
          .map(_.flatten)

        case _ =>
          "Invalid to parse with_items".invalid
      }

    case (yamlMap: Map[String, Yaml], vars) =>
      val localVars = yamlMap.get("vars").getOrElse(Vars.empty)

      (yamlMap - "vars")
        .map({ case (taskName, yaml) =>
          TaskParser.byName(taskName)
            .toValidated(s"No parsed found for task named ${taskName}")
            .andThen { parser =>
              parser.parse(config)(yaml, vars)
            }
        })
        .toSeq
        .traverse

    case _ =>
      "Unable to parse tasks".invalid
  }

}

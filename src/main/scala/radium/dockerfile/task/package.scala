package radium.dockerfile

import java.net.URL
import java.nio.file.{Path, Paths}

import cats.data.{NonEmptyList, Validated, ValidatedNel}
import Validated._
import cats.implicits._
import radium.dockerfile.implicits._
import com.hubspot.jinjava.Jinjava
import radium.dockerfile.statement._
import radium.dockerfile.task.GenerateFile

import scala.collection.JavaConverters._

package object task {

  type Mode = String // TODO

  type State = String // TODO

  type User = String

  type DependencyName = String

  type TaskName = String

  type Yaml = AnyRef

  type ValidatedTask = ValidatedNel[Cause, Task]

  type ValidatedValue[T] = ValidatedNel[Cause, T]

  type Content = Code

  trait ValueParser[T] {

    def parseValue(yaml: Yaml): ValidatedValue[T]

  }

  object ValueParser {

    implicit val string = new ValueParser[String] {

      override def parseValue(yaml: Yaml): ValidatedValue[String] = yaml.toString.validNel[Cause]

    }

    implicit val path = new ValueParser[Path] {

      override def parseValue(yaml: Yaml): ValidatedValue[Path] = Paths.get(yaml.toString).validNel[Cause]

    }

    implicit val url = new ValueParser[URL] {

      override def parseValue(yaml: Yaml): ValidatedValue[URL] = new URL(yaml.toString).validNel[Cause]

    }

    implicit val command = new ValueParser[Command] {

      override def parseValue(yaml: Yaml): ValidatedValue[Command] = yaml match {
        case commandAsString: String =>
          Left(commandAsString).validNel[Cause]

        case commandAsSeq: Seq[String] =>
          Right(commandAsSeq).validNel[Cause]

        case _ =>
          invalidNel("Unable to parse command")
      }

    }
  }

  type Key = String

  type Cause = String

  type Code = String

  trait TaskCreator {

    def renderedTemplates(f: Yaml => ValidatedTask): Yaml => Vars => ValidatedTask = { yaml: Yaml =>
      { vars: Vars =>
        f(yaml.renderTemplates(vars))
      }
    }

    def arg[T:ValueParser](key: Key) = Arg.byKey(key)

    def arg[T:ValueParser] = Arg.whole[T]

    import ValueParser._

    def supportedTaskNames: Seq[TaskName]

    def createTask(implicit config: Config): Yaml => Vars => ValidatedTask

  }

  trait GenerateStatement {
    self: Task =>

    def generateStatements: Function[Distro, Seq[Statement]] = generateStatement andThen { Seq(_) }

    def generateStatement: Function[Distro, Statement]

  }

  trait GenericStatement {
    self: Task with GenerateStatement =>

    override def generateStatement: Function[Distro, Statement] = {
      case _ => statement
    }

    def statement: Statement

  }

  trait GenericStatements {
    self: Task with GenerateStatements =>

    override def generateStatements: Function[Distro, Seq[Statement]] = {
      case _ => statements
    }

    def statements: Seq[Statement]
  }

  trait GenerateGenericStatements extends GenerateStatements with GenericStatements {
    self: Task =>

  }

  trait TranspileAware {

    def onTranspile(): Unit

  }

  trait GenerateGenericStatement extends GenerateStatement with GenericStatement {
    self: Task =>

  }

  trait GenerateRunStatement extends GenerateStatement {
    self: Task =>

    override def generateStatement: Function[Distro, Statement] = provideCommand andThen { RunStatement(_) }

    def provideCommand: PartialFunction[Distro, Command]

  }

  case class FileSpec(val path: Path, val content: Content)

  object FileSpec {

    def collectFileSpecs(tasks: Seq[Task]): PartialFunction[Distro, Seq[FileSpec]] = new PartialFunction[Distro, Seq[FileSpec]] {

      private def collectFileSpecProviders(task: Seq[Task]): Seq[Task with GenerateFiles] = {
        task
          .collect({
            case task: GenerateFiles => task
          })
      }

      override def isDefinedAt(distro: Distro): Boolean = collectFileSpecProviders(tasks)
        .map(_.provideFileSpecs)
        .forall(_.isDefinedAt(distro))

      override def apply(distro: Distro): Seq[FileSpec] = collectFileSpecProviders(tasks)
        .map(_.provideFileSpecs(distro))
        .flatten
    }


      /*new Partia{ distro: Distro =>
      tasks
        .collect({
          case task: GenerateFiles if task.provideFileSpecs.isDefinedAt(distro) =>
            task.provideFileSpecs(distro)
        })
        .flatten
    }*/

  }

  trait GenerateFiles {

    def provideFileSpecs: PartialFunction[Distro, Seq[FileSpec]]

  }

  trait GenerateFile extends GenerateFiles {
    self: Task =>

    override def provideFileSpecs: PartialFunction[Distro, Seq[FileSpec]] = provideFileSpec andThen { Seq(_) }

    def provideFileSpec: PartialFunction[Distro, FileSpec]

  }

  trait GenerateGenericFile extends GenerateFile {
    self: Task =>

    def fileSpec: FileSpec

    override def provideFileSpec: PartialFunction[Distro, FileSpec] = { case _ => fileSpec }
  }

  trait GenericCommand {
    self: GenerateRunStatement =>

    override def provideCommand: PartialFunction[Distro, Command] = {
      case _: Distro => command
    }

    def command: Command
  }

  trait GenerateStatements {

    def generateStatements: Function[Distro, Seq[Statement]]

  }

  trait Task extends GenerateStatements {

    def dependsOf: Seq[DependencyName] = Seq()

  }

  object Task {

    def apply(taskName: TaskName)(implicit config: Config): Function[Yaml, ValidatedTask] = {
      availableTaskCreators
        .collectFirst({
          case taskCreator if taskCreator.supportedTaskNames contains taskName =>
            { yaml: Yaml =>
              taskCreator.createTask(config)(yaml)(Map())
            }
        })
        .getOrElse({ yaml: Yaml =>
          invalidNel[Cause, Task](s"${taskName} does not exists")
        })
    }

    def dependsOf(tasks: Seq[Task]): Seq[DependencyName] = {
      tasks.foldLeft(Seq[DependencyName]())(_ ++ _.dependsOf)
    }

    def generateStatements(tasks: Seq[Task]): PartialFunction[Distro, Seq[Statement]] = {
      case distro: Distro =>
        tasks
          .map(_.generateStatements(distro))
          .flatten
    }

    def evaluate(yaml: Seq[((TaskName, Yaml), Vars)])(implicit config: Config): ValidatedTasks = {
      println(yaml)
      //println(s"yaml=${yaml}")
      yaml
        .map({ case ((taskName, yaml), vars) =>
          (Task(taskName), (yaml, vars))
        })
        .toList
        .traverse({ case (taskCreator, (yaml, vars)) =>
          taskCreator(yaml)
        })
    }

    private def renderTemplates(yaml: Yaml, vars: Vars): Yaml = {
      val jinjava = new Jinjava
      val context = vars.asJava
      yaml match {
        case yamlAsString: String =>
          jinjava.render(yamlAsString, context)

        case yamlAsMap: Map[String, AnyRef] =>
          yamlAsMap.mapValues(renderTemplates(_, vars))

        case yamlAsSeq: Seq[String] =>
          yamlAsSeq.map(renderTemplates(_, vars))

        case yaml =>
          yaml
      }

    }

  }

  def availableTaskCreators: Seq[TaskCreator] = Seq(
    CopyFile,
    DownloadFile,
    Echo,
    Include,
    InstallDependencies,
    ManageFile,
    Shell
  )

}

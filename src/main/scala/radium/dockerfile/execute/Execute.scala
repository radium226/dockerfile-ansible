package radium.dockerfile.execute

import cats.Traverse
import radium.dockerfile.{Distro, statement}
import radium.dockerfile.statement._
import radium.dockerfile.task._
import cats.data.{Validated, ValidatedNel}
import cats.implicits._
import radium.dockerfile.implicits._

trait Execute extends GenerateStatements with GenerateFiles {

  trait No[T] {

    def empty: T

  }

  implicit val emptyFileSpecs = new No[Seq[FileSpec]] {

    override def empty: Seq[FileSpec] = Seq()

  }

  def no[T : No]: PartialFunction[Distro, T] = { case _ => implicitly[No[T]].empty }

  def generic[T](block: => T): PartialFunction[Distro, T] = {
    case _ => block
  }

  def single[T](f: PartialFunction[Distro, T]): PartialFunction[Distro, Seq[T]] = f andThen { Seq(_) }

}

object Execute {

  def availableExecuteParsers = Seq[ExecuteParser with Keyed](Java)

  def parse(yaml: Yaml): ValidatedNel[Cause, Seq[Execute]] = yaml match {
    case command: String =>
      Seq(Default(command)).validNel[Cause]

    case keyedYaml: Map[String, AnyRef] =>
      val l = keyedYaml
        .map({ case (keyName, yaml) =>
          availableExecuteParsers
            .collectFirst({
              case parser if parser.keyName == keyName =>
                parser
            })
            .toValidNel(s"No execute matching the ${keyName} key was found")
            .andThen(_.parse(yaml))
        })
        .toList
      Traverse[List].sequence(l)
  }


}

case class Default(command: Command) extends Execute {

  override def provideFileSpecs = no

  override def generateStatements = generic {
    Seq(CommandStatement(command))
  }

}

object Default extends ExecuteParser {

  import ValueParser._

  def command = Arg.whole[Command].required

  override def parse(yaml: Yaml): ValidatedNel[Cause, Execute] = {
    command.parse(yaml).map(Default.apply)
  }

}



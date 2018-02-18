package radium.dockerfile.execute

import cats.Traverse

import radium.dockerfile._
import radium.dockerfile.yaml._
import radium.dockerfile.arg._
import radium.dockerfile.transpilation._
import radium.dockerfile.{ statement => s }
import radium.dockerfile.implicits._

trait Execute extends GenerateStatements with GenerateFiles

object Execute {

  def availableParsers = Seq[ExecuteParser with Keyed](Java, Shell)

  def parse(config: Config): (Yaml, Vars) => Validated[Seq[Execute]] = {
    case (Some(yaml: Yaml), vars) =>
      parse(config)(yaml, vars)

    case (command: String, vars) =>
      Seq(Default(command)).valid

    case (command: Seq[String], vars) =>
      Seq(Default(command)).valid

    case (keyedYaml: Map[String, AnyRef], vars) =>
     keyedYaml
      .map({ case (keyName, yaml) =>
        availableParsers
          .collectFirst({
            case parser if parser.keyName == keyName =>
              parser
          })
          .toValidated(s"No execute matching the ${keyName} key was found")
          .andThen(_.parse(config)(yaml, vars))
      })
      .toSeq
      .traverse
  }


}

case class Default(command: Command) extends Execute {

  override def provideFileSpecs = no

  override def generateStatements = generic {
    Seq(s.Command(command))
  }

}

object Default extends ExecuteParser {

  def command = Arg.whole[Command].required

  override def parse(config: Config) = { (yaml, vars) =>
    command.parse(yaml).map(Default.apply)
  }

}



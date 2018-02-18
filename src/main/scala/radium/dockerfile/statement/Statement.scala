package radium.dockerfile.statement

import java.lang.System.lineSeparator
import java.nio.file.Path

import radium.dockerfile.{Distro, Source}

import radium.{ dockerfile => d }
import d.implicits._
import radium.dockerfile.transpilation.GenerateStatements

sealed trait Statement {

  def quote(text: String): String = {
    if (text.contains(" ")) s""""${text}""""
    else text
  }

  def toSource(): Source = {

    s"${directiveName} ${directiveValue.toSource}"
  }

  def directiveName: DirectiveName

  def directiveValue: DirectiveValue

}

object Statement {

  private def merge(statements: Seq[Statement]): Seq[Statement] = {
    statements
      .foldLeft(Seq[Statement]()) { (mergedStatements, statement) =>
        (mergedStatements.lastOption, statement) match {
          case Mergeable(mergedStatement) =>
            mergedStatements.dropRight(1) :+ mergedStatement
          case _ =>
            mergedStatements :+ statement
        }
      }
  }

  def optimize(statements: Seq[Statement]): Seq[Statement] = {
    merge(statements)
  }

  def generateStatements[T](elements: Seq[T]): Distro => Seq[Statement] = { distro: Distro =>
    elements
      .collect({
        case element: GenerateStatements => element
      })
      .flatMap(_.generateStatements(distro))
  }

  def toSource(statements: Seq[Statement]): Source = {
    optimize(statements)
        .map(_.toSource)
        .mkString(s"${lineSeparator}${lineSeparator}")
  }

}

case class Run(val commands: Seq[d.Command]) extends Statement with Mergeable[Run] {

  override def directiveName = "RUN"

  def mergeWith(runStatement: Run) = Run(commands ++ runStatement.commands)

  override def directiveValue = Left(commands
      .map(_.fold(identity, _.map(quote).mkString(" ")))
      .map({ command =>
        s"    ${command}"
      })
      .mkString(s" \\${lineSeparator}")
      .trim())

}

case class Copy(val localFilePath: Path, val remoteFilePath: Path) extends Statement {

  override def directiveName: DirectiveName = "COPY"

  override def directiveValue: DirectiveValue = Right(Seq(localFilePath.toString, remoteFilePath.toString))

}

object Run {

  def single(command: d.Command): Run = Run(Seq(command))

  def multiple(commands: Seq[d.Command]): Run = Run(commands)

}

case class Env(val variableName: String, val variableValue: String) extends Statement {

  override def directiveName = "ENV"

  override def directiveValue = Left(s"""${variableName}="${variableValue}"""")

}

case class EntryPoint(val command: d.Command) extends Statement {

  override def directiveName: DirectiveName = "ENTRYPOINT"

  override def directiveValue: DirectiveValue = command

}

case class Command(val command: d.Command) extends Statement {

  override def directiveName: DirectiveName = "CMD"

  override def directiveValue: DirectiveValue = command

}


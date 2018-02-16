package radium.dockerfile

import java.lang.System.lineSeparator
import java.nio.file.Path

package object statement {

  type DirectiveName = String

  type DirectiveValue = Either[String, Seq[String]]

  type Command = Either[String, Seq[String]]

  sealed trait Statement {

    def quote(text: String): String = s""""${text}""""

    def toSource(): Source = {

      val directiveValueSource = directiveValue.fold(identity, _.map(quote).mkString("[", ", ", "]"))

      s"${directiveName} ${directiveValueSource}"
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

    def toSource(statements: Seq[Statement]): Source = {
      optimize(statements)
          .map(_.toSource)
          .mkString(s"${lineSeparator}${lineSeparator}")
    }

  }

  trait Mergeable[T <: Mergeable[T]] {
    self: T =>

    def mergeWith(task: T): T

  }

  object Mergeable {

    private def tryToMerge[L <: Statement, R <: Statement](one: L, other: R): Either[L, (L, R)] = {
      (one, other) match {
        case (one: L @unchecked with Mergeable[L @unchecked], other: L @unchecked with Mergeable[L @unchecked]) if one.getClass == other.getClass =>
          Left(one.mergeWith(other))
        case _ =>
          Right((one, other))
      }
    }

    def unapply(tuple: (Option[Statement], Statement)): Option[(Statement)] = tuple match {
      case (Some(lastStatement), statement) =>
        tryToMerge(lastStatement, statement) match {
          case Left(mergedStatement) =>
            Some(mergedStatement)
          case _ =>
            None
        }
      case _ =>
        None
    }

  }

  case class RunStatement(val commands: Seq[Command]) extends Statement with Mergeable[RunStatement] {

    override def directiveName = "RUN"

    def mergeWith(runStatement: RunStatement) = RunStatement(commands ++ runStatement.commands)

    override def directiveValue = Left(commands
        .map({ command =>
          s"    ${command}"
        })
        .mkString(s" \\${lineSeparator}")
        .trim())

  }

  case class CopyStatement(val localFilePath: Path, val remoteFilePath: Path) extends Statement {

    override def directiveName: DirectiveName = "COPY"

    override def directiveValue: DirectiveValue = Right(Seq(localFilePath.toString, remoteFilePath.toString))

  }

  object RunStatement {

    def apply(command: Command): RunStatement = RunStatement(Seq(command))

  }

  case class EnvStatement(val variableName: String, val variableValue: String) extends Statement {

    override def directiveName = "ENV"

    override def directiveValue = Left(s"""${variableName}="${variableValue}"""")

  }

  case class EntryPointStatement(val command: Command) extends Statement {

    override def directiveName: DirectiveName = "ENTRYPOINT"

    override def directiveValue: DirectiveValue = command

  }

  case class CommandStatement(val command: Command) extends Statement {

    override def directiveName: DirectiveName = "CMD"

    override def directiveValue: DirectiveValue = command

  }

}

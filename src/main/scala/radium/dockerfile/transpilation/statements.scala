package radium.dockerfile.transpilation

import radium.{ dockerfile => d }
import d.Distro
import radium.dockerfile.statement.{ Run, Statement }

trait GenerateStatements {

  def generateStatements: Distro => Seq[Statement]

}

trait GenerateStatement extends GenerateStatements {

  override def generateStatements: Distro => Seq[Statement] = generateStatement andThen { Seq(_) }

  def generateStatement: Distro => Statement

}

trait GenericStatement {
  self: GenerateStatement =>

  override def generateStatement: Function[Distro, Statement] = {
    case _ => statement
  }

  def statement: Statement

}

trait GenericStatements {
  self: GenerateStatements =>

  override def generateStatements: Distro => Seq[Statement] = {
    case _ => statements
  }

  def statements: Seq[Statement]

}

trait GenerateGenericStatements extends GenerateStatements with GenericStatements

trait GenerateGenericStatement extends GenerateStatement with GenericStatement

trait GenerateRunStatement extends GenerateStatement {

  override def generateStatement: Function[Distro, Statement] = provideCommand andThen { Run.single(_) }

  def provideCommand: Distro => d.Command

}

package radium.dockerfile.task

import radium.dockerfile._
import radium.dockerfile.yaml._

import radium.dockerfile.{ statement => s }
import radium.dockerfile.implicits._
import radium.dockerfile.transpilation._

case class Echo(val message: String) extends Task with GenerateRunStatement {

  override def provideCommand = generic {
    s"echo '${message}'"
  }

}

object Echo extends TaskParser {

  override def supportedTaskNames: Seq[TaskName] = Seq("echo")

  def message = Binding.whole[String].required

  override def parse(config: Config) = expandVars { yaml =>
    message.bind(yaml).map(Echo.apply)
  }
}

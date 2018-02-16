package radium.dockerfile.task

import radium.dockerfile.Config
import radium.dockerfile.statement._
import radium.dockerfile.implicits._

case class Echo(val message: String) extends Task with GenerateRunStatement with GenericCommand {

  override def command: Command = s"echo '${message}'"

}

object Echo extends TaskCreator {

  override def supportedTaskNames: Seq[TaskName] = Seq("echo")

  def message = arg[String].required

  override def createTask(implicit config: Config) = renderedTemplates { yaml =>
    message.parse(yaml).map(Echo.apply)
  }
}

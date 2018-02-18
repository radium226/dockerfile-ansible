package radium.dockerfile.task

import radium.dockerfile._
import radium.dockerfile.arg._
import radium.dockerfile.yaml._
import radium.dockerfile.implicits._

trait TaskParser extends Parser[Task] with ValueParserImplicits {

  /*def renderedTemplates(f: Yaml => Validated[Task]): Yaml => Vars => Validated[Task] = { yaml: Yaml =>
  { vars: Vars =>
    f(yaml.renderTemplates(vars))
  }
  }*/

  def supportedTaskNames: Seq[TaskName]

}

object TaskParser {

  def byName(taskName: TaskName): Option[TaskParser] = {
    Task.availableParsers
      .collectFirst({
        case parser if parser.supportedTaskNames contains taskName =>
          parser
      })
  }

}

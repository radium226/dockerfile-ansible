package radium.dockerfile.execute

import radium.dockerfile._
import radium.dockerfile.arg._
import radium.dockerfile.yaml._
import radium.dockerfile.transpilation._
import radium.dockerfile.implicits._
import radium.dockerfile.{ statement => s }

case class Shell(source: Source) extends Execute {

  override def provideFileSpecs = generic {
    Seq(FileSpec("execute.sh", source))
  }

  override def generateStatements = generic {
    Seq(
      s.Copy("execute.sh", "/execute.sh"),
      s.Run(Seq("chmod", "+x", "/execute.sh")),
      s.Command(Seq("/execute.sh"))
    )
  }
}

object Shell extends ExecuteParser with Keyed {

  override def keyName: String = "shell"

  def content = Arg.whole[Source].required

  override def parse(config: Config) = expandVars { yaml =>
    content.parse(yaml).map(Shell.apply)
  }

}

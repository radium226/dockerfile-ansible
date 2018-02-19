package radium.dockerfile

import radium.PathImplicits
import radium.dockerfile.transpilation.FileSpecImplicits
import radium.dockerfile.yaml.{Implicits => YamlImplicits}

trait CommandImplicits {

  implicit def stringSeqToCommand(stringSeq: Seq[String]): Command = Right(stringSeq)

  implicit def stringToCommand(string: String): Command = Left(string)

  implicit class EnrichedCommand(command: Command) {

    private def quote(text: String): String = s""""${text}""""


    def toSource: String = {
      command.fold(identity, _.map(quote).mkString("[", ", ", "]"))
    }

  }

}

object implicits extends YamlImplicits with CommandImplicits with PathImplicits with ConfigImplicits with FileSpecImplicits with Syntax
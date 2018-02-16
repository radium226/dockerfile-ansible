package radium.dockerfile

import radium.dockerfile.statement._

trait CommandImplicits {

  implicit def stringSeqToCommand(stringSeq: Seq[String]): Command = Right(stringSeq)

  implicit def stringToCommand(string: String): Command = Left(string)

}

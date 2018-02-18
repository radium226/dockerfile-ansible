package radium.dockerfile.execute

import radium.dockerfile._
import radium.dockerfile.yaml.ValueParserImplicits

trait Keyed {

  def keyName: String // TODO Type that!

}

trait ExecuteParser extends Parser[Execute] with ValueParserImplicits
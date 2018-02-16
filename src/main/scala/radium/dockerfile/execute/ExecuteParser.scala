package radium.dockerfile.execute

import cats.data.ValidatedNel
import radium.dockerfile.task.{Cause, Yaml}

trait Parser[T] {

  def parse(yaml: Yaml): ValidatedNel[Cause, T]

}

trait Keyed {

  def keyName: String

}

trait ExecuteParser extends Parser[Execute] {

}

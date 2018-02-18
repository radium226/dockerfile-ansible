package radium.dockerfile

import radium.dockerfile._
import radium.dockerfile.yaml._
import radium.dockerfile.implicits._

object Vars extends Parser[Vars] {

  override def parse(config: Config) = validate("Unable to parse vars") {
    case (Some(vars: Vars), otherVars) =>
      otherVars ++ vars

    case (None, vars) =>
      vars

    case (vars: Vars, otherVars) =>
      otherVars ++ vars
  }

  def empty: Vars = Map()

}

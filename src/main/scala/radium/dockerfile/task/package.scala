package radium.dockerfile

import cats.Traverse
import com.hubspot.jinjava.Jinjava
import radium.dockerfile._

import radium.dockerfile.yaml._
import radium.dockerfile.implicits._
import radium.dockerfile.statement._
import radium.dockerfile.transpilation._

import scala.collection.JavaConverters._

package object task {

  type Mode = String // TODO

  type State = String // TODO

  type User = String // TODO

  type DependencyName = String // TODO

  type TaskName = String

}

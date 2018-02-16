package radium.dockerfile

import java.nio.file.Path

import cats.data.Validated.{Invalid, Valid}
import radium.dockerfile.implicits._

object Transpile extends App {

  val yamlFilePath: Path = "/home/groupevsc.com/adrien_besnard/Projects/Ongoing/Others/dockerfile-yaml/src/test/resources/examples/Dockerfile.yml"

  implicit val config = Config(Seq("/home/groupevsc.com/adrien_besnard/Projects/Ongoing/Others/dockerfile-yaml/src/test/resources/examples"))
  Dockerfile.parse(yamlFilePath).map(Dockerfile.transpile) match {
    case Valid(fileSpecs) =>
      fileSpecs.foreach({ fileSpec =>
        println(s"${fileSpec.path} : ${fileSpec.content}")
      })
    case Invalid(causes) =>
      println(s"Something went wrong: ")
      causes.toList.foreach({ cause =>
        println(s" - ${cause}")
      })
  }
}

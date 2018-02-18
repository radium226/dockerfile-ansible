package radium.dockerfile

import java.nio.file.Path

import radium.dockerfile._
import radium.dockerfile.yaml._
import radium.dockerfile.implicits._

object Transpile extends App {

  val filePath: Path = "/home/adrien/Personal/Projects/dockerfile-yaml/src/test/resources/examples/Dockerfile.yml"

  val config = Config(Seq("/home/adrien/Personal/Projects/dockerfile-yaml/src/test/resources/examples"))

  val vars = Map[String, AnyRef]()

  Yaml.parse(filePath) andThen { Dockerfile.parse(config)(_, vars) } map Dockerfile.transpile(config) match {
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

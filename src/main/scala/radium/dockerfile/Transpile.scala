package radium.dockerfile

import java.nio.file.Path

import radium.dockerfile.implicits._

object Transpile extends App {

  val yamlFilePath: Path = "/home/adrien/Personal/Projects/dockerfile-yaml/src/test/resources/examples/Dockerfile.yml"

  implicit val config = Config(Seq("/home/adrien/Personal/Projects/dockerfile-yaml/src/test/resources/examples"))
  val source = Dockerfile.parse(yamlFilePath).map(Dockerfile.transpile)
  println(source)
}

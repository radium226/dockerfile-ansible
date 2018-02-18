package radium.dockerfile

import java.nio.file.{Path, Paths}

import radium.dockerfile._
import radium.dockerfile.yaml._
import radium.dockerfile.implicits._
import com.typesafe.config.ConfigFactory
import org.rogach.scallop.{ScallopConf, ValueConverter}

object Transpile extends App {

  class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {

    implicit val pathValueConverter: ValueConverter[Path] = implicitly[ValueConverter[String]]
        .map({ Paths.get(_) })

    implicit val configValueConverter: ValueConverter[Config] = implicitly[ValueConverter[Path]]
      .map(_.toFile)
      .map(ConfigFactory.parseFile)
      .map(typesafeConfigToConfig)

    val config = opt[Config](name = "config", default = Some(ConfigFactory.load()))

    val vars = propsLong[String](name = "var")

    val inputFilePath = trailArg[Path](descr = "Input Dockerfile")

    val outputFolderPath = trailArg[Path](descr = "Output folder")

    verify()

  }


  val conf = new Conf(args)

  val inputFilePath = conf.inputFilePath()

  val outputFolderPath = conf.outputFolderPath()

  val config = conf.config().withIncludedFolderPath(inputFilePath.getParent)

  val vars = conf.vars

  Yaml.parse(inputFilePath) andThen { Dockerfile.parse(config)(_, vars) } map Dockerfile.transpile(config) match {
    case Valid(fileSpecs) =>
      fileSpecs.writeTo(outputFolderPath)

    case Invalid(causes) =>
      println(s"Things went wrong: ")
      causes.toList.foreach({ cause =>
        println(s" - ${cause}")
      })
  }

}

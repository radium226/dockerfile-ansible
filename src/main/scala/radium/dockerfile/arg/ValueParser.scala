package radium.dockerfile.arg

import java.net.URL
import java.nio.file.{Path, Paths}

import radium.dockerfile._
import radium.dockerfile.yaml._
import radium.dockerfile.implicits._

trait ValueParser[T] {

  def parseValue(yaml: Yaml): Validated[T]

}

trait ValueParserImplicits {

  implicit val valueParserString = new ValueParser[String] {

    override def parseValue(yaml: Yaml): Validated[String] = yaml.toString.valid

  }

  implicit val valueParserPath = new ValueParser[Path] {

    override def parseValue(yaml: Yaml): Validated[Path] = Paths.get(yaml.toString).valid

  }

  implicit val valueParserUrl = new ValueParser[URL] {

    override def parseValue(yaml: Yaml): Validated[URL] = new URL(yaml.toString).valid

  }

  implicit val valueParserCommand = new ValueParser[Command] {

    override def parseValue(yaml: Yaml): Validated[Command] = yaml match {
      case commandAsString: String =>
        Left(commandAsString).valid

      case commandAsSeq: Seq[String] =>
        Right(commandAsSeq).valid

      case _ =>
        "Unable to parse command".invalid
    }

  }

}

object ValueParser extends ValueParserImplicits

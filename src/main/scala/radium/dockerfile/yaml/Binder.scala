package radium.dockerfile.yaml

import java.net.URL
import java.nio.file.{Path, Paths}

import radium.dockerfile._
import radium.dockerfile.implicits._

trait Binder[T] {

  def bind(yaml: Yaml): Validated[T]

}

trait ValueParserImplicits {

  implicit val valueParserString = new Binder[String] {

    override def bind(yaml: Yaml): Validated[String] = yaml.toString.valid

  }

  implicit val valueParserPath = new Binder[Path] {

    override def bind(yaml: Yaml): Validated[Path] = Paths.get(yaml.toString).valid

  }

  implicit val valueParserUrl = new Binder[URL] {

    override def bind(yaml: Yaml): Validated[URL] = new URL(yaml.toString).valid

  }

  implicit val valueParserCommand = new Binder[Command] {

    override def bind(yaml: Yaml): Validated[Command] = yaml match {
      case commandAsString: String =>
        Left(commandAsString).valid

      case commandAsSeq: Seq[String] =>
        Right(commandAsSeq).valid

      case _ =>
        "Unable to parse command".invalid
    }

  }

}

object Binder extends ValueParserImplicits

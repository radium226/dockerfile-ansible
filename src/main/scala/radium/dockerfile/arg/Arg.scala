package radium.dockerfile.arg

import radium.dockerfile._
import radium.dockerfile.yaml._
import radium.dockerfile.implicits._

sealed trait Arg[T] {

  def parse(anyRef: AnyRef): Validated[T]

}

object Arg {

  private def doParse[T:ValueParser](anyRef: AnyRef): Validated[T] = {
    implicitly[ValueParser[T]].parseValue(anyRef)
  }

  sealed trait Optional[T] extends Arg[Option[T]] {

    def required: Required[T]

  }

  object Optional {

    case class Whole[T: ValueParser]() extends Optional[T] {

      override def parse(yaml: Yaml): Validated[Option[T]] = {
        val validated = doParse[T](yaml)
        validated.fold({ _ => None }, { anyRef => Some(anyRef) }).valid
      }

      override def required: Required[T] = Required.Whole[T]()
    }

    case class ByKey[T: ValueParser](key: Key) extends Optional[T] {
      override def parse(yaml: Yaml): Validated[Option[T]] = yaml match {
        case map: Map[Key, AnyRef] =>
          map.get(key) match {
            case Some(anyRef) =>
              val validated = doParse[T](anyRef)
              validated.fold({ _ => None }, { anyRef => Some(anyRef) }).valid
            case None =>
              None.valid

          }
        case _ =>
          None.valid
      }

      override def required: Required[T] = Required.ByKey[T](key)
    }
  }

  sealed trait Required[T] extends Arg[T] {

    import Required._

    def transform[U](f: Function[T, Validated[U]]): Required[U] = Transformed[T, U](this, f)

  }

  object Required {

    case class Transformed[T, U](arg: Required[T], f: Function[T, Validated[U]]) extends Required[U] {

      override def parse(yaml: Yaml): Validated[U] = {
        arg.parse(yaml) andThen f
      }

    }

    case class Whole[T: ValueParser]() extends Required[T] {

      override def parse(yaml: Yaml): Validated[T] = doParse[T](yaml)

    }

    case class ByKey[T: ValueParser](key: Key) extends Required[T] {
      override def parse(yaml: Yaml): Validated[T] = yaml match {
        case map: Map[Key, AnyRef] =>
          map.get(key) match {
            case Some(anyRef) =>
              doParse[T](anyRef)
            case None =>
              s"The ${key} key was not found".invalid

          }
        case _ =>
          "Only map can be used by key".invalid
      }
    }

  }

  def whole[T:ValueParser]: Optional[T] = Optional.Whole[T]()

  def byKey[T:ValueParser](key: Key): Optional[T] = Optional.ByKey[T](key)

}
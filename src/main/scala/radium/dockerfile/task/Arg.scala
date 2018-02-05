package radium.dockerfile.task
import cats.data._
import cats.data.Validated._
import cats.implicits._

sealed trait Arg[T] {

  def parse(anyRef: AnyRef): ValidatedNel[Cause, T]

}

object Arg {

  private def doParse[T:ValueParser](anyRef: AnyRef): ValidatedNel[Cause, T] = {
    implicitly[ValueParser[T]].parseValue(anyRef)
  }

  sealed trait Optional[T] extends Arg[Option[T]] {

    def required: Required[T]

  }

  object Optional {

    case class Whole[T: ValueParser]() extends Optional[T] {

      override def parse(yaml: Yaml): ValidatedValue[Option[T]] = {
        val validated = doParse[T](yaml)
        validated.fold({ _ => none[T] }, { anyRef => Some(anyRef) }).validNel[Cause]
      }

      override def required: Required[T] = Required.Whole[T]()
    }

    case class ByKey[T: ValueParser](key: Key) extends Optional[T] {
      override def parse(yaml: Yaml): ValidatedValue[Option[T]] = yaml match {
        case map: Map[Key, AnyRef] =>
          map.get(key) match {
            case Some(anyRef) =>
              val validated = doParse[T](anyRef)
              validated.fold({ _ => none[T] }, { anyRef => Some(anyRef) }).validNel[Cause]
            case None =>
              None.validNel[Cause]

          }
        case _ =>
          None.validNel[Cause]
      }

      override def required: Required[T] = Required.ByKey[T](key)
    }
  }

  sealed trait Required[T] extends Arg[T] {

    import Required._

    def transform[U](f: Function[T, ValidatedValue[U]]): Required[U] = Transformed[T, U](this, f)

  }

  object Required {

    case class Transformed[T, U](arg: Required[T], f: Function[T, ValidatedValue[U]]) extends Required[U] {

      override def parse(yaml: Yaml): ValidatedValue[U] = {
        println(s"yaml = ${yaml}")
        arg.parse(yaml) andThen f
      }

    }

    case class Whole[T: ValueParser]() extends Required[T] {

      override def parse(yaml: Yaml): ValidatedValue[T] = doParse[T](yaml)

    }

    case class ByKey[T: ValueParser](key: Key) extends Required[T] {
      override def parse(yaml: Yaml): ValidatedValue[T] = yaml match {
        case map: Map[Key, AnyRef] =>
          map.get(key) match {
            case Some(anyRef) =>
              doParse[T](anyRef)
            case None =>
              s"The ${key} was not found".invalidNel[T]

          }
        case _ =>
          "Only map can be used by key".invalidNel[T]
      }
    }

  }

  def whole[T:ValueParser]: Optional[T] = Optional.Whole[T]()

  def byKey[T:ValueParser](key: Key): Optional[T] = Optional.ByKey[T](key)

}
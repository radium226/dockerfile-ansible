package radium.dockerfile.yaml

import radium.dockerfile._
import radium.dockerfile.implicits._

sealed trait Binding[T] {

  def bind(anyRef: AnyRef): Validated[T]

}

object Binding {

  private def doParse[T:Binder](anyRef: AnyRef): Validated[T] = {
    implicitly[Binder[T]].bind(anyRef)
  }

  sealed trait Optional[T] extends Binding[Option[T]] {

    def required: Required[T]

  }

  object Optional {

    case class Whole[T: Binder]() extends Optional[T] {

      override def bind(yaml: Yaml): Validated[Option[T]] = {
        val validated = doParse[T](yaml)
        validated.fold({ _ => None }, { anyRef => Some(anyRef) }).valid
      }

      override def required: Required[T] = Required.Whole[T]()
    }

    case class ByKey[T: Binder](key: Key) extends Optional[T] {
      override def bind(yaml: Yaml): Validated[Option[T]] = yaml match {
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

  sealed trait Required[T] extends Binding[T] {

    import Required._

    def transform[U](f: Function[T, Validated[U]]): Required[U] = Transformed[T, U](this, f)

  }

  object Required {

    case class Transformed[T, U](bind: Required[T], f: Function[T, Validated[U]]) extends Required[U] {

      override def bind(yaml: Yaml): Validated[U] = {
        bind.bind(yaml) andThen f
      }

    }

    case class Whole[T: Binder]() extends Required[T] {

      override def bind(yaml: Yaml): Validated[T] = doParse[T](yaml)

    }

    case class ByKey[T: Binder](key: Key) extends Required[T] {
      override def bind(yaml: Yaml): Validated[T] = yaml match {
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

  def whole[T:Binder]: Optional[T] = Optional.Whole[T]()

  def byKey[T:Binder](key: Key): Optional[T] = Optional.ByKey[T](key)

}
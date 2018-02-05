package radium.dockerfile.experimentation

import java.nio.file.{Path, Paths}

object PlayWithYaml extends App {

  trait ArgParser[O] {

    def parse(argValueAsString: String): Option[O]

  }

  implicit val pathArgParser = new ArgParser[Path] {

    def parse(argValueAsString: String): Option[Path] = {
      Some(Paths.get(argValueAsString))
    }

  }

  def arg[O : ArgParser]: Function[AnyRef, Option[O]] = {
    case argValueAsString: String =>
      val argValue = implicitly[ArgParser[O]].parse(argValueAsString)
      argValue

    case _: Map[String, AnyRef] =>
      None
  }

  def arg[O : ArgParser](argName: String): Function[AnyRef, Option[O]] = {
    case _: String =>
      None

    case argValueAsMap: Map[String, AnyRef] =>
      for {
        argValueAsAnyRef <- argValueAsMap.get(argName)
        argValueAsString <- argValueAsAnyRef match {
          case argValueAsString: String =>
            Some(argValueAsString)
          case _ =>
            None
        }
        argValue <- implicitly[ArgParser[O]].parse(argValueAsString)
      } yield argValue
  }


  val map1: AnyRef = "bouya"

  val map2: AnyRef = Map("toto" -> "tata")

  val arg1 = arg[Path]
  val arg2 = arg[Path]("toto")

  println(arg1(map1))
  println(arg1(map2))

  println(arg2(map1))
  println(arg2(map2))

}

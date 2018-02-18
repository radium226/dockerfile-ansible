package radium.dockerfile.yaml

import java.nio.file.{Files, Path}
import java.util.{ List => JavaList, Map => JavaMap }

import org.yaml.snakeyaml.{ Yaml => SnakeYaml }

import radium.dockerfile._
import radium.dockerfile.implicits._

import scala.collection.JavaConverters._

object Yaml {

  implicit class AnyRefWithAs(anyRef: AnyRef) {

    def as[T]: T = {
      (anyRef match {
        case null =>
          Map[String, AnyRef]() // TODO ?

        case javaList: JavaList[AnyRef] =>
          javaList.asScala.map({ anyRef =>
            anyRef.as[AnyRef]
          })

        case javaMap: JavaMap[String, AnyRef] =>
          Map(javaMap.asScala.toList: _*).mapValues({ anyRef =>
            anyRef.as[AnyRef]
          })

        case anyRef =>
          anyRef

      }).asInstanceOf[T]
    }
  }

  def parse(filePath: Path): Validated[Yaml] = {
    new SnakeYaml().load[AnyRef](Files.newInputStream(filePath)).as[Yaml].valid
  }

}

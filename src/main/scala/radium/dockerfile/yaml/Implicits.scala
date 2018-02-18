package radium.dockerfile.yaml

import com.hubspot.jinjava.Jinjava
import radium.dockerfile.{Validated, Vars}

import scala.collection.JavaConverters._

trait Implicits {

  implicit class YamlWithRender(yaml: Yaml) {

    private def renderTemplates(yaml: AnyRef, vars: Vars): AnyRef = {
      val jinjava = new Jinjava
      val context = vars.asJava
      yaml match {
        case yamlAsString: String =>
          jinjava.render(yamlAsString, context)

        case yamlAsMap: Map[String, AnyRef] =>
          yamlAsMap.mapValues(renderTemplates(_, vars))

        case yamlAsSeq: Seq[String] =>
          yamlAsSeq.map(renderTemplates(_, vars))

        case yaml =>
          yaml
      }
    }

    def renderTemplates(vars: Vars): Yaml = {
      renderTemplates(yaml, vars).asInstanceOf[Yaml]
    }

  }

  implicit class Tuple2WithBind[A, B](tuple2: (Binding[A], Binding[B])) {

    def parse(yaml: Yaml): (Validated[A], Validated[B]) = {

      (tuple2._1.bind(yaml), tuple2._2.bind(yaml))
    }

  }

  implicit class Tuple3WithBind[A, B, C](tuple3: (Binding[A], Binding[B], Binding[C])) {

    def bind(yaml: Yaml): (Validated[A], Validated[B], Validated[C]) = {
      (tuple3._1.bind(yaml), tuple3._2.bind(yaml), tuple3._3.bind(yaml))
    }

  }

  implicit class Tuple4WithBind[A, B, C, D](tuple4: (Binding[A], Binding[B], Binding[C], Binding[D])) {

    def parse(yaml: Yaml): (Validated[A], Validated[B], Validated[C], Validated[D]) = {
      (tuple4._1.bind(yaml), tuple4._2.bind(yaml), tuple4._3.bind(yaml), tuple4._4.bind(yaml))
    }

  }

}

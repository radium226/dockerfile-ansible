package radium.dockerfile.yaml

import com.hubspot.jinjava.Jinjava
import radium.dockerfile.Vars

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

}

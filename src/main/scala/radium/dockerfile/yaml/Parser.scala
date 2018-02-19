package radium.dockerfile.yaml

import com.hubspot.jinjava.Jinjava
import radium.dockerfile.{Config, Validated, Vars}
import scala.collection.JavaConverters._

trait Parser[T] {

  private def renderTemplates(yaml: Yaml, vars: Vars): Yaml = {
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

  def expandVars(f: Yaml => Validated[T]): (Yaml, Vars) => Validated[T] = { (yaml, vars) =>
    f(renderTemplates(yaml, vars))
  }

  def parse(config: Config): (Yaml, Vars) => Validated[T]

}

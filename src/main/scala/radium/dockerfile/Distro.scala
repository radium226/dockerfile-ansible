package radium.dockerfile

import radium.dockerfile.implicits._

sealed trait Distro {

  def name: String

}

object Distro extends Parser[Distro] {

  def availables = Seq(Ubuntu, Alpine)

  def byName(name: String): Option[Distro] = {
    availables.collectFirst({
      case distro if name contains distro.name =>
        distro
    })
  }

  override def parse(config: Config) = {
    case (Some(name: String), vars) =>
      byName(name).toValidated(s"Unable to find a distro named ${name}")

    case (name: String, vars) =>
      byName(name).toValidated(s"Unable to find a distro named ${name}")

    case _ =>
      "The YAML block has a bad format for distro".invalid
  }

}

case object Ubuntu extends Distro {

  def name = "ubuntu"

}

case object Alpine extends Distro {

  def name = "alpine"

}

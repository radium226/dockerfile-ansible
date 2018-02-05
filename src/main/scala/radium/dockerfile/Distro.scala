package radium.dockerfile

sealed trait Distro {

  def name: String

}

object Distro {

  def availables = Seq(Ubuntu, Alpine)

  def byName(name: String): Option[Distro] = {
    availables.collectFirst({
      case distro if name contains distro.name =>
        distro
    })
  }

}

case object Ubuntu extends Distro {

  def name = "ubuntu"

}

case object Alpine extends Distro {

  def name = "alpine"

}

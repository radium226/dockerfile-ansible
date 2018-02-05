package radium.dockerfile

import java.nio.file.{Path, Paths}

trait PathImplicits {

  implicit def stringToPath(string: String): Path = Paths.get(string)

}

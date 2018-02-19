package radium

import java.nio.file.{Path, Paths}

trait PathImplicits {

  implicit def stringToPath(string: String): Path = Paths.get(string)

}

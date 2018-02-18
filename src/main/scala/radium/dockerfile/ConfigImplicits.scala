package radium.dockerfile

import com.typesafe.config.{ Config => TypesafeConfig }
import scala.collection.JavaConverters._

import java.nio.file.Paths

object ConfigKeys {

  val IncludedFolderPath = "included-folders"

}

trait ConfigImplicits {

  implicit def typesafeConfigToConfig(typesafeConfig: TypesafeConfig): Config = {
    import ConfigKeys._
    val includeFolderPaths = typesafeConfig.getStringList(IncludedFolderPath).asScala.map({ Paths.get(_) })
    Config(includeFolderPaths)
  }

}

package radium.dockerfile

import java.nio.file.{Path, Paths}

import com.typesafe.config.{ Config => TypesafeConfig }

case class Config(includedFolderPaths: Seq[Path]) {

  def withIncludedFolderPath(includedFolderPath: Path): Config = {
    copy(includedFolderPath +: includedFolderPaths)
  }

}

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


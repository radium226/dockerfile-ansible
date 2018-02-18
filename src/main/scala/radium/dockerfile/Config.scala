package radium.dockerfile

import java.nio.file.Path

case class Config(includedFolderPaths: Seq[Path]) {

  def withIncludedFolderPath(includedFolderPath: Path): Config = {
    copy(includedFolderPath +: includedFolderPaths)
  }

}

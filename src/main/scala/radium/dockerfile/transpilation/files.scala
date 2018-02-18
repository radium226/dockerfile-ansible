package radium.dockerfile.transpilation

import java.nio.file.{Files, Path}

import radium.dockerfile._

case class FileSpec(val filePath: Path, val content: Content) {

  def writeTo(folderPath: Path): Unit = {
    Files.write(folderPath.resolve(filePath), content.getBytes)
  }

}

trait FileSpecImplicits {

  implicit class fileSpecSeqWithWriteTo(fileSpecs: Seq[FileSpec]) {

    def writeTo(folderPath: Path): Unit = {
      fileSpecs.foreach(_.writeTo(folderPath))
    }

  }

}

object FileSpec {

  def collectFileSpecs[T](seq: Seq[T]): Distro => Seq[FileSpec] = { distro: Distro =>
    seq
      .collect({
        case anyRef: T with GenerateFiles =>
          anyRef
      })
      .map(_.provideFileSpecs(distro))
      .flatten
  }

  def collectFileSpecs(dockerfile: Dockerfile): Distro => Seq[FileSpec] = { distro =>
    collectFileSpecs(dockerfile.tasks)(distro) ++ collectFileSpecs(dockerfile.executes)(distro)
  }

}

trait GenerateFiles {

  def provideFileSpecs: Distro => Seq[FileSpec]

}

trait GenerateFile extends GenerateFiles {

  override def provideFileSpecs = provideFileSpec andThen { Seq(_) }

  def provideFileSpec: Distro => FileSpec

}

trait GenerateGenericFile extends GenerateFile {

  def fileSpec: FileSpec

  override def provideFileSpec = { case _ => fileSpec }
}
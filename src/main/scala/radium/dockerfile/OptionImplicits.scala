package radium.dockerfile

trait OptionImplicits {

  implicit class OptionWithToSeq[T](option: Option[T]) {

    def toSeq(): Seq[T] = {
      option match {
        case Some(t) =>
          Seq(t)
        case None =>
          Seq()
      }
    }

  }

}

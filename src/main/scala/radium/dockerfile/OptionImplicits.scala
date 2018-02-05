package radium.dockerfile

trait OptionImplicits {

  class OptionWithToSeq[T](option: Option[T]) {

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

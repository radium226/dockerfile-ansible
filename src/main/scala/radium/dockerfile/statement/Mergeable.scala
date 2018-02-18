package radium.dockerfile.statement

trait Mergeable[T <: Mergeable[T]] {
  self: T =>

  def mergeWith(task: T): T

}

object Mergeable {

  private def tryToMerge[L <: Statement, R <: Statement](one: L, other: R): Either[L, (L, R)] = {
    (one, other) match {
      case (one: L @unchecked with Mergeable[L @unchecked], other: L @unchecked with Mergeable[L @unchecked]) if one.getClass == other.getClass =>
        Left(one.mergeWith(other))
      case _ =>
        Right((one, other))
    }
  }

  def unapply(tuple: (Option[Statement], Statement)): Option[(Statement)] = tuple match {
    case (Some(lastStatement), statement) =>
      tryToMerge(lastStatement, statement) match {
        case Left(mergedStatement) =>
          Some(mergedStatement)
        case _ =>
          None
      }
    case _ =>
      None
  }

}

package radium

import cats.{Traverse => CatsTraverse}
import cats.data.{Validated => CatsValidated, ValidatedNel => CatsValidatedNel}
import com.hubspot.jinjava.Jinjava
import radium.dockerfile.transpilation.FileSpec
import radium.dockerfile.yaml.Yaml
import scala.collection.JavaConverters._

package object dockerfile {

  type Command = Either[String, Seq[String]]

  type Source = String

  type Vars = Map[String, AnyRef]

  type Cause = String

  type Validated[T] = CatsValidatedNel[Cause, T]

  val Valid = CatsValidated.Valid
  val Invalid = CatsValidated.Invalid

  trait ValidatedSyntax {

    implicit def syntaxAnyInvalid(cause: Cause) = new AnyInvalidSyntax(cause)

    implicit def syntaxAnyValid[A](a: A) = new AnyValidSyntax[A](a)

    implicit def syntaxOptionToValidated[A](a: Option[A]) = new OptionToValidatedSyntax[A](a)

    implicit def syntaxValidatedSeqTraverse[A](a: Seq[Validated[A]]) = new ValidatedSeqTraverseSyntax(a)

  }

  final class AnyInvalidSyntax(val cause: Cause) extends AnyVal {

    def invalid[A]: Validated[A] = CatsValidated.invalidNel[Cause, A](cause)

  }

  final class AnyValidSyntax[A](val a: A) extends AnyVal {

    def valid: Validated[A] = CatsValidated.validNel(a)

  }

  final class ValidatedSeqTraverseSyntax[A](val s: Seq[Validated[A]]) extends AnyVal {

    def traverse: Validated[Seq[A]] = {
      import cats.implicits._
      CatsTraverse[List].sequence(s.toList)
    }

  }

  final class OptionToValidatedSyntax[A](val a: Option[A]) extends AnyVal {

    def toValidated(cause: Cause): Validated[A] = a match {
      case Some(a) =>
        CatsValidated.validNel(a)
      case None =>
        CatsValidated.invalidNel(cause)
    }

  }

  trait Syntax extends ValidatedSyntax


  trait Parser[T] {

    private def renderTemplates(yaml: Yaml, vars: Vars): Yaml = {
      val jinjava = new Jinjava
      val context = vars.asJava
      yaml match {
        case yamlAsString: String =>
          jinjava.render(yamlAsString, context)

        case yamlAsMap: Map[String, AnyRef] =>
          yamlAsMap.mapValues(renderTemplates(_, vars))

        case yamlAsSeq: Seq[String] =>
          yamlAsSeq.map(renderTemplates(_, vars))

        case yaml =>
          yaml
      }

    }

    def expandVars(f: Yaml => Validated[T]): (Yaml, Vars) => Validated[T] = { (yaml, vars) =>
      f(renderTemplates(yaml, vars))
    }

    def parse(config: Config): (Yaml, Vars) => Validated[T]

  }

  trait No[T] {

    def empty: T

  }

  implicit val emptyFileSpecs = new No[Seq[FileSpec]] {

    override def empty: Seq[FileSpec] = Seq()

  }

  def no[T : No]: Distro => T = { case _ => implicitly[No[T]].empty }

  def generic[T](block: => T): Distro => T = {
    case _ => block
  }

  def single[T](f: Distro => T): Distro => Seq[T] = f andThen { Seq(_) }

  def validate[A, B, C](cause: Cause)(pf: => PartialFunction[(A, B), C]): (A, B) => Validated[C] = { (a: A, b: B) =>
    if (pf.isDefinedAt((a, b))) CatsValidated.validNel(pf.apply((a, b)))
    else CatsValidated.invalidNel(cause)
  }

}

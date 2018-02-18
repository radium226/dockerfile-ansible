package radium.dockerfile

package object statement {

  type Directive = String

  type DirectiveName = String

  type DirectiveValue = Either[String, Seq[String]]

}

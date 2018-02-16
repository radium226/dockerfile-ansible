package radium.dockerfile.execute

object Test extends App {

  val yaml1 = "toto tata"

  println(Execute.parse(yaml1))

}

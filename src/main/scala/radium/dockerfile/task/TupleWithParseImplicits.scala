package radium.dockerfile.task

import cats.data.ValidatedNel

trait TupleWithParseImplicits {

  implicit class Tuple2WithParse[A, B](tuple2: (Arg[A], Arg[B])) {

    def parse(yaml: Yaml): (ValidatedNel[Cause, A], ValidatedNel[Cause, B]) = {
      (tuple2._1.parse(yaml), tuple2._2.parse(yaml))
    }

  }

  implicit class Tuple3WithParse[A, B, C](tuple3: (Arg[A], Arg[B], Arg[C])) {

    def parse(yaml: Yaml): (ValidatedNel[Cause, A], ValidatedNel[Cause, B], ValidatedNel[Cause, C]) = {
      (tuple3._1.parse(yaml), tuple3._2.parse(yaml), tuple3._3.parse(yaml))
    }

  }

  implicit class Tuple4WithParse[A, B, C, D](tuple4: (Arg[A], Arg[B], Arg[C], Arg[D])) {

    def parse(yaml: Yaml): (ValidatedNel[Cause, A], ValidatedNel[Cause, B], ValidatedNel[Cause, C], ValidatedNel[Cause, D]) = {
      (tuple4._1.parse(yaml), tuple4._2.parse(yaml), tuple4._3.parse(yaml), tuple4._4.parse(yaml))
    }

  }

}

object TupleWithParseImplicits extends TupleWithParseImplicits

package radium

import cats.data.ValidatedNel
import radium.dockerfile.task.Cause
import radium.dockerfile.task.Task

package object dockerfile {

  type Source = String

  type Vars = Map[String, AnyRef]

  type ValidatedDockerfile = ValidatedNel[Cause, Dockerfile]

  type ValidatedVars = ValidatedNel[Cause, Vars]

  type ValidatedDistro = ValidatedNel[Cause, Distro]

  type ValidatedTasks = ValidatedNel[Cause, Seq[Task]]

}

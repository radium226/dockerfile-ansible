package radium.dockerfile

import radium.dockerfile.task.TupleWithParseImplicits
import radium.dockerfile.transpilation.FileSpecImplicits
import radium.dockerfile.yaml.{Implicits => YamlImplicits}

object implicits extends TupleWithParseImplicits with YamlImplicits with CommandImplicits with PathImplicits with ConfigImplicits with FileSpecImplicits with Syntax
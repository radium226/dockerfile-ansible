package radium.dockerfile

import java.nio.file.{Path, Paths}

import radium.dockerfile.task.TupleWithParseImplicits
import radium.dockerfile.yaml.{ Implicits => YamlImplicits }

object implicits extends TupleWithParseImplicits with YamlImplicits with CommandImplicits with PathImplicits with Syntax
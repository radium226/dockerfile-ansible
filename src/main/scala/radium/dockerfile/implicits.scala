package radium.dockerfile

import radium.dockerfile.task.TupleWithParseImplicits
import radium.dockerfile.yaml.YamlImplicits

object implicits extends PathImplicits with OptionImplicits with TupleWithParseImplicits with YamlImplicits
package radium.dockerfile

import radium.dockerfile.transpilation.FileSpecImplicits
import radium.dockerfile.yaml.{Implicits => YamlImplicits}

object implicits extends YamlImplicits with CommandImplicits with PathImplicits with ConfigImplicits with FileSpecImplicits with Syntax
package com.jakemadethis.graphite.ui

/** Simply a list of derivations */
class GuiGrammar {
  var file : String = null
  var initialGraph : DerivationPair = null
  val derivations = collection.mutable.ListBuffer[DerivationPair]()
}
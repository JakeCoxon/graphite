package com.jakemadethis.graphite.graph


class Vertex(val label:String)
class Hyperedge(val label: String, val isTerminal:Boolean) {
  def isNonTerminal = !isTerminal
}
class Edge(override val label:String, override val isTerminal:Boolean) extends Hyperedge(label, isTerminal)


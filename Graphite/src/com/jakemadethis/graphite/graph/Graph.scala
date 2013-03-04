package com.jakemadethis.graphite.graph
import edu.uci.ics.jung.graph.Hypergraph

object TerminalEdge {
  def apply(label : String) = new Hyperedge(label, true)
} 
object NonTerminalEdge {
  def apply(label : String) = new Hyperedge(label, false)
} 
class Vertex(val label:String="") {
  def copy = new Vertex(label)
}
class Hyperedge(val label: String, val isTerminal:Boolean) {
  def isNonTerminal = !isTerminal
  def copy = new Hyperedge(label, isTerminal)
}
class Edge(override val label:String, override val isTerminal:Boolean) extends Hyperedge(label, isTerminal) {
  override def copy = new Edge(label, isTerminal)
}

//class ExtGraph[V,E](val graph : Hypergraph[V,E], val externalNodes : Seq[V])
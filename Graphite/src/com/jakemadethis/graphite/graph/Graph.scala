package com.jakemadethis.graphite.graph
import edu.uci.ics.jung.graph.Hypergraph

object Termination {
  def terminal(b : Boolean) = if (b) Terminal else NonTerminal
}
sealed class Termination {
  def isTerminal = this == Terminal
  def isNonTerminal = this == NonTerminal
}
object Terminal extends Termination
object NonTerminal extends Termination

class Vertex() {
  def copy = new Vertex()
}
class FakeVertex extends Vertex {
  override def copy = 
    throw new Error("Cannot copy FakeVertex")
}

object TerminalEdge {
  def apply(label : String) = new Hyperedge(label, Terminal)
}
object NonTerminalEdge {
  def apply(label : String) = new Hyperedge(label, NonTerminal)
}
class Hyperedge(val label: String, val termination : Termination) {
  def isTerminal = termination.isTerminal
  def isNonTerminal = termination.isNonTerminal
  def copy = new Hyperedge(label, termination)
}
class Edge(override val label:String, override val termination : Termination) extends Hyperedge(label, termination) {
  override def copy = new Edge(label, termination)
}

//class ExtGraph[V,E](val graph : Hypergraph[V,E], val externalNodes : Seq[V])
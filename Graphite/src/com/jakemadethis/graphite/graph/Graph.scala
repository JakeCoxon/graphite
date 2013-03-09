package com.jakemadethis.graphite.graph
import edu.uci.ics.jung.graph.Hypergraph

object Termination extends Enumeration {
  class Termination(name : String) extends Val(name) {
    def isTerminal = this == Terminal
    def isNonTerminal = this == NonTerminal
  }
  //type Termination = V
  
  def terminal(bool : Boolean) = bool match {
    case true => Terminal
    case false => NonTerminal
  }
  
  val Terminal = new Termination("Terminal")
  val NonTerminal = new Termination("NonTerminal")
  
}
class Vertex(val label:String) {
  def this() = this("")
  def copy = new Vertex(label)
}
class FakeVertex extends Vertex {}

object TerminalEdge {
  def apply(label : String) = new Hyperedge(label, Termination.Terminal)
}
object NonTerminalEdge {
  def apply(label : String) = new Hyperedge(label, Termination.NonTerminal)
}
class Hyperedge(val label: String, val termination : Termination.Termination) {
  def isTerminal = termination.isTerminal
  def isNonTerminal = termination.isNonTerminal
  def copy = new Hyperedge(label, termination)
}
class Edge(override val label:String, override val termination : Termination.Termination) extends Hyperedge(label, termination) {
  override def copy = new Edge(label, termination)
}

//class ExtGraph[V,E](val graph : Hypergraph[V,E], val externalNodes : Seq[V])
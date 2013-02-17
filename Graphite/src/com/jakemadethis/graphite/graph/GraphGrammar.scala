package com.jakemadethis.graphite.graph
import scala.collection.JavaConversions._
import com.jakemadethis.graphite._
import edu.uci.ics.jung.graph.Hypergraph

private object graphutil {
  def nonTerminalStrings(graph : Hypergraph[Vertex, Hyperedge]) = {
    graph.getEdges().filter(_.isNonTerminal).map(_.label).toList
  }
  def terminalSize(graph : Hypergraph[Vertex, Hyperedge]) = {
    graph.getEdges().count(_.isTerminal)
  }
}


class HypergraphDerivation(val graph : Hypergraph[Vertex, Hyperedge]) 
  extends Derivation(graphutil.nonTerminalStrings(graph), graphutil.terminalSize(graph))
  
class HypergraphGrammar(map : Map[String, Seq[HypergraphDerivation]]) extends StringGrammar(map)

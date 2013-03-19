package com.jakemadethis.graphite.algorithm
import scala.collection.JavaConversions._
import com.jakemadethis.graphite._
import edu.uci.ics.jung.graph.Hypergraph
import com.jakemadethis.graphite.algorithm.StringGrammar
import com.jakemadethis.graphite.algorithm.Generator
import com.jakemadethis.graphite.graph.Hyperedge
import com.jakemadethis.graphite.graph.Vertex

private object graphutil {
  def nonTerminalStrings(graph : Hypergraph[Vertex, Hyperedge]) = {
    graph.getEdges().filter(_.isNonTerminal).map(_.label).toList
  }
  def terminalSize(graph : Hypergraph[Vertex, Hyperedge], externalNodes : Seq[Vertex]) = {
    graph.getEdges().count(_.isTerminal) + graph.getVertexCount() - externalNodes.size
  }
}


class HypergraphDerivation(val graph : Hypergraph[Vertex, Hyperedge], 
    val externalNodes : Seq[Vertex], val label : String) 
  extends Derivation(graphutil.nonTerminalStrings(graph), graphutil.terminalSize(graph, externalNodes)) {
  def deriveType = externalNodes.size
}

object HypergraphGrammar {
  def apply(seq : HypergraphDerivation*) : HypergraphGrammar = apply(seq)
  def apply(seq : TraversableOnce[HypergraphDerivation]) = {
    val map = seq.foldLeft(Map[String, Seq[HypergraphDerivation]]()) { (result, a) => 
      result + (a.label -> (result.getOrElse(a.label, Seq()) :+ a))
    }
    new HypergraphGrammar(map)
  }
}
class HypergraphGrammar(map_ : Map[String, Seq[HypergraphDerivation]]) extends StringGrammar[HypergraphDerivation] {
  def map = map_
}

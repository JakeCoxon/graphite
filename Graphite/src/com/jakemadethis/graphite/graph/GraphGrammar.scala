package com.jakemadethis.graphite.graph
import scala.collection.JavaConversions._
import com.jakemadethis.graphite._
import edu.uci.ics.jung.graph.Hypergraph

private object graphutil {
  def nonTerminalStrings(ext : ExtGraph[Vertex, Hyperedge]) = {
    ext.graph.getEdges().filter(_.isNonTerminal).map(_.label).toList
  }
  def terminalSize(ext : ExtGraph[Vertex, Hyperedge]) = {
    ext.graph.getEdges().count(_.isTerminal) + ext.graph.getVertexCount() - ext.externalNodes.size
  }
}


class HypergraphDerivation(val ext : ExtGraph[Vertex, Hyperedge]) 
  extends Derivation(graphutil.nonTerminalStrings(ext), graphutil.terminalSize(ext)) {
  def graph = ext.graph
  def externalNodes = ext.externalNodes
}
  
class HypergraphGrammar(map : Map[String, Seq[HypergraphDerivation]]) extends StringGrammar(map)

class HypergraphGenerator(start : HypergraphDerivation, val graph : Hypergraph[Vertex, Hyperedge]) 
    extends Generator[String, HypergraphDerivation] {
  
  applyGraph(start, Seq())
  
  def derive(label : String, derivation : HypergraphDerivation) {
    val edge = getNonTerminalEdge(label).getOrElse(throw new Error("NT not found"))
    val incidents = new IterableWrapper(graph.getIncidentVertices(edge))
    
    val vs = graph.getIncidentVertices(edge)
    if (vs.size != derivation.externalNodes.size) throw new Error("Incorrect size")
    graph.removeEdge(edge)
    applyGraph(derivation, incidents)
  }
  
  private def applyGraph(derivation : HypergraphDerivation, incidents : Iterable[Vertex]) {
    val vmap = collection.mutable.Map[Vertex,Vertex]()
    derivation.externalNodes.zip(incidents).foreach { tuple => 
      vmap += tuple
    }
    
    derivation.graph.getVertices().filterNot(vmap.contains(_)).foreach { v =>
      val copy = v.copy
      graph.addVertex(copy)
      vmap += (v -> copy)
    }
    
    derivation.graph.getEdges().foreach { e => 
      val copy = e.copy
      val vs = derivation.graph.getIncidentVertices(e).map(vmap(_))
      graph.addEdge(copy, vs)
    }
    
  }
  
  private def getNonTerminalEdge(label : String) = {
    graph.getEdges().find(_.label == label)
  }
  
}
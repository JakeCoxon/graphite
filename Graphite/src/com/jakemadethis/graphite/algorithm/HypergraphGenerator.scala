package com.jakemadethis.graphite.algorithm

import edu.uci.ics.jung.graph._
import com.jakemadethis.graphite.graph._
import collection.JavaConversions._


class HypergraphGenerator(start : HypergraphDerivation, val graph : Hypergraph[Vertex, Hyperedge]) 
    extends Generator[String, HypergraphDerivation] {
  
  applyGraph(start, Seq())
  
  def derive(label : String, derivation : HypergraphDerivation) {
    val edge = getNonTerminalEdge(label).getOrElse(throw new Error("NT not found"))
    val incidents = new IterableWrapper(graph.getIncidentVertices(edge))
    
    val vs = graph.getIncidentVertices(edge)
    if (vs.size != derivation.deriveType) throw new Error("Hypergraph type does not match hyperedge type")
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
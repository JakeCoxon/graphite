package com.jakemadethis.graphite.algorithm

import edu.uci.ics.jung.graph._
import com.jakemadethis.graphite.graph._
import collection.JavaConversions._
import com.jakemadethis.util.OptionIf._

object HypergraphGenerator {
  
  def apply(graph : Hypergraph[Vertex, Hyperedge], path : Derivation.Path[HypergraphDerivation]) = {
    val startGraph = applyToGraph(graph, path.head, Seq())
    path.tail.foldLeft(graph) { case (graph, derivation) => 
  
      val edge = getNonTerminalEdge(graph, derivation.label).getOrElse {
        val nts = graph.getEdges.filter(_.isNonTerminal).map(_.label).optionIf(_.size > 0).
            map { list => "Available nonterminals are "+list.mkString(",") }.
            getOrElse("No available nonterminals")
            
        throw new Error("Nonterminal '"+derivation.label+"' not found. "+nts)
      }
      val incidents = new IterableWrapper(graph.getIncidentVertices(edge))
      
      val vs = graph.getIncidentVertices(edge)
      if (vs.size != derivation.deriveType) throw new Error("Hypergraph type does not match hyperedge type")
      graph.removeEdge(edge)
      applyToGraph(graph, derivation, incidents)
    }
  }
  
  private def applyToGraph(graph : Hypergraph[Vertex, Hyperedge], derivation : HypergraphDerivation, incidents : Iterable[Vertex]) = {
    val extMap = derivation.externalNodes.zip(incidents).toMap
    
    val vMap = extMap ++ derivation.graph.getVertices().filterNot(extMap.contains(_)).map { v =>
      val copy = v.copy
      graph.addVertex(copy)
      (v -> copy)
    }.toMap
    
    derivation.graph.getEdges().foreach { e => 
      val copy = e.copy
      val vs = derivation.graph.getIncidentVertices(e).map(vMap(_))
      graph.addEdge(copy, vs)
    }
    
    graph
    
  }
  
  private def getNonTerminalEdge(graph : Hypergraph[Vertex, Hyperedge], label : String) = {
    // Note this is linear, probably can make this faster
    graph.getEdges().find(_.label == label)
  }
}
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
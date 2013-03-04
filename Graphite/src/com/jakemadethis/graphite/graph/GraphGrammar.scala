package com.jakemadethis.graphite.graph
import scala.collection.JavaConversions._
import com.jakemadethis.graphite._
import edu.uci.ics.jung.graph.Hypergraph

private object graphutil {
  def nonTerminalStrings(graph : Hypergraph[Vertex, Hyperedge]) = {
    graph.getEdges().filter(_.isNonTerminal).map(_.label).toList
  }
  def terminalSize(graph : Hypergraph[Vertex, Hyperedge], externalNodes : Seq[Vertex]) = {
    graph.getEdges().count(_.isTerminal) + graph.getVertexCount() - externalNodes.size
  }
}


class HypergraphDerivation(val graph : Hypergraph[Vertex, Hyperedge], val externalNodes : Seq[Vertex], val label : String) 
  extends Derivation(graphutil.nonTerminalStrings(graph), graphutil.terminalSize(graph, externalNodes)) {
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
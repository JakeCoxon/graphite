package com.jakemadethis.graphite.algorithm
import scala.collection.JavaConversions._
import com.jakemadethis.graphite._
import edu.uci.ics.jung.graph.Hypergraph
import com.jakemadethis.graphite.graph.Hyperedge
import com.jakemadethis.graphite.graph.Vertex
import com.jakemadethis.graphite.graph.OrderedHypergraph

private object graphutil {
  def wrapNonTerminalStrings(graph : Hypergraph[Vertex, Hyperedge]) = {
    graph.getEdges().filter(_.isNonTerminal).map(_.label).map{new Derivation.Item(_)}.toList
  }
  def terminalSize(graph : Hypergraph[Vertex, Hyperedge], externalNodes : Seq[Vertex]) = {
    graph.getEdges().count(_.isTerminal) + graph.getVertexCount() - externalNodes.size
  }
}


class HypergraphDerivation(label : String, val graph : Hypergraph[Vertex, Hyperedge], 
    val externalNodes : Seq[Vertex]) 
  extends Derivation(label, graphutil.wrapNonTerminalStrings(graph), graphutil.terminalSize(graph, externalNodes)) {
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
  
  object factory extends DerivationFactory[String, HypergraphDerivation] {
    
    /** Copy a derivation but remove any edges that arent in `edges' **/
    def copyWithoutNonTerminals(deriv: HypergraphDerivation, filter : Set[Derivation.Item[String]]) = {
        
      val graph = new OrderedHypergraph[Vertex, Hyperedge]()
      val vMap = deriv.graph.getVertices.map { v => v -> v.copy }.toMap
      vMap.values.foreach { graph.addVertex(_) }
      
      val extNodes = deriv.externalNodes.map(vMap)
      val newEdges = filter.foreach { edge => 
        val vs = deriv.graph.getIncidentVertices(edge).map(vMap)
        graph.addEdge(edge.copy, vs)
      }
      
      new HypergraphDerivation(graph, extNodes, deriv.label)

    }
  }
  
}
class HypergraphGrammar(map : Map[String, Seq[HypergraphDerivation]]) 
  extends Grammar.StringGrammar[HypergraphDerivation](map) {
}

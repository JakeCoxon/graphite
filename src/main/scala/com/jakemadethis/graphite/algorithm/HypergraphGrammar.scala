package com.jakemadethis.graphite.algorithm
import scala.collection.JavaConversions._
import com.jakemadethis.graphite._
import edu.uci.ics.jung.graph.Hypergraph
import com.jakemadethis.graphite.graph.Hyperedge
import com.jakemadethis.graphite.graph.Vertex

private object graphutil {
  def edgeSymbolMap(graph : Hypergraph[Vertex, Hyperedge]) = {
    graph.getEdges().filter(_.isNonTerminal).map(edge => new Grammar.Symbol(edge.label) -> edge).toMap
  }
  def terminalSize(graph : Hypergraph[Vertex, Hyperedge], externalNodes : Seq[Vertex]) = {
    graph.getEdges().count(_.isTerminal) + graph.getVertexCount() - externalNodes.size
  }
}


class HypergraphProduction(val graph : Hypergraph[Vertex, Hyperedge], 
    val externalNodes : Seq[Vertex], edgeSymbolMap : Map[Grammar.Symbol, Hyperedge], terminalSize : Int) 
  extends Production(edgeSymbolMap.keys.toList, terminalSize) {
  
  def deriveType = externalNodes.size
  override def isSingleton = super.isSingleton && 
    graph.getIncidentVertices(graph.getEdges().head).size == graph.getVertexCount()
  
  def map(sym : Grammar.Symbol) = edgeSymbolMap(sym)
}

object HypergraphProduction {
  def apply(graph : Hypergraph[Vertex, Hyperedge], externalNodes : Seq[Vertex]) = {
    val edgeSymbolMap = graphutil.edgeSymbolMap(graph)
    val terminalSize = graphutil.terminalSize(graph, externalNodes)
    new HypergraphProduction(graph, externalNodes, edgeSymbolMap, terminalSize)
  }
}

object HypergraphGrammar {
  type HG = Grammar[HypergraphProduction]
  
  
}

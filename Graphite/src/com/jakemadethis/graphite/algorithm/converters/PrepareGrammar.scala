package com.jakemadethis.graphite.algorithm.converters

import edu.uci.ics.jung.graph.Hypergraph
import com.jakemadethis.graphite.graph._
import collection.JavaConversions._
import com.jakemadethis.graphite.algorithm.HypergraphGrammar
import com.jakemadethis.graphite.algorithm.HypergraphProduction

object PrepareGrammar {
  
  
  
  /** Prepares a grammar for the algorithm **/
  def apply(grammar : HypergraphGrammar.HG) = {
    
    
    val convert = 
      ValidateGraphs andThen 
      RemoveSingleProductions andThen 
      ToEpsilonFree
      
    convert(grammar)
    
  }
  
  object ValidateGraphs extends Function[HypergraphGrammar.HG,HypergraphGrammar.HG] {
    
    def apply(grammar : HypergraphGrammar.HG) = {
      if (grammar.productions.exists(d => hasFakeNodes(d._2.graph)))
        throw new GrammarError("A graph has unconnected vertices")
      
      if (!initialIsHandle(grammar.initial))
        throw new GrammarError("Initial graph must be a handle")
      
      else grammar
    }
    
    def hasFakeNodes(graph : Hypergraph[Vertex, Hyperedge]) = 
      graph.getVertices().exists(_.isInstanceOf[FakeVertex])
      
    def initialIsHandle(initial : HypergraphProduction) = 
      initial.graph.getEdgeCount() == 1 && 
      initial.graph.getIncidentCount(initial.graph.getEdges().head) == 
        initial.graph.getVertexCount()
  }
  
  
}

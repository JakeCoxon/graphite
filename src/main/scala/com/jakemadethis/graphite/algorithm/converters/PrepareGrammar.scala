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
      
      checkNonTerminalTypes(grammar)
      
      grammar
    }
    
    def checkNonTerminalTypes(grammar : HypergraphGrammar.HG) = {
      // Make a map of non-terminals to types.
      val ntMap = grammar.productions.foldLeft(Map[String, Int]()) { case (result, (nt, prod)) =>
        val extNodes = prod.externalNodes.size
        if (result.containsKey(nt)) {
          val existing = result(nt)
          if (existing != extNodes) 
            throw new GrammarError("Mismatched type non-terminal '%s': %d and %d".
                format(nt, existing, extNodes))
          result
        }
        else {
          result + (nt -> extNodes)
        }
      }
      
      // Check non-terminals within the graphs
      for ((nt, prod) <- grammar.productions; edge <- prod.graph.getEdges.filter(_.isNonTerminal)) {
        val attachmentNodes = prod.graph.getIncidentCount(edge)
        if (!ntMap.contains(edge.label)) 
          throw new GrammarError("Invalid non-terminal '%s' in graph".format(edge.label))
        val existing = ntMap(edge.label)
        if (existing != attachmentNodes)
          throw new GrammarError("Mismatched non-terminal '%s' in graph: %d and %d".
            format(edge.label, existing, attachmentNodes))
      }
      grammar
    }
    
    def hasFakeNodes(graph : Hypergraph[Vertex, Hyperedge]) = 
      graph.getVertices().exists(_.isInstanceOf[FakeVertex])
      
    def initialIsHandle(initial : HypergraphProduction) = 
      initial.graph.getEdgeCount() == 1 && 
      initial.graph.getIncidentCount(initial.graph.getEdges().head) == 
        initial.graph.getVertexCount()
  }
  
  
}

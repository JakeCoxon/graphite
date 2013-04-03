package com.jakemadethis.graphite.algorithm

import edu.uci.ics.jung.graph.Hypergraph
import com.jakemadethis.graphite.graph._
import collection.JavaConversions._
import com.jakemadethis.graphite.ui.GraphFrame

object Validator {
  
  /** Throws an exception if the graph is invalid and converts graph into a 
   *  form that is workable with the algorithm **/
  def validateGraph(grammar : HypergraphGrammar.HG) = {
    if (grammar.productions.exists(d => isInvalidGraph(d._2.graph))) {
      throw new RuntimeException("Invalid graph")
    }
    
    //convertEpsilonFree(grammar)
    grammar
  }
  
  def isInvalidGraph(graph : Hypergraph[Vertex, Hyperedge]) = 
    graph.getVertices().exists(_.isInstanceOf[FakeVertex])
  
  
}
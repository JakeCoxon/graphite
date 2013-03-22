package com.jakemadethis.graphite.algorithm

import edu.uci.ics.jung.graph.Hypergraph
import com.jakemadethis.graphite.graph._
import collection.JavaConversions._
import com.jakemadethis.graphite.ui.GraphFrame

object Validator {
  
  
  def validateGraph(grammar : HypergraphGrammar) = {
    if (grammar.derivations.exists(d => isInvalidGraph(d.graph))) {
      throw new RuntimeException("Invalid graph")
    }
    
    convertEpsilonFree(grammar)
  }
  
  def isInvalidGraph(graph : Hypergraph[Vertex, Hyperedge]) = 
    graph.getVertices().exists(_.isInstanceOf[FakeVertex])
  
  /** Converts a grammar into an epsilon-free grammar
   *  This means no derivation leads to an empty graph */
  private def convertEpsilonFree(grammar : HypergraphGrammar) : HypergraphGrammar = {
    
    /** Gets whether this derivation lead to an epsilon **/
    def isEpsilon(derivation : HypergraphDerivation, epsilonNonTerminals : Set[String]) : Boolean = {
      if (derivation.graph.getVertexCount > derivation.externalNodes.size) return false
      if (derivation.graph.getEdgeCount == 0) return true
      derivation.graph.getEdges.forall { e => epsilonNonTerminals.contains(e.label) }
    }
    
    def getEpsilonNonTerminals(epsilonNonTerminals : Set[String]) : Set[String] = {
      val epsilonDerivs = grammar.derivations.filter { isEpsilon(_, epsilonNonTerminals) }
      val newNts = epsilonDerivs.map {_.label}.toSet -- epsilonNonTerminals
      if (newNts.isEmpty) epsilonNonTerminals
      else getEpsilonNonTerminals(epsilonNonTerminals ++ newNts)
    }
    
    val epsilonNonTerminals = getEpsilonNonTerminals(Set())
    
    if (epsilonNonTerminals.isEmpty) return grammar
    println("Converting to non-epsilon")
    
    val newDerivs = grammar.derivations.toList.flatMap { derivation => 
      println()
      println(derivation)
      val eps = derivation.graph.getEdges.filter { e => epsilonNonTerminals.contains(e.label) }.toList
      val all = derivation.graph.getEdges
      println(all)
      
      if (isEpsilon(derivation, epsilonNonTerminals)) {
        Nil
      } else if (eps.isEmpty) {
        // eps is empty if derivation leads to an epsilon
        List(derivation)
      } else {
        // Construct every combination of epsilon edges of any size
        val epsComb = (0 to eps.size).flatMap{ i => eps.combinations(i) }
        // Build list of all the edges except the above combination
        val edgeComb = epsComb.map { comb => all.filterNot { comb.contains(_) } }
        println(edgeComb)
        // Make a copy of derivation with only these calculated edges 
        val copy = edgeComb.map { copyDerivationWithOnlyEdges(derivation, _) }
        
        // Only add these to the grammar if they themselves don't lead to epsilon
        val filteredCopy = copy.filterNot { d => isEpsilon(d, epsilonNonTerminals) }
        filteredCopy
      }
    }
    
//    println("---")
//    for (d <- newDerivs) {
//      println(d.label)
//      println(d.graph.getEdges.mkString(" "))
//      println()
//      
//      new GraphFrame(d.graph) { open }
//    }
    
    
    
    HypergraphGrammar(newDerivs)
  }
  
  private def copyDerivationWithOnlyEdges(derivation : HypergraphDerivation, edges : Traversable[Hyperedge]) = {
    val graph = new OrderedHypergraph[Vertex, Hyperedge]()
    val vMap = derivation.graph.getVertices.map { v => v -> v.copy }.toMap
    vMap.values.foreach { graph.addVertex(_) }
    
    val extNodes = derivation.externalNodes.map(vMap)
    val newEdges = edges.foreach { edge => 
      val vs = derivation.graph.getIncidentVertices(edge).map(vMap)
      graph.addEdge(edge.copy, vs)
    }
    
    new HypergraphDerivation(graph, extNodes, derivation.label)
  }
}
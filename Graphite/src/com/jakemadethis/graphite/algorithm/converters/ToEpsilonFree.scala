package com.jakemadethis.graphite.algorithm.converters

import com.jakemadethis.graphite.graph._
import com.jakemadethis.graphite.algorithm._
import collection.JavaConversions._

object ToEpsilonFree extends Function[HypergraphGrammar.HG, HypergraphGrammar.HG] {
  
  
  /** Converts a grammar into an epsilon-free grammar
   *  This means no derivation leads to an empty graph */
  def apply(grammar : HypergraphGrammar.HG) : HypergraphGrammar.HG = {
    
    /** Gets whether this derivation *can* derive to *exactly* an epsilon using a set
     *  of known non-terminals that may also derive to exactly an epsilon. **/
    def isEpsilon(prod : HypergraphProduction, epsilonNonTerminals : Set[String]) : Boolean = {
      if (prod.graph.getVertexCount > prod.externalNodes.size) return false
      if (prod.graph.getEdgeCount == 0) return true
      // Make sure all edges are *non-terminals* and will derive to an epsilon
      prod.graph.getEdges.forall { e => epsilonNonTerminals.contains(e.label) }
    }
    
    /** Recursively finds a set of non-terminals that may derive to *exactly* an epsilon **/
    def getEpsilonNonTerminals(epsilonNonTerminals : Set[String]) : Set[String] = {
      val epsilonProds = grammar.productions.filter { d => isEpsilon(d._2, epsilonNonTerminals) }
      val newNts = epsilonProds.map {_._1}.toSet -- epsilonNonTerminals
      // Terminate if set is unchanged otherwise, call again
      if (newNts.isEmpty) epsilonNonTerminals
      else getEpsilonNonTerminals(epsilonNonTerminals ++ newNts)
    }
    
    val epsilonNonTerminals = getEpsilonNonTerminals(Set())
    
    // Return unmodified grammar if there are no non-terminals that may derive
    // to an epsilon
    if (epsilonNonTerminals.isEmpty) return grammar
    
    val invalidNts = epsilonNonTerminals.filter { nt =>
      grammar(nt).forall { prod => isEpsilon(prod, Set()) }
    }
    
    if (!invalidNts.isEmpty) 
      throw new GrammarError("A non-terminal (%s) derives just epsilons".format(invalidNts.mkString(",")))
    
    println("Converting to non-epsilon")
    
    var numAdded = 0
    var numRemoved = 0
    
    
    
    val newProds = grammar.productions.toList.flatMap { case (prodLabel, prod) => 
      // Make a list of epsilon non-terminals (non-terminals that may derive to an epsilon).
      // This can have repetitions of non-terminals
      val eps = prod.graph.getEdges.filter { e => epsilonNonTerminals.contains(e.label) }.toList
      val nonEps = prod.graph.getEdges.filterNot { e => epsilonNonTerminals.contains(e.label) }.toList
      
      //val all = prod.graph.getEdges
      
      if (isEpsilon(prod, epsilonNonTerminals)) {
        // This production derives to exactly an epsilon so remove it
        numRemoved += 1
        Nil
      } else if (eps.isEmpty) {
        // This production doesn't derive to an epsilon but doesn't have any epsilon non-terminals
        List(prodLabel -> prod)
      } else {
        // Construct every combination of epsilon edges of any size
        val epsComb = (0 to eps.size).flatMap{ i => eps.combinations(i) }
        // Build list of all the edges except the above combination
        //val edgeComb = epsComb.map { comb => filterNot { comb.contains(_) } }
        val edgeComb = epsComb.map { comb => nonEps ++ comb }
        // Make a copy of production with only these calculated edges 
        val copy = edgeComb.map { prodLabel -> copyProductionWithOnlyEdges(prod, _) }
        
        // Only add these to the grammar if they themselves don't lead to epsilon
        val filteredCopy = copy.filterNot { d => isEpsilon(d._2, epsilonNonTerminals) }
        numAdded += filteredCopy.size - 1 // One of these will be equiv to the original
        filteredCopy
      }
    }
    
    println("Added %d and removed %d productions".format(numAdded, numRemoved))
    
    Grammar(newProds, grammar.initial)
  }
  
  /** Copy a derivation but remove any edges that arent in `edges' **/
  private def copyProductionWithOnlyEdges(prod : HypergraphProduction, edges : Traversable[Hyperedge]) = {
    val graph = new OrderedHypergraph[Vertex, Hyperedge]()
    val vMap = prod.graph.getVertices.map { v => v -> v.copy }.toMap
    vMap.values.foreach { graph.addVertex(_) }
    
    val extNodes = prod.externalNodes.map(vMap)
    val newEdges = edges.foreach { edge => 
      val vs = prod.graph.getIncidentVertices(edge).map(vMap)
      graph.addEdge(edge.copy, vs)
    }
    
    HypergraphProduction(graph, extNodes)
  }
}
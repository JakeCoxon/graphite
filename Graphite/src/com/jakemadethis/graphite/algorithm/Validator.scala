package com.jakemadethis.graphite.algorithm

import edu.uci.ics.jung.graph.Hypergraph
import com.jakemadethis.graphite.graph._
import collection.JavaConversions._
import com.jakemadethis.graphite.ui.GraphFrame

object Validator {
  
  /** Throws an exception if the graph is invalid and converts graph into a 
   *  form that is workable with the algorithm **/
  def validateGrammar[K, D <: Derivation[K]](grammar : Grammar[K, D])
    (implicit grammarFactory : GrammarFactory[K, D], derivationFactory : DerivationFactory[K, D]) = {
//    if (grammar.derivations.exists(d => isInvalidGraph(d.graph))) {
//      throw new RuntimeException("Invalid graph")
//    }
    
    convertEpsilonFree(grammar)
  }
  
//  def isInvalidGraph(graph : Hypergraph[Vertex, Hyperedge]) = 
//    graph.getVertices().exists(_.isInstanceOf[FakeVertex])
  
  /** Converts a grammar into an epsilon-free grammar
   *  This means no derivation leads to an empty graph */
  private def convertEpsilonFree[K, D <: Derivation[K]](grammar : Grammar[K, D])
    (implicit grammarFactory : GrammarFactory[K, D], derivationFactory : DerivationFactory[K, D]) : Grammar[K, D] = {
    
    /** Gets whether this derivation *can* derive to *exactly* an epsilon using a set
     *  of known non-terminals that may also derive to exactly an epsilon. **/
    def isEpsilon(derivation : D, epsilonNonTerminals : Set[K]) : Boolean = {
      if (derivation.terminalSize > 0) return false
      if (derivation.nonTerminals.size == 0) return true
      // Return true if all non-terminals will derive to an epsilon
      derivation.nonTerminals.forall { epsilonNonTerminals.contains(_) }
    }
    
    /** Recursively finds a set of non-terminals that may derive to *exactly* an epsilon **/
    def getEpsilonNonTerminals(epsilonNonTerminals : Set[K]) : Set[K] = {
      val epsilonDerivs = grammar.derivations.filter { isEpsilon(_, epsilonNonTerminals) }
      val newNts = epsilonDerivs.map {_.label}.toSet -- epsilonNonTerminals
      // Terminate if set is unchanged otherwise, call again
      if (newNts.isEmpty) epsilonNonTerminals
      else getEpsilonNonTerminals(epsilonNonTerminals ++ newNts)
    }
    
    val epsilonNonTerminals = getEpsilonNonTerminals(Set())
    
    // Return unmodified grammar if there are no non-terminals that may derive
    // to an epsilon
    if (epsilonNonTerminals.isEmpty) return grammar
    
    println("Converting to non-epsilon")
    
    var numAdded = 0
    var numRemoved = 0
    
    val newDerivs = grammar.derivations.toList.flatMap { derivation => 
      // Make a list of epsilon non-terminals (non-terminals that may derive to an epsilon).
      // This can have repetitions of non-terminals
      val eps = derivation.nonTerminals.filter { epsilonNonTerminals.contains(_) }
//      val all = derivation.nonTerminals
      
      if (isEpsilon(derivation, epsilonNonTerminals)) {
        // This derivation derives to exactly an epsilon so remove it
        numRemoved += 1
        Nil
      } else if (eps.isEmpty) {
        // This derivation doesn't derive to an epsilon but doesn't have any epsilon non-terminals
        List(derivation)
      } else {
        // Construct every combination of epsilon non-terminals of any size
        val epsComb = (0 to eps.size).flatMap{ i => eps.combinations(i) }
        // Build list of all the edges except the above combination
//        val edgeComb = epsComb.map { comb => all.filterNot { comb.contains(_) } }
        // Make a copy of derivation with only these calculated edges 
        val copy = epsComb.map { comb => 
          derivationFactory.copyWithoutNonTerminals(derivation, comb.toSet) }
        
        // Only add these to the grammar if they themselves don't lead to epsilon
        val filteredCopy = copy.filterNot { d => isEpsilon(d, epsilonNonTerminals) }
        numAdded += filteredCopy.size - 1 // One of these will be equiv to the original
        filteredCopy
      }
    }
    
    println("Added %d and removed %d derivations".format(numAdded, numRemoved))
    
    grammarFactory.apply(newDerivs)
  }
  
//  /** Copy a derivation but remove any edges that arent in `edges' **/
//  private def copyDerivationWithOnlyEdges(derivation : HypergraphDerivation, edges : Traversable[Hyperedge]) = {
//    val graph = new OrderedHypergraph[Vertex, Hyperedge]()
//    val vMap = derivation.graph.getVertices.map { v => v -> v.copy }.toMap
//    vMap.values.foreach { graph.addVertex(_) }
//    
//    val extNodes = derivation.externalNodes.map(vMap)
//    val newEdges = edges.foreach { edge => 
//      val vs = derivation.graph.getIncidentVertices(edge).map(vMap)
//      graph.addEdge(edge.copy, vs)
//    }
//    
//    new HypergraphDerivation(graph, extNodes, derivation.label)
//  }
}
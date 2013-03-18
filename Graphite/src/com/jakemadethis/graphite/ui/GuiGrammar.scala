package com.jakemadethis.graphite.ui

import com.jakemadethis.graphite.graph._
import edu.uci.ics.jung.graph._
import edu.uci.ics.jung.algorithms.layout._
import edu.uci.ics.jung.algorithms.layout.util._
import java.awt.Dimension
import com.jakemadethis.graphite.visualization.AverageEdgeLayout

object GuiGrammar {
  /** The internal label name for the initial graph. This is used for saving/loading **/
  val INITIAL_LABEL = "$initial"
  
  /** 
   * Constructs an empty grammar 
   */
  def apply() = {
    new GuiGrammar() {
      initialGraph = new InitialDerivation(INITIAL_LABEL, newRightModel(0))
    }
  }
  
  /**
   * Creates a RightModel with `size' number of external nodes
   */
  private def newRightModel(size : Int) = {
    val graph = new OrderedHypergraph[Vertex,Hyperedge]()

    // Create and add `size' number of new vertices for external nodes
    val extNodes = (0 until size).map { new Vertex() -> _ }.toMap
    extNodes.keys.foreach { graph.addVertex(_) }
    
    val pgraph = graph.asInstanceOf[Graph[Vertex,Hyperedge]]
    val rand = new RandomLocationTransformer[Vertex](new Dimension(500, 500))
    val layout = new StaticLayout[Vertex, Hyperedge](pgraph, rand, new Dimension(500, 500))
      with AverageEdgeLayout[Vertex, Hyperedge]
    
    DerivationPair.RightModel(layout, extNodes)
  }
  
  /**
   * Creates a new basic DerivationPair with `label' and `size' number of externalNodes
   */
  def newDerivation(label : String, size : Int) = {
    val leftSide = DerivationPair.LeftModel(label, size)
    val rightSide = newRightModel(size)
    new DerivationPair(leftSide, rightSide)
  }
}


/**
 * A GuiGrammar is distinctly different to the algorithmic grammar
 * It holds a list of derivation models rather than a map and also
 * has a seperate initialGraph entry
 */
class GuiGrammar {
  var file : String = null
  var initialGraph : DerivationPair = null
  val derivations = collection.mutable.ListBuffer[DerivationPair]()
}
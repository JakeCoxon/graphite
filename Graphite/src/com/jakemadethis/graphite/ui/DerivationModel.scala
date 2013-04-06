package com.jakemadethis.graphite.ui

import edu.uci.ics.jung.visualization.DefaultVisualizationModel
import edu.uci.ics.jung.algorithms.layout.Layout
import edu.uci.ics.jung.graph.Hypergraph
import collection.JavaConversions._
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer
import java.awt.Dimension
import com.jakemadethis.graphite.visualization.HyperedgeLayout
import com.jakemadethis.graphite.graph._
import com.jakemadethis.graphite.ui.DerivationPair.LeftModel
import com.jakemadethis.graphite.ui.DerivationPair.RightModel
import collection.Map
import edu.uci.ics.jung.algorithms.layout.StaticLayout

abstract class DerivationModel(layout : Layout[Vertex,Hyperedge]) extends DefaultVisualizationModel(layout) {
  
  def externalNodeId(vertex : Vertex) : Option[Int]
  def graph = layout.getGraph().asInstanceOf[Hypergraph[Vertex,Hyperedge]]
  
}


object DerivationPair {
  
  object RightModel {
    def apply(layout : Layout[Vertex,Hyperedge], extNodes : Map[Vertex,Int]) = 
      new RightModel(layout, extNodes)
  }
  
  
  class RightModel(layout : Layout[Vertex,Hyperedge], _extNodes : Map[Vertex,Int]) extends DerivationModel(layout) {
    val extNodes = collection.mutable.Map[Vertex,Int]() ++= _extNodes
    
    def externalNodeId(vertex : Vertex) = extNodes.get(vertex)
    def externalNodesSet = extNodes.keys.toSet
    
    def numExternalNodes = extNodes.size
    protected[DerivationPair] def numExternalNodes_=(newSize : Int) {
      require(newSize > 0)
      val oldExt : collection.mutable.Buffer[Vertex] = extNodes.keys.toBuffer.sortBy {extNodes(_)}
      
      val vs = oldExt.take(newSize) ++ 
        ((oldExt.size until newSize) map {i => 
          val v = new Vertex(); graph.addVertex(v); v})
      oldExt.drop(newSize).filter {graph.getIncidentEdges(_).isEmpty}.foreach {graph.removeVertex(_)}
      extNodes.clear
      vs.zipWithIndex.foreach { case (v,i) => extNodes(v) = i }
    }
  }
  
  object LeftModel {
    def apply(label : String, size : Int) = {
      require(size > 0)
      val graph = new GraphHandle(label, size)
      val rand = new RandomLocationTransformer[Vertex](new Dimension(500,500))
      val layout = new HyperedgeLayout(graph, new Dimension(500,500))
      layout.lockEdge(graph.edge, true)
      new LeftModel(layout)
    }
  }
  class LeftModel(layout : Layout[Vertex,Hyperedge]) extends DerivationModel(layout) {
    require(graph.getEdgeCount() <= 1)
    
    def hyperedge = graph.getEdges().head
    def vertices = graph.getIncidentVertices(hyperedge).toList
    override def externalNodeId(vertex : Vertex) = vertices.indexOf(vertex) match {
      case -1 => None; case x => Some(x)
    }
    
    protected[DerivationPair] def edit(newLabel : String, newSize : Int) {
      getGraphLayout().setGraph(new GraphHandle(newLabel, newSize))
    }
    
    
    def size = vertices.size
    
    
    def label = hyperedge.label
  }
  
}

/**
 *  DerivationPair holds leftside and rightside and controls them both
 *  A call to numExternalNodes_= will update both sides
 */
class DerivationPair(val leftSide : LeftModel, val rightSide : RightModel) {
  
  def label = leftSide.label
  lazy val isInitial = false
  
  def isInvalid = rightSide.graph.getVertices().exists(_.isInstanceOf[FakeVertex])
  
  def numExternalNodes = rightSide.numExternalNodes
  
  def edit(newLabel : String, newSize : Int) {
    require(newSize > 0)
    leftSide.edit(newLabel, newSize)
    rightSide.numExternalNodes = newSize
  }
    
  
}

object InitialDerivation {
  def emptyLeft = {
    val g = new OrderedHypergraph[Vertex,Hyperedge]()
    val layout = new StaticLayout[Vertex,Hyperedge](g)
    new LeftModel(layout)
  }
}
class InitialDerivation(_label : String, rightSide : RightModel) extends DerivationPair(InitialDerivation.emptyLeft, rightSide) {
  override def label = _label
  override lazy val isInitial = true
  override def edit(newLabel : String, size : Int) = throw new UnsupportedOperationException()
}




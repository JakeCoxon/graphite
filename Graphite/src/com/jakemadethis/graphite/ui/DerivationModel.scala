package com.jakemadethis.graphite.ui

import edu.uci.ics.jung.visualization.DefaultVisualizationModel
import edu.uci.ics.jung.algorithms.layout.Layout
import edu.uci.ics.jung.graph.Hypergraph
import collection.JavaConversions._
import com.jakemadethis.graphite.graph.HyperedgeGraph
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer
import java.awt.Dimension
import com.jakemadethis.graphite.visualization.HyperedgeLayout
import com.jakemadethis.graphite.graph._
import com.jakemadethis.graphite.ui.DerivationPair.LeftModel
import com.jakemadethis.graphite.ui.DerivationPair.RightModel
import collection.Map

abstract class DerivationModel(layout : Layout[Vertex,Hyperedge]) extends DefaultVisualizationModel(layout) {
  def externalNodeId(vertex : Vertex) : Option[Int]
  val graph = layout.getGraph().asInstanceOf[Hypergraph[Vertex,Hyperedge]]
  override def setGraphLayout(layout : Layout[Vertex,Hyperedge], viewSize : Dimension) {
    require(graph == null || graph == layout.getGraph, "Cannot change graph")
    super.setGraphLayout(layout, viewSize)
  }
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
      val oldvs = graph.getVertices().toList
      val vs = oldvs.take(newSize) ++ 
        ((oldvs.size until newSize) map {i => 
          val v = new Vertex(); graph.addVertex(v); v})
      extNodes.clear
      vs.zipWithIndex.foreach { case (v,i) => extNodes(v) = i }
    }
  }
  
  object LeftModel {
    def apply(label : String, size : Int) = {
      val graph = new HyperedgeGraph(label, size)
      val rand = new RandomLocationTransformer[Vertex](new Dimension(500,500))
      val layout = new HyperedgeLayout(graph, new Dimension(500,500))
      new LeftModel(layout)
    }
  }
  class LeftModel(layout : Layout[Vertex,Hyperedge]) extends DerivationModel(layout) {
    require(graph.getEdgeCount() == 1)
    
    def hyperedge = graph.getEdges().head
    def vertices = graph.getIncidentVertices(hyperedge).toList
    override def externalNodeId(vertex : Vertex) = vertices.indexOf(vertex) match {
      case -1 => None; case x => Some(x)
    }
    
    def size = vertices.size
    protected[DerivationPair] def size_=(newSize : Int) {
      require(newSize > 0)
      graph.removeEdge(hyperedge)
      val oldvs = vertices
      val vs = oldvs.take(newSize) ++ 
        ((oldvs.size until newSize) map {i => new Vertex()})
      oldvs.drop(newSize) foreach {graph.removeVertex(_)}
      graph.addEdge(hyperedge, vs)
    }
    
    def label = hyperedge.label
    def label_=(newLabel : String) {
      val oldvs = vertices
      graph.removeEdge(hyperedge)
      graph.addEdge(new Hyperedge(label, NonTerminal), oldvs)
    }
  }
  
}

/**
 *  DerivationPair holds leftside and rightside and controls them both
 *  A call to numExternalNodes_= will update both sides
 */
class DerivationPair(val leftSide : LeftModel, val rightSide : RightModel) {
  
  def numExternalNodes_=(size : Int) {
    require(size > 0)
    leftSide.size = size
    rightSide.numExternalNodes = size
  }
    
  
}


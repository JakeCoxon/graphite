package com.jakemadethis.graphite.visualization

import java.awt.Dimension
import edu.uci.ics.jung.graph._
import edu.uci.ics.jung.algorithms.layout.Layout
import org.apache.commons.collections15.Transformer
import java.awt.geom.Point2D
import collection.JavaConversions._

class HyperedgeLayout[V,E](g : Hypergraph[V,E], d : Dimension) extends Layout[V, E] 
      with EdgeLayout[E] {
  var graph : Hypergraph[V,E] = null
  var dimension = d
  var locs = collection.immutable.Map[V, Point2D]()
  
  setGraph(g.asInstanceOf[Graph[V,E]])
  
  def transform(v : V) = locs(v)
  def setGraph(g : Graph[V,E]) { 
    graph = g
    val edge = g.getEdges().head
    val vs = g.getIncidentVertices(edge)
    locs = vs.zipWithIndex.map { case (v, i) =>
      val n = i.toFloat / vs.size * math.Pi * 2
      v -> new Point2D.Double(-math.sin(n) * 100, -math.cos(n) * 100)
    }.toMap
    lockEdge(edge, true)
  }
  
  val zero = new Point2D.Double(0,0)
  def getEdgeLocation(edge : E) = zero
  
  def initialize() {}
  def setInitializer(init : Transformer[V,Point2D]) {}
  def getGraph = graph.asInstanceOf[Graph[V,E]]
  def reset() {}
  def setSize(d : Dimension) { dimension = d }
  def getSize() = dimension
  def lock(v : V, b : Boolean) {}
  def isLocked(v : V) = true
  def setLocation(v : V, location : Point2D) {}
  
}
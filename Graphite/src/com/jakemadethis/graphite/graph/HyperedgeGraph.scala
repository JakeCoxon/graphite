package com.jakemadethis.graphite.graph

import edu.uci.ics.jung.graph.util.EdgeType
import edu.uci.ics.jung.graph._
import collection.JavaConversions._

class HyperedgeGraph(label : String, size : Int) extends OrderedHypergraph[Vertex, Hyperedge] {
  
  val edge = new Hyperedge(label, NonTerminal)
  val vs = (0 until size) map {x => new Vertex()}
  
  edges.put(edge, new java.util.ArrayList(vs))
  vs foreach { v =>
    vertices.put(v, new java.util.HashSet())
    vertices.get(v).add(edge)
  }
  
  
  override def addVertex(v : Vertex) = false
  override def addEdge(e : Hyperedge, vs : java.util.Collection[_ <: Vertex]) = false
  override def removeVertex(v : Vertex) = false
  override def removeEdge(e : Hyperedge) = false
  
  

}
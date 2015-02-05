package com.jakemadethis.graphite.graph

import edu.uci.ics.jung.graph.util.EdgeType
import edu.uci.ics.jung.graph._
import collection.JavaConversions._

/**
 * A graph consisting of a single edge with label `label' and `size' number of vertices
 * This is for displaying a left-hand side of a derivation
 * Modifying the graph after it has been constructed in unsupported
 */
class GraphHandle(label : String, size : Int) extends OrderedHypergraph[Vertex, Hyperedge] {
  
  //
  val edge = new Hyperedge(label, NonTerminal)
  val vs = (0 until size) map {x => new Vertex()}
  
  edges.put(edge, new java.util.ArrayList(vs))
  vs foreach { v =>
    vertices.put(v, new java.util.HashSet())
    vertices.get(v).add(edge)
  }
  
  
  override def addVertex(v : Vertex) = 
    throw new UnsupportedOperationException()
  override def addEdge(e : Hyperedge, vs : java.util.Collection[_ <: Vertex]) = 
    throw new UnsupportedOperationException()
  override def removeVertex(v : Vertex) = 
    throw new UnsupportedOperationException()
  override def removeEdge(e : Hyperedge) = 
    throw new UnsupportedOperationException()
  
  

}
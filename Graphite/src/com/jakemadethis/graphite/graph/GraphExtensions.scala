package com.jakemadethis.graphite.graph

import edu.uci.ics.jung.graph.Graph
import edu.uci.ics.jung.graph.Hypergraph
import scala.collection.JavaConversions._

object GraphExtensions {
  implicit def graphExtensions[V, E](g : Hypergraph[V, E]) = new GraphExtensions[V, E](g)
}
class GraphExtensions[V, E](graph : Hypergraph[V,E]) {
  def merge(prevV : V, newV : V) {
    val edges = graph.getIncidentEdges(prevV)
    edges.foreach { edge => 
      val newIncidents = graph.getIncidentVertices(edge).map(a => if (a == prevV) newV else a)
      graph.removeEdge(edge)
      graph.addEdge(edge, newIncidents)
    }
    graph.removeVertex(prevV)
  }
}
package com.jakemadethis.graphite.visualization

import com.jakemadethis.graphite.graph.Vertex
import com.jakemadethis.graphite.graph.Hyperedge

abstract class VisualItem {
  
}

class VisualVertex(val vertex : Vertex) extends VisualItem {}
class VisualFakeVertex extends VisualItem {}
class VisualEdge(val edge : Hyperedge) extends VisualItem {}

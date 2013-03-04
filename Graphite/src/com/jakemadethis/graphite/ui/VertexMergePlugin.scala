package com.jakemadethis.graphite.ui

import com.jakemadethis.graphite.visualization.MouseDropPlugin
import edu.uci.ics.jung.graph.Hypergraph
import com.jakemadethis.graphite.graph.GraphExtensions._
import java.awt.event.MouseEvent
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.VisualizationServer
import com.jakemadethis.graphite.graph.Vertex
import com.jakemadethis.graphite.graph.Hyperedge
import com.jakemadethis.graphite.graph.FakeVertex

class VertexMergePlugin extends MouseDropPlugin[Vertex, Hyperedge] {
  def dragFilter(graph : Hypergraph[Vertex, Hyperedge], drag : Vertex) =
    drag.isInstanceOf[FakeVertex]
  
  def dropFilter(graph : Hypergraph[Vertex, Hyperedge], drop : Vertex) = 
    !drop.isInstanceOf[FakeVertex]
  
  override def mouseDragged(e : MouseEvent) {
    ifDrop(e) { (vv, drag, drop) =>
      val layout = vv.getGraphLayout()
      layout.setLocation(drag, layout.transform(drop))
      e.consume()
    }
  }
  
  def vertexDropped(vs : VisualizationServer[Vertex, Hyperedge], drag : Vertex, drop : Vertex) = {
    vs.getGraphLayout().getGraph().merge(drag, drop)
    vs.repaint()
  }
}
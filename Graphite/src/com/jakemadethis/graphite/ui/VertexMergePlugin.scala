package com.jakemadethis.graphite.ui

import com.jakemadethis.graphite.visualization.MouseDropPlugin
import edu.uci.ics.jung.graph.Hypergraph
import com.jakemadethis.graphite.graph.GraphExtensions._
import java.awt.event.MouseEvent
import edu.uci.ics.jung.visualization.VisualizationViewer

class VertexMergePlugin extends MouseDropPlugin[VisualItem, VisualEdge] {
  def dragFilter(graph : Hypergraph[VisualItem, VisualEdge], drag : VisualItem) =
    drag.isInstanceOf[VisualFakeVertex]
  
  def dropFilter(graph : Hypergraph[VisualItem, VisualEdge], drop : VisualItem) = 
    !drop.isInstanceOf[VisualFakeVertex]
  
  override def mouseDragged(e : MouseEvent) {
    super.mouseDragged(e)
    println(dropVertex.isDefined)
    dropVertex.foreach {v =>
      val vv = e.getSource().asInstanceOf[VisualizationViewer[VisualItem, VisualEdge]]
      val layout = vv.getGraphLayout()
      layout.setLocation(dragVertex.get, layout.transform(v))
      vv.repaint()
      e.consume()
    }
  }
  
  def vertexDropped(graph : Hypergraph[VisualItem, VisualEdge], drag : VisualItem, drop : VisualItem) =
    graph.merge(drag, drop)
}
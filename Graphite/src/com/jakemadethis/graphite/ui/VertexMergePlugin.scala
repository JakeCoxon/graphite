package com.jakemadethis.graphite.ui

import com.jakemadethis.graphite.visualization.MouseDropPlugin
import edu.uci.ics.jung.graph.Hypergraph
import com.jakemadethis.graphite.graph.GraphExtensions._
import java.awt.event.MouseEvent
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.VisualizationServer

class VertexMergePlugin extends MouseDropPlugin[VisualItem, VisualEdge] {
  def dragFilter(graph : Hypergraph[VisualItem, VisualEdge], drag : VisualItem) =
    drag.isInstanceOf[VisualFakeVertex]
  
  def dropFilter(graph : Hypergraph[VisualItem, VisualEdge], drop : VisualItem) = 
    !drop.isInstanceOf[VisualFakeVertex]
  
  def vertexDropped(vs : VisualizationServer[VisualItem, VisualEdge], drag : VisualItem, drop : VisualItem) = {
    vs.getGraphLayout().getGraph().merge(drag, drop)
    vs.repaint()
  }
}
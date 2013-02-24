package com.jakemadethis.graphite.visualization

import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin
import java.awt.event.MouseMotionListener
import java.awt.event.MouseEvent
import edu.uci.ics.jung.visualization.VisualizationViewer

class HoverMousePlugin[V,E](hoverSupport : HoverSupport[V,E]) extends AbstractGraphMousePlugin(0) with MouseMotionListener {

  def mouseDragged(e: MouseEvent) = {}

  def mouseMoved(e: MouseEvent) = {

    val vv = e.getSource().asInstanceOf[VisualizationViewer[V,E]]
    val pickSupport = vv.getPickSupport()
    val layout = vv.getGraphLayout()
    val ip = e.getPoint()
    
    
    val vertex = pickSupport.getVertex(layout, ip.getX(), ip.getY())
    val edge = pickSupport.getEdge(layout, ip.getX(), ip.getY())

    hoverSupport.hoverVertexState.clear();
    hoverSupport.hoverEdgeState.clear();
    
    if (vertex != null) {
      hoverSupport.hoverVertexState.pick(vertex, true)
    } else if (edge != null) {
      hoverSupport.hoverEdgeState.pick(edge, true)
    }
  }

}

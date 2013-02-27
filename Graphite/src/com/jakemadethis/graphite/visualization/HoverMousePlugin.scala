package com.jakemadethis.graphite.visualization

import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin
import scala.collection.JavaConversions._
import java.awt.event.MouseMotionListener
import java.awt.event.MouseEvent
import edu.uci.ics.jung.visualization.VisualizationViewer
import java.awt.Rectangle

class HoverMousePlugin[V,E]() extends AbstractGraphMousePlugin(0) with MouseMotionListener {

  def mouseDragged(e: MouseEvent) = mouseMoved(e)

  def mouseMoved(e: MouseEvent) = {

    
    val vv = e.getSource().asInstanceOf[VisualizationViewer[V,E] with HoverSupport[V,E]]
    val pickSupport = vv.getPickSupport()
    val hoverVertexState = vv.getHoverVertexState()
    if (pickSupport != null) {
      val layout = vv.getGraphLayout()
      
      val vs = pickSupport.getVertices(layout, new Rectangle(e.getX()-10, e.getY()-10, 20, 20))
      hoverVertexState.clear
      vs.foreach(hoverVertexState.pick(_, true))
      vv.repaint()
    }
  }

}

package com.jakemadethis.graphite.visualization

import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin
import java.awt.event.MouseMotionListener
import java.awt.event.MouseListener
import java.awt.event.MouseEvent
import java.awt.Point
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.Layer
import java.awt.geom.Point2D

class DraggingMousePlugin[V,E](picker : PickerMousePlugin[V,E]) extends AbstractGraphMousePlugin(0) with MouseMotionListener with MouseListener {

  var dragging : Seq[V] = null
  var offsets : Seq[Point2D] = null
  
  def mouseDragged(e: MouseEvent) {
    
  }

  def mouseMoved(e: MouseEvent) {
    
  }
  
  def mousePressed(e : MouseEvent) {
    val vv = e.getSource().asInstanceOf[VisualizationViewer[V,E]]
    val layout = vv.getGraphLayout()
    val trans = vv.getRenderContext().getMultiLayerTransformer()
    
    dragging = picker.getHoveredVertices.toList
    offsets = dragging.map { vertex =>
      val vp = trans.inverseTransform(Layer.VIEW, layout.transform(vertex));
      new Point2D.Double(e.getX() - vp.getX(), e.getY() - vp.getY())
    }
  }
  
  def mouseReleased(e : MouseEvent) {
  }
  
  def mouseClicked(e : MouseEvent) {}
  
  def mouseEntered(e : MouseEvent) {}
  def mouseExited(e : MouseEvent) {}

}
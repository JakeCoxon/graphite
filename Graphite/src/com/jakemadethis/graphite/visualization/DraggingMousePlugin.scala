package com.jakemadethis.graphite.visualization

import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin
import java.awt.event.MouseMotionListener
import java.awt.event.MouseListener
import java.awt.event.MouseEvent
import java.awt.Point
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.Layer
import java.awt.geom.Point2D
import collection.JavaConversions._

class DraggingMousePlugin[V,E] extends AbstractGraphMousePlugin(0) with MouseMotionListener with MouseListener {

  var offsets : Map[V, Point2D] = Map()
  
  def mouseDragged(e: MouseEvent) {
    val vv = e.getSource().asInstanceOf[VisualizationViewer[V,E]]
    val layout = vv.getGraphLayout()
    val trans = vv.getRenderContext().getMultiLayerTransformer()
    val pickedVertexState = vv.getPickedVertexState()
    
    pickedVertexState.getPicked().foreach { vertex =>
      val offset = offsets(vertex)
      val newP = new Point2D.Double(e.getX() - offset.getX(), e.getY() - offset.getY())
      
      layout.setLocation(vertex, trans.transform(Layer.LAYOUT, newP))
    }
    if (pickedVertexState.getPicked().size > 0) e.consume()
  }

  def mouseMoved(e: MouseEvent) {
    
  }
  
  def mousePressed(e : MouseEvent) {
    val vv = e.getSource().asInstanceOf[VisualizationViewer[V,E]]
    val layout = vv.getGraphLayout()
    val trans = vv.getRenderContext().getMultiLayerTransformer()
    val pickedVertexState = vv.getPickedVertexState()
    
    offsets = pickedVertexState.getPicked().map { vertex =>
      val vp = trans.inverseTransform(Layer.LAYOUT, layout.transform(vertex));
      vertex -> new Point2D.Double(e.getX() - vp.getX(), e.getY() - vp.getY())
    }.toMap
    
    if (offsets.size > 0) e.consume()
  }
  
  def mouseReleased(e : MouseEvent) {
  }
  
  def mouseClicked(e : MouseEvent) {}
  
  def mouseEntered(e : MouseEvent) {}
  def mouseExited(e : MouseEvent) {}

}

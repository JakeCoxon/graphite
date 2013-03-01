package com.jakemadethis.graphite.visualization

import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.event.MouseEvent
import edu.uci.ics.jung.visualization.VisualizationViewer
import java.awt.geom.Rectangle2D
import java.awt.Rectangle
import scala.collection.JavaConversions._
import java.awt.event.InputEvent
import java.awt.Point

class PickerMousePlugin[V,E] extends AbstractGraphMousePlugin(0)
    with MouseListener with MouseMotionListener {
  
  val SINGLE_SELECT = InputEvent.BUTTON1_MASK
  val MULTI_SELECT = InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK

  var downPoint : Point = null
  
  private def hasMovedFar(p1 : Point, p2 : Point) = p1.distance(p2) > 10
  
  def mousePressed(e : MouseEvent) {
    
    downPoint = e.getPoint()
    val vv = e.getSource().asInstanceOf[VisualizationViewer[V,E] with HoverSupport[V,E]]
    val pickedVertexState = vv.getPickedVertexState()
    val pickedEdgeState = vv.getPickedEdgeState()
    val hoverVertexState = vv.getHoverVertexState()
    
    if (pickedVertexState != null && hoverVertexState.getPicked().size > 0) {
      val hover = hoverVertexState.getPicked().last
      if (e.getModifiers() == SINGLE_SELECT) {
        if (!pickedVertexState.isPicked(hover)) {
          pickedVertexState.clear()
          pickedVertexState.pick(hover, true)
        }
      }
      else if (e.getModifiers() == MULTI_SELECT) {
        if (hoverVertexState.getPicked().size > 0) {
          pickedVertexState.pick(hover, true)
        }
      }
    }
  }
  
  def mouseReleased(e : MouseEvent) {
    val vv = e.getSource().asInstanceOf[VisualizationViewer[V,E] with HoverSupport[V,E]]
    val pickedVertexState = vv.getPickedVertexState()
    val hoverVertexState = vv.getHoverVertexState()
    
    if (e.getModifiers() == SINGLE_SELECT && hoverVertexState.getPicked().isEmpty) {
      if (!hasMovedFar(e.getPoint(), downPoint))
        pickedVertexState.clear()
    }
  }
  
  def mouseMoved(e : MouseEvent) {
    
  }
  def mouseDragged(e : MouseEvent) {
  }
  def mouseClicked(e : MouseEvent) {}
  
  def mouseEntered(e : MouseEvent) {}
  def mouseExited(e : MouseEvent) {}
}
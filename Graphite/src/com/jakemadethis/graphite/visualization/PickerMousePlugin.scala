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

  var hoveredVertices : collection.immutable.Iterable[V] = List()
  var downPoint : Point = null
  def getHoveredVertices = hoveredVertices
  def getHoveredVertex = hoveredVertices.headOption
  
  private def hasMovedFar(p1 : Point, p2 : Point) = p1.distance(p2) > 10
  
  def mousePressed(e : MouseEvent) {
    
    downPoint = e.getPoint()
    val vv = e.getSource().asInstanceOf[VisualizationViewer[V,E]]
    val pickedVertexState = vv.getPickedVertexState()
    val pickedEdgeState = vv.getPickedEdgeState()
    
    if (pickedVertexState != null) {
      if (e.getModifiers() == SINGLE_SELECT) {
        if (hoveredVertices.size > 0 && !pickedVertexState.isPicked(hoveredVertices.head)) {
          pickedVertexState.clear()
          pickedVertexState.pick(hoveredVertices.head, true)
        }
      }
      else if (e.getModifiers() == MULTI_SELECT) {
        if (hoveredVertices.size > 0) {
          pickedVertexState.pick(hoveredVertices.head, true)
        }
      }
    }
  }
  
  def mouseReleased(e : MouseEvent) {
    val vv = e.getSource().asInstanceOf[VisualizationViewer[V,E]]
    val pickedVertexState = vv.getPickedVertexState()
    
    if (e.getModifiers() == SINGLE_SELECT && hoveredVertices.isEmpty && !hasMovedFar(e.getPoint(), downPoint))
      pickedVertexState.clear()
  }
  
  def mouseMoved(e : MouseEvent) {
    
    val vv = e.getSource().asInstanceOf[VisualizationViewer[V,E]]
    val pickSupport = vv.getPickSupport()
    val pickedVertexState = vv.getPickedVertexState()
    val pickedEdgeState = vv.getPickedEdgeState()
    if (pickSupport != null) {
      val layout = vv.getGraphLayout()
      hoveredVertices = List() ++ pickSupport.getVertices(layout, new Rectangle(e.getX()-10, e.getY()-10, 20, 20))
    }
  }
  def mouseDragged(e : MouseEvent) = mouseMoved(e)
  def mouseClicked(e : MouseEvent) {}
  
  def mouseEntered(e : MouseEvent) {}
  def mouseExited(e : MouseEvent) {}
}
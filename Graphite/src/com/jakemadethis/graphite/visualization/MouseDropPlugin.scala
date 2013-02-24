package com.jakemadethis.graphite.visualization

import edu.uci.ics.jung.visualization.control.GraphMousePlugin
import scala.collection.JavaConversions._
import java.awt.event.MouseEvent
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin
import java.awt.event.MouseListener
import edu.uci.ics.jung.visualization.VisualizationViewer
import java.awt.Point
import java.awt.Rectangle

abstract class MouseDropPlugin[V,E](dragFilter : V => Boolean, dropFilter : V => Boolean)
extends AbstractGraphMousePlugin(0) with MouseListener {
  
  def vertexDropped(dragged : V, dropped : V)
  
  def mouseReleased(e : MouseEvent) {
    val vv = e.getSource().asInstanceOf[VisualizationViewer[V, E]]
    val pickSupport = vv.getPickSupport()
    val pickedVertexState = vv.getPickedVertexState()
    val layout = vv.getGraphLayout()
    if (pickedVertexState.getPicked().size() != 1) return
    val draggingVertex = pickedVertexState.getPicked().iterator().next() 
    
    if (dragFilter(draggingVertex)) {
        
      val vs = pickSupport.getVertices(layout, 
          new Rectangle(
              e.getPoint.getX().asInstanceOf[Int]-10, 
              e.getPoint.getY().asInstanceOf[Int]-10, 
              20, 
              20)
      ).filterNot(_ == draggingVertex).filter(dropFilter)
      
      if (vs.size > 0) {
        vertexDropped(draggingVertex, vs.head)
      }
    }
  }
  
  def mouseClicked(e : MouseEvent) {}
  def mouseEntered(e : MouseEvent) {}
  def  mouseExited(e : MouseEvent) {}
  def mousePressed(e : MouseEvent) {}
}
package com.jakemadethis.graphite.visualization

import edu.uci.ics.jung.visualization.control.GraphMousePlugin
import scala.collection.JavaConversions._
import java.awt.event.MouseEvent
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin
import java.awt.event.MouseListener
import edu.uci.ics.jung.visualization.VisualizationViewer
import java.awt.Point
import java.awt.Rectangle
import edu.uci.ics.jung.graph.Hypergraph

abstract class MouseDropPlugin[V,E]()
extends AbstractGraphMousePlugin(0) with MouseListener {
  
  def dragFilter(graph : Hypergraph[V,E], dragged : V) : Boolean
  def dropFilter(graph : Hypergraph[V,E], dropped : V) : Boolean
  
  def vertexDropped(graph : Hypergraph[V,E], dragged : V, dropped : V)
  
  def mouseReleased(e : MouseEvent) {
    val vv = e.getSource().asInstanceOf[VisualizationViewer[V, E]]
    val pickSupport = vv.getPickSupport()
    val pickedVertexState = vv.getPickedVertexState()
    val layout = vv.getGraphLayout()
    if (pickedVertexState.getPicked().size() != 1) return
    val draggingVertex = pickedVertexState.getPicked().iterator().next() 
    
    if (dragFilter(layout.getGraph(), draggingVertex)) {
        
      val vs = pickSupport.getVertices(layout, 
          new Rectangle(
              e.getPoint.getX().asInstanceOf[Int]-10, 
              e.getPoint.getY().asInstanceOf[Int]-10, 
              20, 
              20)
      ).filterNot(_ == draggingVertex).filter(dropFilter(layout.getGraph(),_))
      
      if (vs.size > 0) {
        vertexDropped(layout.getGraph(), draggingVertex, vs.head)
      }
    }
  }
  
  def mouseClicked(e : MouseEvent) {}
  def mouseEntered(e : MouseEvent) {}
  def  mouseExited(e : MouseEvent) {}
  def mousePressed(e : MouseEvent) {}
}
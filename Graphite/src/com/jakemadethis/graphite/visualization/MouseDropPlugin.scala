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
import java.awt.event.MouseMotionListener
import java.awt.geom.Rectangle2D
import edu.uci.ics.jung.visualization.VisualizationServer

abstract class MouseDropPlugin[V,E]()
extends AbstractGraphMousePlugin(0) with MouseListener with MouseMotionListener {
  
  def dragFilter(graph : Hypergraph[V,E], dragged : V) : Boolean
  def dropFilter(graph : Hypergraph[V,E], dropped : V) : Boolean
  
  def vertexDropped(vs : VisualizationServer[V,E], dragged : V, dropped : V)
  
  
  def mouseMoved(e : MouseEvent) {
    
  }
  def mouseDragged(e : MouseEvent) {
  }
  
  def mouseReleased(e : MouseEvent) {
    ifDrop(e)(vertexDropped)
  }
  
  protected def ifDrop(e : MouseEvent)(f : (VisualizationServer[V,E], V, V) => Unit) {
    val vv = e.getSource().asInstanceOf[VisualizationViewer[V, E] with HoverSupport[V,E]]
    val pickSupport = vv.getPickSupport()
    val pickedVertexState = vv.getPickedVertexState()
    val hoverVertexState = vv.getHoverVertexState()
    val layout = vv.getGraphLayout()
    
    if (pickedVertexState.getPicked().size != 1) return
    
    val dragged = pickedVertexState.getPicked().head
    
    if (dragFilter(layout.getGraph(), dragged)) {
        
      val hovers = hoverVertexState.getPicked().toList
      val dropped = hovers.reverseIterator.find { v =>
        v != dragged && dropFilter(layout.getGraph(), v)
      }
      
      dropped.foreach { f(vv, dragged, _) }
    }
  }

  def mousePressed(e : MouseEvent) {
  }
  
  def mouseClicked(e : MouseEvent) {}
  def mouseEntered(e : MouseEvent) {}
  def  mouseExited(e : MouseEvent) {}
}
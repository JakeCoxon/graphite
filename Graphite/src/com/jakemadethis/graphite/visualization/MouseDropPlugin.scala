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

abstract class MouseDropPlugin[V,E]()
extends AbstractGraphMousePlugin(0) with MouseListener with MouseMotionListener {
  
  def dragFilter(graph : Hypergraph[V,E], dragged : V) : Boolean
  def dropFilter(graph : Hypergraph[V,E], dropped : V) : Boolean
  
  def vertexDropped(graph : Hypergraph[V,E], dragged : V, dropped : V)
  
  protected var dragVertex : Option[V] = None
  protected var dropVertex : Option[V] = None
  
  def mouseMoved(e : MouseEvent) {
    
  }
  def mouseDragged(e : MouseEvent) {
    
    if (dragVertex.isEmpty) return
    
    val vv = e.getSource().asInstanceOf[VisualizationViewer[V, E]]
    val pickSupport = vv.getPickSupport()
    val pickedVertexState = vv.getPickedVertexState()
    
    dropVertex = None
    
    val layout = vv.getGraphLayout()
    
    if (dragFilter(layout.getGraph(), dragVertex.get)) {
        
      val vs = pickSupport.getVertices(layout, 
          new Rectangle(
              e.getPoint.getX().asInstanceOf[Int]-10, 
              e.getPoint.getY().asInstanceOf[Int]-10, 
              20, 
              20)
      ).filterNot(_ == dragVertex.get).filter(dropFilter(layout.getGraph(),_))
      
      dropVertex = vs.headOption
    }
  }
  
  def mouseReleased(e : MouseEvent) {
    val vv = e.getSource().asInstanceOf[VisualizationViewer[V, E]]
    val layout = vv.getGraphLayout()
    dropVertex.foreach(v =>
        vertexDropped(layout.getGraph(), dragVertex.get, v))
    dragVertex = None
    dropVertex = None
  }

  def mousePressed(e : MouseEvent) {
    val vv = e.getSource().asInstanceOf[VisualizationViewer[V, E]]
    val pickSupport = vv.getPickSupport()
    val pickedVertexState = vv.getPickedVertexState()
    
    dragVertex = if (pickedVertexState.getPicked().size() != 1) None 
      else Some(pickedVertexState.getPicked().iterator().next())
  }
  
  private def getHovers(e : MouseEvent) : Traversable[V] = {
    val vv = e.getSource().asInstanceOf[VisualizationViewer[V, E]]
    val pickSupport = vv.getPickSupport()
    val pickedVertexState = vv.getPickedVertexState()
    val pickedEdgeState = vv.getPickedEdgeState()
    if (pickSupport != null && pickedVertexState != null) {
      val layout = vv.getGraphLayout()
      pickSupport.getVertices(layout, new Rectangle2D.Double(e.getX()-10, e.getY()-10, 20, 20))
    } else List()
  }

  
  def mouseClicked(e : MouseEvent) {}
  def mouseEntered(e : MouseEvent) {}
  def  mouseExited(e : MouseEvent) {}
}
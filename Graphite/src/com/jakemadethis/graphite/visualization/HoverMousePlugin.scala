package com.jakemadethis.graphite.visualization

import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin
import scala.collection.JavaConversions._
import java.awt.event.MouseMotionListener
import java.awt.event.MouseEvent
import edu.uci.ics.jung.visualization.VisualizationViewer
import java.awt.Rectangle
import edu.uci.ics.jung.visualization.Layer
import java.awt.geom.Point2D

class HoverMousePlugin[V,E]() extends AbstractGraphMousePlugin(0) with MouseMotionListener {

  def mouseDragged(e: MouseEvent) = mouseMoved(e)

  def mouseMoved(e: MouseEvent) = {

    
    val vv = e.getSource().asInstanceOf[VisualizationViewer[V,E] with HoverSupport[V,E]]
    val mouse = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, 
            e.getPoint())
    val pickSupport = vv.getPickSupport()
    val hoverVertexState = vv.getHoverVertexState()
    val hoverEdgeState = vv.getHoverEdgeState()
    if (pickSupport != null) {
      val layout = vv.getGraphLayout()
      val g = layout.getGraph()
      
      val vs = g.getVertices().filter { vertex =>
        val loc = layout.transform(vertex)
        val screenLoc = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, loc)
        
        screenLoc.distanceSq(mouse) < 250
      }
      hoverVertexState.clear
      vs.foreach(hoverVertexState.pick(_, true))
      
      
      val edgeLayout = EdgeLayout(layout)
      val es = g.getEdges().filter { edge =>
        val loc = edgeLayout.getEdgeLocation(edge)
        val screenLoc = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, loc)
        
        screenLoc.distanceSq(mouse) < 250
      }
      hoverEdgeState.clear
      es.foreach(hoverEdgeState.pick(_, true))
      
      vv.repaint()
    }
  }

}

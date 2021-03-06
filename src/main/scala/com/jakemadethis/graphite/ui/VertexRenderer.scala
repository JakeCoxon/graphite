package com.jakemadethis.graphite.ui

import edu.uci.ics.jung.visualization.renderers.BasicVertexRenderer
import edu.uci.ics.jung.visualization.renderers.Renderer
import edu.uci.ics.jung.visualization.RenderContext
import edu.uci.ics.jung.algorithms.layout.Layout
import java.awt.Rectangle
import java.awt.Color
import java.awt.geom.Ellipse2D
import edu.uci.ics.jung.visualization.Layer
import java.awt.Paint
import java.awt.AlphaComposite
import edu.uci.ics.jung.visualization.VisualizationViewer
import com.jakemadethis.graphite.visualization.HoverSupport
import scala.collection.JavaConversions._
import com.jakemadethis.graphite.graph.Vertex
import com.jakemadethis.graphite.graph.Hyperedge
import com.jakemadethis.graphite.graph.FakeVertex
import com.jakemadethis.graphite.visualization.renderers.TextRenderer

class VertexRenderer(vv : VisualizationViewer[Vertex,Hyperedge] with HoverSupport[Vertex,Hyperedge]) extends Renderer.Vertex[Vertex, Hyperedge] {
  def paintVertex(rc: RenderContext[Vertex,Hyperedge], layout : Layout[Vertex,Hyperedge], v : Vertex) {
    val p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, layout.transform(v))
    val x = p.getX()
    val y = p.getY()
    val graph = vv.getGraphLayout().getGraph()
    
        
    if (!v.isInstanceOf[FakeVertex]) {
      
      val pickedEdgeState = vv.getPickedEdgeState()
//      if (graph.getIncidentEdges(v).exists(pickedEdgeState.isPicked(_))) {
//        circle(rc, x, y, 10, Color.BLACK)
//      }
//      else {
        val pickedVertexState = vv.getPickedVertexState()
        val hoverVertexState = vv.getHoverVertexState()
        val hovered = hoverVertexState.getPicked().size > 0 && hoverVertexState.getPicked().last == v
        val c = if (pickedVertexState.isPicked(v)) 
            Color.GREEN.darker() 
          else if (hovered)
            Color.GREEN.darker().darker() 
          else
            Color.BLACK
        circle(rc, x, y, 10, c)
//      }
    }
    
    
    vv.getModel() match {
      case model : DerivationModel =>
        model.externalNodeId(v) map { index => 
          val textRenderer = TextRenderer.getComponent(vv, index+1, null, Color.BLACK)
          val size = textRenderer.getPreferredSize()
          rc.getGraphicsContext().draw(textRenderer, rc.getRendererPane(), x.toInt - size.width/2, y.toInt - size.height/2 - 20, size.width, size.height, true)
        }
      case _ =>
    }
    
    
  
    
    
  }
  
  private def circle(rc : RenderContext[Vertex, Hyperedge], x : Double, y : Double, r : Double, paint : Paint) {
    val shape = new Ellipse2D.Double(x-r, y-r, r*2, r*2)
    val g = rc.getGraphicsContext()
    //g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC))
    val oldPaint = g.getPaint()
      g.setPaint(paint)
      g.fill(shape)
      g.setPaint(oldPaint)
  }
}
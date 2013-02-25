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

class VertexRenderer extends Renderer.Vertex[VisualItem, VisualEdge] {
  def paintVertex(rc: RenderContext[VisualItem,VisualEdge], layout : Layout[VisualItem,VisualEdge], v : VisualItem) {
    val p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, layout.transform(v))
    val x = p.getX()
    val y = p.getY()
        
    if (v.isInstanceOf[VisualFakeVertex]) {
      circle(rc, x, y, 7, new Color(1f, 0f, 0f, 0.5f))
    } else {
      circle(rc, x, y, 10, Color.BLACK)
    }
    
  }
  
  private def circle(rc : RenderContext[VisualItem, VisualEdge], x : Double, y : Double, r : Double, paint : Paint) {
    val shape = new Ellipse2D.Double(x-r, y-r, r*2, r*2)
    val g = rc.getGraphicsContext()
    //g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC))
    val oldPaint = g.getPaint()
      g.setPaint(paint)
      g.fill(shape)
      g.setPaint(oldPaint)
  }
}
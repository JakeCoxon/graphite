package com.jakemadethis.graphite.ui

import edu.uci.ics.jung.visualization.renderers.Renderer
import edu.uci.ics.jung.visualization.RenderContext
import edu.uci.ics.jung.algorithms.layout.Layout
import java.awt.Color
import edu.uci.ics.jung.graph.Hypergraph
import scala.collection.JavaConversions._
import edu.uci.ics.jung.visualization.Layer
import com.jakemadethis.graphite.visualization.EdgeLayout
import java.awt.BasicStroke
import edu.uci.ics.jung.visualization.renderers.EdgeArrowRenderingSupport
import com.jakemadethis.graphite.graph.Hyperedge
import com.jakemadethis.graphite.graph.Vertex

class EdgeRenderer() extends Renderer.Edge[Vertex, Hyperedge] {
  def paintEdge(rc : RenderContext[Vertex, Hyperedge], layout : Layout[Vertex, Hyperedge], edge : Hyperedge) {
    val gd = rc.getGraphicsContext()
    val oldPaint = gd.getPaint()
    
    gd.setPaint(Color.BLACK)
    
    val graph = layout.getGraph().asInstanceOf[Hypergraph[Vertex,Hyperedge]]
    
    val points = graph.getIncidentVertices(edge).map { v => 
      rc.getMultiLayerTransformer().transform(Layer.LAYOUT, layout.transform(v))
    }
    
    val edgeLayout = EdgeLayout(layout)
    val edgeLoc = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, edgeLayout.getEdgeLocation(edge))
    
    gd.setStroke(new BasicStroke(2))
    points.foreach { p =>
      gd.drawLine(edgeLoc.getX().toInt, edgeLoc.getY().toInt, p.getX().toInt, p.getY().toInt)
    }
    gd.setPaint(Color.RED.brighter())
    
    gd.drawLine(edgeLoc.getX().toInt, edgeLoc.getY().toInt, points.head.getX().toInt, points.head.getY().toInt)
    
    gd.setPaint(oldPaint)
  }
  def setEdgeArrowRenderingSupport(edgeArrowRenderingSupport : EdgeArrowRenderingSupport[_,_]) {
  }
  def getEdgeArrowRenderingSupport() = null
}
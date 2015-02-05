package com.jakemadethis.graphite.ui

import edu.uci.ics.jung.visualization.VisualizationViewer
import com.jakemadethis.graphite.graph._
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable
import java.awt.Graphics
import java.awt.Color
import java.awt.geom.Ellipse2D
import com.jakemadethis.graphite.visualization.EdgeLayout
import collection.JavaConversions._
import edu.uci.ics.jung.visualization.Layer
import com.jakemadethis.graphite.visualization.HoverSupport

class FakeVertexRenderer(vv : VisualizationViewer[Vertex,Hyperedge] with HoverSupport[Vertex,Hyperedge]) extends Paintable {
  def paint(g : Graphics) {
    val rc = vv.getRenderContext()
    val layout = vv.getGraphLayout()
    val graph = layout.getGraph()
    val edgeLayout = EdgeLayout(vv.getGraphLayout())
    val edges = vv.getPickedEdgeState().getPicked().filterNot {edgeLayout.isEdgeLocked(_)}
    val vs = edges.flatMap {graph.getIncidentVertices(_)} ++ graph.getVertices().filter {_.isInstanceOf[FakeVertex]}
    val hoverState = vv.getHoverVertexState()
    
    vs.foreach {v =>
      val pos = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, layout.transform(v))
      fakeVertex(g, pos.getX(), pos.getY(), hoverState.isPicked(v))
    }
  }
  
  
  def fakeVertex(g : Graphics, x : Double, y : Double, hover : Boolean) {
    val r = 7
    val shape = new Ellipse2D.Double(x-r, y-r, r*2, r*2)
    val alpha = if (hover) 0.8f else 0.5f 
    g.setColor(new Color(1f, 0f, 0f, alpha))
    g.fillArc(x.toInt-r, y.toInt-r, r*2, r*2, 0, 360)
  }
  def useTransform = true
}
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
import edu.uci.ics.jung.visualization.VisualizationViewer
import com.jakemadethis.graphite.visualization.HoverSupport
import com.jakemadethis.graphite.visualization.renderers.TextRenderer
import java.awt.geom.AffineTransform
import java.awt.Rectangle
import java.awt.geom.Point2D
import com.jakemadethis.graphite.graph.FakeVertex
import java.awt.geom.QuadCurve2D
import java.awt.geom.Line2D

class EdgeRenderer(vv: VisualizationViewer[Vertex, Hyperedge] with HoverSupport[Vertex, Hyperedge]) extends Renderer.Edge[Vertex, Hyperedge] {
  def paintEdge(rc: RenderContext[Vertex, Hyperedge], layout: Layout[Vertex, Hyperedge], edge: Hyperedge) {
    val gd = rc.getGraphicsContext()
    val oldPaint = gd.getPaint()

    val pickedEdgeState = vv.getPickedEdgeState()
    val hoverEdgeState = vv.getHoverEdgeState()
    val hovered = hoverEdgeState.getPicked().size > 0 && hoverEdgeState.getPicked().last == edge

    val c = if (pickedEdgeState.isPicked(edge))
      Color.GREEN.darker()
    else if (hovered)
      Color.GREEN.darker().darker()
    else
      Color.BLACK

    gd.setPaint(c)

    val graph = layout.getGraph().asInstanceOf[Hypergraph[Vertex, Hyperedge]]

    val incs = graph.getIncidentVertices(edge).toSeq
    val points = incs.map { v =>
      rc.getMultiLayerTransformer().transform(Layer.LAYOUT, layout.transform(v))
    }

    val edgeLayout = EdgeLayout(layout)
    val edgeLoc = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, edgeLayout.getEdgeLocation(edge))

    gd.setStroke(new BasicStroke(2))

    val drawTentacleLabels = pickedEdgeState.isPicked(edge) || hovered
    
    def alt(id : Int, total : Int) = {
      // -2  -1  0  1
      // -2  -1  0  1  2
      val i = id - total/2
      // -1.5  -0.5  0.5  1.5
      // -2  -1  0  1  2
      if (total % 2 == 0) i + 0.5f else i
    }
    
    def curveShape(index : Int, total : Int) = {
      val controlY = alt(index, total) * 40f
      new QuadCurve2D.Float(0.0f, 0.0f, 0.5f, controlY, 1.0f, 0.0f)
    }
    def curveTextPoint(index : Int, total : Int) = {
      val controlY = alt(index, total) * 20f
      new Point2D.Double(0.5f, controlY)
    }
    
    val grouped = points.zipWithIndex.groupBy { p => p._1 }
    grouped.foreach {
      case (p, points) =>
        val shapes = points.zipWithIndex.map { case ((p, tentId), i) =>
          val shape = curveShape(i, points.size)
          val point = curveTextPoint(i, points.size)
          (shape, point, tentId)
        }
        
        val xform = AffineTransform.getTranslateInstance(edgeLoc.getX, edgeLoc.getY)
        val dx = p.getX - edgeLoc.getX
        val dy = p.getY - edgeLoc.getY
        val thetaRadians = math.atan2(dy, dx)
        xform.rotate(thetaRadians)
        val dist = math.sqrt(dx*dx + dy*dy)
        xform.scale(dist, 1.0)
        
          
        shapes.foreach { case(shape, textp, tentId) =>
          
          val edgeShape = xform.createTransformedShape(shape)
          gd.draw(edgeShape)
          
          if (drawTentacleLabels) {
            xform.transform(textp, textp)
            
            val textRenderer = TextRenderer.getComponent(vv, tentId+1, null, Color.BLACK)
            val size = textRenderer.getPreferredSize()
            rc.getGraphicsContext().draw(textRenderer, rc.getRendererPane(), textp.getX.toInt - size.width / 2, textp.getY.toInt - size.height / 2, size.width, size.height, true)
          }
          
        }
          
    }
    
    //gd.drawLine(edgeLoc.getX().toInt, edgeLoc.getY().toInt, p.getX().toInt, p.getY().toInt)
          

    if (edge.label == null || edge.label.size == 0 && points.toSet.size == 2) {

      val d = points(1).distance(points(0))
      val dx = (points(1).getX - points(0).getX) / d
      val dy = (points(1).getY - points(0).getY) / d
      val p0 = points(0)
      // Shift p1 down by radius of vertex
      val p1 = if (incs(1).isInstanceOf[FakeVertex]) points(1) else
        new Point2D.Double(points(1).getX - dx * 10, points(1).getY - dy * 10)

      val alpha = 10
      val beta = 5

      val arrowp0 = new Point2D.Double(p1.getX - dx * alpha + dy * beta, p1.getY - dy * alpha - dx * beta)
      val arrowp1 = new Point2D.Double(p1.getX - dx * alpha - dy * beta, p1.getY - dy * alpha + dx * beta)
      gd.drawLine(p1.getX.toInt, p1.getY.toInt, arrowp0.getX.toInt, arrowp0.getY.toInt)
      gd.drawLine(p1.getX.toInt, p1.getY.toInt, arrowp1.getX.toInt, arrowp1.getY.toInt)

    }
    else {

      // Label

      val textRenderer = TextRenderer.getComponent(vv, edge.label, null, Color.BLACK)
      val size = textRenderer.getPreferredSize()

      val old = gd.getTransform()
      val xform = new AffineTransform(old)
      xform.translate(edgeLoc.getX(), edgeLoc.getY())

      gd.setTransform(xform)
      gd.setStroke(new BasicStroke(1))
      val rect = new Rectangle(-10, -10, 20, 20)
      gd.setPaint(Color.WHITE)
      gd.fill(rect)
      gd.setPaint(Color.GRAY)
      gd.draw(rect)

      xform.translate(-size.width / 2, -size.height / 2)
      gd.setTransform(xform)

      gd.setTransform(old)
      rc.getGraphicsContext().draw(textRenderer, rc.getRendererPane(), edgeLoc.getX.toInt - size.width / 2, edgeLoc.getY.toInt - size.height / 2, size.width, size.height, true)
    }

    gd.setPaint(oldPaint)
  }
  def setEdgeArrowRenderingSupport(edgeArrowRenderingSupport: EdgeArrowRenderingSupport[_, _]) {
  }
  def getEdgeArrowRenderingSupport() = null
}
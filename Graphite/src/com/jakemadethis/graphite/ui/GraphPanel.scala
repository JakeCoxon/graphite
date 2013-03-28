package com.jakemadethis.graphite.ui

import edu.uci.ics.jung.visualization.VisualizationViewer
import com.jakemadethis.graphite.graph._
import scala.swing._
import com.jakemadethis.graphite.visualization.HoverSupport
import org.apache.commons.collections15.Transformer
import java.awt.event.ItemListener
import java.awt.event.ItemEvent
import com.jakemadethis.graphite.visualization.renderers.BasicHypergraphRenderer
import com.jakemadethis.graphite.visualization.renderers.HyperedgeLabelRenderer
import org.apache.commons.collections15.Predicate
import javax.swing.border.LineBorder
import java.awt.Color
import edu.uci.ics.jung.graph.util.Context
import edu.uci.ics.jung.graph._
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.Layer
import collection.JavaConversions._
import java.awt.geom.Point2D
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable
import java.awt.Graphics

class GraphPanel(model_ : VisualizationModel[Vertex, Hyperedge]) 
    extends VisualizationViewer[Vertex, Hyperedge](model_, new Dimension(500, 500)) 
    with HoverSupport[Vertex, Hyperedge] {

  val vv = this
  
  setGraphMouse(new GraphMouseHandler())
  
  getRenderContext().setEdgeLabelTransformer(new Transformer[Hyperedge, String]() {
    def transform(e : Hyperedge) = e.label
  })

  
  getRenderContext().getPickedVertexState().addItemListener(new ItemListener() {
    def itemStateChanged(e : ItemEvent) {
      val v = e.getItem().asInstanceOf[Vertex]
      getGraphLayout().lock(v, e.getStateChange() == ItemEvent.SELECTED)
    }
  })
  

  // Setup renderers
  setRenderer(new BasicHypergraphRenderer[Vertex, Hyperedge]() {
    setVertexRenderer(new VertexRenderer(vv))
    setEdgeRenderer(new EdgeRenderer(vv))
    
    setEdgeLabelRenderer(new HyperedgeLabelRenderer[Vertex, Hyperedge]() {
      setDrawPredicate(new Predicate[Context[Hypergraph[Vertex, Hyperedge], Hyperedge]]() {
        def evaluate(c : Context[Hypergraph[Vertex, Hyperedge], Hyperedge]) = true
      })
    })
    
  })
  
  addPostRenderPaintable(new FakeVertexRenderer(vv))
  
  addPostRenderPaintable(new Paintable() {
    
    def paint(g : Graphics) {
      val rc = vv.getRenderContext()
      val graphlayout = vv.getGraphLayout()
      
      
      val view = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)
      val layout = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)

      val scale = view.getScale()
      
      val vs = graphlayout.getGraph.getVertices().toList
      val ps = vs.map(v => layout.transform(graphlayout.transform(v)))
      val xs = ps.map(_.getX())
      val ys = ps.map(_.getY())
      val midX = (xs.min + xs.max) / 2
      val midY = (ys.min + ys.max) / 2
      
      g.setColor(Color.RED)
      g.drawRect(midX.toInt, midY.toInt, 2, 2)
    
      val ctr = view.inverseTransform(vv.getCenter())
      
      g.drawRect(ctr.getX().toInt, ctr.getY().toInt, 2, 2)
    }
  
    
    def useTransform = false
  })
    
    
  setBorder(Swing.BeveledBorder(Swing.Lowered))
  setFocusable(true)
  
  def graph = getGraphLayout().getGraph()
  def graphLayout = getGraphLayout()
  def model_=(model : VisualizationModel[Vertex, Hyperedge]) { setModel(model) }
  
  def center() {
    val view = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)
    val layout = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)

    val vs = getGraphLayout.getGraph.getVertices().toList
    val ps = vs.map(v => view.transform(layout.transform(getGraphLayout.transform(v))))
    val xs = ps.map(_.getX())
    val ys = ps.map(_.getY())
    val midX = (xs.min + xs.max) / 2
    val midY = (ys.min + ys.max) / 2

    val p = new Point2D.Double(midX, midY)
    
    val ctr = vv.getCenter(); 
    val pnt = view.inverseTransform(ctr);

    val scale = view.getScale();

    val deltaX = -(p.getX() - pnt.getX()) / scale;
    val deltaY = -(p.getY() - pnt.getY()) / scale;
    val delta = new Point2D.Double(deltaX, deltaY);

//    layout.translate(deltaX, deltaY);
    layout.setTranslate(0, 0)
    view.setTranslate(0, 0)
  }
  
  override def setModel(model : VisualizationModel[Vertex, Hyperedge]) {
    super.setModel(model)
    getPickedEdgeState().clear()
    getPickedVertexState().clear()
    center
  }
}
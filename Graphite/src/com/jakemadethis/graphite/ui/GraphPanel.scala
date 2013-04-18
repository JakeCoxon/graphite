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
import edu.uci.ics.jung.visualization.renderers.Renderer.EdgeLabel
import edu.uci.ics.jung.visualization.RenderContext
import edu.uci.ics.jung.algorithms.layout.Layout

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
    
    setEdgeLabelRenderer(new EdgeLabel[Vertex, Hyperedge] {
      def labelEdge(rc : RenderContext[Vertex, Hyperedge], layout : Layout[Vertex,Hyperedge], e : Hyperedge, label : String) {}
    })
//    setEdgeLabelRenderer(new HyperedgeLabelRenderer[Vertex, Hyperedge]() {
//      setDrawPredicate(new Predicate[Context[Hypergraph[Vertex, Hyperedge], Hyperedge]]() {
//        def evaluate(c : Context[Hypergraph[Vertex, Hyperedge], Hyperedge]) = true
//      })
//    })
    
  })
  
  addPostRenderPaintable(new FakeVertexRenderer(vv))
    
    
  setBorder(Swing.BeveledBorder(Swing.Lowered))
  setFocusable(true)
  
  def graph = getGraphLayout().getGraph()
  def graphLayout = getGraphLayout()
  def model_=(model : VisualizationModel[Vertex, Hyperedge]) { setModel(model) }
  
  override def setModel(model : VisualizationModel[Vertex, Hyperedge]) {
    super.setModel(model)
    getPickedEdgeState().clear()
    getPickedVertexState().clear()
  }
}
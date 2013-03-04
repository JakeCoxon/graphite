package com.jakemadethis.graphite.ui

import javax.swing.JPanel
import scala.collection.JavaConversions._
import edu.uci.ics.jung.visualization._
import edu.uci.ics.jung.visualization.picking._
import com.jakemadethis.graphite.graph.Vertex
import com.jakemadethis.graphite.graph.Hyperedge
import java.awt.Dimension
import edu.uci.ics.jung.algorithms.layout.FRLayout
import edu.uci.ics.jung.graph.Hypergraph
import edu.uci.ics.jung.graph.Graph
import com.jakemadethis.graphite.visualization.HyperedgePickSupport
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller
import org.apache.commons.collections15.functors.ConstantTransformer
import java.awt.geom.Ellipse2D
import org.apache.commons.collections15.functors.TruePredicate
import edu.uci.ics.jung.graph.util.Context
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer
import java.awt.Color
import com.jakemadethis.graphite.visualization.renderers.MultiPickableVertexPaint
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import com.jakemadethis.graphite.visualization.renderers.BasicHypergraphRenderer
import com.jakemadethis.graphite.visualization.renderers.HyperedgeLabelRenderer
import org.apache.commons.collections15.Predicate
import com.jakemadethis.graphite.visualization.EdgeLayout
import java.awt.Point
import org.apache.commons.collections15.Transformer
import com.jakemadethis.graphite.visualization.MouseDropPlugin
import com.jakemadethis.graphite.graph.GraphExtensions._
import com.jakemadethis.graphite.visualization.HoverSupport
import com.jakemadethis.graphite.visualization.BasicEdgeLayout
import edu.uci.ics.jung.algorithms.layout.StaticLayout
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer
import edu.uci.ics.jung.algorithms.layout.Layout

class GraphPanel(model : VisualizationModel[Vertex, Hyperedge]) extends JPanel {
  
  
  val visualization = new VisualizationViewer[Vertex, Hyperedge](model, new Dimension(500, 500)) 
      with HoverSupport[Vertex, Hyperedge] {
    
    setGraphMouse(new GraphMouseHandler())
    setPickSupport(new HyperedgePickSupport[Vertex, Hyperedge](this))
    
    getRenderContext().setEdgeLabelTransformer(new Transformer[Hyperedge, String]() {
      def transform(e : Hyperedge) = e.label
    })
    //getRenderContext().setVertexShapeTransformer(new ConstantTransformer(
    //      new Ellipse2D.Float(-8,-8,16,16)));
    //getRenderContext().setEdgeArrowPredicate(TruePredicate.getInstance());
    //getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer[Hyperedge](hoverEdgeState, Color.gray, Color.cyan));
    //getRenderContext().setVertexFillPaintTransformer(new MultiPickableVertexPaint[Vertex](
    //  getPickedVertexState(), Color.cyan, hoverVertexState, Color.cyan.darker(), Color.black));
    //v.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<V>(hoverVertexState, Color.black, Color.yellow));

    //getModel().getRelaxer().setSleepTime(10);
    
    getRenderContext().getPickedVertexState().addItemListener(new ItemListener() {
      def itemStateChanged(e : ItemEvent) {
        val v = e.getItem().asInstanceOf[Vertex]
        getGraphLayout().lock(v, e.getStateChange() == ItemEvent.SELECTED)
      }
    })
    
    //v.addKeyListener(gm.getModeKeyListener());
    setFocusable(true)
    
    setRenderer(new BasicHypergraphRenderer[Vertex, Hyperedge]())
    
    getRenderer().setVertexRenderer(new VertexRenderer(this))
    
    val lr = new HyperedgeLabelRenderer[Vertex, Hyperedge]()
    lr.setDrawPredicate(new Predicate[Context[Hypergraph[Vertex, Hyperedge], Hyperedge]]() {
      def evaluate(c : Context[Hypergraph[Vertex, Hyperedge], Hyperedge]) = true
    })
    
    getRenderer().setEdgeLabelRenderer(lr);
    
//      val r = new HyperedgeRenderer[Vertex, Hyperedge](edgeLayout)
//      r.setDrawAsHyperedge(new Predicate[Context[Hypergraph[Vertex, Hyperedge], Hyperedge]]() {
//        def evaluate(c : Context[Hypergraph[Vertex, Hyperedge], Hyperedge]) = true
//      })
//      
//      getRenderer().setEdgeRenderer(r)
    getRenderer().setEdgeRenderer(new EdgeRenderer())
  }
  
  add(visualization)
    
  def graph = visualization.getModel().getGraphLayout().getGraph()
  
  def setGraphModel(model : VisualizationModel[Vertex, Hyperedge]) {
    println(model)
    visualization.setModel(model)
    visualization.repaint()
  }
  
  
  
}
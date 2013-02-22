package com.jakemadethis.graphite.ui

import javax.swing.JPanel
import edu.uci.ics.jung.visualization._
import edu.uci.ics.jung.visualization.picking._
import com.jakemadethis.graphite.graph.Vertex
import com.jakemadethis.graphite.graph.Hyperedge
import java.awt.Dimension
import edu.uci.ics.jung.algorithms.layout.FRLayout
import edu.uci.ics.jung.graph.Hypergraph
import edu.uci.ics.jung.graph.Graph
import com.jakemadethis.graph.visualization.MyGraphMouse
import com.jakemadethis.graph.visualization.HyperedgePickSupport
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller
import org.apache.commons.collections15.functors.ConstantTransformer
import java.awt.geom.Ellipse2D
import org.apache.commons.collections15.functors.TruePredicate
import edu.uci.ics.jung.graph.util.Context
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer
import java.awt.Color
import com.jakemadethis.graph.visualization.MultiPickableVertexPaint
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import com.jakemadethis.graph.visualization.BasicHypergraphRenderer
import com.jakemadethis.graph.visualization.HyperedgeLabelRenderer
import org.apache.commons.collections15.Predicate
import com.jakemadethis.graph.visualization.HyperedgeRenderer
import com.jakemadethis.graph.visualization.EdgeLayout
import java.awt.Point
import org.apache.commons.collections15.Transformer

class GraphPanel extends JPanel {
  val hoverVertexState = new MultiPickedState[Vertex]();
  val hoverEdgeState = new MultiPickedState[Hyperedge]();
  var visualization : VisualizationViewer[Vertex, Hyperedge] = null
    
  def setGraph(graph : Hypergraph[Vertex,Hyperedge]) {
    if (visualization != null) remove(visualization)
    val pseudoGraph = graph.asInstanceOf[Graph[Vertex,Hyperedge]];
    
    val glayout = 
            new FRLayout[Vertex, Hyperedge](pseudoGraph, new Dimension(500, 500))
            
    val edgeLayout = new EdgeLayout[Hyperedge]() {
      def getEdgeLocation(e : Hyperedge) : Point = {
        val es = glayout.getGraph().getIncidentVertices(e)
        if (es.size == 2) {
          val iterator = graph.getIncidentVertices(e).iterator();
          val p1 = glayout.transform(iterator.next());
          val p2 = glayout.transform(iterator.next());
          return new Point(((p1.getX() + p2.getX())/2).asInstanceOf[Int], ((p1.getY() + p2.getY())/2).asInstanceOf[Int]);
        }
        new Point(0,0)
      }
    }
    
    // create visualization viewer
    visualization = new VisualizationViewer[Vertex, Hyperedge](glayout, new Dimension(500, 500)) {
      
      val gm = new MyGraphMouse[Vertex, Hyperedge](hoverVertexState, hoverEdgeState)
      setGraphMouse(gm);
      setPickSupport(new HyperedgePickSupport[Vertex, Hyperedge](this));
      
      getRenderContext().setEdgeLabelTransformer(new Transformer[Hyperedge, String]() {
        def transform(e : Hyperedge) = e.label
      })
      //getRenderContext().setVertexShapeTransformer(new ConstantTransformer(
      //      new Ellipse2D.Float(-8,-8,16,16)));
      getRenderContext().setEdgeArrowPredicate(TruePredicate.getInstance());
      getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer[Hyperedge](hoverEdgeState, Color.gray, Color.cyan));
      getRenderContext().setVertexFillPaintTransformer(new MultiPickableVertexPaint[Vertex](
        getPickedVertexState(), Color.cyan, hoverVertexState, Color.cyan.darker(), Color.black));
      //v.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<V>(hoverVertexState, Color.black, Color.yellow));
  
      getModel().getRelaxer().setSleepTime(10);
      
      getRenderContext().getPickedVertexState().addItemListener(new ItemListener() {
        def itemStateChanged(e : ItemEvent) {
          val v = e.getItem().asInstanceOf[Vertex]
          visualization.getGraphLayout().lock(v, e.getStateChange() == ItemEvent.SELECTED)
        }
      })
      
      //v.addKeyListener(gm.getModeKeyListener());
      setFocusable(true);
    
      setRenderer(new BasicHypergraphRenderer[Vertex, Hyperedge]())
      
      val lr = new HyperedgeLabelRenderer[Vertex, Hyperedge](edgeLayout)
      lr.setDrawPredicate(new Predicate[Context[Hypergraph[Vertex,Hyperedge], Hyperedge]]() {
        def evaluate(c : Context[Hypergraph[Vertex,Hyperedge], Hyperedge]) = true
      })
      
      getRenderer().setEdgeLabelRenderer(lr);
      
      val r = new HyperedgeRenderer[Vertex, Hyperedge](edgeLayout)
      r.setDrawAsHyperedge(new Predicate[Context[Hypergraph[Vertex,Hyperedge], Hyperedge]]() {
        def evaluate(c : Context[Hypergraph[Vertex,Hyperedge], Hyperedge]) = true
      })
      
      getRenderer().setEdgeRenderer(r)
    }
    
    add(visualization)
  }
}
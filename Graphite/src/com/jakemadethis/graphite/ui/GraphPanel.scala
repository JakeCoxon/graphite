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
import com.jakemadethis.graphite.visualization.MyGraphMouse
import com.jakemadethis.graphite.visualization.HyperedgePickSupport
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller
import org.apache.commons.collections15.functors.ConstantTransformer
import java.awt.geom.Ellipse2D
import org.apache.commons.collections15.functors.TruePredicate
import edu.uci.ics.jung.graph.util.Context
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer
import java.awt.Color
import com.jakemadethis.graphite.visualization.MultiPickableVertexPaint
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import com.jakemadethis.graphite.visualization.BasicHypergraphRenderer
import com.jakemadethis.graphite.visualization.HyperedgeLabelRenderer
import org.apache.commons.collections15.Predicate
import com.jakemadethis.graphite.visualization.HyperedgeRenderer
import com.jakemadethis.graphite.visualization.EdgeLayout
import java.awt.Point
import org.apache.commons.collections15.Transformer
import com.jakemadethis.graphite.visualization.VisualItem
import com.jakemadethis.graphite.visualization.VisualEdge
import com.jakemadethis.graphite.visualization.MouseDropPlugin
import com.jakemadethis.graphite.visualization.VisualFakeVertex
import com.jakemadethis.graphite.graph.GraphExtensions._

class GraphPanel extends JPanel {
  var visualization : VisualizationViewer[VisualItem, VisualEdge] = null
    
  def setGraph(graph : Hypergraph[VisualItem, VisualEdge]) {
    if (visualization != null) remove(visualization)
    val pseudoGraph = graph.asInstanceOf[Graph[VisualItem, VisualEdge]];
    
    val glayout = 
            new FRLayout[VisualItem, VisualEdge](pseudoGraph, new Dimension(500, 500))
            
    val edgeLayout = new EdgeLayout[VisualEdge]() {
      def getEdgeLocation(e : VisualEdge) : Point = {
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
    visualization = new VisualizationViewer[VisualItem, VisualEdge](glayout, new Dimension(500, 500)) {
      
      
      val gm = new MyGraphMouse[VisualItem, VisualEdge]()
      gm.add(new MouseDropPlugin[VisualItem, VisualEdge](
          {a => a.isInstanceOf[VisualFakeVertex]}, {a => !a.isInstanceOf[VisualFakeVertex]}) {
        def vertexDropped(drag : VisualItem, drop : VisualItem) {
          graph.merge(drag, drop)
        }
      })
      setGraphMouse(gm);
      setPickSupport(new HyperedgePickSupport[VisualItem, VisualEdge](this));
      
      getRenderContext().setEdgeLabelTransformer(new Transformer[VisualEdge, String]() {
        def transform(e : VisualEdge) = e.edge.label
      })
      //getRenderContext().setVertexShapeTransformer(new ConstantTransformer(
      //      new Ellipse2D.Float(-8,-8,16,16)));
      //getRenderContext().setEdgeArrowPredicate(TruePredicate.getInstance());
      //getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer[VisualEdge](hoverEdgeState, Color.gray, Color.cyan));
      //getRenderContext().setVertexFillPaintTransformer(new MultiPickableVertexPaint[VisualItem](
      //  getPickedVertexState(), Color.cyan, hoverVertexState, Color.cyan.darker(), Color.black));
      //v.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<V>(hoverVertexState, Color.black, Color.yellow));
  
      getModel().getRelaxer().setSleepTime(10);
      
      getRenderContext().getPickedVertexState().addItemListener(new ItemListener() {
        def itemStateChanged(e : ItemEvent) {
          val v = e.getItem().asInstanceOf[VisualItem]
          visualization.getGraphLayout().lock(v, e.getStateChange() == ItemEvent.SELECTED)
        }
      })
      
      //v.addKeyListener(gm.getModeKeyListener());
      setFocusable(true);
    
      setRenderer(new BasicHypergraphRenderer[VisualItem, VisualEdge]())
      
      val lr = new HyperedgeLabelRenderer[VisualItem, VisualEdge](edgeLayout)
      lr.setDrawPredicate(new Predicate[Context[Hypergraph[VisualItem, VisualEdge], VisualEdge]]() {
        def evaluate(c : Context[Hypergraph[VisualItem, VisualEdge], VisualEdge]) = true
      })
      
      getRenderer().setEdgeLabelRenderer(lr);
      
      val r = new HyperedgeRenderer[VisualItem, VisualEdge](edgeLayout)
      r.setDrawAsHyperedge(new Predicate[Context[Hypergraph[VisualItem, VisualEdge], VisualEdge]]() {
        def evaluate(c : Context[Hypergraph[VisualItem, VisualEdge], VisualEdge]) = true
      })
      
      getRenderer().setEdgeRenderer(r)
    }
    
    add(visualization)
  }
}
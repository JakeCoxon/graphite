package com.jakemadethis.graphite.ui

import scala.swing._
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
import org.apache.commons.collections15.functors.TruePredicate
import edu.uci.ics.jung.graph.util.Context
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer
import java.awt.Color
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
import java.awt.event.ItemListener
import java.awt.event.ItemEvent
import javax.swing.BorderFactory
import com.jakemadethis.graphite.graph.OrderedHypergraph
import com.jakemadethis.graphite.graph.DerivationModel
import com.jakemadethis.graphite.visualization.AverageEdgeLayout
import com.jakemadethis.graphite.graph.HypergraphDerivation
import com.jakemadethis.graphite.graph.HyperedgeGraph
import com.jakemadethis.graphite.visualization.HyperedgeLayout
import com.jakemadethis.graphite.graph.FakeVertex
import java.awt.geom.Dimension2D

class DerivationPanel(model : DerivationModel) extends BoxPanel(Orientation.NoOrientation) {
  
  var currentModel : DerivationModel = model
  
  val leftVis = new GraphPanel(newLeftModel(model.derivation))
  val visualization = new GraphPanel(model)
  
  val rightBox = new BoxPanel(Orientation.Vertical) {
    val menubar = new FlowPanel() {
      background = Color.DARK_GRAY
      contents += new NoFocusButton(Action("Add Vertex") {
        graph.addVertex(new Vertex())
        visualization.repaint()
      })
      
      contents += new NoFocusButton(Action("Add Edge...") {
        def addEdge(d : EdgeDialogObject) {
          val vs = (1 to d.sizing).map {i => new FakeVertex()}
          vs foreach { graph.addVertex(_) }
          graph.addEdge(new Hyperedge(d.label, d.termination), vs)
          visualization.repaint()
        }
        new EdgeDialog(null)(addEdge(_)) {
          centerOnScreen
          open
        }
      })
      
      // When deleting a vertex attached to an edge, the vertex should be replaced with a fake vertex
      def replaceWithFakeVertex(g : Hypergraph[Vertex, Hyperedge], vertexToReplace : Vertex) {
        
        val edges = graph.getIncidentEdges(vertexToReplace)
        
        edges.toList.foreach { edge => 
          val fake = new FakeVertex()
          val newIncidents = graph.getIncidentVertices(edge).map(a => 
            if (a == vertexToReplace) fake else a)
          graph.removeEdge(edge)
          graph.addEdge(edge, newIncidents)
          
          // Fake vertex should have same position as old vertex
          visualization.graphLayout.setLocation(fake, 
              visualization.graphLayout.transform(vertexToReplace))
        }
    
      }
      def removeItems(vertices : Set[Vertex], edges : Set[Hyperedge]) {
        
        val vs = (vertices filterNot {_.isInstanceOf[FakeVertex]}) -- currentModel.derivation.externalNodes
        val es = edges
        val edgesToReplace = vs.flatMap { v => graph.getIncidentEdges(v) } -- es
        
        if (es.size + vs.size > 0) {
          def dialog = {
            val vtext = vs.size match { case 0 => null case 1 => "1 vertex" case x => x + " vertices" }
            val etext = es.size match { case 0 => null case 1 => "1 edge" case x => x + " edges" }
            val text = List(vtext, etext).filter(_ != null).mkString(" and ")
            
            Dialog.showConfirmation(this, 
              message="Delete "+text+"?", 
              title="Delete?")
          }
                
          if (es.size + vs.size == 1 || dialog == Dialog.Result.Ok) {
            es.foreach { e => 
              graph.getIncidentVertices(e) filter {_.isInstanceOf[FakeVertex]} foreach {graph.removeVertex(_)}
              graph.removeEdge(e)
              
              setPicked(e, false)
            }
            vs.foreach { v => 
              if (graph.getIncidentEdges(v).exists(edgesToReplace.contains(_))) {
                replaceWithFakeVertex(graph, v)
              }
              graph.removeVertex(v)
              setPicked(v, false) 
            }
            
            visualization.repaint()
          }
        }
      }
      contents += new NoFocusButton(Action("Delete") {
        removeItems(pickedVertices, pickedEdges)
      })
      
      contents += new NoFocusButton(Action("Clear") {
        removeItems(graph.getVertices().toSet, graph.getEdges().toSet)
      })
      
      contents += new NoFocusButton(Action("Edit Edge...") {
        if (pickedEdges.size == 1) {
          val edge = pickedEdges.head
          val oldvs = graph.getIncidentVertices(edge).toList
          
          def editEdge(d : EdgeDialogObject) {
            graph.removeEdge(edge)
            val vs = oldvs.take(d.sizing) ++ 
              ((oldvs.size until d.sizing) map {i => new FakeVertex()})
            oldvs.drop(d.sizing) filter {_.isInstanceOf[FakeVertex]} foreach {graph.removeVertex(_)}
            graph.addEdge(new Hyperedge(d.label, d.termination), vs)
            visualization.repaint()
          }
          
          val obj = new EdgeDialogObject(oldvs.size, edge.label, edge.termination)
          new EdgeDialog(null, obj)(editEdge(_)) {
            centerOnScreen
            open
          }
        }
      })
      minimumSize = new Dimension(0, minimumSize.getHeight().toInt)
      maximumSize = new Dimension(Int.MaxValue, minimumSize.getHeight().toInt)
    }
    contents += menubar
    contents += Component.wrap(visualization)
  }
  val split = new SplitPane(Orientation.Vertical, Component.wrap(leftVis),
      rightBox) {
    dividerLocation = 300
    resizeWeight = 0.3
  }
  
  contents += split
      
    
  def graph = visualization.graph
  
  private def newLeftModel(derivation : HypergraphDerivation) = {
    
    val g = new HyperedgeGraph(derivation.label, derivation.deriveType)
    val rand = new RandomLocationTransformer[Vertex](new Dimension(500,500))
    val layout = new HyperedgeLayout(g, new Dimension(500,500)) 
    
    
    new LeftsideModel(g, layout)
  }
  
  def graphModel = visualization.getModel
  def graphModel_=(model : DerivationModel) {
    currentModel = model
    leftVis.setModel(newLeftModel(model.derivation))
    leftVis.repaint()
    visualization.setModel(model)
    visualization.repaint()
  }
  
  def pickedVertices = visualization.getPickedVertexState.getPicked().toSet
  def pickedEdges = visualization.getPickedEdgeState.getPicked().toSet
  def setPicked(v : Vertex, picked : Boolean) = visualization.getPickedVertexState.pick(v, picked)
  def setPicked(e : Hyperedge, picked : Boolean) = visualization.getPickedEdgeState.pick(e, picked)
  
  
}

case class LeftsideModel[V,E](val graph : Hypergraph[V, E], layout_ : Layout[V, E]) extends DefaultVisualizationModel[V, E](layout_) {
  
}
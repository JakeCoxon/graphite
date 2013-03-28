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
import com.jakemadethis.graphite.visualization.AverageEdgeLayout
import com.jakemadethis.graphite.visualization.HyperedgeLayout
import java.awt.geom.Dimension2D
import com.jakemadethis.graphite.graph._

class DerivationPanel(derivPair : DerivationPair) extends BoxPanel(Orientation.NoOrientation) {
  
  border = Swing.EmptyBorder
  var currentPair : DerivationPair = derivPair
  
  val leftVis = new GraphPanel(derivPair.leftSide)
  val rightVis = new GraphPanel(derivPair.rightSide)
  
  val leftBox = new BoxPanel(Orientation.Vertical) {
    val menubar = new FlowPanel() {
      background = Color.DARK_GRAY
      contents += new NoFocusButton(Action("Edit edge...") {
        
        def editEdge(d : EdgeDialogObject) {
          currentPair.edit(d.label, d.sizing)
          leftVis.repaint()
          rightVis.repaint()
        }
        
        val obj = new EdgeDialogObject(currentPair.numExternalNodes, currentPair.leftSide.label, NonTerminal)
        new EdgeDialog(null, obj)(editEdge(_)) {
          centerOnScreen
          open
        }
      })
      minimumSize = new Dimension(0, minimumSize.getHeight().toInt)
      maximumSize = new Dimension(Int.MaxValue, minimumSize.getHeight().toInt)
    }
    contents += menubar
    contents += Component.wrap(leftVis)
  }
  
  val rightBox = new BoxPanel(Orientation.Vertical) {
    val menubar = new FlowPanel() {
      background = Color.DARK_GRAY
      contents += new NoFocusButton(Action("Add Vertex") {
        graph.addVertex(new Vertex())
        rightVis.repaint()
      })
      
      contents += new NoFocusButton(Action("Add Edge...") {
        def addEdge(d : EdgeDialogObject) {
          val vs = (1 to d.sizing).map {i => new FakeVertex()}
          vs foreach { graph.addVertex(_) }
          graph.addEdge(new Hyperedge(d.label, d.termination), vs)
          rightVis.repaint()
        }
        new EdgeDialog(null)(addEdge(_)) {
          centerOnScreen
          open
        }
      })
      
      
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
            rightVis.repaint()
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
    contents += Component.wrap(rightVis)
  }
  val split = new SplitPane(Orientation.Vertical, leftBox, rightBox) {
    dividerLocation = 300
    resizeWeight = 0.3
    var oldDividerLocation = 300
    border = Swing.EmptyBorder
  }
  
  contents += split
      
    
  def graph = rightVis.graph
  def graphModel = currentPair.rightSide
  
  def derivationPair = currentPair
  def derivationPair_=(pair : DerivationPair) {
    if (pair.isInitial) {
      if (!currentPair.isInitial)
        split.oldDividerLocation = split.dividerLocation
      split.dividerLocation = 0
      leftBox.visible = false
    } else {
      leftBox.visible = true
      leftVis.setModel(pair.leftSide)
      leftVis.repaint()
      split.dividerLocation = split.oldDividerLocation
    }
    
    currentPair = pair
    rightVis.setModel(pair.rightSide)
    rightVis.repaint()
  }
  
  def pickedVertices = rightVis.getPickedVertexState.getPicked().toSet
  def pickedEdges = rightVis.getPickedEdgeState.getPicked().toSet
  def setPicked(v : Vertex, picked : Boolean) = rightVis.getPickedVertexState.pick(v, picked)
  def setPicked(e : Hyperedge, picked : Boolean) = rightVis.getPickedEdgeState.pick(e, picked)
  
  // When deleting a vertex attached to an edge, the vertex should be replaced with a fake vertex
  protected def replaceWithFakeVertex(g : Hypergraph[Vertex, Hyperedge], vertexToReplace : Vertex) {
    
    val edges = graph.getIncidentEdges(vertexToReplace)
    
    edges.toList.foreach { edge => 
      val fake = new FakeVertex()
      val newIncidents = graph.getIncidentVertices(edge).map(a => 
        if (a == vertexToReplace) fake else a)
      graph.removeEdge(edge)
      graph.addEdge(edge, newIncidents)
      
      // Fake vertex should have same position as old vertex
      rightVis.graphLayout.setLocation(fake, 
          rightVis.graphLayout.transform(vertexToReplace))
    }

  }
  def removeItems(vertices : Set[Vertex], edges : Set[Hyperedge]) {
    
    val vs = (vertices filterNot {_.isInstanceOf[FakeVertex]}) -- currentPair.rightSide.externalNodesSet
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
        
        rightVis.repaint()
      }
    }
  }
  
}

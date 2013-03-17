package com.jakemadethis.graphite.ui

import com.jakemadethis.graphite.visualization.MouseDropPlugin
import edu.uci.ics.jung.graph.Hypergraph
import com.jakemadethis.graphite.graph.GraphExtensions._
import java.awt.event.MouseEvent
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.VisualizationServer
import com.jakemadethis.graphite.graph.Vertex
import com.jakemadethis.graphite.graph.Hyperedge
import com.jakemadethis.graphite.graph.FakeVertex
import com.jakemadethis.graphite.visualization.HoverSupport
import collection.JavaConversions._
import java.awt.event.InputEvent
import com.jakemadethis.graphite.visualization.EdgeLayout

class VertexMergePlugin extends MouseDropPlugin[Vertex, Hyperedge] {
  
  val fakes = collection.mutable.Seq[FakeVertex]()
  var oldEdgeSelection = collection.immutable.List[Hyperedge]()
  
  def dragFilter(graph : Hypergraph[Vertex, Hyperedge], drag : Vertex) =
    drag.isInstanceOf[FakeVertex]
  
  def dropFilter(graph : Hypergraph[Vertex, Hyperedge], drop : Vertex) = 
    !drop.isInstanceOf[FakeVertex]
  
  override def mouseDragged(e : MouseEvent) {
    ifDrop(e) { (vv, drag, drop) =>
      val layout = vv.getGraphLayout()
      layout.setLocation(drag, layout.transform(drop))
      e.consume()
    }
  }
  
  override def mousePressed(e : MouseEvent) {
    val vv = e.getSource().asInstanceOf[VisualizationViewer[Vertex,Hyperedge] with HoverSupport[Vertex,Hyperedge]]
    val graph = vv.getGraphLayout().getGraph()
    val edgeLayout = EdgeLayout(vv.getGraphLayout())
    val hoverVertices = vv.getHoverVertexState().getPicked()
    val hoverEdgeState = vv.getHoverEdgeState()
    val pickedEdges = vv.getPickedEdgeState().getPicked()
    val pickedVertices = vv.getPickedVertexState().getPicked()
    
    val notshift = e.getModifiers() != InputEvent.SHIFT_MASK
    if (notshift && hoverVertices.size > 0 && pickedVertices.size == 1 && !hoverVertices.last.isInstanceOf[FakeVertex]) {
      val drag = hoverVertices.last
      val incidents = graph.getIncidentEdges(drag)
      oldEdgeSelection.filter(incidents.contains(_)).filterNot(edgeLayout.isEdgeLocked(_)).lastOption map { edge =>
        val tentacle = incidents.toList.indexOf(drag)
        val fake = new FakeVertex()
        val newincs = graph.getIncidentVertices(edge) map { v => if (v == drag) fake else v }
        graph.removeEdge(edge)
        graph.addEdge(edge, newincs)
        vv.getGraphLayout().setLocation(fake, vv.getGraphLayout().transform(drag))
        vv.getPickedVertexState().pick(fake, true)
        vv.getHoverVertexState().pick(fake, true)
        vv.getPickedVertexState().pick(drag, false)
        vv.getHoverVertexState().pick(drag, false)
        vv.getPickedEdgeState().pick(edge, true)
      }
    }
    oldEdgeSelection = pickedEdges.toList
  }
  
  override def mouseReleased(e : MouseEvent) {
    super.mouseReleased(e)
    val vv = e.getSource().asInstanceOf[VisualizationViewer[Vertex,Hyperedge] with HoverSupport[Vertex,Hyperedge]]
    val pickedEdges = vv.getPickedEdgeState().getPicked()
    
    oldEdgeSelection = pickedEdges.toList
  }
  
  def vertexDropped(vs : VisualizationServer[Vertex, Hyperedge], drag : Vertex, drop : Vertex) = {
    vs.getGraphLayout().getGraph().replaceVertex(drag, drop)
    vs.repaint()
  }
}
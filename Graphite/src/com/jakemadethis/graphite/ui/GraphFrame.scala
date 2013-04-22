package com.jakemadethis.graphite.ui

import javax.swing.JFrame
import edu.uci.ics.jung.graph.Hypergraph
import com.jakemadethis.graphite.graph._
import edu.uci.ics.jung.graph.Graph
import edu.uci.ics.jung.algorithms.layout.StaticLayout
import com.jakemadethis.graphite.visualization.AverageEdgeLayout
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer
import java.awt.Dimension
import edu.uci.ics.jung.visualization.DefaultVisualizationModel
import javax.swing.WindowConstants
import scala.swing._
import com.jakemadethis.graphite.algorithm.{Derivation,HypergraphProduction}
import com.jakemadethis.graphite.algorithm.HypergraphGenerator
import java.awt.Color
import edu.uci.ics.jung.algorithms.layout.Layout
import collection.JavaConversions._

class GraphFrame(devPaths : Seq[Derivation.Path[HypergraphProduction]]) extends MainFrame {
  
  type Model = DefaultVisualizationModel[Vertex, Hyperedge]
  val genGraphs = devPaths.map(new GeneratableGraph(_))
  
  val graphpanel = new GraphPanel(genGraphs.head.model, editable=false)

  val graphButtons = new BoxPanel(Orientation.Vertical) {
    
    background = Color.WHITE
    
    var toggled : ToggleButton = null
    
    def setToggled(button : ToggleButton) {
      if (toggled != null) toggled.selected = false
      toggled = button
      toggled.selected = true
    }
    def clear() {
      toggled = null
      contents.clear
    }
    def +=(t : (GeneratableGraph, ToggleButton)) {
      val (graph, button) = t
      contents += button
      if (toggled == null) setToggled(button)
    }
  }
  
  val sidebar = new BoxPanel(Orientation.Vertical) {
    preferredSize = new Dimension(150, 10)
    
    val dimension = new Dimension(1000,30)
    
    genGraphs.zipWithIndex.foreach { case (graph, num) =>
      graphButtons += graph -> new ToggleButton() {
        val b = this
        action = Action("Graph "+(num+1)) {
          graphButtons.setToggled(b)
          graphpanel.setModel(graph.model)
          graphpanel.repaint()
        }
        maximumSize = dimension
        focusable = false
        
      }
    }
      
    contents += new ScrollPane(graphButtons) {
      border = Swing.EmptyBorder(0)
    }
    
    
  }
  
  menuBar = new MenuBar() {
    def menuItem(text: String)(op: => Unit) = {
      new MenuItem(Action(text)(op))
    }
    contents += new Menu("Layout") {
      contents += menuItem("Tree") { 
        PerformTreeLayout(graphpanel.getGraphLayout) 
        graphpanel.repaint()
      }
      contents += menuItem("Force Directed") {}
    }
  }
  
  contents = new BorderPanel() {
    layout(sidebar) = BorderPanel.Position.West
    layout(Component.wrap(graphpanel)) = BorderPanel.Position.Center
  }
  
  size = new Dimension(800,600)
  
}

class GeneratableGraph(path : Derivation.Path[HypergraphProduction]) {
  
  lazy val model : DefaultVisualizationModel[Vertex, Hyperedge] = makeModel()
  
  def makeModel() = {
    val graph = HypergraphGenerator(new OrderedHypergraph(), path)
    val pseudoGraph = graph.asInstanceOf[Graph[Vertex, Hyperedge]]
    
    object glayout extends StaticLayout[Vertex, Hyperedge](pseudoGraph, 
        new RandomLocationTransformer(new Dimension(500, 500)), new Dimension(500, 500))
      with AverageEdgeLayout[Vertex, Hyperedge]
    
    println("Generated")
    new DefaultVisualizationModel(glayout)
  }
}

object PerformTreeLayout {
  import org.abego.treelayout._
  import org.abego.treelayout.util._
  import java.awt.geom.Point2D
  
  def apply[V,E](layout : Layout[V,E]) {
    val graph = layout.getGraph().asInstanceOf[Hypergraph[V,E]]
    if (graph.getVertexCount() <= 1) return
    
    try {
      
      val tree = makeTree(graph)
      val config = new DefaultConfiguration[V](100, 100, Configuration.Location.Top)
      val ext = new NodeExtentProvider[V]() {
        def getWidth(v : V) = 10
        def getHeight(v : V) = 10
      }
      val treelayout = new TreeLayout(tree, ext, config)
      
      graph.getVertices().foreach { v => 
        val x = treelayout.getNodeBounds()(v).getCenterX()
        val y = treelayout.getNodeBounds()(v).getCenterY()
        layout.setLocation(v, new Point2D.Double(x, y))
      }
    
    } catch {
      case ex : TreeError => println("Not a tree")
    }
  }
  
  class TreeError extends Exception
  
  private def makeTree[V,E](graph : Hypergraph[V,E]) : TreeForTreeLayout[V] = {
    
    // Edges can be type 0, 1 or 2 but nothing larger
    if (graph.getEdges().exists(edge => graph.getIncidentCount(edge) > 2))
      throw new TreeError
    
    // Get the parent vertex of a given vertex
    def getParent(v : V) = {
      val parents = graph.getIncidentEdges(v).flatMap { edge => 
        val incv = graph.getIncidentVertices(edge)
        if (incv.size() == 2 && incv.tail.head == v) Some(incv.head) else None
      }
      if (parents.size > 1) throw new TreeError 
      parents.headOption
    }

    // Get the children vertices of a given vertex
    def getChildren(v : V) = graph.getIncidentEdges(v).flatMap { edge => 
      val incv = graph.getIncidentVertices(edge)
      if (incv.size() == 2 && incv.head == v) Some(incv.tail.head) else None
    }
    
    // Get the top-most vertex of a given vertex
    def getRoot(v : V) : V = {
      getParent(v).map { getRoot(_) }.getOrElse(v)
    }
    
    val root = getRoot(graph.getVertices().head)
    var addedVertices = 1
    val tree = new DefaultTreeForTreeLayout[V](root)
    
    def addChildrenOf(v : V) {
      val children = getChildren(v).toSeq
      if (children.exists(tree.hasNode(_))) throw new TreeError
      tree.addChildren(v, children:_*)
      addedVertices += children.size
      children.foreach(addChildrenOf(_))
    }
    
    addChildrenOf(root)
    if (addedVertices != graph.getVertexCount()) throw new TreeError
    
    tree
  }
}
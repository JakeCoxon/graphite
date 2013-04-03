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

class GraphFrame(devPaths : Seq[Derivation.Path[HypergraphProduction]]) extends MainFrame {
  
  type Model = DefaultVisualizationModel[Vertex, Hyperedge]
  val genGraphs = devPaths.map(new GeneratableGraph(_))
  
  val graphpanel = new GraphPanel(genGraphs.head.model)

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
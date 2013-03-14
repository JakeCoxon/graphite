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

class GraphFrame(graph : Hypergraph[Vertex, Hyperedge]) extends MainFrame {
  
  val pseudoGraph = graph.asInstanceOf[Graph[Vertex, Hyperedge]]
      
  object glayout extends StaticLayout[Vertex, Hyperedge](pseudoGraph, 
      new RandomLocationTransformer(new Dimension(500, 500)), new Dimension(500, 500))
    with AverageEdgeLayout[Vertex, Hyperedge]
  
  val graphmodel = new DefaultVisualizationModel(glayout)
  
  val graphpanel = new GraphPanel(graphmodel)
  contents = Component.wrap(graphpanel)
  
  size = new Dimension(800,600)
  
}
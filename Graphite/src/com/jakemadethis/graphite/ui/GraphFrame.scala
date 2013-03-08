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
import java.awt.BorderLayout
import javax.swing.WindowConstants

class GraphFrame(graph : Hypergraph[Vertex, Hyperedge]) extends JFrame {
  
  val pseudoGraph = graph.asInstanceOf[Graph[Vertex, Hyperedge]]
      
  object glayout extends StaticLayout[Vertex, Hyperedge](pseudoGraph, 
      new RandomLocationTransformer(new Dimension(500, 500)), new Dimension(500, 500))
    with AverageEdgeLayout[Vertex, Hyperedge]
  
  val graphmodel = new DefaultVisualizationModel(glayout)
  
  setLayout(new BorderLayout());
  val graphpanel = new GraphPanel(graphmodel)
  add(graphpanel)
  
  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
  
  
  setVisible(true)
  setSize(800,600)
  
}
package com.jakemadethis.graphite
import com.jakemadethis.util.MultiSet
import scala.collection.mutable.Map
import scala.collection.mutable.ArraySeq
import com.jakemadethis.graphite.ui.GraphFrame
import com.jakemadethis.graphite.graph._
import scala.collection.JavaConversions._
import javax.swing.UIManager
import com.jakemadethis.graphite.ui.VisualItem
import com.jakemadethis.graphite.ui.VisualEdge
import com.jakemadethis.graphite.ui.VisualVertex



object App {
  
  
  
  def main(args: Array[String]) {
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Graphite");
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
              
              
    val frame = new GraphFrame()
    
    val graph = new OrderedHypergraph[VisualItem, VisualEdge]()
    graph.addVertex(new VisualVertex(new Vertex()))
    frame.setGraph(graph)
    frame.setVisible(true)
    
  }
  
}
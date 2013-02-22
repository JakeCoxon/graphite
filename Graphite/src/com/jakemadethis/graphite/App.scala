package com.jakemadethis.graphite
import com.jakemadethis.util.MultiSet
import scala.collection.mutable.Map
import scala.collection.mutable.ArraySeq
import com.jakemadethis.graphite.ui.GraphFrame
import com.jakemadethis.graphite.graph._
import scala.collection.JavaConversions._



object App {
  
  
  
  def main(args: Array[String]) {
    val frame = new GraphFrame()
    
    val graph = TestGraphGrammar.genGraph
    frame.setGraph(graph)
    frame.setVisible(true)
    
  }
  
}
package com.jakemadethis.graphite

import com.jakemadethis.graphite.graph._
import collection.JavaConversions._
import scala.collection.mutable.Buffer
import com.jakemadethis.graphite.algorithm._
import com.jakemadethis.graphite.ui.GrammarFrame
import java.io.File
import com.jakemadethis.graphite.io.GrammarSaver

object CreateGrammar {
  import GraphTestUtils._
  
  def main(args : Array[String]) {
    val gram = new GrammarBuilder("C", 2)
    
    gram += "C" -> new GraphBuilder(numVertex=3, 0,2).
      edge("C", 0, 1).
      edge("C", 1, 2)
    gram += "C" -> new GraphBuilder(numVertex=2, 0,1).
      edge("D", 0, 1, 1)
    gram += "C" -> new GraphBuilder(numVertex=2, 0,1).
      edge("D", 0, 0, 1)
    gram += "C" -> new GraphBuilder(numVertex=2, 0,1).
      edge("D", 0, 1, 0)
    gram += "C" -> new GraphBuilder(numVertex=2, 0,1).
      edge("c", 0, 1)
      
      
    gram += "D" -> new GraphBuilder(numVertex=4, 0,2,3).
      edge("D", 0, 2, 1).
      edge("C", 1, 3)
    gram += "D" -> new GraphBuilder(numVertex=4, 0,2,3).
      edge("D", 0, 1, 3).
      edge("C", 1, 2)
    gram += "D" -> new GraphBuilder(numVertex=4, 0,2,3).
      edge("D", 0, 1, 3).
      edge("D", 1, 0, 2)
    gram += "D" -> new GraphBuilder(numVertex=4, 0,2,3).
      edge("D", 0, 2, 1).
      edge("D", 1, 3, 0)
    gram += "D" -> new GraphBuilder(numVertex=4, 0,2,3).
      edge("D", 0, 2, 1).
      edge("D", 1, 2, 3)
    gram += "D" -> new GraphBuilder(numVertex=4, 0,2,3).
      edge("D", 0, 1, 3).
      edge("D", 1, 2, 3)
    gram += "D" -> new GraphBuilder(numVertex=4, 0,2,3).
      edge("C", 0, 1).
      edge("D", 1, 2, 3)
    gram += "D" -> new GraphBuilder(numVertex=3, 0,1,2).
      edge("d", 0, 1, 2)
      
      
    val filename = "data/flowgrammar.xml"
    val file = new File(filename)
    val saver = new GrammarSaver(file, gram.build)
    GuiApp.start(Some(filename))
  }
}
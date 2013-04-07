package com.jakemadethis.graphite

import com.jakemadethis.graphite.graph._
import collection.JavaConversions._
import scala.collection.mutable.Buffer
import com.jakemadethis.graphite.algorithm._
import com.jakemadethis.graphite.ui.GrammarFrame
import java.io.File
import com.jakemadethis.graphite.io.GrammarSaver

object CreateGrammar {
  
  class GraphBuilder(numVertex : Int, extIds : Int*) {
    val vs = (1 to numVertex).map {i=> new Vertex()}
    val ext = pickVs(extIds:_*)
    val graph = new OrderedHypergraph[Vertex, Hyperedge]()
    
    def pickVs(ids : Int*) = {
      ids.map(vs(_))
    }
    def edge(label : String, vertexIds : Int*) = {
      val isT = label.size == 0 || label(0).isLower
      val edge = new Hyperedge(label, Termination.terminal(isT))
      graph.addEdge(edge, pickVs(vertexIds:_*))
      this
    }
  }
  class GrammarBuilder(initialLabel : String, initialType : Int) {
    val g = Buffer[(String, HypergraphProduction)]()
    val initial = new GraphBuilder(numVertex=initialType).
      edge(initialLabel, (0 until initialType):_*)
    val initProd = HypergraphProduction(initial.graph, Seq())
    
    def += (t : (String, GraphBuilder)) = {
      val (nt, gb) = t
      val prod = HypergraphProduction(gb.graph, gb.ext)
      g += ((nt, prod))
    }
    def build = Grammar(g.toList, initProd)
  }
  
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
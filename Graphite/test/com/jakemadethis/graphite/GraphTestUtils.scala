package com.jakemadethis.graphite

import scala.util.Random
import com.jakemadethis.graphite.algorithm._
import com.jakemadethis.graphite.graph._
import scala.collection.mutable.Buffer

object GraphTestUtils {
  object GrammarBuilder {
    
    def apply(strProds : (Char, String)*)(implicit random : Random) = {
      val productions = strProds.map { strProd =>
        val (nt, str) = strProd
        val edges = str.map { char =>
          new Hyperedge(char.toString, Termination.terminal(char.isLower))
        }
        val vertices = edges.size match {
          case 0 => IndexedSeq(new Vertex, new Vertex)
          case n => (0 to n).map { i => new Vertex }
        }
        val extNodes = Seq(vertices.head, vertices.last)
        
        val graph = new OrderedHypergraph[Vertex,Hyperedge]()
        for (v <- vertices)
          graph.addVertex(v)
          
        for ((e, i) <- edges.zipWithIndex) 
          graph.addEdge(e, vertices(i), vertices(i+1))
        
        nt.toString() -> HypergraphProduction(graph, extNodes)
      }
      Grammar(productions)
    }
  }
  
  
}
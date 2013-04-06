package com.jakemadethis.graphite

import scala.util.Random
import com.jakemadethis.graphite.algorithm._
import com.jakemadethis.graphite.graph._
import scala.collection.mutable.Buffer

object GraphTestUtils {
  object GrammarBuilder {
    
    def apply(strProds : (Char, String)*)(initial : Char)(implicit random : Random) = {
      def convert(str : String) = {
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
        
        HypergraphProduction(graph, extNodes)
      }
    
      val productions = strProds.map { case(nt, prod) =>
        nt.toString() -> convert(prod)
      }
      Grammar(productions, convert(initial.toString))
    }
  }
  
  
}
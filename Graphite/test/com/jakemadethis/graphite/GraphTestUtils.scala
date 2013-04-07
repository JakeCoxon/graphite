package com.jakemadethis.graphite

import scala.util.Random
import com.jakemadethis.graphite.algorithm._
import com.jakemadethis.graphite.graph._
import scala.collection.mutable.Buffer
import collection.JavaConversions._

object GraphTestUtils {
  object StringToGraphGrammar {
    
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
  
  
}
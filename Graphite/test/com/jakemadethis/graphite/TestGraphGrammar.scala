package com.jakemadethis.graphite

import com.jakemadethis.util._
import com.jakemadethis.graphite.graph._
import scala.collection.mutable.ArraySeq
import com.jakemadethis.graphite.graph.HypergraphDerivation
import com.jakemadethis.graphite.graph.OrderedHypergraph
import edu.uci.ics.jung.graph.Graph
import com.jakemadethis.graphite.graph.HypergraphGrammar
import scala.collection.JavaConversions._
import edu.uci.ics.jung.graph.Hypergraph


object TestGraphGrammar {
  
  def newGraph : Hypergraph[Vertex,Hyperedge] = new OrderedHypergraph[Vertex,Hyperedge]()
  
  var hasSetup = false
  var randomizer : GrammarRandomizer[String, HypergraphDerivation] = null
  var startder : HypergraphDerivation = null
  
  def setup() {
    hasSetup = true
    val g1 = {
      val g = newGraph
      val v1 = new Vertex()
      val v2 = new Vertex()
      val v3 = new Vertex()
      val e1 = TerminalEdge("(")
      val e2 = NonTerminalEdge("B")
      g.addVertex(v1)
      g.addVertex(v2)
      g.addVertex(v3)
      g.addEdge(e1, Seq(v1, v2))
      g.addEdge(e2, Seq(v2, v3))
      new ExtGraph(g, Seq(v1, v3))
    }
    val g2 = {
      val g = newGraph
      val v1 = new Vertex()
      val v2 = new Vertex()
      val v3 = new Vertex()
      val e1 = NonTerminalEdge("A")
      val e2 = NonTerminalEdge("B")
      g.addVertex(v1)
      g.addVertex(v2)
      g.addVertex(v3)
      g.addEdge(e1, Seq(v1, v2))
      g.addEdge(e2, Seq(v2, v3))
      new ExtGraph(g, Seq(v1, v3))
    }
    val g3 = {
      val g = newGraph
      val v1 = new Vertex()
      val v2 = new Vertex()
      val e1 = TerminalEdge(")")
      g.addVertex(v1)
      g.addVertex(v2)
      g.addEdge(e1, Seq(v1, v2))
      new ExtGraph(g, Seq(v1, v2))
    }
    val start = {
      val g = newGraph
      val v1 = new Vertex()
      val v2 = new Vertex()
      val e1 = NonTerminalEdge("A")
      g.addVertex(v1)
      g.addVertex(v2)
      g.addEdge(e1, Seq(v1, v2))
      new ExtGraph(g, Seq())
    }
    
    val grammar = new HypergraphGrammar(Map(
      "A" -> Seq(new HypergraphDerivation(g1)),
      "B" -> Seq(new HypergraphDerivation(g2), new HypergraphDerivation(g3)))
    )
    
    val enumerator = Time {
      new GrammarEnumerator(grammar)
    }
    
    Time {
      for (x <- 0 to 20) {
        println("gA("+x+") = "+enumerator.count("A", x));
        println("gB("+x+") = "+enumerator.count("B", x));
      }
      
    }
  
    
    randomizer = new GrammarRandomizer(enumerator, scala.util.Random)
    startder = new HypergraphDerivation(start)
  }
  
  def main(args : Array[String]) {
    
        
    Time {
      for (x <- 0 to 10) {
        printgraph(genGraph)
      }
    }
    
  }
  
  
  def genGraph() = {
    if (!hasSetup) setup()
    randomizer.generate(startder, 21, new HypergraphGenerator(_, new OrderedHypergraph())).graph
  }
  
  def printgraph(g : Hypergraph[Vertex, Hyperedge]) {
    println("Graph: "+g)
    println(g.getVertices().zipWithIndex.map(_._2).mkString(","))
    println(g.getEdges().map(_.label).mkString(","))
  }
  
}
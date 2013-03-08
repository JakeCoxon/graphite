package com.jakemadethis.graphite

import edu.uci.ics.jung.graph.Hypergraph
import com.jakemadethis.graphite.graph._
import collection.JavaConversions._
import com.jakemadethis.util.Time
import com.jakemadethis.graphite.ui.GraphFrame

object TestTreeGrammar {
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
      val e1 = TerminalEdge("")
      val e2 = TerminalEdge("")
      val e3 = NonTerminalEdge("A")
      val e4 = NonTerminalEdge("A")
      g.addVertex(v1)
      g.addVertex(v2)
      g.addVertex(v3)
      g.addEdge(e1, Seq(v1, v2))
      g.addEdge(e2, Seq(v1, v3))
      g.addEdge(e3, Seq(v2))
      g.addEdge(e4, Seq(v3))
      new HypergraphDerivation(g, Seq(v1), "A")
    }
    val g2 = {
      val g = newGraph
      val v1 = new Vertex()
      val v2 = new Vertex()
      val e1 = TerminalEdge("")
      g.addVertex(v1)
      g.addVertex(v2)
      g.addEdge(e1, Seq(v1, v2))
      new HypergraphDerivation(g, Seq(v1), "A")
    }
    val g3 = {
      val g = newGraph
      val v1 = new Vertex()
      g.addVertex(v1)
      new HypergraphDerivation(g, Seq(v1), "A")
    }
    
    val start = {
      val g = newGraph
      val v1 = new Vertex()
      val e1 = NonTerminalEdge("A")
      g.addVertex(v1)
      g.addEdge(e1, Seq(v1))
      new HypergraphDerivation(g, Seq(), null)
    }
    
    val grammar = HypergraphGrammar(g1, g2, g3)
    
    val enumerator = Time {
      new GrammarEnumerator(grammar)
    }
    
    Time {
      for (x <- 0 to 21) {
        println("gA("+x+") = "+enumerator.count("A", x));
      }
    }
  
    
    randomizer = new GrammarRandomizer(enumerator, scala.util.Random)
    startder = start
  }
  
  def main(args : Array[String]) {
    
        
    Time {
      for (x <- 0 to 1) {
        val g = genGraph
        printgraph(g)
        new GraphFrame(g)
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
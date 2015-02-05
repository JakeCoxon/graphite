package com.jakemadethis.graphite

import scala.util.Random
import org.scalatest.FlatSpec
import com.jakemadethis.graphite.algorithm.converters.ToEpsilonFree
import com.jakemadethis.graphite.algorithm._
import collection.JavaConversions._
import com.jakemadethis.graphite.algorithm.converters.GrammarError

class TestConverters extends FlatSpec {
  import GraphTestUtils._
  
  implicit val random = new Random(100)
  
  def printG(grammar : Grammar[HypergraphProduction]) {
    for ((str, prod) <- grammar.productions) {
      val edges = prod.graph.getEdges.map(_.label)
      println("%s -> %s (%d)".format(str, edges.mkString(","), prod.terminalSize))
    }
  }
  
  it should "duplicate productions deriving epsilons" in {
    val egrammar = StringToGraphGrammar(
      'A' -> "Bbb",
      'B' -> "",
      'B' -> "b"
    )('A')
    val output = ToEpsilonFree(egrammar)
    printG(output)
    
    assert(output("A").size === 2)
    assert(output("B").size === 1)
  }
  
  it should "fail on productions that derive just epsilons" in {
    val egrammar = StringToGraphGrammar(
      'A' -> "AABaa", 
      'A' -> "B",
      'B' -> ""
    )('A')
    intercept[GrammarError] { ToEpsilonFree(egrammar) }
  }
  
}
package com.jakemadethis.graphite

import scala.util.Random
import com.jakemadethis.util.Logger
import com.jakemadethis.graphite.algorithm.converters.PrepareGrammar

object Benchmark {
  import GraphTestUtils._
  
  def main(args : Array[String]) {
    
    implicit val random = new Random(100)
    
    for (i <- 1 until 10) {
      val grammar = StringToGraphGrammar(
        'A' -> ((1 to i).map(x => 'B').mkString),
        'B' -> "b",
        'B' -> "Bb"
      )('A')
      val prepGrammar = PrepareGrammar(grammar)
      
      object logger extends Logger {
        import App._
        
        case class Message(msg : String)
        override val receive : P = _ match {
          case Message(msg) => println(msg)
          case Done(size, number, time) => println("%,d (%,d) -> %,d ms".format(i, size, time/1000))
          case msg @ NoDerivations(_) => println(msg)
        }
      }
      println("Size "+i)
      for (n <- 1 until 50) App.runAlgorithm(prepGrammar, n, 1)(logger)
      
    }
  }
  
  
}
package com.jakemadethis.graphite

import com.jakemadethis.util._
import scala.collection.mutable.Map
import scala.collection.mutable.ArraySeq


object TestStringGrammar {
  
  
  def main(args : Array[String]) {
    
    val grammar = CharGrammar(
      'A' -> Seq("(B"),
      'B' -> Seq("AB", ")")
    )
    
    val enumerator = Time {
      new GrammarEnumerator(grammar)
    }
    
    Time {
      for (x <- 0 to 20)
        println("g1("+x+") = "+enumerator.count('A', x));
      
    }

    
    val randomizer = new GrammarRandomizer(enumerator, scala.util.Random)
    val start = CharDerivation("A")
        
    Time {
      for (x <- 0 to 100) {
        println(randomizer.generate(start, 20, new StringGenerator(_)).makeString)
      }
    }
    
  }
  
}
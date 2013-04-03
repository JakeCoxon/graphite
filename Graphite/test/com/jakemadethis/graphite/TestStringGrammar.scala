//package com.jakemadethis.graphite
//
//import com.jakemadethis.util._
//import scala.collection.mutable.Map
//import scala.collection.mutable.ArraySeq
//import com.jakemadethis.graphite.algorithm.GrammarRandomizer
//import com.jakemadethis.graphite.algorithm.GrammarEnumerator
//import com.jakemadethis.graphite.algorithm.StringGenerator
//import com.jakemadethis.graphite.algorithm.Derivation
//
//
//object TestStringGrammar {
//  
//  
//  def main(args : Array[String]) {
//    
//    val grammar = CharGrammar(
//      'A' -> Seq("(B"),
//      'B' -> Seq("AB", ")")
//    )
//    
//    val enumerator = Time {
//      new GrammarEnumerator(grammar)
//    }
//    
//    Time {
//      for (x <- 0 to 20)
//        println("g1("+x+") = "+enumerator.count('A', x));
//      
//    }
//
//    
//    val randomizer = new GrammarRandomizer(enumerator, scala.util.Random)
//    //val start = Derivation("A")
//        
//    Time {
//      for (x <- 0 to 100) {
//        //println(randomizer.generate(start, 20, new StringGenerator(_)).makeString)
//      }
//    }
//    
//  }
//  
//}
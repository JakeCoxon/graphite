package com.jakemadethis.graphite
import com.jakemadethis.util.MultiSet
import scala.collection.mutable.Map
import scala.collection.mutable.ArraySeq
import collection.immutable.{Map => imMap}



object App {
  
  
  
  def time[A](a: => A) = {
    val now = System.nanoTime
    val result = a
    val micros = (System.nanoTime - now) / 1000
    println("%,d microseconds".format(micros))
    result
  }
  
  def main(args: Array[String]) {
    
    
    /*lazy val ff : BigInt => BigInt = makeConvolution(f, f);
    lazy val d1 = makeDelta(1)
    def f: BigInt => BigInt = makeMemo {(x:BigInt) =>
      ff(x-1) + d1(x)
    }*/
    
    
    val grammar = CharGrammar(
      'A' -> Seq("(B"),
      'B' -> Seq("AB", ")")
    )
    /*val grammar = CharGrammar(
      'S' -> Seq("A", "B", "C"),
      'A' -> Seq("aSa", "aa", "a"),
      'B' -> Seq("bSb", "bb", "b"),
      'C' -> Seq("cSc", "cc", "c")
    )*/
    
    val enumerator = time {
      new GrammarEnumerator(grammar)
    }
    
    
    
    
    time {
      for (x <- 0 to 100)
        println("g1("+x+") = "+enumerator.count('A', x));
      
    }
    
   
    
    

    
    val randomizer = new GrammarRandomizer(enumerator, scala.util.Random)
        val start = CharDerivation("A")
        
    time {
      
      
        
      for (x <- 0 to 100) {
        println(new StringGenerator(randomizer, start, 20).makeString)
      }
    }
    
    
//    time {
//      val m = MultiSet[Char]()
//      for (a1 <- 0 to 1) {
//        val m1 : MultiSet[Char] = m.add('A', a1)
//        for (a2 <- 0 to 4) {
//          val m2 : MultiSet[Char] = m1.add('B', a2)
//          for (t <- 0 to 8) {
//            print(counter.countAll(m2, t))
//            print(" ")
//          }
//          println()
//        }
//      }
//    }
    
  }
  
}
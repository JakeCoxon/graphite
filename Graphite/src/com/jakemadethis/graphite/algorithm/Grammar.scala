package com.jakemadethis.graphite.algorithm

class Grammar[D <: Production](map : Map[String, Seq[D]], val initial : D) extends Iterable[(String, Seq[D])] {
  
  import Grammar._
  
  def apply(key : String) = map(key)
  def iterator = map.iterator
  def get(key : String) = map.get(key)
  val productions = {
    map.foldLeft(List[(String,D)]()) { 
      case (result, (key, seq)) =>
        result ++ seq.map {key -> _}
      }
  }
  def nonTerminals = map.keys.toSet
}



object Grammar {
  class Symbol(val label : String)
  
  def apply[D <: Production](seq : Traversable[(String,D)], initial : D) = {
    val map = seq.foldLeft(Map[String, Seq[D]]()) { (result, a) => 
      result + (a._1 -> (result.getOrElse(a._1, Seq()) :+ a._2))
    }
    new Grammar(map, initial)
  }
}



object Derivation {
  import Grammar._
  import com.jakemadethis.util.MultiSet
  
  type Path[D <: Production] = Seq[(String,D)]
  
  
  class State(val nonTerminals: List[Symbol], val terminalSize: Int) {
      
    def head = nonTerminals.head
    def headLabel = head.label
  
    //def nonTerminalSize = nonTerminals.size
    def isTerminal = nonTerminals.isEmpty

    val nonTerminalLabels = nonTerminals.map(_.label)
    val nonTerminalLabelSet = MultiSet(nonTerminalLabels)
    
  
    def deriveState(newDerivation : State) = {
      new State(newDerivation.nonTerminals ::: nonTerminals.tail, terminalSize + newDerivation.terminalSize)
    }
  }
  
}

abstract class Production(nonTerminals: List[Grammar.Symbol], terminalSize: Int) 
  extends Derivation.State(nonTerminals, terminalSize){
  
  //def map(sym : Symbol) : Any
}


//class CharDerivation(label : Char, val string : String, nts : Seq[Char]) 
//  extends Derivation[Char](label, List(nts:_*), string.size - nts.size)
//  
//object CharDerivation {
//  def apply(label : Char, str : String) = new CharDerivation(label, str, str.filter(_.isUpper))
//}
//
//object CharGrammar {
//  def apply(seq : (Char, Seq[String])*) = {
//    val s = seq.map { tuple =>
//      tuple._1 -> tuple._2.map(CharDerivation(tuple._1, _))
//    }
//    new CharGrammar(Map[Char, Seq[CharDerivation]](s:_*))
//  }
//}




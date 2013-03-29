package com.jakemadethis.graphite.algorithm

import com.jakemadethis.util.MultiSet

object Derivation {
  type Path[D <: Derivation[_]] = Seq[D]
  
  //def empty[T]() = new Derivation[T](null, List(), 0)
//  def apply[T](seq : Seq[T], terminalSize: Int): Derivation[T] = {
//    Derivation(List() ++ seq, terminalSize)
//  }
//  def apply[T](seq : Seq[T]): Derivation[T] = apply(seq, 0)
  
  
  class Item[T](val label : T)
  
  class State[T](val nonTerminals: List[Item[T]], val terminalSize: Int) {
      
    def head = nonTerminals.head
    def headLabel = head.label
  
    //def nonTerminalSize = nonTerminals.size
    def isTerminal = nonTerminals.isEmpty
    val nonTerminalLabelSet = MultiSet(nonTerminalLabels)
    
    val nonTerminalLabels = nonTerminals.map(_.label)
  
    def deriveState(newDerivation : State[T]) = {
      new State[T](newDerivation.nonTerminals ::: nonTerminals.tail, terminalSize + newDerivation.terminalSize)
    }
  }
}

trait DerivationFactory[T, D <: Derivation[T]] {
  def copyWithoutNonTerminals(deriv: D, filter : Set[Derivation.Item[T]]) : D
}

abstract class Derivation[T](val label : T, nonTerminals: List[Derivation.Item[T]], terminalSize: Int)
    extends Derivation.State[T](nonTerminals, terminalSize) {
  import Derivation._
  
}



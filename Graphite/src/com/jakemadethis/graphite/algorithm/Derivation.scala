package com.jakemadethis.graphite.algorithm

import com.jakemadethis.util.MultiSet

object Derivation {
  def empty[T]() = new Derivation[T](List(), 0)
  def apply[T](seq : Seq[T], terminalSize: Int): Derivation[T] = {
    Derivation(List() ++ seq, terminalSize)
  }
  def apply[T](seq : Seq[T]): Derivation[T] = apply(seq, 0)
}

class Derivation[T](val nonTerminals: List[T], val terminalSize: Int) {
  def head = nonTerminals.head
  
  //def nonTerminalSize = nonTerminals.size
  def isTerminal = nonTerminals.isEmpty
  val ntSet = MultiSet(nonTerminals)
  def nonTerminalSet = ntSet
  
  def derive(newDerivation : Derivation[T]) = {
    new Derivation[T](newDerivation.nonTerminals ::: nonTerminals.tail, terminalSize + newDerivation.terminalSize)
  }
}


class DerivationPath[K](val seq : Seq[Derivation[K]])
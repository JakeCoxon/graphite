package com.jakemadethis.graphite.algorithm

import scala.util.Random

class GrammarRandomizer[K, D <: Derivation[K]](enumerator : GrammarEnumerator[K, D], rng : Random) {
  val grammar = enumerator.grammar
  
  def probability(string: Derivation[K], nt: K, derivation : Derivation[K], len: Int) = {
    val size = len - string.terminalSize
    val nts = string.nonTerminalSet
    enumerator.countAll(nts - nt + derivation.nonTerminalSet, 
        size - derivation.terminalSize)
  }
  
  private def pick(seq : Seq[BigInt]) : Int = {
    val sum = seq.sum
    if (sum > 0) {
      var rnd = util.random(sum-1, rng)
      seq.zipWithIndex.foreach { case (v,i) =>
        if (rnd < v) return i; rnd -= v
      }
    }
    return -1 // todo: When does this happen?
  }
    
  def generate[G <: Generator[K, D]]
      (startString : D, len : Int, factory : D => G) : G = {
    
    var string : Derivation[K] = startString
    val generator : G = factory(startString)
    
    while (!string.isTerminal) {
      val vals = grammar(string.head).map(probability(string, string.head, _, len))
      
      val prodId = 
        if (vals.size == 1) 0 
        else pick(vals)
        
      if (prodId == -1) throw new Error("There are no availible derivations")
      
      val derivation = grammar(string.head)(prodId)
      
      generator.derive(string.head, derivation)
      string = string.derive(derivation)
    }
    generator
  }
  
}
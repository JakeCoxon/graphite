package com.jakemadethis.graphite.algorithm

import scala.util.Random

class GrammarRandomizer[K, D <: Derivation[K]](enumerator : GrammarEnumerator[K, D], rng : Random) {
  val grammar = enumerator.grammar
  
  def probability(currentState: Derivation.State[K], nt: K, derivation : Derivation[K], len: Int) = {
    val size = len - currentState.terminalSize
    val nts = currentState.nonTerminalLabelSet
    enumerator.countAll(nts - nt + derivation.nonTerminalLabelSet, 
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
    
    var state : Derivation.State[K] = startString
    val generator : G = factory(startString)
    
    while (!state.isTerminal) {
      val vals = grammar(state.headLabel).map(probability(state, state.headLabel, _, len))
      
      val prodId = 
        if (vals.size == 1) 0 
        else pick(vals)
        
      if (prodId == -1) throw new Error("There are no availible derivations")
      
      val derivation = grammar(state.headLabel)(prodId)
      
      generator.derive(state.headLabel, derivation)
      state = state.deriveState(state)
    }
    generator
  }
  
  def generatePath(startString : D, len : Int) : Derivation.Path[D] = {
    
    var state : Derivation.State[K] = startString
    var list = List[D]() :+ startString
    
    while (!state.isTerminal) {
      val vals = grammar(state.headLabel).map(probability(state, state.headLabel, _, len))
      
      val prodId = 
        if (vals.size == 1) 0 
        else pick(vals)
        
      if (prodId == -1) throw new Error("There are no availible derivations")
      
      val derivation = grammar(state.headLabel)(prodId)
      state = state.deriveState(derivation)
      list = list :+ derivation
    }
    list
  }
  
}
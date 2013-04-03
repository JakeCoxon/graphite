package com.jakemadethis.graphite.algorithm

import scala.util.Random

class GrammarRandomizer[D <: Production](enumerator : GrammarEnumerator[D], rng : Random) {
  val grammar = enumerator.grammar
  
  def probability(string: Derivation.State, nt: Grammar.Symbol, prod : Production, len: Int) = {
    val size = len - string.terminalSize
    val nts = string.nonTerminalLabelSet
    enumerator.countAll(nts - nt.label + prod.nonTerminalLabelSet, 
        size - prod.terminalSize)
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
    
//  def generate[G <: Generator[K, D]]
//      (startString : Derivation[K], len : Int, factory : D => G) : G = {
//    
//    var string : Derivation[K] = startString
//    val generator : G = factory(startString)
//    
//    while (!string.isTerminal) {
//      val vals = grammar(string.head).map(probability(string, string.head, _, len))
//      
//      val prodId = 
//        if (vals.size == 1) 0 
//        else pick(vals)
//        
//      if (prodId == -1) throw new Error("There are no availible derivations")
//      
//      val derivation = grammar(string.head)(prodId)
//      
//      generator.derive(string.head, derivation)
//      string = string.derive(derivation)
//    }
//    generator
//  }
  
  def generatePath(startProduction : D, len : Int) : Derivation.Path[D] = {
    
    var string : Derivation.State = startProduction
    var list = List[(String,D)]() :+ "" -> startProduction
    
    while (!string.isTerminal) {
      val vals = grammar(string.head.label).map(probability(string, string.head, _, len))
      
      val prodId = 
        if (vals.size == 1) 0 
        else pick(vals)
        
      if (prodId == -1) throw new Error("There are no availible derivations")
      
      val label = string.head.label
      val chosenProduction = grammar(label)(prodId)
      string = string.deriveState(chosenProduction)
      list :+= label -> chosenProduction
    }
    list
  }
  
}
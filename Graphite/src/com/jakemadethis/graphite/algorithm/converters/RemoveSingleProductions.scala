package com.jakemadethis.graphite.algorithm.converters

import com.jakemadethis.graphite.algorithm._


object RemoveSingleProductions extends Function[Grammar[HypergraphProduction], Grammar[HypergraphProduction]] {
  
  def apply(grammar : Grammar[HypergraphProduction]) = convert(grammar)
  
  def convert[D <: Production](grammar : Grammar[D]) = {
    
    /** Gets whether this derivation derives to *exactly* the given non-terminals **/
    def derivesOnlyInSet(production : D, nonTerminals : Set[String]) : Boolean = {
      if (production.terminalSize > 0) return false
      production.nonTerminalLabels.forall { nonTerminals.contains(_) }
    }
  
    def isSingleProduction(production : D) = 
      production.terminalSize == 0 && production.nonTerminals.size == 1
      
    def isSingleProductionTo(production : D, nts : Set[String]) = 
      isSingleProduction(production) &&
        nts.contains(production.nonTerminalLabels.head)
    
    
    def getSingleProductionsTo(nts : Set[String]) : Set[String] = {
      val newNts = grammar.productions.foldLeft(nts) { case (result, (prodLabel,prod)) =>
        if (isSingleProductionTo(prod, nts)) 
          result + prodLabel
        else result
      }
      if (newNts == nts) newNts
      else getSingleProductionsTo(newNts)
    }
    
    val ntMap = grammar.nonTerminals.map { nt =>
      nt -> getSingleProductionsTo(Set(nt))
    }.toMap
    
    val prods = grammar.productions.toList.flatMap { case(label, prod) =>
      if (isSingleProduction(prod))
        Nil
      else {
        ntMap(label).map { nt => nt -> prod }
      }
         
    }
    
    Grammar(prods, grammar.initial)
    
  }
  
  
}
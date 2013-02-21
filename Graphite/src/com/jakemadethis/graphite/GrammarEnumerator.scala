package com.jakemadethis.graphite

import com.jakemadethis.util.MultiSet
import scala.collection.mutable.Map
import scala.collection.mutable.ArraySeq
import collection.immutable.{Map => imMap}
import scala.util.Random

private object util {
  
  def makeDelta(n: BigInt) : BigInt => BigInt = {
    x: BigInt => if (x == n) 1 else 0
  }
  val d0 = makeDelta(0)
  val sum_identity : BigInt => BigInt = a => 0
  
  def random(max : BigInt, rng : Random) : BigInt = {
    val r = BigInt(max.bitLength, rng)
    if (r <= max) r else random(max, rng)
  }
  
  type Func = BigInt => BigInt
  
  def convolution(f1: Func, f2: Func)(num : BigInt) : BigInt = {
    (BigInt(1) to num-1).map { i => f1(i) * f2(num-i) }.sum
  }
  def selfConvolution(f : Func, reps : Int) : Func = {
    if (reps == 0) return d0
    (1 until reps).foldLeft(f) { (result, _) => convolution(result, f) }
  }
  
  def makeMemo(f: Func): Func = {
    val map = Map[BigInt, BigInt]()
    x: BigInt => map.getOrElseUpdate(x, f(x))
  }
}

class GrammarEnumerator[K, D <: Derivation[K]](val grammar: Grammar[K, D]) {
  
  val funcs = Map[K, BigInt => BigInt]()
  
  
  
  def count(nt: K, len : Int) = funcs(nt)(len)
  
  def countAll(set: MultiSet[K], len : Int) : BigInt = {
    if (set.isEmpty) return util.d0(len)
    set.map { case (k, num) => util.selfConvolution(funcs(k), num) }
      .reduce { util.convolution(_,_) }(len)
  }
  
  //

  
  private def getf(nt : K)(x : BigInt) = funcs(nt)(x)
  private def sumSubtractN(fOrig: util.Func, f1: util.Func, T: BigInt)(n : BigInt) = {
    fOrig(n) + f1(n - T)
  }
  
  grammar.foreach({ case (nt, prods) =>
    
    val summation = prods.zipWithIndex.foldLeft(util.sum_identity) { 
      case (sum_result, (derivation, prod_id)) => 

        val conv = 
          if (derivation.isTerminal) util.d0 
          else derivation.nonTerminals.map(getf _).reduceLeft(util.convolution(_, _))
        
        sumSubtractN(sum_result, conv, derivation.terminalSize)
    }
    
    funcs(nt) = util.makeMemo { x => if (x <= 0) 0 else summation(x) }
  })
    
}

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

abstract class Generator[K, D <: Derivation[K]] {
  def derive(nonTerminal : K, derivation : D)
}

class StringGenerator(start : CharDerivation) extends Generator[Char, CharDerivation] {
  
  var string = List[Char]()

  derive(0, start)
  
  def derive(c : Char, der : CharDerivation) {
    val pos = string.indices.find { string(_).isUpper }.getOrElse(0)
    string = string.take(pos) ::: der.string.toList ::: string.drop(pos+1)
  }
  
  
  def makeString() : String = {
    string.mkString
  }
}
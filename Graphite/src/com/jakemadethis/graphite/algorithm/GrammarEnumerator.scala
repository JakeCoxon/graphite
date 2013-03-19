package com.jakemadethis.graphite.algorithm
import scala.collection.mutable.Map
import scala.util.Random
import com.jakemadethis.util.MultiSet

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
    (BigInt(0) to num).map { i => f1(i) * f2(num-i) }.sum
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
  
  def precompute(len : Int) {
    (0 to len).foreach { i => funcs.values.map(_.apply(i))}
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
    
    funcs(nt) = util.makeMemo { x => if (x < 0) 0 else summation(x) }
  })
    
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
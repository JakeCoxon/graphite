package com.jakemadethis.graphite.algorithm
import scala.collection.mutable.Map
import scala.util.Random
import com.jakemadethis.util.MultiSet
import scala.collection.mutable.Buffer
import scala.collection.mutable.ArrayBuffer

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
  
  /** convolution(f1, f2) makes a function f(t) that performs a convolution 
   *  from 0 to t.
   *  It is assumed that f1(0) = 0 and f2(0) = 0 */
  def convolution(f1: Func, f2: Func)(num : BigInt) : BigInt = {
    (BigInt(1) to num-1).map { i => f1(i) * f2(num-i) }.sum
  }
  
  /** Makes a convolution of `f' with itself `reps' times.
   *  If reps is 0 then returns d0 the identity function **/
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
  val conv_funcs = Map[K, Map[Int, BigInt => BigInt]]()
  
  
  def count(nt: K, len : Int) = funcs(nt)(len)
  
  def countAll(set: MultiSet[K], len : Int) : BigInt = {
    if (set.isEmpty) return util.d0(len)
    set.map { case (k, num) => conv_funcs(k)(num) }
      .reduce { util.convolution(_,_) }(len)
  }
  
  def count(derivation : D, len : Int) = countAll(derivation.nonTerminalSet, len-derivation.terminalSize)
  
  def countRange(derivation : D, min : Int, max : Int) = 
    (min to max).foldLeft(BigInt(0)) { case (result, i) => result + count(derivation, i) }
  
  def precompute(len : Int) {
    for (i <- 0 to len) funcs.values.foreach(_.apply(i))
    
    for (n <- funcs.keys) {
      conv_funcs(n) = (0 to len).foldLeft(Map[Int, BigInt => BigInt]()) { (result, i) => 
        val f = i match {
          case 0 => util.d0
          case 1 => funcs(n)
          case i => util.makeMemo(x => util.convolution(conv_funcs(n)(i-1), funcs(n))(x))
        }
        result += i -> f; result
      }
      for (i <- 0 to len) {
        for (j <- 0 to len) {
          conv_funcs(n)(i)(j)
        }
      }
    }
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
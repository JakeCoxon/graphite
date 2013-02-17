package com.jakemadethis.graphite;
//import scala.collection.mutable.Map
import scala.collection.mutable.ArraySeq


abstract class Grammar[K, D <: Derivation[K]](map : Map[K, Seq[D]]) {

//  def count(nt: Int, prodId: Int, ntc: Int) : Int = {
//    array(nt)(prodId).count { ntc == _ }
//  }

  def foreach[U](f : ((K, Seq[D])) => U) = map.foreach(f)
  //def terminalSize(seq: Seq[T]) = seq.count(isTerminal _)
  //def terminalSize(key: K, prodId: Int) : Int = terminalSize(map(key)(prodId))
  
  //def string(nt: K, prod : Int) = map(nt)(prod)
  //def numProductions(nt: K) = map(nt).size
  
  //def apply(nt: T) = map(key(nt))
  def apply(nt: K) = map(nt)
  def apply(nt: K, prod: Int) = map(nt)(prod)
  //def get(nt: T) = map.get(key(nt))
  //def key(nt: T) : K
  //def isNonTerminal(t: T) : Boolean = map.contains(key(t))
  //final def isTerminal(t: T) : Boolean = !isNonTerminal(t)
  val size = map.size
}


class CharGrammar(map : Map[Char, Seq[CharDerivation]]) extends Grammar(map)
class StringGrammar[D <: Derivation[String]](map : Map[String, Seq[D]]) extends Grammar(map)

class CharDerivation(val string : String, nts : Seq[Char]) 
  extends Derivation[Char](List(nts:_*), string.size - nts.size)
  
object CharDerivation {
  def apply(str : String) = new CharDerivation(str, str.filter(_.isUpper))
}

object CharGrammar {
  def apply(seq : (Char, Seq[String])*) = {
    val s = seq.map { tuple =>
      tuple._1 -> tuple._2.map(CharDerivation(_))
    }
    new CharGrammar(Map[Char, Seq[CharDerivation]](s:_*))
  }
}




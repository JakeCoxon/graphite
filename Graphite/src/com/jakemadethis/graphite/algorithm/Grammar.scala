package com.jakemadethis.graphite.algorithm

trait Grammar[K, D <: Derivation[K]] extends collection.Map[K, Seq[D]] {

  def map : Map[K, Seq[D]]
  def iterator = map.iterator
  def get(key : K) = map.get(key)
  def + [B1 >: Seq[D]](kv: (K, B1)) = throw new UnsupportedOperationException()
  def -(key: K) = throw new UnsupportedOperationException()
  def derivations = {
    values.foldLeft(Stream.empty[D]) { (result, seq) =>
      result #::: seq.toStream
    }
  }
}


class CharGrammar(map_ : Map[Char, Seq[CharDerivation]]) extends Grammar[Char, CharDerivation] {
  def map = map_
}
trait StringGrammar[D <: Derivation[String]] extends Grammar[String, D] 

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




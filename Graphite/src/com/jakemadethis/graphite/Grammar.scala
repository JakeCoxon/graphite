package com.jakemadethis.graphite;
//import scala.collection.mutable.Map
import scala.collection.mutable.ArraySeq

abstract class Grammar[K, D <: Derivation[K]](map : Map[K, Seq[D]]) extends collection.immutable.Map[K, Seq[D]] {

  def iterator = map.iterator
  def get(key : K) = map.get(key)
  def + [B1 >: Seq[D]](kv: (K, B1)) = throw new UnsupportedOperationException()
  def -(key: K) = throw new UnsupportedOperationException()
  
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




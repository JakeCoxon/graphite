package com.jakemadethis.graphite.algorithm



/** K is the non-terminal type **/
class Grammar[K, D <: Derivation[K]](map : Map[K, Seq[D]]) extends collection.Map[K, Seq[D]] {

  //def map : Map[K, Seq[D]]
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

object Grammar {
  type StringGrammar[D <: Derivation[String]] = Grammar[String, D]
  type CharGrammar[D <: Derivation[Char]] = Grammar[Char, D]
}

trait GrammarFactory[K, D <: Derivation[K]] {
  def apply(derivMap : Map[K, Seq[D]]) : Grammar[K, D]
  def apply(d1 : D, dn : D*) : Grammar[K, D] = apply(d1 +: dn)
  def apply(derivs : Seq[D]) : Grammar[K, D] = {
    val map = derivs.foldLeft(Map[K, Seq[D]]()) { (result, a) => 
      result + (a.label -> (result.getOrElse(a.label, Seq()) :+ a))
    }
    apply(map)
  }
}




class CharDerivation(val left : Char, val itemList : List[Derivation.Item[Char]], nts : List[Derivation.Item[Char]]) 
  extends Derivation[Char](left, nts, itemList.size - nts.size) {
  
  val string = itemList.map(_.label)
  
  def this(left : Char, string : String, nts : List[Char]) =
    this(left, string.map{new Derivation.Item(_)}.toList, nts.map{new Derivation.Item(_)})
  
  def copyWithoutNonTerminals(filter : Set[Derivation.Item[Char]]) = {
  }
}
  
object CharDerivation {
  def apply(label : Char, str : String) = new CharDerivation(label, str, str.toList.filter(_.isUpper))
  
  object factory extends DerivationFactory[Char, CharDerivation] {
    def copyWithoutNonTerminals(deriv: CharDerivation, filter : Set[Derivation.Item[Char]]) = {
      val newString = deriv.itemList.filterNot { filter.contains(_) }
      val newNts = deriv.nonTerminals.filterNot { filter.contains(_) }
      new CharDerivation(deriv.left, newString, newNts)
    }
  }
//  def apply(label : Char, nonTerminals: List[Char], terminalSize: Int) = 
//    new CharDerivation(label, nonTerminals, terminalSize)
  
  
}

object CharGrammar extends GrammarFactory[Char, CharDerivation] {
  def apply(derivMap : Map[Char, Seq[CharDerivation]]) = 
    new Grammar.CharGrammar(derivMap)
  
  def fromSeq(seq : (Char, Seq[String])*) = {
    val s = seq.map { tuple =>
      tuple._1 -> tuple._2.map(CharDerivation(tuple._1, _))
    }
    new Grammar.CharGrammar(Map(s:_*))
  }
}




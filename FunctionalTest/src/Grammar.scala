import scala.collection.mutable.Map
import scala.collection.mutable.ArraySeq


abstract class Grammar[T](map : Map[T, Seq[Seq[T]]]) {
  val idMap = Map[T, Int]()
  val array = new Array[Seq[Seq[Int]]](map.size)
  
  map.zipWithIndex.foreach { case ((key, v), i) =>
    if (isTerminal(key)) throw new Exception("Grammar must be context-free")
    
    idMap.put(key, i)
  }
  
  map.foreach { case (key, v) =>
    val seq = v.map { a => 
      a.map { b => if (isTerminal(b)) -1 else idMap(b) }
    }
    array.update(idMap(key), seq)
  }
  
//  def count(nt: Int, prodId: Int, ntc: Int) : Int = {
//    array(nt)(prodId).count { ntc == _ }
//  }
  def stringSize(nt: T, prodId: Int) = {
    map(nt)(prodId).count { isTerminal(_) }
  }
  
  def foreach[U](f : Seq[Seq[Int]] => U) = array.foreach(f)
  
  def apply(nt: T) = array(idMap(nt))
  def isTerminal(t: T) : Boolean
  def getArray = array
  val size = map.size
  def getMap() = map
}

class StringGrammar(map : Map[Char, Seq[Seq[Char]]]) extends Grammar[Char](map) {
  def isTerminal(t:Char) = t.isLower
}

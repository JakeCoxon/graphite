package com.jakemadethis.util
import scala.collection.immutable.Map

object MultiSet {
  def apply[K]() = new MultiSet[K]()
  def apply[K](seq : TraversableOnce[K]) = {
    new MultiSet(seq.foldLeft(Map[K, Int]()) { case (r, k) => 
      r.updated(k, { 
        if (r.get(k).isEmpty)  1 
        else r(k)+1
      })
    })
  }
  def apply[K](seq : (K, Int)*) = {
    new MultiSet(Map(seq:_*))
  }
  def apply[K](first: K, seq : K*) : MultiSet[K] = apply(first +: seq)
}
class MultiSet[K](map : Map[K, Int]) extends Traversable[(K, Int)] {

  def this() = this(Map[K, Int]())
  
  //def ++:(that : MultiSet[K]) = {
  //  that.foldLeft(this) { case (a, v) => a + v}
  //}
  
  def add(value : K, count : Int) = updated(value, multiplicity(value) + count)
  def +(other : K) = add(other, 1)
  
  def +(other : Traversable[K]) : MultiSet[K] = other.foldLeft(this) { (r, v) => r + v }
  def +(other : MultiSet[K]) : MultiSet[K] = other.foldLeft(this) { (r, t) => r.add(t._1, t._2) }
  
  def -(other : K) = {
    val m = multiplicity(other)
    if (m == 0) this
    else if (m == 1) new MultiSet(map - other)
    else updated(other, multiplicity(other) - 1)
  }
  
  override def foreach[U](f : ((K,Int)) => U) = map.foreach(f)
  
  override def isEmpty = map.isEmpty
  
  def multiplicity(key : K) : Int = map.getOrElse(key, 0)
  def contains(key : K) = map.contains(key)
  
  def updated(k : K, v : Int) = new MultiSet(map.updated(k, v))
  override def toString = map.toString
}
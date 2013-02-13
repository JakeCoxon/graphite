import scala.collection.mutable.Map
import scala.collection.mutable.ArraySeq

object App {
  def makeDelta(n: BigInt) : BigInt => BigInt = {
    x: BigInt => if (x == n) 1 else 0
  }
  val d0 = makeDelta(0)
  val sum_identity : BigInt => BigInt = a => 0
  
  
  def makeConvolution(f1: BigInt => BigInt, f2: BigInt => BigInt) : BigInt => BigInt = {
    n: BigInt => {
//      (BigInt(0) to n).foldLeft(BigInt(0)) { (r,i) => 
//        lazy val a = f1(i)
//        lazy val b = f2(n-i)
//        if (i == 0 && a == 0 || i == n && b == 0) r else r + a*b
//      }
      (BigInt(1) to n-1).foldLeft(BigInt(0)) { (r,i) => 
        r + f1(i) * f2(n-i)
      }
    }
  }
//  def makeRepeatedConvolution(forig : BigInt => BigInt, f1 : BigInt => BigInt, reps : Int) : BigInt => BigInt = {
//    if (reps == 0) return forig
//    val repf1 = if (reps == 1) f1 else 
//      (1 to reps-1).foldLeft(f1) { (r, i) => makeConvolution(r, f1) }
//    if (forig == d0) repf1 else makeConvolution(forig, repf1)
//  }
//  def makeSelfConvolution(f: BigInt => BigInt, num: Int) : BigInt => BigInt = {
//    if (f == null) throw new Error("f cannot be null")
//    if (num <= 0) throw new Error("Num must be above 0")
//    if (num == 1) return f
//    (1 to num-1).foldLeft(f) { (r, i) => makeConvolution(r, f) }
//  }
  def makeSumReduceN(fOrig: BigInt => BigInt, f1: BigInt => BigInt, T: BigInt) : BigInt => BigInt = {
    n: BigInt => fOrig(n) + f1(n - T)
  }
  def makeMemo(f: BigInt => BigInt): BigInt => BigInt = {
    val map = Map[BigInt, BigInt]()
    x: BigInt => map.getOrElseUpdate(x, f(x))
  }
  
  
  
  def make[T](grammar : Grammar[T]) : Map[T, BigInt => BigInt] = {
    val funcs = Map[T, BigInt => BigInt]()
    
    def nonTerminals(seq: Seq[T]) = seq.filterNot(grammar.isTerminal _)
    def getf(nt : T) : BigInt => BigInt = {x => funcs(nt)(x)}
    
    grammar.getMap.foreach({ case (nt, prods) =>
      
      val summation = prods.zipWithIndex.foldLeft(sum_identity) { case (sum_result, (string, prod_id)) => 
        
        val conv = nonTerminals(string).foldLeft(d0) { case (result, nt_x) => 
          if (result == d0) getf(nt_x)
          else makeConvolution(result, getf(nt_x))
        }
        makeSumReduceN(sum_result, conv, grammar.stringSize(nt, prod_id))
      }
      
      funcs(nt) = makeMemo { x => if (x <= 0) 0 else summation(x) }
    })
    
    funcs
  }
  
  def time[A](a: => A) = {
    val now = System.nanoTime
    val result = a
    val micros = (System.nanoTime - now) / 1000
    println("%,d microseconds".format(micros))
    result
  }
  
  def main(args: Array[String]) = {
    
    
    /*lazy val ff : BigInt => BigInt = makeConvolution(f, f);
    lazy val d1 = makeDelta(1)
    def f: BigInt => BigInt = makeMemo {(x:BigInt) =>
      ff(x-1) + d1(x)
    }*/
    
    val grammar = new StringGrammar(Map(
      'A' -> Seq("aB", "C"),
      'B' -> Seq("AC", "b"),
      'C' -> Seq("BBBBB", "b")
    ))
    
    val fs = time {
      make(grammar)
    }
   
    
    time {
      for (x <- 0 to 50)
        println("g1("+x+") = "+fs('A')(x));
      
//      for (x <- 0 to 8)
//        println("g1("+x+") = "+fs(1)(x));
    }
    
    time {
      val l = (0 to 50).foldLeft(BigInt(0)) { (r, x) => 
        r + fs('A')(x)
      }
      println(l)
    }
  }
  
}
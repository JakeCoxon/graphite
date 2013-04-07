package com.jakemadethis.graphite
import com.jakemadethis.util.MultiSet
import scala.collection.mutable.ArraySeq
import com.jakemadethis.graphite.ui.GrammarFrame
import com.jakemadethis.graphite.graph._
import scala.collection.JavaConversions._
import java.io.File
import edu.uci.ics.jung.visualization.VisualizationModel
import scala.collection.immutable.Traversable
import edu.uci.ics.jung.graph.Hypergraph
import scala.swing._
import java.io.FileReader
import javax.swing.UIManager
import java.awt.FileDialog
import java.awt.FileDialog
import java.io.FilenameFilter
import java.awt.FileDialog
import com.jakemadethis.graphite.ui.GuiGrammar
import com.jakemadethis.graphite.algorithm._
import com.jakemadethis.util.Time
import com.jakemadethis.util.Logger
import com.jakemadethis.util.NumUtil._

object App {
  
  
  
  def main(args: Array[String]) {
    val opts = new Options(args.toList)
    val help = opts.getBool('help)
    val file = opts.get('infile)
    val process = opts.get('process).map(Symbol(_))
    val gui = process.map {_ == 'gui}.getOrElse(true)
    if (gui && !help.isDefined) GuiApp.start(file)
    else ConsoleApp.start(file, process.getOrElse('help), opts)
  }
  
  class Options(args : List[String]) {
    val keyValuePattern = """--([a-zA-Z0-9\-]+)=([\S]+)""".r
    val switchPattern = """--([a-zA-Z0-9\-]+)""".r
    
    type OptionMap = Map[Symbol, String]
    
    private def nextOption(map : OptionMap, list : List[String]) : OptionMap = {
      list match {
        case Nil => map
        case keyValuePattern(key, value) :: tail => 
          nextOption(map ++ Map(Symbol(key) -> value), tail)
        case switchPattern(switch) :: tail =>
          nextOption(map ++ Map(Symbol(switch) -> "true"), tail)
        case string :: tail =>
          if (!map.contains('process)) 
            nextOption(map ++ Map('process -> string), tail)
          else
            nextOption(map ++ Map('infile -> string), tail)
      }
    }
    val map = nextOption(Map(), args)
    
    def getBool(key : Symbol) = 
      map.get(key).map(_.toBoolean)
      
    def get(key : Symbol) = map.get(key)
  }
  
  
  
  
  type Path = Derivation.Path[HypergraphProduction]
  
  /** Runs the algorithm and provides logging **/
  def runAlgorithm(grammar : HypergraphGrammar.HG, size : Int, number : Int)(implicit logger : Logger) : Option[Seq[Path]] = {
    val enumerator = new GrammarEnumerator(grammar)
    
    val (paths, time) = Time.get {
      enumerator.precompute(size)
      
      val count = enumerator.count(grammar.initial, size)
      if (count == 0) {
        logger ! NoDerivations(size)
        None
      }
      else {
        logger ! TotalGraphs(count)
        
        val randomizer = new GrammarRandomizer(enumerator, scala.util.Random)
        
        val paths = (1 to number).map { i => 
          
          logger ! Generating(i, number)
          
          val path = randomizer.generatePath(grammar.initial, size)
          path
        }
        
        Some(paths)
      } : Option[IndexedSeq[Path]]
    }
    if (paths.isDefined)
      logger ! Done(size, number, time)
    
    paths
  }
  
  

  // Case classes for output, in order to filter for verbose outputs
  case class TotalGraphs(num : BigInt) {
    override def toString = "Total number of terminal graphs: %s".format(num.toStdForm)
  }
  case class Generating(num : Int, total : Int) {
    override def toString = "Generating %,d of %,d".format(num, total)
  }
  case class GraphData(vs : Int, es : Int)
  case class Done(size : Int, number : Int, time : Long) {
    override def toString = "Done"
  }
  case class NoDerivations(size : Int) {
    override def toString = "No available derivations at size %,d".format(size)
  }
  
}
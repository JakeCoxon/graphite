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


object App {
  
  
  
  def main(args: Array[String]) {
    val opts = new Options(args.toList)
    val help = opts.getBool('help)
    val gui = opts.getBool('gui).getOrElse(!opts.get('infile).isDefined)
    val file = opts.get('infile)
    if (gui && !help.isDefined) GuiApp.start(file)
    else ConsoleApp.start(file, opts)
  }
  
  class Options(args : List[String]) {
    def isOption(str : String) = str.startsWith("--")
    val keyValuePattern = """--([a-zA-Z0-9-]+)=([a-zA-Z0-9]+)""".r
    val switchPattern = """--([a-zA-Z0-9-]+)""".r
    
    type OptionMap = Map[Symbol, String]
    
    private def nextOption(map : OptionMap, list : List[String]) : OptionMap = {
      list match {
        case Nil => map
        case keyValuePattern(key, value) :: tail => 
          nextOption(map ++ Map(Symbol(key) -> value), tail)
        case switchPattern(switch) :: tail =>
          nextOption(map ++ Map(Symbol(switch) -> "true"), tail)
        case string :: tail =>
          nextOption(map ++ Map('infile -> string), tail)
      }
    }
    val map = nextOption(Map(), args)
    
    def getBool(key : Symbol) = 
      map.get(key).map(_.toBoolean)
      
    def get(key : Symbol) = map.get(key)
  }
  
}
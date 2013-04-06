package com.jakemadethis.graphite

import java.io.FileReader
import java.io.File
import com.jakemadethis.graphite.io.GrammarLoader
import com.jakemadethis.graphite.graph.OrderedHypergraph
import com.jakemadethis.graphite.algorithm.{HypergraphGenerator,GrammarRandomizer,GrammarEnumerator}
import com.jakemadethis.util.Time
import collection.JavaConversions._
import com.jakemadethis.graphite.algorithm.converters.PrepareGrammar
import com.jakemadethis.graphite.algorithm.HypergraphGrammar
import com.jakemadethis.graphite.algorithm.HypergraphProduction
import com.jakemadethis.graphite.algorithm.Derivation

object ConsoleApp {
  
  private def toStdForm(num : BigInt) = {
    val str = num.toString
    if (str.length <= 8) {
      "%,d".format(num)
    } else {
      val a = str.charAt(0)
      val b = str.substring(1, 3)
      val exp = str.length - 1
      a + "." + b + " * 10^" + exp
    }
  }
  
  def start(fileOption : Option[String], process : Symbol, opts : App.Options) {
    def valid(sym : Symbol*) = sym.forall(opts.get(_).isDefined)
    
    if (process == 'generate && valid('infile, 'size)) {
      generate(fileOption, opts)
    }
    
    else if (process == 'enumerate && valid('infile, 'size)) {
      enumerate(fileOption, opts)
    }
    
    else if (process == 'benchmark && valid('infile, 'size)) {
      benchmark(fileOption, opts)
    }
    
    else {
      
      println("graphite")
      println("  Opens the graphite gui")
      println("graphite gui filename")
      println("  Opens the graphite gui with a specified file")
      println("graphite generate --size=int [--number=int] [--verbose] [--open] filename")
      println("  Generates a number of graphs with a specified size")
      println("    size       : The size of graph to generate, optionally use a range eg 1..10")
      println("    number     : The number of graphs to generate. Default 1")
      println("    verbose    : Output detailed infomation. Default false")
      println("    open       : Whether to open the graphs after generated. Default false")
      println("graphite enumerate --size=int filename")
      println("  Counts the number of graphs with a specified size")
      println("    size       : The size of graph to count")
      println("graphite benchmark --size=int [--number=int] filename")
      println("  Generates graphs with sizes iterating from 1 to a given size")
      println("    size       : The maximum size graph to generate")
      println("    number     : Each iteration should generate this number of graphs. Default 1")
    }
    
    
  }
  
  def enumerate(fileOption : Option[String], opts : App.Options) {
    val filename = fileOption.get
    val rangePattern = """([0-9]+)\.\.([0-9]+)""".r
    val sizeStr = opts.get('size).get
    
    println("Loading file: "+filename)
    val loader = new GrammarLoader(new FileReader(new File(filename)))
    if (loader.grammar.isEmpty) 
      return println("Grammar is invalid")
    
    val grammar = PrepareGrammar(loader.grammar.get)
    val enumerator = new GrammarEnumerator(grammar)
    
    // Count the number of terminal graphs with a given size
    def timeCountSingle(size : Int) = Time.get {
      enumerator.precompute(size)
      enumerator.count(grammar.initial, size)
    }
    // Count the number of terminal graphs with a given range
    def timeCountRange(min : Int, max : Int) = Time.get {
      enumerator.precompute(max)
      enumerator.countRange(grammar.initial, min, max)
    }
    
    // Run the relevant counting function
    val (count, time) = sizeStr match {
      case rangePattern(min, max) => 
                   timeCountRange(min.toInt, max.toInt)
      case size => timeCountSingle(size.toInt)
    }
    
    val sizeOutput = sizeStr match {
      case rangePattern(min, max) => "from " + min + " to " + max
      case size => size.toString
    }
    
    
    println("Number of terminal graphs with size "+sizeOutput+": %s".format(toStdForm(count)))
    println("Time to compute: %,d milliseconds".format(time/1000))
  }
    
  def generate(fileOption : Option[String], opts : App.Options) {
    
    val filename = fileOption.get
    val size = opts.get('size).get.toInt
    val number = opts.get('number).map{_.toInt}.getOrElse(1)
    val verbose = opts.getBool('verbose).getOrElse(false)
    val gui = opts.getBool('open).getOrElse(false)
    
    println("Loading file: "+filename)
    val loader = new GrammarLoader(new FileReader(new File(filename)))
    if (loader.grammar.isEmpty) 
      return println("Grammar is invalid")
    
    val grammar = PrepareGrammar(loader.grammar.get)
    
    
    
    def verboseOutput(message : Any) {
      message match {
        case OutputTotalGraphs(num) => println("Total number of terminal graphs: %s".format(toStdForm(num)))
        case OutputGenerating(num, total) => println("Generating %,d of %,d: ".format(num, number))
        case OutputGraphData(v, e) => println("Vertices(%,d) + Edges(%,d) = %,d".format(v, e, v+e))
        case OutputDone(size, number, time) => println("Time to compute: %,d milliseconds".format(time/1000))
        case OutputNoAvailableDerivations(size) => println("No available derivations at size %,d".format(size))
        case _ =>
      }
    }
    
    def regularOutput(message : Any) {
      message match {
        case OutputGenerating(num, total) => print("\rGenerating %,d of %,d".format(num, number))
        case OutputDone(size, number, time) => println(); println("Time to compute: %,d milliseconds".format(time/1000))
        case OutputNoAvailableDerivations(size) => println("No available derivations at size %,d".format(size))
        case _ =>
      }
    }
    
    val runfunc = run(grammar, size, number) _
    val paths = 
      if (verbose) runfunc(verboseOutput) 
              else runfunc(regularOutput)
    
    if (gui && paths.isDefined) {
      GuiApp.setup
      GuiApp.openGraphs(paths.get)
    }
    
  }
  
  def benchmark(fileOption : Option[String], opts : App.Options) {
    val filename = fileOption.get
    val size = opts.get('size).get.toInt
    val number = opts.get('number).map{_.toInt}.getOrElse(1)
    
    println("Loading file: "+filename)
    val loader = new GrammarLoader(new FileReader(new File(filename)))
    if (loader.grammar.isEmpty)
      return println("Grammar is invalid")
      
    val grammar = PrepareGrammar(loader.grammar.get)
    
    
    
    def output(message : Any) {
      message match {
        case OutputDone(size, number, time) => println("Size %s: %,d milliseconds".format(size, time/1000))
        case _ =>
      }
    }
    
    (1 to size).foreach { i =>
      run(grammar, i, number)(output)
    }
  }
  
  
  type Path = Derivation.Path[HypergraphProduction]
  
  /** Runs the algorithm **/
  def run(grammar : HypergraphGrammar.HG, size : Int, number : Int)(send : (Any) => Unit) : Option[Seq[Path]] = {
    val enumerator = new GrammarEnumerator(grammar)
    
    val (paths, time) = Time.get {
      enumerator.precompute(size)
      
      val count = enumerator.count(grammar.initial, size)
      if (count == 0) {
        send(OutputNoAvailableDerivations(size))
        return None
      }
      send(OutputTotalGraphs(count))
      
      val randomizer = new GrammarRandomizer(enumerator, scala.util.Random)
      
      val paths = (1 to number).map { i => 
        
        send(OutputGenerating(i, number))
        
        val path = randomizer.generatePath(grammar.initial, size)
        path
      }
      
      paths
    }
    send(OutputDone(size, number, time))
    
    Some(paths)
  }
  

  // Case classes for output, in order to filter for verbose outputs
  case class OutputTotalGraphs(num : BigInt)
  case class OutputGenerating(num : Int, total : Int)
  case class OutputGraphData(vs : Int, es : Int)
  case class OutputDone(size : Int, number : Int, time : Long)
  case class OutputNoAvailableDerivations(size : Int)
}
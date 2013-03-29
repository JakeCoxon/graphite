package com.jakemadethis.graphite

import java.io.FileReader
import java.io.File
import com.jakemadethis.graphite.io.GrammarLoader
import com.jakemadethis.graphite.graph.OrderedHypergraph
import com.jakemadethis.graphite.algorithm.{HypergraphGrammar,HypergraphGenerator,GrammarRandomizer,GrammarEnumerator}
import com.jakemadethis.util.Time
import collection.JavaConversions._
import com.jakemadethis.graphite.algorithm.Validator

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
      println("    size       : Counts the number of terminal graphs with this size")
    }
    
    
  }
  
  def enumerate(fileOption : Option[String], opts : App.Options) {
    val filename = fileOption.get
    val rangePattern = """([0-9]+)\.\.([0-9]+)""".r
    val sizeStr = opts.get('size).get
    
    println("Loading file: "+filename)
    val loader = new GrammarLoader(new FileReader(new File(filename)))
    val initial = loader.initial.getOrElse {throw new Error("No initial graph")}
    
    val grammar = Validator.validateGrammar(loader.grammar)(HypergraphGrammar$, HypergraphDerivation)
    val enumerator = new GrammarEnumerator(grammar)
    
    // Count the number of terminal graphs with a given size
    def timeCountSingle(size : Int) = Time.get {
      enumerator.precompute(size)
      enumerator.count(initial, size)
    }
    // Count the number of terminal graphs with a given range
    def timeCountRange(min : Int, max : Int) = Time.get {
      enumerator.precompute(max)
      enumerator.countRange(initial, min, max)
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
    val initial = loader.initial.getOrElse {throw new Error("No initial graph")}
    
    
    
    // Case classes for output, in order to filter for verbose outputs
    case class OutputTotalGraphs(num : BigInt)
    case class OutputGenerating(num : Int, total : Int)
    case class OutputGraphData(vs : Int, es : Int)
    case class OutputDone(time : Long)
    
    // Runs the algorithm
    def run(send : (Any) => Unit) {
      val grammar = Validator.validateGraph(loader.grammar)
      val enumerator = new GrammarEnumerator(grammar)
      
      val (paths, time) = Time.get {
        enumerator.precompute(size)
        
        val count = enumerator.count(initial, size)
        if (count == 0) {
          println("No available derivations at size %,d".format(size))
          return
        }
        send(OutputTotalGraphs(count))
        
        val randomizer = new GrammarRandomizer(enumerator, scala.util.Random)
        
        val paths = (1 to number).map { i => 
          
          send(OutputGenerating(i, number))
          
          val path = randomizer.generatePath(initial, size)
          //val graph = HypergraphGenerator(new OrderedHypergraph(), path)
          
          //send(OutputGraphData(graph.getVertexCount(), graph.getEdgeCount()))
          path
        }
        
        paths
      }
      send(OutputDone(time))
      
      if (gui) {
        GuiApp.setup
        GuiApp.openGraphs(paths)
      }
    }
    
    def verboseOutput(message : Any) {
      message match {
        case OutputTotalGraphs(num) => println("Total number of terminal graphs: %s".format(toStdForm(num)))
        case OutputGenerating(num, total) => println("Generating %,d of %,d: ".format(num, number))
        case OutputGraphData(v, e) => println("Vertices(%,d) + Edges(%,d) = %,d".format(v, e, v+e))
        case OutputDone(time) => println("Time to compute: %,d milliseconds".format(time/1000))
        case _ =>
      }
    }
    
    def regularOutput(message : Any) {
      message match {
        case OutputGenerating(num, total) => print("\rGenerating %,d of %,d".format(num, number))
        case OutputDone(time) => println(); println("Time to compute: %,d milliseconds".format(time/1000))
        case _ =>
      }
    }
    
    if (verbose) run(verboseOutput) 
    else run(regularOutput)
    
  }
}
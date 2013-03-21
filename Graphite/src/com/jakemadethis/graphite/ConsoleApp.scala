package com.jakemadethis.graphite

import java.io.FileReader
import java.io.File
import com.jakemadethis.graphite.io.GrammarLoader
import com.jakemadethis.graphite.graph.OrderedHypergraph
import com.jakemadethis.graphite.algorithm.{HypergraphGenerator,GrammarRandomizer,GrammarEnumerator}
import com.jakemadethis.util.Time
import collection.JavaConversions._

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
  
  def start(fileOption : Option[String], opts : App.Options) {
    def valid(sym : Symbol*) = sym.forall(opts.get(_).isDefined)
    
    if (valid('infile, 'size)) {
      generate(fileOption, opts)
    }
    
    else if (valid('infile, 'count)) {
      count(fileOption, opts)
    }
    
    else {
      
      println("graphite [--gui=bool] filename")
      println("graphite --size=int [--number=int] [--verbose] filename")
      println("graphite --count=int filename")
      println("  gui        : Whether to display the gui or not")
      println("  size       : The size of graph to generate")
      println("  number     : The number of graphs to generate. Default 1")
      println("  count      : Counts the number of terminal graphs with this size")
      println("  verbose    : Output detailed infomation. Default false")
    }
    
    
  }
  
  def count(fileOption : Option[String], opts : App.Options) {
    val filename = fileOption.get
    val rangePattern = """([0-9]+)\.\.([0-9]+)""".r
    val sizeStr = opts.get('count).get
    
    println("Loading file: "+filename)
    val loader = new GrammarLoader(new FileReader(new File(filename)))
    val initial = loader.initial.getOrElse {throw new Error("No initial graph")}
    
    
    val enumerator = new GrammarEnumerator(loader.grammar)
    
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
    
    println("Loading file: "+filename)
    val loader = new GrammarLoader(new FileReader(new File(filename)))
    val initial = loader.initial.getOrElse {throw new Error("No initial graph")}
    
    val enumerator = new GrammarEnumerator(loader.grammar)
    
    // Case classes for output, in order to filter for verbose outputs
    case class OutputTotalGraphs(num : BigInt)
    case class OutputGenerating(num : Int, total : Int)
    case class OutputGraphData(vs : Int, es : Int)
    case class OutputDone()
    
    // Runs the algorithm
    def run(send : (Any) => Unit) {
      enumerator.precompute(size)
      
      val count = enumerator.count(initial, size)
      if (count == 0) {
        println("No available derivations at size %,d".format(size))
        return
      }
      send(OutputTotalGraphs(count))
      
      val randomizer = new GrammarRandomizer(enumerator, scala.util.Random)
      
      for ( i <- 1 to number ) {
        
        send(OutputGenerating(i, number))
        
        val path = randomizer.generatePath(initial, size)
        //val graph = HypergraphGenerator(new OrderedHypergraph(), path)
        
        //send(OutputGraphData(graph.getVertexCount(), graph.getEdgeCount()))
        
      }
      send(OutputDone())
    }
    
    def verboseOutput(message : Any) {
      message match {
        case OutputTotalGraphs(num) => println("Total number of terminal graphs: %s".format(toStdForm(num)))
        case OutputGenerating(num, total) => println("Generating %,d of %,d: ".format(num, number))
        case OutputGraphData(v, e) => println("Vertices(%,d) + Edges(%,d) = %,d".format(v, e, v+e))
        case _ =>
      }
    }
    
    def regularOutput(message : Any) {
      message match {
        case OutputGenerating(num, total) => print("\rGenerating %,d of %,d".format(num, number))
        case OutputDone() => println()
        case _ =>
      }
    }
    
    val (_, time) = Time.get(run(if (verbose) verboseOutput else regularOutput))
    println("Time to compute: %,d milliseconds".format(time/1000))
  }
}
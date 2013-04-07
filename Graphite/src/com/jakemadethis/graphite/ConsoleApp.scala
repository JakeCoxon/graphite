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
import com.jakemadethis.util.Logger
import com.jakemadethis.util.NumUtil._

object ConsoleApp {
  
  def start(fileOption : Option[String], process : Symbol, opts : App.Options) {
    def valid(sym : Symbol*) = sym.forall(opts.get(_).isDefined)
    
    val verbose = opts.getBool('verbose).getOrElse(false)
    implicit val logger =  if (verbose) VerboseLogger else BasicLogger
    
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
      println("graphite generate --size=int [--number=int] [--verbose] [--distinct] [--open] filename")
      println("  Generates a number of graphs with a specified size")
      println("    size       : The size of graph to generate, optionally use a range eg 1..10")
      println("    number     : The number of graphs to generate. Default 1")
      println("    verbose    : Output detailed infomation. Default false")
      println("    distinct   : Print the distinct graphs when it's finished.")
      println("    open       : Whether to open the graphs after generated.")
      println("graphite enumerate --size=int filename")
      println("  Counts the number of graphs with a specified size")
      println("    size       : The size of graph to count")
      println("graphite benchmark --size=int [--number=int] filename")
      println("  Generates graphs with sizes iterating from 1 to a given size")
      println("    size       : The maximum size graph to generate")
      println("    number     : Each iteration should generate this number of graphs. Default 1")
    }
    
    
  }
  
  def enumerate(fileOption : Option[String], opts : App.Options)(implicit logger : Logger) {
    val filename = fileOption.get
    val rangePattern = """([0-9]+)\.\.([0-9]+)""".r
    val sizeStr = opts.get('size).get
    
    logger ! "Loading file: %s".format(filename)
    val loader = new GrammarLoader(new FileReader(new File(filename)))
    if (loader.grammar.isEmpty) 
      return logger @! "Grammar is invalid"
    
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
    
    
    logger ! "Number of terminal graphs with size %s: %s".format(sizeOutput, count.toStdForm)
    logger ! "Time to compute: %,d milliseconds".format(time/1000)
  }
    
  def generate(fileOption : Option[String], opts : App.Options)(implicit logger : Logger) {
    
    val filename = fileOption.get
    val size = opts.get('size).get.toInt
    val number = opts.get('number).map{_.toInt}.getOrElse(1)
    val printDistinct = opts.getBool('distinct).getOrElse(false)
    val gui = opts.getBool('open).getOrElse(false)
    
    logger ! "Loading file: %s".format(filename)
    val loader = new GrammarLoader(new FileReader(new File(filename)))
    if (loader.grammar.isEmpty) 
      return logger @! "Grammar is invalid"
    
    val grammar = PrepareGrammar(loader.grammar.get)
    
    val paths = App.runAlgorithm(grammar, size, number)
    
    if (printDistinct && paths.isDefined) {
      logger ! "Distinct graphs:"
      val map = grammar.productions.zipWithIndex.map { case (p, i) => p._2 -> i }.toMap
      val distinctMap = collection.mutable.Map[Seq[HypergraphProduction], Int]()
      
      paths.get.foreach { p =>
        val v = p.tail.map(_._2)
        val num = distinctMap.getOrElse(v, 0) + 1
        distinctMap(v) = num
      }
      distinctMap.toList.zipWithIndex.foreach {case ((k, v), id) =>
        // k.map(map(_)).mkString(",")
        logger ! "%,d -> %,d".format(id, v)
      }
    }
    
    if (gui && paths.isDefined) {
      GuiApp.setup
      GuiApp.openGraphs(paths.get)
    }
    
  }
  
  def benchmark(fileOption : Option[String], opts : App.Options)(implicit logger : Logger) {
    val filename = fileOption.get
    val size = opts.get('size).get.toInt
    val number = opts.get('number).map{_.toInt}.getOrElse(1)
    
    logger ! "Loading file: %s".format(filename)
    val loader = new GrammarLoader(new FileReader(new File(filename)))
    if (loader.grammar.isEmpty)
      return logger @! "Grammar is invalid"
      
    val grammar = PrepareGrammar(loader.grammar.get)
    
    (1 to size).foreach { i =>
      App.runAlgorithm(grammar, i, number)(BenchmarkLogger)
    }
  }
  
  
  case class VerboseMessage(msg : String)
  
  object VerboseLogger extends Logger {
    import App._
  
    override def receive = verboseReceive orElse super.receive
    
    def verboseReceive : PartialFunction[Any,Unit] = {
      case Done(size, number, time) => println("Time to compute: %,d milliseconds".format(time/1000))
      case msg @ GraphData(_,_)   => println(msg)
      case msg @ TotalGraphs(_)   => println(msg)
      case msg @ Generating(_, _) => println(msg)
      case msg @ NoDerivations(_) => println(msg)
      case msg : String => println(msg)
    }
  }
  
  object BasicLogger extends Logger {
    import App._
    
    override def receive = basicReceive orElse super.receive
    
    def basicReceive : PartialFunction[Any,Unit] = {
      case Done(size, number, time) => println(); println("Time to compute: %,d milliseconds".format(time/1000))
      case msg @ Generating(_, _) => print("\r"+msg)
      case msg @ NoDerivations(_) => println(msg)
      case msg : String => println(msg)
    }
  }
  
  
  object BenchmarkLogger extends Logger {
    import App._
    
    override def receive = benchReceive orElse super.receive
    
    def benchReceive : PartialFunction[Any,Unit] = {
      case Done(size, number, time) => println("Size %s: %,d milliseconds".format(size, time/1000))
    }
  }
  
  
}
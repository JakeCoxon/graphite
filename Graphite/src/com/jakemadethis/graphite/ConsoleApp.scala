package com.jakemadethis.graphite

import com.jakemadethis.graphite.io.GrammarLoader
import java.io.FileReader
import java.io.File
import com.jakemadethis.graphite.algorithm.HypergraphGenerator
import com.jakemadethis.graphite.graph.OrderedHypergraph
import com.jakemadethis.graphite.algorithm.GrammarRandomizer
import com.jakemadethis.graphite.algorithm.GrammarEnumerator
import com.jakemadethis.util.Time

object ConsoleApp {
  
  def start(fileOption : Option[String], opts : App.Options) {
    def valid(sym : Symbol*) = sym.forall(opts.get(_).isDefined)
    
    if (valid('infile, 'size, 'number)) {
      generate(fileOption, opts)
    }
    
    else if (valid('infile, 'count)) {
      count(fileOption, opts)
    }
    
    else {
      
      println("graphite [--gui=bool] filename")
      println("graphite --size=int --number=int filename")
      println("graphite --count=int filename")
      println("  gui        : Whether to display the gui or not")
      println("  size       : The size of graph to generate")
      println("  number     : The number of graphs to generate")
      println("  count : Counts the number of terminal graphs with this size")
    }
    
    
  }
  
  def count(fileOption : Option[String], opts : App.Options) {
    val filename = fileOption.get
    val size = opts.get('count).get.toInt
    
    println("Loading file: "+filename)
    val loader = new GrammarLoader(new FileReader(new File(filename)))
    val initial = loader.initial.getOrElse {throw new Error("No initial graph")}
    
    val enumerator = new GrammarEnumerator(loader.grammar)
    val (_, time) = Time.get(enumerator.precompute(size))
    
    val count = enumerator.count(initial, size)
    
    
    val countstr = count.toString
    val output = if (countstr.length > 5) {
      val a = countstr.charAt(0)
      val b = countstr.substring(1, 3)
      val exp = countstr.length - 1
      a+"."+b+" * 10^"+exp
    } else countstr

    println("Number of terminal graphs at size "+size+": "+output)
    println("Time to compute: %,d milliseconds".format(time/1000))
  }
    
  def generate(fileOption : Option[String], opts : App.Options) {
    
    val filename = fileOption.get
    val size = opts.get('size).get.toInt
    val number = opts.get('number).get.toInt
    
    println("Loading file: "+filename)
    val loader = new GrammarLoader(new FileReader(new File(filename)))
    val initial = loader.initial.getOrElse {throw new Error("No initial graph")}
    
    val enumerator = new GrammarEnumerator(loader.grammar)
    enumerator.precompute(size)
    
    if (enumerator.count(initial, size) == 0) {
      println("No available derivations at size "+size)
      return
    }
    
    val randomizer = new GrammarRandomizer(enumerator, scala.util.Random)
    
    for ( i <- 1 to number ) {
      
      val path = randomizer.generatePath(initial, size)
      val graph = HypergraphGenerator(new OrderedHypergraph(), path)
      print("\r")
      print("Generating "+i+" of "+number)
    }
    println()
  }
}
package com.jakemadethis.graphite

object ConsoleApp {
  
  def start(fileOption : Option[String], opts : App.Options) {
    
    val required = fileOption :: opts.get('size) :: opts.get('number) :: Nil
    
    if (required.exists(_.isEmpty)) {
      println("graphite [--gui=bool] [--size=int] [--number=int] filename")
      println("  gui    : Whether to display the gui or not")
      println("  size   : The size of graph to generate")
      println("  number : The number of graphs to generate")
      return
    }
    
    val filename = fileOption.get
    val size = opts.get('size).get.toInt
    val number = opts.get('number).get.toInt
    
    println("Loading file: "+filename)
    
  }
}
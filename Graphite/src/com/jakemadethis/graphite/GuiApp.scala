package com.jakemadethis.graphite

import java.io.{FilenameFilter, File, FileReader}
import com.jakemadethis.graphite.ui._
import com.jakemadethis.graphite.graph._
import java.awt.FileDialog
import scala.swing._
import javax.swing.UIManager
import com.jakemadethis.graphite.io.GrammarSaver
import com.jakemadethis.graphite.io.GrammarLoader
import scala.collection.mutable.Subscriber
import scala.swing.event._
import com.jakemadethis.graphite.algorithm._
import edu.uci.ics.jung.graph.Hypergraph

/**
 * Object for controlling various loading/saving dialogs
 */
object GuiApp extends Reactor {

  object XMLFilter extends FilenameFilter() {
    def accept(dir : File, name : String) = name.endsWith(".xml")
  }
  
  /**
   * Starts the Gui App, sets swing options such as look&feel, loads default grammar
   */
  def start() {
    
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Graphite");
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
              
    loadGrammar(new File("data/grammar.xml"))
  }
  
  reactions += {
    case NewGrammar() =>
      newGrammar()
    case LoadGrammar(parent) =>
      loadGrammarDialog(parent)
    case SaveGrammar(parent, grammar, saveAs) =>
      saveGrammarDialog(parent, grammar)
    case LoadGraph(parent) =>
      loadGraphDialog(parent)
    case GenerateGraphs(grammar, size, number) =>
      generateGraphs(grammar, size, number)
  }
  
  
  /**
   * Opens a frame with an empty grammar
   */
  def newGrammar() {
    new GrammarFrame(GuiGrammar(), None) {
      open
      GuiApp.listenTo(this)
    }
    
  }
  
  
  /**
   * Opens a frame with the given grammar file loaded
   */
  def loadGrammar(file : File) {
    val grammarLoader = new GrammarLoader(new FileReader(file))
    
    new GrammarFrame(grammarLoader.grammar, Some(file)) {
      open
      GuiApp.listenTo(this)
    }
    
    println("Loaded " + file.getAbsolutePath())
  }
  
  
  /**
   * Saves a grammar and outputs a message
   */
  def saveGrammar(grammar : GuiGrammar, file : File) {
    val saver = new GrammarSaver(file, grammar)
    println("Saved " + file.getAbsolutePath())
  }
  
  
  /**
   * Opens the load-grammar dialog
   */
  def loadGrammarDialog(parent : Frame) {
    
    val dialog = new FileDialog(parent.peer, "Open Graph Grammar") {
      setDirectory(new File(".").getAbsolutePath())
      setFilenameFilter(XMLFilter)
      setMode(FileDialog.LOAD)
      setVisible(true)
    }
    if (dialog.getFile() == null) return
    
    val file = new File(dialog.getDirectory() + "/" + dialog.getFile())
    loadGrammar(file)
  }
  
  /**
   * Opens a single graph in a new window
   */
  def openGraph(graph : Hypergraph[Vertex,Hyperedge]) {
    new GraphFrame(graph) {
      open
    }
  }
  
  /**
   * Opens the load-graph dialog
   */
  def loadGraphDialog(parent : Frame) {}
  
  
  /**
   * Opens the save-grammar dialog
   */
  def saveGrammarDialog(parent : Frame, grammar : GuiGrammar) {
    
    val d = new FileDialog(parent.peer, "Save Graph Grammar") {
      setDirectory(new File(".").getAbsolutePath())
      setFilenameFilter(XMLFilter)
      setMode(FileDialog.SAVE)
      setVisible(true)
    }
    if (d.getFile() == null) return
    
    val file = new File(d.getDirectory() + "/" + d.getFile())
    saveGrammar(grammar, file)
  }
  
  /**
   * Performs the algorithm on a GuiGrammar (by converting to algorithm grammar first)
   */
  def generateGraphs(guiGrammar : GuiGrammar, size : Int, number : Int) {
    
    def derivationFromPair(pair : DerivationPair) = {
      val graph = pair.rightSide.graph
      val extNodes = pair.rightSide.extNodes.toList.sortBy(_._2).map(_._1)
      val label = pair.label
      new HypergraphDerivation(graph, extNodes, label)
    }
    
    val derivs = guiGrammar.derivations.map(derivationFromPair _)
    val startder = new HypergraphDerivation(guiGrammar.initialGraph.rightSide.graph, Seq(), "")
    val grammar = HypergraphGrammar(derivs)
    println(grammar)
    
    val enumerator = new GrammarEnumerator(grammar)
    enumerator.precompute(size)
    
    val randomizer = new GrammarRandomizer(enumerator, scala.util.Random)
    
    (0 until number).foreach { i =>
      val g = randomizer.generate(startder, size, { new HypergraphGenerator(_, new OrderedHypergraph()) }).graph
      openGraph(g)
    }
  
    
  }
}
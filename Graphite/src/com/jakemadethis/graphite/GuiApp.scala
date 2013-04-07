package com.jakemadethis.graphite

import java.io.{FilenameFilter, File, FileReader}
import com.jakemadethis.graphite.ui._
import com.jakemadethis.graphite.graph._
import java.awt.FileDialog
import scala.swing._
import javax.swing.UIManager
import com.jakemadethis.graphite.io.{GuiGrammarSaver,GuiGrammarLoader}
import scala.collection.mutable.Subscriber
import scala.swing.event._
import com.jakemadethis.graphite.algorithm._
import edu.uci.ics.jung.graph.Hypergraph
import collection.JavaConversions._
import javax.swing.plaf.metal.MetalLookAndFeel
import javax.swing.plaf.metal.OceanTheme
import com.jakemadethis.util.Logger

/**
 * Object for controlling various loading/saving dialogs
 */
object GuiApp extends Reactor {

  object XMLFilter extends FilenameFilter() {
    def accept(dir : File, name : String) = name.endsWith(".xml")
  }
  
  def setup {
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Graphite")
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
  }
  
  /**
   * Starts the Gui App, sets swing options such as look&feel, loads default grammar
   */
  def start(file : Option[String]) {
    setup
    
    if (file.isDefined) 
      loadGrammar(new File(file.get))
    else
      newGrammar()
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
    case GenerateGraphs(parent, grammar, size, number) =>
      generateGraphs(parent, grammar, size, number)
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
    val grammarLoader = new GuiGrammarLoader(new FileReader(file))
    
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
    val saver = new GuiGrammarSaver(file, grammar)
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
//    new GraphFrame(graph) {
//      open
//    }
  }
  
  /**
   * Opens a collection of graphs in the form of derivation paths 
   */
  def openGraphs(graphs : Seq[Derivation.Path[HypergraphProduction]]) {
    new GraphFrame(graphs) {
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
  def generateGraphs(parent : Frame, guiGrammar : GuiGrammar, size : Int, number : Int) {
    
    if (guiGrammar.derivations.exists(_.isInvalid)) {
      println("Invalid graph")
      return
    }
    
    def derivationFromPair(pair : DerivationPair) = {
      val graph = pair.rightSide.graph
      val extNodes = pair.rightSide.extNodes.toList.sortBy(_._2).map(_._1)
      val label = pair.label
      label -> HypergraphProduction(graph, extNodes)
    }
    
    val derivs = guiGrammar.derivations.map(derivationFromPair _)
    val initial = HypergraphProduction(guiGrammar.initialGraph.rightSide.graph, Seq())
    val grammar = Grammar(derivs, initial)
    
    val loadingDialog = new Dialog(parent) {
      lazy val message = new Label("Generating")
      lazy val closebtn = new Button(Action("Close") {
        close
      }) { enabled = false }
      
      contents = new BoxPanel(Orientation.Vertical) {
        contents += new FlowPanel(message)
        contents += new FlowPanel(closebtn)
      }
      
      def ! (s : String) {
        Swing.onEDT { message.text = s; pack }
      }
      def done { close }
      def error(message : String) = Swing.onEDT {
        Dialog.showMessage(null, message, "Generating Error", Dialog.Message.Error, null)
        close
      }
      
      
      open; centerOnScreen
    }
    loadingDialog ! "Precompting"
    
    object logger extends Logger {
      import App._
      
      override def receive : PartialFunction[Any,Unit] = {
        case Logger.Error(msg) => loadingDialog.error(msg)
        case msg @ NoDerivations(_) => loadingDialog.error(msg.toString)
        case msg @ Generating(_, _) => loadingDialog ! msg.toString
        case Done(_, _, _) => loadingDialog.done
      }
    }
    
    val thread = new Thread(new Runnable {
      def run() {
        val paths = App.runAlgorithm(grammar, size, number)(logger)
        paths.foreach { openGraphs(_) }
      }
    })
    
    thread.start()
    
  }
}
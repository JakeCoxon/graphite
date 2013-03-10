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


object App {
  
  
  
  def main(args: Array[String]) {
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Graphite");
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
              
    openGrammar(new File("data/grammar.xml"))
    
  }
  
  object XMLFilter extends FilenameFilter() {
    def accept(dir : File, name : String) = name.endsWith(".xml")
  }
  
  def openGrammar(file : File) {
    val grammarLoader = new GrammarLoader(new FileReader(file))
    println(grammarLoader.grammar)
    
    new GrammarFrame(grammarLoader.grammar, Some(file)) {
      open
    }
    
    println("Loaded " + file.getAbsolutePath())
  }
  def saveGrammar(grammar : HypergraphGrammar, models: Map[Hypergraph[Vertex,Hyperedge], VisualizationModel[Vertex,Hyperedge]], file : File) {
    val saver = new GrammarSaver(file, grammar, models)
    println("Saved " + file.getAbsolutePath())
  }
  
  def loadGrammarGui(parent : Frame) {
    
    val dialog = new FileDialog(parent.peer, "Open Graph Grammar") {
      setDirectory(new File(".").getAbsolutePath())
      setFilenameFilter(XMLFilter)
      setMode(FileDialog.LOAD)
      setVisible(true)
    }
    if (dialog.getFile() == null) return
    
    val file = new File(dialog.getDirectory() + "/" + dialog.getFile())
    openGrammar(file)
  }
  def loadGraphGui(parent : Frame) {
    
  }
  
  def saveGrammarGui(parent : Frame, grammar : HypergraphGrammar, models: Map[Hypergraph[Vertex,Hyperedge], VisualizationModel[Vertex,Hyperedge]]) {
    
    val d = new FileDialog(parent.peer, "Save Graph Grammar") {
      setDirectory(new File(".").getAbsolutePath())
      setFilenameFilter(XMLFilter)
      setMode(FileDialog.SAVE)
      setVisible(true)
    }
    if (d.getFile() == null) return
    
    val file = new File(d.getDirectory() + "/" + d.getFile())
    saveGrammar(grammar, models, file)
  }
  
}
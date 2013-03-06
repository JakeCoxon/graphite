package com.jakemadethis.graphite
import com.jakemadethis.util.MultiSet
import scala.collection.mutable.Map
import scala.collection.mutable.ArraySeq
import com.jakemadethis.graphite.ui.GrammarFrame
import com.jakemadethis.graphite.graph._
import scala.collection.JavaConversions._
import javax.swing.UIManager
import java.io.FileReader
import javax.swing.JFileChooser
import javax.swing.JComponent
import java.io.File
import javax.swing.JFrame
import javax.swing.filechooser.FileFilter
import java.io.FilenameFilter
import java.awt.FileDialog
import edu.uci.ics.jung.visualization.VisualizationModel



object App {
  
  
  
  def main(args: Array[String]) {
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Graphite");
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
              
              
    
    openGrammar(new File("data/grammar.xml"))
    
  }
  
  def openGrammar(file : File) {
    val grammarLoader = new GrammarLoader(new FileReader(file))
    println(grammarLoader.grammar)
    
    val frame = new GrammarFrame(grammarLoader.grammar)
    frame.setVisible(true)
  }
  
  def loadGrammarGui(parent : JFrame) {
    
    val d = new FileDialog(parent, "Open Graph Grammar")
    d.setDirectory(new File(".").getAbsolutePath())
    d.setFilenameFilter(new FilenameFilter() {
      def accept(dir : File, name : String) = name.endsWith(".xml")
    })
    d.setMode(FileDialog.LOAD)
    d.setVisible(true)
    if (d.getFile() == null) return
    val file = new File(d.getDirectory()+"/"+d.getFile())
    openGrammar(file)
    println("Loaded " + file.getAbsolutePath())
  }
  
  def saveGrammarGui(parent : JFrame, grammar : HypergraphGrammar, models: Traversable[VisualizationModel[Vertex,Hyperedge]]) {
    
    val d = new FileDialog(parent, "Save Graph Grammar")
    d.setDirectory(new File(".").getAbsolutePath())
    d.setFilenameFilter(new FilenameFilter() {
      def accept(dir : File, name : String) = name.endsWith(".xml")
    })
    d.setMode(FileDialog.SAVE)
    d.setVisible(true)
    if (d.getFile() == null) return
    val file = new File(d.getDirectory()+"/"+d.getFile())
    val saver = new GrammarSaver(file, grammar, models)
    println("Saved " + file.getAbsolutePath())
  }
  
}
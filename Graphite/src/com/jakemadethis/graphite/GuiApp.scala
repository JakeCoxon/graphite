package com.jakemadethis.graphite

import java.io.FilenameFilter
import java.io.File
import com.jakemadethis.graphite.graph.GrammarLoader
import java.io.FileReader
import com.jakemadethis.graphite.ui._
import com.jakemadethis.graphite.graph._
import java.awt.FileDialog
import scala.swing._
import javax.swing.UIManager

object GuiApp {

  object XMLFilter extends FilenameFilter() {
    def accept(dir : File, name : String) = name.endsWith(".xml")
  }
  
  def start() {
    
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Graphite");
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
              
    loadGrammar(new File("data/grammar.xml"))
  }
  
  def newGrammar() {
    val grammar = new GuiGrammar()
    grammar.derivations += GrammarLoader.newDerivation("A", 2)
    new GrammarFrame(grammar, None) {
      open
    }
  }
  
  def loadGrammar(file : File) {
    val grammarLoader = new GrammarLoader(new FileReader(file))
    
    new GrammarFrame(grammarLoader.grammar, Some(file)) {
      open
    }
    
    println("Loaded " + file.getAbsolutePath())
  }
  def saveGrammar(grammar : GuiGrammar, file : File) {
    val saver = new GrammarSaver(file, grammar)
    println("Saved " + file.getAbsolutePath())
  }
  
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
  
  def loadGraphDialog(parent : Frame) {}
  
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
}
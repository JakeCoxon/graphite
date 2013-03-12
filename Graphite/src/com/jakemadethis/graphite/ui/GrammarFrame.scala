package com.jakemadethis.graphite.ui

import scala.swing._
import com.jakemadethis.graphite.graph._
import scala.collection.JavaConversions._
import edu.uci.ics.jung.graph.Hypergraph
import edu.uci.ics.jung.graph.Graph
import edu.uci.ics.jung.algorithms.layout.StaticLayout
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer
import com.jakemadethis.graphite.visualization.BasicEdgeLayout
import edu.uci.ics.jung.visualization.DefaultVisualizationModel
import edu.uci.ics.jung.visualization.VisualizationModel
import com.jakemadethis.graphite.visualization.AverageEdgeLayout
import com.jakemadethis.graphite.App
import scala.collection.immutable.Traversable
import java.io.File
import java.awt.Color
import java.awt.Dimension
import com.jakemadethis.graphite.graph.GraphExtensions._


class GrammarFrame(loadedGrammar : LoadedGrammarObject, file : Option[File]) extends MainFrame {
  
  var currentModel : DerivationModel = null
  def setDerivation(deriv : HypergraphDerivation) {
     currentModel = loadedGrammar.getModel(deriv.graph)
     graphpanel.graphModel = currentModel
  }
    
  val sidebar = new BoxPanel(Orientation.Vertical) {
    size = new Dimension(200, 10)
    background = Color.WHITE
  }
  
  
  
  // Generate the sidebar
  loadedGrammar.grammar.derivations.zipWithIndex.foreach { case (derivation, num) =>
    sidebar.contents += new NoFocusButton(Action("Rule "+(num+1)) {
      setDerivation(derivation)
    })
  }
  
  // Create graph panel with first model
  currentModel = loadedGrammar.getModel(loadedGrammar.grammar.derivations.head.graph)
  val graphpanel = new GraphPanel(currentModel)
  
  
  
  def graph = graphpanel.graph

  
  val main = new BoxPanel(Orientation.Vertical) {
    
    
    
    contents += graphpanel
  }
  
  
  val mainMenuBar = new MenuBar() {
    def menuItem(text: String)(op: => Unit) = {
      new MenuItem(Action(text)(op))
    }
    
    contents += new Menu("File") {
      contents += menuItem("Open Grammar...") {
        App.loadGrammarGui(GrammarFrame.this)
      }
      contents += menuItem("Open Graph...") {
        App.loadGraphGui(GrammarFrame.this)
      }
//      contents += menuItem("Save Grammar") {
//        if (file.isDefined) App.saveGrammar(grammarObject.grammar, models, file.get)
//        else App.saveGrammarGui(GrammarFrame.this, grammar, models)
//      }
      contents += menuItem("Save Grammar as...") {
        App.saveGrammarGui(GrammarFrame.this, loadedGrammar)
      }
    }
    contents += new Menu("Tools") {
      contents += new MenuItem("Add Vertex")
      contents += new MenuItem("Add Hyperedge")
    }
  }
  menuBar = mainMenuBar
  
  contents = new BorderPanel() {
    layout(sidebar) = BorderPanel.Position.West
    layout(main) = BorderPanel.Position.Center
  }
  
  size = new Dimension(800, 600)
}
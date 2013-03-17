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


class GrammarFrame(loadedGrammar : GuiGrammar, file : Option[File]) extends MainFrame {
  
  
  
  def setDerivation(deriv : DerivationPair) {
     graphpanel.derivationPair = deriv
  }
    
  val sidebar = new BoxPanel(Orientation.Vertical) {
    size = new Dimension(200, 10)
    background = Color.WHITE
    
    val dimension = new Dimension(1000,30)
    def button(action : Action) = new NoFocusButton(action) {
      maximumSize = dimension
    }
    
    val buttons = new BoxPanel(Orientation.Vertical)
    
    def refreshButtons() {
      buttons.contents.clear
      var toggled : ToggleButton = null
    
      // Generate the sidebar
      loadedGrammar.derivations.zipWithIndex.foreach { case (derivation, num) =>
        buttons.contents += new ToggleButton(){
          action = Action("Rule "+(num+1)) {
            toggled.selected = false
            setDerivation(derivation)
            toggled = this
            toggled.selected = true
          }
          maximumSize = dimension
          focusable = false
        }
      }
      
      toggled = buttons.contents.head.asInstanceOf[ToggleButton]
      
      buttons.revalidate
    }
    refreshButtons()
    
    contents += new ScrollPane(buttons)
    contents += button(Action("Add") {
      val label = "A"
      val newDerivation = GrammarLoader.newDerivation(label, 2)
      loadedGrammar.derivations += newDerivation
      refreshButtons()
    })
    contents += button(Action("Delete") {})
  }
  
  
  
  
  
  
  
  // Create graph panel with first model
  val graphpanel = new DerivationPanel(loadedGrammar.derivations.head)
  
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
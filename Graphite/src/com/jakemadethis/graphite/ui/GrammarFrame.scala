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
import com.jakemadethis.graphite.GuiApp


class GrammarFrame(loadedGrammar : GuiGrammar, file : Option[File]) extends MainFrame {
  
  
  // Create graph panel with first model
  val graphpanel = new DerivationPanel(loadedGrammar.initialGraph)
  
  val buttons = new BoxPanel(Orientation.Vertical) {
    val map = collection.mutable.Map[DerivationPair, ToggleButton]()
    var toggled : ToggleButton = null
    def setToggled(derivation : DerivationPair) {
      map.get(derivation) map {b =>
        if (toggled != null) toggled.selected = false
        toggled = b
        toggled.selected = true
      }
    }
    def clear {
      toggled = null
      contents.clear
      map.clear
    }
    def +=(t : (DerivationPair, ToggleButton)) {
      val (derivation, button) = t
      contents += button
      map += t
    }
  }
  
  
    
  def setDerivation(deriv : DerivationPair) {
    graphpanel.derivationPair = deriv
    buttons.setToggled(deriv)
    sidebar.delButton.enabled = !deriv.isInitial
  }
  
    
  val sidebar = new BoxPanel(Orientation.Vertical) {
    size = new Dimension(200, 10)
    background = Color.WHITE
    
    val dimension = new Dimension(1000,30)
    def button(action : Action) = new NoFocusButton(action) {
      maximumSize = dimension
    }
    
    
    def refreshButtons() {
      buttons.clear
      
      buttons += loadedGrammar.initialGraph -> new ToggleButton() {
        action = Action("Initial") {
          setDerivation(loadedGrammar.initialGraph)
        }
        maximumSize = dimension
        focusable = false
      }
    
      // Generate the sidebar
      loadedGrammar.derivations.zipWithIndex.foreach { case (derivation, num) =>
        buttons += derivation -> new ToggleButton(){
          action = Action("Rule "+(num+1)) {
            setDerivation(derivation)
          }
          maximumSize = dimension
          focusable = false
        }
      }
      
      buttons.setToggled(graphpanel.currentPair)
      
      buttons.revalidate
      buttons.repaint
    }
    refreshButtons()
    
    contents += new ScrollPane(buttons)
    
    
    contents += button(Action("Add") {
      val label = "A"
      val newDerivation = GrammarLoader.newDerivation(label, 2)
      loadedGrammar.derivations += newDerivation
      refreshButtons()
      setDerivation(newDerivation)
    })
    
    
    val delButton = button(Action("Delete") {
      val todel = graphpanel.currentPair
      loadedGrammar.derivations -= todel
      if (buttons.toggled == buttons.map(todel)) {
        setDerivation(loadedGrammar.initialGraph)
      }
      refreshButtons()
    })
    contents += delButton
  }
  
  
  
  
  
  
  
  
  setDerivation(loadedGrammar.initialGraph)
  
  def graph = graphpanel.graph

  
  val main = new BoxPanel(Orientation.Vertical) {
    
    
    contents += graphpanel
  }
  
  
  val mainMenuBar = new MenuBar() {
    def menuItem(text: String)(op: => Unit) = {
      new MenuItem(Action(text)(op))
    }
    
    contents += new Menu("File") {
      contents += menuItem("New Grammar") {
        GuiApp.newGrammar()
      }
      contents += menuItem("Open Grammar...") {
        GuiApp.loadGrammarDialog(GrammarFrame.this)
      }
      contents += menuItem("Open Graph...") {
        GuiApp.loadGraphDialog(GrammarFrame.this)
      }
//      contents += menuItem("Save Grammar") {
//        if (file.isDefined) App.saveGrammar(grammarObject.grammar, models, file.get)
//        else App.saveGrammarGui(GrammarFrame.this, grammar, models)
//      }
      contents += menuItem("Save Grammar as...") {
        GuiApp.saveGrammarDialog(GrammarFrame.this, loadedGrammar)
      }
    }
    contents += new Menu("Graph") {
      contents += menuItem("Duplicate items") {}
      contents += menuItem("Clear") {}
    }
  }
  menuBar = mainMenuBar
  
  contents = new BorderPanel() {
    layout(sidebar) = BorderPanel.Position.West
    layout(main) = BorderPanel.Position.Center
  }
  
  size = new Dimension(800, 600)
}
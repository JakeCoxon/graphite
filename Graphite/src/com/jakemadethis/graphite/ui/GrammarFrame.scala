package com.jakemadethis.graphite.ui

import scala.swing._
import com.jakemadethis.graphite.graph._
import scala.collection.JavaConversions._
import edu.uci.ics.jung.graph.{Hypergraph,Graph}
import edu.uci.ics.jung.algorithms.layout.StaticLayout
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer
import com.jakemadethis.graphite.visualization.{BasicEdgeLayout,AverageEdgeLayout}
import edu.uci.ics.jung.visualization.{DefaultVisualizationModel,VisualizationModel}
import scala.collection.immutable.Traversable
import java.io.File
import java.awt.{Color, Dimension}
import com.jakemadethis.graphite.graph.GraphExtensions._
import com.jakemadethis.graphite.GuiApp
import scala.swing.Swing$
import scala.swing.Separator


class GrammarFrame(loadedGrammar : GuiGrammar, file : Option[File]) extends MainFrame {
  
  val frame = this
  title = file.map { f => f.getName() }.getOrElse("Untitled Grammar")
  
  // Create graph panel with first model
  val graphpanel = new DerivationPanel(loadedGrammar.initialGraph)
  
  val derivButtons = new BoxPanel(Orientation.Vertical) {
    
    background = Color.WHITE
    
    val map = collection.mutable.Map[DerivationPair, ToggleButton]()
    var toggled : ToggleButton = null
    
    def setToggled(derivation : DerivationPair) {
      map.get(derivation) map {b =>
        if (toggled != null) toggled.selected = false
        toggled = b
        toggled.selected = true
      }
    }
    def clear() {
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
    derivButtons.setToggled(deriv)
    sidebar.delButton.enabled = !deriv.isInitial
  }
  
    
  val sidebar = new BoxPanel(Orientation.Vertical) {
    preferredSize = new Dimension(150, 10)
    
    val dimension = new Dimension(1000,30)
    def button(action : Action) = new NoFocusButton(action) {
      maximumSize = dimension
    }
    
    
    def refreshButtons() {
      derivButtons.clear
      
      derivButtons += loadedGrammar.initialGraph -> new ToggleButton() {
        action = Action("Initial") {
          setDerivation(loadedGrammar.initialGraph)
        }
        maximumSize = dimension
        focusable = false
      }
    
      // Generate the sidebar
      loadedGrammar.derivations.zipWithIndex.foreach { case (derivation, num) =>
        derivButtons += derivation -> new ToggleButton(){
          action = Action("Rule "+(num+1)) {
            setDerivation(derivation)
          }
          maximumSize = dimension
          focusable = false
          
        }
      }
      
      derivButtons.setToggled(graphpanel.currentPair)
      
      derivButtons.revalidate
      derivButtons.repaint
    }
    refreshButtons()
    
    contents += new ScrollPane(derivButtons) {
      border = Swing.EmptyBorder(0)
    }
    
    
    contents += button(Action("Add Rule") {
      val label = "A"
      val newDerivation = GuiGrammar.newDerivation(label, 2)
      loadedGrammar.derivations += newDerivation
      refreshButtons()
      setDerivation(newDerivation)
    })
    
    
    val delButton = button(Action("Delete Rule") {
      val todel = graphpanel.currentPair
      loadedGrammar.derivations -= todel
      if (derivButtons.toggled eq derivButtons.map(todel)) {
        setDerivation(loadedGrammar.initialGraph)
      }
      refreshButtons()
    })
    contents += delButton
    
    contents += Swing.VStrut(20)
//    contents += new Separator(Orientation.Horizontal) {
//      maximumSize = new Dimension(1000,10)
//    }
    contents += new Button() {
      action = Action("Generate...") {
        def generate(obj : GenerateDialogObject) {
          frame.publish(GenerateGraphs(frame, loadedGrammar, obj.size, obj.number))
        }
        new GenerateDialog(GrammarFrame.this)(generate) {
          centerOnScreen
          open
        }
      }
      focusable = false
      maximumSize = dimension
    }
  }
  
  
  
  
  
  
  
  
  
  
  setDerivation(loadedGrammar.initialGraph)
  
  
  menuBar = new MenuBar() {
    def menuItem(text: String)(op: => Unit) = {
      new MenuItem(Action(text)(op))
    }
    
    contents += new Menu("File") {
      contents += menuItem("New Grammar") {
        frame.publish(NewGrammar())
      }
      contents += menuItem("Open Grammar...") {
        frame.publish(LoadGrammar(frame))
      }
//      contents += menuItem("Save Grammar") {
//        if (file.isDefined) App.saveGrammar(grammarObject.grammar, models, file.get)
//        else App.saveGrammarGui(GrammarFrame.this, grammar, models)
//      }
      contents += menuItem("Save Grammar As...") {
        frame.publish(SaveGrammar(frame, loadedGrammar, saveAs=true))
      }
      
      contents += new Separator()
      contents += menuItem("Open Graph...") {
        frame.publish(LoadGraph(frame))
      }
    }
    contents += new Menu("Graph") {
      contents += menuItem("Duplicate items") {}
      contents += menuItem("Clear") {}
    }
  }
  
  contents = new BorderPanel() {
    layout(sidebar) = BorderPanel.Position.West
    layout(graphpanel) = BorderPanel.Position.Center
  }
  
  size = new Dimension(800, 600)
}

case class NewGrammar extends event.Event
case class LoadGrammar(parent : Frame) extends event.Event
case class SaveGrammar(parent : Frame, grammar : GuiGrammar, saveAs : Boolean) extends event.Event
case class LoadGraph(parent : Frame) extends event.Event
case class GenerateGraphs(parent : Frame, grammar : GuiGrammar, size : Int, number : Int) extends event.Event


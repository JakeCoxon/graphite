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


class GrammarFrame(loadedGrammar : LoadedGrammarObject, file : Option[File]) extends MainFrame {
  
      
  
  val sidebar = new BoxPanel(Orientation.Vertical) {
    size = new Dimension(200, 10)
    background = Color.WHITE
  }
  
  
  // Generate the sidebar
  loadedGrammar.models.zipWithIndex.foreach { case (model, num) =>
    sidebar.contents += new NoFocusButton(Action("Rule "+(num+1)) {
      graphpanel.graphModel = model
    })
  }
  
  // Create graph panel with first model
  val graphpanel = new GraphPanel(loadedGrammar.models.head)
  
  
  def graph = graphpanel.graph

  
  val main = new BoxPanel(Orientation.Vertical) {
    
    val menubar = new FlowPanel() {
      background = Color.DARK_GRAY
      contents += new NoFocusButton(Action("Add Vertex") {
        graph.addVertex(new Vertex())
        graphpanel.visualization.repaint()
      })
      
      contents += new NoFocusButton(Action("Add Edge") {
        def addEdge(d : EdgeDialogSuccess) {
          val vs = (1 to d.sizing).map {i => new FakeVertex()}
          vs foreach { graph.addVertex(_) }
          graph.addEdge(new Hyperedge(d.label, d.termination), vs)
          graphpanel.visualization.repaint()
        }
        new AddEdgeDialog(GrammarFrame.this, addEdge(_)) {
          centerOnScreen
          open
        }
      })
      
      contents += new NoFocusButton(Action("Delete") {
        val vs = graphpanel.pickedVertices
        val es = graphpanel.pickedEdges ++ vs.flatMap { v => graph.getIncidentEdges(v) }
        
        if (es.size + vs.size > 0) {
          def dialog = {
            val vtext = vs.size match { case 0 => null case 1 => "1 vertex" case x => x + " vertices" }
            val etext = es.size match { case 0 => null case 1 => "1 edge" case x => x + " edges" }
            val text = List(vtext, etext).filter(_ != null).mkString(" and ")
            
            Dialog.showConfirmation(this, 
              message="Delete "+text+"?", 
              title="Delete?")
          }
                
          if (es.size + vs.size == 1 || dialog == Dialog.Result.Ok) {
            es.foreach { graph.removeEdge(_) }
            vs.foreach { graph.removeVertex(_) }
            graphpanel.visualization.repaint()
          }
        }
      })
    }
    
    contents ++= menubar :: graphpanel :: Nil
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
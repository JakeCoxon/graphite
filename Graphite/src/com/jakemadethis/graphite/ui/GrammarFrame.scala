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


class GrammarFrame(grammar : HypergraphGrammar, file : Option[File]) extends MainFrame {
  
  /** Make a map of hypergraphs to models **/
  protected def makeModels(g : HypergraphGrammar) : Map[Hypergraph[Vertex,Hyperedge], VisualizationModel[Vertex,Hyperedge]] = {
    
    // Construct a model for each derivation
    g.derivations.toList.map { derivation => 
      val pseudoGraph = derivation.graph.asInstanceOf[Graph[Vertex, Hyperedge]];
      
      object glayout extends StaticLayout[Vertex, Hyperedge](pseudoGraph, new RandomLocationTransformer(new Dimension(500, 500)), new Dimension(500, 500))
        with AverageEdgeLayout[Vertex, Hyperedge]
              
      derivation.graph -> new DefaultVisualizationModel(glayout)
    }.toMap
    
  }
      
  
  val sidebar = new BoxPanel(Orientation.Vertical) {
    size = new Dimension(200, 10)
    background = Color.WHITE
  }
  
  // Create models
  val models = makeModels(grammar)
  
  // Generate the sidebar
  models.values.zipWithIndex.foreach { case (model, num) =>
    sidebar.contents += new NoFocusButton(Action("Rule "+(num+1)) {
      graphpanel.graphModel = model
    })
  }
  
  // Create graph panel with first model
  val (label, model) = models.head
  val graphpanel = new GraphPanel(model)
  
  
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
    }
    
    contents ++= menubar :: graphpanel :: Nil
  }
  
  
  val mainMenuBar = new MenuBar() {
    def menuItem(text: String)(op: => Unit) = {
      new MenuItem(Action(text)(op))
    }
    
    contents += new Menu("File") {
      contents += menuItem("Load Grammar...") {
        App.loadGrammarGui(GrammarFrame.this)
      }
      contents += menuItem("Load Graph...") {
        App.loadGraphGui(GrammarFrame.this)
      }
      contents += menuItem("Save Grammar") {
        if (file.isDefined) App.saveGrammar(grammar, models, file.get)
        else App.saveGrammarGui(GrammarFrame.this, grammar, models)
      }
      contents += menuItem("Save Grammar as...") {
        App.saveGrammarGui(GrammarFrame.this, grammar, models)
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
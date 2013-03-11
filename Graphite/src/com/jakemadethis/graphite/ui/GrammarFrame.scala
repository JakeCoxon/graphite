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
  val graphpanel = new GraphPanel()
  setDerivation(loadedGrammar.grammar.derivations.head)
  
  
  def graph = graphpanel.graph

  
  val main = new BoxPanel(Orientation.Vertical) {
    
    val menubar = new FlowPanel() {
      background = Color.DARK_GRAY
      contents += new NoFocusButton(Action("Add Vertex") {
        graph.addVertex(new Vertex())
        graphpanel.visualization.repaint()
      })
      
      contents += new NoFocusButton(Action("Add Edge...") {
        def addEdge(d : EdgeDialogObject) {
          val vs = (1 to d.sizing).map {i => new FakeVertex()}
          vs foreach { graph.addVertex(_) }
          graph.addEdge(new Hyperedge(d.label, d.termination), vs)
          graphpanel.visualization.repaint()
        }
        new EdgeDialog(GrammarFrame.this)(addEdge(_)) {
          centerOnScreen
          open
        }
      })
      
      // When deleting a vertex attached to an edge, the vertex should be replaced with a fake vertex
      def replaceWithFakeVertex(g : Hypergraph[Vertex, Hyperedge], vertexToReplace : Vertex) {
        
        val edges = graph.getIncidentEdges(vertexToReplace)
        
        edges.toList.foreach { edge => 
          val fake = new FakeVertex()
          val newIncidents = graph.getIncidentVertices(edge).map(a => 
            if (a == vertexToReplace) fake else a)
          graph.removeEdge(edge)
          graph.addEdge(edge, newIncidents)
          
          // Fake vertex should have same position as old vertex
          currentModel.getGraphLayout().setLocation(fake, 
              currentModel.getGraphLayout().transform(vertexToReplace))
        }
    
      }
      def removeItems(vertices : Set[Vertex], edges : Set[Hyperedge]) {
        
        val vs = (vertices filterNot {_.isInstanceOf[FakeVertex]}) -- currentModel.derivation.externalNodes
        val es = edges
        val edgesToReplace = vs.flatMap { v => graph.getIncidentEdges(v) } -- es
        
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
            es.foreach { e => 
              graph.getIncidentVertices(e) filter {_.isInstanceOf[FakeVertex]} foreach {graph.removeVertex(_)}
              graph.removeEdge(e)
              
              graphpanel.setPicked(e, false)
            }
            vs.foreach { v => 
              if (graph.getIncidentEdges(v).exists(edgesToReplace.contains(_))) {
                replaceWithFakeVertex(graph, v)
              }
              graph.removeVertex(v)
              graphpanel.setPicked(v, false) 
            }
            
            graphpanel.visualization.repaint()
          }
        }
      }
      contents += new NoFocusButton(Action("Delete") {
        removeItems(graphpanel.pickedVertices, graphpanel.pickedEdges)
      })
      
      contents += new NoFocusButton(Action("Clear") {
        removeItems(graph.getVertices().toSet, graph.getEdges().toSet)
      })
      
      contents += new NoFocusButton(Action("Edit...") {
        if (graphpanel.pickedEdges.size == 1) {
          val edge = graphpanel.pickedEdges.head
          val oldvs = graph.getIncidentVertices(edge).toList
          
          def editEdge(d : EdgeDialogObject) {
            graph.removeEdge(edge)
            val vs = oldvs.take(d.sizing) ++ 
              ((oldvs.size until d.sizing) map {i => new FakeVertex()})
            oldvs.drop(d.sizing) filter {_.isInstanceOf[FakeVertex]} foreach {graph.removeVertex(_)}
            graph.addEdge(new Hyperedge(d.label, d.termination), vs)
            graphpanel.visualization.repaint()
          }
          
          val obj = new EdgeDialogObject(oldvs.size, edge.label, edge.termination)
          new EdgeDialog(GrammarFrame.this, obj)(editEdge(_)) {
            centerOnScreen
            open
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
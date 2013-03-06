package com.jakemadethis.graphite.ui

import java.awt.BorderLayout
import javax.swing._
import java.awt.CardLayout
import java.awt.Color
import java.awt.Dimension
import com.jakemadethis.graphite.graph._
import scala.collection.JavaConversions._
import edu.uci.ics.jung.graph.Hypergraph
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import edu.uci.ics.jung.graph.Graph
import edu.uci.ics.jung.algorithms.layout.StaticLayout
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer
import com.jakemadethis.graphite.visualization.BasicEdgeLayout
import edu.uci.ics.jung.visualization.DefaultVisualizationModel
import edu.uci.ics.jung.visualization.VisualizationModel
import com.jakemadethis.graphite.visualization.AverageEdgeLayout
import com.jakemadethis.graphite.App


class GrammarFrame(g : HypergraphGrammar) extends JFrame {
  
  implicit def convertFunctionToAction(f : => Unit) : ActionListener = new ActionListener() {
    def actionPerformed(e : ActionEvent) = f
  }
  
//  var grammar : HypergraphGrammar = g

  
  protected def makeModels(g : HypergraphGrammar) : Traversable[(String, VisualizationModel[Vertex,Hyperedge])] = {
    
    // Construct a model for each derivation
    val models = g.derivations.toList.map { derivation => 
      val pseudoGraph = derivation.graph.asInstanceOf[Graph[Vertex, Hyperedge]];
      
      object glayout extends StaticLayout[Vertex, Hyperedge](pseudoGraph, new RandomLocationTransformer(new Dimension(500, 500)), new Dimension(500, 500))
        with AverageEdgeLayout[Vertex, Hyperedge]
              
      derivation.label -> new DefaultVisualizationModel(glayout)
    }

    
    models
  }
  
//  def setGrammar(g : HypergraphGrammar) {
//    grammar = g
//    val models = makeModels(g)
//    updateSidebar(models)
//    val (label, model) = models.head
//    graphpanel.setGraphModel(model)
//  }
  
      
  
  val sidebar = new JPanel() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBackground(Color.WHITE)
    setSize(new Dimension(200, 10))
  }
  
  protected def updateSidebar(models : Traversable[(String, VisualizationModel[Vertex,Hyperedge])]) {
    sidebar.removeAll()
    
    // Add button to sidebar for each derivation, to activate the model
    models.foreach { case (label, m) =>
      val btn = new GButton(label) {
        addActionListener(graphpanel.setGraphModel(m))
      }
      sidebar.add(btn)
    }
  }
  
  // Create models
  val models = makeModels(g)
  // Generate the sidebar
  updateSidebar(models)
  // Create graph panel with first model
  val (label, model) = models.head
  val graphpanel = new GraphPanel(model)
  
  setLayout(new BorderLayout());
  

  
  val main = new JPanel() {
    
    val cards = new JPanel(new CardLayout())
    val menubar = new JPanel() {
      setBackground(Color.DARK_GRAY)
      add(new GButton("Add Vertex") {
        addActionListener({
          graphpanel.graph.addVertex(new Vertex())
          graphpanel.visualization.repaint()
        } : Unit)
      })
      add(new GButton("Add Edge") {
        addActionListener({
          val v1 = new FakeVertex()
          val v2 = new FakeVertex()
          graphpanel.graph.addVertex(v1); 
          graphpanel.graph.addVertex(v2)
          graphpanel.graph.addEdge(new Hyperedge("A", true), Seq(v1, v2))
          graphpanel.visualization.repaint()
        } : Unit)
      })
    }
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  
    cards.add("Hi", graphpanel)
    
    add(menubar);
    add(cards);
  }
  
  
  val menu = new JMenuBar() {
    add(new JMenu("File") {
      add(new JMenuItem("Load Grammar...") {
        addActionListener(App.loadGrammarGui(GrammarFrame.this))
      })
      add(new JMenuItem("Save Grammar...") {
        addActionListener(App.saveGrammarGui(GrammarFrame.this, g, models.map(_._2)))
      })
    })
    add(new JMenu("Tools") {
      add(new JMenuItem("Add Vertex"))
      add(new JMenuItem("Add Hyperedge"))
    })
  }
  setJMenuBar(menu)
  
  
  getContentPane().add(sidebar, BorderLayout.WEST);
  getContentPane().add(main, BorderLayout.CENTER);    
  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
  
  
  setVisible(true)
  setSize(800,600)
}
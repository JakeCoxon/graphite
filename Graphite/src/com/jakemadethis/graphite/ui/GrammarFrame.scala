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


class GrammarFrame(g : HypergraphGrammar) extends JFrame {
  
  implicit def convertFunctionToAction(f : => Unit) : ActionListener = new ActionListener() {
    def actionPerformed(e : ActionEvent) = f
  }

  var graphpanel : GraphPanel = null
  
  val sidebar = new JPanel() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBackground(Color.WHITE)
    setSize(new Dimension(200, 10))
  }
  
  def setGrammar(g : HypergraphGrammar) {
    
    var firstModel : VisualizationModel[Vertex, Hyperedge] = null
    
    sidebar.removeAll()
    g.foreach { case (_, derivations) => 
      
      println(derivations)
      derivations.foreach { derivation =>
        
        val pseudoGraph = derivation.graph.asInstanceOf[Graph[Vertex, Hyperedge]];
        
        object glayout extends StaticLayout[Vertex, Hyperedge](pseudoGraph, new RandomLocationTransformer(new Dimension(500, 500)), new Dimension(500, 500))
          with BasicEdgeLayout[Vertex, Hyperedge]
                
        // create visualization viewer
        val graphModel = new DefaultVisualizationModel(glayout)
        if (firstModel == null) firstModel = graphModel
          
        val btn = new GButton(derivation.label) {
          addActionListener({
            graphpanel.setGraphModel(graphModel)
          } : Unit)
        }
        sidebar.add(btn)
      }
    }
    //graphpanel.setGraph(g)
    
    
    graphpanel = new GraphPanel(firstModel)
  }
  setGrammar(g)
  
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
      add(new JMenuItem("Load Grammar..."))
      add(new JMenuItem("Save Grammar..."))
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
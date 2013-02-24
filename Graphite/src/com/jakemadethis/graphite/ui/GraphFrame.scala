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
import com.jakemadethis.graphite.ui.VisualEdge
import com.jakemadethis.graphite.ui.VisualItem
import com.jakemadethis.graphite.ui.VisualVertex
import com.jakemadethis.graphite.ui.VisualFakeVertex


class GraphFrame extends JFrame {
  
  implicit def convertFunctionToAction(f : => Unit) : ActionListener = new ActionListener() {
    def actionPerformed(e : ActionEvent) = f
  }

  var graph : Hypergraph[VisualItem, VisualEdge] = null
  val graphpanel = new GraphPanel()
  def setGraph(g : Hypergraph[VisualItem, VisualEdge]) = {
    graph = g
    graphpanel.setGraph(g)
  }
  
  setLayout(new BorderLayout());
  
  val sidebar = new JPanel() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBackground(Color.WHITE)
    setSize(new Dimension(200, 10))
    add(new GButton("OK"))
  }
  
  val main = new JPanel() {
    
    val cards = new JPanel(new CardLayout())
    val menubar = new JPanel() {
      setBackground(Color.DARK_GRAY)
      add(new GButton("Add Vertex") {
        addActionListener({
          graph.addVertex(new VisualVertex(new Vertex()))
          graphpanel.visualization.repaint()
        } : Unit)
      })
      add(new GButton("Add Edge") {
        addActionListener({
          val v1 = new VisualFakeVertex()
          val v2 = new VisualFakeVertex()
          graph.addVertex(v1); graph.addVertex(v2)
          graph.addEdge(new VisualEdge(new Hyperedge("A", true)), Seq(v1, v2))
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
  setSize(500,400)
}
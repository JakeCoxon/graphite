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

class GraphFrame extends JFrame {
  
  implicit def convertFunctionToAction(f : => Unit) : ActionListener = new ActionListener() {
    def actionPerformed(e : ActionEvent) = f
  }
  val graphpanel = new GraphPanel()
  def setGraph(g : Hypergraph[Vertex,Hyperedge]) = {
    graphpanel.setGraph(g)
  }
  
  setLayout(new BorderLayout());
  
  val sidebar = new JPanel() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBackground(Color.WHITE)
    setSize(new Dimension(200, 10))
    add(GButton("OK"))
  }
  
  val main = new JPanel() {
    
    val cards = new JPanel(new CardLayout());
    val menubar = new JPanel() {
      setBackground(Color.DARK_GRAY);
      add(GButton("Test1"));
      add(GButton("Test2"));
    }
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
  
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
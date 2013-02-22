package com.jakemadethis.graphite.ui

import java.awt.BorderLayout
import javax.swing._
import java.awt.CardLayout
import java.awt.Color
import java.awt.Dimension
import com.jakemadethis.graphite.graph._
import scala.collection.JavaConversions._
import edu.uci.ics.jung.graph.Hypergraph

class GraphFrame extends JFrame {
  
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
  
  
  getContentPane().add(sidebar, BorderLayout.WEST);
  getContentPane().add(main, BorderLayout.CENTER);    
  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
  
  
  setVisible(true)
  setSize(500,400)
}
package com.jakemadethis.graph;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.jakemadethis.graph.Chooser.ChoiceListener;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.graph.Hypergraph;

public class GraphFrame extends JFrame implements ChoiceListener<Hypergraph<Vertex, Hyperedge>> {
	private JPanel sidebar;
	private JPanel cards;
	private JPanel menubar;
	private final Chooser<Hypergraph<Vertex, Hyperedge>> chooser;

	public GraphFrame(Chooser<Hypergraph<Vertex, Hyperedge>> chooser) {
		this.chooser = chooser;
		chooser.addChoiceListener(this);
		
		setLayout(new BorderLayout());
		sidebar = new JPanel();
		sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
		sidebar.setBackground(Color.WHITE);
		getContentPane().add(sidebar, BorderLayout.WEST);
		cards = new JPanel(new CardLayout());
		JPanel main = new JPanel();
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
		menubar = new JPanel();
		menubar.setBackground(Color.DARK_GRAY);
		menubar.add(new JButton("Test1"));
		menubar.add(new JButton("Test2"));
		main.add(menubar);
		main.add(cards);
		
		getContentPane().add(main, BorderLayout.CENTER);		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);	
	}

	@Override
	public void makeChoice(Hypergraph<Vertex, Hyperedge> item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addChoice(Hypergraph<Vertex, Hyperedge> item) {
	}

	@Override
	public void removeChoice(Hypergraph<Vertex, Hyperedge> item) {
		// TODO Auto-generated method stub
		
	}
}

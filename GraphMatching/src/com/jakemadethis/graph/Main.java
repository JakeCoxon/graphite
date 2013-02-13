package com.jakemadethis.graph;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;
import org.apache.commons.collections15.functors.TruePredicate;
import org.xml.sax.SAXException;

import com.jakemadethis.graph.matching.GraphRule;
import com.jakemadethis.graph.matching.HypergraphMatcher;
import com.jakemadethis.graph.matching.MatchEdge;
import com.jakemadethis.graph.matching.MatchVertex;
import com.jakemadethis.graph.matching.RuleMatch;
import com.jakemadethis.graph.matching.RuleReplacer;
import com.jakemadethis.graph.visualization.BasicHypergraphRenderer;
import com.jakemadethis.graph.visualization.ForceDirectedLayout;
import com.jakemadethis.graph.visualization.HyperedgeLabelRenderer;
import com.jakemadethis.graph.visualization.HyperedgeLocationState;
import com.jakemadethis.graph.visualization.HyperedgePickSupport;
import com.jakemadethis.graph.visualization.HyperedgeRenderer;
import com.jakemadethis.graph.visualization.MultiPickableVertexPaint;
import com.jakemadethis.graph.visualization.MyGraphMouse;


import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.HypergraphLayoutFacade;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.PseudoHypergraph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.GraphMLReader;
import edu.uci.ics.jung.io.GraphMLWriter;
import edu.uci.ics.jung.io.graphml.EdgeMetadata;
import edu.uci.ics.jung.io.graphml.GraphMLReader2;
import edu.uci.ics.jung.io.graphml.GraphMetadata;
import edu.uci.ics.jung.io.graphml.HyperEdgeMetadata;
import edu.uci.ics.jung.io.graphml.NodeMetadata;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.MultiPickedState;


public class Main {

	private JFrame frame;
	private JPanel cards;
	private JPanel sidebar;

	Factory<Hypergraph<Vertex, Hyperedge>> factory = OrderedHypergraph.<Vertex, Hyperedge>getFactory();
	Factory<Hypergraph<MatchVertex, MatchEdge>> matchFactory = OrderedHypergraph.<MatchVertex, MatchEdge>getFactory();
	private JPanel menubar;
	private VisualizationViewer<Vertex, Hyperedge> visualization;

	private Hypergraph<Vertex, Hyperedge> makeGraph() {
		Hypergraph<Vertex, Hyperedge> g = factory.create();
		final Vertex v1 = new Vertex();
		g.addVertex(v1);
		final Vertex v2 = new Vertex();
		g.addVertex(v2);
		final Vertex v3 = new Vertex();
		g.addVertex(v3);
		g.addVertex(new Vertex());
		g.addVertex(new Vertex());
		g.addVertex(new Vertex());
		g.addEdge(new Hyperedge("A"), Arrays.asList(v1, v2, v3));
		return g;
	}
	
	private GraphRule makeRule(String label, int interfaceNodes) {

		GraphRule graphRule = new GraphRule(matchFactory.create(), factory.create());
		ArrayList<MatchVertex> edgeVertices = new ArrayList<MatchVertex>();
		
		for (int i = 0; i < interfaceNodes; i++) {
			MatchVertex mv1 = new MatchVertex();
			Vertex v1 = new Vertex();
			graphRule.addInterfaceVertex(mv1, v1);
			edgeVertices.add(mv1);
		}
		
		graphRule.getRuleGraph().addEdge(new MatchEdge(label), edgeVertices);
		
		return graphRule;
	}

	
	public Main() {
		GraphRule graphRule = makeRule("A", 3);

		Vertex r1 = graphRule.getInterfaceReplacement(0);
		Vertex r2 = graphRule.getInterfaceReplacement(1);
		graphRule.getReplacementGraph().addEdge(new Hyperedge("B"), Arrays.asList(r1, r2));
		
		Hypergraph<Vertex, Hyperedge> graph = makeGraph();
		HypergraphMatcher matcher = new HypergraphMatcher(graph, graphRule);

		RuleMatch first = matcher.getMatches().iterator().next();
		RuleReplacer replacer = new RuleReplacer(graph, first);
		Hypergraph<Vertex, Hyperedge> newGraph = replacer.createGraph(OrderedHypergraph.<Vertex, Hyperedge>getFactory());
		
		Hypergraph<Vertex, Hyperedge> loaded = load("load.xml");
		
		
		frame = new JFrame("App");
		frame.setSize(new Dimension(600, 600));
		frame.setLayout(new BorderLayout());
		sidebar = new JPanel();
		sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
		sidebar.setBackground(Color.WHITE);
		frame.getContentPane().add(sidebar, BorderLayout.WEST);
		cards = new JPanel(new CardLayout());
		JPanel main = new JPanel();
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
		menubar = new JPanel();
		menubar.setBackground(Color.DARK_GRAY);
		menubar.add(new JButton("Test1"));
		menubar.add(new JButton("Test2"));
		main.add(menubar);
		main.add(cards);
		
		frame.getContentPane().add(main, BorderLayout.CENTER);		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		
		
		show(graph, "Initial");
		//show(graphRule.getRuleGraph(), "Rule");
		show(graphRule.getReplacementGraph(), "Replacement");
		show(newGraph, "Output");
		show(loaded, "Loaded");
		
		
		cards.add("?", visualization);
		
		
		frame.getRootPane().putClientProperty("windowModified", Boolean.TRUE);
		
    frame.setVisible(true);
	}
	
	private Hypergraph<Vertex, Hyperedge> load(String filename) {
		
		Transformer<GraphMetadata, Hypergraph<Vertex, Hyperedge>> graphTrans = new Transformer<GraphMetadata, Hypergraph<Vertex,Hyperedge>>() {
			@Override
			public Hypergraph<Vertex, Hyperedge> transform(GraphMetadata m) {
				return factory.create();
			}
		};
		
		Transformer<NodeMetadata, Vertex> vertexTrans = new Transformer<NodeMetadata, Vertex>() {
			@Override
			public Vertex transform(NodeMetadata m) {
				return new Vertex();
			}
		};
		
		Transformer<EdgeMetadata, Hyperedge> edgeTrans = new Transformer<EdgeMetadata, Hyperedge>() {
			
			@Override
			public Hyperedge transform(EdgeMetadata m) {
				return new Edge();
			}
		};
		
		Transformer<HyperEdgeMetadata, Hyperedge> hyperTrans = new Transformer<HyperEdgeMetadata, Hyperedge>() {
			
			@Override
			public Hyperedge transform(HyperEdgeMetadata m) {
				return new Hyperedge(m.getProperty("name"));
			}
		};
		
		try {
			GraphMLReader2<Hypergraph<Vertex,Hyperedge>, Vertex, Hyperedge> reader = 
						new GraphMLReader2<Hypergraph<Vertex,Hyperedge>, Vertex, Hyperedge>(new FileReader(filename), 
								graphTrans, vertexTrans, edgeTrans, hyperTrans);
			
				return reader.readGraph();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
			
	}
	private <V, E> void save(Hypergraph<V, E> graph, final Layout<V, E> layout) {
		GraphMLWriter<V, E> writer = new GraphMLWriter<V, E>();
		
		writer.addVertexData("x", null, "0", new Transformer<V, String>() {
			@Override
			public String transform(V v) {
				return String.valueOf(layout.transform(v).getX());
			}
		});
		
		writer.addVertexData("y", null, "0", new Transformer<V, String>() {
			@Override
			public String transform(V v) {
				return String.valueOf(layout.transform(v).getY());
			}
		});
		
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(new File("./output.txt"));
			writer.save(graph, fileWriter);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void show(Hypergraph<Vertex, Hyperedge> graph, final String title) {
	// create hypergraph layout
			//Graph<Vertex, Hyperedge> pseudoGraph = new PseudoHypergraph<Vertex, Hyperedge>(graph);
			Graph<Vertex, Hyperedge> pseudoGraph = (Graph) graph;
			
			final ForceDirectedLayout<Vertex, Hyperedge> l = 
					new ForceDirectedLayout<Vertex, Hyperedge>(pseudoGraph, new Dimension(500, 500));
			//final HypergraphLayoutFacade<V, E> l = new HypergraphLayoutFacade<V, E>(graph, ForceDirectedLayout.class);
			//((FRLayout)l.getUnderlyingLayout()).setRepulsionMultiplier(0.5);
			
			final VisualizationViewer<Vertex, Hyperedge> v = getVisualization(l);
     
			
			cards.add(title, visualization);
			JButton btn = new JButton(title);
			btn.setAlignmentX(Component.LEFT_ALIGNMENT);
			btn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {

					v.setGraphLayout(l);
					
			    Relaxer relaxer = visualization.getModel().getRelaxer();
					if(relaxer != null) {
				    relaxer.setSleepTime(10);
						relaxer.stop();
						//relaxer.prerelax();
						relaxer.relax();
					}
				}
			});
			sidebar.add(btn);
	}
	
	private VisualizationViewer<Vertex, Hyperedge> getVisualization(Layout<Vertex, Hyperedge> l) {
		
		if (visualization != null) return visualization;
		
		MultiPickedState<Vertex> hoverVertexState = new MultiPickedState<Vertex>();
		MultiPickedState<Hyperedge> hoverEdgeState = new MultiPickedState<Hyperedge>();
		
	// create visualization viewer
		visualization = new VisualizationViewer<Vertex, Hyperedge>(l, new Dimension(500, 500));
		
		
    //DefaultModalGraphMouse<V, E> gm = new DefaultModalGraphMouse<V,E>();
    MyGraphMouse<Vertex, Hyperedge> gm = new MyGraphMouse<Vertex, Hyperedge>(hoverVertexState, hoverEdgeState);
		visualization.setGraphMouse(gm);
		visualization.setPickSupport(new HyperedgePickSupport<Vertex, Hyperedge>(visualization));
		
		visualization.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<Hyperedge>());
		visualization.getRenderContext().setVertexShapeTransformer(new ConstantTransformer(
      		new Ellipse2D.Float(-8,-8,16,16)));
		visualization.getRenderContext().setEdgeArrowPredicate(TruePredicate.<Context<Graph<Vertex,Hyperedge>,Hyperedge>>getInstance());
		visualization.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<Hyperedge>(hoverEdgeState, Color.gray, Color.cyan));
		visualization.getRenderContext().setVertexFillPaintTransformer(new MultiPickableVertexPaint<Vertex>(
				visualization.getPickedVertexState(), Color.cyan, hoverVertexState, Color.cyan.darker(), Color.black));
    //v.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<V>(hoverVertexState, Color.black, Color.yellow));

    visualization.getModel().getRelaxer().setSleepTime(10);
    
    visualization.getRenderContext().getPickedVertexState().addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				Vertex v = (Vertex)e.getItem();
				visualization.getGraphLayout().lock(v, e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		
		//v.addKeyListener(gm.getModeKeyListener());
		visualization.setFocusable(true);
		
		// replace standard renderer with hypergraph renderer
		visualization.setRenderer(new BasicHypergraphRenderer<Vertex, Hyperedge>());
		HyperedgeLabelRenderer<Vertex, Hyperedge> lr = new HyperedgeLabelRenderer<Vertex, Hyperedge>();
		lr.setDrawPredicate(new Predicate<Context<Hypergraph<Vertex,Hyperedge>,Hyperedge>>() {
			@Override
			public boolean evaluate(Context<Hypergraph<Vertex, Hyperedge>, Hyperedge> c) {
				return !(c.element instanceof Edge);
			}
		});
		visualization.getRenderer().setEdgeLabelRenderer(lr);
		HyperedgeRenderer<Vertex, Hyperedge> r = new HyperedgeRenderer<Vertex,Hyperedge>();
		r.setDrawAsHyperedge(new Predicate<Context<Hypergraph<Vertex,Hyperedge>,Hyperedge>>() {
			@Override
			public boolean evaluate(Context<Hypergraph<Vertex, Hyperedge>, Hyperedge> c) {
				return !(c.element instanceof Edge);
			}
		});
		visualization.getRenderer().setEdgeRenderer(r);
		return visualization;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
		      // Set System L&F
		  UIManager.setLookAndFeel(
		      UIManager.getSystemLookAndFeelClassName());
		} 
		catch (UnsupportedLookAndFeelException e) {
		 // handle exception
		}
		catch (ClassNotFoundException e) {
		 // handle exception
		}
		catch (InstantiationException e) {
		 // handle exception
		}
		catch (IllegalAccessException e) {
		 // handle exception
		}
		new Main();
	}

}

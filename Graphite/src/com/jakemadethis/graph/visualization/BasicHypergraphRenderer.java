package com.jakemadethis.graph.visualization;

import java.awt.Color;
import java.util.ConcurrentModificationException;

import com.jakemadethis.graph.visualization.HyperedgeRenderer;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.LayoutDecorator;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.renderers.BasicRenderer;

/**
 * This class implements a basic hypergraph renderer.
 * 
 * @author Andrea Francke, Olivier Clerc
 * @param <V> Type of hypervertex 
 * @param <E> Type of hyperedge
 */
public class BasicHypergraphRenderer<V,E> extends BasicRenderer<V,E> {

  /**
   * Construct a new basic hypergraph renderer that uses the basic
   * hyperedge and hypervertex renders.
   */
  public BasicHypergraphRenderer() {
    super();
    
    //setVertexRenderer(new BasicHypervertexRenderer<V,E>());
    //setEdgeRenderer(new HyperedgeRenderer<V,E>());
  }
  
  /**
   * Render a hypergraph.
   */
  public void render(RenderContext<V, E> renderContext, Layout<V, E> layout) {
    
    // get graph from layout
    Graph<V,E> g = layout.getGraph();

    Hypergraph<V, E> hg = g;

    // paint all the edges
    try {
      for(E e : hg.getEdges()) {
        renderEdge(renderContext, layout, e);
        renderEdgeLabel(renderContext, layout, e);
      }
    } catch(ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }

    // paint all the vertices
    try {
      for(V v : hg.getVertices()) {
        renderVertex(renderContext, layout, v);
        renderVertexLabel(renderContext, layout, v);
      }
    } catch(ConcurrentModificationException cme) {
        renderContext.getScreenDevice().repaint();
    }
  }
  
  
  public static <V, E> EdgeLayout<E> getEdgeLayout(Layout<V, E> layout) {
  	EdgeLayout<E> edgeLocations;
		if (layout instanceof LayoutDecorator) {
			LayoutDecorator layoutDecorator = (LayoutDecorator) layout;
			if (layoutDecorator.getDelegate() instanceof EdgeLayout)
				return (EdgeLayout<E>) layoutDecorator.getDelegate();
			else
				throw new Error("renderer requires EdgeLayout");
		}
		else if (layout instanceof EdgeLayout<?>)
	  	return (EdgeLayout<E>) layout;
		else
  		throw new Error("renderer requires EdgeLayout");
  }

}
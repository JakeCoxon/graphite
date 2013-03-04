/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Aug 23, 2005
 */
package com.jakemadethis.graphite.visualization.renderers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.functors.TruePredicate;

import com.jakemadethis.graphite.visualization.EdgeLayout;
import com.jakemadethis.graphite.visualization.EdgeLayout$;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

public class HyperedgeLabelRenderer<V,E> implements Renderer.EdgeLabel<V,E> {
	
  private Predicate<Context<Hypergraph<V,E>,E>> drawPredicate = TruePredicate.getInstance();

	public HyperedgeLabelRenderer() {
		
	}
	
	public void setDrawPredicate(Predicate<Context<Hypergraph<V, E>, E>> drawPredicate) {
		this.drawPredicate = drawPredicate;
	}

	public Component prepareRenderer(RenderContext<V,E> rc, EdgeLabelRenderer graphLabelRenderer, Object value, 
			boolean isSelected, E edge) {
		return rc.getEdgeLabelRenderer().<E>getEdgeLabelRendererComponent(rc.getScreenDevice(), value, 
				rc.getEdgeFontTransformer().transform(edge), isSelected, edge);
	}
    
    public void labelEdge(RenderContext<V,E> rc, Layout<V,E> layout, E e, String label) {
    	if(label == null || label.length() == 0) return;
    	
    	Graph<V,E> g = layout.getGraph();
    	
    	EdgeLayout<E> edgeLayout = EdgeLayout$.MODULE$.apply(layout);
      
      Hypergraph<V, E> hg = g;
      
      if (!drawPredicate.evaluate(Context.<Hypergraph<V, E>, E>getInstance(hg, e)))
      	return;
    	
        if (!rc.getEdgeIncludePredicate().evaluate(Context.<Graph<V,E>,E>getInstance(g,e)))
            return;
        
    		Point2D edgePos = edgeLayout.getEdgeLocation(e);
    		edgePos = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, edgePos);

        GraphicsDecorator gd = rc.getGraphicsContext();
        
        
        Component component = prepareRenderer(rc, rc.getEdgeLabelRenderer(), label, 
                rc.getPickedEdgeState().isPicked(e), e);
        
        Dimension d = component.getPreferredSize();

        
        
        AffineTransform old = gd.getTransform();
        AffineTransform xform = new AffineTransform(old);
        xform.translate(edgePos.getX(), edgePos.getY());



        gd.setTransform(xform);
        gd.setStroke(new BasicStroke(1));
    		Rectangle rect = new Rectangle(-10, -10, 20, 20);
    		gd.setPaint(Color.WHITE);
    		gd.fill(rect);
    		gd.setPaint(Color.GRAY);
    		gd.draw(rect);
    		
        xform.translate(-d.width/2, -d.height/2);
        gd.setTransform(xform);
        
        gd.draw(component, rc.getRendererPane(), 0, 0, d.width, d.height, true);

        gd.setTransform(old);
    }

}

package com.jakemadethis.graphite.visualization;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.picking.ShapePickSupport;

public class HyperedgePickSupport<V, E> extends ShapePickSupport<V, E> {

	public HyperedgePickSupport(VisualizationServer<V, E> vv) {
		super(vv);
	}
	
	@Override
	public E getEdge(Layout<V, E> layout, double x, double y) {
		return null; // For now
	}

	
}

package com.jakemadethis.graphite.visualization;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;
import edu.uci.ics.jung.visualization.picking.MultiPickedState;
import edu.uci.ics.jung.visualization.picking.PickedState;

public class HoverGraphMousePlugin<V, E> extends AbstractGraphMousePlugin implements
		MouseMotionListener {
	
	private final PickedState<V> hoverVertex;
	private final PickedState<E> hoverEdge;

	public HoverGraphMousePlugin(PickedState<V> hoverVertex, PickedState<E> hoverEdge) {
		super(0);
		this.hoverVertex = hoverVertex;
		this.hoverEdge = hoverEdge;
	}
	public HoverGraphMousePlugin() {
		super(0);
		this.hoverVertex = new MultiPickedState<V>();
		this.hoverEdge = new MultiPickedState<E>();
	}
	
	public PickedState<E> getHoverEdgeState() {
		return hoverEdge;
	}
	public PickedState<V> getHoverVertexState() {
		return hoverVertex;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {

    VisualizationViewer<V,E> vv = (VisualizationViewer)e.getSource();
    GraphElementAccessor<V,E> pickSupport = vv.getPickSupport();
    Layout<V,E> layout = vv.getGraphLayout();
    Point2D ip = e.getPoint();

    V vertex = pickSupport.getVertex(layout, ip.getX(), ip.getY());
    E edge = pickSupport.getEdge(layout, ip.getX(), ip.getY());

  	hoverVertex.clear();
  	hoverEdge.clear();
    if (vertex != null) {
    	hoverVertex.pick(vertex, true);
    } else if (edge != null) {
    	hoverEdge.pick(edge, true);
    }
	}
	
}

package com.jakemadethis.graph.visualization;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LazyMap;

import prefuse.util.force.DragForce;
import prefuse.util.force.ForceItem;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

public class ForceDirectedLayout<V, E> extends AbstractLayout<V, E> implements IterativeContext, EdgeLayout<E> {
	
	private static Object staticLock = new Object();
	
	private long lastTime = -1L;
	private long maxTimestep = 50L;
	private ForceSimulator sim;

  protected Map<E, Point2D> edgeLocations = 
    	LazyMap.decorate(new HashMap<E, Point2D>(),
    			new Transformer<E,Point2D>() {
					public Point2D transform(E arg0) {
						return new Point2D.Double();
					}});
	
  private Map<Object, ForceItem> forceItems =
  		Collections.synchronizedMap(LazyMap.decorate(new HashMap<Object,ForceItem>(), new Factory<ForceItem>() {
    		public ForceItem create() {
    			return new ForceItem();
    		}}));

	public ForceDirectedLayout(Graph<V, E> graph, Dimension dimension) {
		super(graph, dimension);
		
		sim = new ForceSimulator();
		sim.addForce(new NBodyForce());
		sim.addForce(new SpringForce());
		sim.addForce(new DragForce(0.01f));
		
	}

	@Override
	public void step() {
		if (lastTime == -1)
			lastTime = System.currentTimeMillis()-20;
		long time = System.currentTimeMillis();
		long timestep = Math.min(maxTimestep, time - lastTime);
		lastTime = time;

		// Simulator uses static factory so things go bad
		// when multiple threads run
		synchronized(staticLock) {
			sim.clear();
			initSimulator();
			sim.runSimulator(timestep);
		}
		updatePositions();
	}
	
	private void updatePositions() {
		for (V v : graph.getVertices()) {
			ForceItem forceItem = forceItems.get(v);
			if (isLocked(v)) {
				forceItem.force[0] = forceItem.force[1] = 0.0f;
				forceItem.velocity[0] = forceItem.velocity[1] = 0.0f;
				continue;
			}
			double x = forceItem.location[0];
			double y = forceItem.location[1];
			locations.get(v).setLocation(x, y);
		}
		for (E e : graph.getEdges()) {
			if (graph.getIncidentCount(e) != 2) {
				ForceItem forceItem = forceItems.get(e);
				double x = forceItem.location[0];
				double y = forceItem.location[1];
				edgeLocations.get(e).setLocation(x, y);
			} else {
				Iterator<V> iterator = graph.getIncidentVertices(e).iterator();
				Point2D p1 = edgeLocations.get(iterator.next());
				Point2D p2 = edgeLocations.get(iterator.next());
				edgeLocations.get(e).setLocation((p1.getX() + p2.getX())/2, (p1.getY() + p2.getY())/2);
			}
		}
	}

	private void initSimulator() {
		for (V v : graph.getVertices()) {
			ForceItem forceItem = forceItems.get(v);
			forceItem.mass = 1.0f;
			forceItem.location[0] = (float) locations.get(v).getX();
			forceItem.location[1] = (float) locations.get(v).getY();
			sim.addItem(forceItem);
		}
		for (E e : graph.getEdges()) {
			if (graph.getIncidentCount(e) != 2) {
				ForceItem edgeItem = forceItems.get(e);
				edgeItem.mass = 1.0f;
				edgeItem.location[0] = (float) edgeLocations.get(e).getX();
				edgeItem.location[1] = (float) edgeLocations.get(e).getY();
				sim.addItem(edgeItem);
				for (V incident : graph.getIncidentVertices(e)) {
					ForceItem f1 = forceItems.get(incident);
					sim.addSpring(edgeItem, f1, 5E-4f, 50);
				}
			} else {
				Iterator<V> iterator = graph.getIncidentVertices(e).iterator();
				ForceItem v1 = forceItems.get(iterator.next());
				ForceItem v2 = forceItems.get(iterator.next());
				sim.addSpring(v1, v2, 1E-4f, 100);
			}
		}
	}

	@Override
	public boolean done() {
		return false;
	}

	@Override
	public void initialize() {
		
	}

	@Override
	public void reset() {
		
	}

	@Override
	public Point2D getEdgeLocation(E edge) {
		return edgeLocations.get(edge);
	}
	
}

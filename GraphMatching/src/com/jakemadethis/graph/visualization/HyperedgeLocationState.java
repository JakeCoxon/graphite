package com.jakemadethis.graph.visualization;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LazyMap;

public class HyperedgeLocationState<E> {
  protected Map<E, Point2D> locations = 
    	LazyMap.decorate(new HashMap<E, Point2D>(),
    			new Transformer<E,Point2D>() {
					public Point2D transform(E arg0) {
						return new Point2D.Double();
					}});

	public Point2D get(E edge) {
		return locations.get(edge);
	}

	public void set(E edge, Point2D pos) {
		locations.get(edge).setLocation(pos);
	}
	public void set(E edge, double x, double y) {
		locations.get(edge).setLocation(x, y);
	}
}

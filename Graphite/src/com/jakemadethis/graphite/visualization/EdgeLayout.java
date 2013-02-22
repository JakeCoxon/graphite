package com.jakemadethis.graphite.visualization;

import java.awt.geom.Point2D;

public interface EdgeLayout<E> {
	Point2D getEdgeLocation(E edge);
}

package com.jakemadethis.graphite.visualization

import edu.uci.ics.jung.visualization.layout.PersistentLayout.Point
import edu.uci.ics.jung.algorithms.layout.Layout
import java.awt.geom.Point2D
import collection.JavaConversions._

class BasicEdgeLayout[V,E](layout: Layout[V,E]) extends EdgeLayout[E] {
  def getEdgeLocation(e : E) : Point2D = {
    val vs = layout.getGraph().getIncidentVertices(e)
    val ps = vs.map(layout.transform(_))
    val xs = ps.map(_.getX())
    val ys = ps.map(_.getY())
    val midX = (xs.min + xs.max) / 2
    val midY = (ys.min + ys.max) / 2

    return new Point2D.Double(midX, midY);
  }
}

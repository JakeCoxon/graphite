package com.jakemadethis.graphite.visualization

import com.jakemadethis.graphite.ui.VisualEdge
import edu.uci.ics.jung.visualization.layout.PersistentLayout.Point
import edu.uci.ics.jung.algorithms.layout.Layout
import java.awt.geom.Point2D

class BasicEdgeLayout[V,E](layout: Layout[V,E]) extends EdgeLayout[E] {
  def getEdgeLocation(e : E) : Point2D = {
    val es = layout.getGraph().getIncidentVertices(e)
    if (es.size == 2) {
      val iterator = layout.getGraph().getIncidentVertices(e).iterator()
      val p1 = layout.transform(iterator.next())
      val p2 = layout.transform(iterator.next())
      return new Point2D.Double((p1.getX() + p2.getX())/2, (p1.getY() + p2.getY())/2);
    }
    new Point2D.Double(0,0)
  }
}

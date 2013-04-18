package com.jakemadethis.graphite.visualization

import edu.uci.ics.jung.visualization.layout.PersistentLayout.Point
import edu.uci.ics.jung.algorithms.layout.Layout
import java.awt.geom.Point2D
import collection.JavaConversions._
import edu.uci.ics.jung.algorithms.layout.LayoutDecorator

trait BasicEdgeLayout[V,E] extends Layout[V,E] with EdgeLayout[E] {
  def getEdgeLocation(e : E) : Point2D = {
    val vs = getGraph().getIncidentVertices(e)
    val ps = vs.map(transform(_))
    val xs = ps.map(_.getX())
    val ys = ps.map(_.getY())
    val midX = (xs.min + xs.max) / 2
    val midY = (ys.min + ys.max) / 2

    new Point2D.Double(midX, midY)
  }
}

trait AverageEdgeLayout[V,E] extends Layout[V,E] with EdgeLayout[E] {
  def getEdgeLocation(e : E) : Point2D = {
    val vs = getGraph().getIncidentVertices(e)
    val ps = vs.map(transform(_)).toSet.toList
    if (ps.size == 1) return new Point2D.Double(ps.head.getX(), ps.head.getY()+80)
    val x = ps.map(_.getX()).sum / ps.size
    val y = ps.map(_.getY()).sum / ps.size
    new Point2D.Double(x, y)
  }
}
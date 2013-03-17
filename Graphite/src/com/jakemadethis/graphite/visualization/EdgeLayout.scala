package com.jakemadethis.graphite.visualization

import java.awt.geom.Point2D
import edu.uci.ics.jung.algorithms.layout.Layout
import edu.uci.ics.jung.algorithms.layout.LayoutDecorator

trait EdgeLayout[E] {
  val locked = collection.mutable.Map[E, Boolean]()
  def getEdgeLocation(edge : E) : Point2D
  def lockEdge(edge : E, b : Boolean) {
    if (b) locked(edge) = true
    else locked.remove(edge)
  }
  def isEdgeLocked(edge : E) = locked.getOrElse(edge, false)
}


object EdgeLayout {
  def apply[V,E](layout : Layout[V,E]) : EdgeLayout[E] = {
    layout match {
      case layout : LayoutDecorator[V,E] => apply(layout.getDelegate())
      case layout : EdgeLayout[E] => layout
      case _ => throw new Error("renderer requires Layout to have trait EdgeLayout")
    }
  }
}
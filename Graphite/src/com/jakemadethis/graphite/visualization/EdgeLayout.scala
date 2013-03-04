package com.jakemadethis.graphite.visualization

import java.awt.geom.Point2D
import edu.uci.ics.jung.algorithms.layout.Layout
import edu.uci.ics.jung.algorithms.layout.LayoutDecorator

trait EdgeLayout[E] {
  def getEdgeLocation(edge : E) : Point2D 
}


object EdgeLayout {
  def apply[V,E](layout : Layout[V,E]) : EdgeLayout[E] = {
    layout match {
      case layout : LayoutDecorator[V,E] => layout.getDelegate() match {
        case delegate : EdgeLayout[E] => delegate
      }
      case layout : EdgeLayout[E] => layout
      case _ => throw new Error("renderer requires Layout to have trait EdgeLayout")
    }
  }
}
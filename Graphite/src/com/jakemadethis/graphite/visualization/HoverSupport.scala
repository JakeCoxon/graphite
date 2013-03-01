package com.jakemadethis.graphite.visualization

import edu.uci.ics.jung.visualization.picking.MultiPickedState
import edu.uci.ics.jung.visualization.picking.PickedState

trait HoverSupport[V, E] {
  protected var hoverVertexState : PickedState[V] = new MultiPickedState[V]()
  protected var hoverEdgeState : PickedState[E] = new MultiPickedState[E]()
  
  def getHoverVertexState() = hoverVertexState
  def getHoverEdgeState() = hoverEdgeState
}

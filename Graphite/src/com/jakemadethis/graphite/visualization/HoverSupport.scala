package com.jakemadethis.graphite.visualization

import edu.uci.ics.jung.visualization.picking.MultiPickedState

class HoverSupport[V, E] {
  val hoverVertexState = new MultiPickedState[V]()
  val hoverEdgeState = new MultiPickedState[E]()
}

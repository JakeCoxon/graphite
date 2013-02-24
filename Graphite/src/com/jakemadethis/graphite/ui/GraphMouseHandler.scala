package com.jakemadethis.graphite.ui

import edu.uci.ics.jung.visualization.control.PluggableGraphMouse
import com.jakemadethis.graphite.visualization.SinglePickingGraphMousePlugin
import com.jakemadethis.graphite.visualization.BoxPickingGraphMousePlugin
import com.jakemadethis.graphite.visualization.MouseDropPlugin
import com.jakemadethis.graphite.ui.VisualItem
import com.jakemadethis.graphite.ui.VisualEdge
import edu.uci.ics.jung.graph.Hypergraph
import java.awt.event.InputEvent
import com.jakemadethis.graphite.ui.VisualFakeVertex
import com.jakemadethis.graphite.graph.GraphExtensions._
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import com.jakemadethis.graphite.visualization.HoverMousePlugin
import com.jakemadethis.graphite.visualization.HoverSupport

class GraphMouseHandler(hoverSupport : HoverSupport[VisualItem, VisualEdge]) extends PluggableGraphMouse {
  val in = 1.1f;
  val out = 1/1.1f;
    
  add(new SinglePickingGraphMousePlugin[VisualItem, VisualEdge]
      (InputEvent.BUTTON1_MASK, InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK))
  
  add(new BoxPickingGraphMousePlugin[VisualItem, VisualEdge]
      (0, InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK))
  
  add(new MouseDropPlugin[VisualItem, VisualEdge]() {
    def dragFilter(graph : Hypergraph[VisualItem, VisualEdge], drag : VisualItem) =
      drag.isInstanceOf[VisualFakeVertex]
    def dropFilter(graph : Hypergraph[VisualItem, VisualEdge], drop : VisualItem) = 
      !drop.isInstanceOf[VisualFakeVertex]
    def vertexDropped(graph : Hypergraph[VisualItem, VisualEdge], drag : VisualItem, drop : VisualItem) =
      graph.merge(drag, drop)
  })
  
  add(new HoverMousePlugin[VisualItem, VisualEdge](hoverSupport))

  val translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK)
  val scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out)
  
  add(translatingPlugin)
  add(scalingPlugin)
}
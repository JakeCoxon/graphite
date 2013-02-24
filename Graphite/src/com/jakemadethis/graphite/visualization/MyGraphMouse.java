package com.jakemadethis.graphite.visualization;

import java.awt.event.InputEvent;

import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.GraphMousePlugin;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.picking.PickedState;

public class MyGraphMouse<V, E> extends PluggableGraphMouse {
  protected GraphMousePlugin scalingPlugin;
  protected GraphMousePlugin translatingPlugin;
	final private float out;
	final private float in;
  
	public MyGraphMouse() {
		in = 1.1f;
		out = 1/1.1f;
		//pickingPlugin = new PickingGraphMousePlugin<V,E>(InputEvent.BUTTON1_MASK, InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK);
    add(new SinglePickingGraphMousePlugin<V, E>(InputEvent.BUTTON1_MASK, InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK));
		add(new BoxPickingGraphMousePlugin<V, E>(0, InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK));
		//add(new HoverGraphMousePlugin<V, E>(hoverVertexState, hoverEdgeState));
    translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
    scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out);
    
    add(translatingPlugin);
    add(scalingPlugin);
    //add(new DeselectGraphMousePlugin<V, E>(InputEvent.BUTTON1_MASK));
	}
}

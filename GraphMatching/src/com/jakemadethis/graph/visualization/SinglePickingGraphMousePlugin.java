/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 * Created on Mar 8, 2005
 *
 */
package com.jakemadethis.graph.visualization;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import javax.swing.JComponent;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;
import edu.uci.ics.jung.visualization.picking.PickedState;

/** 
 * SinglePickingGraphMousePlugin supports the picking of single graph elements
 * with the mouse. MouseButtonOne picks a single vertex
 * or edge, and MouseButtonTwo adds to the set of selected Vertices
 * or EdgeType. If a Vertex is selected and the mouse is dragged while
 * on the selected Vertex, then that Vertex will be repositioned to
 * follow the mouse until the button is released.
 * 
 * @author Tom Nelson
 * @author Jake Coxon
 */
public class SinglePickingGraphMousePlugin<V, E> extends AbstractGraphMousePlugin
    implements MouseListener, MouseMotionListener {

	/**
	 * the picked Vertex, if any
	 */
    protected V vertex;
    
    /**
     * the picked Edge, if any
     */
    protected E edge;
    
    /**
     * the x distance from the picked vertex center to the mouse point
     */
    protected double offsetx;
    
    /**
     * the y distance from the picked vertex center to the mouse point
     */
    protected double offsety;
    
    /**
     * controls whether the Vertices may be moved with the mouse
     */
    protected boolean locked;
    
    /**
     * additional modifiers for the action of adding to an existing
     * selection
     */
    protected int addToSelectionModifiers;
    
    /**
	 * create an instance with default settings
	 */
	public SinglePickingGraphMousePlugin() {
	    this(InputEvent.BUTTON1_MASK, InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK);
	}

	/**
	 * create an instance with overides
	 * @param selectionModifiers for primary selection
	 * @param addToSelectionModifiers for additional selection
	 */
    public SinglePickingGraphMousePlugin(int selectionModifiers, int addToSelectionModifiers) {
        super(selectionModifiers);
        this.addToSelectionModifiers = addToSelectionModifiers;
        this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    }
    
	/**
	 * For primary modifiers (default, MouseButton1):
	 * pick a single Vertex or Edge that
     * is under the mouse pointer. If no Vertex or edge is under
     * the pointer, unselect all picked Vertices and edges
     * For additional selection (default Shift+MouseButton1):
     * Add to the selection, a single Vertex or Edge that is
     * under the mouse pointer. If a previously picked Vertex
     * or Edge is under the pointer, it is un-picked.
	 * 
	 * @param e the event
	 */
    @SuppressWarnings("unchecked")
    public void mousePressed(MouseEvent e) {
        down = e.getPoint();
        VisualizationViewer<V,E> vv = (VisualizationViewer)e.getSource();
        GraphElementAccessor<V,E> pickSupport = vv.getPickSupport();
        PickedState<V> pickedVertexState = vv.getPickedVertexState();
        PickedState<E> pickedEdgeState = vv.getPickedEdgeState();
        if(pickSupport != null && pickedVertexState != null) {
            Layout<V,E> layout = vv.getGraphLayout();
            if(e.getModifiers() == modifiers) {
                // p is the screen point for the mouse event
                Point2D ip = e.getPoint();

                vertex = pickSupport.getVertex(layout, ip.getX(), ip.getY());
                if(vertex != null) {
                    if(pickedVertexState.isPicked(vertex) == false) {
                    	pickedVertexState.clear();
                    	pickedVertexState.pick(vertex, true);
                    }
                    // layout.getLocation applies the layout transformer so
                    // q is transformed by the layout transformer only
                    Point2D q = layout.transform(vertex);
                    // transform the mouse point to graph coordinate system
                    Point2D gp = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.LAYOUT, ip);

                    offsetx = (float) (gp.getX()-q.getX());
                    offsety = (float) (gp.getY()-q.getY());
                } else if((edge = pickSupport.getEdge(layout, ip.getX(), ip.getY())) != null) {
                  pickedEdgeState.clear();
                  pickedEdgeState.pick(edge, true);
                }
                
            } else if(e.getModifiers() == addToSelectionModifiers) {
                Point2D ip = e.getPoint();
                vertex = pickSupport.getVertex(layout, ip.getX(), ip.getY());
                if(vertex != null) {
                    boolean wasThere = pickedVertexState.pick(vertex, !pickedVertexState.isPicked(vertex));
                    if(wasThere) {
                        vertex = null;
                    } else {

                        // layout.getLocation applies the layout transformer so
                        // q is transformed by the layout transformer only
                        Point2D q = layout.transform(vertex);
                        // translate mouse point to graph coord system
                        Point2D gp = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.LAYOUT, ip);

                        offsetx = (float) (gp.getX()-q.getX());
                        offsety = (float) (gp.getY()-q.getY());
                    }
                } else if((edge = pickSupport.getEdge(layout, ip.getX(), ip.getY())) != null) {
                    pickedEdgeState.pick(edge, !pickedEdgeState.isPicked(edge));
                }
            }
        }
        if(vertex != null) e.consume();
    }

    /**
     * rejects picking if the rectangle is too small, like
     * if the user meant to select one vertex but moved the
     * mouse slightly
     * @param p
     * @param q
     * @param min
     * @return
     */
    private boolean heyThatsTooClose(Point2D p, Point2D q, double min) {
        return Math.abs(p.getX()-q.getX()) < min &&
                Math.abs(p.getY()-q.getY()) < min;
    }
    
    
    /**
	 * If the mouse is dragging a rectangle, pick the
	 * Vertices contained in that rectangle
	 * 
	 * clean up settings from mousePressed
	 */
    @SuppressWarnings("unchecked")
    public void mouseReleased(MouseEvent e) {
      Point out = e.getPoint();
      
      if (e.getModifiers() == modifiers) {
	      if (vertex == null && heyThatsTooClose(down, out, 5)) {
	        VisualizationViewer<V,E> vv = (VisualizationViewer)e.getSource();
	        PickedState<V> pickedVertexState = vv.getPickedVertexState();
	        PickedState<E> pickedEdgeState = vv.getPickedEdgeState();
	        
	      	pickedEdgeState.clear();
	        pickedVertexState.clear();
	      }
      }
    
      down = null;
      vertex = null;
      edge = null;
        
    }
    
    /**
	 * If the mouse is over a picked vertex, drag all picked
	 * vertices with the mouse.
	 * If the mouse is not over a Vertex, draw the rectangle
	 * to select multiple Vertices
	 * 
	 */
    @SuppressWarnings("unchecked")
    public void mouseDragged(MouseEvent e) {
        if(locked == false) {
            VisualizationViewer<V,E> vv = (VisualizationViewer)e.getSource();
            if(vertex != null) {
                Point p = e.getPoint();
                Point2D graphPoint = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(p);
                Point2D graphDown = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(down);
                Layout<V,E> layout = vv.getGraphLayout();
                double dx = graphPoint.getX()-graphDown.getX();
                double dy = graphPoint.getY()-graphDown.getY();
                PickedState<V> ps = vv.getPickedVertexState();
                
                for(V v : ps.getPicked()) {
                    Point2D vp = layout.transform(v);
                    vp.setLocation(vp.getX()+dx, vp.getY()+dy);
                    layout.setLocation(v, vp);
                }
                down = p;
            }
            if(vertex != null) e.consume();
            vv.repaint();
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        JComponent c = (JComponent)e.getSource();
        c.setCursor(cursor);
    }

    public void mouseExited(MouseEvent e) {
        JComponent c = (JComponent)e.getSource();
        c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void mouseMoved(MouseEvent e) {
    }

    /**
     * @return Returns the locked.
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * @param locked The locked to set.
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}

package com.jakemadethis.graphite.visualization.renderers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Composite;
import java.awt.AlphaComposite;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.functors.TruePredicate;

import com.jakemadethis.graphite.visualization.EdgeLayout;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.LayoutDecorator;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.EdgeShape.IndexedRendering;
import edu.uci.ics.jung.visualization.renderers.BasicEdgeArrowRenderingSupport;
import edu.uci.ics.jung.visualization.renderers.BasicEdgeRenderer;
import edu.uci.ics.jung.visualization.renderers.EdgeArrowRenderingSupport;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.transform.LensTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

/**
 * This class implements a basic hyperedge render. Hyperedges are drawn as organic shapes.
 * 
 * @author Andrea Francke, Olivier Clerc
 * @param <V> Type of hypervertex 
 * @param <E> Type of hyperedge
 */
public class HyperedgeRenderer<V,E> implements Renderer.Edge<V,E> {
	
  private Predicate<Context<Hypergraph<V,E>,E>> drawAsHyperedge = TruePredicate.getInstance();
  private final EdgeLayout<E> edgeLayout;
  
	public HyperedgeRenderer(EdgeLayout<E> edgeLayout) {
    this.edgeLayout = edgeLayout;
	}
	
	public void setDrawAsHyperedge(Predicate<Context<Hypergraph<V, E>, E>> drawAsHyperedge) {
		this.drawAsHyperedge = drawAsHyperedge;
	}

	public void paintEdge(RenderContext<V,E> rc, Layout<V, E> layout, E e) {

		// get graph from layout
		Graph<V,E> g = layout.getGraph();

		Hypergraph<V, E> hg = g;
		
		
		if (!drawAsHyperedge.evaluate(Context.<Hypergraph<V,E>,E>getInstance(hg,e))) {
			drawSimpleEdge(rc, layout, e);
			return;
		}
		
    GraphicsDecorator g2d = rc.getGraphicsContext();
    if (!rc.getEdgeIncludePredicate().evaluate(Context.<Graph<V,E>,E>getInstance(g,e)))
        return;
    

    
    Stroke new_stroke = rc.getEdgeStrokeTransformer().transform(e);
    Stroke old_stroke = g2d.getStroke();
    if (new_stroke != null)
        g2d.setStroke(new_stroke);
    
    
  	drawHyperedge(rc, edgeLayout, layout, e, hg);

    // restore paint and stroke
    if (new_stroke != null)
        g2d.setStroke(old_stroke);

  }
  

	protected EdgeArrowRenderingSupport edgeArrowRenderingSupport =
		new BasicEdgeArrowRenderingSupport();

	public EdgeArrowRenderingSupport getEdgeArrowRenderingSupport() {
		return edgeArrowRenderingSupport;
	}

	public void setEdgeArrowRenderingSupport(
			EdgeArrowRenderingSupport edgeArrowRenderingSupport) {
		this.edgeArrowRenderingSupport = edgeArrowRenderingSupport;
	}
	
	/**
	 * Render a hyperedge.
	 * @param hg 
	 */
	protected void drawHyperedge(RenderContext<V,E> rc, EdgeLayout<E> edgeLayout, Layout<V,E> layout, E e, Hypergraph<V, E> hg) {		
		

		
    GraphicsDecorator gd = rc.getGraphicsContext();
    
    Paint oldPaint = gd.getPaint();
    
    // get Paints for filling and drawing
    // (filling is done first so that drawing and label use same Paint)
    Paint fill_paint = rc.getEdgeFillPaintTransformer().transform(e); 
    if (fill_paint != null)
    {
        gd.setPaint(fill_paint);
    }
    Paint draw_paint = rc.getEdgeDrawPaintTransformer().transform(e);
    if (draw_paint != null)
    {
        gd.setPaint(draw_paint);
    }

    
		// construct list of points that correspond to the hypervertices using the layout
    

		List<Point2D> points = new ArrayList<Point2D>();
		for (V v : hg.getIncidentVertices(e)) {
			Point2D p = layout.transform(v);
			p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
			points.add(p);
		}
		
		Point2D edgeLoc = edgeLayout.getEdgeLocation(e);
		edgeLoc = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, edgeLoc);
		
		
		
		
		//store original, non-transparent Composite
		//Composite originalComposite = gd.getComposite();
		//gd.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15F));
		
		// set line thickness
		gd.setStroke(new BasicStroke(2));

		//gd.setColor(new Color(1f, 0f, 0f, 1f));
		for (Point2D p : points) {
			gd.drawLine((int)edgeLoc.getX(), (int)edgeLoc.getY(), (int)p.getX(), (int)p.getY());
		}
		
		
		
		gd.setPaint(oldPaint);

		
		//reset Composite to original Composite in order to draw
		// vertices et al. in a non-transparent way
		//gd.setComposite(originalComposite);
	}
	
	/**
   * Draws the edge <code>e</code>, whose endpoints are at <code>(x1,y1)</code>
   * and <code>(x2,y2)</code>, on the graphics context <code>g</code>.
   * The <code>Shape</code> provided by the <code>EdgeShapeFunction</code> instance
   * is scaled in the x-direction so that its width is equal to the distance between
   * <code>(x1,y1)</code> and <code>(x2,y2)</code>.
   */
  @SuppressWarnings("unchecked")
  protected void drawSimpleEdge(RenderContext<V,E> rc, Layout<V,E> layout, E e) {
      
      GraphicsDecorator g = rc.getGraphicsContext();
      Graph<V,E> graph = layout.getGraph();
      Pair<V> endpoints = graph.getEndpoints(e);
      V v1 = endpoints.getFirst();
      V v2 = endpoints.getSecond();
      
      Point2D p1 = layout.transform(v1);
      Point2D p2 = layout.transform(v2);
      p1 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p1);
      p2 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p2);
      float x1 = (float) p1.getX();
      float y1 = (float) p1.getY();
      float x2 = (float) p2.getX();
      float y2 = (float) p2.getY();
      
      boolean isLoop = v1.equals(v2);
      Shape s2 = rc.getVertexShapeTransformer().transform(v2);
      Shape edgeShape = rc.getEdgeShapeTransformer().transform(Context.<Graph<V,E>,E>getInstance(graph, e));
      
      boolean edgeHit = true;
      boolean arrowHit = true;
      Rectangle deviceRectangle = null;
      JComponent vv = rc.getScreenDevice();
      if(vv != null) {
          Dimension d = vv.getSize();
          deviceRectangle = new Rectangle(0,0,d.width,d.height);
      }

      AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);
      
      if(isLoop) {
          // this is a self-loop. scale it is larger than the vertex
          // it decorates and translate it so that its nadir is
          // at the center of the vertex.
          Rectangle2D s2Bounds = s2.getBounds2D();
          xform.scale(s2Bounds.getWidth(),s2Bounds.getHeight());
          xform.translate(0, -edgeShape.getBounds2D().getWidth()/2);
      } else if(rc.getEdgeShapeTransformer() instanceof EdgeShape.Orthogonal) {
          float dx = x2-x1;
          float dy = y2-y1;
          int index = 0;
          if(rc.getEdgeShapeTransformer() instanceof IndexedRendering) {
          	EdgeIndexFunction<V,E> peif = 
          		((IndexedRendering<V,E>)rc.getEdgeShapeTransformer()).getEdgeIndexFunction();
          	index = peif.getIndex(graph, e);
          	index *= 20;
          }
          GeneralPath gp = new GeneralPath();
          gp.moveTo(0,0);// the xform will do the translation to x1,y1
          if(x1 > x2) {
          	if(y1 > y2) {
          		gp.lineTo(0, index);
          		gp.lineTo(dx-index, index);
          		gp.lineTo(dx-index, dy);
          		gp.lineTo(dx, dy);
          	} else {
          		gp.lineTo(0, -index);
          		gp.lineTo(dx-index, -index);
          		gp.lineTo(dx-index, dy);
          		gp.lineTo(dx, dy);
          	}

          } else {
          	if(y1 > y2) {
          		gp.lineTo(0, index);
          		gp.lineTo(dx+index, index);
          		gp.lineTo(dx+index, dy);
          		gp.lineTo(dx, dy);
          		
          	} else {
          		gp.lineTo(0, -index);
          		gp.lineTo(dx+index, -index);
          		gp.lineTo(dx+index, dy);
          		gp.lineTo(dx, dy);
          		
          	}
          	
          }

          edgeShape = gp;
      	
      } else {
          // this is a normal edge. Rotate it to the angle between
          // vertex endpoints, then scale it to the distance between
          // the vertices
          float dx = x2-x1;
          float dy = y2-y1;
          float thetaRadians = (float) Math.atan2(dy, dx);
          xform.rotate(thetaRadians);
          float dist = (float) Math.sqrt(dx*dx + dy*dy);
          xform.scale(dist, 1.0);
      }
      
      edgeShape = xform.createTransformedShape(edgeShape);
      
      MutableTransformer vt = rc.getMultiLayerTransformer().getTransformer(Layer.VIEW);
      if(vt instanceof LensTransformer) {
      	vt = ((LensTransformer)vt).getDelegate();
      }
      edgeHit = vt.transform(edgeShape).intersects(deviceRectangle);

      if(edgeHit == true) {
          
          Paint oldPaint = g.getPaint();
          
          // get Paints for filling and drawing
          // (filling is done first so that drawing and label use same Paint)
          Paint fill_paint = rc.getEdgeFillPaintTransformer().transform(e); 
          if (fill_paint != null)
          {
              g.setPaint(fill_paint);
              g.fill(edgeShape);
          }
          Paint draw_paint = rc.getEdgeDrawPaintTransformer().transform(e);
          if (draw_paint != null)
          {
              g.setPaint(draw_paint);
              g.draw(edgeShape);
          }
          
          float scalex = (float)g.getTransform().getScaleX();
          float scaley = (float)g.getTransform().getScaleY();
          // see if arrows are too small to bother drawing
          if(scalex < .3 || scaley < .3) return;
          
          if (rc.getEdgeArrowPredicate().evaluate(Context.<Graph<V,E>,E>getInstance(graph, e))) {
          	
              Stroke new_stroke = rc.getEdgeArrowStrokeTransformer().transform(e);
              Stroke old_stroke = g.getStroke();
              if (new_stroke != null)
                  g.setStroke(new_stroke);

              
              Shape destVertexShape = 
                  rc.getVertexShapeTransformer().transform(graph.getEndpoints(e).getSecond());

              AffineTransform xf = AffineTransform.getTranslateInstance(x2, y2);
              destVertexShape = xf.createTransformedShape(destVertexShape);
              
              arrowHit = rc.getMultiLayerTransformer().getTransformer(Layer.VIEW).transform(destVertexShape).intersects(deviceRectangle);
              if(arrowHit) {
                  
                  AffineTransform at = 
                      edgeArrowRenderingSupport.getArrowTransform(rc, edgeShape, destVertexShape);
                  if(at == null) return;
                  Shape arrow = rc.getEdgeArrowTransformer().transform(Context.<Graph<V,E>,E>getInstance(graph, e));
                  arrow = at.createTransformedShape(arrow);
                  g.setPaint(rc.getArrowFillPaintTransformer().transform(e));
                  g.fill(arrow);
                  g.setPaint(rc.getArrowDrawPaintTransformer().transform(e));
                  g.draw(arrow);
              }
              
              // restore paint and stroke
              if (new_stroke != null)
                  g.setStroke(old_stroke);

          }
          
          // restore old paint
          g.setPaint(oldPaint);
      }
  }

	
}
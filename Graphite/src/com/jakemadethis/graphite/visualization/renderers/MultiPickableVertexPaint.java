/*
* Created on Mar 10, 2005
*
* Copyright (c) 2005, the JUNG Project and the Regents of the University 
* of California
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* http://jung.sourceforge.net/license.txt for a description.
*/
package com.jakemadethis.graphite.visualization.renderers;

import java.awt.Paint;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.visualization.picking.PickedInfo;

/**
 * Paints each vertex according to the <code>Paint</code>
 * parameters given in the constructor, so that picked and
 * non-picked vertices can be made to look different.
 */
public class MultiPickableVertexPaint<V> implements Transformer<V,Paint> {

    protected Paint fill_paint;
    protected PickedInfo<V> pi1;
    protected Paint picked_paint1;
		protected PickedInfo<V> pi2;
		protected Paint picked_paint2;
    
    /**
     * 
     * @param pi            specifies which vertices report as "picked"
     * @param draw_paint    <code>Paint</code> used to draw vertex shapes
     * @param fill_paint    <code>Paint</code> used to fill vertex shapes
     * @param picked_paint  <code>Paint</code> used to fill picked vertex shapes
     */
    public MultiPickableVertexPaint(PickedInfo<V> pi1, 
    		Paint picked_paint1, PickedInfo<V> pi2, Paint picked_paint2, Paint fill_paint)
    {
        if (pi1 == null || pi2 == null)
            throw new IllegalArgumentException("PickedInfo instance must be non-null");
        this.pi1 = pi1;
        this.picked_paint1 = picked_paint1;
        this.pi2 = pi2;
        this.picked_paint2 = picked_paint2;
        this.fill_paint = fill_paint;
    }

    public Paint transform(V v)
    {
        if (pi1.isPicked(v))
            return picked_paint1;
        else if (pi2.isPicked(v))
        		return picked_paint2;
        else
            return fill_paint;
    }

}

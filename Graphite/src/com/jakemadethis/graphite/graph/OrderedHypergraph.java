/*
 * Created on Feb 4, 2007
 *
 * Copyright (c) 2007, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package com.jakemadethis.graphite.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.MultiGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * An implementation of <code>Hypergraph</code> that has ordered tentacles
 */
@SuppressWarnings("serial")
public class OrderedHypergraph<V,H> 
	implements Hypergraph<V,H>, MultiGraph<V,H>, Graph<V,H>, Serializable
{
    protected Map<V, HashSet<H>> vertices; // Map of vertices to incident hyperedge sets
    protected Map<H, ArrayList<V>> edges;    // Map of hyperedges to incident vertex sets
 
    /**
     * Returns a <code>Factory</code> which creates instances of this class.
     * @param <V> vertex type of the hypergraph to be created
     * @param <H> edge type of the hypergraph to be created
     * @return a <code>Factory</code> which creates instances of this class
     */
    public static <V,H> Factory<Hypergraph<V,H>> getFactory() {
        return new Factory<Hypergraph<V,H>> () {
            public Hypergraph<V,H> create() {
                return new OrderedHypergraph<V,H>();
            }
        };
    }

    /**
     * Creates a <code>SetHypergraph</code> and initializes the internal data structures.
     */
    public OrderedHypergraph()
    {
        vertices = new HashMap<V, HashSet<H>>();
        edges = new HashMap<H, ArrayList<V>>();
    }
    
    /**
     * Adds <code>hyperedge</code> to this graph and connects them to the vertex collection <code>to_attach</code>.
     * The order in which vertices appear in <code>to_attach</code> is the order that they will appear
     * in the incident vertex collection
     * 
     * @see Hypergraph#addEdge(Object, Collection)
     */
    public boolean addEdge(H hyperedge, Collection<? extends V> to_attach)
    {
        if (hyperedge == null)
            throw new IllegalArgumentException("input hyperedge may not be null");
        
        if (to_attach == null)
            throw new IllegalArgumentException("endpoints may not be null");

        if(to_attach.contains(null)) 
            throw new IllegalArgumentException("cannot add an edge with a null endpoint");
        
        ArrayList<V> new_endpoints = new ArrayList<V>(to_attach);
        if (edges.containsKey(hyperedge))
        {
            Collection<V> attached = edges.get(hyperedge);
            if (!attached.equals(new_endpoints))
            {
                throw new IllegalArgumentException("Edge " + hyperedge + 
                        " exists in this graph with endpoints " + attached);
            }
            else
                return false;
        }
        edges.put(hyperedge, new_endpoints);
        
        for (V v : to_attach)
        {
            // add v if it's not already in the graph
            addVertex(v);
            
            // associate v with hyperedge
            vertices.get(v).add(hyperedge);
        }
        return true;
    }
    
    /**
     * @see Hypergraph#addEdge(Object, Collection, EdgeType)
     */
    public boolean addEdge(H hyperedge, Collection<? extends V> to_attach, 
    	EdgeType edge_type)
    {
    	/*if (edge_type != EdgeType.UNDIRECTED)
    		throw new IllegalArgumentException("Edge type for this " +
    				"implementation must be EdgeType.UNDIRECTED, not " + 
    				edge_type);*/
    	return addEdge(hyperedge, to_attach);
    }
    
    /**
     * @see Hypergraph#getEdgeType(Object)
     */
    public EdgeType getEdgeType(H edge)
    {
        if (containsEdge(edge))
            return EdgeType.UNDIRECTED;
        else
            return null;
    }
    
    public boolean containsVertex(V vertex) {
    	return vertices.keySet().contains(vertex);
    }
    
    public boolean containsEdge(H edge) {
    	return edges.keySet().contains(edge);
    }

    public Collection<H> getEdges()
    {
        return edges.keySet();
    }
    
    public Collection<V> getVertices()
    {
        return vertices.keySet();
    }

    public int getEdgeCount()
    {
        return edges.size();
    }
    
    public int getVertexCount()
    {
        return vertices.size();
    }
    
    public Collection<V> getNeighbors(V vertex)
    {
        if (!containsVertex(vertex))
            return null;
        
        Set<V> neighbors = new HashSet<V>();
        for (H hyperedge : vertices.get(vertex))
        {
            neighbors.addAll(edges.get(hyperedge));
        }
        return neighbors;
    }
    
    public Collection<H> getIncidentEdges(V vertex)
    {
        return vertices.get(vertex);
    }
    
    public Collection<V> getIncidentVertices(H edge)
    {
        return edges.get(edge);
    }
    
    public H findEdge(V v1, V v2)
    {
        if (!containsVertex(v1) || !containsVertex(v2))
            return null;
        
        for (H h : getIncidentEdges(v1))
        {
            if (isIncident(v2, h))
                return h;
        }
        return null;
    }

    public Collection<H> findEdgeSet(V v1, V v2)
    {
        if (!containsVertex(v1) || !containsVertex(v2))
            return null;
        
        Collection<H> edges = new ArrayList<H>();
        for (H h : getIncidentEdges(v1))
        {
            if (isIncident(v2, h))
                edges.add(h);
        }
        return Collections.unmodifiableCollection(edges);
    }
    
    public boolean addVertex(V vertex)
    {
    	if(vertex == null) 
    	    throw new IllegalArgumentException("cannot add a null vertex");
        if (containsVertex(vertex))
            return false;
        vertices.put(vertex, new HashSet<H>());
        return true;
    }
    
    public boolean removeVertex(V vertex)
    {
        if (!containsVertex(vertex))
            return false;
        for (H hyperedge : vertices.get(vertex))
        {
        		nullifyValue(edges.get(hyperedge), vertex);
        }
        vertices.remove(vertex);
        return true;
    }
    
    public boolean removeEdge(H hyperedge)
    {
        if (!containsEdge(hyperedge))
            return false;
        for (V vertex : edges.get(hyperedge))
        {
        		vertices.get(vertex).remove(hyperedge);
        }
        edges.remove(hyperedge);
        return true;
    }
    
    public boolean isNeighbor(V v1, V v2)
    {
        if (!containsVertex(v1) || !containsVertex(v2))
            return false;
        
        if (vertices.get(v2).isEmpty())
            return false;
        for (H hyperedge : vertices.get(v1))
        {
            if (edges.get(hyperedge).contains(v2))
                return true;
        }
        return false;
    }
    
    public boolean isIncident(V vertex, H edge)
    {
        if (!containsVertex(vertex) || !containsEdge(edge))
            return false;
        
        return vertices.get(vertex).contains(edge);
    }
    
    public int degree(V vertex)
    {
        if (!containsVertex(vertex))
            return 0;
        
        return vertices.get(vertex).size();
    }
    
    public int getNeighborCount(V vertex)
    {
        if (!containsVertex(vertex))
            return 0;
        
        return getNeighbors(vertex).size();
    }
    
    public int getIncidentCount(H edge)
    {
        if (!containsEdge(edge))
            return 0;
        
        return edges.get(edge).size();
    }

    public int getEdgeCount(EdgeType edge_type)
    {
        if (edge_type == EdgeType.UNDIRECTED)
            return edges.size();
        return 0;
    }

    public Collection<H> getEdges(EdgeType edge_type)
    {
        if (edge_type == EdgeType.UNDIRECTED)
            return edges.keySet();
        return null;
    }

	public EdgeType getDefaultEdgeType() 
	{
		return EdgeType.UNDIRECTED;
	}

	public Collection<H> getInEdges(V vertex) 
	{
		return getIncidentEdges(vertex);
	}

	public Collection<H> getOutEdges(V vertex) 
	{
		return getIncidentEdges(vertex);
	}

	public int inDegree(V vertex) 
	{
		return degree(vertex);
	}

	public int outDegree(V vertex) 
	{
		return degree(vertex);
	}

	public V getDest(H directed_edge) 
	{
		return null;
	}

	public V getSource(H directed_edge) 
	{
		return null;
	}

	public Collection<V> getPredecessors(V vertex) 
	{
		return getNeighbors(vertex);
	}

	public Collection<V> getSuccessors(V vertex) 
	{
		return getNeighbors(vertex);
	}
	
	
	private static <O> void nullifyValue(ArrayList<O> arrayList, O value) {
		int i = 0;
	  for (O item : arrayList) {
	  	if (item.equals(value)) {
	  		arrayList.set(i, null);
	  	}
	  	i ++;
	  }
	}
	
	
	/*
	 * For pretending we are a graph
	 */

	@Override
	public boolean isPredecessor(V v1, V v2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSuccessor(V v1, V v2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getPredecessorCount(V vertex) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSuccessorCount(V vertex) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSource(V vertex, H edge) {
		Collection<V> incidentVertices = getIncidentVertices(edge);
		if (incidentVertices != null && incidentVertices.size() == 2) {
			Iterator<V> it = incidentVertices.iterator();
			V v1 = it.next();
			if (vertex == v1) return true;
		}
		return false;
	}

	@Override
	public boolean isDest(V vertex, H edge) {
		Collection<V> incidentVertices = getIncidentVertices(edge);
		if (incidentVertices != null && incidentVertices.size() == 2) {
			Iterator<V> it = incidentVertices.iterator();
			V v1 = it.next(); V v2 = it.next();
			if (vertex == v2) return true;
		}
		return false;
	}

	@Override
	public boolean addEdge(H e, V v1, V v2) {
		return addEdge(e, new Pair<V>(v1, v2));
	}

	@Override
	public boolean addEdge(H e, V v1, V v2, EdgeType edgeType) {
		return addEdge(e, new Pair<V>(v1, v2), edgeType);
	}

	@Override
	public Pair<V> getEndpoints(H edge) {
		Collection<V> incidentVertices = getIncidentVertices(edge);
		if (incidentVertices != null && incidentVertices.size() == 2)
			return new Pair<V>(incidentVertices);
		return null;
	}

	@Override
	public V getOpposite(V vertex, H edge) {
		Collection<V> incidentVertices = getIncidentVertices(edge);
		if (incidentVertices != null && incidentVertices.size() == 2) {
			Iterator<V> it = incidentVertices.iterator();
			V v1 = it.next(); V v2 = it.next();
			if (vertex == v1) return v2;
			if (vertex == v2) return v1;
		}
		
		return null;
	}
}

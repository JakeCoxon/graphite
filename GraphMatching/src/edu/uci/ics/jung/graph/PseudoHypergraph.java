package edu.uci.ics.jung.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * This class is a wrapper for a hypergraph to make it comply with the interface of
 * a normal garph. The underlying hypergraph can be retrieved by a getter method.
 * 
 * The query methods are implemented as far as they make sense. The modifying 
 * methods do not perform anything, because the Pseudo-hypergraph cannot be changed.
 * 
 * @author Andrea Francke, Olivier Clerc
 * @param <V> Type of hypervertex 
 * @param <E> Type of hyperedge
 * */
@SuppressWarnings("serial")
public class PseudoHypergraph<V,E> extends UndirectedSparseGraph<V,E> implements
    UndirectedGraph<V,E> {
  
  private Hypergraph<V,E> hypergraph; // underlying hypergraph
  
  /**
   * Construct a new pseudo-hypergraph.
   * @param hypergraph The hypergraph to be wrapped
   */
  public PseudoHypergraph(Hypergraph<V,E> hypergraph){
    this.hypergraph = hypergraph;
  }
  
  /**
   * Retrieve the underlying hypergraph.
   * @return The underlying hypergraph
   */
  public Hypergraph<V,E> getHypergraph(){
    return hypergraph;
  }
  
  // GRAPH QUERIES
    public Collection<E> getInEdges(V vertex)
    {
        return hypergraph.getIncidentEdges(vertex);
    }

    public Collection<E> getOutEdges(V vertex) {
      return hypergraph.getIncidentEdges(vertex);
    }

    public Collection<V> getPredecessors(V vertex) {
        return hypergraph.getNeighbors(vertex);
    }

    public Collection<V> getSuccessors(V vertex) {
        return hypergraph.getNeighbors(vertex);
    }

    public E findEdge(V v1, V v2) {
        return hypergraph.findEdge(v1, v2); 
    }
    
    public Collection<E> findEdgeSet(V v1, V v2) {
        return hypergraph.findEdgeSet(v1, v2);
    }

    public Collection<E> getEdges(EdgeType edgeType) {
        if (edgeType == EdgeType.UNDIRECTED)
            return hypergraph.getEdges();
        
        else
            return null;
    }

  // return the two first hypervertices as the endpoints
  public Pair<V> getEndpoints(E edge) {
    V v1, v2;
    Iterator<V> iterator = hypergraph.getIncidentVertices(edge).iterator();
    
    if(iterator.hasNext()) {
      v1 = iterator.next();
      
      if(iterator.hasNext()) {
        v2 = iterator.next();     
      } else {
        v2 = v1;
      }
    } else {
      throw new Error("Hyperedge with zero hypervertices");
    }
    return new Pair<V>(v1, v2);
  }
  
  // return the two first hypervertices as the endpoints
  public Collection<V> getIncidentVertices(E edge) {
    /*Pair<V> endPoints = getEndpoints(edge);
    
    ArrayList<V> result = new ArrayList<V>();
    result.add(endPoints.getFirst());
    result.add(endPoints.getSecond());
    return result;*/
  	return hypergraph.getIncidentVertices(edge);
  }

    public EdgeType getEdgeType(E edge) {
        if (hypergraph.containsEdge(edge))
            return EdgeType.UNDIRECTED;
        
        else
            return null;
    }

    public V getSource(E directed_edge) {
        return null;
    }

    public V getDest(E directed_edge) {
        return null;
    }

    public boolean isSource(V vertex, E edge) {
        return false;
    }

    public boolean isDest(V vertex, E edge) {
        return false;
    }

    public Collection<E> getEdges() {
        return hypergraph.getEdges();
    }

    public Collection<V> getVertices() {
      return hypergraph.getVertices();
    }

    public boolean containsVertex(V vertex) {
        return hypergraph.containsVertex(vertex);
    }

    public boolean containsEdge(E edge) {
      return hypergraph.containsEdge(edge);
    }

    public int getEdgeCount() {
        return hypergraph.getEdgeCount();
    }

    public int getVertexCount() {
      return hypergraph.getVertexCount();
    }

    public Collection<V> getNeighbors(V vertex) {
        return hypergraph.getNeighbors(vertex);
    }

    public Collection<E> getIncidentEdges(V vertex) {
        return hypergraph.getIncidentEdges(vertex);
    }
    
  public V getOpposite(V vertex, E edge) {
    return null;
  }

  public int getPredecessorCount(V vertex) {
    return 0;
  }

  public int getSuccessorCount(V vertex) {
    return 0;
  }

  public int inDegree(V vertex) {
    return 0;
  }

  public boolean isPredecessor(V v1, V v2) {
    return false;
  }

  public boolean isSuccessor(V v1, V v2) {
    return false;
  }

  public int outDegree(V vertex) {
    return 0;
  }

  public int degree(V vertex) {
    return 0;
  }

  public int getIncidentCount(E edge) {
    return hypergraph.getIncidentCount(edge);
  }

  public int getNeighborCount(V vertex) {
    return hypergraph.getNeighborCount(vertex);
  }
  
  // GRAPH MODIFIERS
  
    public boolean addVertex(V vertex) {
      return false;
    }
  
    public boolean addEdge(E e, V v1, V v2) {
      return false;
    }

    public boolean addEdge(E e, V v1, V v2, EdgeType edgeType) {
      return false;
    }
    
    public boolean addEdge(E edge, Pair<? extends V> endpoints) {
        return false;
    }

    public boolean addEdge(E edge, Pair<? extends V> endpoints, EdgeType edgeType) {
        return false;
    }
    
  public boolean addEdge(E edge, Collection<? extends V> vertices) {
    return false;
  }

    public boolean removeVertex(V vertex) {
      return false;
    }

    public boolean removeEdge(E edge) {
      return false;
    } 
}
package com.jakemadethis.graph.matching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.jakemadethis.graph.Hyperedge;
import com.jakemadethis.graph.Vertex;

import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class Production {

  private final Hypergraph<Vertex, Hyperedge> graph;
  private List<Vertex> externalNodes;
  private final int size;
  private boolean isTerminal;
  private int nonTerminals;

  public Production(Hypergraph<Vertex, Hyperedge> graph, List<Vertex> externalNodes) {
    this.graph = graph;
    this.externalNodes = externalNodes;
    int terminals = 0, nonTerminals = 0;
    for (Hyperedge h : graph.getEdges()) {
      if (h.isTerminal()) terminals++;
      else nonTerminals ++;
    }
    this.isTerminal = terminals == graph.getEdgeCount();
    this.size = graph.getVertexCount() - externalNodes.size() + terminals;
    this.nonTerminals = nonTerminals;
  }
  
  public Hypergraph<Vertex, Hyperedge> getOriginal() {
    return graph;
  }
  
  public List<Vertex> getExternalNodes() {
    return Collections.unmodifiableList(externalNodes);
  }
  
  public boolean isExternalVertex(Vertex v) {
    return externalNodes.contains(v);
  }
  
  public int size() {
    return size;
  }
  
  public int nonTerminals() {
    return nonTerminals;
  }
  
  public boolean isTerminal() {
    return isTerminal;
  }
  
}

package com.jakemadethis.graph.matching;

import java.util.ArrayList;
import java.util.Collection;

import com.jakemadethis.graph.Hyperedge;
import com.jakemadethis.graph.Vertex;

import edu.uci.ics.jung.graph.Hypergraph;

public class ProductionSet {
  public final String label;
  public final Collection<Production> rules;
  
  public ProductionSet(String label) {
    this.label = label;
    this.rules = new ArrayList<Production>();
  }
  
  public void add(Production p) {
    rules.add(p);
  }
}

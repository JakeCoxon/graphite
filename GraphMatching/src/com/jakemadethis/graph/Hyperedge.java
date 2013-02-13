package com.jakemadethis.graph;

public class Hyperedge {


	public final String label;
  private final boolean isTerminal;

	public Hyperedge(String label) {
		this(label, false);
	}
	public Hyperedge(String label, boolean terminal) {
    this.label = label;
    this.isTerminal = terminal;
  }
	
	public Hyperedge(Hyperedge edge) {
		this.label = edge.label;
		this.isTerminal = edge.isTerminal;
	}
  
  public boolean isTerminal() {
    return isTerminal;
  }

	@Override
	public String toString() {
		return label;
	}


}

package com.jakemadethis.graph.matching;

import com.jakemadethis.graph.Hyperedge;

public class MatchEdge {


	public final String label;

	public MatchEdge(String label) {
		this.label = label;
	}
	
	public boolean matches(Hyperedge edge) {
		return label.equals(edge.label);
	}
	
	@Override
	public String toString() {
		return label;
	}


}

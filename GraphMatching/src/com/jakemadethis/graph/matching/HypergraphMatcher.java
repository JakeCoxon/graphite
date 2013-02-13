package com.jakemadethis.graph.matching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import com.jakemadethis.graph.Hyperedge;
import com.jakemadethis.graph.Vertex;

import edu.uci.ics.jung.graph.Hypergraph;

/**
 * Matches a single edge on a graph
 * @author jbc504
 *
 * @param <V>
 * @param <E>
 */
public class HypergraphMatcher {

	
	private LinkedList<Vertex> mainListOrder;
	private Collection<RuleMatch> matches = new ArrayList<RuleMatch>();
	private MatchEdge matchingEdge;
	private final GraphRule graphRule;
	
	public Collection<RuleMatch> getMatches() {
		return matches;
	}
	

	public HypergraphMatcher(Hypergraph<Vertex, Hyperedge> mainGraph, GraphRule rule) {
		this.graphRule = rule;
		Hypergraph<MatchVertex, MatchEdge> matchGraph = rule.getRuleGraph();
		if (matchGraph.getEdgeCount() != 1)
			throw new RuntimeException("Match graph should have 1 edge");
		
		matchingEdge = matchGraph.getEdges().iterator().next();
		
		for (Hyperedge mainEdge : mainGraph.getEdges()) {
			checkEdge(mainGraph, mainEdge, matchGraph);
		}
	}
	
	private void addMatch(Hyperedge edge, Collection<Vertex> matchedOrder) {
		RuleMatch match = new RuleMatch(edge, graphRule, matchedOrder);
		
		matches.add(match);
	}

	private void checkEdge(Hypergraph<Vertex, Hyperedge> mainGraph, Hyperedge edge, Hypergraph<MatchVertex, MatchEdge> matchGraph) {
		if (!matchingEdge.matches(edge)) return;
		
		Collection<Vertex> incidentVertices = mainGraph.getIncidentVertices(edge);
		Collection<MatchVertex> matchIncidentVertices = matchGraph.getIncidentVertices(matchingEdge);
		
		if (incidentVertices.size() != matchIncidentVertices.size()) return;
		
		addMatch(edge, incidentVertices);
	}


}

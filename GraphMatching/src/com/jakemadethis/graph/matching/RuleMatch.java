package com.jakemadethis.graph.matching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.MapTransformer;

import com.jakemadethis.graph.Hyperedge;
import com.jakemadethis.graph.Vertex;

public class RuleMatch {
	HashMap<MatchVertex, Vertex> vertexMap = new HashMap<MatchVertex, Vertex>();
	
	private final GraphRule graphRule;
	private final Hyperedge foundEdge;


	private final ArrayList<Vertex> matchedOrder;
	private final ArrayList<MatchVertex> interfaceVertices;

	private MatchEdge ruleEdge;

	private HashMap<MatchVertex, Vertex> transformMap;


	public RuleMatch(Hyperedge foundEdge, GraphRule graphRule, Collection<Vertex> matchedOrder) {
		this.foundEdge = foundEdge;
		this.graphRule = graphRule;
		this.matchedOrder = new ArrayList<Vertex>(matchedOrder);
		
		this.ruleEdge = graphRule.getRuleGraph().getEdges().iterator().next();
		interfaceVertices = new ArrayList<MatchVertex>(graphRule.getRuleGraph().getIncidentVertices(ruleEdge));
		
		transformMap = new HashMap<MatchVertex, Vertex>();
		for (int i = 0; i < matchedOrder.size(); i++) {
			transformMap.put(interfaceVertices.get(i), this.matchedOrder.get(i));
		}
	}
	
	public MatchVertex getMatchVertexById(int id) {
		return interfaceVertices.get(id);
	}
	public Vertex getVertexById(int id) {
		return matchedOrder.get(id);
	}
	
	public GraphRule getGraphRule() {
		return graphRule;
	}
	public Hyperedge getFoundEdge() {
		return foundEdge;
	}

	public Vertex transformVertex(MatchVertex matchVertex) {
		return transformMap.get(matchVertex);
	}
	
}

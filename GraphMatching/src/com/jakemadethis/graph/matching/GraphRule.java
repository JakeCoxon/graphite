package com.jakemadethis.graph.matching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.collections15.Factory;

import com.jakemadethis.graph.Hyperedge;
import com.jakemadethis.graph.Vertex;

import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.SetHypergraph;

public class GraphRule {

	private HashMap<MatchVertex, Vertex> ruleToReplaceInterfaces = new HashMap<MatchVertex, Vertex>();
	private HashMap<Vertex, MatchVertex> replaceToRuleInterfaces = new HashMap<Vertex, MatchVertex>();
	private Hypergraph<MatchVertex, MatchEdge> rule;
	private Hypergraph<Vertex, Hyperedge> replacement;
	private ArrayList<Vertex> interfaceNodes = new ArrayList<Vertex>();
	
	public GraphRule(Hypergraph<MatchVertex, MatchEdge> rule, Hypergraph<Vertex, Hyperedge> replacement) {
		this.rule = rule;
		this.replacement = replacement;
	}
	
	public void addInterfaceVertex(MatchVertex v1, Vertex v2) {
		rule.addVertex(v1);
		replacement.addVertex(v2);
		interfaceNodes.add(v2);
		ruleToReplaceInterfaces.put(v1, v2);
		replaceToRuleInterfaces.put(v2, v1);
	}

	public Vertex getInterfaceReplacement(int id) {
		return interfaceNodes.get(id);
	}
	public Vertex getInterfaceReplacement(MatchVertex ruleVertex) {
		return ruleToReplaceInterfaces.get(ruleVertex);
	}
	public MatchVertex getInterfaceOriginal(Vertex vertex) {
		return replaceToRuleInterfaces.get(vertex);
	}
	
	public Hypergraph<MatchVertex, MatchEdge> getRuleGraph() {
		return rule;
	}
	public Hypergraph<Vertex, Hyperedge> getReplacementGraph() {
		return replacement;
	}
	
	public boolean isInterfaceVertex(Vertex vertex) {
		return replaceToRuleInterfaces.containsKey(vertex);
	}
	
	public MatchEdge getMatchEdge() {
		return rule.getEdges().iterator().next();
	}
	

}

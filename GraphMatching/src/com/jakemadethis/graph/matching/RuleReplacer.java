package com.jakemadethis.graph.matching;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.collections15.Factory;

import com.jakemadethis.graph.Hyperedge;
import com.jakemadethis.graph.Vertex;

import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.SetHypergraph;

public class RuleReplacer {

	private Hypergraph<MatchVertex, MatchEdge> rule;
	private Hypergraph<Vertex, Hyperedge> replacement;
	private final Hypergraph<Vertex, Hyperedge> originalGraph;
	private Map<Vertex, Vertex> oldToNewVertexMap;
	private GraphRule graphRule;
	private final RuleMatch match;
	private Map<Vertex, Vertex> replaceToNewVertexMap;

	public RuleReplacer(Hypergraph<Vertex, Hyperedge> graph, RuleMatch match) {
		this.originalGraph = graph;
		this.match = match;
		graphRule = match.getGraphRule();
		rule = graphRule.getRuleGraph();
		replacement = graphRule.getReplacementGraph();
		oldToNewVertexMap = new HashMap<Vertex, Vertex>();
		replaceToNewVertexMap = new HashMap<Vertex, Vertex>();
		
	}
	
	public Hypergraph<Vertex, Hyperedge> createGraph(Factory<Hypergraph<Vertex, Hyperedge>> factory) {

		Hypergraph<Vertex, Hyperedge> newGraph = factory.create();
		
		// Add all the original vertices + edges
		
		for (Vertex vertex : originalGraph.getVertices()) {
			Vertex newVertex = new Vertex();
			oldToNewVertexMap.put(vertex, newVertex);
			newGraph.addVertex(newVertex);
		}
		
		for (Hyperedge edge : originalGraph.getEdges()) {
			if (edge != match.getFoundEdge()) {
				Collection<Vertex> incidentVertices = originalGraph.getIncidentVertices(edge);
				Collection<Vertex> newVertices = new LinkedList<Vertex>();
				for (Vertex v : incidentVertices) {
					newVertices.add(oldToNewVertexMap.get(v));
				}
				newGraph.addEdge(new Hyperedge(edge), newVertices);
			}
		}
		
		// Add all the replacement vertices + edges (minus interface vertices)
		
		for (Vertex vertex : replacement.getVertices()) {
			if (!graphRule.isInterfaceVertex(vertex)) {
				Vertex newVertex = new Vertex();
				replaceToNewVertexMap.put(vertex, newVertex);
				newGraph.addVertex(newVertex);
			}
			else {
				Vertex originalVertex = match.transformVertex(graphRule.getInterfaceOriginal(vertex));
				replaceToNewVertexMap.put(vertex, oldToNewVertexMap.get(originalVertex));
			}
		}
		
		for (Hyperedge edge : replacement.getEdges()) {
			Collection<Vertex> incidentVertices = replacement.getIncidentVertices(edge);
			Collection<Vertex> newVertices = new LinkedList<Vertex>();
			for (Vertex v : incidentVertices) {
				newVertices.add(replaceToNewVertexMap.get(v));
			}
			newGraph.addEdge(new Hyperedge(edge), newVertices);
		}
		
		return newGraph;
	}
	
}

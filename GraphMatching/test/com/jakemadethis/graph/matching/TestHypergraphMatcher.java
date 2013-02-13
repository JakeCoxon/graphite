package com.jakemadethis.graph.matching;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;

import com.jakemadethis.graph.Hyperedge;
import com.jakemadethis.graph.Vertex;
import com.jakemadethis.graph.matching.HypergraphMatcher;
import com.jakemadethis.graph.matching.MatchEdge;
import com.jakemadethis.graph.matching.MatchVertex;

import edu.uci.ics.jung.graph.SetHypergraph;

public class TestHypergraphMatcher {
	
	private SetHypergraph<Vertex, Hyperedge> makeGraph(String... names) {
		SetHypergraph<Vertex, Hyperedge> g = new SetHypergraph<Vertex, Hyperedge>();
		HashSet<Vertex> hashSet = new HashSet<Vertex>();
		for (String name : names) {
			Vertex v = new Vertex();
			g.addVertex(v);
			hashSet.add(v);
		}
		g.addEdge(new Hyperedge("?"), hashSet);
		return g;
	}
	private SetHypergraph<MatchVertex, MatchEdge> makeMatchGraph(String... names) {
		SetHypergraph<MatchVertex, MatchEdge> g = new SetHypergraph<MatchVertex, MatchEdge>();
		HashSet<MatchVertex> hashSet = new HashSet<MatchVertex>();
		for (String name : names) {
			MatchVertex v = new MatchVertex();
			g.addVertex(v);
			hashSet.add(v);
		}
		g.addEdge(new MatchEdge("?"), hashSet);
		return g;
	}
	
	/*

	@Test
	public void test1() {
		SetHypergraph<Vertex, Edge> g = makeGraph("A", "A", "A");
		SetHypergraph<MatchVertex, MatchEdge> m = makeMatchGraph("A", "A", "A");
		assertEquals(6, new HypergraphMatcher(g, m).getMatches().size());
	}
	
	@Test
	public void test2() {
		SetHypergraph<Vertex, Edge> g = makeGraph("A", "A", "B");
		SetHypergraph<MatchVertex, MatchEdge> m = makeMatchGraph("A", "A", "B");
		assertEquals(2, new HypergraphMatcher(g, m).getMatches().size());
	}

	@Test
	public void test3() {
		SetHypergraph<Vertex, Edge> g = makeGraph("A", "B", "C");
		SetHypergraph<MatchVertex, MatchEdge> m = makeMatchGraph("A", "B", "C");
		assertEquals(1, new HypergraphMatcher(g, m).getMatches().size());
	}

	@Test
	public void test4() {
		SetHypergraph<Vertex, Edge> g = makeGraph("A", "B", "C");
		SetHypergraph<MatchVertex, MatchEdge> m = makeMatchGraph("A", "B", "C", "D");
		assertEquals(0, new HypergraphMatcher(g, m).getMatches().size());
	}
	
	@Test
	public void test5() {
		SetHypergraph<Vertex, Edge> g = makeGraph("A", "B", "B", "A");
		SetHypergraph<MatchVertex, MatchEdge> m = makeMatchGraph("B", "A", "B", "A");
		assertEquals(4, new HypergraphMatcher(g, m).getMatches().size());
	}*/
	
}

package com.jakemadethis.graphite.graph

import java.io.FileReader
import edu.uci.ics.jung.io.graphml.GraphMLReader2
import org.apache.commons.collections15.Transformer
import edu.uci.ics.jung.io.graphml.GraphMetadata
import edu.uci.ics.jung.graph.Hypergraph
import edu.uci.ics.jung.io.graphml.NodeMetadata
import edu.uci.ics.jung.io.graphml.EdgeMetadata
import edu.uci.ics.jung.io.graphml.HyperEdgeMetadata
import edu.uci.ics.jung.io.GraphIOException
import java.io.Reader
import collection.JavaConversions._

class GrammarLoader(reader : Reader) {
  
  val graphTransformer = new Transformer[GraphMetadata, Hypergraph[Vertex, Hyperedge]]() {
    def transform(g : GraphMetadata) = new OrderedHypergraph()
  }
  val vertexTransformer = new Transformer[NodeMetadata, Vertex]() {
    def transform(n : NodeMetadata) = new Vertex()
  }
  val edgeTransformer = new Transformer[EdgeMetadata, Hyperedge]() {
    def transform(n : EdgeMetadata) = new Hyperedge("!!", true)
  }
  val hyperedgeTransformer = new Transformer[HyperEdgeMetadata, Hyperedge]() {
    def transform(n : HyperEdgeMetadata) = new Hyperedge("!!", true)
  }
  
  val graphreader = new GraphMLReader2(reader, 
      graphTransformer, vertexTransformer, edgeTransformer, hyperedgeTransformer)
  
  // read graphs
  try {
    while(true) { graphreader.readGraph() }
  } catch {
    case ex : GraphIOException => null
  }
  
  val derivations = graphreader.getGraphMLDocument().getGraphMetadata().map { meta =>
    val label = meta.getProperty("label")
    new HypergraphDerivation(meta.getGraph().asInstanceOf[Hypergraph[Vertex,Hyperedge]], Seq(), label)
  }
  
  def grammar = HypergraphGrammar(derivations)
  
}
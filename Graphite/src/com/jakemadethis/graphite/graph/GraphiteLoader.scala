package com.jakemadethis.graphite.graph

import java.io.FileReader
import edu.uci.ics.jung.io.graphml.GraphMLReader2
import org.apache.commons.collections15.Transformer
import edu.uci.ics.jung.io.graphml.GraphMetadata
import edu.uci.ics.jung.graph.Hypergraph
import com.jakemadethis.graphite.ui.VisualItem
import com.jakemadethis.graphite.ui.VisualEdge
import edu.uci.ics.jung.io.graphml.NodeMetadata
import com.jakemadethis.graphite.ui.VisualVertex
import edu.uci.ics.jung.io.graphml.EdgeMetadata
import edu.uci.ics.jung.io.graphml.HyperEdgeMetadata
import edu.uci.ics.jung.io.GraphIOException
import java.io.Reader

class GraphiteLoader(reader : Reader) {
  
  
  val graphTransformer = new Transformer[GraphMetadata, Hypergraph[VisualItem, VisualEdge]]() {
    def transform(g : GraphMetadata) = new OrderedHypergraph()
  }
  val vertexTransformer = new Transformer[NodeMetadata, VisualItem]() {
    def transform(n : NodeMetadata) = new VisualVertex(new Vertex())
  }
  val edgeTransformer = new Transformer[EdgeMetadata, VisualEdge]() {
    def transform(n : EdgeMetadata) = new VisualEdge(new Hyperedge("!!", true))
  }
  val hyperedgeTransformer = new Transformer[HyperEdgeMetadata, VisualEdge]() {
    def transform(n : HyperEdgeMetadata) = new VisualEdge(new Hyperedge("!!", true))
  }
  
  val graphreader = new GraphMLReader2(reader, 
      graphTransformer, vertexTransformer, edgeTransformer, hyperedgeTransformer)
  
  def readGraph() = graphreader.readGraph()
  def readGraphs() = {
    var list = List[Hypergraph[VisualItem, VisualEdge]]()
    try {
      while(true) { list = graphreader.readGraph() :: list }
    } catch {
      case ex : GraphIOException => null
    }
    list
  }
  
}
package com.jakemadethis.graphite.io
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
import edu.uci.ics.jung.graph.Graph
import com.jakemadethis.graphite.graph._
import com.jakemadethis.graphite.algorithm._
import com.jakemadethis.graphite.ui.GuiGrammar

object GrammarLoader {

}
class GrammarLoader(reader : Reader) {
  
  // This means vertices cannot be shared across graphs
  private val allExternalNodes = collection.mutable.Map[Vertex, Int]()
  
  val graphTransformer = new Transformer[GraphMetadata, Hypergraph[Vertex, Hyperedge]]() {
    def transform(m : GraphMetadata) = new OrderedHypergraph()
  }
  val vertexTransformer = new Transformer[NodeMetadata, Vertex]() {
    def transform(m : NodeMetadata) = {
      val v = if (m.getProperty("fake").toBoolean) throw new Exception("Invalid vertex") else new Vertex()
      val ex = m.getProperty("external").toInt
      if (ex > -1) allExternalNodes(v) = ex
      v
    }
  }
  val edgeTransformer = new Transformer[EdgeMetadata, Hyperedge]() {
    def transform(m : EdgeMetadata) = {
      val t = try { m.getProperty("terminal").toBoolean } catch { case x : NumberFormatException => true }
      new Hyperedge(m.getProperty("label"), Termination.terminal(t))
    }
  }
  val hyperedgeTransformer = new Transformer[HyperEdgeMetadata, Hyperedge]() {
    def transform(m : HyperEdgeMetadata) = {
      val t = try { m.getProperty("terminal").toBoolean } catch { case x : NumberFormatException => true }
      new Hyperedge(m.getProperty("label"), Termination.terminal(t))
    }
  }
  
  val graphreader = new GraphMLReader2(reader, 
      graphTransformer, vertexTransformer, edgeTransformer, hyperedgeTransformer)
  
  // read graphs
  try {
    while(true) { graphreader.readGraph() }
  } catch {
    case ex : GraphIOException => null
  }
  
  private val objs = graphreader.getGraphMLDocument().getGraphMetadata().map { meta =>
    
    // Construct a temp object
    new {
      val label = meta.getProperty("label")
      val isInitial = label == GuiGrammar.INITIAL_LABEL
      val graph = meta.getGraph().asInstanceOf[Hypergraph[Vertex,Hyperedge]]
      
      // Map any vertices to externalnode id
      def mapExternalNodeId(v : Vertex) = allExternalNodes.get(v).map {v -> _}
      val extNodes = graph.getVertices().collect(Function.unlift(mapExternalNodeId)).toList.sortBy(_._2).map(_._1)
    }
    
  }
  
  // The derivations excluding the initial graph
  val derivations = objs.filterNot {_.isInitial} map { obj => 
    obj.label -> HypergraphProduction(obj.graph, obj.extNodes)
  }
  
  // The initial graph
  val initial = objs.find {_.isInitial} map { obj =>
    HypergraphProduction(obj.graph, Seq())
  }
  
  val grammar = initial.map { ini => Grammar(derivations, ini) }
}


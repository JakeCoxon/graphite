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
import edu.uci.ics.jung.algorithms.layout.StaticLayout
import com.jakemadethis.graphite.visualization.AverageEdgeLayout
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer
import java.awt.Dimension
import edu.uci.ics.jung.graph.Graph
import java.awt.geom.Point2D
import com.jakemadethis.graphite.ui.DerivationPair
import com.jakemadethis.graphite.ui.GuiGrammar
import com.jakemadethis.graphite.ui.InitialDerivation
import com.jakemadethis.graphite.graph.FakeVertex
import com.jakemadethis.graphite.graph.Hyperedge
import com.jakemadethis.graphite.graph.OrderedHypergraph
import com.jakemadethis.graphite.graph.Termination
import com.jakemadethis.graphite.graph.Vertex

object GuiGrammarLoader {

}
class GuiGrammarLoader(reader : Reader) {
  
  // This means vertices cannot be shared across graphs
  val posMap = collection.mutable.Map[Vertex, Point2D]()
  val allExternalNodes = collection.mutable.Map[Vertex, Int]()
  
  val graphTransformer = new Transformer[GraphMetadata, Hypergraph[Vertex, Hyperedge]]() {
    def transform(m : GraphMetadata) = new OrderedHypergraph()
  }
  val vertexTransformer = new Transformer[NodeMetadata, Vertex]() {
    def transform(m : NodeMetadata) = {
      val v = if (m.getProperty("fake").toBoolean) new FakeVertex() else new Vertex()
      if (m.getProperty("x") != null && m.getProperty("y") != null) {
        val x = m.getProperty("x").toDouble
        val y = m.getProperty("y").toDouble
        posMap(v) = new Point2D.Double(x, y)
      }
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
  
  val objs = graphreader.getGraphMLDocument().getGraphMetadata().map { meta =>
    
    // Construct a temp object
    new {
      val label = meta.getProperty("label")
      val isInitial = label == GuiGrammar.INITIAL_LABEL
      val graph = meta.getGraph().asInstanceOf[Hypergraph[Vertex,Hyperedge]]
      
      // Map any vertices to externalnode id
      def mapExternalNodeId(v : Vertex) = allExternalNodes.get(v).map {v -> _}
      val extNodes = graph.getVertices().collect(Function.unlift(mapExternalNodeId)).toMap
      
      val pgraph = graph.asInstanceOf[Graph[Vertex,Hyperedge]]
      val rand = new RandomLocationTransformer[Vertex](new Dimension(500, 500))
      val layout = new StaticLayout[Vertex, Hyperedge](pgraph, rand, new Dimension(500, 500))
        with AverageEdgeLayout[Vertex, Hyperedge]
      
      // Set locations of all vertices
      graph.getVertices().foreach { v => 
        if (posMap.containsKey(v))
          layout.setLocation(v, posMap(v))
      }
    }
    
  }
  
  // The derivations excluding the initial graph
  val derivations = objs.filterNot {_.isInitial} map { obj => 
    val leftSide = DerivationPair.LeftModel(obj.label, obj.extNodes.size)
    val rightSide = DerivationPair.RightModel(obj.layout, obj.extNodes)
    new DerivationPair(leftSide, rightSide)
  }
  
  // The initial graph
  val initial = objs.find {_.isInitial} map { obj =>
    val rightSide = DerivationPair.RightModel(obj.layout, obj.extNodes)
    new InitialDerivation(obj.label, rightSide)
  }
  
  val grammar = new GuiGrammar()
  grammar.derivations ++= derivations
  grammar.initialGraph = initial getOrElse
    {throw new Exception("No initial graph")}
}


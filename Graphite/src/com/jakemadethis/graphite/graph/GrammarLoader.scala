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
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.algorithms.layout.StaticLayout
import com.jakemadethis.graphite.visualization.AverageEdgeLayout
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer
import java.awt.Dimension
import edu.uci.ics.jung.graph.Graph
import edu.uci.ics.jung.visualization.DefaultVisualizationModel
import java.awt.geom.Point2D
import org.apache.commons.collections15.functors.MapTransformer
import edu.uci.ics.jung.algorithms.layout.Layout
import scala.collection.mutable.Buffer
import com.jakemadethis.graphite.ui.DerivationPair
import com.jakemadethis.graphite.ui.GuiGrammar

object GrammarLoader {
  def newDerivation(label : String, size : Int) = {
    val extNodes = (0 to size).map {i => new Vertex() -> i}.toMap
    val graph = new OrderedHypergraph[Vertex,Hyperedge]()
    extNodes.keys.foreach { v => graph.addVertex(v) }
    
    val pgraph = graph.asInstanceOf[Graph[Vertex,Hyperedge]]
    val rand = new RandomLocationTransformer[Vertex](new Dimension(500, 500))
    val layout = new StaticLayout[Vertex, Hyperedge](pgraph, rand, new Dimension(500, 500))
      with AverageEdgeLayout[Vertex, Hyperedge]
    
    val leftSide = DerivationPair.LeftModel(label, size)
    val rightSide = DerivationPair.RightModel(layout, extNodes)
    new DerivationPair(leftSide, rightSide)
  }
}
class GrammarLoader(reader : Reader) {
  
  // This means vertices cannot be shared across graphs
  val posMap = collection.mutable.Map[Vertex, Point2D]()
  val allExternalNodes = collection.mutable.Map[Vertex, Int]()
  
  val graphTransformer = new Transformer[GraphMetadata, Hypergraph[Vertex, Hyperedge]]() {
    def transform(m : GraphMetadata) = new OrderedHypergraph()
  }
  val vertexTransformer = new Transformer[NodeMetadata, Vertex]() {
    def transform(m : NodeMetadata) = {
      val v = if (m.getProperty("fake").toBoolean) new FakeVertex() else new Vertex()
      val x = m.getProperty("x").toDouble
      val y = m.getProperty("y").toDouble
      val ex = m.getProperty("external").toInt
      if (ex > -1) allExternalNodes(v) = ex
      posMap(v) = new Point2D.Double(x, y)
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
  
  val derivations = graphreader.getGraphMLDocument().getGraphMetadata().map { meta =>
    val label = meta.getProperty("label")
    val graph = meta.getGraph().asInstanceOf[Hypergraph[Vertex,Hyperedge]]
    val extNodes = collection.mutable.Map[Vertex,Int]()
    graph.getVertices().foreach { v =>
      allExternalNodes.get(v).map { id => extNodes += (v -> id) }
    }
    
    val pgraph = graph.asInstanceOf[Graph[Vertex,Hyperedge]]
    val rand = new RandomLocationTransformer[Vertex](new Dimension(500, 500))
    val layout = new StaticLayout[Vertex, Hyperedge](pgraph, rand, new Dimension(500, 500))
      with AverageEdgeLayout[Vertex, Hyperedge]
    
    val leftSide = DerivationPair.LeftModel(label, extNodes.size)
    val rightSide = DerivationPair.RightModel(layout, extNodes)
    new DerivationPair(leftSide, rightSide)
  }
  
  val grammar = new GuiGrammar()
  grammar.derivations ++= derivations
}


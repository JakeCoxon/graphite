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

class GrammarLoader(reader : Reader) {
  
  // This means vertices cannot be shared across graphs
  val posMap = collection.mutable.Map[Vertex, Point2D]()
  val allExternalNodes = collection.mutable.Map[Vertex, Int]()
  
  val graphTransformer = new Transformer[GraphMetadata, Hypergraph[Vertex, Hyperedge]]() {
    def transform(m : GraphMetadata) = new OrderedHypergraph()
  }
  val vertexTransformer = new Transformer[NodeMetadata, Vertex]() {
    def transform(m : NodeMetadata) = {
      val v = new Vertex()
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
    val g = meta.getGraph().asInstanceOf[Hypergraph[Vertex,Hyperedge]]
    val extNodes = g.getVertices().foldLeft(Buffer[Vertex]()) { (buffer, v) =>
      allExternalNodes.get(v).map { id => 
        while (buffer.size <= id) buffer.append(null)
        buffer(id) = v
      }; buffer
    }
    new HypergraphDerivation(g, extNodes, label)
  }
  
  /** Make a model for derivation **/
  protected def makeModel(derivation : HypergraphDerivation) = {
    
    val pseudoGraph = derivation.graph.asInstanceOf[Graph[Vertex, Hyperedge]];
    
    val rand = new RandomLocationTransformer[Vertex](new Dimension(500, 500))
    val layout = new StaticLayout[Vertex, Hyperedge](pseudoGraph, rand, new Dimension(500, 500))
      with AverageEdgeLayout[Vertex, Hyperedge]
    posMap.foreach { case (v, p) => layout.setLocation(v, p) }
    new DerivationModel(derivation, layout)
  }
  
  val _grammar = HypergraphGrammar(derivations)
       
  // Construct a model for each derivation
  val modelMap = derivations.map { derivation =>      
    derivation.graph -> makeModel(derivation)
  }.toMap
  
  def grammar = _grammar
  
  val _lgo = new LoadedGrammarObject(_grammar, modelMap)
  def loadedGrammarObject = _lgo
  
}

class LoadedGrammarObject(val grammar : HypergraphGrammar, modelMap : Map[Hypergraph[Vertex, Hyperedge], DerivationModel]) {
  def getModel(graph : Hypergraph[Vertex, Hyperedge]) = modelMap(graph)
  def models = modelMap.values
  def graphs = modelMap.keys
}

class DerivationModel(val derivation : HypergraphDerivation, layout : Layout[Vertex, Hyperedge]) extends DefaultVisualizationModel(layout) {
  
}
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

class GrammarLoader(reader : Reader) {
  
  // This means vertices cannot be shared across graphs
  val posMap = collection.mutable.Map[Vertex, Point2D]()
  
  val graphTransformer = new Transformer[GraphMetadata, Hypergraph[Vertex, Hyperedge]]() {
    def transform(m : GraphMetadata) = new OrderedHypergraph()
  }
  val vertexTransformer = new Transformer[NodeMetadata, Vertex]() {
    def transform(m : NodeMetadata) = {
      val v = new Vertex()
      val x = m.getProperty("x").toDouble
      val y = m.getProperty("y").toDouble
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
    new HypergraphDerivation(meta.getGraph().asInstanceOf[Hypergraph[Vertex,Hyperedge]], Seq(), label)
  }
  
  /** Make a model for derivation **/
  protected def makeModel(derivation : HypergraphDerivation) = {
    
      val pseudoGraph = derivation.graph.asInstanceOf[Graph[Vertex, Hyperedge]];
      
      val posMapTrans = MapTransformer.getInstance(posMap)
      val layout = new StaticLayout[Vertex, Hyperedge](pseudoGraph, posMapTrans, new Dimension(500, 500))
        with AverageEdgeLayout[Vertex, Hyperedge]
      new DefaultVisualizationModel(layout)
  }
       
  // Construct a model for each derivation
  val modelMap = derivations.map { derivation =>      
    derivation.graph -> makeModel(derivation)
  }.toMap
  
  val _grammar = HypergraphGrammar(derivations)
  def grammar = _grammar
  
  val _lgo = new LoadedGrammarObject(_grammar, modelMap)
  def loadedGrammarObject = _lgo
  
}

class LoadedGrammarObject(val grammar : HypergraphGrammar, modelMap : Map[Hypergraph[Vertex, Hyperedge], VisualizationModel[Vertex, Hyperedge]]) {
  def getModel(graph : Hypergraph[Vertex, Hyperedge]) = modelMap(graph)
  def models = modelMap.values
  def graphs = modelMap.keys
}
package com.jakemadethis.graphite.graph

import java.io.Writer
import edu.uci.ics.jung.graph.Hypergraph
import java.io.BufferedWriter
import collection.JavaConversions._
import edu.uci.ics.jung.graph.UndirectedGraph
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.io.GraphMLMetadata
import org.apache.commons.collections15.Transformer
import java.awt.geom.Point2D
import java.io.FileWriter
import com.jakemadethis.graphite.graph.GraphMLWriter
import java.io.File

class GrammarSaver(file : File, grammar : HypergraphGrammar, modelMap: Map[Hypergraph[Vertex,Hyperedge], VisualizationModel[Vertex,Hyperedge]]) {
  implicit def convertFunctionToTransformer[A,B](f : A => B) : Transformer[A,B] = new Transformer[A,B]() {
    def transform(obj : A) : B = f(obj)
  }
  
  val points = modelMap.foldLeft(Map[Vertex, Point2D]()) { case (result, (graph, model)) =>
    val layout = model.getGraphLayout()
    val graph = layout.getGraph().asInstanceOf[Hypergraph[Vertex,Hyperedge]]
    
    result ++ graph.getVertices().map { v => v -> layout.transform(v) }
  }
  
  
  val derivationMap = grammar.derivations.map { d => d.graph -> d }.toMap
  
  var id = 0
  val vIdMap = collection.mutable.Map[Vertex, Int]()
  val eIdMap = collection.mutable.Map[Hyperedge, Int]()
   
  val writer = new GraphMLWriter[Vertex, Hyperedge](
    (g, v) => {
      val newid = vIdMap.getOrElseUpdate(v, { id += 1; id })
      newid.toString
    },
    (g, e) => {
      val newid = eIdMap.getOrElseUpdate(e, { id += 1; id })
      newid.toString
    })
      
  
  writer.addGraphData("label", "The grammar label", "?") { g =>
    derivationMap(g).label
  }
  writer.addEdgeData("label", "The edge label", "?") { (_, e) => e.label }
  writer.addEdgeData("terminal", "If the edge is terminal", "true") { (_, e) => e.isTerminal.toString }
  
  writer.addVertexData("x", "The x coordinate", "?") { (g, v) =>
    modelMap(g).getGraphLayout().transform(v).getX().toString
  }
  writer.addVertexData("y", "The y coordinate", "?") { (g, v) =>
    modelMap(g).getGraphLayout().transform(v).getY().toString
  }
  
  writer.saveHypergraphs(modelMap.keys, file)
}


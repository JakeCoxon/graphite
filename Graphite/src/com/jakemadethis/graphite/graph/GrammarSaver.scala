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
import com.jakemadethis.graphite.ui.GuiGrammar

class GrammarSaver(file : File, grammar : GuiGrammar) {
  implicit def convertFunctionToTransformer[A,B](f : A => B) : Transformer[A,B] = new Transformer[A,B]() {
    def transform(obj : A) : B = f(obj)
  }
  
  val derivations = grammar.initialGraph +: grammar.derivations
  
  val points = derivations.foldLeft(Map[Vertex, Point2D]()) { case (result, pair) =>
    val layout = pair.rightSide.getGraphLayout
    val graph = layout.getGraph().asInstanceOf[Hypergraph[Vertex,Hyperedge]]
    
    result ++ graph.getVertices().map { v => v -> layout.transform(v) }
  }
  
  
  val derivationMap = derivations.map { pair => pair.rightSide.graph -> pair }.toMap
  
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
      
  
  writer.addGraphData("label", "The grammar label", "?") { g => derivationMap(g).label }
  writer.addEdgeData("label", "The edge label", "?") { (_, e) => e.label }
  writer.addEdgeData("terminal", "If the edge is terminal", "true") { (_, e) => e.isTerminal.toString }
  
  writer.addVertexData("x", "The x coordinate", "?") { (g, v) =>
    derivationMap(g).rightSide.getGraphLayout().transform(v).getX().toString
  }
  writer.addVertexData("y", "The y coordinate", "?") { (g, v) =>
    derivationMap(g).rightSide.getGraphLayout().transform(v).getY().toString
  }
  writer.addVertexData("external", "The external node id", "-1") { (g, v) =>
    val id = derivationMap(g).rightSide.externalNodeId(v)
    id.map { _.toString }.orNull
  }
  writer.addVertexData("fake", "If this vertex is fake", "false") { (g, v) =>
    v match { case v : FakeVertex => "true" case _ => null }
  }
  
  val graphs = derivations.map { pair => pair.rightSide.graph }
  writer.saveHypergraphs(graphs, file)
}


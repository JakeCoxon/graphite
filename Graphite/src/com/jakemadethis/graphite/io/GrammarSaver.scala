package com.jakemadethis.graphite.io
import edu.uci.ics.jung.graph.Hypergraph
import collection.JavaConversions._
import org.apache.commons.collections15.Transformer
import java.awt.geom.Point2D
import java.io.File
import com.jakemadethis.graphite.graph.FakeVertex
import com.jakemadethis.graphite.graph.Hyperedge
import com.jakemadethis.graphite.graph.Vertex
import com.jakemadethis.graphite.algorithm._
import com.jakemadethis.graphite.ui.GuiGrammar

class GrammarSaver(file : File, grammar : Grammar[HypergraphProduction]) {
  implicit def convertFunctionToTransformer[A,B](f : A => B) : Transformer[A,B] = new Transformer[A,B]() {
    def transform(obj : A) : B = f(obj)
  }
  
  val derivations = (GuiGrammar.INITIAL_LABEL, grammar.initial) +: grammar.productions
  
  val derivationMap = derivations.map { t => t._2.graph -> t }.toMap
  
  class Incrementor[C] {
    var id = 0
    val map = collection.mutable.Map[C, Int]()
    def apply(c : C) = map.getOrElseUpdate(c, { id += 1; id }).toString
  }
  
  val vertexInc = new Incrementor[(Hypergraph[Vertex, Hyperedge], Vertex)]()
  val edgeInc = new Incrementor[(Hypergraph[Vertex, Hyperedge], Hyperedge)]()
  val getVertexId = Function.untupled(vertexInc.apply _)
  val getEdgeId = Function.untupled(edgeInc.apply _)
  
  val writer = new GraphMLWriter[Vertex, Hyperedge](getVertexId, getEdgeId)
      
  
  writer.addGraphData("label", "The grammar label", "?") { g => derivationMap(g)._1 }
  writer.addEdgeData("label", "The edge label", "?") { (_, e) => e.label }
  writer.addEdgeData("terminal", "If the edge is terminal", "true") { (_, e) => e.isTerminal.toString }
  
//  writer.addVertexData("x", "The x coordinate", "?") { (g, v) =>
//    derivationMap(g)._2.getGraphLayout().transform(v).getX().toString
//  }
//  writer.addVertexData("y", "The y coordinate", "?") { (g, v) =>
//    derivationMap(g).rightSide.getGraphLayout().transform(v).getY().toString
//  }
  writer.addVertexData("external", "The external node id", "-1") { (g, v) =>
    val id = derivationMap(g)._2.externalNodes.indexOf(v)
    if (id == -1) null else id.toString
  }
  writer.addVertexData("fake", "If this vertex is fake", "false") { (g, v) =>
    null
  }
  
  val graphs = derivations.map { t => t._2.graph }
  writer.saveHypergraphs(graphs, file)
}


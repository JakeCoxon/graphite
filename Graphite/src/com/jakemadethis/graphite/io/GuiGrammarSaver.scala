package com.jakemadethis.graphite.io
import edu.uci.ics.jung.graph.Hypergraph
import collection.JavaConversions._
import org.apache.commons.collections15.Transformer
import java.awt.geom.Point2D
import java.io.File
import com.jakemadethis.graphite.ui.GuiGrammar
import com.jakemadethis.graphite.graph.FakeVertex
import com.jakemadethis.graphite.graph.Hyperedge
import com.jakemadethis.graphite.graph.Vertex

class GuiGrammarSaver(file : File, grammar : GuiGrammar) {
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
      
  
  writer.addGraphData("label", "The grammar label", "?") { g => derivationMap(g).label }
  writer.addEdgeData("label", "The edge label", "") { (_, e) => e.label }
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


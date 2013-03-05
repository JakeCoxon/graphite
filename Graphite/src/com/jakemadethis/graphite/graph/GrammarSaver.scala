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
import com.jakemadethis.graphite.GraphMLWriter

class GrammarSaver(grammar : HypergraphGrammar, models: Traversable[VisualizationModel[Vertex,Hyperedge]]) {
  implicit def convertFunctionToTransformer[A,B](f : A => B) : Transformer[A,B] = new Transformer[A,B]() {
    def transform(obj : A) : B = f(obj)
  }
  
  val points = models.foldLeft(Map[Vertex, Point2D]()) { (result, model) =>
    val layout = model.getGraphLayout()
    val graph = layout.getGraph().asInstanceOf[Hypergraph[Vertex,Hyperedge]]
    
    result ++ graph.getVertices().map { v => v -> layout.transform(v) }
  }
  val derivationMap = grammar.derivations.map { g =>
    g.graph -> g
  }.toMap
  
  val modelMap = models.map { m => 
    val g = m.getGraphLayout().getGraph().asInstanceOf[Hypergraph[Vertex,Hyperedge]] 
    g -> m
  }.toMap
  
  var id = 0
  val vIdMap = collection.mutable.Map[Vertex, Int]()
  val eIdMap = collection.mutable.Map[Hyperedge, Int]()
   
  val writer = new GraphMLWriter[Vertex, Hyperedge](
    (g, v) => {
      id = vIdMap.getOrElseUpdate(v, id+1)
      id.toString
    },
    (g, e) => {
      id = eIdMap.getOrElseUpdate(e, id+1)
      id.toString
    })
      
  
  writer.addGraphData("label", "The grammar label", "?") { g =>
    derivationMap(g).label
  }
  writer.addVertexData("x", "The x coordinate", "0") { (g, v) =>
    modelMap(g).getGraphLayout().transform(v).getX().toString
  }
  writer.addVertexData("y", "The y coordinate", "0") { (g, v) =>
    modelMap(g).getGraphLayout().transform(v).getY().toString
  }
  
  
  
  val fwriter = new FileWriter("grammarout.xml")
  writer.saveHypergraphs(modelMap.keys, fwriter)
}


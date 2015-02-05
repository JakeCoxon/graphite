package com.jakemadethis.graphite.io

import java.io.File

import com.jakemadethis.graphite.graph.{Hyperedge, Vertex}
import edu.uci.ics.jung.graph.Hypergraph
import org.apache.commons.collections15.Transformer

object GraphSaver {

  def save(file : File, graph : Hypergraph[Vertex,Hyperedge]) {
    implicit def convertFunctionToTransformer[A,B](f : A => B) : Transformer[A,B] = new Transformer[A,B]() {
      def transform(obj : A) : B = f(obj)
    }


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


    writer.addEdgeData("label", "The edge label", "") { (_, e) => e.label }

    writer.saveHypergraphs(Seq(graph), file)
  }
}

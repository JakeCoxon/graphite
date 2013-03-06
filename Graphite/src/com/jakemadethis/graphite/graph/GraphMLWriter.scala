package com.jakemadethis.graphite.graph

import edu.uci.ics.jung.graph.Hypergraph
import edu.uci.ics.jung.io.GraphMLMetadata
import org.apache.commons.collections15.Transformer
import collection.JavaConversions._
import java.io.File
import com.jmcejuela.scala.xml.XMLPrettyPrinter
import scala.collection.TraversableOnce.wrapTraversableOnce
import scala.collection.TraversableOnce

class GraphMLWriter[V,E](
  vertex_ids : (Hypergraph[V,E], V) => String,
  edge_ids : (Hypergraph[V,E], E) => String
){
  
  val graph_data = collection.mutable.Map[String, GraphMLMetadata[Hypergraph[V,E]]]()
  val vertex_data = collection.mutable.Map[String, GraphMLMetadata[(Hypergraph[V,E], V)]]()
  val edge_data = collection.mutable.Map[String, GraphMLMetadata[(Hypergraph[V,E], E)]]()
  
  /** Adds data for every vertex **/
  def addVertexData(name : String, desc : String, default : String)(f : (Hypergraph[V,E], V) => String) {
    vertex_data += name -> new GraphMLMetadata(desc, default, new Transformer[(Hypergraph[V,E], V), String]() {
      def transform(c : (Hypergraph[V,E], V)) = f.tupled(c)
    })
  }
  
  /** Adds data for every edge **/
  def addEdgeData(name : String, desc : String, default : String)(f : (Hypergraph[V,E], E) => String) {
    edge_data += name -> new GraphMLMetadata(desc, default, new Transformer[(Hypergraph[V,E], E), String]() {
      def transform(c : (Hypergraph[V,E], E)) = f.tupled(c)
    })
  }
  
  /** Adds data for every graph **/
  def addGraphData(name : String, desc : String, default : String)(f : Hypergraph[V,E] => String) {
    graph_data += name -> new GraphMLMetadata(desc, default, new Transformer[Hypergraph[V,E], String]() {
      def transform(c : Hypergraph[V,E]) = f(c)
    })
  }
  
  def saveHypergraphs(graphs : TraversableOnce[Hypergraph[V,E]], file : File) {

    val graphsxml = graphs.map { graph =>
          
      <graph edgedefault="undirected">
        {data(graph_data, graph)}
        {vertexData(graph)}
        {edgeData(graph)}
      </graph>
      
    }
    
    
    val graphml = 
      <graphml xmlns="http://graphml.graphdrawing.org/xmlns/graphml"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns/graphml">
        {specs}
        {graphsxml}
      </graphml>
          
    val printer = new XMLPrettyPrinter(2)
    printer.write(graphml)(file)
  }
  
  def edgeData(g : Hypergraph[V,E]) = {
    g.getEdges().map { edge =>
      val incidents = g.getIncidentVertices(edge)
      val id = edge_ids(g, edge)
      
      val endpoints = incidents.map { v =>
        <endpoint node={vertex_ids(g, v)} />
      }
      
      <hyperedge id={id}>
        {data(edge_data, (g, edge))}
        {endpoints}
      </hyperedge>
    }
  }
  
  def vertexData(g : Hypergraph[V,E]) = {
    g.getVertices().map { vertex => 
      val id = vertex_ids(g, vertex)
      
      <node id={id}>
        {data(vertex_data, (g, vertex))}
      </node>
    }
  }
  
  def data[T](metaData : collection.mutable.Map[String, GraphMLMetadata[T]], value : T) = {
    metaData.map { case (key, data) => 
      data.transformer match {
        case null => null
        case t => t.transform(value) match {
          case null => null
          case v => <data key={key}>{v}</data>
        }
      }
    }
  }
  
  
  def specs = {
    val g = graph_data.map { case (key, value) => 
      keySpecification(key, "graph", value)
    }
    val v = vertex_data.map { case (key, value) => 
      keySpecification(key, "node", value)
    }
    val e = edge_data.map { case (key, value) => 
      keySpecification(key, "hyperedge", value)
    }
    g ++ v ++ e
  }
  
  def keySpecification[T](key : String, typpe : String, ds : GraphMLMetadata[T]) = {
    val default = ds.default_value match {
      case null => null
      case d => <default>{d}</default>
    }
    
    <key id={key} for={typpe}>{default}</key>
  }
  
}
package com.jakemadethis.graphite

import edu.uci.ics.jung.graph.Hypergraph
import edu.uci.ics.jung.io.GraphMLMetadata
import org.apache.commons.collections15.Transformer
import java.io.BufferedWriter
import java.io.Writer
import collection.JavaConversions._
import scala.xml.Node
import scala.xml.Elem

class GraphMLWriter[V,E](
  vertex_ids : (Hypergraph[V,E], V) => String,
  edge_ids : (Hypergraph[V,E], E) => String
){
  
  val graph_data = collection.mutable.Map[String, GraphMLMetadata[Hypergraph[V,E]]]()
  val vertex_data = collection.mutable.Map[String, GraphMLMetadata[(Hypergraph[V,E], V)]]()
  val edge_data = collection.mutable.Map[String, GraphMLMetadata[(Hypergraph[V,E], E)]]()
  
  
  def addVertexData(name : String, desc : String, default : String)(f : (Hypergraph[V,E], V) => String) {
    vertex_data += name -> new GraphMLMetadata(desc, default, new Transformer[(Hypergraph[V,E], V), String]() {
      def transform(c : (Hypergraph[V,E], V)) = f.tupled(c)
    })
  }
  def addEdgeData(name : String, desc : String, default : String)(f : (Hypergraph[V,E], E) => String) {
    edge_data += name -> new GraphMLMetadata(desc, default, new Transformer[(Hypergraph[V,E], E), String]() {
      def transform(c : (Hypergraph[V,E], E)) = f.tupled(c)
    })
  }
  def addGraphData(name : String, desc : String, default : String)(f : Hypergraph[V,E] => String) {
    graph_data += name -> new GraphMLMetadata(desc, default, new Transformer[Hypergraph[V,E], String]() {
      def transform(c : Hypergraph[V,E]) = f(c)
    })
  }
  
  def saveHypergraphs(graphs : TraversableOnce[Hypergraph[V,E]], w : Writer) {
    // write out data specifiers, including defaults

    val graphsxml = graphs.map { graph =>
      // write out graph-level information
      // set edge default direction
//  
//       write graph description, if any
//      val desc = graph_desc.transform(graph)
//      if (desc != null)
//        bw.write("<desc>" + desc + "</desc>\n")
//      
//       write graph data out if any
      val graphMeta = graph_data.map { case (key, data) => 
        data.transformer match {
          case null => null
          case t => <data key={key}>{t.transform(graph)}</data>
        }
      }
          
      <graph edgedefault="undirected">
        {graphMeta}
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
          
    val bw = new BufferedWriter(w)
    bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    bw.write(graphml.toString)
    bw.flush();
    
    bw.close();
  }
  
  def edgeData(g : Hypergraph[V,E]) = {
    g.getEdges().map { edge =>
      val incidents = g.getIncidentVertices(edge)
      val id = edge_ids(g, edge)
 
      val edgeMeta = edge_data.map { case (key, data) => 
        data.transformer match {
          case null => null
          case t => <data key={key}>{t.transform((g, edge))}</data>
        }
      }
      
      val endpoints = incidents.map { v =>
        <endpoint node={vertex_ids(g, v)} />
      }
      
      <hyperedge id={id}>
        {edgeMeta}
        {endpoints}
      </hyperedge>
    }
  }
  
  def vertexData(g : Hypergraph[V,E]) = {
    g.getVertices().map { vertex => 
      val id = vertex_ids(g, vertex)
      
      val vertexMeta = vertex_data.map { case (key, data) => 
        data.transformer match {
          case null => null
          case t => <data key={key}>{t.transform((g, vertex))}</data>
        }
      }
      <node id={id}>
        {vertexMeta}
      </node>
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
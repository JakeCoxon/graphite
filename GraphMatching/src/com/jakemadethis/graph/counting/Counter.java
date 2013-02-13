package com.jakemadethis.graph.counting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections15.Factory;

import com.jakemadethis.graph.Hyperedge;
import com.jakemadethis.graph.OrderedHypergraph;
import com.jakemadethis.graph.Vertex;
import com.jakemadethis.graph.counting.Counter.Function;
import com.jakemadethis.graph.matching.Production;
import com.jakemadethis.graph.matching.ProductionSet;

import edu.uci.ics.jung.graph.Hypergraph;

public class Counter {
  static Factory<Hypergraph<Vertex, Hyperedge>> factory = OrderedHypergraph.<Vertex, Hyperedge>getFactory();
  
  public static void main(String[] args) {
    new Counter();
  }
  private ProductionSet productionSetS;
  
  public Counter() {
    /*productionSetS = new ProductionSet("S");
    make1(productionSetS);
    make2(productionSetS);
    make3(productionSetS);

    System.out.println(count(productionSetS, 1));
    System.out.println(count(productionSetS, 2));
    System.out.println(count(productionSetS, 3));
    System.out.println(count(productionSetS, 4));
    System.out.println(count(productionSetS, 5));
    System.out.println(count(productionSetS, 6));
    System.out.println(count(productionSetS, 10));
    System.out.println(count(productionSetS, 100));
    System.out.println(count(productionSetS, 1000));*/
    testConvolution();
  }
  
  public int count(ProductionSet ps, int bound) {
    return countProductionSet(ps, bound, 1);
  }
  public int countProductionSet(ProductionSet ps, int x, int nonTerminals) {
    if (nonTerminals == 0) {
      return 1;
    }
    if (x <= 0 || nonTerminals <= 0) 
      return 0;
    
    int sum = 0;
    for (Production p : ps.rules) {
      int n = nonTerminals - 1 + p.nonTerminals();
        sum += countProductionSet(ps, x - p.size(), n);
      //else 
        //System.out.println("X too small: x:"+x+" size:"+p.size());
    }
    //if (sum != 0)
      //System.out.println("size: "+sum+" x: "+x+" NT: "+nonTerminals);
    return sum;
  }
  
  
  interface Function<A, R> {
    R apply(A arg);
  }
  
  private <A, R> Function<A, R> makeCacheFunc(final Function<A, R> f) {
    return new Function<A, R>() {
      final Map<A, R> cache = new TreeMap<A, R>();
      @Override public R apply(A arg) {
        if (cache.containsKey(arg)) return cache.get(arg);
        R v = f.apply(arg);
        cache.put(arg, v);
        return v;
      }
    };
  }
  
  private Function<Double, Double> ff;
  private Function<Double, Double> d1;
  private void testConvolution() {
    final Function<Double, Double> f = makeCacheFunc(new Function<Double, Double>() {
      @Override public Double apply(Double arg) {
        return ff.apply(arg-1) + d1.apply(arg);
      }
      
    });

    d1 = makeDelta(1);
    ff = makeConvolution(f, f);
    
    Function<Double, Double> fn = makeSelfConvolution(f, 2);
    
    for (int i = 0; i < 9; i++)
      System.out.println(fn.apply((double)i));
    
    
  }
  
  private Function<Double, Double> makeDelta(final int d) {
    return new Function<Double, Double>() {
      @Override public Double apply(Double n) {
        if (n == d) return 1.0;
        return 0.0;
      }
    };
  }
  
  private Function<Double, Double> makeConvolution(final Function<Double, Double> f1, final Function<Double, Double> f2) {
    return new Function<Double, Double>() {
      @Override public Double apply(Double n) {
        double sum = 0;
        for (int i = 0; i < n; i++) {
          sum += f1.apply((double)i) * f2.apply(n-i);
        }
        return sum;
      }
    };
  }
  private Function<Double, Double> makeSelfConvolution(final Function<Double, Double> f) {
    return makeConvolution(f, f);
  }
  private Function<Double, Double> makeSelfConvolution(final Function<Double, Double> f, int num) {
     if (num == 0) return makeDelta(0);
     Function<Double, Double> f1 = f;
     for (int i = 1; i < num; i++) {
       f1 = makeConvolution(f, f1);
     }
     return f1;
  }
  
  
  

  private static void make1(ProductionSet ps) {
    Hypergraph<Vertex, Hyperedge> g = factory.create();

    final Vertex v1 = new Vertex();
    g.addVertex(v1);
    final Vertex v2 = new Vertex();
    g.addVertex(v2);
    final Vertex v3 = new Vertex();
    g.addVertex(v3);
    g.addEdge(new Hyperedge("S", false), Arrays.asList(v1, v2));
    g.addEdge(new Hyperedge("S", false), Arrays.asList(v2, v3));
    
    ps.add(new Production(g, Arrays.asList(v1, v3)));
  }
  private static void make2(ProductionSet ps) {
    Hypergraph<Vertex, Hyperedge> g = factory.create();

    final Vertex v1 = new Vertex();
    g.addVertex(v1);
    final Vertex v2 = new Vertex();
    g.addVertex(v2);
    g.addEdge(new Hyperedge("s", true), Arrays.asList(v1, v2));
    ps.add(new Production(g, Arrays.asList(v1, v2)));
  }
  private static void make3(ProductionSet ps) {
    Hypergraph<Vertex, Hyperedge> g = factory.create();

    final Vertex v1 = new Vertex();
    g.addVertex(v1);
    final Vertex v2 = new Vertex();
    g.addVertex(v2);
    g.addEdge(new Hyperedge("b", true), Arrays.asList(v1, v2));
    ps.add(new Production(g, Arrays.asList(v1, v2)));
  }
}

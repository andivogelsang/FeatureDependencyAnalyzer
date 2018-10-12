/**
 * 
 */
package de.tum.in.i4.fda.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import de.tum.in.i4.fda.model.Component;
import grph.Grph;
import grph.algo.AllPaths;
import grph.in_memory.InMemoryGrph;
import grph.path.Path;

/**
 * @author vogelsang
 * 
 *         An int based representation of an object graph
 *
 */
public class IntNodeGraph<T> {

  private DefaultDirectedGraph<T, DefaultEdge> originalGraph;
  private Grph intGraph = new InMemoryGrph();
  private Map<T, Integer> nodeMapping = new HashMap<T, Integer>();
  private Map<Integer, T> revNodeMapping = new HashMap<Integer,T>();

  public IntNodeGraph(DefaultDirectedGraph<T, DefaultEdge> graph) {
    this.originalGraph = graph;
    int index = 0;
    for (T node : originalGraph.vertexSet()) {
      nodeMapping.put(node, index);
      revNodeMapping.put(index, node);
      intGraph.addVertex(index);
      index++;
    }
    for (DefaultEdge edge : originalGraph.edgeSet()) {
      T sourceNode = originalGraph.getEdgeSource(edge);
      T targetNode = originalGraph.getEdgeTarget(edge);
      intGraph.addDirectedSimpleEdge(nodeMapping.get(sourceNode), nodeMapping.get(targetNode));
    }
  }

  public List<GraphPath<Component, DefaultEdge>> getAllPaths(Component source, Component target) {
    
    final Collection<Collection<Path>> allIntPaths = AllPaths.compute(nodeMapping.get(source), intGraph, intGraph.getNumberOfVertices(), Integer.MAX_VALUE, false);
    for (Collection<Path> paths : allIntPaths) {
      
      System.out.println("TEst");
    }
    

//
//    final AllDirectedPaths<Integer, DefaultEdge> allIntPaths = new AllDirectedPaths<>(intGraph);
//    final List<GraphPath<Integer, DefaultEdge>> intPaths = allIntPaths.getAllPaths(
//        nodeMapping.get(source), nodeMapping.get(target), true, intGraph.vertexSet().size());

    return null;
  }
}
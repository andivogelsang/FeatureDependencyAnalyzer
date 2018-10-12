package de.tum.in.i4.fda.visualization;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jgrapht.ext.DOTExporter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import de.tum.in.i4.fda.FAAnalyzer;
import de.tum.in.i4.fda.model.Component;
import de.tum.in.i4.fda.model.Feature;

public class GraphvizVisualization {
  
  private String targetDirectory = "output/graph/";

  public void exportDot(DefaultDirectedGraph<Feature, DefaultEdge> graph) {
    FeatureNameProvider nameProvider = new FeatureNameProvider();
    FeatureIDProvider idProvider = new FeatureIDProvider();

    DOTExporter<Feature, DefaultEdge> exporter = new DOTExporter<Feature, DefaultEdge>(idProvider,
        nameProvider, null);
    new File(targetDirectory).mkdirs();
    try {
      exporter.export(new FileWriter(targetDirectory + "graph.dot"), graph);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void exportDot(final DefaultDirectedGraph<Component, DefaultEdge> componentgraph,
      final Feature containingFeature) {
    final ComponentNameProvider nameProvider = new ComponentNameProvider();
    final ComponentIDProvider idProvider = new ComponentIDProvider();

    final DOTExporter<Component, DefaultEdge> exporter = new DOTExporter<Component, DefaultEdge>(
        idProvider, nameProvider, null);
    new File(targetDirectory).mkdirs();
    try {
      final String filename = containingFeature.name.replaceAll("[^a-zA-Z0-9.-]", "_");
      exporter.export(new FileWriter(targetDirectory + filename + ".dot"),
          componentgraph);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public void exportDot(DefaultDirectedGraph<Component, DefaultEdge> graph, FAAnalyzer analyzer) {
    ComponentFeatureNameProvider nameProvider = new ComponentFeatureNameProvider(analyzer);
    ComponentFeatureIDProvider idProvider = new ComponentFeatureIDProvider();

    DOTExporter<Component, DefaultEdge> exporter = new DOTExporter<Component, DefaultEdge>(
        idProvider, nameProvider, null);
    new File(targetDirectory).mkdirs();
    try {
      exporter.export(new FileWriter(targetDirectory + "componentgraph.dot"),
          graph);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }

}

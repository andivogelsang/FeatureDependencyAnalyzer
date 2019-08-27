package de.tum.in.i4.fda;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.math.stat.descriptive.rank.Median;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.AllDirectedPaths;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import de.tum.in.i4.fda.model.FA;
import de.tum.in.i4.fda.model.Feature;
import de.tum.in.i4.fda.model.FeatureDependency;
import de.tum.in.i4.fda.model.FirstOrderFeatureDependency;
import de.tum.in.i4.fda.model.HighOrderFeatureDependency;
import de.tum.in.i4.fda.visualization.GraphvizVisualization;
import de.tum.in.i4.fda.graph.IntNodeGraph;
import de.tum.in.i4.fda.model.Component;

public class FAAnalyzer {

  public FA fa;

  private static final Logger LOGGER = Logger.getLogger(FAAnalyzer.class.getName());

  public FAAnalyzer(FA fa) {
    this.fa = fa;
  }

  public void calcDependencies() {

    fa.clearFeatureDependencies();
    // Two features are dependent if at least two of their logical
    // components exchange data

    for (Feature f1 : fa.features) {
      for (Feature f2 : fa.features) {
        // a feature does not have dependencies with itself
        if (!f1.equals(f2)) {
          Collection<Component> componentsF1 = fa.fcMapping.get(f1);
          Collection<Component> componentsF2 = fa.fcMapping.get(f2);
          for (Component c1 : componentsF1) {
            for (Component c2 : componentsF2) {
              // both components must not be included in the
              // other's feature
              if (!componentsF1.contains(c2) && !componentsF2.contains(c1)) {
                Collection<String> sharedChannels = getSharedChannels(c1, c2);
                if (!sharedChannels.isEmpty()) {
                  fa.addDependency(f1, f2, c1, c2, sharedChannels);
                }
              }
            }
          }
        }
      }
    }
  }

  private Collection<String> getSharedChannels(Component source, Component target) {
    Collection<String> sharedChannels = new HashSet<String>(source.outputs);
    sharedChannels.retainAll(target.inputs);
    return sharedChannels;
  }

  public int numberOfFeaturesWithOutgoingDependencies() {
    int res = 0;
    for (Feature f : fa.features) {
      Collection<Feature> dependentFeatures = getOutgoingFeatures(f);
      if (!dependentFeatures.isEmpty()) {
        res++;
      }
    }
    return res;
  }

  public int numberOfFeaturesWithIncomingDependencies() {
    int res = 0;
    for (Feature f : fa.features) {
      Collection<Feature> incomingFeatureDependencies = getIncomingFeatures(f);
      if (!incomingFeatureDependencies.isEmpty()) {
        res++;
      }
    }
    return res;
  }

  public int numberOfFeaturesWithIncomingAndOutgoingDependencies() {
    int res = 0;
    for (Feature f : fa.features) {
      Collection<Feature> incomingFeatureDependencies = getIncomingFeatures(f);
      Collection<Feature> outgoingFeatureDependencies = getOutgoingFeatures(f);
      if (!incomingFeatureDependencies.isEmpty() && !outgoingFeatureDependencies.isEmpty()) {
        res++;
      }
    }
    return res;
  }

  public String largestFanOut() {
    int max = 0;
    Feature fmax = null;
    for (Feature f : fa.features) {
      Collection<Feature> dependentFeatures = getOutgoingFeatures(f);
      if (dependentFeatures.size() > max) {
        max = dependentFeatures.size();
        fmax = f;
      }
    }
    if (fmax == null) {
      return "n/a";
    } else {
      return fmax.name + " with " + max;
    }
  }

  public String largestFanIn() {
    int max = 0;
    Feature fmax = null;
    for (Feature f : fa.features) {
      Collection<Feature> dependentFeatures = getIncomingFeatures(f);
      if (dependentFeatures.size() > max) {
        max = dependentFeatures.size();
        fmax = f;
      }
    }
    if (fmax == null) {
      return "n/a";
    } else {
      return fmax.name + " with " + max;
    }
  }

  public int medianFanIn() {
    double[] inDependencies = new double[fa.features.size()];
    Median median = new Median();
    int i = 0;
    for (Feature f : fa.features) {
      Collection<Feature> dependentFeatures = getIncomingFeatures(f);
      inDependencies[i] = dependentFeatures.size();
      i++;
    }
    return ((int) median.evaluate(inDependencies));
  }

  public int medianFanOut() {
    double[] outDependencies = new double[fa.features.size()];
    Median median = new Median();
    int i = 0;
    for (Feature f : fa.features) {
      Collection<Feature> dependentFeatures = getOutgoingFeatures(f);
      outDependencies[i] = dependentFeatures.size();
      i++;
    }
    return ((int) median.evaluate(outDependencies));
  }

  public String avgFanIn() {
    int sum = 0;
    int count = 0;
    for (Feature f : fa.features) {
      Collection<Feature> dependentFeatures = getIncomingFeatures(f);
      sum += dependentFeatures.size();
      count++;
    }
    return "" + sum / count;
  }

  public String avgFanOut() {
    int sum = 0;
    int count = 0;
    for (Feature f : fa.features) {
      Collection<Feature> dependentFeatures = getOutgoingFeatures(f);
      sum += dependentFeatures.size();
      count++;
    }
    return "" + sum / count;
  }

  public Collection<Feature> getOutgoingFeatures(Feature f) {
    Collection<Feature> dependentFeatures = new HashSet<Feature>();
    for (FeatureDependency dependency : fa.featureDependencies) {
      if (dependency.sourceFeature.equals(f)) {
        dependentFeatures.add(dependency.targetFeature);
      }
    }
    return dependentFeatures;
  }

  public Collection<Feature> getIncomingFeatures(Feature f) {
    Collection<Feature> incomingFeatures = new HashSet<Feature>();
    for (FeatureDependency dependency : fa.featureDependencies) {
      if (dependency.targetFeature.equals(f)) {
        incomingFeatures.add(dependency.sourceFeature);
      }
    }
    return incomingFeatures;
  }

  public void removeEmptyFA() {
    Collection<Component> components = new HashSet<Component>(fa.components);
    for (Component c : components) {
      if (c.inputs.isEmpty() && c.outputs.isEmpty()) {
        fa.removeComponent(c);
        LOGGER.info("Removed Component " + c.name);
      }
    }

    Collection<Feature> features = new HashSet<Feature>(fa.features);
    for (Feature f : features) {
      if (fa.fcMapping.get(f).isEmpty()) {
        fa.removeFeature(f);
        LOGGER.info("Removed Feature " + f.name);
      }
    }
  }

  public Collection<Component> getInputComponents() {
    Collection<Component> inputBlocks = new HashSet<Component>();
    for (Component c : fa.components) {
      if (c.inputs.isEmpty()) {
        inputBlocks.add(c);
      } else {
        Collection<String> outputs = fa.getAllOutputs();
        outputs.retainAll(c.inputs);
        if (outputs.isEmpty()) {
          inputBlocks.add(c);
        }
      }
    }
    return inputBlocks;
  }

  public Collection<Component> getOutputComponents() {
    Collection<Component> outputBlocks = new HashSet<Component>();
    for (Component c : fa.components) {
      if (c.outputs.isEmpty()) {
        outputBlocks.add(c);
      } else {
        Collection<String> inputs = fa.getAllInputs();
        inputs.retainAll(c.inputs);
        if (inputs.isEmpty()) {
          outputBlocks.add(c);
        }
      }
    }
    return outputBlocks;
  }

  public String numberOfComponents() {
    return String.valueOf(fa.components.size());
  }

  /**
   * returns the number of features in the FA
   */
  public String numberOfFeatures() {
    return String.valueOf(fa.features.size());
  }

  /**
   * returns all {@link FirstOrderFeatureDependency} objects with source feature f and target
   * feature g.
   */
  public Collection<FirstOrderFeatureDependency> getFirstOrderDependenciesForFeatures(Feature f,
      Feature g) {
    Collection<FirstOrderFeatureDependency> dependencies = new HashSet<FirstOrderFeatureDependency>();
    for (FeatureDependency dep : fa.featureDependencies) {
      if (dep instanceof FirstOrderFeatureDependency && dep.sourceFeature.equals(f)
          && dep.targetFeature.equals(g)) {
        dependencies.add((FirstOrderFeatureDependency) dep);
      }
    }
    return dependencies;
  }

  /**
   * returns all {@link HighOrderFeatureDependency} objects with source feature f and target feature
   * g.
   */
  public Collection<HighOrderFeatureDependency> getHighOrderDependenciesForFeatures(Feature f,
      Feature g) {
    Collection<HighOrderFeatureDependency> dependencies = new HashSet<HighOrderFeatureDependency>();
    for (FeatureDependency dep : fa.featureDependencies) {
      if (dep instanceof HighOrderFeatureDependency && dep.sourceFeature.equals(f)
          && dep.targetFeature.equals(g)) {
        dependencies.add((HighOrderFeatureDependency) dep);
      }
    }
    return dependencies;
  }

  /**
   * returns all {@link FeatureDependency} objects with source feature f.
   */
  public Collection<FeatureDependency> getOutgoingDependenciesForFeature(Feature f) {
    Collection<FeatureDependency> dependencies = new HashSet<FeatureDependency>();
    for (FeatureDependency dep : fa.featureDependencies) {
      if (dep.sourceFeature.equals(f)) {
        dependencies.add(dep);
      }
    }
    return dependencies;
  }

  /**
   * returns all {@link FeatureDependency} objects with target feature f.
   */
  public Collection<FeatureDependency> getIncomingDependenciesForFeature(Feature f) {
    Collection<FeatureDependency> dependencies = new HashSet<FeatureDependency>();
    for (FeatureDependency dep : fa.featureDependencies) {
      if (dep.targetFeature.equals(f)) {
        dependencies.add(dep);
      }
    }
    return dependencies;
  }

  /**
   * returns all {@link FeatureDependency} objects with source or target feature f.
   */
  public Collection<FeatureDependency> getAllDependenciesForFeature(Feature f) {
    Collection<FeatureDependency> dependencies = new HashSet<FeatureDependency>(
        getOutgoingDependenciesForFeature(f));
    dependencies.addAll(getIncomingDependenciesForFeature(f));
    return dependencies;
  }

  public void printComponentDependencies() {
    for (Component c : fa.components) {
      int dep = getNumberOfDependencies(c);
      if (dep > 0) {
        LOGGER.info(c.name + ": " + dep);
      }
    }

  }

  public void printDependencyReport() {
    LOGGER.info("Number of Features: " + numberOfFeatures());
    LOGGER.info("Number of Components: " + numberOfComponents());
    LOGGER.info("Overal number of Dependencies: " + getNumberOfDependencies());
    LOGGER.info("Number of features with incoming dependencies: "
        + numberOfFeaturesWithIncomingDependencies());
    LOGGER.info("Number of features with outgoing dependencies: "
        + numberOfFeaturesWithOutgoingDependencies());
    LOGGER.info("Number of features with in & out dependencies: "
        + numberOfFeaturesWithIncomingAndOutgoingDependencies());
    LOGGER
        .info("Number of features without dependencies: " + numberOfFeaturesWithoutDependencies());
    LOGGER.info("Feature with largest fan-in: " + largestFanIn());
    LOGGER.info("Feature with largest fan-out: " + largestFanOut());
    LOGGER.info("Average feature fan-in: " + avgFanIn());
    LOGGER.info("Average feature fan-out: " + avgFanOut());
    LOGGER.info("Median feature fan-in: " + medianFanIn());
    LOGGER.info("Median feature fan-out: " + medianFanOut());

  }

  public int numberOfFeaturesWithoutDependencies() {
    int res = 0;
    for (Feature f : fa.features) {
      Collection<Feature> incomingFeatureDependencies = getIncomingFeatures(f);
      Collection<Feature> outgoingFeatureDependencies = getOutgoingFeatures(f);
      if (incomingFeatureDependencies.isEmpty() && outgoingFeatureDependencies.isEmpty()) {
        res++;
      }
    }
    return res;
  }

  /**
   * extracts n components from the LA in descending order of their contribution to feature
   * dependencies.
   */
  public void extractNCommunalComponents(int n) {
    for (int i = 0; i < n; i++) {
      extractTopCommunalComponent();
      calcDependencies();
    }

  }

  /**
   * determines the component with the highest number of feature dependencies and removes it from
   * the LA.
   */
  private void extractTopCommunalComponent() {
    Component topComponent = getTopComponent();
    LOGGER.info("Top communal component '" + topComponent.name + "' with "
        + getNumberOfDependencies(topComponent) + " dependencies will be removed");
    fa.removeComponent(topComponent);
  }

  private Component getTopComponent() {
    Component topComponent = null;
    int maxDependencies = 0;
    for (Component c : fa.components) {
      int componentDependencies = getNumberOfDependencies(c);
      if (maxDependencies <= componentDependencies) {
        maxDependencies = componentDependencies;
        topComponent = c;
      }
    }
    return topComponent;
  }

  public int getNumberOfCommunalComponents() {
    int numberOfCommunalComponents = 0;
    for (Component c : fa.components) {
      int componentDependencies = getNumberOfDependencies(c);
      if (componentDependencies > 0) {
        numberOfCommunalComponents++;
      }
    }
    return numberOfCommunalComponents;
  }

  public int getNumberOfComponents() {
    return fa.components.size();
  }

  public int getNumberOfDependencies() {
    Collection<String> dependencies = new HashSet<String>();
    for (FeatureDependency dep : fa.featureDependencies) {
      dependencies.add(dep.sourceFeature + "->" + dep.targetFeature);
    }
    return dependencies.size();
  }

  public int getNumberOfDependencies(Component c) {
    Collection<String> dependencies = new HashSet<String>();
    for (final FeatureDependency dep : fa.featureDependencies) {
      if (dep instanceof FirstOrderFeatureDependency) {
        if (((FirstOrderFeatureDependency) dep).sourceComponent.equals(c)) {
          dependencies.add(dep.sourceFeature + "->" + dep.targetFeature);
        }
        if (((FirstOrderFeatureDependency) dep).targetComponent.equals(c)) {
          dependencies.add(dep.targetFeature + "->" + dep.sourceFeature);
        }
      }
    }
    return dependencies.size();

  }

  /**
   * Returns a {@link Feature} that contains the component
   */
  public Feature getRandomContainingFeature(Component component) {
    Feature containingFeature = null;

    for (Feature f : fa.features) {
      if (fa.fcMapping.get(f).contains(component)) {
        containingFeature = f;
      }
    }
    return containingFeature;
  }

  public Integer getNumberOfChannels(Component c) {
    int nrChannels = c.inputs.size() + c.outputs.size();
    return nrChannels;
  }

  /**
   * Computes higher order feature dependencies based on the feature graph
   */
  public void computeSimpleHighOrderDependencies() {
    // init with feature interaction of order 1
    calcDependencies();

    DefaultDirectedGraph<Feature, DefaultEdge> featuregraph = createFeatureGraph();

    // addDependenciesViaShortestPath(featuregraph);

    addDependenciesViaAllPaths(featuregraph);
  }

  private void addDependenciesViaAllPaths(DefaultDirectedGraph<Feature, DefaultEdge> featuregraph) {
    // based on all paths
    for (Feature f1 : featuregraph.vertexSet()) {
      for (Feature f2 : featuregraph.vertexSet()) {
        if (f1 != f2) {
          addDependenciesViaAllPathsBtwFeatures(featuregraph, f1, f2);
        }
      }
    }
  }

  private void addDependenciesViaAllPathsBtwFeatures(
      DefaultDirectedGraph<Feature, DefaultEdge> featuregraph, Feature f1, Feature f2) {
    LOGGER.info("Checking paths for : " + f1.name + " and " + f2.name);
    final AllDirectedPaths<Feature, DefaultEdge> allPaths = new AllDirectedPaths<>(featuregraph);
    int count = 0;
    for (GraphPath<Feature, DefaultEdge> path : allPaths.getAllPaths(f1, f2, true, 10)) {
      FeatureDependency fd = new HighOrderFeatureDependency(f1, f2, Graphs.getPathVertexList(path));
      fa.featureDependencies.add(fd);
      count++;
      if (count > 100) {
        LOGGER.info("exceeded");
        break;
      }
    }
  }

  private void addDependenciesViaShortestPath(
      DefaultDirectedGraph<Feature, DefaultEdge> featuregraph) {
    // based on shortest path
    for (Feature f1 : featuregraph.vertexSet()) {
      for (Feature f2 : featuregraph.vertexSet()) {
        if (f1 != f2) {
          DijkstraShortestPath<Feature, DefaultEdge> shortestPath = new DijkstraShortestPath<Feature, DefaultEdge>(
              featuregraph, f1, f2);
          if (shortestPath.getPath() != null && shortestPath.getPathEdgeList().size() > 1) {
            FeatureDependency fd = new HighOrderFeatureDependency(f1, f2,
                Graphs.getPathVertexList(shortestPath.getPath()));
            fa.featureDependencies.add(fd);
          }
        }
      }
    }
  }

  /**
   * @return a graph with all {@link Feature}s as nodes and {@link FirstOrderFeatureDependency}s as
   *         edges.
   */
  private DefaultDirectedGraph<Feature, DefaultEdge> createFeatureGraph() {
    DefaultDirectedGraph<Feature, DefaultEdge> featuregraph = new DefaultDirectedGraph<Feature, DefaultEdge>(
        DefaultEdge.class);

    for (Feature feature : fa.features) {
      featuregraph.addVertex(feature);
    }

    for (FeatureDependency fd : fa.featureDependencies) {
      if (fd instanceof FirstOrderFeatureDependency) {
        featuregraph.addEdge(((FirstOrderFeatureDependency) fd).sourceFeature, fd.targetFeature);
      }
    }

    GraphvizVisualization viz = new GraphvizVisualization();
    viz.exportDot(featuregraph);

    return featuregraph;
  }

  public void printFIOrderReport() {
    for (FeatureDependency fi : fa.featureDependencies) {
      LOGGER.info(fi.sourceFeature.name + " -> " + fi.targetFeature.name + ":" + fi.order);
    }

  }

  public void computeHighOrderDependencies() {
    calcDependencies();

    // create graph from components
    final DefaultDirectedGraph<Component, DefaultEdge> graph = createComponentGraphFromDependencies();
    final GraphvizVisualization viz = new GraphvizVisualization();
    viz.exportDot(graph, this);
    LOGGER.info("Number of nodes: " + graph.vertexSet().size());
    LOGGER.info("Number of edges: " + graph.edgeSet().size());

    for (final Feature sourceFeature : fa.features) {
      for (final Feature targetFeature : fa.features) {
        if (!sourceFeature.equals(targetFeature)) {
          // find dependencies by adding dummy feature nodes and investigating all paths between the
          // dummy nodes
          final DefaultDirectedGraph<Component, DefaultEdge> reducedGraph = createReducedFeatureNodeGraph(
              graph, sourceFeature, targetFeature);

          viz.exportDot(reducedGraph, this);
          LOGGER.info("Number of nodes: " + reducedGraph.vertexSet().size());
          LOGGER.info("Number of edges: " + reducedGraph.edgeSet().size());

          final Collection<Collection<Feature>> featureInteractions = addDependenciesViaAllPathsBtwComponents(
              reducedGraph, getDummyComponent(sourceFeature), getDummyComponent(targetFeature));

          // add dependencies
          for (final Collection<Feature> featureInteraction : featureInteractions) {
            final List<Feature> connectingFeatures = new LinkedList<Feature>();
            connectingFeatures.add(sourceFeature);
            connectingFeatures.addAll(featureInteraction);
            connectingFeatures.add(targetFeature);
            final FeatureDependency fd = new HighOrderFeatureDependency(sourceFeature,
                targetFeature, connectingFeatures);
            fa.featureDependencies.add(fd);
          }
        }

      }

    }

    // check all paths via a closest first iterator
    LOGGER.info("Check here");

  }

  private Collection<Collection<Feature>> addDependenciesViaAllPathsBtwComponents(
      final DefaultDirectedGraph<Component, DefaultEdge> graph, final Component source,
      final Component target) {

    final AllDirectedPaths<Component, DefaultEdge> allPaths = new AllDirectedPaths<>(graph);
    final List<GraphPath<Component, DefaultEdge>> paths = new IntNodeGraph<Component>(graph)
        .getAllPaths(source, target);

    final Collection<Collection<Feature>> featureInteractions = new HashSet<Collection<Feature>>();

    LOGGER.info(
        "Number of paths between " + source.name + " and " + target.name + ": " + paths.size());

    for (final GraphPath<Component, DefaultEdge> path : paths) {
      final Collection<Feature> visitedFeatures = new HashSet<Feature>();
      for (final Component visitedComponent : Graphs.getPathVertexList(path)) {
        visitedFeatures.addAll(getAllContainingFeatures(visitedComponent));
      }
      if (!visitedFeatures.isEmpty() && !containedIn(featureInteractions, visitedFeatures)) {
        featureInteractions.add(visitedFeatures);
      }
    }
    return featureInteractions;

  }

  private boolean containedIn(final Collection<Collection<Feature>> featureInteractions,
      final Collection<Feature> visitedFeatures) {
    boolean contained = false;
    for (final Collection<Feature> featureInteraction : featureInteractions) {
      if (featureInteraction.equals(visitedFeatures)) {
        contained = true;
        break;
      }
    }

    return contained;
  }

  private DefaultDirectedGraph<Component, DefaultEdge> createReducedFeatureNodeGraph(
      final DefaultDirectedGraph<Component, DefaultEdge> graph, final Feature sourceFeature,
      final Feature targetFeature) {

    @SuppressWarnings("unchecked")
    final DefaultDirectedGraph<Component, DefaultEdge> reducedGraph = (DefaultDirectedGraph<Component, DefaultEdge>) graph
        .clone();

    final Component sourceFeatureComponent = getDummyComponent(sourceFeature);
    final Component targetFeatureComponent = getDummyComponent(targetFeature);
    reducedGraph.addVertex(sourceFeatureComponent);
    reducedGraph.addVertex(targetFeatureComponent);

    for (final Component component : graph.vertexSet()) {
      final Collection<Feature> containingFeatures = getAllContainingFeatures(component);
      if (containingFeatures.contains(sourceFeature)) {
        // reroute all outgoing edges of component to the source dummy feature node
        for (final DefaultEdge edge : graph.outgoingEdgesOf(component)) {
          if (reducedGraph.containsVertex(graph.getEdgeTarget(edge))) {
            reducedGraph.addEdge(sourceFeatureComponent, graph.getEdgeTarget(edge));
          }
        }
        reducedGraph.removeVertex(component);
      }
      if (containingFeatures.contains(targetFeature)) {
        // reroute all incoming edges of component to the target dummy feature node
        for (final DefaultEdge edge : graph.incomingEdgesOf(component)) {
          if (reducedGraph.containsVertex(graph.getEdgeSource(edge))) {
            reducedGraph.addEdge(graph.getEdgeSource(edge), targetFeatureComponent);
          }
        }
        reducedGraph.removeVertex(component);
      }
    }

    return reducedGraph;

  }

  /*
   * private void addHigherOrderDependenciesFromComponentGraph( DefaultDirectedGraph<Component,
   * DefaultEdge> graph) {
   * 
   * AllDirectedPaths<Component, DefaultEdge> allPaths = new AllDirectedPaths<>(graph);
   * Collection<Feature> visitedFeatures = new HashSet<Feature>(); for (final Component
   * sourceComponent : graph.vertexSet()) { for (final Component targetComponent :
   * graph.vertexSet()) { final Collection<Feature> commonFeatures =
   * getAllContainingFeatures(sourceComponent);
   * commonFeatures.retainAll(getAllContainingFeatures(targetComponent)); if
   * (commonFeatures.isEmpty()) { for (GraphPath<Component, DefaultEdge> path :
   * allPaths.getAllPaths(sourceComponent, targetComponent, true, null)) { visitedFeatures.clear();
   * for (Component visitedComponent : Graphs.getPathVertexList(path)) { if (!
   * getAllContainingFeatures(visitedComponent).contains(o)) } }
   * 
   * }
   * 
   * }
   * 
   * }
   * 
   * }
   */

  private Component getDummyComponent(final Feature feature) {
    return new Component(feature.name);
  }

  /**
   * @return a graph with {@link Component}s as nodes derived from analyzing
   *         {@link FirstOrderFeatureDependency}s.
   */
  private DefaultDirectedGraph<Component, DefaultEdge> createComponentGraphFromDependencies() {
    DefaultDirectedGraph<Component, DefaultEdge> graph = new DefaultDirectedGraph<Component, DefaultEdge>(
        DefaultEdge.class);

    // for each feature add dependency contributing components and connect them
    for (Feature feature : fa.features) {
      addDependencyContributingComponents(graph, feature);
    }

    GraphvizVisualization viz = new GraphvizVisualization();
    viz.exportDot(graph, this);

    // add all components from first order FIs
    addFirstOrderFDs(graph);

    viz.exportDot(graph, this);

    /*
     * // add edges between components of one feature for (Component dependencyTarget :
     * graph.vertexSet()) { for (Component dependencySource : graph.vertexSet()) { Feature
     * containingfeature = getRandomContainingFeature(dependencySource); if
     * (!dependencySource.equals(dependencyTarget) &&
     * containingfeature.equals(getRandomContainingFeature(dependencyTarget))) { List<DefaultEdge>
     * path = getPathWithinFeature(dependencyTarget, dependencySource, containingfeature); if (path
     * != null) { graph.addEdge(dependencyTarget, dependencySource); } } }
     * 
     * }
     */

    return graph;
  }

  private void addFirstOrderFDs(DefaultDirectedGraph<Component, DefaultEdge> graph) {
    for (FeatureDependency fd : fa.featureDependencies) {
      if (fd instanceof FirstOrderFeatureDependency) {
        graph.addVertex(((FirstOrderFeatureDependency) fd).sourceComponent);
        graph.addVertex(((FirstOrderFeatureDependency) fd).targetComponent);
        graph.addEdge(((FirstOrderFeatureDependency) fd).sourceComponent,
            ((FirstOrderFeatureDependency) fd).targetComponent);
      }
    }
  }

  private void addDependencyContributingComponents(
      DefaultDirectedGraph<Component, DefaultEdge> graph, Feature feature) {
    Collection<FeatureDependency> incomingDep = getIncomingDependenciesForFeature(feature);
    Collection<FeatureDependency> outgoingDep = getOutgoingDependenciesForFeature(feature);

    Collection<Component> targetComponents = getTargetComponentsFromFDs(incomingDep);
    Collection<Component> sourceComponents = getSourceComponentsFromFDs(outgoingDep);

    // add vertices
    for (Component component : targetComponents) {
      graph.addVertex(component);
    }
    for (Component component : sourceComponents) {
      graph.addVertex(component);
    }

    // add edges
    for (Component targetComponent : targetComponents) {
      for (Component sourceComponent : sourceComponents) {
        if (targetComponent != sourceComponent) {
          List<DefaultEdge> path = getPathWithinFeature(targetComponent, sourceComponent, feature);
          if (path != null) {
            graph.addEdge(targetComponent, sourceComponent);
          }
        }
      }
    }
  }

  private Collection<Component> getSourceComponentsFromFDs(
      Collection<FeatureDependency> outgoingDep) {
    Collection<Component> components = new HashSet<Component>();

    for (FeatureDependency dep : outgoingDep) {
      if (dep instanceof FirstOrderFeatureDependency) {
        components.add(((FirstOrderFeatureDependency) dep).sourceComponent);
      }
    }
    return components;
  }

  private Collection<Component> getTargetComponentsFromFDs(
      Collection<FeatureDependency> incomingDep) {
    Collection<Component> components = new HashSet<Component>();

    for (FeatureDependency dep : incomingDep) {
      if (dep instanceof FirstOrderFeatureDependency) {
        components.add(((FirstOrderFeatureDependency) dep).targetComponent);
      }
    }
    return components;
  }

  private List<DefaultEdge> getPathWithinFeature(Component sourceComponent,
      Component targetComponent, Feature containingfeature) {

    DefaultDirectedGraph<Component, DefaultEdge> componentgraph = createComponentGraphForFeature(
        containingfeature);
    List<DefaultEdge> path = DijkstraShortestPath.findPathBetween(componentgraph, sourceComponent,
        targetComponent);

    return path;
  }

  private DefaultDirectedGraph<Component, DefaultEdge> createComponentGraphForFeature(
      Feature feature) {
    DefaultDirectedGraph<Component, DefaultEdge> componentgraph = new DefaultDirectedGraph<>(
        DefaultEdge.class);

    Collection<Component> components = fa.fcMapping.get(feature);

    for (Component component : components) {
      componentgraph.addVertex(component);
    }

    for (Component sourceComponent : components) {
      for (Component targetComponent : components) {
        if (!getSharedChannels(sourceComponent, targetComponent).isEmpty()) {
          componentgraph.addEdge(sourceComponent, targetComponent);
        }
      }
    }

    GraphvizVisualization viz = new GraphvizVisualization();
    viz.exportDot(componentgraph, feature);

    return componentgraph;
  }

  /**
   * Returns all {@link Feature}s that contain the component.
   */
  public Collection<Feature> getAllContainingFeatures(Component component) {
    Collection<Feature> containingFeatures = new HashSet<Feature>();

    for (Feature f : fa.features) {
      if (fa.fcMapping.get(f).contains(component)) {
        containingFeatures.add(f);
      }
    }
    return containingFeatures;
  }

  public void computePositionInFeature() {
    for (Feature f : fa.features) {
      LOGGER.info("Component distribution for feature: " + f.name);
      calcComponentPositions(f);
    }

  }

  public void printPositionData() {
    for (Feature f : fa.features) {
      LOGGER.info("Component distribution for feature: " + f.name);
      for (Component c : f.getComponents()) {
        LOGGER.info("Component: " + c.name + " ; Position: " + c.getInputPositionInFeature(f));
      }
    }

  }

  public void calcComponentPositions(Feature f) {
    Collection<Component> components = fa.fcMapping.get(f);
    for (Component c : components) {
      calcLengthToInputs(f, c);
      calcLengthToOutputs(f, c);
    }
  }

  private void calcLengthToOutputs(Feature f, Component c) {
    // the longest of the shortest paths to a feature output
    int max = 0;
    for (String out : f.getOutputs()) {
      visited.clear();
      int path = shortestPathToOutput(c, out, f);
      if (path != Integer.MAX_VALUE) { // if there is actually a path to the current output
        max = max < path ? path : max;
      }
    }
    c.setOutputPositionInFeature(f, max);
  }

  private int shortestPathToOutput(Component c, String out, Feature f) {
    int length = Integer.MAX_VALUE;
    visited.add(c);
    if (c.outputs.contains(out)) {
      length = 1;
    } else {
      if (c.outputs.isEmpty()) {
        length = Integer.MAX_VALUE;
      } else {
        for (Component post : f.getComponents()) {
          if (!getSharedChannels(c, post).isEmpty() && !visited.contains(post)) {
            // Set of unvisited successor components
            int pathLength;
            int postPathLength = shortestPathToOutput(post, out, f);
            if (postPathLength == Integer.MAX_VALUE) {
              pathLength = Integer.MAX_VALUE; // do not increase by one if already MAX
            } else {
              pathLength = postPathLength + 1;
            }
            length = pathLength < length ? pathLength : length;
          }
        }
      }
    }
    return length;
  }

  private void calcLengthToInputs(Feature f, Component c) {
    // the longest of the shortest paths to a feature input
    int max = 0;
    for (String in : f.getInputs()) {
      visited.clear();
      int path = shortestPathToInput(c, in, f);
      if (path != Integer.MAX_VALUE) { // if there is actually a path to the current input
        max = max < path ? path : max;
      }
    }
    c.setInputPositionInFeature(f, max);
  }

  private Collection<Component> visited = new HashSet<Component>();

  private int shortestPathToInput(Component c, String in, Feature f) {
    int length = Integer.MAX_VALUE;
    visited.add(c);
    if (c.inputs.contains(in)) {
      length = 1;
    } else {
      if (c.inputs.isEmpty()) {
        length = Integer.MAX_VALUE;
      } else {
        for (Component pre : fa.fcMapping.get(f)) {
          if (!getSharedChannels(pre, c).isEmpty() && !visited.contains(pre)) {
            // Set of unvisited predecessor components
            int pathLength;
            int prePathLength = shortestPathToInput(pre, in, f);
            if (prePathLength == Integer.MAX_VALUE) {
              pathLength = Integer.MAX_VALUE; // do not increase by one if already MAX
            } else {
              pathLength = prePathLength + 1;
            }
            length = pathLength < length ? pathLength : length;
          }
        }
      }
    }
    return length;
  }

}

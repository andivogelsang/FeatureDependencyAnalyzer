package de.tum.in.i4.fda.visualization;

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jgrapht.ext.VertexNameProvider;

import de.tum.in.i4.fda.FAAnalyzer;
import de.tum.in.i4.fda.model.Component;
import de.tum.in.i4.fda.model.Feature;

public class ComponentFeatureNameProvider implements VertexNameProvider<Component> {

  private FAAnalyzer analyzer;

  public ComponentFeatureNameProvider(FAAnalyzer analyzer) {
    this.analyzer = analyzer;
  }

  @Override
  public String getVertexName(Component component) {
    Collection<Feature> containingFeatures = analyzer.getAllContainingFeatures(component);
    return component.name + " ("
        + containingFeatures.stream().map(f -> f.name).collect(Collectors.joining(",")) + ")";
  }

}

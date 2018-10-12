package de.tum.in.i4.fda.model;

import java.util.List;

public class HighOrderFeatureDependency extends FeatureDependency {
  public List<Feature> connectingFeatures;

  public HighOrderFeatureDependency(final Feature sourceFeature, final Feature targetFeature,
      final int order) {
    super(sourceFeature, targetFeature, order);
  }

  public HighOrderFeatureDependency(final Feature sourceFeature, final Feature targetFeature,
      final List<Feature> connectingFeatures) {
    super(sourceFeature, targetFeature, connectingFeatures.size() - 1);
    this.connectingFeatures = connectingFeatures;
  }

}

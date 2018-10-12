package de.tum.in.i4.fda.model;

public abstract class FeatureDependency {
	public Feature sourceFeature;
	public Feature targetFeature;
	public int order;
	
	public FeatureDependency(Feature sourceFeature, Feature targetFeature, int order) {
		this.sourceFeature = sourceFeature;
		this.targetFeature = targetFeature;
		this.order=order;
	}
}

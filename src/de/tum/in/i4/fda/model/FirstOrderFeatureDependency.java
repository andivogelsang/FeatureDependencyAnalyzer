package de.tum.in.i4.fda.model;

import java.util.Collection;

public class FirstOrderFeatureDependency extends FeatureDependency{
	public Component sourceComponent;
	public Component targetComponent;
	public Collection<String> sharedChannels;

	public FirstOrderFeatureDependency(Feature sourceFeature, Feature targetFeature, Component sourceComponent,
			Component targetComponent, Collection<String> sharedChannels) {
		super(sourceFeature,targetFeature,1);
		this.sourceComponent = sourceComponent;
		this.targetComponent = targetComponent;
		this.sharedChannels = sharedChannels;
	}

}

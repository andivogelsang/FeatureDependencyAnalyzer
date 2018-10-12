package de.tum.in.i4.fda.visualization;

import org.jgrapht.ext.VertexNameProvider;

import de.tum.in.i4.fda.model.Feature;

public class FeatureIDProvider implements VertexNameProvider<Feature> {

	@Override
	public String getVertexName(Feature f) {
		return "\""+ f.name+ "\"";
	}

}

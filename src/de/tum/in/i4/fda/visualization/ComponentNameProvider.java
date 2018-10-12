package de.tum.in.i4.fda.visualization;

import org.jgrapht.ext.VertexNameProvider;

import de.tum.in.i4.fda.model.Component;

public class ComponentNameProvider implements VertexNameProvider<Component> {

	@Override
	public String getVertexName(Component component) {
		return component.name;
	}

}

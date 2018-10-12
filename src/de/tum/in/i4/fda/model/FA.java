package de.tum.in.i4.fda.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class FA {

	public Collection<Feature> features;
	public Collection<Component> components;
	public Map<Feature, Collection<Component>> fcMapping;
	public Collection<FeatureDependency> featureDependencies;

	private Feature lastAddedFeature;
	private Component lastAddedComponent;

	public FA() {
		features = new HashSet<Feature>();
		components = new HashSet<Component>();
		fcMapping = new HashMap<Feature, Collection<Component>>();
		featureDependencies = new HashSet<FeatureDependency>();
	}

	public void addFeature(String name) {
		Feature feature = new AtomicFeature(name);
		feature.assignFA(this);
		features.add(feature);
		fcMapping.put(feature, new HashSet<Component>());
		lastAddedFeature = feature;
	}

	public List<String> getFeatureNames() {
		List<String> featurenames = new ArrayList<String>();
		for (Feature f : features) {
			featurenames.add(f.name);
		}
		return featurenames;
	}

	/**
	 * adds component to LA and establishes a mapping from last added feature to
	 * component
	 */
	public void addComponent(String name) {
		Component component = getComponentByName(name);
		if (component == null) {
			component = new Component(name);
			components.add(component);
		}
		fcMapping.get(lastAddedFeature).add(component);
		this.lastAddedComponent = component;
	}

	/**
	 * adds component to LA and establishes a mapping from specified feature to
	 * component
	 */
	public void addComponent(String name, String featurename) {
		Component component = getComponentByName(name);
		if (component == null) {
			component = new Component(name);
			components.add(component);
		}
		Feature feature = getFeatureByName(featurename);
		if (feature != null) {
			fcMapping.get(feature).add(component);
		} else {
			System.out.println("Feature " + featurename + " ignored.");
		}
	}

	public Feature getFeatureByName(String featurename) {
		Feature feature = null;
		for (Feature f : features) {
			if (featurename.equals(f.name))
				feature = f;
		}
		return feature;
	}

	/**
	 * Prints features and related components
	 */
	public String print() {
		String res = "Printing the FA: \n";
		for (Feature f : features) {
			res += f.print();
			for (Component c : fcMapping.get(f)) {
				res += c.print();
			}
		}
		return res;
	}

	/**
	 * Adds an input port to the last added component
	 */
	public void addIn(String inputName) {
		lastAddedComponent.addIn(inputName);
	}

	/**
	 * Adds an output port to the last added component
	 */
	public void addOut(String outputName) {
		lastAddedComponent.addOut(outputName);
	}

	/**
	 * Adds an input port to the specified component
	 */
	public void addIn(String name, String component) {
		for (Component c : components) {
			if (component.equals(c.name))
				c.addIn(name);
		}
	}

	/**
	 * Adds an output port to the specified component
	 */
	public void addOut(String name, String component) {
		for (Component c : components) {
			if (component.equals(c.name))
				c.addOut(name);
		}
	}

	public void removeComponent(Component c) {
		// remove component
		components.remove(c);

		// remove component mappings
		for (Collection<Component> mappedComponents : fcMapping.values()) {
			if (mappedComponents.contains(c)) {
				mappedComponents.remove(c);
			}
		}
	}

	/**
	 * Removes the feature from the FA
	 */
	public void removeFeature(Feature f) {
		// remove feature
		features.remove(f);

		// remove feature mappings
		fcMapping.remove(f);

	}

	/**
	 * Returns all outputs of any component in the FA
	 */
	public Collection<String> getAllOutputs() {
		Collection<String> outputs = new HashSet<String>();
		for (Component c : components) {
			outputs.addAll(c.outputs);
		}
		return outputs;
	}

	/**
	 * Returns all inputs of any component in the FA
	 */
	public Collection<String> getAllInputs() {
		Collection<String> inputs = new HashSet<String>();
		for (Component c : components) {
			inputs.addAll(c.inputs);
		}
		return inputs;
	}

	public Component getComponentByName(String cName) {
		Component component = null;
		for (Component c : components) {
			if (cName.equals(c.name))
				component = c;
		}
		return component;
	}

	public void addDependency(Feature sourceFeature, Feature targetFeature, Component sourceComponent,
			Component targetComponent, Collection<String> sharedChannels) {
		FeatureDependency dep = new FirstOrderFeatureDependency(sourceFeature, targetFeature, sourceComponent, targetComponent,
				sharedChannels);
		featureDependencies.add(dep);
	}

	public void clearFeatureDependencies() {
		featureDependencies.clear();
	}

	public void addFQN(String featurename, String fqn) {
		for (Feature f : features){
			if (f.name.equals(featurename)){
				f.addFQN(fqn);
			}
		}
	}


}

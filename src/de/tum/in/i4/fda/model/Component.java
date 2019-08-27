package de.tum.in.i4.fda.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Component {
  public String name;
  public Collection<String> inputs;
  public Collection<String> outputs;
  private Map<Feature, Integer> featureInputPosition;
  private Map<Feature, Integer> featureOutputPosition;

  public Component(String name) {
    this.name = name;
    this.inputs = new HashSet<String>();
    this.outputs = new HashSet<String>();
    this.featureInputPosition = new HashMap<Feature, Integer>();
    this.featureOutputPosition = new HashMap<Feature, Integer>();
  }

  public String print() {
    String res = "Component " + name + "(in: " + inputs + "; out: " + outputs + ")\n";
    return res;
  }

  public void addIn(String inputName) {
    inputs.add(inputName);
  }

  public void addOut(String outputName) {
    outputs.add(outputName);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof Component))
      return false;
    Component other = (Component) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  public int getInputPositionInFeature(Feature feature) {
    if (featureInputPosition.get(feature) == null) {
      setInputPositionInFeature(feature, 0);
    }
    return featureInputPosition.get(feature);
  }

  public int getOutputPositionInFeature(Feature feature) {
    if (featureOutputPosition.get(feature) == null) {
      setOutputPositionInFeature(feature, 0);
    }
    return featureOutputPosition.get(feature);
  }

  public void setInputPositionInFeature(Feature feature, int position) {
    featureInputPosition.put(feature, position);
  }

  public void setOutputPositionInFeature(Feature feature, int position) {
    featureOutputPosition.put(feature, position);
  }

}

package de.tum.in.i4.fda.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Feature */
public abstract class Feature {

  private FA fa;

  public String name;
  public String fqn;
  private Collection<String> inputs;
  private Collection<String> outputs;
  private int eventChainLength = 0;

  public Feature(String name) {
    this.name = name;
  }

  public String print() {
    String res = "Feature " + name + "(in:" + getInputs() + "; out:" + getOutputs() + ")\n";
    return res;
  }

  public int getEventChainLength() {
    if (eventChainLength == 0) {
      int max = 0;
      for (Component c : fa.fcMapping.get(this)) {
        max = c.getInputPositionInFeature(this) > max ? c.getInputPositionInFeature(this) : max;
      }
    }
    return eventChainLength;

  }



  public Collection<String> getInputs() {
    inputs = new HashSet<String>();
    Collection<String> fin = getAllInputs();
    Collection<String> fout = getAllOutputs();
    fin.removeAll(fout);
    inputs.addAll(fin);
    return inputs;
  }

  public Collection<String> getOutputs() {
    outputs = new HashSet<String>();
    Collection<String> fout = getAllOutputs();
    Collection<String> fin = getAllInputs();
    fout.removeAll(fin);
    outputs.addAll(fout);
    return outputs;
  }

  public Collection<String> getAllInputs() {
    Collection<String> fin = new HashSet<String>();
    Collection<Component> relatedComponents = fa.fcMapping.get(this);
    for (Component c : relatedComponents) {
      fin.addAll(c.inputs);
    }
    return fin;
  }

  public Set<String> getAllOutputs() {
    Set<String> fout = new HashSet<String>();
    Collection<Component> relatedComponents = fa.fcMapping.get(this);
    for (Component c : relatedComponents) {
      fout.addAll(c.outputs);
    }
    return fout;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Feature)) {
      return false;
    }
    Feature other = (Feature) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  public void assignFA(FA fa) {
    this.fa = fa;
  }

  public void addFQN(String fqn) {
    this.fqn = fqn;
  }

  public Collection<Component> getComponents() {
    return fa.fcMapping.get(this);
  }
}

/** "Composite" */
class CompositeFeature extends Feature {

  public CompositeFeature(String name) {
    super(name);
  }

  // Collection of child graphics.
  private Set<Feature> subFunctions = new HashSet<Feature>();

  // Adds the graphic to the composition.
  public void add(Feature feature) {
    subFunctions.add(feature);
  }

  // Removes the graphic from the composition.
  public void remove(Feature feature) {
    subFunctions.remove(feature);
  }
}

/** "Leaf" */
class AtomicFeature extends Feature {
  Component addedLast;

  public AtomicFeature(String name) {
    super(name);
  }
}

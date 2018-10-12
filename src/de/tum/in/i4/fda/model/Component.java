package de.tum.in.i4.fda.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Component {
	public String name;
	public Collection<String> inputs;
	public Collection<String> outputs;

	public Component(String name) {
		this.name = name;
		this.inputs = new HashSet<String>();
		this.outputs = new HashSet<String>();
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

	public Set<String> dependsOn(Component y) {
		Set<String> in = new HashSet<String>(y.inputs);
		Set<String> out = new HashSet<String>(outputs);
		in.retainAll(out);
		return in;
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

}

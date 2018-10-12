package de.tum.in.i4.fda.visualization;

public class MyEdge {
	private String sourceFunction; 
	/**
	 * @return the sourceFunction
	 */
	public String getSourceFunction() {
		return sourceFunction;
	}

	/**
	 * @param sourceFunction the sourceFunction to set
	 */
	public void setSourceFunction(String sourceFunction) {
		this.sourceFunction = sourceFunction;
	}

	/**
	 * @return the targetFunction
	 */
	public String getTargetFunction() {
		return targetFunction;
	}

	/**
	 * @param targetFunction the targetFunction to set
	 */
	public void setTargetFunction(String targetFunction) {
		this.targetFunction = targetFunction;
	}

	private String targetFunction; 
	private String label;
	private String source;
	private String dest;

	public MyEdge(String label, String source, String dest) {
		super();
		this.label = label;
		this.source = source;
		this.dest = dest;
	}
	


	public MyEdge(String label, String source, String dest, String sourceFunction, String targetFunction) {
		super();
		this.sourceFunction = sourceFunction;
		this.targetFunction = targetFunction;
		this.label = label;
		this.source = source;
		this.dest = dest;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the dest
	 */
	public String getDest() {
		return dest;
	}

	/**
	 * @param dest the dest to set
	 */
	public void setDest(String dest) {
		this.dest = dest;
	}

	public MyEdge(String label) {
		super();
		this.label = label;
	}

	public String toString() {
		return label;
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
		result = prime * result + ((dest == null) ? 0 : dest.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
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
		if (!(obj instanceof MyEdge)) {
			return false;
		}
		MyEdge other = (MyEdge) obj;
		if (dest == null) {
			if (other.dest != null) {
				return false;
			}
		} else if (!dest.equals(other.dest)) {
			return false;
		}
		if (label == null) {
			if (other.label != null) {
				return false;
			}
		} else if (!label.equals(other.label)) {
			return false;
		}
		if (source == null) {
			if (other.source != null) {
				return false;
			}
		} else if (!source.equals(other.source)) {
			return false;
		}
		return true;
	}

}

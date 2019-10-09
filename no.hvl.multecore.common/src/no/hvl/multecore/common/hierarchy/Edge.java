package no.hvl.multecore.common.hierarchy;

public class Edge extends AbstractEdge {

	private boolean isContainment;
	
	
	public Edge(String name, IEdge type, INode sourceNode, INode targetNode, IModel model) {
		this.name = name;
		this.type = type;
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		this.isContainment = false;
		this.model = model;
		this.potency = new Potency();
	}
	
	public Edge(String name, IEdge type, INode sourceNode, INode targetNode, IModel model, Potency potency) {
		this.name = name;
		this.type = type;
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		this.isContainment = false;
		this.model = model;
		this.potency = potency;
	}
	
	
	public Edge(IEdge edge) {
		this.name = edge.getName();
		this.type = edge.getType();
		this.sourceNode = edge.getSource();
		this.targetNode = getTarget();
		this.isContainment = edge.isContainment();
		this.lowerBound = edge.getLowerBound();
		this.upperBound = edge.getUpperBound();
		this.model = edge.getModel();
		this.potency = edge.getPotency();
	}
	

	public void setName(String name) {
		this.name = name;
	}


	public void setType(IEdge type) {
		this.type = type;
	}

	
	public void setPotency(Potency potency) {
		this.potency = potency;
	}


	public void setSource(INode sourceNode) {
		this.sourceNode = sourceNode;
	}


	public void setTarget(INode targetNode) {
		this.targetNode = targetNode;
	}


	public void setLowerBound(int lowerBound) {
		this.lowerBound = lowerBound;
	}


	public void setUpperBound(int upperBound) {
		this.upperBound = upperBound;
	}


	@Override
	public boolean isContainment() {
		return isContainment;
	}
	
	
	public void setContainment(boolean containment) {
		this.isContainment = containment;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AbstractEdge))
			return false;
		AbstractEdge other = (AbstractEdge) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (sourceNode == null) {
			if (other.sourceNode != null)
				return false;
		} else if (!sourceNode.equals(other.sourceNode))
			return false;
		if (targetNode == null) {
			if (other.targetNode != null)
				return false;
		} else if (!targetNode.equals(other.targetNode))
			return false;
		return true;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result	+ ((sourceNode == null) ? 0 : sourceNode.hashCode());
		result = prime * result	+ ((targetNode == null) ? 0 : targetNode.hashCode());
		return result;
	}
	
}

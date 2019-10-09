package no.hvl.multecore.common.hierarchy;

import no.hvl.multecore.common.Constants;

public class EReferenceEdge extends AbstractEdge {
	
	public EReferenceEdge(EClassNode eClassNode, EcoreModel ecoreModel) {
		this.name = Constants.EREFERENCE_ID;
		this.type = this;
		this.sourceNode = eClassNode;
		this.targetNode = eClassNode;
		this.lowerBound = 0;
		this.upperBound = Constants.UNBOUNDED;
		this.model = ecoreModel;
		this.potency = new Potency(0, Constants.UNBOUNDED, Constants.UNBOUNDED);
	}
	
	
	@Override
	public boolean isContainment() {
		return false;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result	+ ((sourceNode == null) ? 0 : sourceNode.hashCode());
		result = prime * result	+ ((targetNode == null) ? 0 : targetNode.hashCode());
		return result;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof EReferenceEdge)
			return true;
		return false;
	}
	
}

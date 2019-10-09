package no.hvl.multecore.common.hierarchy;

public abstract class AbstractAttribute implements IAttribute {

	protected String nameOrValue;
	protected IAttribute type;
	protected Potency potency;
	protected INode containingNode;
	
	
	@Override
	public String getNameOrValue() {
		return nameOrValue;
	}

	
	@Override
	public IAttribute getType() {
		return type;
	}
	
	
	@Override
	public INode getContainingNode() {
		return containingNode;
	}
	
	
	@Override
	public IModel getModel() {
		return containingNode.getModel();
	}
	

	@Override
	public Potency getPotency() {
		return potency;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractAttribute other = (AbstractAttribute) obj;
		if (containingNode == null) {
			if (other.containingNode != null)
				return false;
		} else if (!containingNode.equals(other.containingNode))
			return false;
		if (nameOrValue == null) {
			if (other.nameOrValue != null)
				return false;
		} else if (!nameOrValue.equals(other.nameOrValue))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((containingNode == null) ? 0 : containingNode.hashCode());
		result = prime * result + ((nameOrValue == null) ? 0 : nameOrValue.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
	
}

package no.hvl.multecore.common.hierarchy;

public class InstantiatedAttribute extends AbstractAttribute implements Comparable<InstantiatedAttribute> {
	
	public InstantiatedAttribute(String value, DeclaredAttribute type, INode containingNode) {
		this.nameOrValue = value;
		this.type = type;
		this.containingNode = containingNode;
		this.potency = new Potency(0,0,0);
	}
	

	@Override
	public int compareTo(InstantiatedAttribute otherAttribute) {
		return type.getNameOrValue().compareTo(otherAttribute.getType().getNameOrValue());
	}
	
}

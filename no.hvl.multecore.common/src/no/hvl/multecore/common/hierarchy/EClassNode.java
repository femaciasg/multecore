package no.hvl.multecore.common.hierarchy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import no.hvl.multecore.common.Constants;


public class EClassNode extends AbstractNode {
	
	public EClassNode(EcoreModel ecoreModel) {
		this.name = Constants.ECLASS_ID;
		this.type = this;
		this.declaredAttributes = new HashMap<String, DeclaredAttribute>();
		this.model = ecoreModel;
		this.potency = new Potency(0, Constants.UNBOUNDED, Constants.UNBOUNDED);
		parentNodes = new HashSet<INode>();
	}


	@Override
	public Set<INode> getTransitiveTypes() {
		Set<INode> types = new HashSet<INode>();
		types.add(type);
		return types;
	}
	
	
	@Override
	public Set<INode> getTransitiveTypesPlusInherited() {
		return getTransitiveTypes();
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof EClassNode)
			return true;
		return false;
	}

	
	@Override
	public int hashCode() {
		return 31 * 1 + ((name == null) ? 0 : name.hashCode());
	}


	@Override
	public Set<IAttribute> getAttributes(boolean includePotencyAccessible) {
		return new HashSet<IAttribute>(getDeclaredAttributes(includePotencyAccessible));
	}
	
	
	@Override
	public Set<IAttribute> getAttributesPlusInherited(boolean includePotencyAccessible) {
		return getAttributes(includePotencyAccessible);
	}


	@Override
	public Set<InstantiatedAttribute> getInstantiatedAttributes() {
		return new HashSet<InstantiatedAttribute>();
	}
	
	
	@Override
	public Set<InstantiatedAttribute> getInstantiatedAttributesPlusInherited() {
		return getInstantiatedAttributes();
	}

	
	@Override
	public InstantiatedAttribute getInstantiatedAttribute(String attributeName) {
		return null;
	}
	
	
	@Override
	public InstantiatedAttribute getInstantiatedAttributePlusInherited(String attributeName) {
		return null;
	}


	@Override
	public Set<INode> getSupplementaryTypes() {
		return null;
	}


	@Override
	public INode getSupplementaryType(String nodeName) {
		return null;
	}


	@Override
	public INode getSupplementaryTypeInModel(IModel supplementaryModel) {
		// TODO Auto-generated method stub
		return null;
	}

}

package no.hvl.multecore.common.hierarchy;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractNode implements INode {

	protected String name;
	protected INode type;
	protected Map<String,DeclaredAttribute> declaredAttributes;
	protected IModel model;
	protected Potency potency;
	protected Set<INode> parentNodes;
	
	
	@Override
	public String getName() {
		return name;
	}


	@Override
	public INode getType() {
		return type;
	}


	@Override
	public Set<DeclaredAttribute> getDeclaredAttributes(boolean includePotencyAccessible) {
		HashSet<DeclaredAttribute> attributes = new HashSet<DeclaredAttribute>(declaredAttributes.values());
		if (!includePotencyAccessible)
			return attributes;
		for (INode typeNode : getTransitiveTypes()) {
			for (DeclaredAttribute da : typeNode.getDeclaredAttributesPlusInherited(false)) {
				// Need to check if the potency is compatible with the next level, so that this attribute can be instantiated
				if (da.getPotency().compatibleWithReversePotency(model.getLevel() - typeNode.getModel().getLevel() + 1))
					attributes.add(da);
			}
		}
		return attributes;
	}


	@Override
	public Set<DeclaredAttribute> getDeclaredAttributesPlusInherited(boolean includePotencyAccessible) {
		Set<DeclaredAttribute> declaredAttributesPlusInherited = getDeclaredAttributes(includePotencyAccessible);
		for (INode parentNode : getAllParentNodes())
			declaredAttributesPlusInherited.addAll(parentNode.getDeclaredAttributesPlusInherited(includePotencyAccessible));
		return declaredAttributesPlusInherited;
	}
	
	
	@Override
	public DeclaredAttribute getDeclaredAttribute(String attributeName, boolean includePotencyAccessible) {
		if (!includePotencyAccessible)
			return declaredAttributes.get(attributeName);
		for (DeclaredAttribute da : getDeclaredAttributes(includePotencyAccessible)) {
			if (da.nameOrValue.equals(attributeName))
				return da;
		}
		return null;
	}
	
	
	@Override
	public DeclaredAttribute getDeclaredAttributePlusInherited(String attributeName, boolean includePotencyAccessible) {
		DeclaredAttribute declaredAttribute = getDeclaredAttribute(attributeName, includePotencyAccessible);
		if (null != declaredAttribute)
			return declaredAttribute;
		for (INode parentNode : getAllParentNodes()) {
			if (null != (declaredAttribute = parentNode.getDeclaredAttribute(attributeName, includePotencyAccessible)))
				return declaredAttribute;
		}
		return null;
	}
	

	@Override
	public IModel getModel() {
		return model;
	}
	

	@Override
	public Potency getPotency() {
		return potency;
	}
	
	
	@Override
	public Set<INode> getParentNodes() {
		return parentNodes;
	}
	
	
	@Override
	public Set<INode> getAllParentNodes() {
		Set<INode> allParentNodes = getParentNodes();
		for (INode parentNode : getParentNodes())
			allParentNodes.addAll(parentNode.getAllParentNodes());
		return allParentNodes;
	}
	
	
	@Override
	public String toString() {
		return name;
	}
}

package no.hvl.multecore.common.hierarchy;

import java.util.Set;

public interface INode {
	
	public String getName();
	
	public INode getType();

	public Set<INode> getTransitiveTypes();

	public Set<INode> getTransitiveTypesPlusInherited();

	public Set<IAttribute> getAttributes(boolean includePotencyAccessible);
	
	public Set<IAttribute> getAttributesPlusInherited(boolean includePotencyAccessible);

	public Set<DeclaredAttribute> getDeclaredAttributes(boolean includePotencyAccessible);
	
	public Set<DeclaredAttribute> getDeclaredAttributesPlusInherited(boolean includePotencyAccessible);

	public Set<InstantiatedAttribute> getInstantiatedAttributes();
	
	public Set<InstantiatedAttribute> getInstantiatedAttributesPlusInherited();

	public DeclaredAttribute getDeclaredAttribute(String attributeName, boolean includePotencyAccessible);
	
	public DeclaredAttribute getDeclaredAttributePlusInherited(String attributeName, boolean includePotencyAccessible);
	
	public InstantiatedAttribute getInstantiatedAttribute(String attributeName);
	
	public InstantiatedAttribute getInstantiatedAttributePlusInherited(String attributeName);
	
	public Set<INode> getSupplementaryTypes();
	
	public INode getSupplementaryType(String typeName);
	
	public INode getSupplementaryTypeInModel(IModel supplementaryModel);

	public IModel getModel();

	public Potency getPotency();

	public Set<INode> getParentNodes();

	public Set<INode> getAllParentNodes();
	
}

package no.hvl.multecore.common.hierarchy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Node extends AbstractNode {
	
	private Map<String,InstantiatedAttribute> instantiatedAttributes;
	private boolean isAbstract;
	private Set<INode> supplementaryNodes;


	public Node(String name, INode type, IModel model) {
		this.name = name;
		this.type = type;
		this.declaredAttributes = new HashMap<String, DeclaredAttribute>();
		this.instantiatedAttributes = new HashMap<String, InstantiatedAttribute>();
		this.model = model;
		this.potency = new Potency();
		isAbstract = false;
		parentNodes = new HashSet<INode>();
		supplementaryNodes = new HashSet<INode>();
	}
	
	
	public Node(String name, INode type, IModel model, Potency potency) {
		this.name = name;
		this.type = type;
		this.declaredAttributes = new HashMap<String, DeclaredAttribute>();
		this.instantiatedAttributes = new HashMap<String, InstantiatedAttribute>();
		this.model = model;
		this.potency = potency;
		isAbstract = false;
		parentNodes = new HashSet<INode>();
		supplementaryNodes = new HashSet<INode>();

	}
	
	
	public void setName(String name) {
		this.name = name;
	}
	
	
	public void setType(INode type) {
		this.type = type;
	}


	@Override
	public Set<INode> getTransitiveTypes() {
		Set<INode> types = new HashSet<INode>();
		types.add(this);
		types.addAll(type.getTransitiveTypes());
		return types;
	}


	@Override
	public Set<INode> getTransitiveTypesPlusInherited() {
		Set<INode> types = new HashSet<INode>();
		types.add(this);
		types.addAll(getAllParentNodes());
		types.addAll(type.getTransitiveTypesPlusInherited());
		return types;
	}


	@Override
	public Set<IAttribute> getAttributes(boolean includePotencyAccessible) {
		Set<IAttribute> attributes = new HashSet<IAttribute>(getDeclaredAttributes(includePotencyAccessible));
		attributes.addAll(getInstantiatedAttributes());
		return attributes;
	}


	@Override
	public Set<IAttribute> getAttributesPlusInherited(boolean includePotencyAccessible) {
		Set<IAttribute> attributesPlusInherited = getAttributes(includePotencyAccessible);
		for (INode parentNode : getAllParentNodes())
			attributesPlusInherited.addAll(parentNode.getAttributesPlusInherited(includePotencyAccessible));
		return attributesPlusInherited;
	}


	@Override
	public Set<InstantiatedAttribute> getInstantiatedAttributes() {
		return new HashSet<InstantiatedAttribute>(instantiatedAttributes.values());
	}


	@Override
	public Set<InstantiatedAttribute> getInstantiatedAttributesPlusInherited() {
		Set<InstantiatedAttribute> instantiatedAttributesPlusInherited = getInstantiatedAttributesPlusInherited();
		for (INode parentNode : getAllParentNodes())
			instantiatedAttributesPlusInherited.addAll(parentNode.getInstantiatedAttributesPlusInherited());
		return instantiatedAttributesPlusInherited;
	}


	@Override
	public InstantiatedAttribute getInstantiatedAttribute(String attributeType) {
		return instantiatedAttributes.get(attributeType);
	}
	
	
	@Override
	public InstantiatedAttribute getInstantiatedAttributePlusInherited(String attributeName) {
		InstantiatedAttribute instantiatedAttribute = getInstantiatedAttribute(attributeName);
		if (null != instantiatedAttribute)
			return instantiatedAttribute;
		for (INode parentNode : getAllParentNodes()) {
			if (null != (instantiatedAttribute = parentNode.getInstantiatedAttribute(attributeName)))
				return instantiatedAttribute;
		}
		return null;
	}
	
	
	public void addAttribute(IAttribute attribute) {
		if (attribute instanceof DeclaredAttribute) {
			declaredAttributes.put(attribute.getNameOrValue(), (DeclaredAttribute) attribute);
			return;
		}
		if (attribute instanceof InstantiatedAttribute)
			instantiatedAttributes.put(attribute.getType().getNameOrValue(), (InstantiatedAttribute) attribute);
	}

	
	@Override
	public Set<INode> getSupplementaryTypes() {
		return supplementaryNodes;
	}
	
	
	@Override
	public INode getSupplementaryType(String nodeName) {
		for (INode iNode : supplementaryNodes) {
			if (iNode.getName().equals(nodeName))
				return iNode;
		}
		return null;
	}
	
	
	@Override
	public INode getSupplementaryTypeInModel(IModel supplementaryModel) {
		for (INode iNode : supplementaryNodes) {
			if (iNode.getModel().equals(supplementaryModel))
				return iNode;
		}
		return null;
	}
	
	public void addSupplementaryNodes(Set<INode> supplementaryNodes) {
		this.supplementaryNodes = supplementaryNodes;
	}

	
	public void addSupplementaryNode(INode supplementaryNode) {
		supplementaryNodes.add(supplementaryNode);
	}
	
	
	public void setPotency(Potency potency) {
		this.potency = potency;
	}


	public boolean isAbstract() {
		return isAbstract;
	}


	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}


	public void setParentNodes(Set<INode> parentNodes) {
		this.parentNodes = parentNodes;
	}

	
	public void addParentNode(Node parentNode) {
		parentNodes.add(parentNode);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Node))
			return false;
		Node other = (Node) obj;
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
		IModel otherModel = other.getModel();
		if (model == null) {
			if (otherModel != null)
				return false;
		} else if (!model.equals(otherModel))
			return false;
		return true;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
	
}

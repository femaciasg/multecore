package no.hvl.multecore.common.hierarchy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.hvl.multecore.common.Constants;
import no.hvl.multecore.common.Debugger;
import no.hvl.multecore.common.MultEcoreManager;
import no.hvl.multecore.common.exceptions.BadlyFormattedElement;
import no.hvl.multecore.common.exceptions.ConflictingAttribute;
import no.hvl.multecore.common.exceptions.DanglingEdge;
import no.hvl.multecore.common.exceptions.DuplicatedElement;
import no.hvl.multecore.common.exceptions.ElementNotExists;
import no.hvl.multecore.common.exceptions.IncompatibleInheritance;
import no.hvl.multecore.common.exceptions.InvalidSupplementaryElement;
import no.hvl.multecore.common.exceptions.MultEcoreException;
import no.hvl.multecore.common.hierarchy.DeclaredAttribute.NativeType;

public class MultilevelHierarchy {
	
	protected final String name;
	protected int maxLevel;
	protected final Map<String,IModel> models;
	protected final Map<IModel,Set<IModel>> childrenModels;
	protected final Map<IModel,Set<INode>> nodes;
	protected final Map<IModel,Set<IEdge>> edges;
	protected final IModel root;
	protected final Set<MultilevelHierarchy> supplementaryHierarchies;
	
	public MultilevelHierarchy(String name) {
		this.name = name;
		maxLevel = 0;
		root = new EcoreModel(this);
		models = new HashMap<String,IModel>();
		models.put(root.getName(), root);
		childrenModels = new HashMap<IModel, Set<IModel>>();
		childrenModels.put(root, new HashSet<IModel>());
		nodes = new LinkedHashMap<IModel, Set<INode>>();
		nodes.put(root, root.getNodes());
		edges = new LinkedHashMap<IModel, Set<IEdge>>();
		edges.put(root, root.getEdges());
		supplementaryHierarchies = new HashSet<MultilevelHierarchy>();
	}
	
	
	public String getName() {
		return name;
	}
	
	
	public int getMaxLevel() {
		return maxLevel;
	}
	
	
	public IModel getModelByName(String modelName) {		
		return models.get(modelName);
	}
	
	
	public Set<IModel> getAllModels() {
		return new HashSet<IModel>(models.values());
	}

	
	public IModel createModel(String modelName, String metamodelName) {
		IModel metamodel = getModelByName(metamodelName);
		int modelLevel = metamodel.getLevel() + 1;
		IModel model = new Model(modelName, metamodel, modelLevel);
		childrenModels.computeIfAbsent(metamodel, k -> new HashSet<IModel>()).add(model);
		childrenModels.put(model, new HashSet<IModel>());
		nodes.put(model, new HashSet<INode>());
		edges.put(model, new HashSet<IEdge>());
		models.put(modelName, model);
		if (modelLevel > maxLevel)
			maxLevel = modelLevel;
		return model;
	}
	
	
	public void addModel(IModel model) {
		models.put(model.getName(), model);
		int modelLevel = model.getLevel();
		if (modelLevel > maxLevel)
			maxLevel = modelLevel;
		childrenModels.put(model, new HashSet<IModel>());
		for (IModel m : model.getDirectMetamodels()) {
			childrenModels.computeIfAbsent(m, k -> new HashSet<IModel>()).add(model);
		}
		nodes.computeIfAbsent(model, k -> new HashSet<INode>()).addAll(model.getNodes());
		edges.computeIfAbsent(model, k -> new HashSet<IEdge>()).addAll(model.getEdges());
	}
	
	
	public IModel createModelUpdateCopy(IModel oldModelVersion) {
		return new Model(oldModelVersion.getName(), oldModelVersion.getDirectMetamodels(), oldModelVersion.getLevel());
	}
	
	
	public void updateModel(IModel oldModelVersion, IModel newModelVersion) {
		models.put(oldModelVersion.getName(), newModelVersion);
		Set<IModel> children = childrenModels.remove(oldModelVersion);
		for (IModel metamodel : oldModelVersion.getAllMetamodels()) {
			Set<IModel> metamodelChildrenModels = childrenModels.get(metamodel);
			if (metamodelChildrenModels.contains(oldModelVersion)) {
				metamodelChildrenModels.remove(oldModelVersion);
				metamodelChildrenModels.add(newModelVersion);
			}
		}
		for (IModel child : children) {
			Model childModel = (Model) child; // We can safely assume that a children model will never be EcoreModel
			childModel.addOrReplaceMetamodel(newModelVersion);
		}
		childrenModels.put(newModelVersion, children);
		nodes.remove(oldModelVersion);
		nodes.put(newModelVersion, newModelVersion.getNodes());
		edges.remove(oldModelVersion);
		edges.put(newModelVersion, newModelVersion.getEdges());
	}
	
	
	public Set<MultilevelHierarchy> getSupplementaryHierarchies() {
		return supplementaryHierarchies;
	}
	
	
	public MultilevelHierarchy getSupplementaryHierarchy(String hierarchyName) {
		for (MultilevelHierarchy mlh : supplementaryHierarchies) {
			if (mlh.getName().equals(hierarchyName))
				return mlh;
		}
		return null;
	}

	
	
	public void addSupplementaryHierarchy(MultilevelHierarchy supplementaryHierarchy) {
		this.supplementaryHierarchies.add(supplementaryHierarchy);
	}
	
	
	public void addSupplementaryHierarchies(Set<MultilevelHierarchy> supplementaryHierarchies) {
		this.supplementaryHierarchies.addAll(supplementaryHierarchies);
	}
	
	
	public void addSupplementaryModel(IModel applicationIModel, String supplementaryHierarchyName, String supplementaryModelName) throws MultEcoreException {
		// Check if application model is valid
		if (!(applicationIModel instanceof Model))
			throw new InvalidSupplementaryElement("model", supplementaryModelName, applicationIModel.getName());
		
		// Check if supplementary hierarchy is valid
		MultilevelHierarchy supplementaryHierarchy = getSupplementaryHierarchy(supplementaryHierarchyName);
		if (null == supplementaryHierarchy)
			throw new InvalidSupplementaryElement("hierarchy", supplementaryHierarchyName, name);

		// Check if supplementary model is valid
		IModel supplementaryModel = supplementaryHierarchy.getModelByName(supplementaryModelName);
		if (null == supplementaryModel)
			throw new InvalidSupplementaryElement("model", supplementaryModelName, applicationIModel.getName());
		
		// Add supplementary model
		Model applicationModel = (Model) applicationIModel;
		applicationModel.addSupplementaryModel(supplementaryModel);
	}

	public void addSupplementaryNode (INode applicationINode, String supplementaryModelName, String supplementaryNodeName) throws MultEcoreException {
		// Check if application node is valid
		if (!(applicationINode instanceof Node))
			throw new InvalidSupplementaryElement("node", supplementaryNodeName, applicationINode.getName());

		// Check if supplementary model is valid
		IModel supplementaryModel = applicationINode.getModel().getSupplementaryModel(supplementaryModelName);
		if (null == supplementaryModel)
			throw new InvalidSupplementaryElement("model", supplementaryModelName, applicationINode.getModel().getName());
		
		// Check if supplementary node is valid
		INode supplementaryNode = applicationINode.getModel().getSupplementaryModel(supplementaryModelName).getNode(supplementaryNodeName);
		List<String> primitiveTypes = new ArrayList<String>(Arrays.asList(MultEcoreManager.instance().getPrimitiveTypesNames()));
		if (null == supplementaryNode && !primitiveTypes.contains(supplementaryNodeName))
			throw new InvalidSupplementaryElement("node", supplementaryNodeName, applicationINode.getName());

		Node applicationNode = (Node) applicationINode;
		applicationNode.addSupplementaryNode(supplementaryNode);
	}
	
	public Node addNode(String nodeName, String typeName, int typeReversePotency, int startPotency, int endPotency, int depthPotency, Set<String> parentNodeNames, IModel iModel) throws MultEcoreException {
		Model model = (Model) iModel;
		int modelLevel = model.getLevel();

		IModel metamodel = null;
		if (typeName.equals(Constants.ECLASS_ID)) {
			metamodel = models.get(Constants.ECORE_ID);
		} else {
			metamodel = model.getMetamodelInLevel(modelLevel - typeReversePotency);
		}
		
		// Check if metamodel is valid
		if (null == metamodel)
			throw new BadlyFormattedElement(nodeName, typeName, model.getName());

		 // Check if type is valid
		INode typeNode = metamodel.getNode(typeName);
		if (null == typeNode)
			throw new BadlyFormattedElement(nodeName, typeName, model.getName());
		if (!typeNode.getPotency().compatibleWithReversePotency(typeReversePotency))
			throw new BadlyFormattedElement(nodeName, typeName, model.getName());
		
		// Check if the value of depth is valid, and fix it otherwise
		int typeDepthPotency = typeNode.getPotency().getDepth();
		int depthPotencyChecked = depthPotency;
		if ((typeDepthPotency != Constants.UNBOUNDED) && (typeDepthPotency <= depthPotency)) {
			depthPotencyChecked = typeDepthPotency - 1;
			Debugger.log("Assuming default depth (type-1) for node " + nodeName);
		}
		
		// Create node
		Potency potency = new Potency(startPotency, endPotency, depthPotencyChecked);
		Node node = new Node(nodeName, typeNode, model, potency);	
		if (model.getNodes().contains(node))
			throw new DuplicatedElement(nodeName, model.getName());

		// Add parent nodes
		for (String parentName : parentNodeNames) {
        	Node parentNode = (Node) model.getNode(parentName);
        	if (null == parentNode)
        		throw new ElementNotExists(name, model.getName());
        	if (!(parentNode.getType().equals(typeNode)) || !(parentNode.getPotency().equals(potency)))
        		throw new IncompatibleInheritance(nodeName, parentName, model.getName());
        	node.addParentNode(parentNode);
		}
		
		// Register node
		model.addOrReplaceNode(node);
		nodes.computeIfAbsent(model, k -> new HashSet<INode>()).remove(node);
		nodes.get(model).add(node);
		
		return node;
	}
	
	
	public Edge addEdge(String edgeName, String typeName, int typeReversePotency, String sourceName, String targetName, int startPotency, int endPotency, int depthPotency, IModel iModel) throws MultEcoreException {
		Model model = (Model) iModel;
		INode sourceNode = null, targetNode = null;
		if (null == (sourceNode = model.getNode(sourceName)))
			throw new DanglingEdge(edgeName, sourceName, targetName, iModel.getName());
		if (null == (targetNode = model.getNode(targetName)))
			throw new DanglingEdge(edgeName, sourceName, targetName, iModel.getName());
		if (null != iModel.getEdgePlusInherited(edgeName, sourceName, targetName))
			throw new DuplicatedElement(edgeName, iModel.getName());
			
		// Find metamodel and type
		IModel metamodel = null;
		IEdge typeEdge =  null;
		if (typeName.equals(Constants.EREFERENCE_ID)) {
			metamodel = models.get(Constants.ECORE_ID);
			typeEdge = metamodel.getEdge(Constants.EREFERENCE_ID, Constants.ECLASS_ID, Constants.ECLASS_ID);
		} else {
			metamodel = model;
			do {
				metamodel = metamodel.getMainMetamodel();
				INode sourceTypeNode = null, targetTypeNode = null;
				if (null == (sourceTypeNode = getTypeInModel(sourceNode, metamodel))) continue;
				if (null == (targetTypeNode = getTypeInModel(targetNode, metamodel))) continue;
				typeEdge = metamodel.getEdgePlusInherited(typeName, sourceTypeNode.getName(), targetTypeNode.getName());
				if ((null != typeEdge) && (typeEdge.getPotency().compatibleWithReversePotency(typeReversePotency))) break;
			} while (!(metamodel instanceof EcoreModel));
		}
		
		// Check if type is valid
		if (null == typeEdge)
			throw new BadlyFormattedElement(edgeName, typeName, model.getName());
		
		// Non-dangling typing condition
		INode typeEdgeSourceNode = typeEdge.getSource();
		INode typeEdgeTargetNode = typeEdge.getTarget();
		if (!sourceNode.getTransitiveTypesPlusInherited().contains(typeEdgeSourceNode) || !targetNode.getTransitiveTypesPlusInherited().contains(typeEdgeTargetNode))
			throw new DanglingEdge(edgeName, sourceName, targetName, iModel.getName());

		// Check if the value of depth is valid, and fix it otherwise
		int typeDepthPotency = typeEdge.getPotency().getDepth();
		int depthPotencyChecked = depthPotency;
		if ((typeDepthPotency != Constants.UNBOUNDED) && (typeDepthPotency <= depthPotency)) {
			depthPotencyChecked = typeDepthPotency - 1;
			Debugger.log("Assuming default depth (type-1) for edge " + edgeName);
		}
		
		// Create and register edge
		Edge edge = new Edge(edgeName, typeEdge, sourceNode, targetNode, model, new Potency(startPotency, endPotency, depthPotencyChecked));
		model.addOrReplaceEdge(edge);
		edges.computeIfAbsent(model, k -> new HashSet<IEdge>()).add(edge);
		return edge;
	}
	
	
	public DeclaredAttribute addDeclaredAttribute(String attributeName, NativeType type, String containingNodeName, int startPotency, int endPotency, IModel iModel) throws MultEcoreException {
		INode containingNode = iModel.getNode(containingNodeName);
		if (null == containingNode)
			throw new ElementNotExists(containingNodeName, iModel.getName());
		
		if (null != containingNode.getDeclaredAttributePlusInherited(attributeName, false))
			throw new DuplicatedElement(attributeName, iModel.getName());
		
		// Check for duplicates through potency
		IAttribute conflictingAttribute = containingNode.getDeclaredAttributePlusInherited(attributeName, true);
		if (null != conflictingAttribute) {
			// Check for potency overlap for equally named attributes inside the chain of typings
			int levelDifference = containingNode.getModel().getLevel() - conflictingAttribute.getModel().getLevel();
			Potency conflictingAttributePotency = conflictingAttribute.getPotency();
			if (conflictingAttributePotency.getEnd() >= (startPotency + levelDifference) && conflictingAttributePotency.getStart() <= (endPotency + levelDifference))
				throw new ConflictingAttribute(attributeName, containingNodeName, iModel.getName(), conflictingAttribute.getContainingNode().getName(), conflictingAttribute.getModel().getName());
		}
		
		// Create and register attribute
		DeclaredAttribute attribute = new DeclaredAttribute(attributeName, type, containingNode, new Potency(startPotency, endPotency, 1));
		((Node) containingNode).addAttribute(attribute);
		return attribute;
	}
	
	
	public InstantiatedAttribute addInstantiatedAttribute(String attributeValue, String typeName, String containingNodeName, IModel iModel) throws MultEcoreException {
		INode containingNode = iModel.getNode(containingNodeName);
		if (null == containingNode)
			throw new ElementNotExists(containingNodeName, iModel.getName());
		
		// Find the declared attribute, in order to instantiate it
		DeclaredAttribute attributeType = containingNode.getType().getDeclaredAttributePlusInherited(typeName, true);
		if (null == attributeType) {
			// Check if the attribute is declared in a supplementary type node
			for (INode supplementaryTypeNode : containingNode.getSupplementaryTypes()) {
				attributeType = supplementaryTypeNode.getDeclaredAttributePlusInherited(typeName, true);
				
				if (null != attributeType)
					break;
			}
			
			if (null == attributeType)
				throw new BadlyFormattedElement(attributeValue, typeName, iModel.getName());
		}

		// Create and register attribute
		InstantiatedAttribute attribute = new InstantiatedAttribute(attributeValue, attributeType, containingNode);
		((Node) containingNode).addAttribute(attribute);
		return attribute;
	}

	
	public IModel getNodeModel(INode node) {
		for (IModel model : nodes.keySet()) {
			Set<INode> modelNodes = nodes.get(model);
			if (modelNodes.contains(node))
				return model;
		}
		
		return null;
	}
	
	
	public IModel getEdgeModel(IEdge edge) {
		for (IModel model : edges.keySet()) {
			Set<IEdge> modelEdges = edges.get(model);
			if (modelEdges.contains(edge))
				return model;
		}
		
		return null;
	}
	
	
	public Map<INode, Set<IEdge>> allAvailableTypesInModel(IModel model, boolean excludeEcoreTypes) {
		Map<INode,Set<IEdge>> nodeTypesAvailable = new LinkedHashMap<INode,Set<IEdge>>();
		Set<IEdge> edgeTypesAvailable = new HashSet<IEdge>();
		for (IModel metamodel : model.getAllMetamodels()) {
			int typeReversePotency = model.getLevel() - metamodel.getLevel();
			for (INode node : metamodel.getNodes()) {
				if ((node instanceof EClassNode) && (excludeEcoreTypes))
					continue;
				if (node.getPotency().compatibleWithReversePotency(typeReversePotency))
					nodeTypesAvailable.put(node, new HashSet<IEdge>());
			}
			for (IEdge edge : metamodel.getEdges()) {
				if ((edge instanceof EReferenceEdge) && (excludeEcoreTypes))
					continue;
				if (edge.getPotency().compatibleWithReversePotency(typeReversePotency))
					edgeTypesAvailable.add(edge);
			}
		}
		for (IEdge edge : edgeTypesAvailable) {
			INode edgeSourceNode = edge.getSource(), edgeTargetNode = edge.getTarget();
			for (INode sNode : nodeTypesAvailable.keySet()) {
				for (INode tNode : nodeTypesAvailable.keySet()) {
					if ((sNode.equals(edgeSourceNode) || sNode.getTransitiveTypes().contains(edgeSourceNode)) &&
							(tNode.equals(edgeTargetNode) || tNode.getTransitiveTypes().contains(edgeTargetNode))) {
						Edge edgeCopy = new Edge(edge);
						edgeCopy.setSource(sNode);
						edgeCopy.setTarget(tNode);
						nodeTypesAvailable.get(sNode).add(edgeCopy);
					}
				}
			}
		}
		return nodeTypesAvailable;
	}
	
	
	// Return the type (direct or indirect) of a node in a model, if it exists
	public INode getTypeInModel(INode node, IModel model) {
		Set<INode> types;
		(types = node.getTransitiveTypes()).retainAll(nodes.get(model));
		return (types.size()==1)? types.iterator().next() : null;
	}
	

	public Set<IModel> getChildrenModels(IModel iModel) {
		return childrenModels.get(iModel);
	}


	public IModel getRootModel() {
		return root;
	}

	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		List<IModel> modelList = new ArrayList<IModel>(models.values());
		Collections.sort(modelList);
		result.append("==== " + name + " ====\n");
		for (IModel model : modelList) {
			if (model instanceof EcoreModel)
				continue;
			result.append("-- " + model.getName() + " : ");
			for (IModel metamodel : model.getDirectMetamodels()) {
				result.append(metamodel.getName() + ", ");
			}
			result.delete(result.length()-2, result.length());
			result.append(" --\n");
			for (INode iNode : nodes.get(model)) {
				Node node = (Node) iNode;
				String parentNodeNames = "";
				Set<INode> parentNodes = node.getParentNodes();
				if(!parentNodes.isEmpty()) parentNodeNames += "(";
				for (INode parentNode : parentNodes) {
					parentNodeNames += parentNode.getName() + ",";
				}
				parentNodeNames.substring(0, parentNodeNames.length());
				if(!parentNodes.isEmpty()) parentNodeNames += ")";
				Potency nodePotency = node.getPotency();
				INode typeNode = node.getType();
				int levelDifference = node.getModel().getLevel()-typeNode.getModel().getLevel();
				String typeNodeName = typeNode.getName() + ((levelDifference>1)? "@" + levelDifference : "");
				result.append(node.getName() + "@" + nodePotency.getStart() + "-" + nodePotency.getEnd() + "-" + nodePotency.getDepth() +
						": " + typeNodeName +
						parentNodeNames +
						"\n");
				for (IEdge edge : model.getOutEdges(node)) {
					Potency edgePotency = edge.getPotency();
					String containment = (edge.isContainment())? "<>" : "--";
					IEdge typeEdge = edge.getType();
					levelDifference = edge.getModel().getLevel()-typeEdge.getModel().getLevel();
					String typeEdgeName = typeEdge.getName() + ((levelDifference>1)? "@" + levelDifference : "");
					result.append("  " + containment + "- " + edge.getName() + "@" + edgePotency.getStart() + "-" + edgePotency.getEnd() + "-" + edgePotency.getDepth() + ": " + typeEdgeName + " --> " + edge.getTarget().getName() + "\n");
				}
				for (IAttribute iAttribute : iNode.getAttributes(false)) {
					Potency attributePotency = iAttribute.getPotency();
					String isInstantiated = (iAttribute instanceof InstantiatedAttribute)? " * " : " + ";
					result.append("  " + isInstantiated + iAttribute.getNameOrValue() + "@" + attributePotency.getStart() + "-" + attributePotency.getEnd() + "-" + attributePotency.getDepth() + ": " + iAttribute.getType().getNameOrValue() + "\n");
				}
			}
			result.append("\n\n");
		}
		return result.toString();
	}
	

	public boolean isCPN() {
		IModel cpnModel = getModelByName(Constants.CPN_ID);
		return (null != cpnModel) && (cpnModel.getLevel() == 1);
	}
	
}

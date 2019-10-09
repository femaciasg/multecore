package no.hvl.multecore.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EContentsEList;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import no.hvl.multecore.common.Constants;
import no.hvl.multecore.common.Debugger;
import no.hvl.multecore.common.MultEcoreManager;
import no.hvl.multecore.common.exceptions.BadlyFormattedElement;
import no.hvl.multecore.common.exceptions.MultEcoreException;
import no.hvl.multecore.common.hierarchy.DeclaredAttribute;
import no.hvl.multecore.common.hierarchy.DeclaredAttribute.NativeType;
import no.hvl.multecore.common.hierarchy.Edge;
import no.hvl.multecore.common.hierarchy.IModel;
import no.hvl.multecore.common.hierarchy.Model;
import no.hvl.multecore.common.hierarchy.MultilevelHierarchy;
import no.hvl.multecore.common.registry.ModelRegistryEntry;

public class ModelToHierarchyTransformer extends AbstractTransformerToHierarchy {

	private EObject rootEObject;

	public ModelToHierarchyTransformer(URI modelURI, MultilevelHierarchy multilevelHierarchy) {
		super(modelURI, multilevelHierarchy);
		getMetamodelName();
	}

	@Override
	public String getMetamodelName() {
		if (null != metamodelName)
			return metamodelName;
		ModelRegistryEntry mre = MultEcoreManager.instance().getModelRegistry().getEntry(modelURI);
		Resource modelResource = mre.getModelResource();
		if (!modelResource.isLoaded()) {
			ResourceSet resourceSet = new ResourceSetImpl();
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
			modelResource = resourceSet.getResource(modelResource.getURI(), true);
			mre.setModelResource(modelResource);
		}
		rootEObject = modelResource.getContents().get(0);
		String metamodelsAttribute = this.getAttributeValue(rootEObject, Constants.METAMODELS_ATTRIBUTE_NAME).toString();

		if (metamodelsAttribute.isEmpty()) {
			metamodelName = rootEObject.eClass().getEPackage().getName();
		} else {
			metamodelName = metamodelsAttribute;
		}
		return metamodelName;
	}

	@SuppressWarnings("unchecked")
	public void transform() throws MultEcoreException {
		super.transform();
		// Add supplementary Hierarchies
		List<String> supplementaryHierarchiesAttribute = new ArrayList<String>();
		supplementaryHierarchiesAttribute = (List<String>) this.getAttributeValue(rootEObject,
				Constants.SUPPLEMENTARY_METAMODELS_ATTRIBUTE_NAME);
		for (int i = 0; i < supplementaryHierarchiesAttribute.size(); i++) {
			String[] supplementaryPair = supplementaryHierarchiesAttribute.get(i).split(":");
			Model model = (Model) iModelUpdate;
			// We differentiate, if it is the primitive types hierarchy, we can't search a project
			if (supplementaryPair[0].equals(no.hvl.multecore.common.Constants.PRIMITIVE_TYPES_HIERARCHY)) {
				iModelUpdate.getHierarchy().addSupplementaryHierarchy(MultEcoreManager.instance().getPrimitiveTypesHirarchy());
				model.addSupplementaryModel(MultEcoreManager.instance().getPrimitiveTypesHirarchy().getModelByName(supplementaryPair[1]));
			} else {
				iModelUpdate.getHierarchy().addSupplementaryHierarchy(MultEcoreManager.instance().getMultilevelHierarchy(supplementaryPair[0]));
				if (supplementaryPair.length==2) {	
					model.addSupplementaryModel(MultEcoreManager.instance().getMultilevelHierarchy(supplementaryPair[0]).getModelByName(supplementaryPair[1]));					
				}
			}
		}

		// Transform nodes
		for (EObject eObject : rootEObject.eContents()) {
			transform(eObject);
		}

		// Transform edges
		for (EObject eObject : rootEObject.eContents()) {
			transformReferences(eObject);
		}

		// Update the model to the new version (or create)
		if (null != iModel) {
			multilevelHierarchy.updateModel(iModel, iModelUpdate);
		}
	}

	@SuppressWarnings("rawtypes")
	private void transformReferences(EObject eObject) throws MultEcoreException {
		EObject target = null;
		for (EContentsEList.FeatureIterator featureIteratorReferences = (EContentsEList.FeatureIterator) eObject
				.eCrossReferences().iterator(); featureIteratorReferences.hasNext();) {
			target = (EObject) featureIteratorReferences.next();
			transform((EReference) featureIteratorReferences.feature(), eObject, target);
		}

		// Transform references of contained eObjects
		for (EContentsEList.FeatureIterator featureIteratorContainments = (EContentsEList.FeatureIterator) eObject
				.eContents().iterator(); featureIteratorContainments.hasNext();) {
			EObject containedEO = (EObject) featureIteratorContainments.next();
			transformReferences(containedEO);
		}
	}

	@SuppressWarnings("unchecked")
	private void transform(EObject eObject) throws MultEcoreException {
		Map<String, String> supplementaryNodesMap = new HashMap<String, String>();
		List<String> supplementaryNodes = (List<String>) this.getAttributeValue(eObject,
				Constants.SUPPLEMENTARY_NODES_ATTRIBUTE_NAME);
		if (null != supplementaryNodes) {
			for (int i = 0; i < supplementaryNodes.size(); i++) {
				String[] supplementaryPair = supplementaryNodes.get(i).split(":");
				supplementaryNodesMap.put(supplementaryPair[0], supplementaryPair[1]);
			}
		}

		String name = this.getAttributeValue(eObject, Constants.NAME_ATTRIBUTE_NAME).toString();
		int startPotency = Constants.START_POTENCY_DEFAULT_VALUE;
		int endPotency = Constants.END_POTENCY_DEFAULT_VALUE;
		int depthPotency = Constants.DEPTH_POTENCY_DEFAULT_VALUE;
		String typeName = eObject.eClass().getName();
		int typeReversePotency = Constants.POTENCY_DEFAULT_VALUE;
		if (typeName.contains(no.hvl.multecore.core.Constants.IMPORTED_TYPE_SEPARATOR)) {
			String[] fragments = typeName.split(no.hvl.multecore.core.Constants.IMPORTED_TYPE_SEPARATOR);
			IModel metamodel = multilevelHierarchy.getModelByName(fragments[0]);
			if ((fragments.length == 2) && (null != metamodel)) {
				typeReversePotency = iModelUpdate.getLevel() - metamodel.getLevel();
				typeName = fragments[1];
			}
		}

		if (name.equals(Constants.EMPTY_STRING)) {
			throw new BadlyFormattedElement(name, typeName, modelName);
		}

		String[] potencies = this.getAttributeValue(eObject, Constants.POTENCY_ATTRIBUTE_NAME).toString()
				.split(Constants.POTENCY_SEPARATOR);
		if (potencies.length == 3) {
			startPotency = Integer.parseUnsignedInt(potencies[0]);
			String potencyString = potencies[1];
			if (potencyString.equals(Constants.POTENCY_ATTRIBUTE_UNBOUNDED_VALUE)) {
				endPotency = Constants.UNBOUNDED;
			} else {
				endPotency = Integer.parseUnsignedInt(potencyString);
			}
			potencyString = potencies[2];
			if (potencyString.equals(Constants.POTENCY_ATTRIBUTE_UNBOUNDED_VALUE)) {
				depthPotency = Constants.UNBOUNDED;
			} else {
				depthPotency = Integer.parseUnsignedInt(potencyString);
			}
			if ((endPotency < startPotency) && (endPotency != Constants.UNBOUNDED)) {
				Debugger.logError("Format error in potency of element " + modelName + "::" + name);
			}
		} else {
			Debugger.logError("Format error in potency of element " + modelName + "::" + name);
		}

		// Parse whether node is abstract and its inheritance relations
		boolean isAbstract = Boolean
				.valueOf(this.getAttributeValue(eObject, Constants.IS_ABSTRACT_ATTRIBUTE_NAME).toString());
		Set<String> parentNodeNames = new HashSet<String>();
		Object parentNodesAttributeValue = this.getAttributeValue(eObject, Constants.PARENT_NODES_ATTRIBUTE_NAME);
		if ((null != parentNodesAttributeValue) && (!parentNodesAttributeValue.toString().isEmpty())
				&& (!parentNodesAttributeValue.toString().equals(Constants.EMPTY_STRING))) {
			String[] parentNodeNamesArray = parentNodesAttributeValue.toString().split(Constants.NAMES_SEPARATOR);
			for (int i = 0; i < parentNodeNamesArray.length; i++)
				parentNodeNames.add(parentNodeNamesArray[i]);
		}

		attemptToAddNode(new PendingNode(eObject, name, typeName, typeReversePotency, startPotency, endPotency,
				depthPotency, parentNodeNames, isAbstract, supplementaryNodesMap));
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void addNode(PendingNode pendingNode) throws MultEcoreException {
		EObject eObject = (EObject) pendingNode.originalNode;

		// Create node in model
		no.hvl.multecore.common.hierarchy.Node node = multilevelHierarchy.addNode(pendingNode.nodeName,
				pendingNode.typeName, pendingNode.typeReversePotency, pendingNode.startPotency, pendingNode.endPotency,
				pendingNode.depthPotency, pendingNode.parentNodeNames, iModelUpdate);
		node.setAbstract(pendingNode.isAbstract);

		if ((null != pendingNode.supplementaryNodesMap)) {
			Set<String> modelNames = pendingNode.supplementaryNodesMap.keySet();
			for (String modelName : modelNames) {
				String supplementaryNodeName = pendingNode.supplementaryNodesMap.get(modelName);
				if (!supplementaryNodeName.isEmpty())
					multilevelHierarchy.addSupplementaryNode(node, modelName, supplementaryNodeName);
			}
		}

		// Add its containment relations and (recursively) its contained elements
		for (EContentsEList.FeatureIterator featureIteratorContainments = (EContentsEList.FeatureIterator) eObject
				.eContents().iterator(); featureIteratorContainments.hasNext();) {
			EObject target = (EObject) featureIteratorContainments.next();
			transform(target);
			transform((EReference) featureIteratorContainments.feature(), eObject, target);
		}

		// Instantiated attributes
		for (EAttribute ea : eObject.eClass().getEAttributes()) {
			if (ea.getName().startsWith(Constants.SYNTHETIC_PREFIX))
				continue;

			Object eAttribute = eObject.eGet(ea);
			if ((null == eAttribute) || (!eObject.eIsSet(ea)))
				continue;

			if (eAttribute instanceof EList) {
				Debugger.logError("Unsupported multiplicity for attribute " + ea.getName() + " in " + pendingNode.nodeName);
				continue;
				// EList eAttributeList = (EList) eAttribute;
				// for (Object o : eAttributeList) {
				// ...
				// }
			} else {
				multilevelHierarchy.addInstantiatedAttribute(eAttribute.toString(), ea.getName(), pendingNode.nodeName,
						iModelUpdate);
			}
		}

		// Declared attributes
		EList<String> attributeList = (EList<String>) this.getAttributeValue(eObject,
				Constants.ATTRIBUTE_NAMES_ATTRIBUTE_NAME);
		if (null != attributeList) {
			for (String s : attributeList) {
				Matcher matcher = Pattern.compile(Constants.ATTRIBUTE_NAMES_REGEX_PARSE).matcher(s);
				while (matcher.find()) {
					DeclaredAttribute declaredAttribute = multilevelHierarchy.addDeclaredAttribute(matcher.group(1),
							NativeType.valueOf(matcher.group(2)), pendingNode.nodeName,
							Integer.parseInt(matcher.group(5)), Integer.parseInt(matcher.group(6)), iModelUpdate);
					declaredAttribute.setLowerBound(Integer.parseInt(matcher.group(3)));
					declaredAttribute.setUpperBound(Integer.parseInt(matcher.group(4)));
					declaredAttribute.setId(Boolean.valueOf(matcher.group(7)));
				}
			}
		}
	}

	private void transform(EReference eReference, EObject source, EObject target) throws MultEcoreException {
		
//		boolean foo = true;
		
		String typeName = eReference.getName();
		String name = typeName.toLowerCase();
		String importedTypeFullName = typeName;
		int typeReversePotency = Constants.POTENCY_DEFAULT_VALUE;
		if (typeName.contains(no.hvl.multecore.core.Constants.IMPORTED_TYPE_SEPARATOR)) {
			String[] fragments = typeName.split(no.hvl.multecore.core.Constants.IMPORTED_TYPE_SEPARATOR);
			IModel metamodel = multilevelHierarchy.getModelByName(fragments[0]);
			if ((fragments.length == 3 || fragments.length == 4) && (null != metamodel)) {
				typeReversePotency = iModelUpdate.getLevel() - metamodel.getLevel();
				name = fragments[1].toLowerCase();
				typeName = fragments[1];
			}
		}

		String sourceName = source.eGet(source.eClass().getEStructuralFeature(Constants.NAME_ATTRIBUTE_NAME)).toString();
		String targetName = getAttributeValue(target, Constants.NAME_ATTRIBUTE_NAME).toString();
		int lowerBound = Constants.RELATION_LOWERBOUND_DEFAULT_VALUE;
		int upperBound = Constants.RELATION_UPPERBOUND_DEFAULT_VALUE;
		int startPotency = Constants.START_POTENCY_DEFAULT_VALUE;
		int endPotency = Constants.END_POTENCY_DEFAULT_VALUE;
		int depthPotency = Constants.DEPTH_POTENCY_DEFAULT_VALUE;
		boolean isContainment = Constants.RELATION_IS_CONTAINMENT_DEFAULT_VALUE;

		if (sourceName.equals(Constants.NODE_NAME_ROOT)) {
			name = Constants.SERIALIZATION_ROOT_CONTAINMENT_PREFIX + target.eGet(target.eClass().getEStructuralFeature(Constants.NAME_ATTRIBUTE_NAME));
		} else {
			EStructuralFeature eStructuralFeature = source.eClass().getEStructuralFeature(Constants.RELATION_NAMES_ATTRIBUTE_NAME);
			String relationNames = source.eGet(eStructuralFeature).toString();
			if (relationNames.isEmpty())
				Debugger.log("Assuming default values for instance of edge \"" + typeName + "\"");
			Matcher matcher = Pattern.compile(Constants.RELATION_NAMES_REGEX_PARSE).matcher(relationNames);
			while (matcher.find()) {
				String actualTypeName = (typeReversePotency > 1) ? importedTypeFullName : typeName;
				if (actualTypeName.equals(matcher.group(1)) && targetName.equals(matcher.group(2))) {
					name = matcher.group(3);
					lowerBound = Integer.parseInt(matcher.group(4));
					upperBound = Integer.parseInt(matcher.group(5));
					int startPotencyCandidate = Integer.parseInt(matcher.group(6));
					String endPotencyString = matcher.group(7);
					int endPotencyCandidate = (endPotencyString.equals(Constants.POTENCY_ATTRIBUTE_UNBOUNDED_VALUE))? Constants.UNBOUNDED : Integer.parseInt(endPotencyString);
					String depthPotencyString = matcher.group(8);
					int depthPotencyCandidate = (depthPotencyString.equals(Constants.POTENCY_ATTRIBUTE_UNBOUNDED_VALUE))? Constants.UNBOUNDED : Integer.parseInt(depthPotencyString);
					if ((startPotencyCandidate <= endPotencyCandidate) || (endPotencyCandidate == Constants.UNBOUNDED)) {
						startPotency = startPotencyCandidate;
						endPotency = endPotencyCandidate;
						depthPotency = depthPotencyCandidate;
					} else {
						throw new BadlyFormattedElement(name, typeName, iModelUpdate.getName());
					}
					isContainment = Boolean.valueOf(matcher.group(9));
					break;
				}
			}
		}

		// Update runtime version of the model in the hierarchy
		Edge edge = multilevelHierarchy.addEdge(name, typeName, typeReversePotency, sourceName, targetName,
				startPotency, endPotency, depthPotency, iModelUpdate);
		edge.setLowerBound(lowerBound);
		edge.setUpperBound(upperBound);
		edge.setContainment(isContainment);
	}

	private Object getAttributeValue(EObject eObject, String attributeName) {
		EStructuralFeature eStructuralFeature = eObject.eClass().getEStructuralFeature(attributeName);
		if (null != eStructuralFeature)
			return eObject.eGet(eStructuralFeature);

		return null;
	}

}

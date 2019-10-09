package no.hvl.multecore.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import no.hvl.multecore.common.Debugger;
import no.hvl.multecore.common.MultEcoreManager;
import no.hvl.multecore.common.exceptions.MultEcoreException;
import no.hvl.multecore.common.hierarchy.DeclaredAttribute;
import no.hvl.multecore.common.hierarchy.DeclaredAttribute.NativeType;
import no.hvl.multecore.common.hierarchy.EcoreModel;
import no.hvl.multecore.common.hierarchy.IAttribute;
import no.hvl.multecore.common.hierarchy.IEdge;
import no.hvl.multecore.common.hierarchy.IModel;
import no.hvl.multecore.common.hierarchy.INode;
import no.hvl.multecore.common.hierarchy.InstantiatedAttribute;
import no.hvl.multecore.common.hierarchy.MultilevelHierarchy;
import no.hvl.multecore.common.hierarchy.Node;
import no.hvl.multecore.common.hierarchy.Potency;

public class ModelFromHierarchyTransformer extends AbstractTransformerFromHierarchy {
    
	protected Map<String, EObject> eObjects;
	protected EClass eClassEClass;
    protected EObject rootEObject;
    
    // Exceptionally, if the model is the topmost one, no XMI is generated from it
    private boolean isTopMostModel = false;
    
    public ModelFromHierarchyTransformer (URI modelURI, MultilevelHierarchy multilevelHierarchy) {
        super(modelURI, multilevelHierarchy);
        eObjects = new HashMap<String, EObject>();
        newResource = MultEcoreManager.instance().getModelRegistry().createResource(modelURI.trimFileExtension().appendFileExtension(Constants.FILE_EXTENSION_MODEL));
    }
    
    
    @Override
    public void transform() throws MultEcoreException {
    	// Handling the special case when this is a topmost model (right below Ecore)
    	if (isTopMostModel = (multilevelHierarchy.getModelByName(modelName).getMainMetamodel() instanceof EcoreModel))
    		return;
    	super.transform();
    }


    @Override
    protected void initialize() {
    	// Ecore metamodel required to create an instance
    	createEcore();
    	// Synthetic root
        EClass eClass = (EClass) ePackage.getEClassifier(no.hvl.multecore.common.Constants.NODE_NAME_ROOT);
        rootEObject = ePackage.getEFactoryInstance().create(eClass);
        rootEObject.eSet(rootEObject.eClass().getEStructuralFeature(no.hvl.multecore.common.Constants.METAMODELS_ATTRIBUTE_NAME), iModel.getMainMetamodel().getName());
        
        //For supplementary hierarchies
        List<String> supplementaryHierarchies = new ArrayList<String>();
        for (MultilevelHierarchy supplementaryHierarchy : multilevelHierarchy.getSupplementaryHierarchies()) {
        	IModel supplementaryModel = null;
        	supplementaryModel = iModel.getSupplementaryModelForHierarchy(supplementaryHierarchy);
        	if (supplementaryModel != null) {
            	String pairHierarchyModel = "";
            	pairHierarchyModel = pairHierarchyModel + supplementaryHierarchy.getName() + ":" + supplementaryModel.getName();
            	supplementaryHierarchies.add(pairHierarchyModel);
        	}     	
		}
        rootEObject.eSet(rootEObject.eClass().getEStructuralFeature(no.hvl.multecore.common.Constants.SUPPLEMENTARY_METAMODELS_ATTRIBUTE_NAME), supplementaryHierarchies);
        
        newResource.getContents().add(rootEObject);
    }
    
    
    @Override
    protected void addElement (INode iNode) {
        // Since we are creating a model, nodes are transformed into EObjects
    	Node node = (Node) iNode;
        int typeReversePotency = iNode.getModel().getLevel() - iNode.getType().getModel().getLevel();
        String typeName = iNode.getType().getName();
        if ((typeReversePotency>1) && (!typeName.equals(no.hvl.multecore.common.Constants.ECLASS_ID)))
        	typeName = iModel.getMetamodelInLevel(iModel.getLevel() - typeReversePotency).getName() + Constants.IMPORTED_TYPE_SEPARATOR + typeName;
        String parentNodeNames = "";
		for(INode parentNode : node.getParentNodes()) {
			parentNodeNames += parentNode.getName() + no.hvl.multecore.common.Constants.NAMES_SEPARATOR;
        }
        EClass eClass = (EClass) ePackage.getEClassifier(typeName);
        EObject eObject = ePackage.getEFactoryInstance().create(eClass);
        eObject.eSet(eClass.getEStructuralFeature(no.hvl.multecore.common.Constants.NAME_ATTRIBUTE_NAME), iNode.getName());
        eObject.eSet(eClass.getEStructuralFeature(no.hvl.multecore.common.Constants.PARENT_NODES_ATTRIBUTE_NAME), parentNodeNames);
        eObject.eSet(eClass.getEStructuralFeature(no.hvl.multecore.common.Constants.IS_ABSTRACT_ATTRIBUTE_NAME), node.isAbstract());
        Potency nodePotency = iNode.getPotency();
        String startString = String.valueOf(nodePotency.getStart());
        String endString = (nodePotency.getEnd()==no.hvl.multecore.common.Constants.UNBOUNDED)? no.hvl.multecore.common.Constants.POTENCY_ATTRIBUTE_UNBOUNDED_VALUE : String.valueOf(nodePotency.getEnd());
        String depthString = (nodePotency.getDepth()==no.hvl.multecore.common.Constants.UNBOUNDED)? no.hvl.multecore.common.Constants.POTENCY_ATTRIBUTE_UNBOUNDED_VALUE : String.valueOf(nodePotency.getDepth());
		eObject.eSet(eClass.getEStructuralFeature(no.hvl.multecore.common.Constants.POTENCY_ATTRIBUTE_NAME), startString + no.hvl.multecore.common.Constants.POTENCY_SEPARATOR + endString + no.hvl.multecore.common.Constants.POTENCY_SEPARATOR + depthString);
        
        //Supplementary Nodes
        List<String> supplementaryNodes = new ArrayList<String>();
        for (MultilevelHierarchy supplementaryHierarchy : multilevelHierarchy.getSupplementaryHierarchies()) {
			IModel supplementaryModel = null;
        	supplementaryModel = iModel.getSupplementaryModelForHierarchy(supplementaryHierarchy);
			INode supplementaryNode = node.getSupplementaryTypeInModel(supplementaryModel);
			if (supplementaryNode != null) {
            	String pairModelNode = "";
            	pairModelNode = pairModelNode + supplementaryModel.getName() + ":" + supplementaryNode.getName();
            	supplementaryNodes.add(pairModelNode);
			}

        }

        eObject.eSet(eClass.getEStructuralFeature(no.hvl.multecore.common.Constants.SUPPLEMENTARY_NODES_ATTRIBUTE_NAME), supplementaryNodes);

        
        // Add to internal collection
        eObjects.put(iNode.getName(), eObject);
        
        // Add to synthetic Root node
        this.addRootContainment(eObject);
    }

    
    @SuppressWarnings("unchecked")
	@Override
    protected void addRelation (IEdge iEdge) {
        // Since we are creating a model, edges are transformed into instances of EReferences
        EObject sourceEObject = eObjects.get(iEdge.getSource().getName());
        String targetName = iEdge.getTarget().getName();
		EObject targetEObject = eObjects.get(targetName);
        EStructuralFeature relationNames = sourceEObject.eClass().getEStructuralFeature(no.hvl.multecore.common.Constants.RELATION_NAMES_ATTRIBUTE_NAME); 
        int typeReversePotency = iEdge.getModel().getLevel() - iEdge.getType().getModel().getLevel();
        String typeName = iEdge.getType().getName();
        if ((typeReversePotency>1) && (!typeName.equals(no.hvl.multecore.common.Constants.EREFERENCE_ID)))
        	typeName = iModel.getMetamodelInLevel(iModel.getLevel() - typeReversePotency).getName() + Constants.IMPORTED_TYPE_SEPARATOR + typeName + Constants.IMPORTED_TYPE_SEPARATOR + targetEObject.eClass().getName();
        Potency edgePotency = iEdge.getPotency();
        String startString = String.valueOf(edgePotency.getStart());
        String endString = (edgePotency.getEnd()==no.hvl.multecore.common.Constants.UNBOUNDED)? no.hvl.multecore.common.Constants.POTENCY_ATTRIBUTE_UNBOUNDED_VALUE : String.valueOf(edgePotency.getEnd());
        String depthString = (edgePotency.getDepth()==no.hvl.multecore.common.Constants.UNBOUNDED)? no.hvl.multecore.common.Constants.POTENCY_ATTRIBUTE_UNBOUNDED_VALUE : String.valueOf(edgePotency.getDepth());
        String newRelationName = String.format(no.hvl.multecore.common.Constants.RELATION_NAMES_REGEX_FORMAT, typeName, targetName, iEdge.getName(), iEdge.getLowerBound(), iEdge.getUpperBound(), startString, endString, depthString, iEdge.isContainment());
        String relationNamesAttributeValue = (String) sourceEObject.eGet(relationNames);
        if (relationNamesAttributeValue.isEmpty()) {
            sourceEObject.eSet(relationNames, newRelationName);
        } else {
            sourceEObject.eSet(relationNames, relationNamesAttributeValue + no.hvl.multecore.common.Constants.NAMES_SEPARATOR + newRelationName);
        }
        
        EReference eReference = (EReference) sourceEObject.eClass().getEStructuralFeature(typeName);
        
        if (eReference.getUpperBound() > 1 || eReference.getUpperBound() < 0) {
        	EList<EObject> referenceTargets = (EList<EObject>) sourceEObject.eGet(eReference);
        	referenceTargets.add(targetEObject);
        } else {
        	sourceEObject.eSet(eReference, targetEObject);
        }
    }


	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void addAttribute(IAttribute iAttribute) {
		EObject containingEO = eObjects.get(iAttribute.getContainingNode().getName());
		IAttribute typeAttribute = iAttribute.getType();
		if (iAttribute instanceof InstantiatedAttribute) {
			EStructuralFeature eStructuralFeature = containingEO.eClass().getEStructuralFeature(typeAttribute.getNameOrValue());
			if (eStructuralFeature instanceof EList) {
				Debugger.logError("Could not create instance of list attribute " + typeAttribute.getNameOrValue());
				//TODO
			} else {
				containingEO.eSet(eStructuralFeature, ((NativeType) typeAttribute.getType()).getValueWithCorrectType(iAttribute.getNameOrValue()));
			}
		} else  {
			DeclaredAttribute declaredAttribute = (DeclaredAttribute) iAttribute;
			Potency attributePotency = iAttribute.getPotency();
	        String startString = String.valueOf(attributePotency.getStart());
	        String endString = (attributePotency.getEnd()==no.hvl.multecore.common.Constants.UNBOUNDED)? no.hvl.multecore.common.Constants.POTENCY_ATTRIBUTE_UNBOUNDED_VALUE : String.valueOf(attributePotency.getEnd());
			String declaredAttributeString = String.format(no.hvl.multecore.common.Constants.ATTRIBUTE_NAMES_REGEX_FORMAT, iAttribute.getNameOrValue(), typeAttribute.getNameOrValue(), declaredAttribute.getLowerBound(), declaredAttribute.getUpperBound(), startString, endString, declaredAttribute.isId());
			EList attributeEList = (EList) containingEO.eGet(containingEO.eClass().getEStructuralFeature(no.hvl.multecore.common.Constants.ATTRIBUTE_NAMES_ATTRIBUTE_NAME));
			attributeEList.add(declaredAttributeString);
		}
	}


	@SuppressWarnings("unchecked")
	protected void addRootContainment (EObject eObject) {
		EReference eReference = (EReference) rootEObject.eClass().getEStructuralFeature(no.hvl.multecore.common.Constants.SERIALIZATION_ROOT_CONTAINMENT_PREFIX);
		((EList<EObject>) rootEObject.eGet(eReference)).add(eObject);
	}


	private void createEcore () {
		String syntheticMetamodelName = modelName  + Constants.DESERIALIZARION_FILE_SYNTHETIC_METAMODEL_SUFFIX;
		ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName(syntheticMetamodelName);
		ePackage.setNsPrefix(syntheticMetamodelName);
		ePackage.setNsURI(syntheticMetamodelName);

		// Create new single Root node
		EClass rootEClass = EcoreFactory.eINSTANCE.createEClass();
		rootEClass.setName(no.hvl.multecore.common.Constants.NODE_NAME_ROOT);
		addEAttribute(rootEClass, no.hvl.multecore.common.Constants.METAMODELS_ATTRIBUTE_NAME, EcorePackage.eINSTANCE.getEString(), no.hvl.multecore.common.Constants.METAMODELS_ATTRIBUTE_DEFAULT_VALUE, 1, 1);
		addEAttribute(rootEClass, no.hvl.multecore.common.Constants.SUPPLEMENTARY_METAMODELS_ATTRIBUTE_NAME, EcorePackage.eINSTANCE.getEString(), no.hvl.multecore.common.Constants.METAMODELS_ATTRIBUTE_DEFAULT_VALUE, 0, -1);
		ePackage.getEClassifiers().add(rootEClass);

		// Superclass contained by Root and representing EClass, with EReference and synthetic attributes
		eClassEClass = EcoreFactory.eINSTANCE.createEClass();
		eClassEClass.setName(no.hvl.multecore.common.Constants.ECLASS_ID);
		addEAttribute(eClassEClass, no.hvl.multecore.common.Constants.NAME_ATTRIBUTE_NAME, EcorePackage.eINSTANCE.getEString(), no.hvl.multecore.common.Constants.NAME_ATTRIBUTE_DEFAULT_VALUE, 1, 1);
		addEAttribute(eClassEClass, no.hvl.multecore.common.Constants.RELATION_NAMES_ATTRIBUTE_NAME, EcorePackage.eINSTANCE.getEString(), no.hvl.multecore.common.Constants.RELATION_NAMES_ATTRIBUTE_DEFAULT_VALUE, 1, 1);
		addEAttribute(eClassEClass, no.hvl.multecore.common.Constants.POTENCY_ATTRIBUTE_NAME, EcorePackage.eINSTANCE.getEString(), no.hvl.multecore.common.Constants.POTENCY_ATTRIBUTE_DEFAULT_VALUE + no.hvl.multecore.common.Constants.POTENCY_SEPARATOR + no.hvl.multecore.common.Constants.POTENCY_ATTRIBUTE_DEFAULT_VALUE, 1, 1);
		addEAttribute(eClassEClass, no.hvl.multecore.common.Constants.PARENT_NODES_ATTRIBUTE_NAME, EcorePackage.eINSTANCE.getEString(), no.hvl.multecore.common.Constants.PARENT_NODES_ATTRIBUTE_DEFAULT_VALUE, 1, 1);
		addEAttribute(eClassEClass, no.hvl.multecore.common.Constants.IS_ABSTRACT_ATTRIBUTE_NAME, EcorePackage.eINSTANCE.getEBoolean(), no.hvl.multecore.common.Constants.IS_ABSTRACT_ATTRIBUTE_DEFAULT_VALUE, 1, 1);
        addEAttribute(eClassEClass, no.hvl.multecore.common.Constants.ATTRIBUTE_NAMES_ATTRIBUTE_NAME, EcorePackage.eINSTANCE.getEString(), no.hvl.multecore.common.Constants.ATTRIBUTE_NAMES_ATTRIBUTE_DEFAULT_VALUE, 0, -1);
        addEAttribute(eClassEClass, no.hvl.multecore.common.Constants.SUPPLEMENTARY_NODES_ATTRIBUTE_NAME, EcorePackage.eINSTANCE.getEString(), no.hvl.multecore.common.Constants.SUPPLEMENTARY_NODES_ATTRIBUTE_DEFAULT_VALUE, 0, -1);
		EReference eReference = EcoreFactory.eINSTANCE.createEReference();
		eReference.setName(no.hvl.multecore.common.Constants.EREFERENCE_ID);
		eReference.setEType(eClassEClass);
		eReference.setContainment(false);
		eReference.setLowerBound(0);
		eReference.setUpperBound(-1);
		eClassEClass.getEStructuralFeatures().add(eReference);
		eReference = EcoreFactory.eINSTANCE.createEReference();
		eReference.setName(no.hvl.multecore.common.Constants.SERIALIZATION_ROOT_CONTAINMENT_PREFIX);
		eReference.setEType(eClassEClass);
		eReference.setContainment(true);
		eReference.setLowerBound(0);
		eReference.setUpperBound(-1);
		rootEClass.getEStructuralFeatures().add(eReference);
		ePackage.getEClassifiers().add(eClassEClass);

		// Calculate all nodes and edges available for the model due to potency
		IModel model = multilevelHierarchy.getModelByName(modelName);
		Map<INode, Set<IEdge>> availableTypes = multilevelHierarchy.allAvailableTypesInModel(model, true);
		
        // Sorting to make sure that parent nodes are transformed before their children
        List<INode> sortedNodes = new ArrayList<INode>();
        Set<INode> availableTypesKeys = availableTypes.keySet();
		while (sortedNodes.size() < availableTypesKeys.size()) {
        	for (INode iNode : availableTypesKeys) {
        		if (sortedNodes.contains(iNode))
        			continue;
        		else if (sortedNodes.containsAll(iNode.getParentNodes()))
        			sortedNodes.add(iNode);
        	}
        }
        
		// Create all classes (nodes)
		Map<INode, EClass> transformedNodes = new HashMap<INode, EClass>();
		for (INode node : sortedNodes) {
			EClass newEClass = EcoreFactory.eINSTANCE.createEClass();
			String nodeName = node.getName();
			if (!node.getModel().equals(model.getMainMetamodel())) nodeName = node.getModel().getName() + Constants.IMPORTED_TYPE_SEPARATOR + nodeName;
			newEClass.setName(nodeName);
			
			// Record information for the next loop
			transformedNodes.put(node, newEClass);
			
			// Add parent classes
			EList<EClass> eSuperTypes = newEClass.getESuperTypes();
			for(INode parentNode : node.getParentNodes()) {
				eSuperTypes.add(transformedNodes.get(parentNode));
			}
			
			// Only inherit from eClassEClass is it has no parents (otherwise, it will transitively inherit)
			if (eSuperTypes.isEmpty())
				eSuperTypes.add(eClassEClass);
			
			// Add to ePackage
			ePackage.getEClassifiers().add(newEClass);
			
			// Add attributes that can be instantiated
			for (DeclaredAttribute declaredAttribute : node.getDeclaredAttributesPlusInherited(true)) {
				EAttribute eAttribute = EcoreFactory.eINSTANCE.createEAttribute();
				eAttribute.setName(declaredAttribute.getNameOrValue());
				eAttribute.setEType(((NativeType) declaredAttribute.getType()).getEDataType());
				eAttribute.setLowerBound(declaredAttribute.getLowerBound());
				eAttribute.setUpperBound(declaredAttribute.getUpperBound());
				eAttribute.setID(declaredAttribute.isId());
				eAttribute.setUnsettable(true);
				newEClass.getEStructuralFeatures().add(eAttribute);
			}
		}
		
		// Create all references (edges)
		for (INode sourceNode : availableTypesKeys) {
			EClass sourceEClass = transformedNodes.get(sourceNode);
			for (IEdge edge : availableTypes.get(sourceNode)) {
				INode targetNode = edge.getTarget();
				boolean sourceNodeAvailableByPotency = !sourceNode.getModel().equals(model.getMainMetamodel());
				boolean targetNodeAvailableByPotency = !targetNode.getModel().equals(model.getMainMetamodel());
				// The source is available via potency, hence the reference must be replicated for every instance of the target
				if (sourceNodeAvailableByPotency && !targetNodeAvailableByPotency) {
					for (INode targetInstanceCandidate : availableTypesKeys) {
						if (targetInstanceCandidate.getTransitiveTypes().contains(targetNode)) {
							createEReference(edge, sourceEClass, transformedNodes.get(targetNode), model);
						}
					}
				}
				// The target is available via potency, hence the reference must be replicated for every instance of the source
				if (targetNodeAvailableByPotency && !sourceNodeAvailableByPotency) {
					for (INode sourceInstanceCandidate : availableTypesKeys) {
						if (sourceInstanceCandidate.getTransitiveTypes().contains(sourceNode)) {
							createEReference(edge, sourceEClass, transformedNodes.get(targetNode), model);
						}
					}
				}
				// Both source and target are available (via potency or not), and need to have the reference between them
				if (sourceNodeAvailableByPotency == targetNodeAvailableByPotency) {
					createEReference(edge, sourceEClass, transformedNodes.get(targetNode), model);
				}
			}
		}

		ResourceSet resourceSet = new ResourceSetImpl();
		Resource syntheticMetamodelResource = resourceSet.createResource(URI.createURI(ePackage.getNsURI()), syntheticMetamodelName + Constants.FILE_EXTENSION_METAMODEL_WITH_SEPARATOR);
		syntheticMetamodelResource.getContents().add(ePackage);
		EPackage.Registry.INSTANCE.put(ePackage.getNsURI(), ePackage);
	}


	private void createEReference(IEdge edge, EClass sourceEClass, EClass targetEClass, IModel model) {
		EReference newEReference = EcoreFactory.eINSTANCE.createEReference();
		String edgeName = edge.getName();
		if (!edge.getModel().equals(model.getMainMetamodel())) edgeName = edge.getModel().getName() + Constants.IMPORTED_TYPE_SEPARATOR + edgeName + Constants.IMPORTED_TYPE_SEPARATOR + targetEClass.getName();
		newEReference.setName(edgeName);
		newEReference.setEType(targetEClass);
		newEReference.setContainment(edge.isContainment());
		newEReference.setLowerBound(edge.getLowerBound());
		newEReference.setUpperBound(edge.getUpperBound());

		// Add to EClass
		sourceEClass.getEStructuralFeatures().add(newEReference);
	}


	@Override
	public void save() throws IOException {
		// No XMI has been generated if this is the topmost model
		if (isTopMostModel)
			return;

		MultEcoreManager.instance().getModelRegistry().getEntry(modelURI).setModelResource(newResource);
		try {
			super.save();
		} catch (TransformerException e) {
			Debugger.logError("This should never happen: the exception in super.save() is forced by MefFromHierarchyTransformer#save().");
		}
	}

}

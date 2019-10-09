package no.hvl.multecore.core;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;

import no.hvl.multecore.common.Debugger;
import no.hvl.multecore.common.MultEcoreManager;
import no.hvl.multecore.common.hierarchy.DeclaredAttribute;
import no.hvl.multecore.common.hierarchy.DeclaredAttribute.NativeType;
import no.hvl.multecore.common.hierarchy.IAttribute;
import no.hvl.multecore.common.hierarchy.IEdge;
import no.hvl.multecore.common.hierarchy.IModel;
import no.hvl.multecore.common.hierarchy.INode;
import no.hvl.multecore.common.hierarchy.InstantiatedAttribute;
import no.hvl.multecore.common.hierarchy.MultilevelHierarchy;
import no.hvl.multecore.common.hierarchy.Node;
import no.hvl.multecore.common.hierarchy.Potency;

public class MetamodelFromHierarchyTransformer extends AbstractTransformerFromHierarchy {
    
    protected EClass rootEClass, eClassEClass;
    
    public MetamodelFromHierarchyTransformer (URI modelURI, MultilevelHierarchy multilevelHierarchy) {
        super(modelURI, multilevelHierarchy);
        ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setName(this.modelName);
        ePackage.setNsPrefix(this.modelName);
        ePackage.setNsURI(modelName);
        newResource = MultEcoreManager.instance().getModelRegistry().createResource(modelURI.trimFileExtension().appendFileExtension(Constants.FILE_EXTENSION_METAMODEL));
        newResource.getContents().add(ePackage);
    }


    @Override
    protected void initialize() {
        rootEClass = EcoreFactory.eINSTANCE.createEClass();
        rootEClass.setName(no.hvl.multecore.common.Constants.NODE_NAME_ROOT);
        addEAttribute(rootEClass, no.hvl.multecore.common.Constants.METAMODELS_ATTRIBUTE_NAME, EcorePackage.eINSTANCE.getEString(), no.hvl.multecore.common.Constants.METAMODELS_ATTRIBUTE_DEFAULT_VALUE, 1, 1);
        addEAttribute(rootEClass, no.hvl.multecore.common.Constants.SUPPLEMENTARY_METAMODELS_ATTRIBUTE_NAME, EcorePackage.eINSTANCE.getEString(), no.hvl.multecore.common.Constants.METAMODELS_ATTRIBUTE_DEFAULT_VALUE, 0, -1);
        
        // Add its main application metamodel
        EAnnotation eAnnotation = EcoreFactory.eINSTANCE.createEAnnotation();
        eAnnotation.setSource(no.hvl.multecore.common.Constants.ONTOLOGICAL_METAMODEL_PREFIX + no.hvl.multecore.common.Constants.NAMES_ASSOCIATOR + iModel.getMainMetamodel().getName());
        rootEClass.getEAnnotations().add(eAnnotation);
        
        // Add its supplementary metamodels
        eAnnotation = EcoreFactory.eINSTANCE.createEAnnotation();
        eAnnotation.setSource(no.hvl.multecore.common.Constants.LINGUISTIC_METAMODEL_PREFIX);
        for (MultilevelHierarchy supplementaryHierarchy : multilevelHierarchy.getSupplementaryHierarchies()) {
        	IModel supplementaryModel = iModel.getSupplementaryModelForHierarchy(supplementaryHierarchy);
    		String supplementaryModelName = "";
    		// The primitive types model entry pair must always exists  
        	if (supplementaryHierarchy.getName().equals(no.hvl.multecore.common.Constants.PRIMITIVE_TYPES_HIERARCHY))
        		supplementaryModelName = no.hvl.multecore.common.Constants.PRIMITIVE_TYPES_HIERARCHY;
        	else
        		supplementaryModelName = (null == supplementaryModel)? "" : supplementaryModel.getName();
        	eAnnotation.getDetails().put(supplementaryHierarchy.getName(), supplementaryModelName);
        }
        rootEClass.getEAnnotations().add(eAnnotation);
        
        ePackage.getEClassifiers().add(rootEClass);

		// Superclass contained by Root and representing EClass, with EReference and synthetic attributes
		eClassEClass = EcoreFactory.eINSTANCE.createEClass();
		eClassEClass.setName(no.hvl.multecore.common.Constants.ECLASS_ID);
		addEAttribute(eClassEClass, no.hvl.multecore.common.Constants.NAME_ATTRIBUTE_NAME, EcorePackage.eINSTANCE.getEString(), no.hvl.multecore.common.Constants.NAME_ATTRIBUTE_DEFAULT_VALUE, 1, 1);
		addEAttribute(eClassEClass, no.hvl.multecore.common.Constants.RELATION_NAMES_ATTRIBUTE_NAME, EcorePackage.eINSTANCE.getEString(), no.hvl.multecore.common.Constants.RELATION_NAMES_ATTRIBUTE_DEFAULT_VALUE, 1, 1);
		addEAttribute(eClassEClass, no.hvl.multecore.common.Constants.POTENCY_ATTRIBUTE_NAME, EcorePackage.eINSTANCE.getEString(), no.hvl.multecore.common.Constants.POTENCY_ATTRIBUTE_DEFAULT_VALUE + no.hvl.multecore.common.Constants.POTENCY_SEPARATOR + no.hvl.multecore.common.Constants.POTENCY_ATTRIBUTE_DEFAULT_VALUE + no.hvl.multecore.common.Constants.POTENCY_SEPARATOR + no.hvl.multecore.common.Constants.POTENCY_ATTRIBUTE_UNBOUNDED_VALUE, 1, 1);
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
    }
    
    
    @Override
    protected void addElement (INode iNode) {
    	Node node = (Node) iNode;
        // Since we are creating a metamodel, nodes are transformed into EClasses
        EClass eClass = EcoreFactory.eINSTANCE.createEClass();
        eClass.setName(iNode.getName());
        EList<EClass> eSuperTypes = eClass.getESuperTypes();
		for(INode parentNode : node.getParentNodes()) {
        	eSuperTypes.add((EClass) ePackage.getEClassifier(parentNode.getName()));
        }
		// Only inherit from eClassEClass is it has no parents (otherwise, it will transitively inherit)
		if (eSuperTypes.isEmpty())
			eSuperTypes.add(eClassEClass);
        
        int typeReversePotency = iNode.getModel().getLevel() - iNode.getType().getModel().getLevel();
        String typeName = iNode.getType().getName();
        if ((typeReversePotency > 1) && (!typeName.equals(no.hvl.multecore.common.Constants.ECLASS_ID)))
        	typeName += no.hvl.multecore.common.Constants.TYPE_POTENCY_SEPARATOR + typeReversePotency;
        EAnnotation eAnnotation = EcoreFactory.eINSTANCE.createEAnnotation();
        eAnnotation.setSource(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_TYPE + no.hvl.multecore.common.Constants.NAMES_ASSOCIATOR + typeName);
        eClass.getEAnnotations().add(eAnnotation);
        //Supplementary types for Nodes
        eAnnotation = EcoreFactory.eINSTANCE.createEAnnotation();
        eClass.getEAnnotations().add(eAnnotation);
        eAnnotation.setSource(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_SUPTYPES);
        for (IModel supplementaryModel : iNode.getModel().getSupplementaryModels()) {
        	INode supplementaryNode = iNode.getSupplementaryTypeInModel(supplementaryModel);
        	String supplementaryNodeName = (null == supplementaryNode)? "" : supplementaryNode.getName();
        	eAnnotation.getDetails().put(supplementaryModel.getName(), supplementaryNodeName);
        }
        eClass.getEAnnotations().add(eAnnotation);

        Potency nodePotency = iNode.getPotency();
        eAnnotation = EcoreFactory.eINSTANCE.createEAnnotation();
        String endPotencyString = (no.hvl.multecore.common.Constants.UNBOUNDED == nodePotency.getEnd())? no.hvl.multecore.common.Constants.POTENCY_ATTRIBUTE_UNBOUNDED_VALUE : String.valueOf(nodePotency.getEnd());
        String depthPotencyString = (no.hvl.multecore.common.Constants.UNBOUNDED == nodePotency.getDepth())? no.hvl.multecore.common.Constants.POTENCY_ATTRIBUTE_UNBOUNDED_VALUE : String.valueOf(nodePotency.getDepth());
		eAnnotation.setSource(Constants.DESERIALIZATION_ATTRIBUTE_NAME_POTENCY + no.hvl.multecore.common.Constants.NAMES_ASSOCIATOR + nodePotency.getStart() + no.hvl.multecore.common.Constants.POTENCY_SEPARATOR + endPotencyString  + no.hvl.multecore.common.Constants.POTENCY_SEPARATOR + depthPotencyString);
        eClass.getEAnnotations().add(eAnnotation);
        
        eClass.setAbstract(node.isAbstract());
        
        // Add to EPackage
        ePackage.getEClassifiers().add(eClass);
    }

    
    @Override
    protected void addRelation (IEdge iEdge) {
        // Since we are creating a metamodel, edges are transformed into EReferences
        EClass sourceEClass = (EClass) ePackage.getEClassifier(iEdge.getSource().getName());
        EClass targetEClass = (EClass) ePackage.getEClassifier(iEdge.getTarget().getName());
        EReference eReference = EcoreFactory.eINSTANCE.createEReference();
        eReference.setName(iEdge.getName());
        eReference.setEType(targetEClass);
        eReference.setContainment(iEdge.isContainment());
        eReference.setLowerBound(iEdge.getLowerBound());
        eReference.setUpperBound(iEdge.getUpperBound());
        
        int typeReversePotency = iEdge.getModel().getLevel() - iEdge.getType().getModel().getLevel();
        String typeName = iEdge.getType().getName();
        if ((typeReversePotency > 1) && (!typeName.equals(no.hvl.multecore.common.Constants.EREFERENCE_ID)))
        	typeName += no.hvl.multecore.common.Constants.TYPE_POTENCY_SEPARATOR + typeReversePotency;
        EAnnotation eAnnotation = EcoreFactory.eINSTANCE.createEAnnotation();
        eAnnotation.setSource(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_TYPE + no.hvl.multecore.common.Constants.NAMES_ASSOCIATOR + typeName);
        eReference.getEAnnotations().add(eAnnotation);

        Potency edgePotency = iEdge.getPotency();
        eAnnotation = EcoreFactory.eINSTANCE.createEAnnotation();
        String endPotencyString = (no.hvl.multecore.common.Constants.UNBOUNDED == edgePotency.getEnd())? no.hvl.multecore.common.Constants.POTENCY_ATTRIBUTE_UNBOUNDED_VALUE : String.valueOf(edgePotency.getEnd());
        String depthPotencyString = (no.hvl.multecore.common.Constants.UNBOUNDED == edgePotency.getDepth())? no.hvl.multecore.common.Constants.POTENCY_ATTRIBUTE_UNBOUNDED_VALUE : String.valueOf(edgePotency.getDepth());
        eAnnotation.setSource(Constants.DESERIALIZATION_ATTRIBUTE_NAME_POTENCY + no.hvl.multecore.common.Constants.NAMES_ASSOCIATOR + edgePotency.getStart() + no.hvl.multecore.common.Constants.POTENCY_SEPARATOR + endPotencyString + no.hvl.multecore.common.Constants.POTENCY_SEPARATOR + depthPotencyString);
        eReference.getEAnnotations().add(eAnnotation);
        
        // Add to EClass
        sourceEClass.getEStructuralFeatures().add(eReference);
    }
    
    
    @Override
    protected void addAttribute(IAttribute iAttribute) {
    	EAttribute eAttribute = EcoreFactory.eINSTANCE.createEAttribute();
    	if (iAttribute instanceof InstantiatedAttribute) {
    		eAttribute.setName(iAttribute.getType().getNameOrValue());
    		EAnnotation valueEA = EcoreFactory.eINSTANCE.createEAnnotation();
    		valueEA.setSource(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_VALUE);
    		valueEA.getDetails().put(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_VALUE, iAttribute.getNameOrValue());
    		eAttribute.getEAnnotations().add(valueEA);
    		// Default, unused values for instantiated attributes
        	eAttribute.setEType(EcorePackage.eINSTANCE.getEJavaObject());
        	eAttribute.setLowerBound(0);
        	eAttribute.setUpperBound(-2);
    	} else {
    		DeclaredAttribute declaredAttribute = (DeclaredAttribute) iAttribute;
    		eAttribute.setName(iAttribute.getNameOrValue());
        	EAnnotation potencyEA = EcoreFactory.eINSTANCE.createEAnnotation();
        	potencyEA.setSource(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY);
        	Potency attributePotency = iAttribute.getPotency();
			potencyEA.getDetails().put(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_START, String.valueOf(attributePotency.getStart()));
        	String endPotencyString = (no.hvl.multecore.common.Constants.UNBOUNDED == attributePotency.getEnd())? no.hvl.multecore.common.Constants.POTENCY_ATTRIBUTE_UNBOUNDED_VALUE : String.valueOf(attributePotency.getEnd());
        	potencyEA.getDetails().put(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_END, endPotencyString);
    		eAttribute.getEAnnotations().add(potencyEA);
			eAttribute.setID(declaredAttribute.isId());
        	eAttribute.setEType(((NativeType) iAttribute.getType()).getEDataType());
        	eAttribute.setLowerBound(declaredAttribute.getLowerBound());
        	eAttribute.setUpperBound(declaredAttribute.getUpperBound());
    	}
    	((EClass) ePackage.getEClassifier(iAttribute.getContainingNode().getName())).getEStructuralFeatures().add(eAttribute);
    }
    

    public void save () throws IOException {
    	MultEcoreManager.instance().getModelRegistry().getEntry(modelURI).setMetamodelResource(newResource);
    	try {
			super.save();
		} catch (TransformerException e) {
			Debugger.logError("This should never happen: the exception in super.save() is forced by MefFromHierarchyTransformer#save().");
		}
    }
    
}

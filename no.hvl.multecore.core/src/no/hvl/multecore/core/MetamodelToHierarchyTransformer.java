package no.hvl.multecore.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import no.hvl.multecore.common.Constants;
import no.hvl.multecore.common.Debugger;
import no.hvl.multecore.common.MultEcoreManager;
import no.hvl.multecore.common.exceptions.MultEcoreException;
import no.hvl.multecore.common.hierarchy.DeclaredAttribute;
import no.hvl.multecore.common.hierarchy.DeclaredAttribute.NativeType;
import no.hvl.multecore.common.hierarchy.Edge;
import no.hvl.multecore.common.hierarchy.MultilevelHierarchy;
import no.hvl.multecore.common.registry.ModelRegistryEntry;

public class MetamodelToHierarchyTransformer extends AbstractTransformerToHierarchy {
    
	private EClass rootEClass;
    private EPackage ePackage;
    
    
    public MetamodelToHierarchyTransformer (URI modelURI, MultilevelHierarchy multilevelHierarchy) {
    	super(modelURI, multilevelHierarchy);
    	getMetamodelName();
    }


	@Override
	public String getMetamodelName() {
		if (null != metamodelName)
			return metamodelName;
		metamodelName = Constants.ECORE_ID;
		ModelRegistryEntry mre = MultEcoreManager.instance().getModelRegistry().getEntry(modelURI);
		if (null == mre)
			return metamodelName;
		ePackage = (EPackage) mre.getMetamodelResource().getContents().get(0);
		rootEClass = (EClass) ePackage.getEClassifier(Constants.NODE_NAME_ROOT);
		if (null == rootEClass)
			return metamodelName;
		if (rootEClass.getEAnnotations().isEmpty())
			return metamodelName;
		for (EAnnotation eAnnotation : rootEClass.getEAnnotations()) {
			String[] fragments = eAnnotation.getSource().split(Constants.NAMES_ASSOCIATOR);
			if (fragments[0].equals(Constants.ONTOLOGICAL_METAMODEL_PREFIX)) {
				metamodelName = fragments[1];
				break;
			}
		}
		return metamodelName;
	}

    
    public void transform () throws MultEcoreException {
        super.transform();
        
        // Add supplementary models
        EAnnotation supplementaryModelsEA = (null == rootEClass)? null : rootEClass.getEAnnotation(Constants.LINGUISTIC_METAMODEL_PREFIX);
        EMap<String, String> supplementaryModelsMap = null;
        if ((null != supplementaryModelsEA) && (null != (supplementaryModelsMap = supplementaryModelsEA.getDetails()))) {
        	Set<String> hierarchyNames = supplementaryModelsMap.keySet();
        	for (String hierarchyName : hierarchyNames) {
        		String supplementaryModelName = supplementaryModelsMap.get(hierarchyName);
        		if (!supplementaryModelName.isEmpty())
        			multilevelHierarchy.addSupplementaryModel(iModelUpdate, hierarchyName, supplementaryModelName);
        	}
        }
        
        // Create nodes
        EList<EClassifier> eClassList = ePackage.getEClassifiers();
        Predicate<EClassifier> isEClass = (Predicate<EClassifier>) Utils::isEClass;
        Predicate<EClassifier> isRoot = (Predicate<EClassifier>) Utils::isRoot;
        Predicate<EClassifier> isEClassEClass = (Predicate<EClassifier>) Utils::isEClassEClass;
        eClassList.removeIf((isEClass).negate().or(isRoot).or(isEClassEClass));
        for (EClassifier eClass : eClassList) {
        	EClass ec = (EClass) eClass;
        	transform(ec);
        }
        
        
        
        // Create relations
        EList<EReference> eReferences = new BasicEList<EReference>();
        eClassList.forEach((eClass) -> {
            eReferences.addAll(((EClass) eClass).getEReferences());
        });
        for (EReference eReference : eReferences) {
        	transform((EReference) eReference);
        }
        
        // Update the model to the new version (or create)
        if (null != iModel) {
        	multilevelHierarchy.updateModel(iModel, iModelUpdate);
        }
    }
    

    private void transform (EClass eClass) throws MultEcoreException {
        String name = eClass.getName();
        String typeName = Constants.NODE_TYPE_ATTRIBUTE_DEFAULT_VALUE;
        String typeNameString = typeName;
        int startPotency = Constants.START_POTENCY_DEFAULT_VALUE;
        int endPotency = Constants.END_POTENCY_DEFAULT_VALUE;
        int depthPotency = Constants.DEPTH_POTENCY_DEFAULT_VALUE;
        int typeReversePotency = Constants.POTENCY_DEFAULT_VALUE;

        for(EAnnotation e : eClass.getEAnnotations()) {
        	String source = e.getSource();
        	if(source.startsWith(Constants.SERIALIZATION_ATTRIBUTE_NAME_TYPE)) {
        		typeNameString = source.split(Constants.NAMES_ASSOCIATOR)[1];
    		    typeReversePotency = no.hvl.multecore.common.Utils.getTypeReversePotency(typeNameString);
    		    typeName = no.hvl.multecore.common.Utils.getTypeName(typeNameString);
        	}
        	
        	
        	if(source.startsWith(Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY)) {
        	    String[] fragments = source.split(Constants.NAMES_ASSOCIATOR);
        	    if (fragments.length == 2) {
        	    	String[] potencies = fragments[1].split(Constants.POTENCY_SEPARATOR);
        	    	startPotency = Integer.parseInt(potencies[0]);
        	    	String endPotencyString = potencies[1];
					endPotency = (endPotencyString.equals(Constants.POTENCY_ATTRIBUTE_UNBOUNDED_VALUE))? Constants.UNBOUNDED : Integer.parseInt(endPotencyString);
        	    	String depthPotencyString = potencies[2];
        	    	depthPotency = (depthPotencyString.equals(Constants.POTENCY_ATTRIBUTE_UNBOUNDED_VALUE))? Constants.UNBOUNDED : Integer.parseInt(depthPotencyString);
        	    	if (((startPotency > endPotency) && (endPotency != Constants.UNBOUNDED)) || startPotency < 0 || potencies.length != 3) {
        	    		Debugger.logError("Format error in potency of element " + modelName + "::" + eClass.getName());
					}
        	    }
        	    else {
        	    	Debugger.logError("Format error in potency of element " + modelName + "::" + eClass.getName());
        	    }
        	}
        }

        Set<String> parentNodeNames = new HashSet<String>();
        for(EClass ec : eClass.getESuperTypes()) {
        	String parentNodeName = ec.getName();
        	if (!parentNodeName.equals(Constants.ECLASS_ID))
        		parentNodeNames.add(parentNodeName);
        }
        
        Map <String, String> supplementaryNodesMap = new HashMap <String,String>();
        //EMap<String, String> supplementaryNodesMap = null;
        EAnnotation supplementaryNodesEA = (null == eClass)? null : eClass.getEAnnotation(Constants.SERIALIZATION_ATTRIBUTE_NAME_SUPTYPES);
        if (supplementaryNodesEA!=null) {
            for (Entry<String,String>  pairSupp : supplementaryNodesEA.getDetails()) {
				supplementaryNodesMap.put(pairSupp.getKey(), pairSupp.getValue());
			}
        }
        attemptToAddNode(new PendingNode(eClass, name, typeName, typeReversePotency, startPotency, endPotency, depthPotency, parentNodeNames, eClass.isAbstract(), supplementaryNodesMap));
        
    }
    
    
    @Override
    protected void addNode(PendingNode pendingNode) throws MultEcoreException {
    	// Create node in model
    	no.hvl.multecore.common.hierarchy.Node node = multilevelHierarchy.addNode(pendingNode.nodeName, pendingNode.typeName, pendingNode.typeReversePotency, pendingNode.startPotency, pendingNode.endPotency, pendingNode.depthPotency, pendingNode.parentNodeNames, iModelUpdate);
    	node.setAbstract(pendingNode.isAbstract);
        
        // Add supplementary nodes
        if ((null != pendingNode.supplementaryNodesMap)) {
        	Set<String> modelNames = pendingNode.supplementaryNodesMap.keySet();
        	for (String modelName : modelNames) {
        		String supplementaryNodeName = pendingNode.supplementaryNodesMap.get(modelName);
        		if (!supplementaryNodeName.isEmpty())
        			multilevelHierarchy.addSupplementaryNode (node, modelName, supplementaryNodeName);
        	}
        }
    	
        // Create all attributes of the class
		for (EAttribute ea : ((EClass) pendingNode.originalNode).getEAttributes()) {
        	transform(ea);
        }
    }
    
    
    private void transform (EReference eReference) throws MultEcoreException {
        String name = eReference.getName();
        String sourceEClassName = ((EClass) eReference.eContainer()).getName();
        String targetEClassName = ((EClass) eReference.getEReferenceType()).getName();
        boolean containment = eReference.isContainment();
        int upperBound = eReference.getUpperBound();
        int lowerBound = eReference.getLowerBound();

        String typeName = Constants.RELATION_TYPE_ATTRIBUTE_DEFAULT_VALUE;
        String typeNameString = typeName;
        int startPotency = Constants.START_POTENCY_DEFAULT_VALUE;
        int endPotency = Constants.END_POTENCY_DEFAULT_VALUE;
        int depthPotency = Constants.DEPTH_POTENCY_DEFAULT_VALUE;
        int typeReversePotency = Constants.POTENCY_DEFAULT_VALUE;
        
        for(EAnnotation e : eReference.getEAnnotations()) {
            String source = e.getSource();
            if(source.startsWith(Constants.SERIALIZATION_ATTRIBUTE_NAME_TYPE)) {
        		typeNameString = source.split(Constants.NAMES_ASSOCIATOR)[1];
    		    typeReversePotency = no.hvl.multecore.common.Utils.getTypeReversePotency(typeNameString);
    		    typeName = no.hvl.multecore.common.Utils.getTypeName(typeNameString);
            }
        	if(source.startsWith(no.hvl.multecore.core.Constants.DESERIALIZATION_ATTRIBUTE_NAME_POTENCY)) {
        	    String[] fragments = source.split(Constants.NAMES_ASSOCIATOR);
        	    if (fragments.length == 2) {
        	    	String[] potencies = fragments[1].split(Constants.POTENCY_SEPARATOR);
        	    	startPotency = Integer.parseInt(potencies[0]);
        	    	String endPotencyString = potencies[1];
					endPotency = (endPotencyString.equals(Constants.POTENCY_ATTRIBUTE_UNBOUNDED_VALUE))? Constants.UNBOUNDED : Integer.parseInt(endPotencyString);
        	    	String depthPotencyString = potencies[2];
        	    	depthPotency = (depthPotencyString.equals(Constants.POTENCY_ATTRIBUTE_UNBOUNDED_VALUE))? Constants.UNBOUNDED : Integer.parseInt(depthPotencyString);
        	    	if (((startPotency > endPotency) && (endPotency != Constants.UNBOUNDED)) || potencies.length != 3) {
        	    		Debugger.logError("Format error in potency of element " + modelName + "::" + name);
					}
        	    }
        	    else {
        	    	Debugger.logError("Format error in potency of element " + modelName + "::" + name);
        	    }
        	}
        }
        
        // Update runtime version of the model in the hierarchy
        Edge edge = multilevelHierarchy.addEdge(name, typeName, typeReversePotency, sourceEClassName, targetEClassName, startPotency, endPotency, depthPotency, iModelUpdate);
        edge.setLowerBound(lowerBound);
        edge.setUpperBound(upperBound);
        edge.setContainment(containment);
    }
    
    
    private void transform (EAttribute eAttribute) throws MultEcoreException {
    	String name = eAttribute.getName();
    	if (name.startsWith(Constants.SYNTHETIC_PREFIX))
    		return;

    	String containingNodeName = ((EClass) eAttribute.eContainer()).getName();
    	EAnnotation valueEAnnotation = eAttribute.getEAnnotation(Constants.SERIALIZATION_ATTRIBUTE_NAME_VALUE);
    	boolean isInstantiated = (null != valueEAnnotation); // If it has a value, it is instantiated
		if (isInstantiated) { // It is an instantiated attribute
    		String typeName = eAttribute.getName();
    		String value = valueEAnnotation.getDetails().get(Constants.SERIALIZATION_ATTRIBUTE_NAME_VALUE);
    		multilevelHierarchy.addInstantiatedAttribute(value, typeName, containingNodeName, iModelUpdate);
    	} else { // It is an non-instantiated attribute
    		int startPotency = Constants.START_POTENCY_DEFAULT_VALUE;
    		int endPotency = Constants.END_POTENCY_DEFAULT_VALUE;
    		EAnnotation potencyEAnnotation = eAttribute.getEAnnotation(no.hvl.multecore.core.Constants.DESERIALIZATION_ATTRIBUTE_NAME_POTENCY);
    		if (potencyEAnnotation != null) {
    			startPotency = Integer.parseInt(potencyEAnnotation.getDetails().get(Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_START));
    			String endPotencyString = potencyEAnnotation.getDetails().get(Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_END);
    			endPotency = (endPotencyString.equals(Constants.POTENCY_ATTRIBUTE_UNBOUNDED_VALUE))? Constants.UNBOUNDED : Integer.parseInt(endPotencyString);
    		}

    		// Update runtime version of the model in the hierarchy
    		DeclaredAttribute attribute = multilevelHierarchy.addDeclaredAttribute(eAttribute.getName(), NativeType.getNativeType((EDataType) eAttribute.getEType()), containingNodeName, startPotency, endPotency, iModelUpdate);
    		attribute.setLowerBound(eAttribute.getLowerBound());
    		attribute.setUpperBound(eAttribute.getUpperBound());
    		attribute.setId(eAttribute.isID());
    	}
    }

}

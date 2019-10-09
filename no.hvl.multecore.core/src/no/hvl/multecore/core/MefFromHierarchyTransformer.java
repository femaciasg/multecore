package no.hvl.multecore.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.emf.common.util.URI;
import org.w3c.dom.Element;

import no.hvl.multecore.common.Debugger;
import no.hvl.multecore.common.MultEcoreManager;
import no.hvl.multecore.common.hierarchy.DeclaredAttribute;
import no.hvl.multecore.common.hierarchy.EcoreModel;
import no.hvl.multecore.common.hierarchy.IAttribute;
import no.hvl.multecore.common.hierarchy.IEdge;
import no.hvl.multecore.common.hierarchy.IModel;
import no.hvl.multecore.common.hierarchy.INode;
import no.hvl.multecore.common.hierarchy.InstantiatedAttribute;
import no.hvl.multecore.common.hierarchy.MultilevelHierarchy;
import no.hvl.multecore.common.hierarchy.Node;
import no.hvl.multecore.common.hierarchy.Potency;

public class MefFromHierarchyTransformer extends AbstractTransformerFromHierarchy {

    private Element model, elements, relations;
    private Map<String, Element> elementNodes;
    
    
    public MefFromHierarchyTransformer (URI modelURI, MultilevelHierarchy multilevelHierarchy) {
        super(modelURI, multilevelHierarchy);
        newResource = MultEcoreManager.instance().getModelRegistry().createResource(modelURI.trimFileExtension().appendFileExtension(Constants.FILE_EXTENSION_MEF));
        elementNodes = new HashMap<String, Element>();
    }
    

	@SuppressWarnings("null")
	@Override
	protected void initialize() {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			Debugger.logError("Could not generate MEF for " + modelName);
			return;
		}
		document = documentBuilder.newDocument();
		
		// Create model container
		model = document.createElement(no.hvl.multecore.common.Constants.NODE_NAME_MODEL);
		IModel mainMetamodel = iModel.getMainMetamodel();
		URI metamodelURI = null;
		if (mainMetamodel instanceof EcoreModel) {
			metamodelURI = URI.createURI(no.hvl.multecore.common.Constants.ECORE_ID);
		} else {
			metamodelURI = MultEcoreManager.instance().getModelRegistry().getEntry(mainMetamodel).getUri(false).deresolve(baseURI.appendSegment("")).trimFileExtension();
		}
		
		
		model.setAttribute(no.hvl.multecore.common.Constants.ONTOLOGICAL_METAMODEL_PREFIX, metamodelURI.toFileString().replaceAll("\\\\", "/"));

		//Adding lm to MEF
		String supplementaryHierarchies ="";
		MultilevelHierarchy[] mh =  multilevelHierarchy.getSupplementaryHierarchies().toArray(new MultilevelHierarchy[ multilevelHierarchy.getSupplementaryHierarchies().size()]);
		IModel supplementaryModel = null;
		for (int i = 0; i < mh.length-1; i++) {
			supplementaryModel = iModel.getSupplementaryModelForHierarchy(mh[i]);
			if (supplementaryModel != null) {
				supplementaryHierarchies = supplementaryHierarchies + mh[i].getName() + 
						no.hvl.multecore.common.Constants.SUPPLEMENTARY_SEPARATOR + supplementaryModel.getName() + 
						no.hvl.multecore.common.Constants.NAMES_SEPARATOR;	
			}
			
		}
		if (mh.length>0) {
			supplementaryModel =iModel.getSupplementaryModelForHierarchy(mh[mh.length-1]);
			if (supplementaryModel != null) {
				supplementaryHierarchies = supplementaryHierarchies + mh[mh.length-1].getName()  + 
						no.hvl.multecore.common.Constants.SUPPLEMENTARY_SEPARATOR + supplementaryModel.getName();
			model.setAttribute(no.hvl.multecore.common.Constants.LINGUISTIC_METAMODEL_PREFIX, supplementaryHierarchies);
			}
		}
			
	

		document.appendChild(model);
			
		// Create elements container
		elements = document.createElement(no.hvl.multecore.common.Constants.NODE_NAME_ELEMENTS);
		model.appendChild(elements);
		
		// Create relations container
		relations = document.createElement(no.hvl.multecore.common.Constants.NODE_NAME_RELATIONS);
		model.appendChild(relations);
		
	}


    @Override
    protected void addElement (INode iNode) {
        Node node = (Node) iNode;
        String parentNodesAttribute = "";
        for(INode parentNode : node.getParentNodes()) {
        	parentNodesAttribute = parentNodesAttribute + parentNode.getName() + no.hvl.multecore.common.Constants.NAMES_SEPARATOR;
        }
        INode typeNode = iNode.getType();
        String typeName = typeNode.getName();
        int typeReversePotency = iNode.getModel().getLevel() - typeNode.getModel().getLevel();
        if ((typeReversePotency > 1) && (!typeName.equals(no.hvl.multecore.common.Constants.ECLASS_ID)))
        	typeName += no.hvl.multecore.common.Constants.TYPE_POTENCY_SEPARATOR + typeReversePotency;
        Potency nodePotency = iNode.getPotency();
        Element element = document.createElement(iNode.getName());
        element.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_IS_ABSTRACT, String.valueOf(node.isAbstract()));
        element.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_TYPE, typeName);
        element.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_START, String.valueOf(nodePotency.getStart()));
        element.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_END, String.valueOf(nodePotency.getEnd()));
        element.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_DEPTH, String.valueOf(nodePotency.getDepth()));
        element.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_PARENT_NODES, parentNodesAttribute);
        
        //Adding supplementary types to nodes
		MultilevelHierarchy[] mh =  multilevelHierarchy.getSupplementaryHierarchies().toArray(new MultilevelHierarchy[ multilevelHierarchy.getSupplementaryHierarchies().size()]);
		for (int i = 0; i < mh.length; i++) {
			IModel supplementaryModel = null;
			supplementaryModel = iModel.getSupplementaryModelForHierarchy(mh[i]);
			INode supplementaryNode = node.getSupplementaryTypeInModel(supplementaryModel);
			if (supplementaryNode != null) {
				element.setAttribute(supplementaryModel.getName(), supplementaryNode.getName());
			}	
		}    
        
        elements.appendChild(element);
        elementNodes.put(iNode.getName(), element);
    }


    @Override
    protected void addRelation (IEdge iEdge) {
		IEdge typeEdge = iEdge.getType();
		String typeName = typeEdge.getName();
        int typeReversePotency = iEdge.getModel().getLevel() - typeEdge.getModel().getLevel();
        if ((typeReversePotency > 1) && (!typeName.equals(no.hvl.multecore.common.Constants.EREFERENCE_ID)))
        	typeName += no.hvl.multecore.common.Constants.TYPE_POTENCY_SEPARATOR + typeReversePotency;
        Potency edgePotency = iEdge.getPotency();
        Element element = document.createElement(iEdge.getName());
        element.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_SOURCE, iEdge.getSource().getName());
        element.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_TARGET, iEdge.getTarget().getName());
		element.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_CONTAINMENT, String.valueOf(iEdge.isContainment()));
		element.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_UPPERBOUND, String.valueOf(iEdge.getUpperBound()));
		element.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_LOWERBOUND, String.valueOf(iEdge.getLowerBound()));
        element.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_TYPE, typeName);
        element.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_START, String.valueOf(edgePotency.getStart()));
        element.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_END, String.valueOf(edgePotency.getEnd()));
        element.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_DEPTH, String.valueOf(edgePotency.getDepth()));
        
        relations.appendChild(element);
    }

    
    @Override
    protected void addAttribute(IAttribute iAttribute) {
    	Element containingNodeElement = elementNodes.get(iAttribute.getContainingNode().getName());
    	if (iAttribute instanceof InstantiatedAttribute) {
    		// Instantiated attribute
    		Element attributeElement = document.createElement(iAttribute.getType().getNameOrValue());
    		attributeElement.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_VALUE, iAttribute.getNameOrValue());
    		containingNodeElement.appendChild(attributeElement);
    	} else {
    		// Declared attribute
    		DeclaredAttribute declaredAttribute = (DeclaredAttribute) iAttribute;
    		Potency attributePotency = iAttribute.getPotency();
    		Element attributeElement = document.createElement(iAttribute.getNameOrValue());
    		attributeElement.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_TYPE, iAttribute.getType().getNameOrValue());
    		attributeElement.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_LOWERBOUND, String.valueOf(declaredAttribute.getLowerBound()));
    		attributeElement.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_UPPERBOUND, String.valueOf(declaredAttribute.getUpperBound()));
    		attributeElement.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_START, String.valueOf(attributePotency.getStart()));
    		attributeElement.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_END, String.valueOf(attributePotency.getEnd()));
    		attributeElement.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_DEPTH, String.valueOf(attributePotency.getDepth()));
    		attributeElement.setAttribute(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_IS_ID, String.valueOf(declaredAttribute.isId()));
    		containingNodeElement.appendChild(attributeElement);
    	}
    }
   
    
    @Override
    public void save() throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        DOMSource source = new DOMSource(document);
        File newMefFile = new File(baseURI.toFileString() + Constants.URI_SEPARATOR + modelName + Constants.FILE_EXTENSION_MEF_WITH_SEPARATOR);
		StreamResult result = new StreamResult(newMefFile);
        javax.xml.transform.Transformer transformer = null;
        transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(source, result);
        
		// Update the resource in the model registry
        MultEcoreManager.instance().getModelRegistry().getEntry(modelURI).setMefResource(newResource);
    }

}
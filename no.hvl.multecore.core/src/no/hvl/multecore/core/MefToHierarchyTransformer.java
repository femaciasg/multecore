package no.hvl.multecore.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import no.hvl.multecore.common.Debugger;
import no.hvl.multecore.common.MultEcoreManager;
import no.hvl.multecore.common.exceptions.MultEcoreException;
import no.hvl.multecore.common.hierarchy.DeclaredAttribute;
import no.hvl.multecore.common.hierarchy.DeclaredAttribute.NativeType;
import no.hvl.multecore.common.hierarchy.Edge;
import no.hvl.multecore.common.hierarchy.IModel;
import no.hvl.multecore.common.hierarchy.INode;
import no.hvl.multecore.common.hierarchy.Model;
import no.hvl.multecore.common.hierarchy.MultilevelHierarchy;
import no.hvl.multecore.common.registry.ModelRegistryEntry;

public class MefToHierarchyTransformer extends AbstractTransformerToHierarchy {
    
    protected EPackage ePackage;
    protected DocumentBuilder documentBuilder;
    
    
    public MefToHierarchyTransformer (URI modelURI, MultilevelHierarchy multilevelHierarchy) throws ParserConfigurationException {
        super(modelURI, multilevelHierarchy);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        getMetamodelName();
    }
    
    
    @Override
    public String getMetamodelName() {
    	if (null != metamodelName)
    		return metamodelName;
    	metamodelName = no.hvl.multecore.common.Constants.ECORE_ID; // Default value
    	Resource mefResource = MultEcoreManager.instance().getModelRegistry().getEntry(modelURI).getMefResource();
    	if (null == mefResource)
    		return metamodelName;
    	try {
			document = documentBuilder.parse(new File(mefResource.getURI().toFileString()));
		} catch (SAXException | IOException e) {
			Debugger.logError("Error in Deserializer.findMetamodels()");
		}
    	NamedNodeMap metamodelsAttribute = document.getFirstChild().getAttributes();
    	if (metamodelsAttribute.getLength() > 0) {
    		Node n = metamodelsAttribute.getNamedItem(no.hvl.multecore.common.Constants.ONTOLOGICAL_METAMODEL_PREFIX);
    		URI relativeURI = URI.createURI(n.getNodeValue());
    		metamodelName = relativeURI.lastSegment();
    	}
    	return metamodelName;
    }
    
    
    @Override
    public void transform () throws MultEcoreException {
    	ModelRegistryEntry mre = MultEcoreManager.instance().getModelRegistry().getEntry(modelURI);
    	if (null == mre) {
			Debugger.logError("Error in Deserializer.transform()");
    		return;
    	}
    	try {
			document = documentBuilder.parse(new File(mre.getMefResource().getURI().toFileString()));
		} catch (SAXException | IOException e) {
			Debugger.logError("Error in Deserializer.transform()");
		}
    	super.transform();
        this.transform(document);
    }


    protected void transform (Node node) {
        int type = node.getNodeType();
        switch (type) {
        case Node.ELEMENT_NODE:
            String parentNodeName = node.getParentNode().getNodeName();
            if (parentNodeName.equals(no.hvl.multecore.common.Constants.NODE_NAME_MODEL)) {
            	NamedNodeMap metamodelsAttribute = document.getFirstChild().getAttributes();
        		if (metamodelsAttribute.getLength()>1) {
            		Node n = metamodelsAttribute.getNamedItem(no.hvl.multecore.common.Constants.LINGUISTIC_METAMODEL_PREFIX);
        			String supplementaryInformation = n.getNodeValue();
        			String[] supplementaryPairs = supplementaryInformation.split(no.hvl.multecore.common.Constants.NAMES_SEPARATOR);
        			for (int i = 0; i < supplementaryPairs.length; i++) {
        				String[] supplementaryElements = supplementaryPairs[i].split(no.hvl.multecore.common.Constants.SUPPLEMENTARY_SEPARATOR);
        				Model model = (Model) iModelUpdate;
        				// We differentiate, if it is the primitive types hierarchy, we can't search a project
        				if (supplementaryElements[0].equals(no.hvl.multecore.common.Constants.PRIMITIVE_TYPES_HIERARCHY)) {
        					iModelUpdate.getHierarchy().addSupplementaryHierarchy(MultEcoreManager.instance().getPrimitiveTypesHirarchy());
        					model.addSupplementaryModel(MultEcoreManager.instance().getPrimitiveTypesHirarchy().getModelByName(supplementaryElements[1]));     					
        				}
        				else {
        					iModelUpdate.getHierarchy().addSupplementaryHierarchy(MultEcoreManager.instance().getMultilevelHierarchy(supplementaryElements[0]));
        					model.addSupplementaryModel(MultEcoreManager.instance().getMultilevelHierarchy(supplementaryElements[0]).getModelByName(supplementaryElements[1]));
        				}
    				}
        		}
            }
            else if (parentNodeName.equals(no.hvl.multecore.common.Constants.NODE_NAME_ELEMENTS)) {
                try {
					addElement(node);
				} catch (MultEcoreException e) {
					Debugger.logError("Could not add node " + node.getNodeName());
					e.createNotificationDialog();
				}
            } else if (parentNodeName.equals(no.hvl.multecore.common.Constants.NODE_NAME_RELATIONS)) {
                try {
					addRelation(node);
				} catch (MultEcoreException e) {
					Debugger.logError("Could not add edge " + node.getNodeName());
					e.createNotificationDialog();
				}
            }
            break;
        case Node.DOCUMENT_NODE:
        case Node.TEXT_NODE:
        case Node.ATTRIBUTE_NODE:
            // Do nothing
            break;
        default:
        	// TODO Use exception
            Debugger.logError("Unsupported Node type in the ." + Constants.FILE_EXTENSION_MEF + " file");
            break;
        }

        // Process children if any
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            transform(child);
        }
    }

    
    // Get information from MEF node to create a node
    protected void addElement (Node xmiNode) throws MultEcoreException {
    	
    	//Parse possible supplementary Nodes. In MEF the syntax is <nameSuppModel>="<nameSuppNode>"
    	Map <String, String> supplementaryNodesMap = new HashMap<String, String>();
    	for ( IModel supplementaryModel : iModelUpdate.getSupplementaryModels()) {
    		Node supplementaryNode = xmiNode.getAttributes().getNamedItem(supplementaryModel.getName());
    		if (supplementaryNode != null)
			supplementaryNodesMap.put(supplementaryModel.getName(), supplementaryNode.getNodeValue());
		}
    	
        // Parse name, type and type (reverse) potency
    	String name = xmiNode.getNodeName();
    	String typeName = no.hvl.multecore.common.Constants.NODE_TYPE_ATTRIBUTE_DEFAULT_VALUE;
    	int typeReversePotency = no.hvl.multecore.common.Constants.TYPE_REVERSE_POTENCY_DEFAULT_VALUE;
    	Node typeAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_TYPE);
    	if (null != typeAttribute) {
    		String typeNameString = typeAttribute.getNodeValue();
    		typeName = no.hvl.multecore.common.Utils.getTypeName(typeNameString);
    		typeReversePotency = no.hvl.multecore.common.Utils.getTypeReversePotency(typeNameString);
    	}
    
    	
        // Parse potency
        int startPotency = no.hvl.multecore.common.Constants.START_POTENCY_DEFAULT_VALUE;
        int endPotency = no.hvl.multecore.common.Constants.END_POTENCY_DEFAULT_VALUE;
        int depthPotency = no.hvl.multecore.common.Constants.DEPTH_POTENCY_DEFAULT_VALUE;
        Node startPotencyAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_START);
        Node endPotencyAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_END);
        Node depthPotencyAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_DEPTH);
        if (null != startPotencyAttribute && null != endPotencyAttribute && null != depthPotencyAttribute) {
        	startPotency = Integer.parseInt(startPotencyAttribute.getNodeValue());
        	endPotency = Integer.parseInt(endPotencyAttribute.getNodeValue());
        	depthPotency = Integer.parseInt(depthPotencyAttribute.getNodeValue());
        } else {
        	Debugger.log("Assuming default potency for node " + modelName + "." + name);
        }
        
        // Parse whether node is abstract and its inheritance relations
        boolean isAbstract = no.hvl.multecore.common.Constants.IS_ABSTRACT_DEFAULT_VALUE;
        Set<String> parentNodeNames = new HashSet<String>();
        Node isAbstractAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_IS_ABSTRACT);
        if (null != isAbstractAttribute) {
        	isAbstract = Boolean.parseBoolean(isAbstractAttribute.getNodeValue());
        }
        Node parentNodesAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_PARENT_NODES);
        if (null != parentNodesAttribute) {
        	String parentNodesAttributeValue = parentNodesAttribute.getNodeValue();
        	if (!parentNodesAttributeValue.isEmpty()) {
				String[] parentNodeNameList = parentNodesAttributeValue.split(no.hvl.multecore.common.Constants.NAMES_SEPARATOR);
	        	for(int i=0; i<parentNodeNameList.length; i++) {
	        		parentNodeNames.add(parentNodeNameList[i]);
	        	}
	        }
        }
        
        attemptToAddNode(new PendingNode(xmiNode, name, typeName, typeReversePotency, startPotency, endPotency, depthPotency, parentNodeNames, isAbstract, supplementaryNodesMap));
    }
    
    
    @Override
    protected void addNode(PendingNode pendingNode) throws MultEcoreException {
    	// Create node in model
    	no.hvl.multecore.common.hierarchy.Node node = multilevelHierarchy.addNode(pendingNode.nodeName, pendingNode.typeName, pendingNode.typeReversePotency, pendingNode.startPotency, pendingNode.endPotency, pendingNode.depthPotency, pendingNode.parentNodeNames, iModelUpdate);
    	node.setAbstract(pendingNode.isAbstract);
        
        if ((null != pendingNode.supplementaryNodesMap)) {
        	Set<String> modelNames = pendingNode.supplementaryNodesMap.keySet();
        	for (String modelName : modelNames) {
        		String supplementaryNodeName = pendingNode.supplementaryNodesMap.get(modelName);
        		if (!supplementaryNodeName.isEmpty())
        			multilevelHierarchy.addSupplementaryNode (node, modelName, supplementaryNodeName);
        	}
        }
    	
        // Add its attributes
        NodeList attributeList = ((Node) pendingNode.originalNode).getChildNodes();
        for (int i=0;i<attributeList.getLength();i++) {
        	Node attribute = attributeList.item(i);
        	if (attribute.getNodeType() == Node.ELEMENT_NODE)
        		addAttribute(attributeList.item(i), node);
        }
    }
    

    protected void addRelation (Node xmiNode) throws MultEcoreException {
    	// Parse and get source and target nodes
        Node sourceAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_SOURCE);
        Node targetAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_TARGET);
        if ((null == sourceAttribute) || (null == targetAttribute))
        	Debugger.logError("Edge " + modelName + "." + xmiNode.getNodeName() + " has no valid source or target"); //TODO Turn into exception
        String sourceNodeName = sourceAttribute.getNodeValue();
        String targetNodeName = targetAttribute.getNodeValue();
        
        // Parse name, type and type (reverse) potency
    	String name = xmiNode.getNodeName();
    	String typeName = no.hvl.multecore.common.Constants.RELATION_TYPE_ATTRIBUTE_DEFAULT_VALUE;
    	int typeReversePotency = no.hvl.multecore.common.Constants.TYPE_REVERSE_POTENCY_DEFAULT_VALUE;
    	Node typeAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_TYPE);
    	if (null != typeAttribute) {
    		String typeNameString = typeAttribute.getNodeValue();
    		typeName = no.hvl.multecore.common.Utils.getTypeName(typeNameString);
    		typeReversePotency = no.hvl.multecore.common.Utils.getTypeReversePotency(typeNameString);
    	}
        
        // Parse potency, cardinality and whether relation is containment
        int startPotency = no.hvl.multecore.common.Constants.START_POTENCY_DEFAULT_VALUE;
        int endPotency = no.hvl.multecore.common.Constants.END_POTENCY_DEFAULT_VALUE;
        int depthPotency = no.hvl.multecore.common.Constants.DEPTH_POTENCY_DEFAULT_VALUE;
        Node startPotencyAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_START);
        Node endPotencyAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_END);
        Node depthPotencyAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_DEPTH);
        if (null != startPotencyAttribute && null != endPotencyAttribute) {
        	startPotency = Integer.parseInt(startPotencyAttribute.getNodeValue());
        	endPotency = Integer.parseInt(endPotencyAttribute.getNodeValue());
        	depthPotency = Integer.parseInt(depthPotencyAttribute.getNodeValue());
        } else {
        	Debugger.log("Assuming default potency for node " + modelName + "." + xmiNode.getNodeName());
        }
        int lowerBound = no.hvl.multecore.common.Constants.RELATION_LOWERBOUND_DEFAULT_VALUE;
        int upperBound = no.hvl.multecore.common.Constants.RELATION_UPPERBOUND_DEFAULT_VALUE;
        Node lowerBoundAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_LOWERBOUND);
        Node upperBoundAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_UPPERBOUND);
        if (null != lowerBoundAttribute && null != upperBoundAttribute) {
        	lowerBound = Integer.parseInt(lowerBoundAttribute.getNodeValue());
        	upperBound = Integer.parseInt(upperBoundAttribute.getNodeValue());
        } else {
        	Debugger.log("Assuming default potency for node " + modelName + "." + xmiNode.getNodeName());
        }
        boolean isContainment = no.hvl.multecore.common.Constants.RELATION_IS_CONTAINMENT_DEFAULT_VALUE;
        Node isContainmentAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_CONTAINMENT);
        if (null != isContainmentAttribute) {
        	isContainment = Boolean.parseBoolean(isContainmentAttribute.getNodeValue());
        }
        
        // Create edge in model
        Edge edge = multilevelHierarchy.addEdge(name, typeName, typeReversePotency, sourceNodeName, targetNodeName, startPotency, endPotency, depthPotency, iModelUpdate);
        edge.setLowerBound(lowerBound);
        edge.setUpperBound(upperBound);
        edge.setContainment(isContainment);
    }
    
    
    protected void addAttribute (Node xmiNode, INode iNode) throws MultEcoreException {
    	String xmiNodeName = xmiNode.getNodeName();
    	if (xmiNodeName.startsWith(no.hvl.multecore.common.Constants.SYNTHETIC_PREFIX))
    		return;
    	String containingNodeName = iNode.getName();
    	Node valueAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_VALUE);
    	
    	// If it has a value, it is instantiated
    	if (null != valueAttribute) {
    		// Update runtime version of the model in the hierarchy
    		multilevelHierarchy.addInstantiatedAttribute(valueAttribute.getNodeValue(), xmiNodeName, containingNodeName, iModelUpdate);
    		return;
    	}
    	
    	// It is an declared attribute
    	Node typeAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_TYPE);
    	String typeName = typeAttribute.getNodeValue();
    	
    	// Parse potency
    	int startPotency = no.hvl.multecore.common.Constants.START_POTENCY_DEFAULT_VALUE;
    	int endPotency = no.hvl.multecore.common.Constants.END_POTENCY_DEFAULT_VALUE;
    	Node startPotencyAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_START);
    	Node endPotencyAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_POTENCY_END);
    	if (null != startPotencyAttribute && null != endPotencyAttribute) {
    		startPotency = Integer.parseInt(startPotencyAttribute.getNodeValue());
    		endPotency = Integer.parseInt(endPotencyAttribute.getNodeValue());
    	} else {
    		Debugger.log("Assuming default potency for attribute " + xmiNodeName + " in node " + containingNodeName + " from model " + iModel.getName());
    	}
    	
    	// Parse cardinality
        int lowerBound = no.hvl.multecore.common.Constants.ATTRIBUTE_LOWERBOUND_DEFAULT_VALUE;
        int upperBound = no.hvl.multecore.common.Constants.ATTRIBUTE_UPPERBOUND_DEFAULT_VALUE;
        Node lowerBoundAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_LOWERBOUND);
        Node upperBoundAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_UPPERBOUND);
        if (null != lowerBoundAttribute && null != upperBoundAttribute) {
        	lowerBound = Integer.parseInt(lowerBoundAttribute.getNodeValue());
        	upperBound = Integer.parseInt(upperBoundAttribute.getNodeValue());
        } else {
        	Debugger.log("Assuming default potency for node " + modelName + "." + xmiNode.getNodeName());
        }
        
        // Parse whether declared attribute is id
        boolean isId = no.hvl.multecore.common.Constants.ATTRIBUTE_IS_ID_DEFAULT_VALUE;
        Node isIdAttribute = xmiNode.getAttributes().getNamedItem(no.hvl.multecore.common.Constants.SERIALIZATION_ATTRIBUTE_NAME_IS_ID);
        if (null != isIdAttribute) {
        	isId = Boolean.parseBoolean(isIdAttribute.getNodeValue());
        }
        
    	// Update runtime version of the model in the hierarchy
    	DeclaredAttribute attribute = multilevelHierarchy.addDeclaredAttribute(xmiNodeName, NativeType.valueOf(typeName), containingNodeName, startPotency, endPotency, iModelUpdate);
    	attribute.setLowerBound(lowerBound);
    	attribute.setUpperBound(upperBound);
    	attribute.setId(isId);
    }

}

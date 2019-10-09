package no.hvl.multecore.core;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;

import no.hvl.multecore.common.MultEcoreManager;
import no.hvl.multecore.common.exceptions.MultEcoreException;
import no.hvl.multecore.common.exceptions.ParentNodeNotExists;
import no.hvl.multecore.common.hierarchy.IModel;
import no.hvl.multecore.common.hierarchy.MultilevelHierarchy;

public abstract class AbstractTransformerToHierarchy extends AbstractTransformer implements ITransformerToHierarchy {

    protected String metamodelName;
    protected IModel iModelUpdate;
    protected Map<String,Set<PendingNode>> pendingNodes;
    
    protected AbstractTransformerToHierarchy (URI modelURI, MultilevelHierarchy multilevelHierarchy) {
        super(modelURI, multilevelHierarchy);
        newResource = MultEcoreManager.instance().getModelRegistry().createResource(modelURI.trimFileExtension().appendFileExtension(no.hvl.multecore.core.Constants.FILE_EXTENSION_MEF));
        pendingNodes = new LinkedHashMap<String, Set<PendingNode>>();
    }
    
    
    @Override
    public abstract String getMetamodelName();

    
    @Override
    public void transform () throws MultEcoreException {
    	super.transform();
    	if (null == iModel) {
    		iModel = iModelUpdate = multilevelHierarchy.createModel(modelName, getMetamodelName());
    	} else {
    		iModelUpdate = multilevelHierarchy.createModelUpdateCopy(iModel);
    	}
    	MultEcoreManager.instance().getModelRegistry().getEntry(modelURI).setModel(iModelUpdate);
    	
    	// Throw exceptions if any node has not been added due to missing parent classes
    	for (String parentNodeName : pendingNodes.keySet()) {
    		for (PendingNode pendingNode : pendingNodes.get(parentNodeName)) {
    			throw new ParentNodeNotExists(pendingNode.nodeName, parentNodeName, iModelUpdate.getName());
    		}
    	}
    }
    
    
    // This method ensures that a node is not added until all its parent nodes exist
    protected void attemptToAddNode (PendingNode pendingNode) throws MultEcoreException {
    	// If parent not present yet, store for adding it later
    	if (!pendingNode.parentNodeNames.isEmpty()) {
    		for (String parentNodeName : pendingNode.parentNodeNames) {
    			if (null == iModelUpdate.getNode(parentNodeName)) {
    				pendingNodes.computeIfAbsent(parentNodeName, k -> new HashSet<PendingNode>()).add(pendingNode);
    				return;
    			}
    		}
    	}

    	// Add node to the hierarchy
    	addNode(pendingNode);
    	
    	// Try to add pending nodes which inherit from the one just created
    	Set<PendingNode> pendingChildren = pendingNodes.get(pendingNode.nodeName);
    	if (null == pendingChildren)
    		return;
    	for (PendingNode pendingChild : pendingChildren)
    		attemptToAddNode(pendingChild);
    }
    
    
    protected abstract void addNode(PendingNode pendingNode) throws MultEcoreException;
    
    
    // To store the information of a node that could not be added since its parents are not added yet
    protected class PendingNode {
    	Object originalNode;
    	String nodeName;
    	String typeName;
    	int typeReversePotency;
    	int startPotency;
    	int endPotency;
    	int depthPotency;
    	Set<String> parentNodeNames;
    	boolean isAbstract;
    	Map<String, String> supplementaryNodesMap;
    	
		public PendingNode(Object originalNode, String nodeName, String typeName, int typeReversePotency,
				int startPotency, int endPotency, int depthPotency, Set<String> parentNodeNames, boolean isAbstract, Map<String, String> supplementaryNodesMap) {
			this.originalNode = originalNode;
			this.nodeName = nodeName;
			this.typeName = typeName;
			this.typeReversePotency = typeReversePotency;
			this.startPotency = startPotency;
			this.endPotency = endPotency;
			this.depthPotency = depthPotency;
			this.parentNodeNames = parentNodeNames;
			this.isAbstract = isAbstract;
			this.supplementaryNodesMap = supplementaryNodesMap;
		}
		
		public PendingNode(Object originalNode, String nodeName, String typeName, int typeReversePotency,
				int startPotency, int endPotency, int depthPotency, Set<String> parentNodeNames, boolean isAbstract) {
			this.originalNode = originalNode;
			this.nodeName = nodeName;
			this.typeName = typeName;
			this.typeReversePotency = typeReversePotency;
			this.startPotency = startPotency;
			this.endPotency = endPotency;
			this.depthPotency = depthPotency;
			this.parentNodeNames = parentNodeNames;
			this.isAbstract = isAbstract;
		}
    	
    }

}
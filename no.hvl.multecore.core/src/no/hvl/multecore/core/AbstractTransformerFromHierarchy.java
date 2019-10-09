package no.hvl.multecore.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.xmi.XMLResource;

import no.hvl.multecore.common.exceptions.MultEcoreException;
import no.hvl.multecore.common.hierarchy.DeclaredAttribute;
import no.hvl.multecore.common.hierarchy.IAttribute;
import no.hvl.multecore.common.hierarchy.IEdge;
import no.hvl.multecore.common.hierarchy.INode;
import no.hvl.multecore.common.hierarchy.InstantiatedAttribute;
import no.hvl.multecore.common.hierarchy.MultilevelHierarchy;

public abstract class AbstractTransformerFromHierarchy extends AbstractTransformer implements ITransformerFromHierarchy {
    
    protected EPackage ePackage;
    
    
    protected AbstractTransformerFromHierarchy (URI modelURI, MultilevelHierarchy multilevelHierarchy) {
        super(modelURI, multilevelHierarchy);
    }
    
    
    @Override
    public void transform () throws MultEcoreException {
    	super.transform();
        this.initialize();
        // Sorting to make sure that parent nodes are transformed before their children
        List<INode> sortedNodes = new ArrayList<INode>();
        Set<INode> modelNodes = iModel.getNodes();
		while (sortedNodes.size() < modelNodes.size()) {
        	for (INode iNode : modelNodes) {
        		if (sortedNodes.contains(iNode))
        			continue;
        		else if (sortedNodes.containsAll(iNode.getParentNodes()))
        			sortedNodes.add(iNode);
        	}
        }
        for (INode node : sortedNodes) {
        	addElement(node);
        	// Need to sort to prevent attributes from moving around due to being generated in different order each time
        	List<InstantiatedAttribute> instantiatedAttributes = new ArrayList<InstantiatedAttribute>(node.getInstantiatedAttributes());
        	Collections.sort(instantiatedAttributes);
        	for (InstantiatedAttribute attribute : instantiatedAttributes) {
        		addAttribute(attribute);
        	}
        	List<DeclaredAttribute> declaredAttributes = new ArrayList<DeclaredAttribute>(node.getDeclaredAttributes(false));
        	Collections.sort(declaredAttributes);
        	for (DeclaredAttribute attribute : declaredAttributes) {
        		addAttribute(attribute);
        	}
        }
        for (IEdge iEdge : iModel.getEdges()) {
        	addRelation(iEdge);
        }
    }

    
    protected abstract void initialize();

    
    protected abstract void addElement(INode iNode);
    

    protected abstract void addRelation(IEdge iEdge);
    
    
    protected abstract void addAttribute(IAttribute iAttribute);


    protected void addEAttribute (EClass eClass, String name, EDataType type, Object defaultValue, int lowerbound, int upperbound) {
        EAttribute eAttribute = EcoreFactory.eINSTANCE.createEAttribute();
        eAttribute.setName(name);
        eAttribute.setEType(type);
        eAttribute.setDefaultValue(defaultValue);
        eAttribute.setLowerBound(lowerbound);
        eAttribute.setUpperBound(upperbound);
        
        // Add to EClass
        eClass.getEStructuralFeatures().add(eAttribute);
    }

    
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void save () throws IOException, TransformerException {
        Map options = new HashMap();
        options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
        newResource.save(options);
    }

}

package no.hvl.multecore.core;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.w3c.dom.Document;

import no.hvl.multecore.common.exceptions.MultEcoreException;
import no.hvl.multecore.common.hierarchy.IModel;
import no.hvl.multecore.common.hierarchy.MultilevelHierarchy;

public abstract class AbstractTransformer implements ITransformer {

	protected String modelName;

	protected URI baseURI;
    protected URI modelURI;

    protected Document document;
    protected Resource newResource;

    protected MultilevelHierarchy multilevelHierarchy;
    protected IModel iModel;
    
    
    protected AbstractTransformer (URI modelURI, MultilevelHierarchy multilevelHierarchy) {
    	this.modelURI = modelURI;
    	this.multilevelHierarchy = multilevelHierarchy;
        modelName = modelURI.trimFileExtension().lastSegment();
        baseURI = modelURI.trimSegments(1);
    }

    
    @Override
    public String getModelName() {
		return modelName;
	}

    
    @Override
    public IModel getIModel() {
    	return iModel;
    }
    
    
    @Override
    public void transform () throws MultEcoreException {
    	iModel = multilevelHierarchy.getModelByName(modelName);
    }
    
}

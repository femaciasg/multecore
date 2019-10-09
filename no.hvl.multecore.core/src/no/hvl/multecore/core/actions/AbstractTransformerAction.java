package no.hvl.multecore.core.actions;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;

import no.hvl.multecore.common.Debugger;
import no.hvl.multecore.core.ITransformer;

public abstract class AbstractTransformerAction extends Job {

    protected URI resourceUri;
    protected String fileExtension;

    
    public AbstractTransformerAction(String jobName, URI selectedResourceUri) {
		super(jobName);
		resourceUri = selectedResourceUri;
	}

    
	public abstract ITransformer getTransformer();

    
    @Override
    protected void canceling() {
    	super.canceling();
    	cancel();
    }
    
    
    @Override
    public boolean shouldRun() {
    	// Only run is the resource to be read/written has the expected file extension
    	if(resourceUri.fileExtension().equals(fileExtension))
    		return true;
		Debugger.logError("Could not run " + this.getClass().getSimpleName() + " due to incorrect file extension: " + resourceUri.fileExtension() + " instead of " + fileExtension);
		return false;
    }
    
}

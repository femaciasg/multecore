package no.hvl.multecore.core.actions;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.xml.sax.SAXException;

import no.hvl.multecore.common.Debugger;
import no.hvl.multecore.common.exceptions.MultEcoreException;
import no.hvl.multecore.core.ITransformerToHierarchy;

public abstract class AbstractTransformerToHierarchyAction extends AbstractTransformerAction {

	protected ITransformerToHierarchy iTransformerToHierarchy;
	
	
    public AbstractTransformerToHierarchyAction(URI selectedResourceUri) {
		super("Processing multilevel changes of " + selectedResourceUri.lastSegment(), selectedResourceUri);
	}

    
	public ITransformerToHierarchy getTransformer() {
		return iTransformerToHierarchy;
	}
	
    
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        try {
        	iTransformerToHierarchy.transform();
        } catch (ParserConfigurationException | SAXException | IOException | NullPointerException e) {
            e.printStackTrace();
            return Status.CANCEL_STATUS;
        } catch (MultEcoreException e) {
        	e.createNotificationDialog();
            return Status.CANCEL_STATUS;
        }
        return Status.OK_STATUS;
    }
    
    
    @Override
    public boolean shouldRun() {
    	if(!super.shouldRun())
    		return false;
    	// Only run if file to be read is there
		File resourceFile = new File(resourceUri.toFileString());
		if (!resourceFile.exists()) {
			Debugger.logError("Could not process multilevel changes due to missing file: " + resourceUri.lastSegment());
			return false;
		}
		return true;
    }
    
}

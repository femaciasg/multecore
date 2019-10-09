package no.hvl.multecore.core.actions;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import no.hvl.multecore.common.exceptions.MultEcoreException;
import no.hvl.multecore.core.ITransformerFromHierarchy;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.xml.sax.SAXException;

public abstract class AbstractTransformerFromHierarchyAction extends AbstractTransformerAction {

	protected ITransformerFromHierarchy iTransformerFromHierarchy;

	
    public AbstractTransformerFromHierarchyAction(URI selectedResourceUri) {
		super("Reflecting multilevel changes on " + selectedResourceUri.lastSegment(), selectedResourceUri);
	}

    
    @Override
	public ITransformerFromHierarchy getTransformer() {
		return iTransformerFromHierarchy;
	}
	
    
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        try {
        	iTransformerFromHierarchy.transform();
            iTransformerFromHierarchy.save();
        } catch (TransformerException | ParserConfigurationException | SAXException | IOException | NullPointerException e) {
            e.printStackTrace();
            return Status.CANCEL_STATUS;
        } catch (MultEcoreException e) {
        	e.createNotificationDialog();
            return Status.CANCEL_STATUS;
        }
        return Status.OK_STATUS;
    }
    
}

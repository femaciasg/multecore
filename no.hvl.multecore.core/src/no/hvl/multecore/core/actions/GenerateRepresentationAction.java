package no.hvl.multecore.core.actions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.sirius.business.api.componentization.ViewpointRegistry;
import org.eclipse.sirius.business.api.session.DefaultLocalSessionCreationOperation;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionCreationOperation;
import org.eclipse.sirius.tools.api.command.semantic.AddSemanticResourceCommand;
import org.eclipse.sirius.ui.business.api.session.UserSession;
import org.eclipse.sirius.ui.business.internal.commands.ChangeViewpointSelectionCommand;
import org.eclipse.sirius.viewpoint.description.Viewpoint;
import org.eclipse.swt.widgets.Display;

import no.hvl.multecore.common.MultEcoreManager;
import no.hvl.multecore.common.registry.ModelRegistryEntry;
import no.hvl.multecore.core.Constants;
import no.hvl.multecore.core.ITransformerFromHierarchy;

@SuppressWarnings("restriction")
public class GenerateRepresentationAction extends AbstractTransformerAction {

	protected ITransformerFromHierarchy iTransformerFromHierarchy;

	
    public GenerateRepresentationAction(URI selectedResourceUri) {
		super("Generating representation for " + selectedResourceUri.lastSegment(), selectedResourceUri);
	}

    
    @Override
	public ITransformerFromHierarchy getTransformer() {
		return null;
	}
	
    
    @Override
    protected IStatus run(IProgressMonitor monitor) {
		ModelRegistryEntry entry = MultEcoreManager.instance().getModelRegistry().getEntry(resourceUri);
		try {
			IJobManager jobManager = Job.getJobManager();
			jobManager.join(ResourcesPlugin.FAMILY_MANUAL_BUILD, monitor);
			jobManager.wakeUp(ResourcesPlugin.FAMILY_AUTO_BUILD);
			jobManager.join(ResourcesPlugin.FAMILY_AUTO_BUILD, monitor);
			entry.getMetamodelIResource().getParent().refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
		} catch (CoreException | OperationCanceledException | InterruptedException e) {
			e.printStackTrace();
		}
		IPath semanticResourceRelativePath = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(resourceUri.toFileString())).getFullPath();
		URI semanticResourceRelativeUri = URI.createPlatformResourceURI(semanticResourceRelativePath.toString(), true);
		URI sessionResourceURI = semanticResourceRelativeUri.trimFileExtension().appendFileExtension("aird");
		
		// Start session (aird)
		SessionCreationOperation o = new DefaultLocalSessionCreationOperation(sessionResourceURI, new NullProgressMonitor());
		try {
			o.execute();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		Session createdSession = o.getCreatedSession();
		
		// Add the Ecore to the aird
		AddSemanticResourceCommand addCommandToSession = new AddSemanticResourceCommand(createdSession, semanticResourceRelativeUri, new NullProgressMonitor());
		createdSession.getTransactionalEditingDomain().getCommandStack().execute(addCommandToSession);

		// Get the viewpoint with the name we wants
		Viewpoint viewpoint = findInViewpointInRegistry("metamodelViewpoint");
		Set<Viewpoint> viewpointSet = new HashSet<Viewpoint>();
		viewpointSet.add(viewpoint);
		
		// Change the enabled viewpoints
		ChangeViewpointSelectionCommand changeViewpoint = new ChangeViewpointSelectionCommand(createdSession, null, viewpointSet, new HashSet<Viewpoint>(), true, new NullProgressMonitor());
		createdSession.getTransactionalEditingDomain().getCommandStack().execute(changeViewpoint);

		// Select the viewpoint we just added
		UserSession userSession = UserSession.from(createdSession);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				userSession.selectViewpoint("metamodelViewpoint");
			}
		});
		
		IResource iResource = MultEcoreManager.instance().getCurrentlySelectedProject().getFile(resourceUri.trimFileExtension().appendFileExtension("aird").toFileString());
    	entry.setRepresentationResource(iResource);
        return Status.OK_STATUS;
    }
    
    
	public Viewpoint findInViewpointInRegistry(String viewpointName) {
		final Set<Viewpoint> registry = ViewpointRegistry.getInstance().getViewpoints();
		Viewpoint candidateViewpoint = null;
		for (Viewpoint registeredViewpoint : registry) {
			if (registeredViewpoint.getName().equals(viewpointName)) {
				candidateViewpoint = registeredViewpoint;
				break;
			}
		}
		return candidateViewpoint ;
	}
    
    
    @Override
    public boolean shouldRun() {
    	// Only run is the resource to be read/written has the expected file extension
    	if(!resourceUri.fileExtension().equals(Constants.FILE_EXTENSION_METAMODEL))
    		return false;
    	ModelRegistryEntry entry;
    	if(null == (entry = MultEcoreManager.instance().getModelRegistry().getEntry(resourceUri)))
    		return false;
    	return null == entry.getRepresentationResource();
    }
    
}

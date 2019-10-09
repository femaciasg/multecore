package no.hvl.multecore.core.events;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import no.hvl.multecore.common.Debugger;
import no.hvl.multecore.common.MultEcoreManager;
import no.hvl.multecore.common.hierarchy.MultilevelHierarchy;
import no.hvl.multecore.common.registry.ModelRegistry;
import no.hvl.multecore.common.registry.ModelRegistryEntry;
import no.hvl.multecore.core.Constants;
import no.hvl.multecore.core.actions.AbstractTransformerAction;
import no.hvl.multecore.core.actions.GenerateRepresentationAction;
import no.hvl.multecore.core.actions.MefFromHierarchyTransformerAction;
import no.hvl.multecore.core.actions.MetamodelFromHierarchyTransformerAction;
import no.hvl.multecore.core.actions.MetamodelToHierarchyTransformerAction;
import no.hvl.multecore.core.actions.ModelFromHierarchyTransformerAction;
import no.hvl.multecore.core.actions.ModelToHierarchyTransformerAction;


public class LogView extends ViewPart {
	
	public static final String ID = "no.hvl.multecore.core.views.LogView";
	
	private StructuredViewer viewer;
	private List<String> logEntryList;
	
	private IWorkspace workspace;
	private IResourceChangeListener rcl;
	
	
	public LogView() {
		logEntryList = new ArrayList<String>();
	}
	

	public void createPartControl(Composite parent) {		
		viewer = new ListViewer(parent);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new LabelProvider());
		viewer.setInput(logEntryList);
		getSite().setSelectionProvider(viewer);

		workspace = ResourcesPlugin.getWorkspace();
		
		// Listener for file changes
		rcl = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				Set<IProject> updatedProjects = new HashSet<IProject>();
				// To avoid infinite loops
				if (!no.hvl.multecore.common.Constants.USER_SAVED_CHANGES)
					return;
				
				// Get list of all changed resources
				IResourceDelta rootDelta = event.getDelta();
				if (rootDelta == null)
					return;
				final ArrayList<IResource> addedOrChangedResources = new ArrayList<IResource>();
				final ArrayList<IResource> deletedResources = new ArrayList<IResource>();
				final ArrayList<IResource> replacedResources = new ArrayList<IResource>();
				IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
					public boolean visit(IResourceDelta delta) {
						// Skip projects with disabled MLM
						IResource ir = delta.getResource();
						if ((ir instanceof IProject) && (MultEcoreManager.instance().isMultilevelEnabled((IProject) ir)))
							return true;
						
						// Only interested in files with relevant extensions
						if (ir.getType() == IResource.FILE && 
								Constants.MLM_RELEVANT_EXTENSIONS_LIST.contains(ir.getFileExtension().toLowerCase())) {
							if (MultEcoreManager.instance().isMultilevelEnabled(ir.getProject())) {
								// Interested in added, changed, replaced or deleted resources
								switch (delta.getKind()) {
								case IResourceDelta.ADDED:
								case IResourceDelta.CHANGED:
									addedOrChangedResources.add(ir);
									updatedProjects.add(ir.getProject());
									break;
								case IResourceDelta.REMOVED:
									deletedResources.add(ir);
									updatedProjects.add(ir.getProject());
									break;
								case IResourceDelta.REPLACED:
									replacedResources.add(ir);
									updatedProjects.add(ir.getProject());
									break;
								default:
									return true;
								}
							}
						}
						return true;
					}
				};
				try {
					rootDelta.accept(visitor);
				} catch (CoreException e) {
					Debugger.logError("Could not retrieve changed resources");
					refreshView();
				}

				// Log and/or process resource updates
				List<AbstractTransformerAction> transformerActions = new ArrayList<AbstractTransformerAction>();
				List<IResource> addedOrChangedResourcesCopy = new ArrayList<IResource>(addedOrChangedResources);
				outerloop:
				for (IResource ir : addedOrChangedResources) {
					MultilevelHierarchy multilevelHierarchy = MultEcoreManager.instance().getMultilevelHierarchy(ir.getProject());
					if (null == multilevelHierarchy)
						continue;
					for (IResource irc : addedOrChangedResourcesCopy) {
						if ((!ir.getFileExtension().equals(Constants.FILE_EXTENSION_MEF))
								&& ((!irc.getFileExtension().equals(Constants.FILE_EXTENSION_MEF)))
								&& (ir.getFullPath().removeFileExtension().equals(irc.getFullPath().removeFileExtension()))
								&& (!ir.equals(irc))
								&& (ir.getLocalTimeStamp() < irc.getLocalTimeStamp()))
							continue outerloop;
					}
					ModelRegistry mr = MultEcoreManager.instance().getModelRegistry();
					ModelRegistryEntry mre = mr.getEntry(ir);
					if (null == mre)
						mre = mr.createEntry(ir);
					String fileExtension = ir.getFileExtension();
					URI absoluteUri = mre.getAbsoluteUri();
					if (fileExtension.equals(Constants.FILE_EXTENSION_METAMODEL) && !(ir.getName().contains(Constants.DESERIALIZARION_FILE_SYNTHETIC_METAMODEL_SUFFIX))) {
						// If Ecore changed
						mre.setMetamodelResource(ir); // Update changes in registry
						transformerActions.add(new MetamodelToHierarchyTransformerAction(absoluteUri.appendFileExtension(Constants.FILE_EXTENSION_METAMODEL), multilevelHierarchy));
						transformerActions.add(new MefFromHierarchyTransformerAction(absoluteUri.appendFileExtension(Constants.FILE_EXTENSION_MEF), multilevelHierarchy));
						transformerActions.add(new MetamodelFromHierarchyTransformerAction(absoluteUri.appendFileExtension(Constants.FILE_EXTENSION_METAMODEL), multilevelHierarchy));
						transformerActions.add(new ModelFromHierarchyTransformerAction(absoluteUri.appendFileExtension(Constants.FILE_EXTENSION_MODEL), multilevelHierarchy));
						transformerActions.add(new GenerateRepresentationAction(absoluteUri.appendFileExtension(Constants.FILE_EXTENSION_METAMODEL)));
					} else if (fileExtension.equals(Constants.FILE_EXTENSION_MODEL)) {
						// If XMI changed, and neither the Ecore version or the MEF version changed
						mre.setModelResource(ir); // Update changes in registry
						transformerActions.add(new ModelToHierarchyTransformerAction(absoluteUri.appendFileExtension(Constants.FILE_EXTENSION_MODEL), multilevelHierarchy));
						transformerActions.add(new MefFromHierarchyTransformerAction(absoluteUri.appendFileExtension(Constants.FILE_EXTENSION_MEF), multilevelHierarchy));
						transformerActions.add(new ModelFromHierarchyTransformerAction(absoluteUri.appendFileExtension(Constants.FILE_EXTENSION_MODEL), multilevelHierarchy));
						transformerActions.add(new MetamodelFromHierarchyTransformerAction(absoluteUri.appendFileExtension(Constants.FILE_EXTENSION_METAMODEL), multilevelHierarchy));
						transformerActions.add(new GenerateRepresentationAction(absoluteUri.appendFileExtension(Constants.FILE_EXTENSION_METAMODEL)));
					}
				}
				for (AbstractTransformerAction ata : transformerActions) {
					Utils.scheduleJob(ata);
					if (!ata.equals(transformerActions.get(transformerActions.size() - 1))) {
						try {
							ata.join();
						} catch (InterruptedException e) {
							Debugger.logError("Waiting for job did not work");
						}
					}
				}
				for (IResource ir : deletedResources) {
					// TODO Delete MEF, in some cases
				}
				for (IResource ir : replacedResources) {
					// Don't know when this happens
				}
				
				for (IProject p : updatedProjects) {
					try {
						p.refreshLocal(IResource.DEPTH_INFINITE, null);
					} catch (CoreException e) {
						Debugger.logError("Error refreshing project (with Progress Monitor)");
					}
				}
				
				// Release the lock that avoids infinite loops
				no.hvl.multecore.common.Constants.USER_SAVED_CHANGES = false;
				
				// TODO Delete later
				if (MultEcoreManager.instance().isMultilevelEnabled()) {
					Debugger.log(MultEcoreManager.instance().getModelRegistry().toString());
					no.hvl.multecore.core.Utils.addtoMultEcoreConsole(MultEcoreManager.instance().getCurrentHierarchy().toString());
				}
			}
		};
		workspace.addResourceChangeListener(rcl, IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.POST_BUILD);
	}

	
	@Override
	public void setFocus() {
		// Do nothing
	}

	
	@Override
	public void dispose() {
		super.dispose();
		   workspace.removeResourceChangeListener(rcl);
	}

	
	public void addLogEntry(String entryText) {
		logEntryList.add(entryText);
		refreshView();
	}
	
	
	private void refreshView() {
        Display display = viewer.getControl().getDisplay();
        if (!display.isDisposed()) {
           display.asyncExec(new Runnable() {
              public void run() {
                 // Make sure the table still exists
                 if (viewer.getControl().isDisposed())
                    return;
                 viewer.refresh();
              }
           });
        }
	}
	
}

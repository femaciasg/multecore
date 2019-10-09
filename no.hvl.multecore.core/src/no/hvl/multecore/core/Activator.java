package no.hvl.multecore.core;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.State;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import no.hvl.multecore.common.MultEcoreManager;
import no.hvl.multecore.core.events.HierarchyView;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "no.hvl.multecore.core"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private ISelectionService selectionService;
	private ICommandService commandService;
	private ISelectionListener selectionListener;
	private IExecutionListener saveListener;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		IWorkbenchWindow workbenchWindow = no.hvl.multecore.common.Utils.getWindow();
		commandService = (ICommandService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ICommandService.class);
		
		// Ensure that project explorer is visible and on focus
		IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
		workbenchPage.showView("org.eclipse.ui.navigator.ProjectExplorer", null, IWorkbenchPage.VIEW_ACTIVATE);
		
		// Track which project is selected, to control the toggle button and displayed hierarchy tree
		selectionService = workbenchWindow.getSelectionService();
		selectionListener = new ISelectionListener() {
			@Override
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				// Get project containing the selection
				if (!(selection instanceof IStructuredSelection))
					return;
				IStructuredSelection iss = (IStructuredSelection) selection;
				Object o = iss.getFirstElement();
				if ((null == o) || !(o instanceof IResource))
					return;
				IProject project = ((IResource) o).getProject();	
				
				MultEcoreManager.instance().setCurrentlySelectedProject(project);
				
				// Get toggle command
				Command command = commandService.getCommand("no.hvl.multecore.core.commands.Toggle");
				State state = command.getState("org.eclipse.ui.commands.toggleState");

				// Updated the toggle state based on whether the project has MLM enabled or not
				state.setValue(MultEcoreManager.instance().isMultilevelEnabled(project));
				
				//
				HierarchyView.update(MultEcoreManager.instance().getMultilevelHierarchy(project));
			}
		};

		// Adding it to "org.eclipse.ui.navigator.resources.ProjectExplorer" does not work
		selectionService.addPostSelectionListener(selectionListener);
		
		saveListener = new IExecutionListener() {
			@Override
			public void preExecute(String commandId, ExecutionEvent event) {
//				Debugger.log("preExecute: " + commandId);
			}
			
			@Override
			public void postExecuteSuccess(String commandId, Object returnValue) {
//				Debugger.log("postExecuteSuccess: " + commandId);
				no.hvl.multecore.common.Constants.USER_SAVED_CHANGES = true;
			}
			
			@Override
			public void postExecuteFailure(String commandId, ExecutionException exception) {
//				Debugger.log("postExecuteFailure: " + commandId);
			}
			
			@Override
			public void notHandled(String commandId, NotHandledException exception) {
//				Debugger.log("notHandled: " + commandId);
			}
		};
		
		commandService.addExecutionListener(saveListener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		
		selectionService.removePostSelectionListener(selectionListener);
		commandService.removeExecutionListener(saveListener);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}

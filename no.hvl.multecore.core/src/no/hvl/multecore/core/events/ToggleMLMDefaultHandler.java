package no.hvl.multecore.core.events;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import no.hvl.multecore.common.Debugger;
import no.hvl.multecore.common.MultEcoreManager;
import no.hvl.multecore.common.hierarchy.IModel;
import no.hvl.multecore.common.hierarchy.MultilevelHierarchy;
import no.hvl.multecore.common.registry.ModelRegistry;
import no.hvl.multecore.common.registry.ModelRegistryEntry;
import no.hvl.multecore.core.Constants;
import no.hvl.multecore.core.ITransformer;
import no.hvl.multecore.core.ITransformerToHierarchy;
import no.hvl.multecore.core.actions.AbstractTransformerAction;
import no.hvl.multecore.core.actions.AbstractTransformerToHierarchyAction;
import no.hvl.multecore.core.actions.GenerateRepresentationAction;
import no.hvl.multecore.core.actions.MefFromHierarchyTransformerAction;
import no.hvl.multecore.core.actions.MefToHierarchyTransformerAction;
import no.hvl.multecore.core.actions.MetamodelFromHierarchyTransformerAction;
import no.hvl.multecore.core.actions.MetamodelToHierarchyTransformerAction;
import no.hvl.multecore.core.actions.ModelFromHierarchyTransformerAction;

public class ToggleMLMDefaultHandler extends AbstractHandler {
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		Command command = event.getCommand();

		// Check if a resource is selected in the Project Explorer view
		IResource resource = null;
		try {
			TreeSelection treeSelection = (TreeSelection) workbenchPage.getSelection(Constants.PROJECT_EXPLORER_VIEW_URI);
			resource = (IResource) treeSelection.getFirstElement();
		} catch (ClassCastException e) {
			Debugger.logError("Could not retrieve a valid selection from the Project Explorer");
		}
		
		// Check if a project (or resource inside one) is selected, and get the project
		if (null == resource) {
			no.hvl.multecore.common.Utils.showPopup("Invalid selection in the Project Explorer",
					"You must select a project or file in the Project Explorer to (de)activate MLM");
			// Less ugly way of doing this?
			HandlerUtil.toggleCommandState(command);
			HandlerUtil.toggleCommandState(command);
			return null;
		}
		IProject project = resource.getProject();
		
		// To prevent an exception the first time the plugin is ran
		if (null == MultEcoreManager.instance().getCurrentlySelectedProject())
			MultEcoreManager.instance().setCurrentlySelectedProject(project);
		
		Map<String, List<IResource>> allResources = Utils.getAllResources(project);
		if (allResources.get(Constants.FILE_EXTENSION_MEF).isEmpty()
				&& allResources.get(Constants.FILE_EXTENSION_MODEL).isEmpty()
				&& allResources.get(Constants.FILE_EXTENSION_METAMODEL).isEmpty()) {
			no.hvl.multecore.common.Utils.showPopup("Project is empty",
					"You cannot (de)activate MLM on a project without models");
			// Less ugly way of doing this?
			HandlerUtil.toggleCommandState(command);
			HandlerUtil.toggleCommandState(command);
			return null;
		}
		
		// Update toggle value
		boolean oldValue = HandlerUtil.toggleCommandState(command);
		
		// Register as soon as possible the change to keep the toggle value consistent, and initialise hierarchy
		MultilevelHierarchy mlh = null;
		if (oldValue) {
			mlh = MultEcoreManager.instance().removeMultilevelEnabledProject(project);
			if (MultEcoreManager.instance().isMultilevelEnabled())
				MultEcoreManager.instance().cleanModelRegistry();
			else
				MultEcoreManager.instance().cleanModelRegistryEntries(mlh.getAllModels());
			// TODO Delete this later
			Debugger.log(MultEcoreManager.instance().getModelRegistry().toString());
		} else {
			mlh = new MultilevelHierarchy(project.getName());
			MultEcoreManager.instance().addMultilevelEnabledProject(project, mlh);
		}
		
		// Get Log View object in order to log the change
		LogView logView = null;
		try {
			logView = (LogView) workbenchPage.showView(LogView.ID);
		} catch (PartInitException e) {
			Debugger.logError("Could not display the Log View");
		}
		
		// Show Hierarchy View in order to display its tree structure
		try {
			workbenchPage.showView(HierarchyView.ID);
		} catch (PartInitException e) {
			Debugger.logError("Could not display the Hierarchy View");
		}
		
		// Register disabling of MLM
		if (oldValue) {
			if (null != logView) logView.addLogEntry("Disabled MLM for project \"" + project.getName() + "\"");
			deactivateMLM(project);
			return null;
		}

		// Register and react to enabling of MLM
		logView.addLogEntry("Enabled MLM for project \"" + project.getName() + "\"");
		activateMLM(project, mlh);
		return null;
	}
	

	private void activateMLM(IProject project, MultilevelHierarchy multilevelHierarchy) {
		// Get all relevant resources in the project
		Map<String,List<IResource>> projectResources = Utils.getAllResources(project);
	
		// All the transformations required for each entry
		Map<ModelRegistryEntry, List<AbstractTransformerAction>> entries = new LinkedHashMap<ModelRegistryEntry, List<AbstractTransformerAction>>();

		ModelRegistry modelRegistry = MultEcoreManager.instance().getModelRegistry();
		
		// For every MEF
		for (IResource mefIR : projectResources.get(Constants.FILE_EXTENSION_MEF)){
			ModelRegistryEntry entry = modelRegistry.createEntry(mefIR);
			entry.setMefResource(mefIR);
			entries.put(entry, new ArrayList<AbstractTransformerAction>());
		}
		
		// For every metamodel
		for (IResource ecoreIR : projectResources.get(Constants.FILE_EXTENSION_METAMODEL)){
			ModelRegistryEntry entry = modelRegistry.getEntry(ecoreIR);
			if (null == entry) {
				entry = modelRegistry.createEntry(ecoreIR);
				entries.put(entry, new ArrayList<AbstractTransformerAction>());
			}
			else
				entry.setMetamodelResource(ecoreIR);
		}
	
		// For every model
		for (IResource xmiIR : projectResources.get(Constants.FILE_EXTENSION_MODEL)){
			ModelRegistryEntry entry = modelRegistry.getEntry(xmiIR);
			if (null == entry) {
				entry = modelRegistry.createEntry(xmiIR);
				entries.put(entry, new ArrayList<AbstractTransformerAction>());
			}
			else
				entry.setModelResource(xmiIR);
		}
		
		// Trying to pre load supplementary hierarchies before load the (selected) application hierarchy
		// Check if there is an ecore file
		if (!projectResources.get(Constants.FILE_EXTENSION_METAMODEL).isEmpty()) {
			// Run over all ecore files
			for (IResource iResource : projectResources.get(Constants.FILE_EXTENSION_METAMODEL)) {
				ModelRegistryEntry mre  = MultEcoreManager.instance().getModelRegistry().getEntry(iResource);
				// Get the epackage of the project
				EPackage ePackage = (EPackage) mre.getMetamodelResource().getContents().get(0);		
				EClass rootEClass = (EClass) ePackage.getEClassifier(no.hvl.multecore.common.Constants.NODE_NAME_ROOT);
				if (rootEClass!= null) {
			        EAnnotation supplementaryModelsEA = (null == rootEClass)? null : rootEClass.getEAnnotation(no.hvl.multecore.common.Constants.LINGUISTIC_METAMODEL_PREFIX);
			        EMap<String, String> supplementaryModelsMap = null;
			        if ((null != supplementaryModelsEA) && (null != (supplementaryModelsMap = supplementaryModelsEA.getDetails()))) {
			        	Set<String> hierarchyNames = supplementaryModelsMap.keySet();
			        	for (String hierarchyName : hierarchyNames) {
			        		String supplementaryModelName = supplementaryModelsMap.get(hierarchyName);
			        		if (!supplementaryModelName.isEmpty()) {
			        			IResource suppResource = iResource.getProject().getParent().findMember(hierarchyName);
			        			if (suppResource != null) {
			        				MultilevelHierarchy suppMultilevelHierarchy = null;
			        				if (MultEcoreManager.instance().getMultilevelHierarchy(hierarchyName) != null) {
			        					suppMultilevelHierarchy = MultEcoreManager.instance().getMultilevelHierarchy(hierarchyName);
			        				}
			        				else {
			        					suppMultilevelHierarchy = new MultilevelHierarchy(suppResource.getProject().getName());
			        				}
			        				if (!MultEcoreManager.instance().isMultilevelEnabled(suppResource.getProject())) {
				        				activateMLM(suppResource.getProject(), suppMultilevelHierarchy);
				        				MultEcoreManager.instance().addMultilevelEnabledProject(suppResource.getProject(), suppMultilevelHierarchy);
			        				}
			        				multilevelHierarchy.addSupplementaryHierarchy(suppMultilevelHierarchy);
			        			}
			        			//If the supplementary hierarchy is the primitive one, and the ePackage name is CPN, we need to add it without looking for it as a physical project
			        			else if (supplementaryModelName.equals(no.hvl.multecore.common.Constants.PRIMITIVE_TYPES_HIERARCHY) && 
			        					no.hvl.multecore.common.Constants.CPN_ID.equals(ePackage.getName()) &&
			        					!multilevelHierarchy.getSupplementaryHierarchies().contains(MultEcoreManager.instance().getPrimitiveTypesHirarchy())) {
			        				multilevelHierarchy.addSupplementaryHierarchy(MultEcoreManager.instance().getPrimitiveTypesHirarchy());
			        			}
			        		}			        			
			        	}
			        }
				}		
			}
		}
		

	
		// TODO Delete this later
		Debugger.log(MultEcoreManager.instance().getModelRegistry().toString());
		
		for (ModelRegistryEntry mre : entries.keySet()) {
			IResource newestIResource = mre.getNewestIResource();
			List<AbstractTransformerAction> transformerActions = entries.get(mre);
			URI absoluteUri = mre.getAbsoluteUri();
			URI mefResourceUri = absoluteUri.appendFileExtension(Constants.FILE_EXTENSION_MEF);
			URI modelResourceUri = absoluteUri.appendFileExtension(Constants.FILE_EXTENSION_MODEL);
			URI metamodelResourceUri = absoluteUri.appendFileExtension(Constants.FILE_EXTENSION_METAMODEL);
			if (newestIResource.equals(mre.getMefIResource())) {
				// MEF is the newest: MEF -> Ecore and MEF -> XMI
				transformerActions.add(new MefToHierarchyTransformerAction(mefResourceUri, multilevelHierarchy));
				transformerActions.add(new MetamodelFromHierarchyTransformerAction(metamodelResourceUri, multilevelHierarchy));
				transformerActions.add(new ModelFromHierarchyTransformerAction(modelResourceUri, multilevelHierarchy));
				transformerActions.add(new GenerateRepresentationAction(metamodelResourceUri));
			} else if (newestIResource.equals(mre.getMetamodelIResource())) {
				// Ecore is the newest: Ecore -> MEF -> XMI, and refresh Ecore
				transformerActions.add(new MetamodelToHierarchyTransformerAction(metamodelResourceUri, multilevelHierarchy));
				transformerActions.add(new MefFromHierarchyTransformerAction(mefResourceUri, multilevelHierarchy));
				transformerActions.add(new ModelFromHierarchyTransformerAction(modelResourceUri, multilevelHierarchy));
				transformerActions.add(new MetamodelFromHierarchyTransformerAction(metamodelResourceUri, multilevelHierarchy));
				transformerActions.add(new GenerateRepresentationAction(metamodelResourceUri));
			} else {
				if (mre.getMefIResource() == null) {
					if (mre.getMetamodelIResource() == null) {
						Debugger.logError("Ignored isolated XMI: " + absoluteUri.appendFileExtension(Constants.FILE_EXTENSION_MODEL));
						continue;
					} else {
						transformerActions.add(new MetamodelToHierarchyTransformerAction(metamodelResourceUri, multilevelHierarchy));
						transformerActions.add(new MefFromHierarchyTransformerAction(mefResourceUri, multilevelHierarchy));
						transformerActions.add(new MetamodelFromHierarchyTransformerAction(metamodelResourceUri, multilevelHierarchy));
						transformerActions.add(new ModelFromHierarchyTransformerAction(modelResourceUri, multilevelHierarchy));
						transformerActions.add(new GenerateRepresentationAction(metamodelResourceUri));
					}
				} else {
					transformerActions.add(new MefToHierarchyTransformerAction(mefResourceUri, multilevelHierarchy));
					transformerActions.add(new MetamodelFromHierarchyTransformerAction(metamodelResourceUri, multilevelHierarchy));
					transformerActions.add(new ModelFromHierarchyTransformerAction(modelResourceUri, multilevelHierarchy));
					transformerActions.add(new GenerateRepresentationAction(metamodelResourceUri));
				}
			}
		}


		
		// Sort the entries and run them in the proper order in the hierarchy (top to bottom)
		List<ModelRegistryEntry> sortedEntries = new ArrayList<ModelRegistryEntry>();
		List<ModelRegistryEntry> unsortedEntries = new ArrayList<ModelRegistryEntry>(entries.keySet());
		List<String> availableMetamodels = new ArrayList<String>();
		availableMetamodels.add(no.hvl.multecore.common.Constants.ECORE_ID);
		while (!unsortedEntries.isEmpty()) {
			int i = 0;
			int size = unsortedEntries.size();
			while (i < unsortedEntries.size()) {
				ModelRegistryEntry mre = unsortedEntries.get(i);
				ITransformerToHierarchy iTransformer = null;
				for (AbstractTransformerAction ata : entries.get(mre)) {
					if (ata instanceof AbstractTransformerToHierarchyAction) {
						iTransformer = (ITransformerToHierarchy) ata.getTransformer();
						break;
					}
				}
				if (null == iTransformer) {
					Debugger.logError("Could not find metamodels for " + mre.getName());
					unsortedEntries.remove(mre);
					continue;
				}
				if (availableMetamodels.contains(iTransformer.getMetamodelName())) {
					sortedEntries.add(mre);
					unsortedEntries.remove(mre);
					availableMetamodels.add(iTransformer.getModelName());
				}
				i++;
			}
			// Could not find the metamodel for some models. Could be an exception
			if (unsortedEntries.size() == size) {
				StringBuilder modelsNotLoadedStringBuilder = new StringBuilder();
				modelsNotLoadedStringBuilder.append("The following models could not be loaded due to missing metamodel:");
				for (ModelRegistryEntry mre : unsortedEntries) {
					ITransformer iTransformer = entries.get(mre).iterator().next().getTransformer();
					IModel iModel = iTransformer.getIModel();
					modelsNotLoadedStringBuilder.append("\n  - " + iModel.getName() + " with metamodel " + iModel.getMainMetamodel().getName());
				}
				no.hvl.multecore.common.Utils.showPopup("Models not loaded", modelsNotLoadedStringBuilder.toString());
				break;
			}
		}
		for (ModelRegistryEntry mre : sortedEntries) {
			for (AbstractTransformerAction ita : entries.get(mre)) {
				Utils.scheduleJob(ita);
				try {
					ita.join();
				} catch (InterruptedException e) {
					Debugger.logError("Waiting for job did not work");
				}
			}
		}

		// The moment we enable MLM in a CPN project, we add the primitive types hierarchy as a supplementary hierarchy
		if (multilevelHierarchy.isCPN() && !multilevelHierarchy.getSupplementaryHierarchies().contains(MultEcoreManager.instance().getPrimitiveTypesHirarchy())) {
			multilevelHierarchy.addSupplementaryHierarchy(MultEcoreManager.instance().getPrimitiveTypesHirarchy());
		}		
		
		
		// TODO Delete this later
		LogView logView = (LogView) no.hvl.multecore.common.Utils.getWindow().getActivePage().findView(LogView.ID);
		if (null != logView) {
			no.hvl.multecore.core.Utils.addtoMultEcoreConsole(multilevelHierarchy.toString());
		}
        
		// Refresh the project
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			Debugger.logError("Error refreshing project (with Progress Monitor");
		}
	}


	// TODO
	private void deactivateMLM(IProject project) {
		Map<String,List<IResource>> projectResources = Utils.getAllResources(project);
		List<IResource> mefList = projectResources.get(Constants.FILE_EXTENSION_MEF);
		for (IResource mefIR : mefList){
			IPath ecoreIP = mefIR.getLocation().removeFileExtension().addFileExtension(Constants.FILE_EXTENSION_METAMODEL);
			IResource ecoreIR = project.getFile(ecoreIP.makeRelativeTo(mefIR.getProject().getLocation()));
		}
	}

}

package no.hvl.multecore.core.events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import no.hvl.multecore.common.MultEcoreManager;
import no.hvl.multecore.common.Utils;
import no.hvl.multecore.common.hierarchy.IModel;
import no.hvl.multecore.common.registry.ModelRegistryEntry;
import no.hvl.multecore.core.Constants;

public class AddSupplementaryHierarchyHandler implements IObjectActionDelegate {

	@SuppressWarnings("unchecked")
	@Override
	public void run(IAction action) {
		// Get the selected project
		MultEcoreManager multEcoreManager = MultEcoreManager.instance();
		IWorkbenchPage workbenchPage = Utils.getWindow().getActivePage();
		IProject project = ((IResource) ((TreeSelection) workbenchPage
				.getSelection(Constants.PROJECT_EXPLORER_VIEW_URI)).getFirstElement()).getProject();

		// Abort if MLM not enabled for selected project
		if (!multEcoreManager.isMultilevelEnabled(project)) {
			Utils.showPopup("MLM not enabled", "Multilevel not enabled for project \"" + project.getName() + "\"");
			return;
		}

		// Create pop up with other projects, candidates for supplementary hierarchies
		Shell shell = new Shell();
		List<IProject> projectsExceptSelected = new ArrayList<IProject>(multEcoreManager.getAllMultilevelEnabledProjects());
		projectsExceptSelected.remove(project);
		ListSelectionDialog dialog = new ListSelectionDialog(shell, projectsExceptSelected, ArrayContentProvider.getInstance(), new LabelProvider(), "Select supplementary hierarchies");
		dialog.setTitle("Choose the projects to be supplementary");
		Set<IProject> previouslySelectedProjects = multEcoreManager.getSupplementaryProjects(project);
		dialog.setInitialElementSelections(new ArrayList<IProject>(previouslySelectedProjects));
		dialog.open();

		// Retrieve the selected projects
		Object[] selection = dialog.getResult();
		if (null == selection)
			return;
		Set<IProject> selectedProjects = new HashSet<IProject>((Collection<? extends IProject>) Arrays.asList(selection));

		// Updated supplementary hierarchies to selected ones
		multEcoreManager.setSupplementaryHierarchies(project, selectedProjects);

		// Get all models for current project (application)
		for (IModel model : multEcoreManager.getMultilevelHierarchy(project).getAllModels()) {
			// Ignore Ecore
			if (model.getName().compareTo("Ecore") != 0) {
				// Get the ecore of each model
				ModelRegistryEntry mre = MultEcoreManager.instance().getModelRegistry().getEntry(model);
				Resource resourceMM = mre.getMetamodelResource();
				EPackage ePackage = (EPackage) resourceMM.getContents().get(0);
				ePackage.getEClassifier("Root").getEAnnotation("lm").getDetails().clear();
				for (IProject suppHierarchyProject : selectedProjects) {
					ePackage.getEClassifier("Root").getEAnnotation("lm").getDetails().put(suppHierarchyProject.getName(),"");
				}
				
				// Since the Primitive types hierarchy is not a real project, we need to manually add it as selected
				if (MultEcoreManager.instance().getMultilevelHierarchy(project).isCPN()) {
					ePackage.getEClassifier("Root").getEAnnotation("lm").getDetails().put(no.hvl.multecore.common.Constants.PRIMITIVE_TYPES_HIERARCHY, no.hvl.multecore.common.Constants.PRIMITIVE_TYPES_HIERARCHY);
				}
				
				Map options = new HashMap();
				options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
				try {
					resourceMM.save(options);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Resource resourceModel = mre.getModelResource();
				//TODO Why can it be not null but still size = 0?
				if ((resourceModel != null) && (resourceModel.getContents().size() > 0)) {
					EObject eObject = resourceModel.getContents().get(0);
					EStructuralFeature eStructuralFeature = eObject.eClass().getEStructuralFeature(no.hvl.multecore.common.Constants.SUPPLEMENTARY_METAMODELS_ATTRIBUTE_NAME);
					EList<String> listSupp = (EList<String>) eObject.eGet(eStructuralFeature);
					listSupp.clear();
					for (IProject suppHierarchyProject : selectedProjects) {
						listSupp.add(suppHierarchyProject.getName());
					}
					Map options2 = new HashMap();
					options2.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
					try {
						resourceModel.save(options2);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {

	}

}

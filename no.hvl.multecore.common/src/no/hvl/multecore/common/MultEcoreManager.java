package no.hvl.multecore.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;

import no.hvl.multecore.common.exceptions.MultEcoreException;
import no.hvl.multecore.common.hierarchy.IModel;
import no.hvl.multecore.common.hierarchy.MultilevelHierarchy;
import no.hvl.multecore.common.registry.ModelRegistry;

// Implements singleton
public class MultEcoreManager {

	private static MultEcoreManager INSTANCE = null;
	
	private ModelRegistry modelRegistry;
	private IProject currentlySelectedProject;
	private Map<IProject, MultilevelHierarchy> multilevelEnabledProjects;
	private Map<MultilevelHierarchy, IProject> multilevelEnabledProjectsInverse;
	private MultilevelHierarchy primitiveTypesHirarchy = new MultilevelHierarchy(Constants.PRIMITIVE_TYPES_HIERARCHY);
	private String[] primitiveTypesArray = { "Byte", "Short", "Integer", "Long", "Float", "Double", "Boolean", "String" };

	private MultEcoreManager() {
		modelRegistry = new ModelRegistry();
		currentlySelectedProject = null;
		multilevelEnabledProjects = new HashMap<IProject, MultilevelHierarchy>();
		multilevelEnabledProjectsInverse = new HashMap<MultilevelHierarchy, IProject>();

		// Create a virtual hierarchy that contains supplementary primitive types
		IModel model = primitiveTypesHirarchy.createModel(Constants.PRIMITIVE_TYPES_HIERARCHY, Constants.ECORE_ID);
		primitiveTypesHirarchy.addModel(model);
		for (int i = 0; i < primitiveTypesArray.length; i++) {
			try {
				primitiveTypesHirarchy.addNode(primitiveTypesArray[i], Constants.ECLASS_ID,
						Constants.TYPE_REVERSE_POTENCY_DEFAULT_VALUE, Constants.START_POTENCY_DEFAULT_VALUE,
						Constants.END_POTENCY_DEFAULT_VALUE, Constants.DEPTH_POTENCY_DEFAULT_VALUE,
						new HashSet<String>(), model);
			} catch (MultEcoreException e) {
				Debugger.logError("Could not add primitive CPN type \"" + primitiveTypesArray[i] + "\" to its hierarchy");
			}
		}
	}

	public static MultEcoreManager instance() {
		if (null == INSTANCE)
			INSTANCE = new MultEcoreManager();
		return INSTANCE;
	}

	public ModelRegistry getModelRegistry() {
		return modelRegistry;
	}

	public IProject getCurrentlySelectedProject() {
		return currentlySelectedProject;
	}

	public MultilevelHierarchy getMultilevelHierarchy(IProject iProject) {
		return multilevelEnabledProjects.get(iProject);
	}

	public MultilevelHierarchy getMultilevelHierarchy(String hierarchyName) {
		for (IProject iProject : multilevelEnabledProjects.keySet()) {
			if (iProject.getName().compareTo(hierarchyName) == 0)
				return multilevelEnabledProjects.get(iProject);
		}
		return null;
	}

	public IProject getProject(MultilevelHierarchy multilevelHierarchy) {
		return multilevelEnabledProjectsInverse.get(multilevelHierarchy);
	}

	public MultilevelHierarchy getCurrentHierarchy() {
		return multilevelEnabledProjects.get(currentlySelectedProject);
	}

	public Set<IProject> getAllMultilevelEnabledProjects() {
		return new HashSet<IProject>(multilevelEnabledProjects.keySet());
	}

	public Set<IProject> getSupplementaryProjects(IProject project) {
		Set<IProject> supplementaryProjects = new HashSet<IProject>();
		Set<MultilevelHierarchy> smlh = getSupplementaryHierarchies(multilevelEnabledProjects.get(project));
		for (MultilevelHierarchy mlh : smlh) {
			if (mlh.getName() != Constants.PRIMITIVE_TYPES_HIERARCHY)
				supplementaryProjects.add(multilevelEnabledProjectsInverse.get(mlh));
		}
		return supplementaryProjects;
	}

	public Set<MultilevelHierarchy> getSupplementaryHierarchies(IProject project) {
		return new HashSet<MultilevelHierarchy>(getSupplementaryHierarchies(multilevelEnabledProjects.get(project)));
	}

	public Set<IProject> getSupplementaryProjects(MultilevelHierarchy applicationHierarchy) {
		Set<IProject> supplementaryProjects = new HashSet<IProject>();
		Set<MultilevelHierarchy> smlh = getSupplementaryHierarchies(applicationHierarchy);
		for (MultilevelHierarchy mlh : smlh) {
			supplementaryProjects.add(multilevelEnabledProjectsInverse.get(mlh));
		}
		return supplementaryProjects;
	}

	public Set<MultilevelHierarchy> getSupplementaryHierarchies(MultilevelHierarchy applicationHierarchy) {
		return new HashSet<MultilevelHierarchy>(applicationHierarchy.getSupplementaryHierarchies());
	}

	public void setCurrentlySelectedProject(IProject currentlySelectedProject) {
		this.currentlySelectedProject = currentlySelectedProject;
	}

	public void addMultilevelEnabledProject(IProject iProject, MultilevelHierarchy multilevelHierarchy) {
		multilevelEnabledProjects.put(iProject, multilevelHierarchy);
		multilevelEnabledProjectsInverse.put(multilevelHierarchy, iProject);
	}

	public void setSupplementaryHierarchies(IProject applicationHierarchyProject, Set<IProject> supplementaryHierarchyProjectSet) {
		MultilevelHierarchy ah = this.multilevelEnabledProjects.get(applicationHierarchyProject);
		Set<MultilevelHierarchy> supplementaryHierarchySet = new HashSet<MultilevelHierarchy>();

		for (IProject p : supplementaryHierarchyProjectSet) {
			supplementaryHierarchySet.add(getMultilevelHierarchy(p));
		}
		ah.getSupplementaryHierarchies().clear();
		ah.addSupplementaryHierarchies(supplementaryHierarchySet);
		
		// Add the removed (not selected) primitive types hierarchy if it is CPN project
		if (ah.isCPN()) {
			ah.addSupplementaryHierarchy(this.primitiveTypesHirarchy);
		}
	}

	public MultilevelHierarchy removeMultilevelEnabledProject(IProject iProject) {
		MultilevelHierarchy multilevelHierarchy = multilevelEnabledProjects.remove(iProject);
		multilevelEnabledProjectsInverse.remove(multilevelHierarchy);
		return multilevelHierarchy;
	}

	public IProject removeMultilevelEnabledProjectForHierarchy(MultilevelHierarchy multilevelHierarchy) {
		IProject iProject = multilevelEnabledProjectsInverse.remove(multilevelHierarchy);
		multilevelEnabledProjects.remove(iProject);
		return iProject;
	}

	// For any project
	public boolean isMultilevelEnabled() {
		return !multilevelEnabledProjects.isEmpty();
	}

	public boolean isMultilevelEnabled(IProject iProject) {
		return multilevelEnabledProjects.containsKey(iProject);
	}

	public void cleanModelRegistry() {
		modelRegistry = new ModelRegistry();
	}

	public void cleanModelRegistryEntries(Set<IModel> allModels) {
		for (IModel model : allModels) {
			modelRegistry.deleteEntry(model);
		}
	}

	public MultilevelHierarchy getPrimitiveTypesHirarchy() {
		return this.primitiveTypesHirarchy;
	}

	public String[] getPrimitiveTypesNames() {
		return this.primitiveTypesArray;

	}

}

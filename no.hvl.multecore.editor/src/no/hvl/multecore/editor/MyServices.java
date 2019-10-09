package no.hvl.multecore.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

import no.hvl.multecore.common.MultEcoreManager;
import no.hvl.multecore.common.hierarchy.INode;
import no.hvl.multecore.common.registry.ModelRegistryEntry;

public class MyServices {

	public MyServices() {

	}

	public List<String> getSuppTypes(EClass self, String nameSuppHierarchy, String nameSuppModel) {
		List<String> nameNodes = new ArrayList<String>();
		String defaultNode = "";
		nameNodes.add(defaultNode);
		if (nameSuppHierarchy.equals(no.hvl.multecore.common.Constants.PRIMITIVE_TYPES_HIERARCHY)) {
			nameNodes.addAll(Arrays.asList(MultEcoreManager.instance().getPrimitiveTypesNames()));
		} else {
			for (INode node : MultEcoreManager.instance().getMultilevelHierarchy(nameSuppHierarchy).getModelByName(nameSuppModel).getNodes()) {
				if (!(node.getName().equals(no.hvl.multecore.common.Constants.ECLASS_ID))
						&& !(node.getName().equals(no.hvl.multecore.common.Constants.NODE_NAME_ROOT))) {
					nameNodes.add(node.getName());
				}
			}
		}
		return nameNodes;
	}
	
	public boolean isCPNProject(EPackage self) {
		URI resourceEMFURI = URI.createURI(self.eResource().getURI().toPlatformString(true));
		String[] segments = resourceEMFURI.toString().split(no.hvl.multecore.core.Constants.URI_SEPARATOR_SERIALIZED);
		segments = Arrays.copyOfRange(segments, 2, segments.length);
		resourceEMFURI = URI.createHierarchicalURI(segments, null, null);

		ModelRegistryEntry entry = MultEcoreManager.instance().getModelRegistry().getEntry(resourceEMFURI);
		IProject project = entry.getMetamodelIResource().getProject();
		return MultEcoreManager.instance().getMultilevelHierarchy(project).isCPN();
	}
	
}

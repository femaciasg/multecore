package no.hvl.multecore.common.registry;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import no.hvl.multecore.common.Utils;
import no.hvl.multecore.common.hierarchy.IModel;


public class ModelRegistry {
	
	protected static final String MEF_FILE_EXTENSION = "mef";
	protected static final String METAMODEL_FILE_EXTENSION = "ecore";
	protected static final String MODEL_FILE_EXTENSION = "xmi";
	protected static final String REPRESENTATION_FILE_EXTENSION = "aird";
	
	private Set<ModelRegistryEntry> entries;
	protected ResourceSet resourceSet;
	
	
	public ModelRegistry() {
		resourceSet = new ResourceSetImpl();
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		entries = new HashSet<ModelRegistryEntry>();
	}


	public Resource createResource(URI uri) {
		Resource resource = resourceSet.createResource(uri);
		resourceSet.getResources().remove(resource);
		return resource;
	}
	
	
	public ModelRegistryEntry getEntry(URI uri) {
		for (ModelRegistryEntry mre : entries) {
			if (uri.trimFileExtension().equals(mre.getUri(uri.isRelative())))
				return mre;
		}
		return null;
	}
	
	
	public ModelRegistryEntry getEntry(IPath iPath) {
		return getEntry(Utils.toEMFURI(iPath));
	}
	
	
	public ModelRegistryEntry getEntry(IModel model) {
		for (ModelRegistryEntry mre : entries) {
			if (model.equals(mre.getModel()))
				return mre;
		}
		return null;
	}
	
	
	public ModelRegistryEntry getEntry(IResource iResource) {
		for (ModelRegistryEntry mre : entries) {
			if ((iResource.equals(mre.getMefIResource())) ||
					(iResource.equals(mre.getModelIResource())) ||
					(iResource.equals(mre.getMetamodelIResource())))
				return mre;
		}
		return getEntry(iResource.getRawLocation());
	}
	
	
	public ModelRegistryEntry getEntry(Resource resource) {
		for (ModelRegistryEntry mre : entries) {
			if ((resource.equals(mre.getMefResource())) ||
					(resource.equals(mre.getModelResource())) ||
					(resource.equals(mre.getMetamodelResource())))
				return mre;
		}
		return getEntry(resource.getURI());
	}
	
	
	public ModelRegistryEntry createEntry(IResource iResource) {
		ModelRegistryEntry entry = new ModelRegistryEntry(Utils.toEMFURI(iResource.getRawLocation().removeFileExtension()));
		entries.add(entry);
		return entry;
	}
	
	
//	public ModelRegistryEntry createEntry(IModel model) {
//		ModelRegistryEntry entry = new ModelRegistryEntry(model);
//		entries.add(entry);
//		return entry;
//	}
	
	
	public void deleteEntry (IModel model) {
		ModelRegistryEntry mre = getEntry(model);
		entries.remove(mre);
	}
	
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();

		result.append("=== Model registry ===\n");
		result.append("* ResourceSet: " + resourceSet.getResources().size() + "\n");
		for (ModelRegistryEntry entry : entries) {
			result.append("- Name:     " + entry.getName() + "\n");
			result.append("- URI:      " + entry.getUri(false) + "\n");
			result.append("- Base URI: " + entry.getBaseUri() + "\n");
			result.append("- Model:    " + entry.getModel() + "\n");
			result.append("- MEF:      " + entry.getMefIResource() + "\n");
			result.append("- Ecore:    " + entry.getMetamodelIResource() + "\n");
			result.append("- XMI:      " + entry.getModelIResource() + "\n");
			result.append("- AIRD:     " + entry.getRepresentationIResource() + "\n");
			result.append("----------------\n");
		}
		
		return result.toString();
	}
	
}

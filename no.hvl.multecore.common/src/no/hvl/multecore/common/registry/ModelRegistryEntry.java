package no.hvl.multecore.common.registry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import no.hvl.multecore.common.MultEcoreManager;
import no.hvl.multecore.common.Utils;
import no.hvl.multecore.common.hierarchy.IModel;

public class ModelRegistryEntry {

	private String name;
	private URI absoluteUri, absoluteProjectUri, projectRelativeUri;
	private IProject iProject;
	private IModel model;
	private ResourcePair mefResourcePair, modelResourcePair, metamodelResourcePair, representationResourcePair;

	
	public ModelRegistryEntry(URI uri) {
		iProject = MultEcoreManager.instance().getCurrentlySelectedProject();
		this.absoluteUri = uri.trimFileExtension();
		projectRelativeUri = uri.deresolve(Utils.toEMFURI(iProject.getLocation()).appendSegment(""));
		absoluteProjectUri = uri.trimSegments(projectRelativeUri.segmentCount()).appendSegment("");
		name = absoluteUri.lastSegment();
	}


//	public ModelRegistryEntry(IModel model) {
//		this.model = model;
//		this.name = model.getName();
//	}


	public String getName() {
		return name;
	}


	public URI getUri(boolean relative) {
		if (relative)
			return projectRelativeUri;
		return absoluteUri;
	}


	public URI getBaseUri() {
		return absoluteUri.trimSegments(1);
	}


	public URI getAbsoluteUri() {
		return absoluteUri;
	}


	public IModel getModel() {
		return model;
	}


	public void setModel(IModel model) {
		this.model = model;
	}


	public Resource getMefResource() {
		if (null == mefResourcePair) {
			Resource resource = MultEcoreManager.instance().getModelRegistry().resourceSet.getResource(absoluteUri.appendFileExtension(ModelRegistry.MEF_FILE_EXTENSION), false);
			if (resource == null)
				return null;
			mefResourcePair = new ResourcePair(resource);
		}
		return mefResourcePair.resource;
	}


	public IResource getMefIResource() {
		if (null == mefResourcePair) {
			IResource iResource = iProject.getFile(projectRelativeUri.appendFileExtension(ModelRegistry.MEF_FILE_EXTENSION).toFileString());
			if (!iResource.exists())
				return null;
			mefResourcePair = new ResourcePair(iResource);
		}
		return mefResourcePair.iResource;
	}


	public Resource getModelResource() {
		if (null == modelResourcePair) {
			Resource resource = MultEcoreManager.instance().getModelRegistry().resourceSet.getResource(absoluteUri.appendFileExtension(ModelRegistry.MODEL_FILE_EXTENSION), false);
			if (resource == null)
				return null;
			modelResourcePair = new ResourcePair(resource);
		}
		return modelResourcePair.resource;
	}


	public IResource getModelIResource() {
		if (null == modelResourcePair) {
			IResource iResource = iProject.getFile(projectRelativeUri.appendFileExtension(ModelRegistry.MODEL_FILE_EXTENSION).toFileString());
			if (!iResource.exists())
				return null;
			modelResourcePair = new ResourcePair(iResource);
		}
		return modelResourcePair.iResource;
	}


	public Resource getMetamodelResource() {
		if (null == metamodelResourcePair) {
			Resource resource = MultEcoreManager.instance().getModelRegistry().resourceSet.getResource(absoluteUri.appendFileExtension(ModelRegistry.METAMODEL_FILE_EXTENSION), false);
			if (resource == null)
				return null;
			metamodelResourcePair = new ResourcePair(resource);
		}
		return metamodelResourcePair.resource;
	}


	public IResource getMetamodelIResource() {
		if (null == metamodelResourcePair) {
			IResource iResource = iProject.getFile(projectRelativeUri.appendFileExtension(ModelRegistry.METAMODEL_FILE_EXTENSION).toFileString());
			if (!iResource.exists())
				return null;
			metamodelResourcePair = new ResourcePair(iResource);
		}
		return metamodelResourcePair.iResource;
	}
	
	
	public Resource getRepresentationResource() {
		if (null == representationResourcePair) {
			Resource resource = MultEcoreManager.instance().getModelRegistry().resourceSet.getResource(absoluteUri.appendFileExtension(ModelRegistry.REPRESENTATION_FILE_EXTENSION), false);
			if (resource == null)
				return null;
			representationResourcePair = new ResourcePair(resource);
		}
		return representationResourcePair.resource;
	}
	
	
	public IResource getRepresentationIResource() {
		if (null == representationResourcePair) {
			IResource iResource = iProject.getFile(projectRelativeUri.appendFileExtension(ModelRegistry.REPRESENTATION_FILE_EXTENSION).toFileString());
			if (!iResource.exists())
				return null;
			representationResourcePair = new ResourcePair(iResource);
		}
		return representationResourcePair.iResource;
	}


	public synchronized void setMefResource(Resource resource) {
		this.mefResourcePair = new ResourcePair(resource);
	}


	public synchronized void setModelResource(Resource resource) {
		this.modelResourcePair = new ResourcePair(resource);
	}


	public synchronized void setMetamodelResource(Resource resource) {
		this.metamodelResourcePair = new ResourcePair(resource);
	}


	public synchronized void setRepresentationResource(Resource resource) {
		this.representationResourcePair = new ResourcePair(resource);
	}


	public synchronized void setMefResource(IResource iResource) {
		this.mefResourcePair = new ResourcePair(iResource);
	}


	public synchronized void setModelResource(IResource iResource) {
		this.modelResourcePair = new ResourcePair(iResource);
	}


	public synchronized void setMetamodelResource(IResource iResource) {
		this.metamodelResourcePair = new ResourcePair(iResource);
	}


	public synchronized void setRepresentationResource(IResource iResource) {
		this.representationResourcePair = new ResourcePair(iResource);
	}

	
	public IResource getNewestIResource() {
		long mefModificationTime = getModificationTime(mefResourcePair);
		long modelModificationTime = getModificationTime(modelResourcePair);
		long metamodelModificationTime = getModificationTime(metamodelResourcePair);
		long newestModificationTime = Math.max(Math.max(mefModificationTime, modelModificationTime), metamodelModificationTime);
		if (newestModificationTime == mefModificationTime)
			return mefResourcePair.iResource;
		if (newestModificationTime == modelModificationTime)
			return modelResourcePair.iResource;
		return metamodelResourcePair.iResource;
	}
	
	
	private long getModificationTime(ResourcePair resourcePair) {
		if (null == resourcePair)
			return 0L;
		if (null == resourcePair.iResource)
			return 0L;
		return resourcePair.iResource.getLocation().toFile().lastModified();
	}
	
	
	private class ResourcePair {
		private Resource resource; // The "file"
		private IResource iResource; // The abstract tree-shaped structure
		
		
		public ResourcePair(Resource resource) {
			ResourceSet resourceSet = MultEcoreManager.instance().getModelRegistry().resourceSet;
			while (null != (this.resource = resourceSet.getResource(resource.getURI(), false)))
				resourceSet.getResources().remove(this.resource);
			this.resource = resource;
			resourceSet.getResources().add(resource);
			iResource = iProject.getFile(resource.getURI().deresolve(absoluteProjectUri).toFileString());
		}

		public ResourcePair(IResource iResource) {
			boolean loadOnDemand = iResource.getFileExtension().equals(ModelRegistry.METAMODEL_FILE_EXTENSION); // Gives problems if it is "false" for metamodels or "true" for models and MEFs
			this.iResource = iResource;
			URI resourceUri = Utils.toEMFURI(iResource.getLocationURI());
			ResourceSet resourceSet = MultEcoreManager.instance().getModelRegistry().resourceSet;
			if (null == (resource = resourceSet.getResource(resourceUri, loadOnDemand))) { // Create
				resource = resourceSet.createResource(resourceUri);
				resource = resourceSet.getResource(resourceUri, loadOnDemand);
			} else { //Refresh
				while (null != (resource = resourceSet.getResource(resourceUri, false)))
					resourceSet.getResources().remove(resource);
				resource = resourceSet.createResource(resourceUri);
				resource = resourceSet.getResource(resourceUri, loadOnDemand);
			}
				
		}
		
	}
	
}
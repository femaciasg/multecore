package no.hvl.multecore.core.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.URI;

import no.hvl.multecore.common.hierarchy.MultilevelHierarchy;
import no.hvl.multecore.core.Constants;
import no.hvl.multecore.core.MetamodelFromHierarchyTransformer;

public class MetamodelFromHierarchyTransformerAction extends AbstractTransformerFromHierarchyAction {

	public MetamodelFromHierarchyTransformerAction(URI selectedResourceUri, MultilevelHierarchy multilevelHierarchy) {
		super(selectedResourceUri);
		fileExtension = Constants.FILE_EXTENSION_METAMODEL;
		iTransformerFromHierarchy = new MetamodelFromHierarchyTransformer(selectedResourceUri, multilevelHierarchy);
	}


	@Override
	protected IStatus run(IProgressMonitor monitor) {
		return super.run(monitor);
	}

}
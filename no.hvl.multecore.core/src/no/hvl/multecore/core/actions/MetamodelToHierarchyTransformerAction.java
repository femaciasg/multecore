package no.hvl.multecore.core.actions;

import org.eclipse.emf.common.util.URI;

import no.hvl.multecore.common.hierarchy.MultilevelHierarchy;
import no.hvl.multecore.core.Constants;
import no.hvl.multecore.core.MetamodelToHierarchyTransformer;

public class MetamodelToHierarchyTransformerAction extends AbstractTransformerToHierarchyAction {

	public MetamodelToHierarchyTransformerAction(URI selectedResourceUri, MultilevelHierarchy multilevelHierarchy) {
		super(selectedResourceUri);
		fileExtension = Constants.FILE_EXTENSION_METAMODEL;
	    iTransformerToHierarchy = new MetamodelToHierarchyTransformer(selectedResourceUri, multilevelHierarchy);
	}
    
}
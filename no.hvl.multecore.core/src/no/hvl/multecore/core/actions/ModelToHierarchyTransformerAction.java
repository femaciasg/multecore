package no.hvl.multecore.core.actions;

import org.eclipse.emf.common.util.URI;

import no.hvl.multecore.common.hierarchy.MultilevelHierarchy;
import no.hvl.multecore.core.Constants;
import no.hvl.multecore.core.ModelToHierarchyTransformer;

public class ModelToHierarchyTransformerAction extends AbstractTransformerToHierarchyAction {

	public ModelToHierarchyTransformerAction(URI selectedResourceUri, MultilevelHierarchy multilevelHierarchy) {
		super(selectedResourceUri);
		fileExtension = Constants.FILE_EXTENSION_MODEL;
	    iTransformerToHierarchy = new ModelToHierarchyTransformer(selectedResourceUri, multilevelHierarchy);
	}
	
}
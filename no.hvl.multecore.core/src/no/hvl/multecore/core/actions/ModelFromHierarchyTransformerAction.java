package no.hvl.multecore.core.actions;

import org.eclipse.emf.common.util.URI;

import no.hvl.multecore.common.hierarchy.MultilevelHierarchy;
import no.hvl.multecore.core.Constants;
import no.hvl.multecore.core.ModelFromHierarchyTransformer;

public class ModelFromHierarchyTransformerAction extends AbstractTransformerFromHierarchyAction {

	public ModelFromHierarchyTransformerAction(URI selectedResourceUri, MultilevelHierarchy multilevelHierarchy) {
		super(selectedResourceUri);
		fileExtension = Constants.FILE_EXTENSION_MODEL;
		iTransformerFromHierarchy = new ModelFromHierarchyTransformer(selectedResourceUri, multilevelHierarchy);
	}
	
}
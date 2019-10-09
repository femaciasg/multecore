package no.hvl.multecore.core.actions;

import org.eclipse.emf.common.util.URI;

import no.hvl.multecore.common.hierarchy.MultilevelHierarchy;
import no.hvl.multecore.core.Constants;
import no.hvl.multecore.core.MefFromHierarchyTransformer;

public class MefFromHierarchyTransformerAction extends AbstractTransformerFromHierarchyAction {

	public MefFromHierarchyTransformerAction(URI selectedResourceUri, MultilevelHierarchy multilevelHierarchy) {
		super(selectedResourceUri);
		fileExtension = Constants.FILE_EXTENSION_MEF;
		iTransformerFromHierarchy = new MefFromHierarchyTransformer(selectedResourceUri, multilevelHierarchy);
	}
	
}
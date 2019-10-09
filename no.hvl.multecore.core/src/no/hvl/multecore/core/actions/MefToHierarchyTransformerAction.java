package no.hvl.multecore.core.actions;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.emf.common.util.URI;

import no.hvl.multecore.common.Debugger;
import no.hvl.multecore.common.hierarchy.MultilevelHierarchy;
import no.hvl.multecore.core.Constants;
import no.hvl.multecore.core.MefToHierarchyTransformer;

public class MefToHierarchyTransformerAction extends AbstractTransformerToHierarchyAction {
	
	public MefToHierarchyTransformerAction(URI selectedResourceUri, MultilevelHierarchy multilevelHierarchy) {
		super(selectedResourceUri);
		fileExtension = Constants.FILE_EXTENSION_MEF;
		try {
			iTransformerToHierarchy = new MefToHierarchyTransformer(selectedResourceUri, multilevelHierarchy);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			Debugger.logError("Error initializing MefToHierarchyTransformerAction with resource " + selectedResourceUri);
		}
	}
	
}
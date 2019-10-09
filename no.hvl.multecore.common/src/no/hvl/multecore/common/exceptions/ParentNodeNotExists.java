package no.hvl.multecore.common.exceptions;


public class ParentNodeNotExists extends MultEcoreException {

	private static final long serialVersionUID = 1L;
	private String elementName;
	private String parentNodeName;
	private String modelName;


	public ParentNodeNotExists(String elementName, String parentNodeName, String modelName) {
		super();
		this.elementName = elementName;
		this.parentNodeName = parentNodeName;
		this.modelName = modelName;
	}


	@Override
	public void createNotificationDialog() {
		super.createNotificationDialog("Model cannot be updated",
				"Element \"" + elementName +
				"\" inherits from missing element \"" + parentNodeName +
				"\" in model \"" + modelName + "\", and could not be added");
	}

}

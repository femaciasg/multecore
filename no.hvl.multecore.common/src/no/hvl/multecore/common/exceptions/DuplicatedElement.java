package no.hvl.multecore.common.exceptions;


public class DuplicatedElement extends MultEcoreException {

	private static final long serialVersionUID = 1L;
	private String elementName;
	private String modelName;


	public DuplicatedElement(String elementName, String modelName) {
		super();
		this.elementName = elementName;
		this.modelName = modelName;
	}


	@Override
	public void createNotificationDialog() {
		super.createNotificationDialog("Model cannot be updated",
				"Element \"" + elementName +
				"\" is duplicated in model \"" + modelName + "\"");
	}

}

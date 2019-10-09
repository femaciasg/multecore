package no.hvl.multecore.common.exceptions;


public class BadlyFormattedElement extends MultEcoreException {

	private static final long serialVersionUID = 1L;
	private String elementName;
	private String elementType;
	private String modelName;


	public BadlyFormattedElement(String elementName, String elementType, String modelName) {
		super();
		this.elementName = elementName;
		this.elementType = elementType;
		this.modelName = modelName;
	}


	@Override
	public void createNotificationDialog() {
		super.createNotificationDialog("Model cannot be updated",
				"Element \"" + elementName + "\" of type \"" +
						elementType + "\" is not valid in model \"" +
						modelName + "\"");

	}

}

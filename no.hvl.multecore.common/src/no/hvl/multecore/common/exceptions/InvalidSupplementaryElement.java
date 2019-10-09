package no.hvl.multecore.common.exceptions;

public class InvalidSupplementaryElement extends MultEcoreException {

	private static final long serialVersionUID = 1L;
	private String elementType;
	private String supplementaryElementName;
	private String applicationElementName;
	

	public InvalidSupplementaryElement(String elementType, String supplementaryElementName, String applicationElementName) {
		this.elementType = elementType;
		this.supplementaryElementName = supplementaryElementName;
		this.applicationElementName = applicationElementName;
	}


	@Override
	public void createNotificationDialog() {
		super.createNotificationDialog("Invalid supplementary " + elementType,
				"Supplementary " + elementType + " \"" + supplementaryElementName + "\" not found for "
				+ elementType + " \"" + applicationElementName + "\"");
	}

}

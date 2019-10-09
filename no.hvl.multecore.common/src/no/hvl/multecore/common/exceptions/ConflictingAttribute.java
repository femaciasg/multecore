package no.hvl.multecore.common.exceptions;


public class ConflictingAttribute extends MultEcoreException {

	private static final long serialVersionUID = 1L;
	private String attributeName;
	private String containerNodeName;
	private String modelName;
	private String containerNodeTypeName;
	private String metamodelName;


	public ConflictingAttribute(String attributeName, String containerNodeName, String modelName, String containerNodeTypeName, String metamodelName) {
		super();
		this.attributeName = attributeName;
		this.containerNodeName = containerNodeName;
		this.modelName = modelName;
		this.containerNodeTypeName = containerNodeTypeName;
		this.metamodelName = metamodelName;
	}


	@Override
	public void createNotificationDialog() {
		super.createNotificationDialog("Model cannot be updated",
				"Attribute \"" + attributeName + "\" in node \"" +
						containerNodeName + "\" from model \""  +
						modelName + "\" conflicts with the one declared in node \"" +
						containerNodeTypeName + "\" from model \"" +
						metamodelName + "\"");
	}

}

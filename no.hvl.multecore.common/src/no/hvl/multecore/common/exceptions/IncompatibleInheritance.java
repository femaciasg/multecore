package no.hvl.multecore.common.exceptions;


public class IncompatibleInheritance extends MultEcoreException {

	private static final long serialVersionUID = 1L;
	private String childElementName;
	private String parentElementName;
	private String modelName;


	public IncompatibleInheritance(String childElementName, String parentElementName, String modelName) {
		super();
		this.childElementName = childElementName;
		this.parentElementName = parentElementName;
		this.modelName = modelName;
	}


	@Override
	public void createNotificationDialog() {
		super.createNotificationDialog("Model cannot be updated",
				"Element \"" + childElementName +
				"\" cannot inherit from element \"" + parentElementName +
				"\" in model \"" + modelName + "\".\n" +
				"The type and potency of the child must match the ones of the parent.");
	}

}

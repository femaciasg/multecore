package no.hvl.multecore.common.exceptions;


public class ModelNotRegistered extends MultEcoreException {

	private static final String UNDEFINED = "undefined";
	private static final long serialVersionUID = 1L;

	private String modelName;
	

	public ModelNotRegistered(String modelName) {
		this.modelName = modelName;
	}


	@Override
	public void createNotificationDialog() {
		String printName = (null == modelName)? UNDEFINED : "\"" + modelName + "\"";
		super.createNotificationDialog("Model could not be registered", "The model with name " + printName + " could not be registered.");
	}

}

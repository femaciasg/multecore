package no.hvl.multecore.common.exceptions;


public class DanglingEdge extends MultEcoreException {

	private static final long serialVersionUID = 1L;
	private String edgeName;
	private String sourceName;
	private String targetName;
	private String modelName;


	public DanglingEdge(String edgeName, String sourceName, String targetName, String modelName) {
		super();
		this.edgeName = edgeName;
		this.sourceName = sourceName;
		this.targetName = targetName;
		this.modelName = modelName;
	}


	@Override
	public void createNotificationDialog() {
		super.createNotificationDialog("Model cannot be updated",
				"Edge \"" + sourceName + "\" -- \"" + edgeName + "\" --> \"" +
						targetName + "\" is not valid in model \"" +
						modelName + "\"");

	}

}

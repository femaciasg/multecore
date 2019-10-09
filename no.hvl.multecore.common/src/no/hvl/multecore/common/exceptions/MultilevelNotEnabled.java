package no.hvl.multecore.common.exceptions;


public class MultilevelNotEnabled extends MultEcoreException {

	private static final long serialVersionUID = 1L;
	private String projectName;


	public MultilevelNotEnabled(String projectName) {
		super();
		this.projectName = projectName;
	}


	@Override
	public void createNotificationDialog() {
		super.createNotificationDialog("MLM not enabled",
				"Multilevel not enabled for project \"" + projectName + "\"");
	}

}

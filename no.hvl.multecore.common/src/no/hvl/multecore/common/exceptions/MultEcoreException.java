package no.hvl.multecore.common.exceptions;

import no.hvl.multecore.common.Utils;

public abstract class MultEcoreException extends Exception {

	private static final long serialVersionUID = 1L;


	public abstract void createNotificationDialog();


	protected void createNotificationDialog(String title, String message) {
		Utils.showPopup(title, message);
	}

}

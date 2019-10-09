package no.hvl.multecore.core.events;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;


public class MultEcoreConsole implements IConsoleFactory {
	
	private static MessageConsole console = null;

	@Override
	public void openConsole() {
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
	    console = new MessageConsole("MultEcore Console", ImageDescriptor.createFromFile(this.getClass(), "icons/multecore-logo-color-15x15.png"));
	    consoleManager.addConsoles(new IConsole[]{console});
	    consoleManager.showConsoleView(console);
	    
	}
	
	public static MessageConsole getConsole() {
		return console;
	}

}

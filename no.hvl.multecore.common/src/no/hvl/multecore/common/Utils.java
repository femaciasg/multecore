package no.hvl.multecore.common;


import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class Utils {

	public static IWorkbenchWindow getWindow() {
		  IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		  if (window == null) {
		    IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		    if (windows.length > 0) {
		       return windows[0];
		    }
		  }
		  else {
		    return window;
		  }
		  return null;
	}
	
	
	public static void showPopup (String title, String message) {
		new Thread(new Runnable() {
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openInformation(
								getWindow().getShell(),
								title, message);
					}
				});
			}
		}).start();
	}

    
    public static String getTypeName (String typeString) {
	    String[] fragments = typeString.split(Constants.TYPE_POTENCY_SEPARATOR);
	    return fragments[0];
    }
    
    
    public static int getTypeReversePotency (String typeString) {
	    String[] fragments = typeString.split(Constants.TYPE_POTENCY_SEPARATOR);
	    if (fragments.length == 1)
	    	return 1;
	    
	    return Integer.parseInt(fragments[1]);
    }
	
	
	public static java.net.URI toJavaURI (URI emfURI) {
		try {
			return new java.net.URI(emfURI.toFileString());
		} catch (Exception e) {
			Debugger.logError("Could not trasform into Java URI: " + emfURI);
			return null;
		}
	}
	
	
	public static URI toEMFURI (java.net.URI javaURI) {
		return URI.createFileURI(javaURI.getRawPath());
	}
	
	
	public static URI toEMFURI (IPath iPath) {
		return URI.createFileURI(iPath.toString());
	}
	
}

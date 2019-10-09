package no.hvl.multecore.core;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import no.hvl.multecore.common.Debugger;
import no.hvl.multecore.core.events.LogView;
import no.hvl.multecore.core.events.MultEcoreConsole;

public class Utils {

	public static void addtoMultEcoreLog (String message) {
		LogView logView = (LogView) no.hvl.multecore.common.Utils.getWindow().getActivePage().findView(LogView.ID);
		if (null != logView) {
			logView.addLogEntry(message);
		}
	}
	
	public static void addtoMultEcoreConsole (String message) {
		MessageConsole console = MultEcoreConsole.getConsole();
		if (null==console)
			return;
		console.newMessageStream().println(message);
		
		// Bring console to front
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					IConsoleView view = (IConsoleView) no.hvl.multecore.common.Utils.getWindow().getActivePage().showView(IConsoleConstants.ID_CONSOLE_VIEW);
					view.display(console);
				} catch (PartInitException e) {
					Debugger.logError("Could not display the Console View");
				}
			}
		});
	}
    
	
    public static Node findChildNodeByName (Node node, String name) {
        Node childNode = null;
        NodeList childNodes = node.getChildNodes();
        for (int i=0; i<childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeName().equals(name)) {
                childNode = childNodes.item(i);
                break;
            }
        }
        return childNode;
    }
    
    
    public static Node findAttributeByName (Node node, String name) {
        Node attributeNode = null;
        NamedNodeMap metamodelsAttribute = node.getAttributes();
        for (int i=0; i<metamodelsAttribute.getLength(); i++) {
            Node attribute = metamodelsAttribute.item(i);
            if (attribute.getNodeName().equals(name)) {
                attributeNode = attribute;
                break;
            }
        }
        return attributeNode;
    }

    
    public static boolean isEClass (EClassifier eClassifier) {
        return eClassifier.getClass().equals(EClassImpl.class);
    }

    
    public static boolean isRoot (EClassifier eClassifier) {
        return eClassifier.getName().equals(no.hvl.multecore.common.Constants.NODE_NAME_ROOT);
    }
    
    
    public static boolean isEClassEClass (EClassifier eClassifier) {
        return eClassifier.getName().equals(no.hvl.multecore.common.Constants.ECLASS_ID);
    }
    
}

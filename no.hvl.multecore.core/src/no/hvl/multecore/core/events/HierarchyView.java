package no.hvl.multecore.core.events;

import java.util.ArrayList;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.sirius.business.api.session.DefaultLocalSessionCreationOperation;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionCreationOperation;
import org.eclipse.sirius.ui.business.api.dialect.DialectUIManager;
import org.eclipse.sirius.viewpoint.DAnalysis;
import org.eclipse.sirius.viewpoint.DRepresentation;
import org.eclipse.sirius.viewpoint.DView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import no.hvl.multecore.common.Constants;
import no.hvl.multecore.common.MultEcoreManager;
import no.hvl.multecore.common.Utils;
import no.hvl.multecore.common.hierarchy.IModel;
import no.hvl.multecore.common.hierarchy.MultilevelHierarchy;


public class HierarchyView extends ViewPart {
	
	public static final String ID = "no.hvl.multecore.core.views.HierarchyView";
	
	@Inject IWorkbench workbench;

	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action action;
	
	
	public static void update(MultilevelHierarchy mh) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		HierarchyView tree = (HierarchyView) page.findView(ID);
		if (null == tree)
			return;		
		TreeParent newTree = tree.new TreeParent("");
		IModel rootModel = (null == mh)? null : mh.getRootModel();
		TreeObject h = mkHierarchy(tree, mh, rootModel);
		newTree.addChild(h);
		tree.viewer.setInput(newTree);
		tree.viewer.refresh();
		tree.viewer.expandAll();
	}
	
	
	public static TreeObject mkHierarchy(HierarchyView tree, MultilevelHierarchy h, IModel iModel) {
		if (h == null) {
			return tree.new TreeParent("Multilevel not enabled");
		}
		final TreeObject result;
		Set<IModel> modelList = h.getChildrenModels(iModel);
		if (modelList.isEmpty()) {
			result = tree.new TreeObject(iModel);
		} else {
			TreeParent parent = tree.new TreeParent(iModel);
			result = parent;
			for (IModel sm : modelList) {
				parent.addChild(mkHierarchy(tree, h, sm));
			}
		}
		return result;
	}


	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);

		viewer.setContentProvider(new ViewContentProvider());
		viewer.setInput(getViewSite());
		viewer.setLabelProvider(new ViewLabelProvider());
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
	}
	

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				HierarchyView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	
	private void fillContextMenu(IMenuManager manager) {
		manager.add(action);
		drillDownAdapter.addNavigationActions(manager);
	}
	

	private void makeActions() {
		action = new Action() {
			public void run() {
				IStructuredSelection selection = viewer.getStructuredSelection();
				Object obj = selection.getFirstElement();
				if (!(obj instanceof TreeObject)) {
					Utils.showPopup("Hierarchy View", "Invalid selection");
					return;
				}
				TreeObject treeObject = (TreeObject) obj;
				if (treeObject.getName().equals(Constants.ECORE_ID)) {
					Utils.showPopup("Hierarchy View", "Cannot open editor for " + Constants.ECORE_ID);
					return;
				}
				IModel iModel = treeObject.getModel();
				if (null == iModel) {
					Utils.showPopup("Hierarchy View", "Cannot open editor for this selection");
				}
				IResource representationIResource = MultEcoreManager.instance().getModelRegistry().getEntry(iModel).getRepresentationIResource();
				URI representationURI = Utils.toEMFURI(representationIResource.getLocationURI());
				SessionCreationOperation o = new DefaultLocalSessionCreationOperation(representationURI, new NullProgressMonitor());
				try {
					o.execute();
				} catch (CoreException e) {
					Utils.showPopup("Hierarchy View", "Cannot open editor for this selection");
					return;
				}
				Session createdSession = o.getCreatedSession();
				DAnalysis root = (DAnalysis) createdSession.getSessionResource().getContents().get(0);
				DView dView = root.getOwnedViews().get(0);
				DRepresentation myDiagram = dView.getOwnedRepresentationDescriptors().get(0).getRepresentation();
				DialectUIManager.INSTANCE.openEditor(createdSession, myDiagram , new NullProgressMonitor());
			}
		};
		action.setText("Open diagram");
	}


	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				action.run();
			}
		});
	}


	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	

	private class TreeObject implements IAdaptable {
		private String name;
		private IModel iModel;
		private TreeParent parent;

		public TreeObject(String name) {
			this.name = name;
			iModel = null;
		}

		public TreeObject(IModel iModel) {
			this.name = iModel.getName();
			this.iModel = iModel;
		}
		
		public String getName() {
			return name;
		}
		
		public IModel getModel() {
			return iModel;
		}
		
		public void setParent(TreeParent parent) {
			this.parent = parent;
		}
		
		public TreeParent getParent() {
			return parent;
		}
		
		@Override
		public String toString() {
			return getName();
		}
		
		@Override
		public <T> T getAdapter(Class<T> key) {
			return null;
		}
		
	}

	
	private class TreeParent extends TreeObject {
		
		private ArrayList<TreeObject> children;
		
		public TreeParent(String name) {
			super(name);
			children = new ArrayList<TreeObject>();
		}
		
		public TreeParent(IModel iModel) {
			super(iModel);
			children = new ArrayList<TreeObject>();
		}
		
		public void addChild(TreeObject child) {
			children.add(child);
			child.setParent(this);
		}
		
		public TreeObject[] getChildren() {
			return (TreeObject[]) children.toArray(new TreeObject[children.size()]);
		}
		
		public boolean hasChildren() {
			return children.size()>0;
		}
		
	}
	

	private class ViewContentProvider implements ITreeContentProvider {
		
		private TreeParent invisibleRoot;

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput != null) {
				if (newInput instanceof TreeParent) {
					invisibleRoot = (TreeParent) newInput;
				}
			}
		}

		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot==null) initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}
		
		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject)child).getParent();
			}
			return null;
		}
		
		public Object [] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent)parent).getChildren();
			}
			return new Object[0];
		}
		
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent)parent).hasChildren();
			return false;
		}
		
		
		private void initialize() {
			MultilevelHierarchy mh = MultEcoreManager.instance().getCurrentHierarchy();
			TreeObject root = mkHierarchy(HierarchyView.this, mh, mh == null ? null : mh.getRootModel());
			invisibleRoot = new TreeParent(""); 
			invisibleRoot.addChild(root);
		}
	}


	private class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}


		public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			if ((obj instanceof TreeParent) && (((TreeParent) obj).getName().equals(Constants.ECORE_ID)))
				imageKey = ISharedImages.IMG_OBJ_FOLDER;
			return workbench.getSharedImages().getImage(imageKey);
		}

	}

}

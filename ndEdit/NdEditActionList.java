/*
 * $Id: NdEditActionList.java,v 1.10 2005/06/17 17:24:17 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/**
 * 
 * 
 * @author oz
 * @version 1.0 02/01/01
 * 
 */

public class NdEditActionList {
	private Vector<NdEditAction> mActions = new Vector<NdEditAction>(30);
	NdEdit mThis;
	ViewManager mViewManager;

	public NdEditActionList(NdEdit nd, ViewManager vm, UserSettingsManager usmgr) {
		mThis = nd;
		mViewManager = vm;
		ResourceBundle b = ResourceBundle.getBundle("ndEdit.NdEditResources");

		// create the file menu actions
		// future open generic file in new ndedit window
		// NewBrowserAction newAction = new NewBrowserAction("file",
		// b.getString("kNewBrowser"), new ImageIcon("gifs/action.gif"),
		// KeyStroke.getKeyStroke(KeyEvent.VK_N,
		// Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false),
		// mViewManager, (NdEdit)mThis);
		//        
		// open a generic file
		OpenFileAction openAction = null;
		try {
			openAction = new OpenFileAction("file", b.getString("kBrowseNetCDF"), new ImageIcon("gifs/action.gif"),
			    mViewManager, (NdEdit) mThis);
		}
		catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mActions.addElement(openAction);

		// mActions.addElement(newAction);

		// AppendPtrFileAction appendAction = new AppendPtrFileAction("file",
		// b.getString("kAppend"), new ImageIcon("gifs/action.gif"),
		// mViewManager, (NdEdit)mThis);
		// mActions.addElement(appendAction);

		// BrowseNetCDFFileAction browseAction = new BrowseNetCDFFileAction("file",
		// b.getString("kBrowseNetCDF"), new ImageIcon("gifs/action.gif"),
		// mViewManager, (NdEdit)mThis);
		// mActions.addElement(browseAction);

		SeparatorAction sepAction = null;
		try {
			sepAction = new SeparatorAction("file", "-------", new ImageIcon("gifs/action.gif"), mViewManager, (NdEdit) mThis);
		}
		catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mActions.addElement(sepAction);

		SaveSelectedAction saveSelActionAsEpic = null;
		try {
			saveSelActionAsEpic = new SaveSelectedAction("file", b.getString("kSaveSelAsEPIC"), new ImageIcon(
			    "gifs/action.gif"), mViewManager, (NdEdit) mThis, Constants.EPIC_FORMAT);
		}
		catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mActions.addElement(saveSelActionAsEpic);

		SaveSelectedAction saveSelActionAsArgo = null;
		try {
			saveSelActionAsArgo = new SaveSelectedAction("file", b.getString("kSaveSelAsArgo"), new ImageIcon(
			    "gifs/action.gif"), mViewManager, (NdEdit) mThis, Constants.ARGO_FORMAT);
		}
		catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mActions.addElement(saveSelActionAsArgo);
		PrintAction printAction = null;
		try {
			printAction = new PrintAction("file", b.getString("kPrint"), new ImageIcon("gifs/action.gif"), KeyStroke
			    .getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false), mViewManager,
			    (NdEdit) mThis);
		}
		catch (HeadlessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mActions.addElement(printAction);

		QuitAction quitAction = null;
		try {
			quitAction = new QuitAction("file", b.getString("kExit"), new ImageIcon("gifs/action.gif"), KeyStroke
			    .getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false), mViewManager,
			    (NdEdit) mThis);
		}
		catch (HeadlessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mActions.addElement(quitAction);

		CloseAction closeAction = null;
		try {
			closeAction = new CloseAction("file", b.getString("kClose"), new ImageIcon("gifs/action.gif"), mViewManager,
			    (NdEdit) mThis);
		}
		catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mActions.addElement(closeAction);

		// add edit menu items
		UndoAllAction undoAllAction = null;
		try {
			undoAllAction = new UndoAllAction("edit", b.getString("kResetAll"), new ImageIcon("gifs/action.gif"),
			    mViewManager, (NdEdit) mThis);
		}
		catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mActions.addElement(undoAllAction);
		// UndoAction undoAction = new UndoAction("edit", b.getString("kUndo"), new
		// ImageIcon("gifs/action.gif"),
		// KeyStroke.getKeyStroke(KeyEvent.VK_Z,
		// Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false),
		// mViewManager, (NdEdit)mThis);
		// //mActions.addElement(undoAction);
		// RedoAction redoAction = new RedoAction("edit", b.getString("kRedo"), new
		// ImageIcon("gifs/action.gif"),
		// KeyStroke.getKeyStroke(KeyEvent.VK_Z,
		// Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()+InputEvent.SHIFT_MASK,
		// false), mViewManager, (NdEdit)mThis);
		// //mActions.addElement(redoAction);

		try {
			SeparatorAction rsepAction = new SeparatorAction("edit", "-------", new ImageIcon("gifs/action.gif"),
			    mViewManager, (NdEdit) mThis);
			mActions.addElement(rsepAction);

			RetainStationsAction retainAction = new RetainStationsAction("edit", b.getString("kRetainStns"), new ImageIcon(
			    "gifs/action.gif"), mViewManager, (NdEdit) mThis);
			mActions.addElement(retainAction);
			DeleteStationsAction deleteAction = new DeleteStationsAction("edit", b.getString("kDeleteStns"), new ImageIcon(
			    "gifs/action.gif"), mViewManager, (NdEdit) mThis);
			mActions.addElement(deleteAction);

			SeparatorAction dsepAction = new SeparatorAction("edit", "-------", new ImageIcon("gifs/action.gif"),
			    mViewManager, (NdEdit) mThis);
			mActions.addElement(dsepAction);

			SelectAllAction selectAllAction = new SelectAllAction("edit", b.getString("kSelectAll"), new ImageIcon(
			    "gifs/action.gif"), mViewManager, (NdEdit) mThis);
			mActions.addElement(selectAllAction);

			SeparatorAction selallsepAction = new SeparatorAction("edit", "-------", new ImageIcon("gifs/action.gif"),
			    mViewManager, (NdEdit) mThis);
			mActions.addElement(selallsepAction);
			UserSettingsAction userSettingsAction = new UserSettingsAction("edit", b.getString("kPreferences"),
			    new ImageIcon("gifs/action.gif"), usmgr);
			mActions.addElement(userSettingsAction);

			// view menu
			ShowAllDataAction showAllAction = new ShowAllDataAction("view", b.getString("kShowAllData"), new ImageIcon(
			    "gifs/action.gif"), mViewManager, (NdEdit) mThis);
			mActions.addElement(showAllAction);

			SeparatorAction vsepAction = new SeparatorAction("view", "-------", new ImageIcon("gifs/action.gif"),
			    mViewManager, (NdEdit) mThis);
			mActions.addElement(vsepAction);
			LatLonViewAction latLonViewAction = new LatLonViewAction("view", b.getString("kLatLon"), new ImageIcon(
			    "gifs/action.gif"), mViewManager, (NdEdit) mThis);
			mActions.addElement(latLonViewAction);
			LonTimeViewAction lonTimeViewAction = new LonTimeViewAction("view", b.getString("kLonTim"), new ImageIcon(
			    "gifs/action.gif"), mViewManager, (NdEdit) mThis);
			mActions.addElement(lonTimeViewAction);
			LonDepthViewAction lonDepthViewAction = new LonDepthViewAction("view", b.getString("kDepLon"), new ImageIcon(
			    "gifs/action.gif"), mViewManager, (NdEdit) mThis);
			mActions.addElement(lonDepthViewAction);
			DepthTimeViewAction depthTimeViewAction = new DepthTimeViewAction("view", b.getString("kDepTim"), new ImageIcon(
			    "gifs/action.gif"), mViewManager, (NdEdit) mThis);
			mActions.addElement(depthTimeViewAction);
			LatDepthViewAction latDepthViewAction = new LatDepthViewAction("view", b.getString("kDepLat"), new ImageIcon(
			    "gifs/action.gif"), mViewManager, (NdEdit) mThis);
			mActions.addElement(latDepthViewAction);
			LatTimeViewAction latTimeViewAction = new LatTimeViewAction("view", b.getString("kLatTim"), new ImageIcon(
			    "gifs/action.gif"), mViewManager, (NdEdit) mThis);
			mActions.addElement(latTimeViewAction);

			ViewInspectorAction inspectorAction = new ViewInspectorAction("view", b.getString("kSelectionInspector"),
			    new ImageIcon("gifs/action.gif"), mViewManager, (NdEdit) mThis);
			mActions.addElement(inspectorAction);

			// add zoom menu items
			ZoomAllViewsAction zoomAllAction = new ZoomAllViewsAction("zoom", b.getString("kZoomSelRgn"), new ImageIcon(
			    "gifs/action.gif"), mViewManager, (NdEdit) mThis);
			mActions.addElement(zoomAllAction);
			ResetZoomAction zoomAction = new ResetZoomAction("zoom", b.getString("kResetZoom"), new ImageIcon(
			    "gifs/action.gif"), mViewManager, (NdEdit) mThis);
			mActions.addElement(zoomAction);

			// add help menu items
			AboutAction aboutAction = new AboutAction("help", b.getString("kAbout"), new ImageIcon("gifs/action.gif"),
			    mViewManager, (NdEdit) mThis);
			mActions.addElement(aboutAction);
			NewsAction newsAction = new NewsAction("help", b.getString("kNews"), new ImageIcon("gifs/action.gif"),
			    mViewManager, (NdEdit) mThis);
			mActions.addElement(newsAction);

			SystemPropertiesAction systemPropertiesAction = new SystemPropertiesAction("help", b
			    .getString("kSystemProperties"), new ImageIcon("gifs/action.gif"), mViewManager, (NdEdit) mThis);
			mActions.addElement(systemPropertiesAction);
		}
		catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Setting a bunch of buttons");
		}
	}

	public void addAction(NdEditAction act) {
		mActions.addElement(act);
	}

	public NdEditAction getAction(String name) {
		Enumeration it = mActions.elements();
		while (it.hasMoreElements()) {
			NdEditAction nda = (NdEditAction) it.nextElement();
			if (nda.getText().equalsIgnoreCase(name))
				return nda;
		}
		return null;
	}

	public Vector<NdEditAction> getActions() {
		return mActions;
	}

	public Vector<NdEditAction> getActions(String menu) {
		Vector<NdEditAction> ret = new Vector<NdEditAction>();
		Enumeration it = mActions.elements();
		while (it.hasMoreElements()) {
			NdEditAction nda = (NdEditAction) it.nextElement();
			if (nda.getMenu().equalsIgnoreCase(menu))
				ret.addElement(nda);
		}
		return ret;
	}
}

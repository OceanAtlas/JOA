/*
 * $Id: NdEditMenuBar.java,v 1.11 2005/06/17 17:24:17 oz Exp $
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

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.*;
import javax.swing.border.*;

public class NdEditMenuBar {
	private Component mThis;
	private Container mMenuHolder;
	private JMenuItem newBrowser;
	private JMenuItem open;
	private JMenuItem openNetCDF;
	private JMenuItem append;
	private JMenuItem saveAs;
	private JMenuItem saveAs2;
	private JMenuItem print;
	private JMenuItem quit;
	private JMenuItem prefs;
	private JMenuItem help;
	private JMenuItem undoAll;
	private JMenuItem undo;
	private JMenuItem redo;
	private JMenuItem retainstns;
	private JMenuItem deletestns;
	private JMenuItem selectall;
	private JMenuItem showselectedrgn;
	private JMenuItem showalldata;
	private JMenuItem ctrofselrgn;
	private JCheckBoxMenuItem latlon;
	private JCheckBoxMenuItem lontim;
	private JCheckBoxMenuItem deplon;
	private JCheckBoxMenuItem deptim;
	private JCheckBoxMenuItem deplat;
	private JCheckBoxMenuItem lattim;
	private JCheckBoxMenuItem inspector;
	private JMenuItem zoom;
	private JMenuItem resetzoom;
	private JMenuItem about;
	private JMenuItem news;
	private JMenuItem sysProps;
	private boolean mEnablePrinting;
	private boolean mQuit;
	private ViewManager mViewManager;
	
	public NdEditMenuBar(Container menuholder, Component c, NdEditActionList actionList, boolean enablePrinting, boolean quit) {
		mThis = c;
		mMenuHolder = menuholder;
		mEnablePrinting = enablePrinting;
		mViewManager = ((NdEdit)c).getViewManager();
		mQuit = quit;

    	buildJMenuBar(actionList);
	}

	public void buildJMenuBar(NdEditActionList actionList) {
		ResourceBundle b = ResourceBundle.getBundle("ndEdit.NdEditResources");
		JMenuBar menubar = new JMenuBar();
		menubar.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        if (mMenuHolder instanceof JFrame) {
          ((JFrame)mMenuHolder).setJMenuBar(menubar);
        } 
        else if(mMenuHolder instanceof JRootPane) {
          ((JRootPane)mMenuHolder).setJMenuBar(menubar);
        } 
        else if(mMenuHolder instanceof JDialog) {
          ((JDialog)mMenuHolder).setJMenuBar(menubar);
        } 
        else if(mMenuHolder instanceof JApplet) {
          ((JApplet)mMenuHolder).setJMenuBar(menubar);
        }
        menubar.setBorder(new BevelBorder(BevelBorder.RAISED));

		// create the menus
		JMenu file = new JMenu(b.getString("kFile"));
		JMenu edit = new JMenu(b.getString("kEdit"));
		JMenu view = new JMenu(b.getString("kView"));
		JMenu zoomM = new JMenu(b.getString("kZoom"));
		JMenu window = new JMenu(b.getString("kWindow"));
		JMenu help = new JMenu(b.getString("kHelp2"));

		// add file menu items
		final OpenFileAction openAction =
		  (OpenFileAction)(actionList.getAction(b.getString("kBrowseNetCDF")));
		open = file.add(openAction);
		open.setAccelerator(openAction.getAccelerator());
//
//		final NewBrowserAction newAction =
//		  (NewBrowserAction)(actionList.getAction(b.getString("kNewBrowser")));
//		newBrowser = file.add(newAction);
//		newBrowser.setAccelerator(newAction.getAccelerator());

		final SaveSelectedAction saveSelAsEpicAction =
		  (SaveSelectedAction)(actionList.getAction(b.getString("kSaveSelAsEPIC")));
		saveAs = file.add(saveSelAsEpicAction);

		final SaveSelectedAction saveSelAsArgoAction =
		  (SaveSelectedAction)(actionList.getAction(b.getString("kSaveSelAsArgo")));
		saveAs2 = file.add(saveSelAsArgoAction);

		if (mEnablePrinting) {
			PrintAction printAction =
		  		(PrintAction)(actionList.getAction(b.getString("kPrint")));
			print = file.add(printAction);
			print.setAccelerator(printAction.getAccelerator());
			file.addSeparator();
		}

		CloseAction closeAction = null;
		QuitAction quitAction = null;
		if (mQuit) {
			quitAction = (QuitAction)(actionList.getAction(b.getString("kExit")));
		    quit = file.add(quitAction);
		}
		else {
		  closeAction = (CloseAction)(actionList.getAction(b.getString("kClose")));
		  quit = file.add(closeAction);
		}
		//quit.setAccelerator(quitAction.getAccelerator());

		file.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent evt) {
				// dim most of the menu items if no pointer collection is open
				//newAction.setEnabled(newAction.isEnabled());
				open.setEnabled(openAction.isEnabled());
				//append.setEnabled(appendAction.isEnabled());
				saveAs.setEnabled(saveSelAsEpicAction.isEnabled());
				saveAs2.setEnabled(saveSelAsArgoAction.isEnabled());
			}
			public void menuDeselected(MenuEvent evt) {

			}
			public void menuCanceled(MenuEvent evt) {

			}
		});

		// add edit menu items
		final UndoAllAction undoAllAction = (UndoAllAction)(actionList.getAction(b.getString("kResetAll")));
		undoAll = edit.add(undoAllAction);

		//final UndoAction undoAction = (UndoAction)(actionList.getAction(b.getString("kUndo")));
		//undo = edit.add(undoAction);
		//undo.setAccelerator(undoAction.getAccelerator());

		//final RedoAction redoAction = (RedoAction)(actionList.getAction(b.getString("kRedo")));
		//redo = edit.add(redoAction);
		//redo.setAccelerator(redoAction.getAccelerator());
		edit.addSeparator();

		final RetainStationsAction retainAction = (RetainStationsAction)(actionList.getAction(b.getString("kRetainStns")));
		retainstns = edit.add(retainAction);

		final DeleteStationsAction deleteAction = (DeleteStationsAction)(actionList.getAction(b.getString("kDeleteStns")));
		deletestns = edit.add(deleteAction);
		edit.addSeparator();

		final SelectAllAction selectAllAction = (SelectAllAction)(actionList.getAction(b.getString("kSelectAll")));
		selectall = edit.add(selectAllAction);
		edit.addSeparator();

		final UserSettingsAction userSettingsAction = (UserSettingsAction)(actionList.getAction(b.getString("kPreferences")));
        edit.add(userSettingsAction);

		edit.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent evt) {
				undoAll.setEnabled(undoAllAction.isEnabled());
				//undo.setEnabled(undoAction.isEnabled());
				//redo.setEnabled(redoAction.isEnabled());
				retainstns.setEnabled(retainAction.isEnabled());
				deletestns.setEnabled(deleteAction.isEnabled());
				selectall.setEnabled(selectAllAction.isEnabled());
				userSettingsAction.setEnabled(userSettingsAction.isEnabled());
			}
			public void menuDeselected(MenuEvent evt) {

			}
			public void menuCanceled(MenuEvent evt) {

			}
		});

		// view menu
		final ShowAllDataAction showAllAction = (ShowAllDataAction)(actionList.getAction(b.getString("kShowAllData")));
		showalldata = view.add(showAllAction);
		view.addSeparator();

		final LatLonViewAction latLonViewAction = (LatLonViewAction)(actionList.getAction(b.getString("kLatLon")));

		latlon = new JCheckBoxMenuItem(b.getString("kLatLon"));//latLonViewAction);
		latlon.addActionListener(latLonViewAction);
		view.add(latlon);

		final LonTimeViewAction lonTimeViewAction = (LonTimeViewAction)(actionList.getAction(b.getString("kLonTim")));
		lontim = new JCheckBoxMenuItem(b.getString("kLonTim"));//lonTimeViewAction);
		lontim.addActionListener(lonTimeViewAction);
		view.add(lontim);

		final LonDepthViewAction lonDepthViewAction = (LonDepthViewAction)(actionList.getAction(b.getString("kDepLon")));
		deplon = new JCheckBoxMenuItem(b.getString("kDepLon"));//lonDepthViewAction);
		deplon.addActionListener(lonDepthViewAction);
		view.add(deplon);

		final DepthTimeViewAction depthTimeViewAction = (DepthTimeViewAction)(actionList.getAction(b.getString("kDepTim")));
		deptim = new JCheckBoxMenuItem(b.getString("kDepTim"));//depthTimeViewAction);
		deptim.addActionListener(depthTimeViewAction);
		view.add(deptim);

		final LatDepthViewAction latDepthViewAction = (LatDepthViewAction)(actionList.getAction(b.getString("kDepLat")));
		deplat = new JCheckBoxMenuItem(b.getString("kDepLat"));//latDepthViewAction);
		deplat.addActionListener(latDepthViewAction);
		view.add(deplat);

		final LatTimeViewAction latTimeViewAction = (LatTimeViewAction)(actionList.getAction(b.getString("kLatTim")));
		lattim = new JCheckBoxMenuItem(b.getString("kLatTim"));//latTimeViewAction);
		lattim.addActionListener(latTimeViewAction);
		view.add(lattim);

		final ViewInspectorAction viewInspectorAction = (ViewInspectorAction)(actionList.getAction(b.getString("kSelectionInspector")));
		inspector = new JCheckBoxMenuItem(b.getString("kSelectionInspector"));
		inspector.addActionListener(viewInspectorAction);
		view.add(inspector);

		view.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent evt) {
				// set the state of the menu checkboxes
				latlon.setSelected(latLonViewAction.getState());
				lontim.setSelected(lonTimeViewAction.getState());
				deplon.setSelected(lonDepthViewAction.getState());
				deptim.setSelected(depthTimeViewAction.getState());
				deplat.setSelected(latDepthViewAction.getState());
				lattim.setSelected(latTimeViewAction.getState());
				inspector.setSelected(viewInspectorAction.getState());
				showalldata.setEnabled(showAllAction.isEnabled());
			}
			public void menuDeselected(MenuEvent evt) {

			}
			public void menuCanceled(MenuEvent evt) {

			}
		});

		// add zoom menu items
		final ZoomAllViewsAction zoomAllAction = (ZoomAllViewsAction)(actionList.getAction(b.getString("kZoomSelRgn")));
		zoom = zoomM.add(zoomAllAction);

		final ResetZoomAction resetZoomAction = (ResetZoomAction)(actionList.getAction(b.getString("kResetZoom")));
		resetzoom = zoomM.add(resetZoomAction);

		zoomM.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent evt) {
				zoom.setEnabled(zoomAllAction.isEnabled());
				resetzoom.setEnabled(resetZoomAction.isEnabled());
			}
			public void menuDeselected(MenuEvent evt) {

			}
			public void menuCanceled(MenuEvent evt) {

			}
		});

		// add help menu items
		AboutAction aboutAction = (AboutAction)(actionList.getAction(b.getString("kAbout")));
		help.add(aboutAction);

		NewsAction newsAction = (NewsAction)(actionList.getAction(b.getString("kNews")));
		help.add(newsAction);

		SystemPropertiesAction systemPropertiesAction = (SystemPropertiesAction)(actionList.getAction(b.getString("kSystemProperties")));
		help.add(systemPropertiesAction);

		// add to the menubar
		menubar.add(file);
		menubar.add(edit);
		menubar.add(view);
		menubar.add(zoomM);
		menubar.add(help);
	}
}

/*
 * $Id: NdEditView.java,v 1.15 2005/06/17 17:24:17 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit.ncBrowse;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.TableColumn;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;
import javax.swing.event.ListSelectionEvent;
import java.beans.*;
import java.awt.*;
import java.io.*;
import ndEdit.ncBrowse.map.ConfigAxisTransform;
import ndEdit.ncBrowse.map.NdEditDnDTable;
import ndEdit.ncBrowse.map.NdEditTableModel;
import ndEdit.ncBrowse.map.NdEditMapModel;
import ndEdit.ncBrowse.map.NdEditTargetMonitor;
import ndEdit.ncBrowse.map.NdEditMapParameter;
import ndEdit.ncBrowse.map.NcFlavorMap;
import ndEdit.ncBrowse.map.NdEditLabelTarget;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.util.*;
import java.text.*;
import ndEdit.*;
import gov.noaa.pmel.util.*;
import gov.noaa.pmel.swing.map.*;
import ucar.multiarray.MultiArray;
import ucar.nc2.*;
// import ucar.netcdf.DimensionIterator;
// import ucar.netcdf.DimensionSet;
// import ucar.netcdf.VariableIterator;
// import ucar.netcdf.Variable;
// import ucar.netcdf.AttributeIterator;
// import ucar.netcdf.AttributeSet;
// import ucar.netcdf.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.Attribute;
import ucar.ma2.ArrayAbstract;

/**
 * Creates a <code>JTable</code> summarizing the netCDF file variables.
 * 
 * @author Donald Denbo
 * @version $Revision: 1.15 $, $Date: 2005/06/17 17:24:17 $
 */

public class NdEditView extends javax.swing.JFrame implements ListSelectionListener, ItemListener, ButtonMaintainer {
	private NcFile ncFile;
	private JTable varTable = null;
	private NdEditDnDTable dimTable = null;
	private static NdEditTableModel dimModel = null;
	private JTable attTable = null;
	private String selectedVar = null;
	private boolean showAllVariables_ = false;
	private BorderLayout borderLayout1 = new BorderLayout(0, 0);
	private FlowLayout flowLayout1 = new FlowLayout(FlowLayout.CENTER, 20, 5);
	private JPanel targetPanel;
	private ResourceBundle b = ResourceBundle.getBundle("ndEdit.NdEditResources");
	private static NdEditMapModel currentMap = null;
	private NdEditTargetMonitor tm;
	private javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
	private javax.swing.JButton closeButton = new javax.swing.JButton();
	private javax.swing.JButton browseButton = new javax.swing.JButton();
	private javax.swing.JButton regridButton = new javax.swing.JButton();
	private javax.swing.JButton clearButton = new javax.swing.JButton();
	private javax.swing.JButton autoMapButton = new javax.swing.JButton();
	private javax.swing.JPanel tablePanel = new javax.swing.JPanel();
	private javax.swing.JScrollPane varScrollPane = new javax.swing.JScrollPane();
	private javax.swing.JScrollPane dimScrollPane = new javax.swing.JScrollPane();
	private TargetPanel theTargetPanel = null;
	private JFrame mThis;
	private JComboBox varList = null;
	private javax.swing.JButton treeViewButton = new javax.swing.JButton();
	private javax.swing.JButton tableViewButton = new javax.swing.JButton();
	NdEdit mParent;
	private Vector mVars;
	TimeRange tRange_ = null;
	TimeRange tValue_ = null;
	Domain domain_ = null;
	JButton latTransform = null;
	JButton lonTransform = null;
	JButton zTransform = null;
	JButton timeTransform = null;
	JButton addLat = null;
	JButton addLon = null;
	JButton addZ = null;
	JButton addT = null;

	public NdEditView(NdEdit parent) {
		mThis = this;
		mParent = parent;
		try {
			jbInit();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void jbInit() throws Exception {
		try {
			ImageIcon icon = new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/fcn.gif"));
			latTransform = new JButton(icon);
			lonTransform = new JButton(icon);
			zTransform = new JButton(icon);
			timeTransform = new JButton(icon);
			ImageIcon icon2 = new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/plus.gif"));
			addLat = new JButton(icon2);
			addLon = new JButton(icon2);
			addZ = new JButton(icon2);
			addT = new JButton(icon2);
		}
		catch (Exception ex) {
			ex.printStackTrace();
    	System.out.println("NdEditView:jbInit");
		}
		getContentPane().setLayout(borderLayout1);
		setSize(850, 300);
		setVisible(false);
		buttonPanel.setLayout(flowLayout1);
		getContentPane().add(buttonPanel, "South");
		closeButton.setText(b.getString("kClose"));
		closeButton.setActionCommand("close");
		buttonPanel.add(closeButton);
		browseButton.setText(b.getString("kExtract"));
		browseButton.setActionCommand("extract");
		buttonPanel.add(browseButton);

		treeViewButton.setText(b.getString("kTreeView"));
		treeViewButton.setActionCommand("treeview");
		buttonPanel.add(treeViewButton);

		tableViewButton.setText(b.getString("kTableView"));
		tableViewButton.setActionCommand("tableview");
		buttonPanel.add(tableViewButton);

		tablePanel.setLayout(new GridLayout(1, 3, 0, 1));
		getContentPane().add(tablePanel, "Center");

		// this pane contains the variable list
		JPanel varContents = new JPanel();
		TitledBorder varBorder = new TitledBorder(b.getString("kVariables"));
		varContents.setBorder(varBorder);
		varContents.setLayout(new BorderLayout(5, 5));
		varContents.add("Center", varScrollPane);
		tablePanel.add(new TenPixelBorder(varContents, 5, 5, 5, 5));

		// this pane contains the dimensions for selected variables
		JPanel dimContents = new JPanel();
		TitledBorder dimBorder = new TitledBorder(b.getString("kDimensions"));
		dimContents.setBorder(dimBorder);
		dimContents.setLayout(new BorderLayout(5, 5));
		dimContents.add("Center", dimScrollPane);

		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
		dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		// dlgBtnsPanel.setLayout(new GridLayout(1, 2, 15, 1));
		dlgBtnsInset.add(new JLabel(b.getString("kRegridTo")));
		Vector v = new Vector();
		v.addElement(b.getString("kNoVariablesSelected"));
		varList = new JComboBox(v);
		dlgBtnsInset.add(varList);
		// dlgBtnsInset.add(dlgBtnsPanel);
		dimContents.add("South", dlgBtnsInset);
		tablePanel.add(new TenPixelBorder(dimContents, 5, 5, 5, 5));

		// add the target panel
		TitledBorder targetBorder = new TitledBorder(b.getString("kNdEditAxes"));
		targetPanel = new JPanel();
		targetPanel.setBorder(targetBorder);
		tablePanel.add(new TenPixelBorder(targetPanel, 5, 5, 5, 5));

		SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);
		SymAction lSymAction = new SymAction();
		closeButton.addActionListener(lSymAction);
		browseButton.addActionListener(lSymAction);
		clearButton.addActionListener(lSymAction);
		regridButton.addActionListener(lSymAction);
		autoMapButton.addActionListener(lSymAction);
		tableViewButton.addActionListener(lSymAction);
		treeViewButton.addActionListener(lSymAction);
		varList.addItemListener(this);
		// setTitle("A Simple Frame");

		MaintenanceTimer mMaintain = new MaintenanceTimer(this, 100);
		mMaintain.startMaintainer();
	}

	public NdEditView(String title, NdEdit parent) {
		this(parent);
		setTitle(title);
	}

	public void setVisible(boolean b) {
		if (b) {
			setLocation(50, 50);
		}
		super.setVisible(b);
	}

	public void addNotify() {
		// Record the size of the window prior to calling parents addNotify.
		java.awt.Dimension d = getSize();

		super.addNotify();

		if (fComponentsAdjusted)
			return;

		// Adjust components according to the insets
		Insets ins = getInsets();
		setSize(ins.left + ins.right + d.width, ins.top + ins.bottom + d.height);
		Component components[] = getContentPane().getComponents();
		for (int i = 0; i < components.length; i++) {
			Point p = components[i].getLocation();
			p.translate(ins.left, ins.top);
			components[i].setLocation(p);
		}
		fComponentsAdjusted = true;
	}

	// Used for addNotify check.
	boolean fComponentsAdjusted = false;
	private int mLatPos;
	private int mLonPos;
	private int mZPos;
	private int mTPos;

	class SymWindow extends java.awt.event.WindowAdapter {
		public void windowClosing(java.awt.event.WindowEvent event) {
			Object object = event.getSource();
			if (object == NdEditView.this)
				NdEditView_WindowClosing(event);
		}
	}

	void NdEditView_WindowClosing(java.awt.event.WindowEvent event) {
		dispose(); // dispose of the Frame.
	}

	public void setShowAllVariables(boolean show) {
		showAllVariables_ = show;
	}

	public NcFile getNcFile() {
		return ncFile;
	}

	public void setNcFile(NcFile ncFile) {
		// create the map
		currentMap = new NdEditMapModel(ncFile);

		Iterator itor = ncFile.getGlobalAttributeIterator();

		// add the global attributes
		while (itor.hasNext()) {
			currentMap.addAttribute((Attribute) itor.next());
		}

		this.ncFile = ncFile;
		String name = ncFile.getFileName();
		this.setTitle(name);// + " (" + b.getString("kNdEditView") + ")");
		varTable = new VariableTable(ncFile, VariableTable.NON_DIMENSION, false,
		    ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		varScrollPane.getViewport().add(varTable);

		// make a blank "source" table
		makeDimensionTable();

		// add it to the UI
		dimScrollPane.getViewport().add(dimTable);

		// need to be able to listen to list selection events from the variable list
		// and get the selected variable
		varTable.getSelectionModel().addListSelectionListener(this);

		theTargetPanel = new TargetPanel();
		targetPanel.add(theTargetPanel);
	}

	public static NdEditMapModel getCurrentMap() {
		return currentMap;
	}

	public static NdEditTableModel getCurrentModel() {
		return dimModel;
	}

	public static void updateAll(NdEditMapModel map) {
		NdEditTargetMonitor.getInstance().updateAll(map);
	}

	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() instanceof JComboBox) {
			JComboBox cb = (JComboBox) evt.getSource();
			if (cb == varList && evt.getStateChange() == ItemEvent.SELECTED) {
				// this Vector will store the resulting variables
				Vector foundVars = new Vector();

				// get the variables for the selected rows
				int[] rows = varTable.getSelectedRows();
				Iterator varIter = null;
				varIter = ncFile.getNonDimensionVariables();
				while (varIter.hasNext()) {
					Variable var = (Variable) varIter.next();
					StringBuffer sbuf = new StringBuffer();
					String name = var.getName();
					for (int i = 0; i < rows.length; i++) {
						String tableVar = (String) varTable.getValueAt(rows[i], 0);
						if (tableVar.equals(name)) {
							// found a match--put it into a Vector
							foundVars.addElement(var);
							break;
						}
					}
				}
				// get the variable
				String selVar = (String) varList.getSelectedItem();
				int selVarIndx = 0;
				for (int i = 0; i < foundVars.size(); i++) {
					Variable var = (Variable) foundVars.elementAt(i);
					if (selVar.equalsIgnoreCase(var.getName())) {
						selVarIndx = i;
						break;
					}
				}

				// if there are matches display them in the source table
				if (foundVars.size() > 0) {
					setDimensionTable(foundVars, ncFile, selVarIndx);
				}
			}
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			// The mouse button has not yet been released
			int[] rows = varTable.getSelectedRows();

			// this Vector will store the resulting variables
			Vector foundVars = new Vector();

			// get the variables for the selected rows
			Iterator varIter = null;
			varIter = ncFile.getNonDimensionVariables();
			while (varIter.hasNext()) {
				Variable var = (Variable) varIter.next();
				StringBuffer sbuf = new StringBuffer();
				String name = var.getName();
				for (int i = 0; i < rows.length; i++) {
					String tableVar = (String) varTable.getValueAt(rows[i], 0);
					if (tableVar.equals(name)) {
						// found a match--put it into a Vector
						foundVars.addElement(var);
						break;
					}
				}
			}

			// if there are matches display them in the source table
			if (foundVars.size() > 0) {
				setDimensionTable(foundVars, ncFile);
			}
		}
	}

	public static void valueChanged(int type) {
		NdEditTargetMonitor.getInstance().valueChanged(type);
	}

	private void makeDimensionTable() {
		String[][] data = new String[6][2];
		for (int i = 0; i < 6; i++) {
			data[i][0] = "";
			data[i][1] = "";
		}

		dimTable = new NdEditDnDTable(data, new String[] { b.getString("kName2"), b.getString("kDescription") });
		dimTable.setSize(1000, 1000);
		dimTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// dimTable.
		TableColumn tc;
		tc = dimTable.getColumnModel().getColumn(0);
		tc.setPreferredWidth(100);
		tc = dimTable.getColumnModel().getColumn(1);
		tc.setPreferredWidth(350);
	}

	private void setDimensionTable(Vector inVars, NcFile ncFile) {
		mVars = inVars;

		/*
		 * double[] vals = null; try { ucar.ma2.Array valMa = vv.read(new
		 * int[vv.getRank()], vv.getShape()); vals = valMa.(double[])valMa.\; }
		 * catch (Exception ex) {}
		 */

		dimModel = new NdEditTableModel(inVars, ncFile);
		dimTable.setModel(dimModel);

		// now add variables to var list if necessary
		if (dimModel != null && !dimModel.isSameGrid()) {
			varList.removeAllItems();
			varList.setEnabled(true);
			for (int i = 0; i < inVars.size(); i++) {
				Variable v = (Variable) inVars.elementAt(i);
				varList.addItem(v.getName());
			}
			JFrame f = new JFrame(b.getString("kMulipleGridError"));
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(f, b.getString("kMulipleGridPromptLine1") + "\n"
			    + b.getString("kMulipleGridPromptLine2"));
		}
		else {
			varList.removeAllItems();
			varList.setEnabled(false);
			varList.addItem(b.getString("kVariablesOnSameGrid"));
		}
	}

	private void setDimensionTable(Vector inVars, NcFile ncFile, int whichVar) {
		mVars = inVars;
		dimModel = new NdEditTableModel(inVars, ncFile, whichVar);
		dimTable.setModel(dimModel);
	}

	public String stripBlanks(String in) {
		StringBuffer sbuf = new StringBuffer(in);
		int len;
		int i;
		len = sbuf.length();

		// remove trailing blanks
		for (i = len - 1; i >= 0; i--) {
			if (sbuf.charAt(i) != ' ') {
				len = i + 1;
				break;
			}
		}
		sbuf.setLength(len);
		return sbuf.toString();
	}

	public String cleanAttribute(String in) {
		int i;
		int len;
		StringBuffer sbuf = new StringBuffer(in.length());
		int start = 0;
		int stop = in.length();
		if (in.charAt(0) == ':')
			start = 1;
		if (in.charAt(stop - 1) == ';')
			stop--;
		for (i = start; i < stop; i++) {
			if (in.charAt(i) != 0)
				sbuf = sbuf.append(in.charAt(i));
		}

		// remove trailing blanks
		len = sbuf.length();
		for (i = len - 1; i >= 0; i--) {
			if (sbuf.charAt(i) != ' ') {
				len = i + 1;
				break;
			}
		}

		int newlen = len;
		if (sbuf.charAt(len - 1) == '"') {
			// remove blanks right of last quote
			for (i = len - 2; i >= 0; i--) {
				if (sbuf.charAt(i) != ' ') {
					sbuf.setCharAt(i + 1, '"');
					newlen = i + 2;
					break;
				}
			}
			sbuf.setLength(newlen);
		}
		return sbuf.toString();
	}

	class SymAction implements java.awt.event.ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent event) {
			Object object = event.getSource();
			if (object == closeButton)
				closeButton_actionPerformed(event);
			else if (object == regridButton)
				regridButton_actionPerformed(event);
			else if (object == clearButton)
				clearButton_actionPerformed(event);
			else if (object == autoMapButton)
				autoMapButton_actionPerformed(event);
			else if (object == tableViewButton)
				asTableItem_actionPerformed(event);
			else if (object == treeViewButton)
				asTreeItem_actionPerformed(event);
			else if (object == browseButton)
				displayGrid_actionPerformed(event);
		}
	}

	void displayGrid_actionPerformed(java.awt.event.ActionEvent event) {
		// get a pointer collection
		domain_ = new Domain(new Range2D(155.0, 200.0), new Range2D(65.0, 45.0));
		try {
			tRange_ = new TimeRange(new GeoDate("1980-01-01", "yyyy-MM-dd"), new GeoDate());
			tValue_ = new TimeRange(new GeoDate("1980-01-01", "yyyy-MM-dd"), new GeoDate());
		}
		catch (IllegalTimeValue ex) {
		}

		boolean domainSet_ = false;
		Range2D xr = domain_.getXRange();
		Range2D yr = domain_.getYRange();
		/*
		 * JMapDialog map = new JMapDialog(this); // map.addDataRegion(45.0, -15.0,
		 * 190.0, 250.0); map.setDefaultSelection(yr.start, yr.end, xr.start,
		 * xr.end); map.setTimeRange(tRange_); map.setTimeValue(tValue_);
		 * map.initMap(); Dimension dlgSize = map.getSize(); Dimension frmSize =
		 * getSize(); Point loc = getLocation(); map.setLocation((frmSize.width -
		 * dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 +
		 * loc.y); int result = map.openDialog(); if (result == JMapDialog.OK) {
		 * domainSet_ = true; tValue_.start = map.getStartValue(); tValue_.end =
		 * map.getEndValue(); xr.start = map.getWestLongitude(); xr.end =
		 * map.getEastLongitude(); yr.start = map.getNorthLatitude(); yr.end =
		 * map.getSouthLatitude(); domain_.setXRange(xr); domain_.setYRange(yr); }
		 */

		mParent.setDataTitle(this.getTitle());
		long st = System.currentTimeMillis();
		mParent.setPointerCollection((PointerCollection) this.getPointerCollection(), true);
		mParent.getViewManager().invalidateAllViews();
		// System.out.println("et = " + (System.currentTimeMillis() - st));
	}

	void closeButton_actionPerformed(java.awt.event.ActionEvent event) {
		try {
			this.setVisible(false);
			this.dispose();
		}
		catch (java.lang.Exception e) {
		}
	}

	void regridButton_actionPerformed(java.awt.event.ActionEvent event) {
		try {
			// this.setVisible(false);
			// this.dispose();
		}
		catch (java.lang.Exception e) {
		}
	}

	void clearButton_actionPerformed(java.awt.event.ActionEvent event) {
		try {
			NdEditTargetMonitor.getInstance().reset();
			this.getCurrentMap().reset();
		}
		catch (java.lang.Exception e) {
		}
	}

	void asTableItem_actionPerformed(java.awt.event.ActionEvent event) {
		TableView tblView = new TableView();
		tblView.setShowAllVariables(true);
		tblView.setNcFile(ncFile);
		Point pt = this.getLocationOnScreen();
		tblView.setLocation(pt.x + 50, pt.y + 50);
		tblView.setVisible(true);
	}

	void asTreeItem_actionPerformed(java.awt.event.ActionEvent event) {
		TreeView treeView = new TreeView();
		treeView.setShowAllVariables(true);
		treeView.setNcFile(ncFile);
		Point pt = this.getLocationOnScreen();
		treeView.setLocation(pt.x + 50, pt.y + 50);
		treeView.setVisible(true);
	}

	void autoMapButton_actionPerformed(java.awt.event.ActionEvent event) {
		// try to map dimensions into NdEdit axes
		int numDims = dimModel.getRowCount();

		boolean latMapped = false;
		boolean lonMapped = false;
		boolean zMapped = false;
		boolean timeMapped = false;
		ucar.nc2.Dimension latDim = null;
		ucar.nc2.Dimension lonDim = null;
		ucar.nc2.Dimension zDim = null;
		ucar.nc2.Dimension timeDim = null;
		
		mLatPos = -99;
		mLonPos = -99;
		mZPos = -99;
		mTPos = -99;

		for (int i = 0; i < numDims; i++) {
			ucar.nc2.Dimension dim = (ucar.nc2.Dimension) (dimModel.getDimAt(i));
			Variable ncVar = dim.getCoordinateVariable();
			String varName;
			try {
				varName = ncVar.getName().toLowerCase();
			}
			catch (Exception ex) {
				continue;
				// probably should try the dim name in this case
			}

			// Step #1: try the obvious matches
			if (varName.startsWith("lat")) {
				latMapped = true;
				latDim = (ucar.nc2.Dimension) (dimModel.getDimAt(i));
				mLatPos = i;
				continue;
			}
			else if (varName.startsWith("lon")) {
				lonMapped = true;
				lonDim = (ucar.nc2.Dimension) (dimModel.getDimAt(i));
				mLonPos = i;
				continue;
			}
			else if (varName.startsWith("z") || varName.startsWith("depth")) {
				zMapped = true;
				zDim = (ucar.nc2.Dimension) (dimModel.getDimAt(i));
				mZPos = i;
				continue;
			}
			else if (varName.startsWith("time")) {
				timeMapped = true;
				timeDim = (ucar.nc2.Dimension) (dimModel.getDimAt(i));
				mTPos = i;
				continue;
			}

			// Step #2: look for attributes that describe the axes
			String dimUnits;
			Attribute attr = ncVar.findAttribute("units");
			if (attr != null) {
				dimUnits = attr.getStringValue().toLowerCase();
				if (dimUnits.indexOf("deg") >= 0) {
					if (dimUnits.indexOf("north") >= 0 || dimUnits.indexOf("north") >= 0) {
						latMapped = true;
						latDim = (ucar.nc2.Dimension) (dimModel.getDimAt(i));
						mLatPos = i;
						continue;
					}
					else if (dimUnits.indexOf("east") >= 0 || dimUnits.indexOf("west") >= 0) {
						lonMapped = true;
						lonDim = (ucar.nc2.Dimension) (dimModel.getDimAt(i));
						mLonPos = i;
						continue;
					}
				}
				else if (dimUnits.indexOf("m") >= 0 || dimUnits.indexOf("db") >= 0) {
					zMapped = true;
					zDim = (ucar.nc2.Dimension) (dimModel.getDimAt(i));
					mZPos = i;
					continue;
				}
				else if (dimUnits.indexOf("day") >= 0 || dimUnits.indexOf("hour") >= 0) {
					timeMapped = true;
					timeDim = (ucar.nc2.Dimension) (dimModel.getDimAt(i));
					mTPos = i;
					continue;
				}
			}

			// Step #3: Match an "x" axis with longitude, "y" with latitude etc...
			if (varName.startsWith("y")) {
				latMapped = true;
				latDim = (ucar.nc2.Dimension) (dimModel.getDimAt(i));
				mLatPos = i;
				continue;
			}
			else if (varName.startsWith("x")) {
				lonMapped = true;
				lonDim = (ucar.nc2.Dimension) (dimModel.getDimAt(i));
				mLonPos = i;
				continue;
			}
			else if (varName.startsWith("z")) {
				zMapped = true;
				zDim = (ucar.nc2.Dimension) (dimModel.getDimAt(i));
				mZPos = i;
				continue;
			}
			else if (varName.startsWith("t")) {
				timeMapped = true;
				timeDim = (ucar.nc2.Dimension) (dimModel.getDimAt(i));
				mTPos = i;
				continue;
			}
		}

		// reset any current mappings
		if (lonMapped || latMapped || zMapped || timeMapped) {
			tm.reset();

			// apply any mappings to the target panel
			if (lonMapped) {
				getCurrentMap().setElement(lonDim, NdEditMapModel.LONGITUDE);
			}
			if (latMapped) {
				getCurrentMap().setElement(latDim, NdEditMapModel.LATITUDE);
			}
			if (zMapped) {
				getCurrentMap().setElement(zDim, NdEditMapModel.Z);
			}
			if (timeMapped) {
				getCurrentMap().setElement(timeDim, NdEditMapModel.TIME);
			}
			updateAll(getCurrentMap());
		}

	}

	private class TargetPanel extends JPanel {
		JPanel jPanel1 = new JPanel();
		JLabel jLabel1 = new JLabel();
		GridBagLayout gridBagLayout9 = new GridBagLayout();
		JPanel jPanel2 = new JPanel();
		BorderLayout borderLayout3 = new BorderLayout();
		JPanel jPanel3 = new JPanel();
		JLabel jLabel3 = new JLabel();
		GridBagLayout gridBagLayout10 = new GridBagLayout();
		JPanel targetPanel = new JPanel();

		// drag target widgets
		JLabel latAxisLabel = new JLabel();
		JLabel zAxisLabel = new JLabel();
		JLabel timeAxisLabel = new JLabel();
		JLabel lonAxisLabel = new JLabel();
		JLabel latAxisTitle = new JLabel();
		JLabel zAxisTitle = new JLabel();
		JLabel timeAxisTitle = new JLabel();
		JLabel lonAxisTitle = new JLabel();
		JPanel latAxisPanel = new JPanel();
		JPanel zAxisPanel = new JPanel();
		JPanel timeAxisPanel = new JPanel();
		JPanel lonAxisPanel = new JPanel();

		BorderLayout borderLayout4 = new BorderLayout();
		BorderLayout borderLayout5 = new BorderLayout();
		BorderLayout borderLayout6 = new BorderLayout();
		BorderLayout borderLayout7 = new BorderLayout();
		BorderLayout borderLayout8 = new BorderLayout();
		BorderLayout borderLayout9 = new BorderLayout();

		Insets m = new Insets(0, 0, 0, 0);

		public TargetPanel() {
			GridBagLayout gridBagLayout1 = new GridBagLayout();
			this.setLayout(new BorderLayout(5, 5));
			JPanel dragTargsPanel = new JPanel();

			dragTargsPanel.setLayout(gridBagLayout1);
			tm = NdEditTargetMonitor.getInstance();

			tm.setItem(NdEditMapModel.LONGITUDE, lonAxisLabel, lonAxisPanel, lonAxisTitle);
			tm.setItem(NdEditMapModel.LATITUDE, latAxisLabel, latAxisPanel, latAxisTitle);
			tm.setItem(NdEditMapModel.Z, zAxisLabel, zAxisPanel, zAxisTitle);
			tm.setItem(NdEditMapModel.TIME, timeAxisLabel, timeAxisPanel, timeAxisTitle);

			NcFlavorMap ncfm = NcFlavorMap.getInstance();
			latTransform.setMargin(m);
			lonTransform.setMargin(m);
			zTransform.setMargin(m);
			timeTransform.setMargin(m);

			// Time component
			timeAxisPanel.setBackground(new Color(186, 217, 217));
			timeAxisLabel.setDropTarget(new DropTarget(timeAxisLabel, DnDConstants.ACTION_COPY_OR_MOVE,
			    new NdEditLabelTarget(timeAxisLabel, NdEditMapModel.TIME), true, ncfm));
			timeAxisLabel.setForeground(Color.black);
			timeAxisLabel.setMaximumSize(new java.awt.Dimension(0, 35));
			timeAxisLabel.setMinimumSize(new java.awt.Dimension(0, 35));
			timeAxisLabel.setPreferredSize(new java.awt.Dimension(0, 35));
			timeAxisTitle.setText(b.getString("kTime"));
			timeAxisPanel.setBorder(BorderFactory.createLoweredBevelBorder());
			timeAxisPanel.setLayout(borderLayout7);

			// Lon Axis
			lonAxisLabel.setDropTarget(new DropTarget(lonAxisLabel, DnDConstants.ACTION_COPY_OR_MOVE, new NdEditLabelTarget(
			    lonAxisLabel, NdEditMapModel.LONGITUDE), true, ncfm));
			lonAxisLabel.setForeground(Color.black);
			lonAxisLabel.setMaximumSize(new java.awt.Dimension(0, 35));
			lonAxisLabel.setMinimumSize(new java.awt.Dimension(0, 35));
			lonAxisLabel.setPreferredSize(new java.awt.Dimension(0, 35));
			lonAxisPanel.setBackground(new Color(186, 217, 217));
			lonAxisPanel.setBorder(BorderFactory.createLoweredBevelBorder());
			lonAxisPanel.setLayout(borderLayout8);
			lonAxisTitle.setText(b.getString("kLongitude"));

			// Z Axis
			zAxisLabel.setDropTarget(new DropTarget(zAxisLabel, DnDConstants.ACTION_COPY_OR_MOVE, new NdEditLabelTarget(
			    zAxisLabel, NdEditMapModel.Z), true, ncfm));
			zAxisLabel.setForeground(Color.black);
			zAxisLabel.setMaximumSize(new java.awt.Dimension(0, 35));
			zAxisLabel.setMinimumSize(new java.awt.Dimension(0, 35));
			zAxisLabel.setPreferredSize(new java.awt.Dimension(0, 35));
			zAxisPanel.setBackground(new Color(186, 217, 217));
			zAxisTitle.setText(b.getString("kZ"));
			zAxisPanel.setBorder(BorderFactory.createLoweredBevelBorder());
			zAxisPanel.setLayout(borderLayout6);

			// Lat Axis
			latAxisPanel.setBackground(new Color(186, 217, 217));
			latAxisPanel.setBorder(BorderFactory.createLoweredBevelBorder());
			latAxisPanel.setLayout(borderLayout5);
			latAxisLabel.setDropTarget(new DropTarget(latAxisLabel, DnDConstants.ACTION_COPY_OR_MOVE, new NdEditLabelTarget(
			    latAxisLabel, NdEditMapModel.LATITUDE), true, ncfm));
			latAxisLabel.setForeground(Color.black);
			latAxisLabel.setMaximumSize(new java.awt.Dimension(0, 35));
			latAxisLabel.setMinimumSize(new java.awt.Dimension(0, 35));
			latAxisLabel.setPreferredSize(new java.awt.Dimension(0, 35));
			latAxisTitle.setText(b.getString("kLatitude"));

			dragTargsPanel.add(lonAxisTitle, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
			    GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
			dragTargsPanel.add(lonAxisPanel, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			    GridBagConstraints.BOTH, new Insets(2, 0, 2, 5), 0, 0));
			dragTargsPanel.add(addLon, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			dragTargsPanel.add(lonTransform, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			lonAxisPanel.add(lonAxisLabel, BorderLayout.CENTER);

			dragTargsPanel.add(latAxisTitle, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
			    GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
			dragTargsPanel.add(latAxisPanel, new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			    GridBagConstraints.BOTH, new Insets(2, 0, 2, 5), 0, 0));
			dragTargsPanel.add(addLat, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			dragTargsPanel.add(latTransform, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			latAxisPanel.add(latAxisLabel, BorderLayout.CENTER);

			dragTargsPanel.add(zAxisTitle, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
			    GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
			dragTargsPanel.add(zAxisPanel, new GridBagConstraints(1, 3, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			    GridBagConstraints.BOTH, new Insets(2, 0, 2, 5), 0, 0));
			dragTargsPanel.add(addZ, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			dragTargsPanel.add(zTransform, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			zAxisPanel.add(zAxisLabel, BorderLayout.CENTER);

			dragTargsPanel.add(timeAxisTitle, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
			    GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
			dragTargsPanel.add(timeAxisPanel, new GridBagConstraints(1, 4, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			    GridBagConstraints.BOTH, new Insets(2, 0, 2, 5), 0, 0));
			dragTargsPanel.add(addT, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
			    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			dragTargsPanel.add(timeTransform, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			timeAxisPanel.add(timeAxisLabel, BorderLayout.CENTER);
			tm.reset();

			// add the drop targets in the north of the overall layout
			this.add("North", dragTargsPanel);

			// create the button panel
			clearButton.setText(b.getString("kClear"));
			clearButton.setActionCommand("clear");
			autoMapButton.setText(b.getString("kAutoMap"));
			autoMapButton.setActionCommand("automap");

			JPanel dlgBtnsInset = new JPanel();
			JPanel dlgBtnsPanel = new JPanel();
			dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
			dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
			dlgBtnsPanel.add(clearButton);
			dlgBtnsPanel.add(autoMapButton);
			dlgBtnsInset.add(dlgBtnsPanel);

			this.add("South", dlgBtnsInset);
			TransformActions transActions = new TransformActions();
			latTransform.addActionListener(transActions);
			lonTransform.addActionListener(transActions);
			zTransform.addActionListener(transActions);
			timeTransform.addActionListener(transActions);

			AddAxisActions addAxisActions = new AddAxisActions();
			addLat.addActionListener(addAxisActions);
			addLon.addActionListener(addAxisActions);
			addZ.addActionListener(addAxisActions);
			addT.addActionListener(addAxisActions);
		}

		public JLabel getLonAxisLabel() {
			return lonAxisLabel;
		}

		public JLabel getLatAxisLabel() {
			return latAxisLabel;
		}

		public JLabel getZLabel() {
			return zAxisLabel;
		}

		public JLabel getTimeLabel() {
			return timeAxisLabel;
		}

		/*
		 * public void setTransformBtns(boolean mode) { if (timeTransform != null)
		 * timeTransform.setEnabled(mode); if (latTransform != null)
		 * latTransform.setEnabled(mode); if (lonTransform != null)
		 * lonTransform.setEnabled(mode); if (zTransform != null)
		 * zTransform.setEnabled(mode); }
		 * 
		 * public void setAddAxisBtns(boolean mode) { if (timeTransform == null)
		 * addT.setEnabled(mode); if (latTransform == null) addLat.setEnabled(mode);
		 * if (lonTransform == null) addLon.setEnabled(mode); if (zTransform ==
		 * null) addZ.setEnabled(mode); }
		 */

		class TransformActions implements java.awt.event.ActionListener {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				Object object = event.getSource();
				if (object == timeTransform)
					transformButton_actionPerformed(getCurrentMap(), NdEditMapModel.TIME);
				else if (object == latTransform)
					transformButton_actionPerformed(getCurrentMap(), NdEditMapModel.LATITUDE);
				else if (object == lonTransform)
					transformButton_actionPerformed(getCurrentMap(), NdEditMapModel.LONGITUDE);
				else if (object == zTransform)
					transformButton_actionPerformed(getCurrentMap(), NdEditMapModel.Z);
			}
		}

		class AddAxisActions implements java.awt.event.ActionListener {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				Object object = event.getSource();
				if (object == addT)
					addAxis_actionPerformed(getCurrentMap(), NdEditMapModel.TIME);
				else if (object == addLat)
					addAxis_actionPerformed(getCurrentMap(), NdEditMapModel.LATITUDE);
				else if (object == addLon)
					addAxis_actionPerformed(getCurrentMap(), NdEditMapModel.LONGITUDE);
				else if (object == addZ)
					addAxis_actionPerformed(getCurrentMap(), NdEditMapModel.Z);
			}
		}

		void transformButton_actionPerformed(NdEditMapModel map, int axis) {
			// Present the transform interface for the selected axis
			ConfigAxisTransform config = new ConfigAxisTransform(mThis, map, axis, Constants.ISMAC);
			config.setSize(550, 500);
			Rectangle dBounds = config.getBounds();
			java.awt.Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
			int x = sd.width / 2 - dBounds.width / 2;
			int y = sd.height / 2 - dBounds.height / 2;
			config.setLocation(x, y);
			config.setVisible(true);
		}

		void addAxis_actionPerformed(NdEditMapModel map, int axis) {
			// adds an axis parameter in case one can't be mapped to this axis

			// Dimensions can only be created via a NetcdfFileWritable
			try {
				String path = System.getProperty("user.dir") + File.separator + "tmp.nc";
				NetcdfFileWriteable nr = new NetcdfFileWriteable();
				nr.setName(path);

				// create a dimension variable
				if (axis == NdEditMapModel.TIME) {
					ucar.nc2.Dimension[] array_time_dim = new ucar.nc2.Dimension[1];
					ucar.nc2.Dimension time_dim = nr.addDimension("time", 1);
					array_time_dim[0] = time_dim;
					nr.addVariable("time", double.class, array_time_dim);
					nr.addVariableAttribute("time", "units", "msec since 1970-01-01 00:00:00");

					double[] time = new double[1];
					GeoDate gd = new GeoDate();
					time[0] = (double) gd.getTime();
					ArrayAbstract dataArrTime = ArrayAbstract.factory(time);

					// create the file
					nr.create();

					// add the data
					nr.write("time", dataArrTime);
					nr.close();

					LocalNcFile ncf = new LocalNcFile(path);
					map.setElement(ncf, time_dim, NdEditMapModel.TIME);
				}
				else if (axis == NdEditMapModel.LATITUDE) {
					ucar.nc2.Dimension[] array_l_dim = new ucar.nc2.Dimension[1];
					ucar.nc2.Dimension l_dim = nr.addDimension("latitude", 1);
					array_l_dim[0] = l_dim;
					nr.addVariable("latitude", double.class, array_l_dim);
					nr.addVariableAttribute("latitude", "units", "degrees north");

					double[] lats = new double[1];
					lats[0] = 0.0;
					ArrayAbstract dataArrL = ArrayAbstract.factory(lats);

					// create the file
					nr.create();

					// add the data
					nr.write("latitude", dataArrL);
					nr.close();

					LocalNcFile ncf = new LocalNcFile(path);
					map.setElement(ncf, l_dim, NdEditMapModel.LATITUDE);
				}
				else if (axis == NdEditMapModel.LONGITUDE) {
					ucar.nc2.Dimension[] array_l_dim = new ucar.nc2.Dimension[1];
					ucar.nc2.Dimension l_dim = nr.addDimension("longitude", 1);
					array_l_dim[0] = l_dim;
					nr.addVariable("longitude", double.class, array_l_dim);
					nr.addVariableAttribute("longitude", "units", "degrees west");

					double[] lons = new double[1];
					lons[0] = 0.0;
					ArrayAbstract dataArrL = ArrayAbstract.factory(lons);

					// create the file
					nr.create();

					// add the data
					nr.write("longitude", dataArrL);
					nr.close();

					LocalNcFile ncf = new LocalNcFile(path);
					map.setElement(ncf, l_dim, NdEditMapModel.LONGITUDE);
				}
				else if (axis == NdEditMapModel.Z) {
					ucar.nc2.Dimension[] array_d_dim = new ucar.nc2.Dimension[1];
					ucar.nc2.Dimension d_dim = nr.addDimension("depth", 1);
					array_d_dim[0] = d_dim;
					nr.addVariable("depth", double.class, array_d_dim);
					nr.addVariableAttribute("depth", "units", "db");

					double[] zs = new double[1];
					zs[0] = 0.0;
					ArrayAbstract dataArrD = ArrayAbstract.factory(zs);

					// create the file
					nr.create();

					// add the data
					nr.write("depth", dataArrD);
					nr.close();

					LocalNcFile ncf = new LocalNcFile(path);
					map.setElement(ncf, d_dim, NdEditMapModel.Z);
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}

			// update the display
			updateAll(map);
		}
	}

	public void maintainButtons() {
		if (dimModel != null && !dimModel.isSameGrid())
			varList.setEnabled(true);
		else
			varList.setEnabled(false);

		if (dimModel != null)
			autoMapButton.setEnabled(true);
		else
			autoMapButton.setEnabled(false);

		if (this.getCurrentMap() != null && this.getCurrentMap().getDimCount() > 0) {
			clearButton.setEnabled(true);
			// if (theTargetPanel != null)
			// theTargetPanel.setTransformBtns(true);
		}
		else {
			clearButton.setEnabled(false);
			// if (theTargetPanel != null)
			// theTargetPanel.setTransformBtns(false);
		}
		if (this.getCurrentMap() != null)
			browseButton.setEnabled(this.getCurrentMap().isMapCorrect());

		// System.out.println("this.getCurrentMap() = " + this.getCurrentMap());
		// if (this.getCurrentMap() != null)
		// System.out.println("this.getCurrentMap().isMapCorrect() = " +
		// this.getCurrentMap().isMapCorrect());

		// maintain the browse button: lon,lat, and z have to have been mapped
		if (this.getCurrentMap() != null && this.getCurrentMap().getDimCount() > 0) {
			// maintain the add axis and transform btna
			if (this.getCurrentMap().getParamElement(0) == null) {
				addLon.setEnabled(true);
				lonTransform.setEnabled(false);
			}
			else {
				addLon.setEnabled(false);
				lonTransform.setEnabled(true);
			}

			if (this.getCurrentMap().getParamElement(1) == null) {
				addLat.setEnabled(true);
				latTransform.setEnabled(false);
			}
			else {
				addLat.setEnabled(false);
				latTransform.setEnabled(true);
			}

			if (this.getCurrentMap().getParamElement(2) == null) {
				addZ.setEnabled(true);
				zTransform.setEnabled(false);
			}
			else {
				addZ.setEnabled(false);
				zTransform.setEnabled(true);
			}

			if (this.getCurrentMap().getParamElement(3) == null) {
				addT.setEnabled(true);
				timeTransform.setEnabled(false);
			}
			else {
				addT.setEnabled(false);
				timeTransform.setEnabled(true);
			}
		}
		else {
			addLon.setEnabled(false);
			lonTransform.setEnabled(false);
			addLat.setEnabled(false);
			latTransform.setEnabled(false);
			addZ.setEnabled(false);
			zTransform.setEnabled(false);
			addT.setEnabled(false);
			timeTransform.setEnabled(false);
		}
	}

	public static String formatDouble(double inVal, int inNumPlaces, boolean pad) {
		if (inVal >= 1.0e35)
			return "    ----";
		int numPl = inNumPlaces;
		String valStr = new Double(inVal).toString();
		int expPlace = valStr.indexOf('E');
		if (expPlace > 0) {
			// number in scientific notation--get the exponent
			String exp = valStr.substring(expPlace, valStr.length());
			exp = exp.toLowerCase();
			exp = exp.substring(1, exp.length());
			int sign = exp.indexOf("-") >= 0 ? -1 : 1;
			numPl = Math.abs(Integer.valueOf(exp).intValue());
		}

		String frmt = null;
		if (numPl == 1)
			frmt = new String("0.0");
		else if (numPl == 2)
			frmt = new String("0.00");
		else if (numPl == 3)
			frmt = new String("0.000");
		else if (numPl == 4)
			frmt = new String("0.0000");
		else if (numPl == 5)
			frmt = new String("0.00000");
		else if (numPl == 6)
			frmt = new String("0.000000");

		StringBuffer out = new StringBuffer();
		try {
			DecimalFormat decFormatter = new DecimalFormat(frmt);
			decFormatter.format(inVal, out, new FieldPosition(0));
		}
		catch (Exception ex) {
			try {
				frmt = new String("###E##");
				DecimalFormat decFormatter = new DecimalFormat(frmt);
				decFormatter.format(inVal, out, new FieldPosition(0));
			}
			catch (Exception exx) {
				return new Double(inVal).toString();
			}
		}
		if (pad) {
			while (out.length() < 8)
				out.insert(0, ' ');
		}
		String str = new String(out);
		return str;
	}

	public static String formatDouble(String inValStr, int numPlaces, boolean pad) {
		int numPl = numPlaces;
		String valStr;
		// look for the decimal point
		int expPlace = inValStr.indexOf('E');
		String exp = "";
		if (expPlace > 0) {
			// number in scientific notation--get the exponent
			String mantissa = inValStr.substring(0, expPlace);
			exp = inValStr.substring(expPlace, inValStr.length());
			exp = exp.toLowerCase();
			exp = exp.substring(1, exp.length());
			int sign = exp.indexOf("-") >= 0 ? -1 : 1;
			numPl = Math.abs(Integer.valueOf(exp).intValue());
			double manVal = Float.valueOf(mantissa).floatValue();
			manVal *= Math.pow(10.0, (double) (sign * numPl));
			valStr = new Float(manVal).toString();
		}
		else
			valStr = inValStr;

		int decPlace = valStr.lastIndexOf('.');
		int len = valStr.length();

		StringBuffer sb = new StringBuffer(valStr);

		if (numPl > 0) {
			if (len - decPlace > numPl + 1) {
				// truncate string
				sb.setLength(decPlace + 1 + numPl);
			}
			else if (len - decPlace < numPl + 1) {
				// pad with 0's
				while (sb.length() < decPlace + 1 + numPl)
					sb.append('0');
			}
		}
		else
			sb.setLength(decPlace);

		if (pad) {
			while (sb.length() < 8)
				sb.insert(0, ' ');
		}
		return new String(sb);
	}

	public GridPointerCollection getPointerCollection() {
		double[] latArr;
		double[] lonArr;
		double[] depthArr;
		double[] timeArr;
		double[] timeArr2 = null;

		// assign storage for the arrays
		NdEditMapModel map = this.getCurrentMap();
		Vector gAttributes = map.getAttributes();
		NdEditMapParameter latParam = (NdEditMapParameter) map.getParamElement(NdEditMapModel.LATITUDE);
		int numLats = latParam.getLength();

		NdEditMapParameter lonParam = (NdEditMapParameter) map.getParamElement(NdEditMapModel.LONGITUDE);
		int numLons = lonParam.getLength();

		NdEditMapParameter zParam = (NdEditMapParameter) map.getParamElement(NdEditMapModel.Z);
		int numDepths = zParam.getLength();

		NdEditMapParameter timeParam = (NdEditMapParameter) map.getParamElement(NdEditMapModel.TIME);
		int numTimes = timeParam.getLength();
		boolean isClimatology = timeParam.isClimatology();
		int lengthClimatology = timeParam.getClimatologyLength();

		latArr = null;
		lonArr = null;
		depthArr = null;
		timeArr = null;

		if (numLats > 0) {
			latArr = new double[numLats];
			// get a value from latParam
			for (int i = 0; i < numLats; i++) {
				double newVal = (double) latParam.getNewDoubleValue(i);
				latArr[i] = newVal;
			}
		}

		if (numLons > 0) {
			lonArr = new double[numLons];
			// get a value from lonParam
			for (int i = 0; i < numLons; i++) {
				double newVal = (double) lonParam.getNewDoubleValue(i);
				lonArr[i] = newVal;
			}
		}

		double zmin = Double.NaN;
		double zmax = Double.NaN;

		if (numDepths > 0) {
			depthArr = new double[numDepths];
			// get a value from depthParam
			for (int i = 0; i < numDepths; i++) {
				double newVal = (double) zParam.getNewDoubleValue(i);
				depthArr[i] = newVal;
			}
			zmin = depthArr[0];
			zmax = depthArr[numDepths - 1];
		}

		if (numTimes > 0) {
			timeArr = new double[numTimes];
			for (int i = 0; i < numTimes; i++) {
				double newVal = (double) (((GeoDate) (timeParam.getNewValue(i))).getTime());
				timeArr[i] = newVal;
			}
		}

		GridPointerCollection pc = new GridPointerCollection(latArr, lonArr, depthArr, timeArr, true, zmin, zmax,
		    isClimatology, lengthClimatology, map.getOriginalShape(), map.getAddedAxes(), mVars, currentMap.getAttributes(),
		    mTPos, mZPos, mLatPos, mLonPos);
		String title = this.getTitle();
		if (title.indexOf('.') > 0) {
			int extPos = title.indexOf('.');
			title = title.substring(0, extPos);
		}
		pc.setPCTitle(title);
		return pc;
	}

	/**
   * @param mLatPos the mLatPos to set
   */
  public void setLatPos(int mLatPos) {
	  this.mLatPos = mLatPos;
  }

	/**
   * @return the mLatPos
   */
  public int getLatPos() {
	  return mLatPos;
  }

	/**
   * @param mLonPos the mLonPos to set
   */
  public void setLonPos(int mLonPos) {
	  this.mLonPos = mLonPos;
  }

	/**
   * @return the mLonPos
   */
  public int getLonPos() {
	  return mLonPos;
  }

	/**
   * @param mZPos the mZPos to set
   */
  public void setZPos(int mZPos) {
	  this.mZPos = mZPos;
  }

	/**
   * @return the mZPos
   */
  public int getZPos() {
	  return mZPos;
  }

	/**
   * @param mTPos the mTPos to set
   */
  public void setTPos(int mTPos) {
	  this.mTPos = mTPos;
  }

	/**
   * @return the mTPos
   */
  public int getTPos() {
	  return mTPos;
  }
}
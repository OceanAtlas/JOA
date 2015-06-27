/*
 * $Id: ConfigAxisTransform.java,v 1.13 2005/06/17 17:24:17 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit.ncBrowse.map;

import javax.swing.*;
import ndEdit.ncBrowse.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.beans.*;
import javax.swing.border.*;
import gov.noaa.pmel.swing.JDateTimeGetter;
import ucar.nc2.*;
import ndEdit.*;
import gov.noaa.pmel.util.Range2D;
import gov.noaa.pmel.util.GeoDate;
import gov.noaa.pmel.util.GeoDateArray;
import gov.noaa.pmel.util.SoTRange;
import gov.noaa.pmel.util.SoTValue;
import gov.noaa.pmel.util.Point2D;
import javax.swing.table.TableColumn;
import java.util.Date;
import java.text.*;

public class ConfigAxisTransform extends JDialog implements ActionListener, ButtonMaintainer, DocumentListener {
	protected NdEditMapModel mMap;
	protected int mAxis;
    protected JButton mOKBtn = null;
    protected JButton mApplyButton = null;
    protected JButton mExpandButton = null;
    protected JButton mCancelButton = null;
    protected JButton mSaveButton = null;
    protected JButton mConvertLonButton = null;
    protected JCheckBox mIsTimeButton = null;
    protected JCheckBox mIsClimatologyButton = null;
    protected JComboBox mUnitsCombo = null;
    protected JButton ellipsisBtn = new JButton("...");
    //protected JTextField mNewAxisName = null;
    //protected JTextField mNewAxisUnits = null;
    protected JTextField mNewAxisMin = null;
    protected JTextField mDelta = null;
    protected JTextField mNumYears = null;
    protected JLabel mNewAxisRange = null;
    protected MaintenanceTimer mMaintain = null;
	protected JDialog mFrame = null;
	protected boolean mIgnore = false;
	protected ResourceBundle b = ResourceBundle.getBundle("ndEdit.NdEditResources");
	private static NdEditEnumValsTableModel enumValsModel = null;
	private NdEditMapParameter mParam;
	private boolean mIsTime;
	protected JDateTimeGetter dateTimeGetter;
	private SimpleDateFormat sdfGMT = new SimpleDateFormat("dd MMM yyyy");
	protected JButton jdtgOK;
	protected JButton jdtgCancel;
	protected JDialog jdtgDialog;
	protected GeoDate mStartGeoDate;
	protected boolean mIsLongitude = false;
	protected boolean mIsMac = false;
	protected boolean mIsPoint = false;
	JFrame mParent;
	Vector mVals;
	JLabel lengthLbl;
	JLabel deltaLbl;
	JPanel climatologySetup;
	    
    public ConfigAxisTransform(JFrame parent, NdEditMapModel map, int axis, boolean isMac) {
    	super(parent, "Transform Axis", false);
    	mParent = parent;
    	mMap = map;
    	mAxis = axis;
    	mIsMac = isMac;
    	if (mAxis == NdEditMapModel.LONGITUDE)
    		mIsLongitude = true;
		this.init();
		this.setTitle(b.getString("kTransformFor") + " " + getSuggestedName(mAxis) + " " + b.getString("kAxis"));
    }
    
    public ConfigAxisTransform(JFrame parent, Vector vals, NdEditMapModel map, int axis, boolean isMac) {
    	super(parent, "Transform Axis", false);
    	mParent = parent;
    	mVals = vals;
    	mMap = map;
    	mAxis = axis;
    	mIsMac = isMac;
    	if (mAxis == NdEditMapModel.LONGITUDE)
    		mIsLongitude = true;
		this.init2();
		this.setTitle(b.getString("kTransformFor") + " " + getSuggestedName(mAxis) + " " + b.getString("kAxis"));
    }
    
	void createJDateTimeGetter() {
		//if (Debug.DEBUG) 
		//	System.out.println("createJDateTimeGetter with date: " + sdfGMT.format(this.getDate()));
		dateTimeGetter = new JDateTimeGetter(false, true);
		createFormatter(1);
		dateTimeGetter.setOutputDateFormatter(sdfGMT);
		dateTimeGetter.setTimeZone(TimeZone.getTimeZone("GMT"));
		dateTimeGetter.setHideTime(false);
		jdtgOK = dateTimeGetter.getOkButn();
		jdtgOK.addActionListener(this);
		jdtgOK.setActionCommand("dateset");
		jdtgCancel = dateTimeGetter.getCancelButn();
		jdtgCancel.addActionListener(this);
		jdtgCancel.setActionCommand("canceldateset");
	}

   public void createFormatter(int timeDisplayFormat) {
        sdfGMT = new SimpleDateFormat("yyyy-MM-dd");
	}

	public static NdEditEnumValsTableModel getCurrentModel() {
		return enumValsModel;
	}
        
    private String getSuggestedName(int axis) {
    	if (axis == NdEditMapModel.LATITUDE)
    		return "Latitude";
    	else if (axis == NdEditMapModel.LONGITUDE)
    		return "Longitude";
    	else if (axis == NdEditMapModel.Z)
    		return "Z";
    	else if (axis == NdEditMapModel.TIME)
    		return "Time";
    	return null;
    }
        
    private String getSuggestedUnits(int axis) {
		if (axis == NdEditMapModel.TIME)
			return "time";
		else if (axis == NdEditMapModel.Z)
			return "m";
		else
			return "degrees";
    }
    
    public String getStrVal(SoTValue val) {
    	Object obj = val.getObjectValue();
    	if (obj instanceof Double)
    		return ((Double)obj).toString();
    	else if (obj instanceof Float)
    		return ((Float)obj).toString();
    	else if (obj instanceof Integer)
    		return ((Integer)obj).toString();
    	else if (obj instanceof Short)
    		return ((Short)obj).toString();
    	else if (obj instanceof Long)
    		return ((Long)obj).toString();
    	else if (obj instanceof GeoDate)
    		return ((GeoDate)obj).toString();
    	return "unk";
    }
    
    public void init2() {
    	Container contents = this.getContentPane();
    	this.getContentPane().setLayout(new BorderLayout(0, 0));
    	
    	JPanel axisDetailPanel = new JPanel();
    	axisDetailPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));
    	
    	JPanel transformDetailPanel = new JPanel();
    	transformDetailPanel.setLayout(new BorderLayout(0, 0));
    	
    	ucar.nc2.Dimension mDim = (ucar.nc2.Dimension)mMap.getDimElement(mAxis);
    	ucar.nc2.Variable ncVar = mDim.getCoordinateVariable();
    	if (ncVar == null) {
    		NcFile ncf = mMap.getNcFile(mAxis);
			ncVar = ncf.findVariable(((ucar.nc2.Dimension)mDim).getName());
		}

    	String origName = mDim.getName();
    	mParam = mMap.getParamElement(mAxis);
    	if (mParam.getLength() == 1)
    		mIsPoint = true;
    	mIsTime = mParam.isTime();
    	String dimUnits;
        Attribute attr = ncVar.findAttribute("units");
        if (attr != null) {
          dimUnits = attr.getStringValue();
        } 
        else {
          dimUnits = " ";
        }
    	
    	// name line
    	JPanel line1 = new JPanel();
    	line1.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    	line1.add(new JLabel(b.getString("kOriginalName") + "  " + origName + "         "));
    	line1.add(new JLabel(b.getString("kNewName") + getSuggestedName(mAxis)));
    	//mNewAxisName = new JTextField(10);
    	//mNewAxisName.setText(getSuggestedName(mAxis));
		//mNewAxisName.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    	//line1.add(mNewAxisName);
    	//axisDetailPanel.add(line1);
    	
    	// units line
    	JPanel line2 = new JPanel();
    	line2.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    	
    	line2.add(new JLabel(b.getString("kOriginalUnits") + "  " + dimUnits + "         "));
    	line2.add(new JLabel(b.getString("kNewUnits")));
    	//if (mIsTime)
    	//	mNewAxisUnits = new JTextField(20);
    	//else
    	//	mNewAxisUnits = new JTextField(5);
		//mNewAxisUnits.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    	if (mIsTime)
    		line2.add(new JLabel(dimUnits));
    	else
    		line2.add(new JLabel(getSuggestedUnits(mAxis)));
    	//line2.add(mNewAxisUnits);
    	axisDetailPanel.add(line2);
    	
    	//Range line
    	JPanel line3 = new JPanel();
    	line3.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    	line3.add(new JLabel(b.getString("kOriginalRange")));
    	SoTRange range = mParam.getSoTRange();
    	SoTValue sotMin = range.getStart();
    	SoTValue sotMax = range.getEnd();
    	line3.add(new JLabel(b.getString("kMin2")));
    	line3.add(new JLabel(getStrVal(sotMin) + "    "));
    	line3.add(new JLabel(b.getString("kMax2")));
    	line3.add(new JLabel(getStrVal(sotMax) + "    "));
    	line3.add(new JLabel(b.getString("kNumPoints") + mParam.getLength()));
    	axisDetailPanel.add(line3);
    	
    	// table of enumerated values
		enumValsModel = new NdEditEnumValsTableModel(mVals, mIsTime);
		JTable enumValsTable = new JTable();
    	enumValsTable.putClientProperty("Quaqua.Table.style", "striped");
		enumValsTable.setModel(enumValsModel);
		//enumValsTable.setSize(100,100);
		enumValsTable.setPreferredScrollableViewportSize(new java.awt.Dimension(250, 100));
		TableColumn tc;
		tc = enumValsTable.getColumnModel().getColumn(0);
		tc.setPreferredWidth(50);
		tc = enumValsTable.getColumnModel().getColumn(1);
		tc.setPreferredWidth(100);
		tc = enumValsTable.getColumnModel().getColumn(2);
		tc.setPreferredWidth(100);
		JScrollPane scroller = new JScrollPane();
		scroller.getViewport().add(enumValsTable);
    	transformDetailPanel.add("Center", new TenPixelBorder(scroller, 15, 15, 15, 15));
    	    	
    	// transform stuff
		JPanel transContCont = new JPanel();
    	transContCont.setLayout(new BorderLayout(5, 5));
		JPanel transCont = new JPanel();
    	transCont.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 5));
		JPanel trans = new JPanel();
    	trans.setLayout(new BorderLayout(5, 5));
    	
    	// is time button
    	JPanel line2a = new JPanel();
    	line2a.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    	
    	mIsTimeButton = new JCheckBox(b.getString("kIsTime"), mIsTime);
    	mIsTimeButton.setActionCommand("converttotime");
    	mIsTimeButton.addActionListener(this);
    	line2a.add(mIsTimeButton);
    	transCont.add(line2a);
    	
    	climatologySetup = new JPanel();
    	climatologySetup.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));
    	
    	JPanel line2b = new JPanel();
    	line2b.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    	mIsClimatologyButton = new JCheckBox(b.getString("kIsClimatology"), false);
    	mIsClimatologyButton.setActionCommand("isclimatology");
    	mIsClimatologyButton.addActionListener(this);
    	line2b.add(mIsClimatologyButton);
    	climatologySetup.add(line2b);
    	
    	JPanel line2c = new JPanel();
    	line2c.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    	line2c.add(new JLabel(b.getString("kNumYears")));
    	mNumYears = new JTextField(5);
		mNumYears.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    	mNumYears.getDocument().addDocumentListener(this);
    	line2c.add(mNumYears);
    	climatologySetup.add(line2c);
    	transCont.add(climatologySetup);
    	
    	if (!mIsTime) {
    		climatologySetup.setVisible(false);
	    }
    	
    	/*if (mIsPoint) {
    		// allow a point to be expanded to a vector
    		mExpandButton = new JButton("Convert to Vector");
    		mExpandButton.setActionCommand("converttovector");
	    	mExpandButton.addActionListener(this);
	    	line2a.add(mExpandButton);
    	}*/
		
    	// new range line
      	ellipsisBtn.setActionCommand("getdate");
      	ellipsisBtn.setMargin(new Insets(0,0,0,0));
    	JPanel line4 = new JPanel();
    	line4.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    	if (!mIsTime) {
    		line4.add(new JLabel(b.getString("kStartValue")));
    		mNewAxisMin = new JTextField(5);
    	}
    	else {
    		line4.add(new JLabel(b.getString("kStartTime")));
    		mNewAxisMin = new JTextField(15);
    	}
		mNewAxisMin.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    	mNewAxisMin.getDocument().addDocumentListener(this);
    	line4.add(mNewAxisMin);
    	line4.add(ellipsisBtn);
    	if (!mIsTime)
    		ellipsisBtn.setVisible(false);
    	transCont.add(line4);
    	
    	JPanel line5 = new JPanel();
    	line5.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    	deltaLbl = new JLabel(b.getString("kDelta"));
    	line5.add(deltaLbl);
    	mDelta = new JTextField(5);
		mDelta.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));;
    	mDelta.getDocument().addDocumentListener(this);
    	line5.add(mDelta);
    	Vector units = new Vector();
    	units.add(b.getString("kYears"));
    	units.add(b.getString("kMonths"));
    	units.add(b.getString("kDays"));
    	units.add(b.getString("kHours"));
    	units.add(b.getString("kMinutes"));
    	units.add(b.getString("kSeconds"));
    	mUnitsCombo = new JComboBox(units);
    	mUnitsCombo.addActionListener(this);
    	line5.add(mUnitsCombo);
    	if (!mIsTime)
    		mUnitsCombo.setVisible(false);
    	transCont.add(line5);
    	
    	JPanel line6 = new JPanel();
    	line6.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    	mNewAxisRange = new JLabel(b.getString("kNewRange"));
    	line6.add(mNewAxisRange);
    	
    	JPanel aFlow = new JPanel();
    	aFlow.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    	aFlow.add(transCont);
    	trans.add("Center", aFlow);
    	trans.add("South", line6);
    	transContCont.add("Center", trans);
    	
    	JPanel line7 = new JPanel();
    	line7.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 1));
    	mApplyButton = new JButton(b.getString("kApplyTransform"));
		mApplyButton.setActionCommand("apply");
    	line7.add(mApplyButton);
    	transContCont.add("South", new TenPixelBorder(line7, 5, 5, 5, 5));
    	TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kTransform"));
    	transContCont.setBorder(tb);
    	transformDetailPanel.add("South", new TenPixelBorder(transContCont, 5, 5, 5, 5));
    	
    	// optional lon conversion button
    	mConvertLonButton = new JButton(b.getString("kConvertLon"));
		mConvertLonButton.setActionCommand("convertlon");
		if (mIsLongitude)
    		line7.add(mConvertLonButton);
    	
		// lower panel
    	mOKBtn = new JButton(b.getString("kOK"));
		mOKBtn.setActionCommand("ok");
    	this.getRootPane().setDefaultButton(mOKBtn);
    	mCancelButton = new JButton(b.getString("kDone"));
		mCancelButton.setActionCommand("cancel");
		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
        dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
        dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
    	if (mIsMac) {
	    	dlgBtnsPanel.add(mCancelButton);
	    	dlgBtnsPanel.add(mOKBtn);
		}
		else {
	    	dlgBtnsPanel.add(mOKBtn);
	    	dlgBtnsPanel.add(mCancelButton);
		}
        dlgBtnsInset.add(dlgBtnsPanel);
        
        this.getContentPane().add("North", axisDetailPanel);
        this.getContentPane().add("Center", transformDetailPanel);
        this.getContentPane().add("South", dlgBtnsInset);
        
        mOKBtn.addActionListener(this);
        mApplyButton.addActionListener(this);
        mConvertLonButton.addActionListener(this);
        mCancelButton.addActionListener(this);
        ellipsisBtn.addActionListener(this);
    	mFrame = this;
		
		mMaintain = new MaintenanceTimer(this, 100);
		mMaintain.startMaintainer();
		
		// show dialog at center of screen
		Rectangle dBounds = this.getBounds();
		java.awt.Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width/2 - dBounds.width/2;
		int y = sd.height/2 - dBounds.height/2;
		this.setLocation(x, y);
    }
    
    public void init() {
    	Container contents = this.getContentPane();
    	this.getContentPane().setLayout(new BorderLayout(0, 0));
    	
    	JPanel axisDetailPanel = new JPanel();
    	axisDetailPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));
    	
    	JPanel transformDetailPanel = new JPanel();
    	transformDetailPanel.setLayout(new BorderLayout(0, 0));
    	
    	ucar.nc2.Dimension mDim = (ucar.nc2.Dimension)mMap.getDimElement(mAxis);
    	ucar.nc2.Variable ncVar = mDim.getCoordinateVariable();
    	if (ncVar == null) {
    		NcFile ncf = mMap.getNcFile(mAxis);
			ncVar = ncf.findVariable(((ucar.nc2.Dimension)mDim).getName());
		}

    	String origName = mDim.getName();
    	mParam = mMap.getParamElement(mAxis);
    	if (mParam.getLength() == 1)
    		mIsPoint = true;
    	mIsTime = mParam.isTime();
    	String dimUnits;
        Attribute attr = ncVar.findAttribute("units");
        if (attr != null) {
          dimUnits = attr.getStringValue();
        } 
        else {
          dimUnits = " ";
        }
    	
    	// name line
    	JPanel line1 = new JPanel();
    	line1.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    	line1.add(new JLabel(b.getString("kOriginalName") + "  " + origName + "         "));
    	line1.add(new JLabel(b.getString("kNewName") + getSuggestedName(mAxis)));
    	//mNewAxisName = new JTextField(10);
    	//mNewAxisName.setText(getSuggestedName(mAxis));
		//mNewAxisName.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    	//line1.add(mNewAxisName);
    	//axisDetailPanel.add(line1);
    	
    	// units line
    	JPanel line2 = new JPanel();
    	line2.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    	
    	line2.add(new JLabel(b.getString("kOriginalUnits") + "  " + dimUnits + "         "));
    	line2.add(new JLabel(b.getString("kNewUnits")));
    	//if (mIsTime)
    	//	mNewAxisUnits = new JTextField(20);
    	//else
    	//	mNewAxisUnits = new JTextField(5);
		//mNewAxisUnits.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    	if (mIsTime)
    		line2.add(new JLabel(dimUnits));
    	else
    		line2.add(new JLabel(getSuggestedUnits(mAxis)));
    	//line2.add(mNewAxisUnits);
    	axisDetailPanel.add(line2);
    	
    	//Range line
    	JPanel line3 = new JPanel();
    	line3.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    	line3.add(new JLabel(b.getString("kOriginalRange")));
    	SoTRange range = mParam.getSoTRange();
    	SoTValue sotMin = range.getStart();
    	SoTValue sotMax = range.getEnd();
    	line3.add(new JLabel(b.getString("kMin2")));
    	line3.add(new JLabel(getStrVal(sotMin) + "    "));
    	line3.add(new JLabel(b.getString("kMax2")));
    	line3.add(new JLabel(getStrVal(sotMax) + "    "));
    	line3.add(new JLabel(b.getString("kNumPoints") + mParam.getLength()));
    	axisDetailPanel.add(line3);
    	
    	// table of enumerated values
		enumValsModel = new NdEditEnumValsTableModel(mMap.getNcFile(mAxis), ncVar, mIsTime);
		JTable enumValsTable = new JTable();
    	enumValsTable.putClientProperty("Quaqua.Table.style", "striped");
		enumValsTable.setModel(enumValsModel);
		//enumValsTable.setSize(100,100);
		enumValsTable.setPreferredScrollableViewportSize(new java.awt.Dimension(250, 100));
		TableColumn tc;
		tc = enumValsTable.getColumnModel().getColumn(0);
		tc.setPreferredWidth(50);
		tc = enumValsTable.getColumnModel().getColumn(1);
		tc.setPreferredWidth(100);
		tc = enumValsTable.getColumnModel().getColumn(2);
		tc.setPreferredWidth(100);
		JScrollPane scroller = new JScrollPane();
		scroller.getViewport().add(enumValsTable);
    	transformDetailPanel.add("Center", new TenPixelBorder(scroller, 15, 15, 15, 15));
    	
    	// transform stuff
		JPanel trans = new JPanel();
    	trans.setLayout(new BorderLayout(5, 5));
    	
		JPanel transformMasterPanel = new JPanel();
    	transformMasterPanel.setLayout(new RowLayout(Orientation.LEFT, Orientation.CENTER, 5));
    	
    	JPanel transformSetup = new JPanel();
    	transformSetup.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 5));
    	
    	JPanel transformBtns = new JPanel();
    	transformBtns.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));
    	
    	climatologySetup = new JPanel();
    	climatologySetup.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));
    	
    	// is time button
    	JPanel line2a = new JPanel();
    	line2a.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    	
    	mIsTimeButton = new JCheckBox(b.getString("kIsTime"), mIsTime);
    	mIsTimeButton.setActionCommand("converttotime");
    	mIsTimeButton.addActionListener(this);
    	line2a.add(mIsTimeButton);
    	transformSetup.add(line2a);
		
    	// new range line
      	ellipsisBtn.setActionCommand("getdate");
      	ellipsisBtn.setMargin(new Insets(0,0,0,0));
    	JPanel line4 = new JPanel();
    	line4.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    	if (!mIsTime) {
    		line4.add(new JLabel(b.getString("kStartValue")));
    		mNewAxisMin = new JTextField(5);
    	}
    	else {
    		line4.add(new JLabel(b.getString("kStartTime")));
    		mNewAxisMin = new JTextField(15);
    	}
		mNewAxisMin.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    	mNewAxisMin.getDocument().addDocumentListener(this);
    	line4.add(mNewAxisMin);
    	line4.add(ellipsisBtn);
    	if (!mIsTime)
    		ellipsisBtn.setVisible(false);
    	transformSetup.add(line4);
    	
    	JPanel line5 = new JPanel();
    	line5.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    	deltaLbl = new JLabel(b.getString("kDelta"));
    	line5.add(deltaLbl);
    	mDelta = new JTextField(5);
		mDelta.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));;
    	mDelta.getDocument().addDocumentListener(this);
    	line5.add(mDelta);
    	Vector units = new Vector();
    	units.add(b.getString("kYears"));
    	units.add(b.getString("kMonths"));
    	units.add(b.getString("kDays"));
    	units.add(b.getString("kHours"));
    	units.add(b.getString("kMinutes"));
    	units.add(b.getString("kSeconds"));
    	mUnitsCombo = new JComboBox(units);
    	mUnitsCombo.addActionListener(this);
    	line5.add(mUnitsCombo);
    	if (!mIsTime)
    		mUnitsCombo.setVisible(false);
    	transformSetup.add(line5);
    	
    	// transform btns
    	mApplyButton = new JButton(b.getString("kApplyTransform"));
		mApplyButton.setActionCommand("apply");
    	transformBtns.add(mApplyButton);
    	
    	// optional lon conversion button
    	mConvertLonButton = new JButton(b.getString("kConvertLon"));
		mConvertLonButton.setActionCommand("convertlon");
		if (mIsLongitude)
    		transformBtns.add(mConvertLonButton);
    		
    	transformMasterPanel.add(transformSetup);
    	transformMasterPanel.add(transformBtns);
    	
    	JPanel line6 = new JPanel();
    	line6.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    	mNewAxisRange = new JLabel(b.getString("kNewRange"));
    	line6.add(mNewAxisRange);
    	climatologySetup.add(line6);
    	
    	JPanel line2b = new JPanel();
    	line2b.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    	mIsClimatologyButton = new JCheckBox(b.getString("kIsClimatology"), false);
    	mIsClimatologyButton.setActionCommand("isclimatology");
    	mIsClimatologyButton.addActionListener(this);
    	line2b.add(mIsClimatologyButton);
    	climatologySetup.add(line2b);
    	
    	JPanel line2c = new JPanel();
    	line2c.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    	lengthLbl = new JLabel(b.getString("kNumYears"));
    	line2c.add(lengthLbl);
    	mNumYears = new JTextField(5);
		mNumYears.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    	mNumYears.getDocument().addDocumentListener(this);
    	line2c.add(mNumYears);
    	climatologySetup.add(line2c);
    	
    	if (!mIsTime) {
    		climatologySetup.setVisible(false);
	    }
    	
    	trans.add("North", new TenPixelBorder(transformMasterPanel, 5, 5, 5, 5));
    	trans.add("South", new TenPixelBorder(climatologySetup, 5, 5, 5, 5));
    	TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kTransform"));
    	trans.setBorder(tb);
    	transformDetailPanel.add("South", new TenPixelBorder(trans, 5, 5, 5, 5));
    	
		// lower panel
    	mOKBtn = new JButton(b.getString("kOK"));
		mOKBtn.setActionCommand("ok");
    	this.getRootPane().setDefaultButton(mOKBtn);
    	mCancelButton = new JButton(b.getString("kDone"));
		mCancelButton.setActionCommand("cancel");
		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
        dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
        dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
    	if (mIsMac) {
	    	dlgBtnsPanel.add(mCancelButton);
	    	dlgBtnsPanel.add(mOKBtn);
		}
		else {
	    	dlgBtnsPanel.add(mOKBtn);
	    	dlgBtnsPanel.add(mCancelButton);
		}
        dlgBtnsInset.add(dlgBtnsPanel);
        
        this.getContentPane().add("North", axisDetailPanel);
        this.getContentPane().add("Center", transformDetailPanel);
        this.getContentPane().add("South", dlgBtnsInset);
        
        mOKBtn.addActionListener(this);
        mApplyButton.addActionListener(this);
        mConvertLonButton.addActionListener(this);
        mCancelButton.addActionListener(this);
        ellipsisBtn.addActionListener(this);
    	mFrame = this;
		
		mMaintain = new MaintenanceTimer(this, 100);
		mMaintain.startMaintainer();
		
		// show dialog at center of screen
		Rectangle dBounds = this.getBounds();
		java.awt.Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width/2 - dBounds.width/2;
		int y = sd.height/2 - dBounds.height/2;
		this.setLocation(x, y);
    }
    
	public void valueChanged(ListSelectionEvent evt) {}
	
	public void changedUpdate(DocumentEvent evt) {
		updatePreview();
	}
	
	public void insertUpdate(DocumentEvent evt) {
		updatePreview();
	}
	
	public void removeUpdate(DocumentEvent evt) {
		updatePreview();
	}
	
	private void updatePreview() {
		String minTxt = mNewAxisMin.getText();
		String delTxt = mDelta.getText();
    	if (minTxt.length() > 0 && delTxt.length() > 0) {
			double start = Float.NaN;
			double delta = Float.NaN;
			boolean err = false;
			String errStr = b.getString("kValErr");
			
			if (!mIsTime) {
				try {
					start = (double)Double.parseDouble(mNewAxisMin.getText());
				}
				catch (NumberFormatException ex) {
					err = true;
					if (!(minTxt.length() == 1 && (minTxt.startsWith("-") || minTxt.startsWith(".")))) {
						errStr += b.getString("kMinErr");
					}
					else
						errStr = "";
				}
				
				try {
					delta = (double)Double.parseDouble(mDelta.getText());
				}
				catch (NumberFormatException ex) {
					err = true;
					if (!(delTxt.length() == 1 && (delTxt.startsWith("-") || delTxt.startsWith(".")))) {
						errStr += b.getString("kDeltaErr");
					}
					else
						errStr = "";
				}
	    		
	    		if (!err) {
	    			double end =  start + ((double)(mParam.getLength() - 1) * delta);
	    			mNewAxisRange.setText(b.getString("kNewRange") + NdEditView.formatDouble(start, 4, false) + b.getString("kto") + NdEditView.formatDouble(end, 4, false));
	    		}
	    		else {
	    			mNewAxisRange.setText(b.getString("kNewRange") + errStr);
	    		}
	    	}
	    	else {
				// use the start GeoDate, delta, and units to assign the values as GeoDates
				GeoDate startDate = new GeoDate(mStartGeoDate);
			
				try {
					delta = (double)Double.parseDouble(mDelta.getText());
				}
				catch (NumberFormatException ex) {
					err = true;
					errStr += b.getString("kDeltaErr");
				}
				
				int deltaUnits = GeoDate.YEARS;
				if (mUnitsCombo.getSelectedIndex() == 0) {
					deltaUnits = GeoDate.YEARS;
				}
				else if (mUnitsCombo.getSelectedIndex() == 1) {
					deltaUnits = GeoDate.MONTHS;
				}
				else if (mUnitsCombo.getSelectedIndex() == 2) {
					deltaUnits = GeoDate.DAYS;
				}
				else if (mUnitsCombo.getSelectedIndex() == 3) {
					deltaUnits = GeoDate.HOURS;
				}
				else if (mUnitsCombo.getSelectedIndex() == 4) {
					deltaUnits = GeoDate.MINUTES;
				}
				else if (mUnitsCombo.getSelectedIndex() == 5) {
					deltaUnits = GeoDate.SECONDS;
				}
				
				if (!err) {
					// apply the transform
					for (int i=0; i<mParam.getLength(); i++) {
						if (i > 0)
							startDate.increment(delta, deltaUnits);
					}
	    			mNewAxisRange.setText(b.getString("kNewRange") + mStartGeoDate.toString() + b.getString("kto") + startDate.toString());
				}
				else {
					// present error alert
				}
			}
    	}
    	else
    		mNewAxisRange.setText(b.getString("kNewRange"));
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComboBox) {
			updatePreview();
			return;
		}
		String cmd = e.getActionCommand();
		
		if (cmd.equals("cancel")) {
			mMaintain.endMaintainer();
			mMaintain = null;
			this.dispose();
		}
		else if (cmd.equals("ok")) {
			// need to store the new values in mParam
			// store the new axis name
			//mParam.setNewDimName(mNewAxisName.getText());
			
			// store the new units
			//mParam.setUnits(mNewAxisUnits.getText());
			
			// store the modified values
			boolean isNew = true;
			if (mParam.getNumNewVals() > 0)
				isNew = false;
			for (int i=0; i<mParam.getLength(); i++) {
				// new value
				Object obj = enumValsModel.getValueAt(i, 2);
				if (obj == null)
					obj = enumValsModel.getValueAt(i, 1);
					
				mParam.setNewValue(obj, i, isNew);
			}
			
			if (mIsTime) {
				mParam.setIsClimatology(mIsClimatologyButton.isSelected());
				int len = 1;
				String errStr = "";
				if (mIsClimatologyButton.isSelected()) {
					try {
						len = (int)Integer.parseInt(mNumYears.getText());
						mParam.setClimatologyLength(len);
					}
					catch (NumberFormatException ex) {
						// present error alert
						errStr = b.getString("kLengthErr");
						JFrame f = new JFrame(b.getString("kClimatologyError"));
						Toolkit.getDefaultToolkit().beep();
						JOptionPane.showMessageDialog(f, b.getString("kLengthErr"));
						
						// don't allow the OK
						return;
					}
				}
			}
			
			mMaintain.endMaintainer();
			mMaintain = null;
			this.dispose();
		}
		else if (cmd.equals("converttotime")) {
			if (mUnitsCombo.isVisible()) {
				mUnitsCombo.setVisible(false);
				ellipsisBtn.setVisible(false);
				mNewAxisMin.setColumns(5);
				mIsTime = false;
				climatologySetup.setVisible(false);
			}
			else {
				mUnitsCombo.setVisible(true);
				ellipsisBtn.setVisible(true);
				mNewAxisMin.setColumns(15);
				mIsTime = true;
				climatologySetup.setVisible(true);
			}
		}
		else if (cmd.equals("convertlon")) {
			for (int i=0; i<mParam.getLength(); i++) {
				// get the existing value
				Object obj = enumValsModel.getValueAt(i, 1);
				
				if (obj instanceof Float) {
					double lonVal = ((Float)obj).floatValue();
					double newVal = lonVal;
					if (newVal > 180)
						newVal -= 360;
					enumValsModel.setValueAt(new Double(newVal), i, 2);
				}
				else if (obj instanceof Double) {
					double newVal = ((Double)obj).floatValue();
					if (newVal > 180)
						newVal -= 360;
					enumValsModel.setValueAt(new Double(newVal), i, 2);
				}
			}
		}
		else if (cmd.equals("converttovector")) {
			Vector v = mParam.expandtoVector();
			mMaintain.endMaintainer();
			mMaintain = null;
			this.dispose();
			ConfigAxisTransform config = new ConfigAxisTransform(mParent, v, mMap, mAxis, mIsMac);
			config.pack();
			Rectangle dBounds = config.getBounds();
			java.awt.Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
			int x = sd.width/2 - dBounds.width/2;
			int y = sd.height/2 - dBounds.height/2;
			config.setLocation(x, y);
			config.setVisible(true);	
		}
		else if (cmd.equals("apply")) {
			// get the transform parameters
			double start = Float.NaN;
			double delta = Float.NaN;
			boolean err = false;
			String errStr = b.getString("kValErr");
			if (!mIsTime) {
				try {
					start = (double)Double.parseDouble(mNewAxisMin.getText());
				}
				catch (NumberFormatException ex) {
					err = true;
					errStr += b.getString("kMinErr");
				}
			
				try {
					delta = (double)Double.parseDouble(mDelta.getText());
				}
				catch (NumberFormatException ex) {
					err = true;
					errStr += b.getString("kDeltaErr");
				}
				
				if (!err) {
					// apply the transform
					for (int i=0; i<mParam.getLength(); i++) {
						// compute the new value
						double newVal = start + ((double)i * delta);
						enumValsModel.setValueAt(new Double(newVal), i, 2);
					}
				}
				else {
					// present error alert
				}
			}
			else {
				int deltaUnits = GeoDate.YEARS;
				// use the start GeoDate, delta, and units to assign the values as GeoDates
				GeoDate startDate = new GeoDate(mStartGeoDate);
				if (mIsPoint) {
					delta = 1;
				}
				else {
					try {
						delta = (double)Double.parseDouble(mDelta.getText());
					}
					catch (NumberFormatException ex) {
						err = true;
						errStr += b.getString("kDeltaErr");
					}
					
					String newUnits = "days since";
					if (mUnitsCombo.getSelectedIndex() == 0) {
						deltaUnits = GeoDate.YEARS;
						newUnits = "years since ";
					}
					else if (mUnitsCombo.getSelectedIndex() == 1) {
						deltaUnits = GeoDate.MONTHS;
						newUnits = "months since ";
					}
					else if (mUnitsCombo.getSelectedIndex() == 2) {
						deltaUnits = GeoDate.DAYS;
						newUnits = "days since ";
					}
					else if (mUnitsCombo.getSelectedIndex() == 3) {
						deltaUnits = GeoDate.HOURS;
						newUnits = "hours since ";
					}
					else if (mUnitsCombo.getSelectedIndex() == 4) {
						deltaUnits = GeoDate.MINUTES;
						newUnits = "minutes since ";
					}
					else if (mUnitsCombo.getSelectedIndex() == 5) {
						deltaUnits = GeoDate.SECONDS;
						newUnits = "seconds since ";
					}
				}
					
				if (!err) {
					// apply the transform
					for (int i=0; i<mParam.getLength(); i++) {
						if (i > 0)
							startDate.increment(delta, deltaUnits);
						
						// compute the new value
						enumValsModel.setValueAt(new GeoDate(startDate), i, 2);
					}
					
					// set the units 
					//mNewAxisUnits.setText(newUnits + mStartGeoDate.toString());
				}
				else {
					// present error alert
				}
			}
		}
		else if (cmd.equals("getdate")) {
			if (dateTimeGetter == null)  {
				createJDateTimeGetter();
			}
			
			jdtgDialog = new JDialog();
			jdtgDialog.setTitle(b.getString("kSetStartTime"));
			jdtgDialog.getContentPane().add(dateTimeGetter);
        	jdtgDialog.pack();
			Rectangle dBounds = jdtgDialog.getBounds();
			java.awt.Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
			int x = sd.width/2 - dBounds.width/2;
			int y = sd.height/2 - dBounds.height/2;
			jdtgDialog.setLocation(x, y);
			jdtgDialog.setVisible(true);
		}
		else if (cmd.equals("dateset")) {
			if (dateTimeGetter != null)  {
				// get the date from the date time setter
				Date date = dateTimeGetter.getDate();
				mStartGeoDate = new GeoDate(date);
				mNewAxisMin.setText(mStartGeoDate.toString());
				jdtgDialog.setVisible(false);
			}
		}
		else if (cmd.equals("canceldateset")) {
			if (dateTimeGetter != null)  {
				jdtgDialog.setVisible(false);
			}
		}
	}
	
    public void maintainButtons() {
		String minTxt = mNewAxisMin.getText();
		String delTxt = mDelta.getText();
		
		if (mIsPoint) {
			deltaLbl.setEnabled(false);
    		mDelta.setEnabled(false);
    		mUnitsCombo.setEnabled(false);
		}
    	
    	// maintain the OK btn: only after a transform
    	mOKBtn.setEnabled(enumValsModel.isTableChanged());
    	
    	if (mIsTime && mIsClimatologyButton.isSelected()) {
    		lengthLbl.setEnabled(true);
    		mNumYears.setEnabled(true);
    	}
    	else if (mIsTime && !(mIsClimatologyButton.isSelected())) {
    		lengthLbl.setEnabled(false);
    		mNumYears.setEnabled(false);
    	}
    	
    	if (minTxt.length() == 0) {
    		mApplyButton.setEnabled(false);
    		return;
    	}
    	else if (delTxt.length() == 0 && !mIsPoint) {
    		mApplyButton.setEnabled(false);
    		return;
    	}
    	else {
    		if (minTxt.length() == 1 && (minTxt.startsWith("-") || minTxt.startsWith(".")))
    			mApplyButton.setEnabled(false);
    		else if (!mIsPoint && delTxt.length() == 1 && (delTxt.startsWith("-") || delTxt.startsWith(".")))
    			mApplyButton.setEnabled(false);
    		else
    			mApplyButton.setEnabled(true);
    	}
    }
}
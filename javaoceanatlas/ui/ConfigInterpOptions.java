/*
 * $Id: ConfigInterpOptions.java,v 1.8 2005/09/07 18:49:31 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import java.io.*;
import javax.swing.border.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import org.w3c.dom.*;
import com.ibm.xml.parser.*;

@SuppressWarnings("serial")
public class ConfigInterpOptions extends JOAJDialog implements ListSelectionListener, ActionListener, ItemListener,
    ButtonMaintainer {
	ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
	protected FileViewer mFileViewer;
	protected String mTitle;
	protected Component mComp;
	protected int mSelLevel = -1;
	protected JButton mOKBtn = null;
	protected JButton mCancelButton = null;
	protected JButton mMakeDefaultButton = null;
	protected JOAJTextField mRefLevelFld = null;
	protected JOAJTextField mFarStdLevelLimitField = null;
	protected JOAJTextField mFarStationField = null;
	protected JOAJTextField mNumCastsField = null;
	protected JOAJTextField mMaxDistField = null;
	protected JOAJTextField mNumBottlesField = null;
	protected JRadioButton mTopDown = null;
	protected JRadioButton mBottomUp = null;
	protected JCheckBox mUseDistanceLimits = null;
	protected JCheckBox mFarField = null;
	protected JCheckBox mFarBottle = null;
	protected JCheckBox mFillEdges = null;
	protected JCheckBox mClipExtrapolation = null;
	protected JOAJList mRefLevels = null;
	protected double mRefLevel;
	protected JOAJLabel mParamLabel = null;
	protected JScrollPane mListScroller = null;
	protected DialogClient mClient = null;
	protected NewInterpolationSurface mSurface = null;
	protected boolean mFarFieldFlag;
	protected boolean mFarBottleFlag;
	protected String mParam;
	protected JOAJList mStnList = null;
	protected JOAJList mFVList = null;
	protected int mTotalStns = 0;
	protected SmallIconButton checkAll = null;
	protected SmallIconButton checkNone = null;
	protected boolean mTopDownFlag = true;
	protected boolean mFillEdgesFlag = false;
	protected boolean mClipExtrapolatedFlag = true;
	protected JRadioButton mNoneInterpolation;
	protected JRadioButton mLocalInterpolation;
	protected JRadioButton mFarFieldInterpolation;
	protected JRadioButton mZGridInterpolation;
	protected int mInterpolationType = Interpolation.NO_MISSING_INTERPOLATION;
	protected int mFarBottleLimit = 2;
	protected int mFarStdLevelLimit = 2;
	protected int mFarStationLimit = 2;
	protected boolean mUseFarFieldLimit;
	protected double mFarFieldLimit;
	protected JOAJLabel iLabel1;
	protected JOAJLabel iLabel2;
	protected JOAJLabel iLabel3;
	protected JOAJLabel iLabel4;
	protected JOAJLabel iLabel5;
	protected JOAJLabel iLabel5a;
	protected JOAJLabel iLabel6;
	protected JOAJLabel iLabel7;
	protected JOAJLabel iLabel8;
	protected JOAJLabel iLabel9;
	protected JOAJLabel iLabel10;
	protected JRadioButton mComputedMeanCast;
	protected JRadioButton mMeanCastFromFile;
	protected JRadioButton mReferenceToLevel;
	protected JCheckBox mReferenceTo;
	protected Vector<JOAViewer> mAllFileViewers;
	private Timer timer = new Timer();
	private JSpinner mNumX, mNumY;
	private JOAJTextField cay, nrng;
	private JOAJCheckBox mMaskBottom = null;
	private int mXDim;
	private int mYDim;
	double mCay;
	int mNumGridSpaces;
	boolean mMask;
	JOAJLabel zlbl1 = new JOAJLabel(b.getString("kGridDimensions"));
	JOAJLabel zlbl2 = new JOAJLabel(b.getString("kX"));
	JOAJLabel zlbl3 = new JOAJLabel(b.getString("kY"));
	JOAJLabel zlbl4 = new JOAJLabel(b.getString("kInterpTension"));
	JOAJLabel zlbl5 = new JOAJLabel(b.getString("kNumGridSpaces"));

	public ConfigInterpOptions(JFrame par, FileViewer fv, DialogClient client, NewInterpolationSurface surface,
	    String param, double refLevel, boolean topDownFlag, int interpolationType, boolean fillEdges,
	    boolean clipExtrapolated, int farBottleLimit, int farStdLevelLimit, int farStationLimit,
	    boolean useFarFieldLimit, double farFieldLimit, int xdim, int ydim, double cay, int numgridspaces, boolean mask) {
		super(par, "Interpolation Options", false);
		mClient = client;
		mSurface = new NewInterpolationSurface(surface);
		mParam = new String(param);
		mFileViewer = fv;
		mRefLevel = refLevel;
		mInterpolationType = interpolationType;
		mTopDownFlag = topDownFlag;
		mFillEdgesFlag = fillEdges;
		mClipExtrapolatedFlag = clipExtrapolated;
		mFarBottleLimit = farBottleLimit;
		mFarStdLevelLimit = farStdLevelLimit;
		mFarStationLimit = farStationLimit;
		mUseFarFieldLimit = useFarFieldLimit;
		mFarFieldLimit = farFieldLimit;
		mXDim = xdim;
		mYDim = ydim;
		mCay = cay;
		mNumGridSpaces = numgridspaces;
		mMask = mask;
		this.init();
	}

	public void init() {
		// fill the stn list
		buildStnList();

		// create the two parameter chooser lists
		Container contents = this.getContentPane();
		this.getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 5));

		// Options
		JPanel optionsContPanel = new JPanel();
		optionsContPanel.setLayout(new BorderLayout(5, 5));
		TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kOptions"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		optionsContPanel.setBorder(tb);

		JPanel optionsPanel = new JPanel();
		optionsPanel.setLayout(new RowLayout(Orientation.LEFT, Orientation.TOP, 5));

		// interp direction
		JPanel interpCont = new JPanel();
		interpCont.setLayout(new BorderLayout(0, 0));
		JPanel interpContCont = new JPanel();
		interpContCont.setLayout(new BorderLayout(0, 0));

		JPanel interpDirection = new JPanel();
		tb = BorderFactory.createTitledBorder(b.getString("kInterpolationDirection"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		interpCont.setBorder(tb);
		interpDirection.setLayout(new GridLayout(2, 1, 5, 0));
		mTopDown = new JRadioButton(b.getString("kTopDown") + "              ", mTopDownFlag);
		mBottomUp = new JRadioButton(b.getString("kBottomUp"), !mTopDownFlag);
		interpDirection.add(mTopDown);
		interpDirection.add(mBottomUp);
		ButtonGroup bg = new ButtonGroup();
		bg.add(mTopDown);
		bg.add(mBottomUp);
		interpCont.add("North", interpDirection);
		mTopDown.addItemListener(this);
		mBottomUp.addItemListener(this);
		interpContCont.add("North", interpCont);

		// fill edges stuff
		mFillEdges = new JCheckBox(b.getString("kFillEdges"), mFillEdgesFlag);

		mClipExtrapolation = new JCheckBox(b.getString("kClipExtrapolation"), mClipExtrapolatedFlag);

		optionsPanel.add(interpContCont);

		// far-field interpolation
		JPanel missingValueOptions = new JPanel();
		missingValueOptions.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
		tb = BorderFactory.createTitledBorder(b.getString("kMissingValueOptions"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		missingValueOptions.setBorder(tb);

		JPanel line1 = new JPanel();
		line1.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		JPanel line2 = new JPanel();
		line2.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		JPanel line3 = new JPanel();
		line3.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		JPanel line4 = new JPanel();
		line4.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		JPanel line5 = new JPanel();
		line5.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		JPanel line6 = new JPanel();
		line6.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		JPanel line7 = new JPanel();
		line7.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		JPanel line8 = new JPanel();
		line8.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		JPanel line9 = new JPanel();
		line9.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		JPanel line10 = new JPanel();
		line10.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		JPanel line11 = new JPanel();
		line11.setLayout(new GridLayout(1, 3, 0, 0));
		JPanel line12 = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));

		// create the radio buttons
		mNoneInterpolation = new JRadioButton(b.getString("kNoneInterpolation"),
		    mInterpolationType == Interpolation.NO_MISSING_INTERPOLATION);
		mLocalInterpolation = new JRadioButton(b.getString("kLocalInterpolation"),
		    mInterpolationType == Interpolation.LOCAL_INTERPOLATION);
		mFarFieldInterpolation = new JRadioButton(b.getString("kFarFieldInterpolation"),
		    mInterpolationType == Interpolation.FAR_FIELD_INTERPOLATION);
		mZGridInterpolation = new JRadioButton(b.getString("kZGridInterpolation"),
		    mInterpolationType == Interpolation.ZGRID_INTERPOLATION);
		ButtonGroup bg2 = new ButtonGroup();
		bg2.add(mNoneInterpolation);
		bg2.add(mLocalInterpolation);
		bg2.add(mFarFieldInterpolation);
		bg2.add(mZGridInterpolation);
		mNoneInterpolation.addItemListener(this);
		mLocalInterpolation.addItemListener(this);
		mFarFieldInterpolation.addItemListener(this);
		mZGridInterpolation.addItemListener(this);

		// first line--just none option
		line1.add(mNoneInterpolation);

		// line 2-4 are local interp
		line2.add(mLocalInterpolation);
		iLabel1 = new JOAJLabel("     " + b.getString("kLocalInterpHelp"));
		iLabel2 = new JOAJLabel("     " + b.getString("kMaxLocalInterpDistance"));
		iLabel3 = new JOAJLabel("  " + b.getString("kObsAboveBelowStdLevel2"));

		line4.add(iLabel2);
		// line4.add(iLabel3);
		mNumBottlesField = new JOAJTextField(4);
		mNumBottlesField.setText(String.valueOf(mFarBottleLimit));
		mNumBottlesField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line4.add(mNumBottlesField);
		line4.add(iLabel3);

		// lines 6-10 are far field interp
		line6.add(mFarFieldInterpolation);
		iLabel4 = new JOAJLabel("     " + b.getString("kFFInterpHelp"));
		iLabel5 = new JOAJLabel("     " + b.getString("kIfObservedNotFound"));
		iLabel5a = new JOAJLabel(b.getString("kStandardLevels2"));
		iLabel6 = new JOAJLabel("     " + b.getString("kUseInterpolated"));
		iLabel7 = new JOAJLabel(b.getString("kStationsLeftAndRight"));
		iLabel8 = new JOAJLabel(b.getString("kKM"));
		// line7.add(iLabel4);
		line8.add(iLabel5);
		mFarStdLevelLimitField = new JOAJTextField(4);
		mFarStdLevelLimitField.setText(String.valueOf(mFarStdLevelLimit));
		mFarStdLevelLimitField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line8.add(mFarStdLevelLimitField);
		line8.add(iLabel5a);
		line9.add(iLabel6);
		mFarStationField = new JOAJTextField(4);
		mFarStationField.setText(String.valueOf(mFarStationLimit));
		mFarStationField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line9.add(mFarStationField);
		line9.add(iLabel7);
		mUseDistanceLimits = new JCheckBox(b.getString("kMaxDistanceColon"), mUseFarFieldLimit);
		line10.add(new JOAJLabel("     "));
		line10.add(mUseDistanceLimits);
		mMaxDistField = new JOAJTextField(4);
		mMaxDistField.setText(String.valueOf(mFarFieldLimit));
		mMaxDistField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line10.add(mMaxDistField);
		line10.add(iLabel8);
		line10.add(mFillEdges);

		// zgrid settings
		JPanel zGridStuff = new JPanel();
		zGridStuff.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 5));

		JPanel zLine1 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 1, 0));
		JPanel zLine2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 1, 0));
		JPanel zLine3 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 1, 0));

		SpinnerNumberModel modely = new SpinnerNumberModel(mXDim, 0, 1000, 10);
		SpinnerNumberModel modelx = new SpinnerNumberModel(mYDim, 0, 1000, 10);

		mNumX = new JSpinner(modelx);
		mNumY = new JSpinner(modely);
		cay = new JOAJTextField(2);
		nrng = new JOAJTextField(2);
		cay.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		nrng.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		cay.setText(String.valueOf(mCay));
		nrng.setText(String.valueOf(mNumGridSpaces));
		mMaskBottom = new JOAJCheckBox(b.getString("kMaskBottom"), mMask);

		zLine1.add(new JOAJLabel("      "));
		zLine1.add(zlbl1);
		zLine1.add(zlbl2);
		zLine1.add(mNumX);
		zLine1.add(new JOAJLabel("  "));
		zLine1.add(zlbl3);
		zLine1.add(mNumY);

		zLine2.add(new JOAJLabel("      "));
		zLine2.add(zlbl4);
		zLine2.add(cay);
		zLine2.add(new JOAJLabel("  "));
		zLine2.add(zlbl5);
		zLine2.add(nrng);

		zGridStuff.add(zLine1);
		zGridStuff.add(zLine2);
		zGridStuff.add(mMaskBottom);

		if (mInterpolationType == Interpolation.FAR_FIELD_INTERPOLATION) {
			enableFarFieldStuff();
		}
		else {
			disableFarFieldStuff();
		}

		if (mInterpolationType == Interpolation.LOCAL_INTERPOLATION) {
			enableFarBottleStuff();
		}
		else {
			disableFarBottleStuff();
		}

		if (mInterpolationType == Interpolation.ZGRID_INTERPOLATION) {
			enableZGridStuff();
		}
		else {
			disableZGridStuff();
		}

		// if (isCTDData()) {
		// mFarFieldInterpolation.setEnabled(false);
		// }

		missingValueOptions.add(line1);
		missingValueOptions.add(line2);
		// missingValueOptions.add(line3);
		missingValueOptions.add(line4);
		missingValueOptions.add(line5);
		missingValueOptions.add(line6);
		// missingValueOptions.add(line7);
		missingValueOptions.add(line8);
		missingValueOptions.add(line9);
		missingValueOptions.add(line10);

		// line 11
		FeatureGroup fg = JOAConstants.JOA_FEATURESET.get("kOverlayContours");
		if (fg != null & fg.hasFeature("kZGridInterpolation") && fg.isFeatureEnabled("kZGridInterpolation")) {
			line12.add(mZGridInterpolation);
		}
		missingValueOptions.add(line12);
		missingValueOptions.add(zGridStuff);
		missingValueOptions.add(mClipExtrapolation);

		mMakeDefaultButton = new JButton(b.getString("kMakeDefault..."));
		mMakeDefaultButton.setActionCommand("defaultmv");
		mMakeDefaultButton.addActionListener(this);
		optionsPanel.add(missingValueOptions);
		optionsContPanel.add(optionsPanel, "Center");

		JPanel mdBtnsInset = new JPanel();
		JPanel mdBtnsPanel = new JPanel();
		mdBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		mdBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
		mdBtnsPanel.add(mMakeDefaultButton);
		mdBtnsInset.add(mdBtnsPanel);
		optionsContPanel.add(mdBtnsInset, "South");

		mReferenceTo = new JOAJCheckBox(b.getString("kReferencedTo"));
		mReferenceTo.addItemListener(this);

		JPanel referenceOptionPanel = new JPanel(); // CheckBoxBorderPanel(b.getString("kReferencedTo"),
		// BorderFactory.createEtchedBorder(),
		referenceOptionPanel.setLayout(new GridLayout(2, 2, 0, 0));
		tb = BorderFactory.createTitledBorder("");
		referenceOptionPanel.setBorder(tb);

		// reference level
		RadioButtonBorderPanel refLevel = new RadioButtonBorderPanel(b.getString("kInterpolationLevel"));
		ButtonGroup bg3 = new ButtonGroup();
		mReferenceToLevel = refLevel.getRadioButton();
		mReferenceToLevel.addItemListener(this);
		bg3.add(mReferenceToLevel);

		refLevel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		mParamLabel = new JOAJLabel("----");
		refLevel.add(mParamLabel);
		if (mRefLevel != -99) {
			mRefLevelFld = new JOAJTextField(6);
			mRefLevelFld.setText(String.valueOf(mRefLevel));
		}
		else {
			mRefLevelFld = new JOAJTextField(6);
		}
		mRefLevelFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		refLevel.add(mRefLevelFld);

		// fill in mRefLevels
		double[] vals = mSurface.getValues();
		Vector<String> listData = new Vector<String>();
		for (int i = 0; i < mSurface.getNumLevels(); i++) {
			listData.addElement(JOAFormulas.formatDouble(String.valueOf(vals[i]), 3, false));
		}

		mRefLevels = new JOAJList(listData);
		mRefLevels.setVisibleRowCount(5);
		mRefLevels.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mRefLevels.setPrototypeCellValue("6000.345");
		mListScroller = new JScrollPane(mRefLevels);
		mRefLevels.addListSelectionListener(this);
		refLevel.add(mListScroller);

		// assign a param label
		mParamLabel.setText(mSurface.getParam() + "=");
		mParamLabel.invalidate();

		// station selection
		RadioButtonBorderPanel stnSelPanel = new RadioButtonBorderPanel(b.getString("kMeanCast"));
		stnSelPanel.setLayout(new BorderLayout(0, 0));
		JPanel allNoneCont = new JPanel();
		allNoneCont.setLayout(new GridLayout(2, 1, 0, 5));
		checkAll = new SmallIconButton(new ImageIcon(getClass().getResource("images/checkall.gif")));
		allNoneCont.add(checkAll);
		checkNone = new SmallIconButton(new ImageIcon(getClass().getResource("images/checknone.gif")));
		allNoneCont.add(checkNone);
		checkAll.addActionListener(this);
		checkNone.addActionListener(this);
		checkAll.setActionCommand("all");
		checkNone.setActionCommand("none");
		JPanel cont1 = new JPanel();
		cont1.setLayout(new BorderLayout(0, 0));
		cont1.add("North", new TenPixelBorder(allNoneCont, 0, 0, 0, 5));
		stnSelPanel.add("East", cont1);
		mComputedMeanCast = stnSelPanel.getRadioButton();
		bg3.add(mComputedMeanCast);
		mComputedMeanCast.addItemListener(this);
		
		checkAll.setToolTipText("Select all stations in the list for mean cast calculation");
		checkNone.setToolTipText("Remove selected stations from mean cast calculation");

		mStnList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		mStnList.setPrototypeCellValue("Atlantic 11 S 210 (11.25S 180.45W, 4/1/1999");
		mStnList.setVisibleRowCount(4);
		JScrollPane listScroller = new JScrollPane(mStnList);
		stnSelPanel.add(new TenPixelBorder(listScroller, 0, 5, 5, 5), "Center");

		// mean cast from fileviewer
		RadioButtonBorderPanel meanCastSelPanel = new RadioButtonBorderPanel(b.getString("kMeanCast2"));
		meanCastSelPanel.setLayout(new BorderLayout(0, 0));
		mMeanCastFromFile = meanCastSelPanel.getRadioButton();
		bg3.add(mMeanCastFromFile);
		mMeanCastFromFile.addItemListener(this);

		// make a list of the existing fileviewers;
		mAllFileViewers = mFileViewer.getOpenFileViewers();
		Vector<String> fvNames = new Vector<String>();
		for (int i = 0; i < mAllFileViewers.size(); i++) {
			fvNames.addElement(((FileViewer) mAllFileViewers.elementAt(i)).getTitle());
		}
		mFVList = new JOAJList(fvNames);
		mFVList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		mFVList.setPrototypeCellValue("Atlantic 11 S 210");
		mFVList.setVisibleRowCount(4);
		JScrollPane listScroller2 = new JScrollPane(mFVList);
		meanCastSelPanel.add(new TenPixelBorder(listScroller2, 0, 5, 5, 5), "Center");

		stnSelPanel.getRadioButton().setSelected(true);

		// build upper panel
		mainPanel.add(optionsContPanel);
		mainPanel.add(mReferenceTo);
		referenceOptionPanel.add(new TenPixelBorder(stnSelPanel, 5, 5, 5, 5));
		referenceOptionPanel.add(new TenPixelBorder(refLevel, 5, 5, 5, 5));
		referenceOptionPanel.add(new TenPixelBorder(meanCastSelPanel, 5, 5, 5, 5));
		mainPanel.add(referenceOptionPanel);
		contents.add("Center", new TenPixelBorder(mainPanel, 5, 5, 5, 5));

		// lower panel
		mOKBtn = new JButton(b.getString("kOK"));
		mOKBtn.setActionCommand("ok");
		this.getRootPane().setDefaultButton(mOKBtn);
		mCancelButton = new JButton(b.getString("kCancel"));
		mCancelButton.setActionCommand("cancel");
		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
		dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
		if (JOAConstants.ISMAC) {
			dlgBtnsPanel.add(mCancelButton);
			dlgBtnsPanel.add(mOKBtn);
		}
		else {
			dlgBtnsPanel.add(mOKBtn);
			dlgBtnsPanel.add(mCancelButton);
		}
		dlgBtnsInset.add(dlgBtnsPanel);

		mOKBtn.addActionListener(this);
		mCancelButton.addActionListener(this);

		// disable all the refernce to stuff
		disableAllReferenceToStuff();

		contents.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
		this.pack();

		// show dialog at center of screen
		Rectangle dBounds = this.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width / 2 - dBounds.width / 2;
		int y = sd.height / 2 - dBounds.height / 2;
		this.setLocation(x, y);

		runTimer();
	}

	public void runTimer() {
		TimerTask task = new TimerTask() {
			public void run() {
				maintainButtons();
			}
		};
		timer.schedule(task, 0, 1000);
	}

	// private boolean isCTDData() {
	// Vector<String> listData = new Vector<String>();
	// for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
	// OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);
	//
	// for (int sec = 0; sec < of.mNumSections; sec++) {
	// Section sech = (Section)of.mSections.elementAt(sec);
	// for (int stc = 0; stc < sech.mStations.size(); stc++) {
	// Station sh = (Station)sech.mStations.elementAt(stc);
	// if (sh.getType() != null && sh.getType().indexOf("CTD") >= 0) {
	// return true;
	// }
	// }
	// }
	// }
	// return false;
	// }

	private void buildStnList() {
		Vector<String> listData = new Vector<String>();
		for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile) mFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section) of.mSections.elementAt(sec);
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station) sech.mStations.elementAt(stc);
					listData.addElement(sech.mSectionDescription + ":" + sh.mStnNum + "(" + JOAFormulas.formatLat(sh.mLat) + " "
					    + JOAFormulas.formatLon(sh.mLon) + ", " + JOAFormulas.formatDate(sh, false) + ")");
					mTotalStns++;
				}
			}
		}

		if (mStnList == null) {
			mStnList = new JOAJList(listData);
		}
		else {
			mStnList.setListData(listData);
			mStnList.invalidate();
		}
	}

	public void valueChanged(ListSelectionEvent evt) {
		if (evt.getSource() == mRefLevels) {
			// get the refLevel
			mSelLevel = mRefLevels.getSelectedIndex();
			mRefLevelFld.setText((String) mRefLevels.getSelectedValue());
			mRefLevelFld.invalidate();
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() instanceof JRadioButton) {
			JRadioButton rb = (JRadioButton) evt.getSource();
			if (evt.getStateChange() == ItemEvent.SELECTED && rb == mTopDown) {
				mTopDownFlag = true;
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mBottomUp) {
				mTopDownFlag = false;
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mNoneInterpolation) {
				this.disableFarBottleStuff();
				this.disableFarFieldStuff();
				this.disableZGridStuff();
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mLocalInterpolation) {
				this.enableFarBottleStuff();
				this.disableFarFieldStuff();
				this.disableZGridStuff();
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mFarFieldInterpolation) {
				this.disableFarBottleStuff();
				this.enableFarFieldStuff();
				this.disableZGridStuff();
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mZGridInterpolation) {
				this.disableFarBottleStuff();
				this.disableFarFieldStuff();
				this.enableZGridStuff();
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mComputedMeanCast) {
				this.enableComputeMeanCastStuff();
				this.disableMeanCastFromFileStuff();
				this.disableRefLevelStuff();
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mMeanCastFromFile) {
				this.disableComputeMeanCastStuff();
				this.enableMeanCastFromFileStuff();
				;
				this.disableRefLevelStuff();
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mReferenceToLevel) {
				this.disableComputeMeanCastStuff();
				this.disableMeanCastFromFileStuff();
				this.enableRefLevelStuff();
			}
		}
		else if (evt.getSource() instanceof JCheckBox) {
			JCheckBox cb = (JCheckBox) evt.getSource();
			if (evt.getStateChange() == ItemEvent.SELECTED && cb == mReferenceTo) {
				enableAllReferenceToStuff();
			}
			else if (evt.getStateChange() == ItemEvent.DESELECTED && cb == mReferenceTo) {
				disableAllReferenceToStuff();
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("cancel")) {
			mClient.dialogCancelled(this);
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("ok")) {
			mClient.dialogDismissed(this);
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("none")) {
			// unselect all selected stations
			mStnList.clearSelection();
			checkNone.setSelected(false);
		}
		else if (cmd.equals("all")) {
			// select all stations in list
			mStnList.setSelectionInterval(0, mTotalStns - 1);
			checkAll.setSelected(false);
		}
		else if (cmd.equals("defaultmv")) {
			save("JOADefault_interp.xml");
		}
	}

	public void saveInterpSettings(File file) {
		// save preferences as XML
		try {
			// create a documentobject
			Document doc = (Document) Class.forName("com.ibm.xml.parser.TXDocument").newInstance();

			// make joapreferences the root element
			Element root = doc.createElement("joainterp");

			if (mTopDown.isSelected()) {
				root.setAttribute("direction", "topdown");
			}
			else {
				root.setAttribute("direction", "bottomup");
			}

			root.setAttribute("filledges", String.valueOf(mFillEdges.isSelected()));
			root.setAttribute("clipextrapolation", String.valueOf(mClipExtrapolation.isSelected()));

			if (mNoneInterpolation.isSelected()) {
				Element item = doc.createElement("nointerpolation");
				root.appendChild(item);
			}
			else if (mLocalInterpolation.isSelected()) {
				Element item = doc.createElement("vertinterpolation");
				item.setAttribute("maxobs", mNumBottlesField.getText());
				root.appendChild(item);
			}
			else {
				Element item = doc.createElement("horzinterpolation");
				item.setAttribute("maxstdlevls", mFarStdLevelLimitField.getText());
				item.setAttribute("numfarstns", mFarStationField.getText());
				if (mUseDistanceLimits.isSelected()) {
					item.setAttribute("maxdist", mMaxDistField.getText());
				}
				root.appendChild(item);
			}

			doc.appendChild(root);
			((TXDocument) doc).setVersion("1.0");
			FileWriter fr = new FileWriter(file);
			((TXDocument) doc).printWithFormat(fr);
			fr.flush();
			fr.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void save(String suggestedName) {
		// get a filename
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith("_interp.xml")) {
					return true;
				}
				else {
					return false;
				}
			}
		};
		Frame fr = new Frame();
		String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
		FileDialog f = new FileDialog(fr, "Save interpolation settings with name ending in \"_interp.xml\"",
		    FileDialog.SAVE);
		f.setDirectory(directory);
		f.setFilenameFilter(filter);
		f.setFile(suggestedName);
		f.setVisible(true);
		directory = f.getDirectory();
		String fs = f.getFile();
		f.dispose();
		if (directory != null && fs != null) {
			File nf = new File(directory, fs);
			try {
				saveInterpSettings(nf);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public boolean getTopDownFlag() {
		return mTopDown.isSelected();
	}

	public int getInterpolationType() {
		if (mNoneInterpolation.isSelected()) {
			return Interpolation.NO_MISSING_INTERPOLATION;
		}
		else if (mLocalInterpolation.isSelected()) {
			return Interpolation.LOCAL_INTERPOLATION;
		}
		else if (mZGridInterpolation.isSelected()) {
			return Interpolation.ZGRID_INTERPOLATION;
		}
		else {
			return Interpolation.FAR_FIELD_INTERPOLATION;
		}
	}

	public boolean getFarFieldFlag() {
		return mFarField.isSelected();
	}

	public boolean isFillEdges() {
		return mFillEdges.isSelected();
	}

	public boolean isClipExtrapolated() {
		return mClipExtrapolation.isSelected();
	}

	public int getFarBottleLimit() {
		int numBottles = 0;
		String fldText = mNumBottlesField.getText();
		if (fldText.length() == 0) {
			numBottles = 2;
		}
		else {
			try {
				numBottles = Integer.valueOf(fldText).intValue();
			}
			catch (NumberFormatException ex) {
				numBottles = 2;
			}
		}
		return numBottles;
	}

	public double getRefLevel() {
		// get the reference level
		String fldText = mRefLevelFld.getText();
		mRefLevel = JOAConstants.MISSINGVALUE;
		if (mReferenceToLevel.isSelected()) {
			if (fldText.length() == 0) {
				mRefLevel = JOAConstants.MISSINGVALUE;
			}
			else {
				try {
					mRefLevel = Double.valueOf(fldText).doubleValue();
				}
				catch (NumberFormatException ex) {
					mRefLevel = JOAConstants.MISSINGVALUE;
				}
			}
		}
		return mRefLevel;
	}

	public int getFarStdLevelLimit() {
		int numStdLevels = 0;
		String fldText = mFarStdLevelLimitField.getText();
		if (fldText.length() == 0) {
			numStdLevels = 2;
		}
		else {
			try {
				numStdLevels = Integer.valueOf(fldText).intValue();
			}
			catch (NumberFormatException ex) {
				numStdLevels = 2;
			}
		}
		return numStdLevels;
	}

	public boolean getUseFarFieldLimitFlag() {
		return mUseDistanceLimits.isSelected();
	}

	public double getFarFieldLimit() {
		String fldText = mMaxDistField.getText();
		double val;
		if (fldText.length() == 0) {
			val = 200.0;
		}
		else {
			try {
				val = Double.valueOf(fldText).doubleValue();
			}
			catch (NumberFormatException ex) {
				val = 200.0;
			}
		}
		return val;
	}

	public int getFarStationLimit() {
		String fldText = mFarStationField.getText();
		int val;
		if (fldText.length() == 0) {
			val = 2;
		}
		else {
			try {
				val = Integer.valueOf(fldText).intValue();
			}
			catch (NumberFormatException ex) {
				val = 2;
			}
		}
		return val;
	}

	public boolean isResidualInterp() {
		if (mStnList.getSelectedIndices().length > 0 || mFVList.getSelectedIndex() >= 0) {
			return true;
		}
		else {
			return false;
		}
	}

	public boolean isMeanCastFromFile() {
		return mMeanCastFromFile.isSelected();
	}

	public boolean[] getStnList() {
		boolean[] stnKeepList = new boolean[mTotalStns];
		int[] stnList = new int[mStnList.getSelectedIndices().length];
		stnList = mStnList.getSelectedIndices();
		for (int i = 0; i < mTotalStns; i++) {
			stnKeepList[i] = false;
		}

		for (int i = 0; i < stnList.length; i++) {
			stnKeepList[stnList[i]] = true;
		}
		return stnKeepList;
	}

	public FileViewer getMeanCastFV() {
		return (FileViewer) (mAllFileViewers.elementAt(mFVList.getSelectedIndex()));
	}

	protected void enableRefLevelStuff() {
		mRefLevelFld.setEnabled(true);
		mParamLabel.setEnabled(true);
		mRefLevels.setEnabled(true);
	}

	protected void disableRefLevelStuff() {
		mRefLevelFld.setEnabled(false);
		mParamLabel.setEnabled(false);
		mRefLevels.clearSelection();
		mRefLevels.setEnabled(false);
	}

	protected void enableZGridStuff() {
		mNumX.setEnabled(true);
		mNumY.setEnabled(true);
		cay.setEnabled(true);
		nrng.setEnabled(true);
		mMaskBottom.setEnabled(true);
		zlbl1.setEnabled(true);
		zlbl2.setEnabled(true);
		zlbl3.setEnabled(true);
		zlbl4.setEnabled(true);
		zlbl5.setEnabled(true);
	}

	protected void disableZGridStuff() {
		mNumX.setEnabled(false);
		mNumY.setEnabled(false);
		cay.setEnabled(false);
		nrng.setEnabled(false);
		mMaskBottom.setEnabled(false);
		zlbl1.setEnabled(false);
		zlbl2.setEnabled(false);
		zlbl3.setEnabled(false);
		zlbl4.setEnabled(false);
		zlbl5.setEnabled(false);
	}

	protected void enableAllReferenceToStuff() {
		enableComputeMeanCastStuff();
		mComputedMeanCast.setEnabled(true);
		mMeanCastFromFile.setEnabled(true);
		mReferenceToLevel.setEnabled(true);
	}

	protected void disableAllReferenceToStuff() {
		mComputedMeanCast.setSelected(true);
		disableRefLevelStuff();
		disableComputeMeanCastStuff();
		disableMeanCastFromFileStuff();
		mComputedMeanCast.setEnabled(false);
		mMeanCastFromFile.setEnabled(false);
		mReferenceToLevel.setEnabled(false);
	}

	protected void enableFarFieldStuff() {
		iLabel4.setEnabled(true);
		iLabel5.setEnabled(true);
		iLabel5a.setEnabled(true);
		iLabel6.setEnabled(true);
		iLabel7.setEnabled(true);
		iLabel8.setEnabled(true);
		mFarStdLevelLimitField.setEnabled(true);
		mFarStationField.setEnabled(true);
		mUseDistanceLimits.setEnabled(true);
		mMaxDistField.setEnabled(true);
		mFillEdges.setEnabled(true);
	}

	protected void disableFarFieldStuff() {
		iLabel4.setEnabled(false);
		iLabel5.setEnabled(false);
		iLabel5a.setEnabled(false);
		iLabel6.setEnabled(false);
		iLabel7.setEnabled(false);
		iLabel8.setEnabled(false);
		mFarStdLevelLimitField.setEnabled(false);
		mFarStationField.setEnabled(false);
		mUseDistanceLimits.setEnabled(false);
		mMaxDistField.setEnabled(false);
		mFillEdges.setEnabled(false);
	}

	protected void enableFarBottleStuff() {
		iLabel1.setEnabled(true);
		iLabel2.setEnabled(true);
		iLabel3.setEnabled(true);
		mNumBottlesField.setEnabled(true);
	}

	protected void disableFarBottleStuff() {
		iLabel1.setEnabled(false);
		iLabel2.setEnabled(false);
		iLabel3.setEnabled(false);
		mNumBottlesField.setEnabled(false);
	}

	protected void enableComputeMeanCastStuff() {
		mStnList.setEnabled(true);
		checkAll.setEnabled(true);
		checkNone.setEnabled(true);
	}

	protected void disableComputeMeanCastStuff() {
		mStnList.clearSelection();
		mStnList.setEnabled(false);
		checkAll.setEnabled(false);
		checkNone.setEnabled(false);
	}

	protected void enableMeanCastFromFileStuff() {
		mFVList.setEnabled(true);
	}

	protected void disableMeanCastFromFileStuff() {
		mFVList.clearSelection();
		mFVList.setEnabled(false);
	}

	public void maintainButtons() {
		// maintain the buttons of the subpanel UIs
		if (mReferenceTo.isSelected() && mComputedMeanCast.isSelected()) {
			if (mStnList.getSelectedIndex() >= 0) {
				mOKBtn.setEnabled(true);
			}
			else {
				mOKBtn.setEnabled(false);
			}
		}
		else if (mReferenceTo.isSelected() && mMeanCastFromFile.isSelected()) {
			if (mFVList.getSelectedIndex() >= 0) {
				mOKBtn.setEnabled(true);
			}
			else {
				mOKBtn.setEnabled(false);
			}
		}
		else if (mReferenceTo.isSelected() && mReferenceToLevel.isSelected()) {
			if (mRefLevelFld.getText().length() > 0) {
				mOKBtn.setEnabled(true);
			}
			else {
				mOKBtn.setEnabled(false);
			}
		}
		else {
			mOKBtn.setEnabled(true);
		}
	}

		public int getXDim() {
		try {
			return ((Integer)mNumX.getValue()).intValue();
		}
		catch (Exception ex) {
			return 101;
		}
	}

	public int getYDim() {
		try {
			return ((Integer)mNumY.getValue()).intValue();
		}
		catch (Exception ex) {
			return 101;
		}
	}

	public double getCay() {
		try {
			return Double.valueOf(cay.getText());
		}
		catch (Exception ex) {
			return 5.0;
		}
	}

	public int getNumGridSpaces() {
		try {
			return Integer.valueOf(nrng.getText());
		}
		catch (Exception ex) {
			return 10;
		}
	}

	public boolean isMask() {
		return mMaskBottom.isSelected();
	}
}

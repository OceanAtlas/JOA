/*
 * $Id: ConfigSectionDifference.java,v 1.9 2005/09/07 18:49:31 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import javax.swing.border.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.PowerOceanAtlas;

@SuppressWarnings("serial")
public class ConfigSectionDifference extends JOAJDialog implements ActionListener, ItemListener, ListSelectionListener,
    ButtonMaintainer, DialogClient {
	protected FileViewer mFileViewer = null;
	protected JOAJButton mOKBtn = null;
	protected JOAJButton mCancelButton = null;
	protected JOAJRadioButton mLatitude = null;
	protected JOAJRadioButton mLongitude = null;
	protected JOAJRadioButton mRegridAtoB = null;
	protected JOAJRadioButton mRegridBtoA = null;
	protected JOAJRadioButton mRegridToUnion = null;
	protected JOAJComboBox mSurfaces = null;
	protected JOAJTextField mSectionNameField = null;
	protected JOAJTextField mMaxDistField = null;
	private JOAJLabel mNameLbl = null;
	protected SectionChooser mSectionAChooser;
	protected SectionChooser mSectionBChooser;
	protected int mSelAFV = -99;
	protected int mSelBFV = -99;
	private Timer timer = new Timer();
	protected double mRefLevel = -99;
	protected double mFarFieldLimit = 200;
	protected boolean mUseFarFieldLimit = false;
	protected boolean mTopDownFlag = true;
	protected int mInterpolationType;
	protected int mFarStdLevelLimit;
	protected int mFarBottleLimit;
	protected int mFarStationLimit;
	protected boolean mFillEdges = false;
	protected boolean mClipExtrapolation = false;
	NewInterpolationSurface mSurface = null;
	protected boolean mClipExtrapolated = true;
	JFrame mParent;
	int mHorzMode = JOAConstants.REGRID_MODE_LAT;
	ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
	JRadioButton mMinus;  
	JRadioButton mPlus;
	private int mOperator = JOAConstants.MINUS_OP;
	
	public ConfigSectionDifference(JFrame par, FileViewer fv) {
		super(par, "Combine Sections", false);
		mParent = par;
		mFileViewer = fv;

		// init the interface
		init();
	}

	public void init() {
		Container contents = this.getContentPane();
		this.getContentPane().setLayout(new BorderLayout(5, 5));
		JPanel mainPanel = new JPanel(); // everything goes in here
		mainPanel.setLayout(new BorderLayout(5, 5));

		JPanel mUpperContents = new JPanel();
		mUpperContents.setLayout(new BorderLayout(5, 5));

		// upper panel contains the section choosers
		JPanel mUpperRow = new JPanel();
		mUpperRow.setLayout(new RowLayout(javaoceanatlas.utility.Orientation.LEFT, javaoceanatlas.utility.Orientation.CENTER,
		    5));

		mSectionAChooser = new SectionChooser(mFileViewer, b.getString("kSectionA"), this, "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		mSectionBChooser = new SectionChooser(mFileViewer, b.getString("kSectionB"), this, "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		mSectionAChooser.init();
		mSectionBChooser.init();

		mUpperRow.add(mSectionAChooser);
		JPanel opHolder = new JPanel(new GridLayout(2, 1, 0, 0));
		mMinus = new JRadioButton("  -  ", true);  
		mPlus = new JRadioButton("  +  ", false);
		ButtonGroup opBG = new ButtonGroup();
		opBG.add(mMinus);
		opBG.add(mPlus);
		
		mMinus.setFont(new java.awt.Font("Dialog", 0, 24));
		mPlus.setFont(new java.awt.Font("Dialog", 0, 24));
		opHolder.add(mMinus);
		opHolder.add(mPlus);
		
		mUpperRow.add(opHolder);

		mUpperRow.add(mSectionBChooser);
		
		JPanel upperCont = new JPanel(new ColumnLayout(javaoceanatlas.utility.Orientation.CENTER,
		    javaoceanatlas.utility.Orientation.CENTER, 5));
		upperCont.add(mUpperRow);

		JPanel namePanel = new JPanel();
		namePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		
		JLabel equal = new JLabel(" = ");    
		equal.setFont(new java.awt.Font("Dialog", 0, 24));
		namePanel.add(equal);
		mNameLbl = new JOAJLabel(b.getString("kNewSection"));
		namePanel.add(mNameLbl);
		mSectionNameField = new JOAJTextField(40);
		mSectionNameField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		namePanel.add(mSectionNameField);
		upperCont.add(namePanel);
		
		mUpperContents.add("North", upperCont);

		JPanel actionsPanel = new JPanel();
		actionsPanel.setLayout(new ColumnLayout(javaoceanatlas.utility.Orientation.CENTER,
		    javaoceanatlas.utility.Orientation.CENTER, 5));

		// container for the surface stuff
		JPanel surfContPanel = new JPanel();
		surfContPanel.setLayout(new ColumnLayout(javaoceanatlas.utility.Orientation.CENTER,
		    javaoceanatlas.utility.Orientation.CENTER, 5));
		TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kVerticalRegridding"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		surfContPanel.setBorder(tb);

		// choosing the interpolation surface
		JPanel surfPanel = new JPanel();
		surfPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		JOAJLabel surfLbl = new JOAJLabel(b.getString("kInterpolationSurface"));
		surfPanel.add(surfLbl);
		String[] params = { "pres", "sig", "ctdp" };
		Vector<String> presetData = JOAFormulas.getFilteredSurfaceList(params);
		mSurfaces = new JOAJComboBox(presetData);
		surfPanel.add(mSurfaces);
		surfContPanel.add(surfPanel);

		// add the adv features button
		JOAJButton mAdvInterpOpt = new JOAJButton(b.getString("kInterpOptions"));
		mAdvInterpOpt.setActionCommand("interpolations");
		mAdvInterpOpt.addActionListener(this);
		surfContPanel.add(mAdvInterpOpt);
		actionsPanel.add(surfContPanel);

		// container for the horizontal regridding
		JPanel horzContPanel = new JPanel();
		horzContPanel.setLayout(new GridLayout(1, 2, 5, 5));
		tb = BorderFactory.createTitledBorder(b.getString("kHorizontalRegridding"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		horzContPanel.setBorder(tb);

		// horizontal grid choices
		JPanel horzGridPanel = new JPanel();
		horzGridPanel.setLayout(new ColumnLayout(javaoceanatlas.utility.Orientation.LEFT,
		    javaoceanatlas.utility.Orientation.TOP, 5));
		tb = BorderFactory.createTitledBorder(b.getString("kRegridUsing"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		horzGridPanel.setBorder(tb);
		horzContPanel.add(horzGridPanel);

		mLatitude = new JOAJRadioButton(b.getString("kLatitude"), true);
		mLongitude = new JOAJRadioButton(b.getString("kLongitude"), false);
		ButtonGroup bg1 = new ButtonGroup();
		bg1.add(mLatitude);
		bg1.add(mLongitude);
		horzGridPanel.add(mLatitude);
		horzGridPanel.add(mLongitude);
		;
		mLatitude.addItemListener(this);
		mLongitude.addItemListener(this);

		JPanel tolPanel = new JPanel();
		tolPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		JOAJLabel tolLbl = new JOAJLabel(b.getString("kMaxDistanceColon2"));
		tolPanel.add(tolLbl);
		mMaxDistField = new JOAJTextField(4);
		mMaxDistField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		tolPanel.add(mMaxDistField);
		JOAJLabel degLbl = new JOAJLabel(b.getString("kDegrees2"));
		tolPanel.add(degLbl);
		horzGridPanel.add(tolPanel);

		JPanel regridActionsPanel = new JPanel();
		regridActionsPanel.setLayout(new ColumnLayout(javaoceanatlas.utility.Orientation.LEFT,
		    javaoceanatlas.utility.Orientation.TOP, 5));
		tb = BorderFactory.createTitledBorder("From");
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		regridActionsPanel.setBorder(tb);
		horzContPanel.add(regridActionsPanel);

		// regrid options
		mRegridAtoB = new JOAJRadioButton(b.getString("kRegridAtoB"), true);
		mRegridBtoA = new JOAJRadioButton(b.getString("kRegridBtoA"), false);
		mRegridToUnion = new JOAJRadioButton(b.getString("kRegridToUnion"), false);
		ButtonGroup bg2 = new ButtonGroup();
		bg2.add(mRegridAtoB);
		bg2.add(mRegridBtoA);
		bg2.add(mRegridToUnion);
		regridActionsPanel.add(mRegridAtoB);
		regridActionsPanel.add(mRegridBtoA);
		// regridActionsPanel.add(mRegridToUnion);
		horzContPanel.add(regridActionsPanel);
		actionsPanel.add(horzContPanel);
		mUpperContents.add("Center", actionsPanel);

		mainPanel.add("Center", new TenPixelBorder(mUpperContents, 0, 5, 0, 5));

		// lower panel
		mOKBtn = new JOAJButton(b.getString("kOK"));
		mOKBtn.setActionCommand("ok");
		this.getRootPane().setDefaultButton(mOKBtn);
		mCancelButton = new JOAJButton(b.getString("kCancel"));
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

		// add all the sub panels to main panel
		mainPanel.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
		contents.add("Center", mainPanel);

		runTimer();

		// show dialog at center of screen
		this.pack();
		Rectangle dBounds = this.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width / 2 - dBounds.width / 2;
		int y = sd.height / 2 - dBounds.height / 2;
		this.setLocation(x, y);
	}

	public void runTimer() {
		TimerTask task = new TimerTask() {
			public void run() {
				maintainButtons();
			}
		};
		timer.schedule(task, 0, 1000);
	}

	public void maintainButtons() {
		// maintain the buttons of the subpanel UIs
		if (mSelAFV >= 0 && mSelBFV >= 0) {
			mOKBtn.setEnabled(true);
		}
		else {
			mOKBtn.setEnabled(false);
		}
	}

	public void valueChanged(ListSelectionEvent evt) {
		if (evt.getSource() == mSectionAChooser.getJList()) {
			// get the param
			mSelAFV = mSectionAChooser.getJList().getSelectedIndex();
			setDiffName();
		}
		else if (evt.getSource() == mSectionBChooser.getJList()) {
			// get the param
			mSelBFV = mSectionBChooser.getJList().getSelectedIndex();
			setDiffName();
		}
	}

	protected void setDiffName() {
		String aname;
		String bname;
		if (mSelAFV >= 0) {
			aname = mSectionAChooser.getSelectedFileViewer().getTitle();
			mRegridBtoA.setText(aname);
		}
		else {
			aname = "";
			mRegridBtoA.setText(b.getString("kRegridAtoB"));
		}

		if (mSelBFV >= 0) {
			bname = mSectionBChooser.getSelectedFileViewer().getTitle();
			mRegridAtoB.setText(bname);
		}
		else {
			bname = "";
			mRegridBtoA.setText(b.getString("kRegridBtoA"));
		}
		String opText = " minus ";
		if (mPlus.isSelected()) {
			opText = " plus ";
		}
		mSectionNameField.setText(aname + opText + bname);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("cancel")) {
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("ok")) {
			int err = combine();
			if (err == 0) {
				timer.cancel();
				this.dispose();
			}
		}
		else if (cmd.equals("interpolations")) {
			// create a contour plot
			ConfigSimpleInterpOptions contOpts = new ConfigSimpleInterpOptions(mParent, mFileViewer, this, mTopDownFlag,
					Interpolation.FAR_FIELD_INTERPOLATION, mFillEdges, mClipExtrapolated, mFarBottleLimit, mFarStdLevelLimit,
			    mFarStationLimit, mUseFarFieldLimit, mFarFieldLimit);
			contOpts.pack();
			contOpts.setVisible(true);
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() instanceof JOAJComboBox) {
			JOAJComboBox cb = (JOAJComboBox) evt.getSource();
			try {
				mSurface = JOAFormulas.getSurface((String) cb.getSelectedItem());
			}
			catch (Exception ex) {
				JFrame f = new JFrame("Surface Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "An error occurred attempting to open the specified surface." + "\n"
				    + "Select another surface for this calculation.");
				return;
			}
		}
		else if (evt.getSource() instanceof JOAJRadioButton) {
			JOAJRadioButton rb = (JOAJRadioButton) evt.getSource();
			if (evt.getStateChange() == ItemEvent.SELECTED && rb == mLongitude) {
				mHorzMode = JOAConstants.REGRID_MODE_LON;
			}
			if (evt.getStateChange() == ItemEvent.SELECTED && rb == mLatitude) {
				mHorzMode = JOAConstants.REGRID_MODE_LAT;
			}

		}
	}

	protected void enableName(boolean flag) {
		mSectionNameField.setEnabled(flag);
		mSectionNameField.invalidate();
		mNameLbl.setEnabled(flag);
		mNameLbl.invalidate();
	}

	protected int combine() {
		double maxDist = 90.0;

		try {
			maxDist = Double.valueOf(mMaxDistField.getText());
		}
		catch (Exception ex) {
		}

		// Step #1 get the individual FileViewers
		FileViewer aFV = mSectionAChooser.getSelectedFileViewer();
		FileViewer bFV = mSectionBChooser.getSelectedFileViewer();
		int numAParams = aFV.getNumProperties();
		
		// step #2: test whether regridding can proceed--sections have to overlap
		boolean latsOverlap = false;
		boolean lonsOverlap = false;
		if (mLatitude.isSelected()) {
			double aMinLat = aFV.getMinLat();
			double aMaxLat = aFV.getMaxLat();
			double bMinLat = bFV.getMinLat();
			double bMaxLat = bFV.getMaxLat();
			
			if (aMinLat == bMinLat && bMinLat == aMinLat) {
				latsOverlap = true;
			}
			else if (aMinLat < bMaxLat && aMinLat > bMinLat && aMaxLat < bMaxLat && aMaxLat > bMinLat) {
				latsOverlap = true;
			}
			else if (bMinLat < aMaxLat && bMinLat > aMinLat && bMaxLat < aMaxLat && bMaxLat > aMinLat) {
				latsOverlap = true;
			}
			else if (aMaxLat > bMinLat && aMaxLat < bMaxLat) {
				latsOverlap = true;
			}
			else if (aMinLat < bMaxLat && aMinLat > bMinLat) {
				latsOverlap = true;
			}
			else if (bMaxLat > aMinLat && bMaxLat < aMaxLat) {
				latsOverlap = true;
			}
			else if (bMinLat < aMaxLat && bMinLat > aMinLat) {
				latsOverlap = true;
			}

			if (!latsOverlap) {
				JFrame f = new JFrame("Regrid Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "Sections do not overlap in latitude." + "\n"
				    + "Can not regrid to destination section.");
				return -1;
			}
		}
		else {
			double aMinLon = aFV.getMinLon();
			double aMaxLon = aFV.getMaxLon();
			double bMinLon = bFV.getMinLon();
			double bMaxLon = bFV.getMaxLon();

			if (aMinLon > 0 && aMaxLon < 0) {
				aMaxLon += 360;
			}
			if (bMinLon > 0 && bMaxLon < 0) {
				bMaxLon += 360;
			}
			
			if (aMinLon == bMinLon && bMaxLon == aMaxLon) {
				lonsOverlap = true;
			}
			else if (aMinLon < bMaxLon && aMinLon > bMinLon && aMaxLon < bMaxLon && aMaxLon > bMinLon) {
				// a totally underbounds n
				lonsOverlap = true;
			}
			else if (bMinLon < aMaxLon && bMinLon > aMinLon && bMaxLon < aMaxLon && bMaxLon > aMinLon) {
				// b totally underbounds a
				lonsOverlap = true;
			}
			else if (aMaxLon > bMinLon && aMaxLon < bMaxLon) {
				// a intersects b left boundary
				lonsOverlap = true;
			}
			else if (aMinLon < bMaxLon && aMinLon > bMinLon) {
				// a intersects b right boundary
				lonsOverlap = true;
			}
			else if (bMaxLon > aMinLon && bMaxLon < aMaxLon) {
				lonsOverlap = true;
			}
			else if (bMinLon < aMaxLon && bMinLon > aMinLon) {
				lonsOverlap = true;
			}

			if (!lonsOverlap) {
				JFrame f = new JFrame("Regrid Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "Sections do not overlap in longitude." + "\n"
				    + "Can not regrid to destination section.");
				return -1;
			}
		}

		// Step #3: get the interpolation surface
		NewInterpolationSurface surf = null;
		String surfFileName = (String) mSurfaces.getSelectedItem();
		try {
			surf = JOAFormulas.getSurface(surfFileName);
		}
		catch (Exception ex) {
			JFrame f = new JFrame("Surface Error");
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(f, "An error occurred attempting to open the specified surface." + "\n"
			    + "Select another surface for this calculation.");
			return -1;
		}
		
		// Step #4: Come up with the common variable list
		int numCommonParams = 0;
		Vector<JOAVariable> nameInA = new Vector<JOAVariable>();
		Vector<JOAVariable> nameInB = new Vector<JOAVariable>();
		Vector<String> units = new Vector<String>();
		for (int ap = 0; ap < numAParams; ap++) {
			String aParam = aFV.mAllProperties[ap].getVarLabel();
			String u = aFV.mAllProperties[ap].getUnits();
			// look for aParam in B
			int bPos = bFV.getPropertyPos(aParam, true);
			if (bPos >= 0) {
				// found match
				numCommonParams++;
				nameInA.addElement(new JOAVariable(aFV, aParam, u, ""));
				nameInB.addElement(new JOAVariable(bFV, bFV.mAllProperties[bPos].getVarLabel(), bFV.mAllProperties[bPos]
				    .getUnits(), ""));
				units.addElement(u);
			}
			else {
				// did not find a simple match--consider some substitutions:
				// substitute O2 and CTDO
				// substitute SALT and CTDS
				// all subs happen in JOA Name Space
				String subNameInB = "";
				if (JOAFormulas.paramNameToJOAName(aParam).equalsIgnoreCase("O2")) {
					bPos = bFV.getPropertyPos("CTDO", false);
					subNameInB = "CTDO";
				}
				else if (JOAFormulas.paramNameToJOAName(aParam).equalsIgnoreCase("CTDO")) {
					bPos = bFV.getPropertyPos("O2", false);
					subNameInB = "O2";
				}
				else if (JOAFormulas.paramNameToJOAName(aParam).equalsIgnoreCase("SALT")) {
					bPos = bFV.getPropertyPos("CTDS", false);
					subNameInB = "CTDs";
				}
				else if (JOAFormulas.paramNameToJOAName(aParam).equalsIgnoreCase("CTDS")) {
					bPos = bFV.getPropertyPos("SALT", false);
					subNameInB = "SALT";
				}

				if (bPos >= 0) {
					// found match
					numCommonParams++;
					nameInA.addElement(new JOAVariable(aFV, aParam, u, ""));
					nameInB.addElement(new JOAVariable(bFV, bFV.mAllProperties[bPos].getVarLabel(), bFV.mAllProperties[bPos]
					    .getUnits(), ""));
					units.addElement(u);
				}
			}
		}
		Vector<Object> diffSections = new Vector<Object>();

		// Step #5 do the interpolations
		for (int i = 1; i < numCommonParams; i++) {
			int aVr = aFV.getPropertyPos(surf.getParam(), true);
			int bVr = bFV.getPropertyPos(surf.getParam(), true);

			if (aVr == -1) {
				// could not find the surface variable in aFV
				JFrame f = new JFrame("Interpolation Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "Surface parameter does not exist in " + aFV.getTitle() + "\n");
				return -1;
			}
			else if (bVr == -1) {
				// could not find the surface variable in bFV
				JFrame f = new JFrame("Interpolation Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "Surface parameter does not exist in " + bFV.getTitle() + "\n");
				return -1;
			}

			// each interpolation is for both sections but for only one common parameter
			LinearInterpolation aResults = new LinearInterpolation(
			    aFV,				 						// <--FileViewer,
			    surfFileName,						// <--file name of surface,
			    surf,				 						// <--surface,
			    (JOAVariable) nameInA.elementAt(i), // <--surfaceParamNum in FV,
			    aVr, 										// <--JOAVariable of interp variable
			    "interpolation", 				// <--name of interpolation?,
			    ((JOAVariable) nameInA.elementAt(i)).getVarName(), // <--name of interp param
			    mTopDownFlag, mInterpolationType, mFillEdges, mClipExtrapolation, mFarBottleLimit, mFarStdLevelLimit,
			    mFarStationLimit, mUseFarFieldLimit, mFarFieldLimit);

			LinearInterpolation bResults = new LinearInterpolation(
			    bFV,
			    surfFileName,
			    surf,
			    (JOAVariable) nameInB.elementAt(i),
			    bVr, 
			    "interpolation",
			    ((JOAVariable) nameInB.elementAt(i)).getVarName(), 
			    mTopDownFlag, mInterpolationType, mFillEdges, mClipExtrapolation, mFarBottleLimit, mFarStdLevelLimit,
			    mFarStationLimit, mUseFarFieldLimit, mFarFieldLimit);

			// do the regridding and differencing for this parameter
			if (mMinus.isSelected()) {
				mOperator = JOAConstants.MINUS_OP;
			}
			else {
				mOperator = JOAConstants.PLUS_OP;
			}
			
			// reverse difference if A - B on A
			boolean reverseDiff = false;
			String AFVName = mRegridAtoB.getText();
			String BFVName = mRegridBtoA.getText();
			
			if (AFVName.equals(aFV.mFileViewerName) && mRegridAtoB.isSelected()) {
				reverseDiff = true;
			}
			
			if (BFVName.equals(aFV.mFileViewerName) && mRegridBtoA.isSelected()) {
				reverseDiff = true;
			}
			
			if (mRegridAtoB.isSelected()) {
				diffSections.add(aResults.regridAndCombine(mOperator, bResults, mHorzMode, true, maxDist, reverseDiff));
			}
			else if (mRegridBtoA.isSelected()) {
				diffSections.add(bResults.regridAndCombine(mOperator, aResults, mHorzMode, true, maxDist, reverseDiff));
			}
			else {
				;
			}
		}

		// at this point I have a vector that contains a differenced section for each
		// parameter
		// the differenced section is a vector array that contains two vectors. One
		// vector (0th element)
		// contains a vector of the profiles of the difference, and the second
		// vector (1st element)
		// contains a vector of the station locations
		// To construct a section, need to reassemble the individual parameters into
		// individual bottles

		String title;
//		if (mRegridAtoB.isSelected()) {
//			title = aFV.getTitle() + " minus " + bFV.getTitle();
//		}
//		else if (mRegridBtoA.isSelected()) {
//			title = aFV.getTitle() + " minus " + bFV.getTitle();
//		}
//		else {
			title = aFV.getTitle() + " minus " + bFV.getTitle();
//		}

		// Make a new section
		Section sec = new Section(0, title, "na", diffSections.size(), numCommonParams);

		// add the common parameters
		for (int i = 0; i < numCommonParams; i++) {
			sec.addNewVarToSection(((JOAVariable) nameInA.elementAt(i)).getVarName(), (String) units.elementAt(i));
		}

		// first create a vector to hold new stations with all parameters
		Vector<Station> mFoundStns = new Vector<Station>(200);

		// get an array of locations from the first parameter
		Vector[] v = (Vector[]) diffSections.elementAt(0);

		// make the stations
		Vector<UVCoordinate> locs = v[1];

		int numStns = locs.size();
		int ord = 0;
		for (int i = 0; i < numStns; i++) {
			UVCoordinate xy = (UVCoordinate) locs.elementAt(i);
			Station newStn = new Station(ord, "na", Integer.toString(ord), 1, xy.getV(), xy.getU(), surf.getNumLevels(),
			    JOAConstants.MISSINGVALUE, JOAConstants.MISSINGVALUE, JOAConstants.MISSINGVALUE, JOAConstants.MISSINGVALUE,
			    JOAConstants.MISSINGVALUE, (int) xy.getW(), JOAConstants.MISSINGVALUE, "", "");
			newStn.setType("INTERP");
			mFoundStns.addElement(new SectionStation(newStn, sec, 0));
		}

		double[] surfValues = surf.getValues();

		// make the Bottles
		Bottle[][] allBottles = new Bottle[numStns][surf.getNumLevels()];

		int botOrd = 0;
		for (int s = 0; s < numStns; s++) {
			for (int z = 0; z < surf.getNumLevels(); z++) {
				allBottles[s][z] = new Bottle(botOrd++, numCommonParams, null, null);

				allBottles[s][z].mDValues[0] = (float) surfValues[z];
			}
		}

		for (int p = 1; p < numCommonParams; p++) {
			Vector[] stnsForParam = (Vector[]) diffSections.elementAt(p - 1);
			Vector profilesForParam = (Vector) stnsForParam[0];

			for (int s = 0; s < numStns; s++) {
				double[] profileData = (double[]) profilesForParam.elementAt(s);
				for (int z = 0; z < surf.getNumLevels(); z++) {
					allBottles[s][z].mDValues[p] = (float) profileData[z];
				}
			}
		}

		for (int s = 0; s < numStns; s++) {
			Station newStn = (Station) mFoundStns.elementAt(s);

			for (int z = 0; z < surf.getNumLevels(); z++) {
				newStn.mBottles.addElement(allBottles[s][z]);
			}
		}

		try {
			JOAConstants.LogFileStream.writeBytes("Computed difference between " + aFV.getTitle() + " and " + bFV.getTitle()
			    + " vertical grid =  " + surfFileName + " horizontal grid = ");
			if (mRegridAtoB.isSelected()) {
				JOAConstants.LogFileStream.writeBytes(bFV.getTitle());
			}
			else if (mRegridBtoA.isSelected()) {
				JOAConstants.LogFileStream.writeBytes(aFV.getTitle());
			}
			else {
				JOAConstants.LogFileStream.writeBytes(aFV.getTitle() + " union " + bFV.getTitle());
			}

			JOAConstants.LogFileStream.writeBytes("\n");
			JOAConstants.LogFileStream.flush();
		}
		catch (Exception ex) {
		}

		if (mFoundStns.size() <= 0) {
			JFrame f = new JFrame("Section Combination Error");
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(f, "Difference calculation failed." + "\n"
			    + "Grids may not overlap sufficiently to perform interpolation.");
			return -1;
		}

		// turn this all into a new fileviewer
		Frame fr = new Frame();
		FileViewer ff = new FileViewer(fr, mFoundStns, mSectionNameField.getText(), mFileViewer, true);
		PowerOceanAtlas.getInstance().addOpenFileViewer(ff);
		ff.pack();
		ff.setVisible(true);
		ff.requestFocus();
		ff.setSavedState(JOAConstants.CREATEDONTHEFLY, null);
		return 0;
	}

	// ok
	public void dialogDismissed(JDialog d) {
		// get the interpolation options
		mTopDownFlag = ((ConfigSimpleInterpOptions) d).getTopDownFlag();
		mInterpolationType = ((ConfigSimpleInterpOptions) d).getInterpolationType();
		mFillEdges = ((ConfigSimpleInterpOptions) d).isFillEdges();
		mClipExtrapolated = ((ConfigSimpleInterpOptions) d).isClipExtrapolated();
		mFillEdges = ((ConfigSimpleInterpOptions) d).isFillEdges();
		mFarBottleLimit = ((ConfigSimpleInterpOptions) d).getFarBottleLimit();
		mFarStdLevelLimit = ((ConfigSimpleInterpOptions) d).getFarStdLevelLimit();
		mFarStationLimit = ((ConfigSimpleInterpOptions) d).getFarStationLimit();
		mFarFieldLimit = ((ConfigSimpleInterpOptions) d).getFarFieldLimit();
		mUseFarFieldLimit = ((ConfigSimpleInterpOptions) d).getUseFarFieldLimitFlag();
	}

	// Cancel button
	public void dialogCancelled(JDialog d) {
	}

	// something other than the OK button
	public void dialogDismissedTwo(JDialog d) {
		;
	}

	// Apply button, OK w/o dismissing the dialog
	public void dialogApply(JDialog d) {
	}

	public void dialogApplyTwo(Object d) {
	}
}

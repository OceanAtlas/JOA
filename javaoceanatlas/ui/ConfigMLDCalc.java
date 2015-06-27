/*
 * $Id: ConfigMLDCalc.java,v 1.5 2005/09/07 18:49:31 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import javaoceanatlas.calculations.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;

@SuppressWarnings("serial")
public class ConfigMLDCalc extends JOAJDialog implements ListSelectionListener, ActionListener, ButtonMaintainer,
    ItemListener, DocumentListener {
	protected FileViewer mFileViewer;
	// MLD Controls
	protected ParameterChooser mMLParamList;
	protected int mSelMLParam = -1;
	protected JOAJTextField mDepthOfDiffMLField = null;
	protected JOAJTextField mDepthOfMLField = null;
	protected JOAJTextField mStartDepthOfMLField = null;
	protected JOAJTextField mStartDepthOfSlopeField = null;
	protected JOAJTextField mDeltaTolerance = null;
	protected JOAJRadioButton mDifferenceMethod = null;
	protected JOAJRadioButton mSurfaceMethod = null;
	protected JOAJRadioButton mSlopeMethod = null;
	protected JOAJLabel mDepthOfSurfaceLbl = null;
	protected JOAJLabel mStartDepthOfSurfaceLbl = null;
	protected JOAJLabel mMaxDepthLbl = null;
	protected JOAJLabel mSlopeDepthLbl = null;
	protected boolean oldMLState = false;
	protected JCheckBox mMixedLayerCalc = null;
	protected JOAJLabel mdb1Lbl = null;
	protected JOAJLabel mdb2Lbl = null;
	protected JOAJLabel mdb3Lbl = null;
	protected JOAJButton mOKBtn = null;
	protected JOAJButton mCancelButton = null;
	protected JOAJTextField mNewParamName = null;
	protected JOAJTextField mNewParamUnits = null;
	private Timer timer = new Timer();
	protected JOAJLabel mToleranceLbl = null;
	int mMethod = JOAConstants.MIXED_LAYER_DIFFERENCE;
	private JOAJTextField mNameField = new JOAJTextField(40);

	public ConfigMLDCalc(JFrame par, FileViewer fv) {
		super(par, "Station Mixed Layer Depth Calculations", false);
		mFileViewer = fv;
		this.init();
	}

	public void init() {
		ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

		// create the two parameter chooser lists
		this.getContentPane().setLayout(new BorderLayout(5, 5));

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(5, 5));

		// Mixedlayer
		JPanel mixedLayerPanel = new JPanel();
		mixedLayerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));// new
																																			// RowLayout(Orientation.LEFT,
																																			// Orientation.BOTTOM,
																																			// 5));

		mMLParamList = new ParameterChooser(mFileViewer, new String(b.getString("kTestParameter")), this, "SALT       ");
		mMLParamList.init();
		mMLParamList.getJList().addListSelectionListener(this);
		mixedLayerPanel.add(new TenPixelBorder(mMLParamList, 0, 0, 0, 0));

		// method
		JPanel methodCont = new JPanel();
		methodCont.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));

		// difference method
		mDifferenceMethod = new JOAJRadioButton(b.getString("kDifferenceMethod"), true);
		mDifferenceMethod.setToolTipText(b.getString("kDifferenceHelp"));
		methodCont.add(mDifferenceMethod);
		mDifferenceMethod.addItemListener(this);
		JPanel linem1 = new JPanel();
		linem1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		mMaxDepthLbl = new JOAJLabel("     " + b.getString("kDepthOfDifference"));
		linem1.add(mMaxDepthLbl);
		mDepthOfDiffMLField = new JOAJTextField(6);
		mDepthOfDiffMLField.setText("5");
		linem1.add(mDepthOfDiffMLField);
		mDepthOfDiffMLField.getDocument().addDocumentListener(this);
		mdb1Lbl = new JOAJLabel(b.getString("kDB"));
		linem1.add(mdb1Lbl);
		methodCont.add(linem1);

		// surface
		mSurfaceMethod = new JOAJRadioButton(b.getString("kSurfaceMethod"));
		mSurfaceMethod.setToolTipText(b.getString("kSurfaceHelp"));
		mSurfaceMethod.addItemListener(this);
		methodCont.add(mSurfaceMethod);
		JPanel line0 = new JPanel();
		line0.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		mStartDepthOfSurfaceLbl = new JOAJLabel("     " + b.getString("kStartDepthOfSurface"));
		mDepthOfSurfaceLbl = new JOAJLabel("     " + b.getString("kMaxDepthOfSurface"));
		mDepthOfMLField = new JOAJTextField(6);
		mDepthOfMLField.setText("5");
		mDepthOfMLField.getDocument().addDocumentListener(this);
		mStartDepthOfMLField = new JOAJTextField(6);
		mStartDepthOfMLField.setText("0");
		mStartDepthOfMLField.getDocument().addDocumentListener(this);
		line0.add(mStartDepthOfSurfaceLbl);
		line0.add(mStartDepthOfMLField);
		line0.add(mDepthOfSurfaceLbl);
		line0.add(mDepthOfMLField);
		mdb2Lbl = new JOAJLabel(b.getString("kDB"));
		line0.add(mdb2Lbl);
		methodCont.add(line0);

		// slope
		mSlopeMethod = new JOAJRadioButton(b.getString("kSlopeMethod"));
		mSlopeMethod.setToolTipText(b.getString("kSlopeHelp"));
		mSlopeMethod.addItemListener(this);
		methodCont.add(mSlopeMethod);
		JPanel line00 = new JPanel();
		line00.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		mSlopeDepthLbl = new JOAJLabel("     " + b.getString("kDepthOfSlope"));
		line00.add(mSlopeDepthLbl);
		mStartDepthOfSlopeField = new JOAJTextField(6);
		mStartDepthOfSlopeField.setText("5");
		mStartDepthOfSlopeField.getDocument().addDocumentListener(this);
		line00.add(mStartDepthOfSlopeField);
		mdb3Lbl = new JOAJLabel(b.getString("kDB"));
		line00.add(mdb3Lbl);
		methodCont.add(line00);

		ButtonGroup b1 = new ButtonGroup();
		b1.add(mDifferenceMethod);
		b1.add(mSurfaceMethod);
		b1.add(mSlopeMethod);
		mixedLayerPanel.add(new TenPixelBorder(methodCont, 0, 0, 0, 0));

		// tolerance panel
		JPanel line3 = new JPanel();
		line3.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		mToleranceLbl = new JOAJLabel(b.getString("kTolerance"));
		line3.add(mToleranceLbl);
		mDeltaTolerance = new JOAJTextField(6);
		mDeltaTolerance.getDocument().addDocumentListener(this);
		mDeltaTolerance.setText("0.05");
		line3.add(mDeltaTolerance);
		mixedLayerPanel.add(line3);

		// construct the dialog
		JPanel mainPanelContents = new JPanel();
		mainPanelContents.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
		mainPanelContents.add(new TenPixelBorder(mixedLayerPanel, 5, 5, 5, 5));

		JPanel line10 = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 5));
		line10.add(new JOAJLabel(b.getString("kStnVariableName")));
		mNameField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line10.add(mNameField);
		mainPanelContents.add(line10);
		
		mainPanel.add("Center", mainPanelContents);
		this.getContentPane().add("Center", new TenPixelBorder(mainPanel, 10, 10, 10, 10));

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

		this.getContentPane().add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
		this.pack();

    runTimer();
    
		// show dialog at center of screen
		Rectangle dBounds = this.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width / 2 - dBounds.width / 2;
		int y = sd.height / 2 - dBounds.height / 2;
		this.setLocation(x, y);

		setSlopeState(false);
		setSurfaceState(false);
		setDiffState(true);
	}

	public void runTimer() {
		TimerTask task = new TimerTask() {
			public void run() {
				maintainButtons();
			}
		};
		timer.schedule(task, 0, 1000);
	}

	public void valueChanged(ListSelectionEvent evt) {
		if (evt.getSource() == mMLParamList.getJList()) {
			// get the integrand param
			mSelMLParam = mMLParamList.getJList().getSelectedIndex();
			if (mSelMLParam < 0)
				return;
			String selParamText = (String) mMLParamList.getJList().getSelectedValue();

			// make sure value of the param is not missing
			int yerrLine = -1;
			double tempYMin = mFileViewer.mAllProperties[mSelMLParam].getPlotMin();
			double tempYMax = mFileViewer.mAllProperties[mSelMLParam].getPlotMax();
			Triplet newRange = JOAFormulas.GetPrettyRange(tempYMin, tempYMax);
			double yInc = newRange.getVal3();
			if (Double.isNaN(yInc)) {
				yerrLine = mSelMLParam;
			}

			if (yerrLine >= 0) {
				// disable the y param
				JFrame f = new JFrame("Parameter Values Missing Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "All values for " + selParamText + " are missing. " + "\n"
				    + "Select a new parameter");
				mMLParamList.clearSelection();
				mSelMLParam = 0;
			}
		}
		genCalcName();
	}

	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() instanceof JOAJRadioButton) {
			JOAJRadioButton rb = (JOAJRadioButton) evt.getSource();
			if (rb == mSlopeMethod && evt.getStateChange() == ItemEvent.SELECTED) {
				setSlopeState(true);
				setSurfaceState(false);
				setDiffState(false);
			}
			else if (rb == mDifferenceMethod && evt.getStateChange() == ItemEvent.SELECTED) {
				setSlopeState(false);
				setSurfaceState(false);
				setDiffState(true);
			}
			else if (rb == mSurfaceMethod && evt.getStateChange() == ItemEvent.SELECTED) {
				setSlopeState(false);
				setSurfaceState(true);
				setDiffState(false);
			}
			genCalcName();
		}
	}

	public void setSlopeState(boolean state) {
		mStartDepthOfSlopeField.setEnabled(state);
		mSlopeDepthLbl.setEnabled(state);
		mdb3Lbl.setEnabled(state);
	}

	public void setSurfaceState(boolean state) {
		mDepthOfMLField.setEnabled(state);
		mStartDepthOfMLField.setEnabled(state);
		mStartDepthOfSurfaceLbl.setEnabled(state);
		mDepthOfSurfaceLbl.setEnabled(state);
		mdb2Lbl.setEnabled(state);
	}

	public void setDiffState(boolean state) {
		mDepthOfDiffMLField.setEnabled(state);
		mMaxDepthLbl.setEnabled(state);
		mdb1Lbl.setEnabled(state);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("cancel")) {
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("ok")) {
			// transfer pending calculations to the fileviewer for processing
			// configure a mixedlevel calc
			double surfaceDepth = -99;
			double startSurfaceDepth = -99;
			if (mDifferenceMethod.isSelected()) {
				mMethod = JOAConstants.MIXED_LAYER_DIFFERENCE;
				try {
					surfaceDepth = Double.valueOf(mDepthOfDiffMLField.getText()).doubleValue();
				}
				catch (NumberFormatException ex) {
					surfaceDepth = 5;
				}
			}
			else if (mSurfaceMethod.isSelected()) {
				mMethod = JOAConstants.MIXED_LAYER_SURFACE;
				try {
					surfaceDepth = Double.valueOf(mDepthOfMLField.getText()).doubleValue();
				}
				catch (NumberFormatException ex) {
					surfaceDepth = 5;
				}
				try {
					startSurfaceDepth = Double.valueOf(mStartDepthOfMLField.getText()).doubleValue();
				}
				catch (NumberFormatException ex) {
					startSurfaceDepth = 0;
				}
			}
			else {
				mMethod = JOAConstants.MIXED_LAYER_SLOPE;
				try {
					surfaceDepth = Double.valueOf(mStartDepthOfSlopeField.getText()).doubleValue();
				}
				catch (NumberFormatException ex) {
					surfaceDepth = 0;
				}
			}

			double toln = 0.001;
			try {
				toln = Double.valueOf(mDeltaTolerance.getText()).doubleValue();
			}
			catch (NumberFormatException ex) {
				toln = 0.001;
			}

			String param = (String) (mMLParamList.getJList().getSelectedValue());
			
			String nameStr = mNameField.getText();

			if (mMethod == JOAConstants.MIXED_LAYER_DIFFERENCE) {
				Calculation calc = new Calculation(nameStr, new MixedLayerCalcSpec(JOAConstants.MIXED_LAYER_DIFFERENCE, param,
				    startSurfaceDepth, surfaceDepth, toln), JOAConstants.STN_CALC_TYPE);
				calc.setUnits("m");
				mFileViewer.addCalculation(calc);
				try {
					calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
				}
				catch (Exception ex) {
				}
			}
			else if (mMethod == JOAConstants.MIXED_LAYER_SURFACE) {
				Calculation calc = new Calculation(nameStr, new MixedLayerCalcSpec(JOAConstants.MIXED_LAYER_SURFACE, param,
				    startSurfaceDepth, surfaceDepth, toln), JOAConstants.STN_CALC_TYPE);
				calc.setUnits("m");
				mFileViewer.addCalculation(calc);
				try {
					calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
				}
				catch (Exception ex) {
				}
			}
			else if (mMethod == JOAConstants.MIXED_LAYER_SLOPE) {
				Calculation calc = new Calculation(nameStr, new MixedLayerCalcSpec(JOAConstants.MIXED_LAYER_SLOPE, param,
				    startSurfaceDepth, surfaceDepth, toln), JOAConstants.STN_CALC_TYPE);
				calc.setUnits("m");
				mFileViewer.addCalculation(calc);
				try {
					calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
				}
				catch (Exception ex) {
				}
			}

			mFileViewer.doCalcs();
			timer.cancel();
			this.dispose();
		}
	}
	
	private void genCalcName() {
		double surfaceDepth = -99;
		double startSurfaceDepth = -99;
		
		String surfaceDepthStr = "?";
		if (mDifferenceMethod.isSelected()) {
			mMethod = JOAConstants.MIXED_LAYER_DIFFERENCE;
			try {
				surfaceDepth = Double.valueOf(mDepthOfDiffMLField.getText()).doubleValue();
			}
			catch (NumberFormatException ex) {
				surfaceDepth = 5;
			}
			surfaceDepthStr = String.valueOf(surfaceDepth);
		}
		else if (mSurfaceMethod.isSelected()) {
			mMethod = JOAConstants.MIXED_LAYER_SURFACE;
			try {
				surfaceDepth = Double.valueOf(mDepthOfMLField.getText()).doubleValue();
			}
			catch (NumberFormatException ex) {
				surfaceDepth = 5;
			}
			try {
				startSurfaceDepth = Double.valueOf(mStartDepthOfMLField.getText()).doubleValue();
			}
			catch (NumberFormatException ex) {
				startSurfaceDepth = 0;
			}
			surfaceDepthStr = String.valueOf(startSurfaceDepth) + "-" + String.valueOf(surfaceDepth);
		}
		else {
			mMethod = JOAConstants.MIXED_LAYER_SLOPE;
			try {
				surfaceDepth = Double.valueOf(mStartDepthOfSlopeField.getText()).doubleValue();
			}
			catch (NumberFormatException ex) {
				surfaceDepth = 0;
			}
			surfaceDepthStr = String.valueOf(surfaceDepth);
		}

		double toln = 0.001;
		try {
			toln = Double.valueOf(mDeltaTolerance.getText()).doubleValue();
		}
		catch (NumberFormatException ex) {
			toln = 0.001;
		}

		String param = "?";
		if (mMLParamList.getJList().getSelectedValue() != null) {
			param = (String) (mMLParamList.getJList().getSelectedValue());
		}
		
		String calcStr = "";
		if (mMethod == JOAConstants.MIXED_LAYER_DIFFERENCE) {
			calcStr = "MLDF(" + param + ":" + surfaceDepthStr + ","
			    + String.valueOf(toln) + ")";
		}
		else if (mMethod == JOAConstants.MIXED_LAYER_SURFACE) {
			calcStr = "MLSF(" + param + ":" + surfaceDepthStr + ","
			    + String.valueOf(toln) + ")";
		}
		else if (mMethod == JOAConstants.MIXED_LAYER_SLOPE) {
			calcStr = "MLSL(" + param + ":" + surfaceDepthStr + ","
			    + String.valueOf(toln) + ")";
		}
		
		this.mNameField.setText(calcStr);
	}

	public void maintainButtons() {

		boolean MLOK = mSelMLParam >= 0;
		if (!MLOK)
			mOKBtn.setEnabled(false);
		else
			mOKBtn.setEnabled(true);
	}

	/* (non-Javadoc)
   * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
   */
  public void changedUpdate(DocumentEvent e) {
		genCalcName();
  }

	/* (non-Javadoc)
   * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
   */
  public void insertUpdate(DocumentEvent e) {
		genCalcName();
  }

	/* (non-Javadoc)
   * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
   */
  public void removeUpdate(DocumentEvent e) {
  		genCalcName();
  }
}

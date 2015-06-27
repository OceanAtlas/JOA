/*
 * $Id: ConfigInterpolationCalc.java,v 1.5 2005/09/07 18:49:31 oz Exp $
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
public class ConfigInterpolationCalc extends JOAJDialog implements ListSelectionListener, ActionListener,
    ButtonMaintainer, ItemListener, DocumentListener {
	protected FileViewer mFileViewer;

	// Interpolation controls
	protected ParameterChooser mInterpParamList;
	protected ParameterChooser mInterpWRTParamList;
	protected JOAJRadioButton mInterpAtValue = null;
	protected JOAJRadioButton mInterpAtSurface = null;
	protected JOAJRadioButton mInterpAtBottom = null;
	protected JOAJTextField mInterpAtValueField = null;
	protected JOAJTextField mDepthLimitValue = null;
	protected int mSelInterpParam = -1;
	protected int mSelInterpWRTParam = -1;
	protected JOAJLabel mDepthLimit = null;
	protected JCheckBox mInterpUseDeepest = null;
	protected JOAJRadioButton mInterpTopDown = null;
	protected JOAJRadioButton mInterpBottomUp = null;
	protected JOAJLabel interpDirecLbl = null;
	protected boolean oldInterpState = false;
	protected JOAJLabel mdb4Lbl = null;
	private JOAJTextField mNameField;
	protected JOAJButton mOKBtn = null;
	protected JOAJButton mCancelButton = null;
	protected JOAJTextField mNewParamName = null;
	protected JOAJTextField mNewParamUnits = null;
	private Timer timer = new Timer();

	public ConfigInterpolationCalc(JFrame par, FileViewer fv) {
		super(par, "Station Interpolation Calculations", false);
		mFileViewer = fv;
		this.init();
	}

	public void init() {
		ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

		// create the two parameter chooser lists
		this.getContentPane().setLayout(new BorderLayout(5, 5));

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(5, 5));

		// Interpolation
		JPanel interpolationPanel = new JPanel();
		interpolationPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));// (Orientation.LEFT,
																																				// Orientation.BOTTOM,
																																				// 5));
		mInterpParamList = new ParameterChooser(mFileViewer, new String(b.getString("kParameter2")), this,
		    "SALT                ");
		mInterpWRTParamList = new ParameterChooser(mFileViewer, new String(b.getString("kOntoSurface")), this,
		    "SALT                ");
		mInterpParamList.init();
		mInterpWRTParamList.init();
		interpolationPanel.add(new TenPixelBorder(mInterpParamList, 0, 0, 0, 0));
		interpolationPanel.add(new TenPixelBorder(mInterpWRTParamList, 0, 0, 0, 0));

		// range container
		JPanel rangeCont2 = new JPanel();
		rangeCont2.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

		// at value container
		JPanel linei1 = new JPanel();
		linei1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		mInterpAtValue = new JOAJRadioButton(b.getString("kAtValue"), true);
		mInterpAtValueField = new JOAJTextField(6);
		mInterpAtValueField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		linei1.add(mInterpAtValue);
		linei1.add(mInterpAtValueField);
		rangeCont2.add(linei1);
		mInterpAtValueField.getDocument().addDocumentListener(this);
		mInterpAtValue.addItemListener(this);

		JPanel linei4 = new JPanel();
		linei4.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		mInterpTopDown = new JOAJRadioButton(b.getString("kTopDown"), true);
		mInterpBottomUp = new JOAJRadioButton(b.getString("kBottomUp"));
		interpDirecLbl = new JOAJLabel(b.getString("kSearchDirection"));
		linei4.add(interpDirecLbl);
		linei4.add(mInterpTopDown);
		linei4.add(mInterpBottomUp);
		ButtonGroup b4 = new ButtonGroup();
		b4.add(mInterpTopDown);
		b4.add(mInterpBottomUp);
		rangeCont2.add(linei4);
		mInterpTopDown.addItemListener(this);
		mInterpBottomUp.addItemListener(this);

		// at surface container
		JPanel linei2 = new JPanel();
		linei2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		mInterpAtSurface = new JOAJRadioButton(b.getString("kAtSurface"), false);
		mInterpAtSurface.addItemListener(this);
		mDepthLimit = new JOAJLabel(b.getString("kDepthLimit"));
		mDepthLimitValue = new JOAJTextField(6);
		mDepthLimitValue.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		linei2.add(mInterpAtSurface);
		linei2.add(mDepthLimit);
		linei2.add(mDepthLimitValue);
		mdb4Lbl = new JOAJLabel(b.getString("kDB"));
		linei2.add(mdb4Lbl);
		rangeCont2.add(linei2);

		// at bottom container
		JPanel linei3 = new JPanel();
		linei3.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		mInterpAtBottom = new JOAJRadioButton(b.getString("kAtBottom"), false);
		mInterpUseDeepest = new JOAJCheckBox(b.getString("kUseDeepestIfBottomIsMissing"));
		linei3.add(mInterpAtBottom);
		linei3.add(mInterpUseDeepest);
		rangeCont2.add(linei3);

		ButtonGroup b3 = new ButtonGroup();
		b3.add(mInterpAtValue);
		b3.add(mInterpAtSurface);
		b3.add(mInterpAtBottom);
		interpolationPanel.add(rangeCont2);
		mInterpAtBottom.addItemListener(this);
		
		// construct the dialog
		JPanel mainPanelContents = new JPanel();
		mainPanelContents.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
		JPanel line10 = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 5));
		line10.add(new JOAJLabel(b.getString("kStnVariableName")));
		mNameField = new JOAJTextField(40);
		mNameField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line10.add(mNameField);
		
		mainPanelContents.add(new TenPixelBorder(interpolationPanel, 5, 5, 5, 5));
		mainPanelContents.add(line10);
		genCalcName();
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
		genCalcName();
		if (evt.getSource() == mInterpParamList.getJList()) {
			// get the interpolation param
			mSelInterpParam = mInterpParamList.getJList().getSelectedIndex();
			if (mSelInterpParam < 0)
				return;
			String selParamText = (String) mInterpParamList.getJList().getSelectedValue();

			// make sure value of the param is not missing
			int yerrLine = -1;
			double tempYMin = mFileViewer.mAllProperties[mSelInterpParam].getPlotMin();
			double tempYMax = mFileViewer.mAllProperties[mSelInterpParam].getPlotMax();
			Triplet newRange = JOAFormulas.GetPrettyRange(tempYMin, tempYMax);
			double yInc = newRange.getVal3();
			if (Double.isNaN(yInc)) {
				yerrLine = mSelInterpParam;
			}

			if (yerrLine >= 0) {
				// disable the y param
				JFrame f = new JFrame("Parameter Values Missing Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "All values for " + selParamText + " are missing. " + "\n"
				    + "Select a new parameter");
				mInterpParamList.clearSelection();
				mSelInterpParam = 0;
			}
		}
		else if (evt.getSource() == mInterpWRTParamList.getJList()) {
			mSelInterpWRTParam = mInterpWRTParamList.getJList().getSelectedIndex();
			if (mSelInterpWRTParam < 0)
				return;
			String selParamText = (String) mInterpWRTParamList.getJList().getSelectedValue();

			// make sure value of the param is not missing
			int yerrLine = -1;
			double tempYMin = mFileViewer.mAllProperties[mSelInterpWRTParam].getPlotMin();
			double tempYMax = mFileViewer.mAllProperties[mSelInterpWRTParam].getPlotMax();
			Triplet newRange = JOAFormulas.GetPrettyRange(tempYMin, tempYMax);
			double yInc = newRange.getVal3();
			if (Double.isNaN(yInc)) {
				yerrLine = mSelInterpWRTParam;
			}

			if (yerrLine >= 0) {
				// disable the y param
				JFrame f = new JFrame("Parameter Values Missing Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "All values for " + selParamText + " are missing. " + "\n"
				    + "Select a new parameter");
				mInterpWRTParamList.clearSelection();
				mSelInterpWRTParam = 0;
			}
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		genCalcName();
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("cancel")) {
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("ok")) {
			// configure an integration calculation
			double val = -99;
			try {
				val = Double.valueOf(mInterpAtValueField.getText()).doubleValue();
			}
			catch (NumberFormatException ex) {
				val = 0;
			}

			double depthLimit = -99;
			try {
				depthLimit = Double.valueOf(mDepthLimitValue.getText()).doubleValue();
			}
			catch (NumberFormatException ex) {
				depthLimit = -99;
			}

			int interpVar = mInterpParamList.getJList().getSelectedIndex();
			int wrtVar = mInterpWRTParamList.getJList().getSelectedIndex();

			int searchMethod = JOAConstants.SEARCH_TOP_DOWN;

			if (mInterpAtSurface.isSelected()) {
			}
			else if (mInterpAtBottom.isSelected()) {
			}
			else {
				if (mInterpTopDown.isSelected()) {
					searchMethod = JOAConstants.SEARCH_TOP_DOWN;
				}
				else if (mInterpBottomUp.isSelected()) {
					searchMethod = JOAConstants.SEARCH_BOTTOM_UP;
				}
			}
			
			Calculation calc = new Calculation(mNameField.getText(),
			    new InterpolationSpecification(mFileViewer, interpVar, wrtVar, val, mInterpAtSurface.isSelected(),
			        depthLimit, mInterpAtBottom.isSelected(), mInterpUseDeepest.isSelected(), searchMethod, false),
			    JOAConstants.STN_CALC_TYPE);
			calc.setUnits(mFileViewer.mAllProperties[interpVar].getUnits());
			mFileViewer.addCalculation(calc);

			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}

			mFileViewer.doCalcs();
			timer.cancel();
			this.dispose();
		}
	}
	
	private void genCalcName() {
		double val = -99;
		try {
			val = Double.valueOf(mInterpAtValueField.getText()).doubleValue();
		}
		catch (NumberFormatException ex) {
			val = 0;
		}

		double depthLimit = -99;
		try {
			depthLimit = Double.valueOf(mDepthLimitValue.getText()).doubleValue();
		}
		catch (NumberFormatException ex) {
			depthLimit = -99;
		}

		String intParam = "?";
		if (mInterpParamList.getJList().getSelectedValue() != null) {
			intParam = (String) (mInterpParamList.getJList().getSelectedValue());
		}
		String wrtParam = "?";
		if (mInterpWRTParamList.getJList().getSelectedValue() != null) {
			wrtParam = (String) (mInterpWRTParamList.getJList().getSelectedValue());
		}

		String atString;
		String searchDirecStr = "";

		if (mInterpAtSurface.isSelected()) {
			atString = "Surface";
		}
		else if (mInterpAtBottom.isSelected()) {
			atString = "Bottom";
		}
		else {
			atString = String.valueOf(val);
			if (mInterpTopDown.isSelected()) {
				searchDirecStr = "-td";
			}
			else if (mInterpBottomUp.isSelected()) {
				searchDirecStr = "-bu";
			}
		}
		
		String nameStr = "INTRP(" + intParam + " on " + wrtParam + " @" + atString + ")" + searchDirecStr;
		
		mNameField.setText(nameStr);
	}

	public void maintainButtons() {
		// maintain the sub controls
		// if pressure is selected then maintain the at surface and bottom controls
		if (mSelInterpWRTParam != 0) {
			mInterpAtValue.setSelected(true);
			mInterpAtSurface.setEnabled(false);
			mInterpAtBottom.setEnabled(false);
		}
		else {
			mInterpAtSurface.setEnabled(true);
			mInterpAtBottom.setEnabled(true);
		}

		if (mInterpAtValue.isSelected()) {
			if (!mInterpAtValueField.isEnabled())
				mInterpAtValueField.setEnabled(true);
			if (!mInterpTopDown.isEnabled())
				mInterpTopDown.setEnabled(true);
			if (!mInterpBottomUp.isEnabled())
				mInterpBottomUp.setEnabled(true);
			if (!interpDirecLbl.isEnabled())
				interpDirecLbl.setEnabled(true);
			if (mDepthLimitValue.isEnabled())
				mDepthLimitValue.setEnabled(false);
			if (mDepthLimit.isEnabled())
				mDepthLimit.setEnabled(false);
			if (mInterpUseDeepest.isEnabled())
				mInterpUseDeepest.setEnabled(false);
			if (mdb4Lbl.isEnabled())
				mdb4Lbl.setEnabled(false);
		}
		else if (mInterpAtSurface.isSelected()) {
			if (mInterpTopDown.isEnabled())
				mInterpTopDown.setEnabled(false);
			if (mInterpBottomUp.isEnabled())
				mInterpBottomUp.setEnabled(false);
			if (interpDirecLbl.isEnabled())
				interpDirecLbl.setEnabled(false);
			if (mInterpAtValueField.isEnabled())
				mInterpAtValueField.setEnabled(false);
			if (!mDepthLimitValue.isEnabled())
				mDepthLimitValue.setEnabled(true);
			if (!mDepthLimit.isEnabled())
				mDepthLimit.setEnabled(true);
			if (mInterpUseDeepest.isEnabled())
				mInterpUseDeepest.setEnabled(false);
			if (!mdb4Lbl.isEnabled())
				mdb4Lbl.setEnabled(true);
		}
		else if (mInterpAtBottom.isSelected()) {
			if (mInterpTopDown.isEnabled())
				mInterpTopDown.setEnabled(false);
			if (mInterpBottomUp.isEnabled())
				mInterpBottomUp.setEnabled(false);
			if (interpDirecLbl.isEnabled())
				interpDirecLbl.setEnabled(false);
			if (mInterpAtValueField.isEnabled())
				mInterpAtValueField.setEnabled(false);
			if (mDepthLimitValue.isEnabled())
				mDepthLimitValue.setEnabled(false);
			if (mDepthLimit.isEnabled())
				mDepthLimit.setEnabled(false);
			if (!mInterpUseDeepest.isEnabled())
				mInterpUseDeepest.setEnabled(true);
			if (mdb4Lbl.isEnabled())
				mdb4Lbl.setEnabled(false);
		}

		boolean IntrpOK = false;
		if (mSelInterpParam >= 0 && mSelInterpWRTParam >= 0) {
			IntrpOK = true;
			if (mInterpAtValue.isSelected()) {
				// test for valid interpolation surface value
				try {
					Double.valueOf(mInterpAtValueField.getText()).doubleValue();
				}
				catch (NumberFormatException ex) {
					IntrpOK = false;
				}
			}
		}

		if (!IntrpOK)
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

/*
 * $Id: ConfigExtremumCalc.java,v 1.6 2005/09/07 18:49:30 oz Exp $
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
public class ConfigExtremumCalc extends JOAJDialog implements ListSelectionListener, ActionListener, ButtonMaintainer,
    ItemListener {
  protected FileViewer mFileViewer;

  //Interpolation controls
  protected JOAJRadioButton mMinExtremum = null;
  protected JOAJRadioButton mMaxExtremum = null;
  protected ParameterChooser mParamList;
  protected JOAJComboBox mSurfParamCombo;
  protected ParameterChooser mOtherParamList;
  protected JOAJRadioButton mFullRange = null;
  protected JOAJRadioButton mSubRange = null;
  protected JOAJTextField mMinRangeValueField = null;
  protected JOAJTextField mMaxRangeValueField = null;
  protected JOAJLabel mLTESymbol1 = null;
  protected JOAJLabel mLTESymbol2 = null;
  protected int mSelParam = -1;
  protected int mSelSurfParam = -1;
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mCancelButton = null;
  protected JOAJTextField mNewParamName = null;
  protected JOAJTextField mNewParamUnits = null;
	private Timer timer = new Timer();

  public ConfigExtremumCalc(JFrame par, FileViewer fv) {
    super(par, "Station Extrema Calculations", false);
    mFileViewer = fv;
    this.init();
  }

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    this.getContentPane().setLayout(new BorderLayout(5, 5));

    // this panel holds the UI for selecting the extremum specification
    JPanel upperPanel = new JPanel();
    upperPanel.setLayout(new RowLayout(Orientation.LEFT, Orientation.CENTER, 0));

    // this panel hold the scroller for selecting the other parameters to report
    JPanel lowerPanel = new JPanel();
    lowerPanel.setLayout(new RowLayout(Orientation.LEFT, Orientation.CENTER, 0));

    // this panel holds the max/min radio button
    JPanel minMaxPanel = new JPanel();
    minMaxPanel.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 5));

    // this panel holds the controls for selecting the range
    JPanel rangePanel = new JPanel();
    rangePanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

    // build the max/min panel
    mMinExtremum = new JOAJRadioButton(b.getString("kMin"), true);
    mMaxExtremum = new JOAJRadioButton(b.getString("kMax"), false);
    ButtonGroup b1 = new ButtonGroup();
    b1.add(mMinExtremum);
    b1.add(mMaxExtremum);
    minMaxPanel.add(mMinExtremum);
    minMaxPanel.add(mMaxExtremum);
    upperPanel.add(minMaxPanel);

    // build the parameter chooser panels
    mParamList = new ParameterChooser(mFileViewer, new String(b.getString("kParameter2")), this, "SALT                ");
    mOtherParamList = new ParameterChooser(mFileViewer, new String(b.getString("kOtherParams")), this,
                                           "SALT                ", true);
    mParamList.init();
    mOtherParamList.init();
    upperPanel.add(new TenPixelBorder(mParamList, 0, 0, 0, 0));

    // build the range panel
    mFullRange = new JOAJRadioButton(b.getString("kOverEachProfile"), true);
    mSubRange = new JOAJRadioButton(b.getString("kInRange"), false);
    ButtonGroup b2 = new ButtonGroup();
    b2.add(mFullRange);
    b2.add(mSubRange);
    JPanel subRangePanel = new JPanel();
    subRangePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
    mMinRangeValueField = new JOAJTextField(6);
    mMinRangeValueField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mMaxRangeValueField = new JOAJTextField(6);
    mMaxRangeValueField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mLTESymbol1 = new JOAJLabel("<=");
    mSurfParamCombo = new JOAJComboBox();
    for (int i = 0; i < mFileViewer.gNumProperties; i++) {
      mSurfParamCombo.addItem(mFileViewer.mAllProperties[i].getVarLabel());
    }
    mLTESymbol2 = new JOAJLabel("<=");

    subRangePanel.add(mMinRangeValueField);
    subRangePanel.add(mLTESymbol1);
    subRangePanel.add(mSurfParamCombo);
    subRangePanel.add(mLTESymbol2);
    subRangePanel.add(mMaxRangeValueField);

    rangePanel.add(mFullRange);
    rangePanel.add(mSubRange);
    rangePanel.add(subRangePanel);
    upperPanel.add(new TenPixelBorder(rangePanel, 5, 5, 5, 5));
    upperPanel.add(mOtherParamList);

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

    this.getContentPane().add(new TenPixelBorder(upperPanel, 5, 5, 5, 5), "Center");
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
    if (evt.getSource() == mParamList.getJList()) {
      // get the interpolation param
      mSelParam = mParamList.getJList().getSelectedIndex();
      if (mSelParam < 0) {
        return;
      }
      String selParamText = (String)mParamList.getJList().getSelectedValue();

      // make sure value of the param is not missing
      int yerrLine = -1;
      double tempYMin = mFileViewer.mAllProperties[mSelParam].getPlotMin();
      double tempYMax = mFileViewer.mAllProperties[mSelParam].getPlotMax();
      Triplet newRange = JOAFormulas.GetPrettyRange(tempYMin, tempYMax);
      double yInc = newRange.getVal3();
      if (Double.isNaN(yInc)) {
        yerrLine = mSelParam;
      }

      if (yerrLine >= 0) {
        //disable the y param
        JFrame f = new JFrame("Parameter Values Missing Error");
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(f,
                                      "All values for " + selParamText + " are missing. " + "\n" + "Select a new parameter");
        mParamList.clearSelection();
        mSelParam = 0;
      }
    }
  }

  public void itemStateChanged(ItemEvent evt) {
    if (evt.getSource() instanceof JOAJComboBox) {
      JOAJComboBox cb = (JOAJComboBox)evt.getSource();
      if (cb == mSurfParamCombo && evt.getStateChange() == ItemEvent.SELECTED) {
        mSelSurfParam = cb.getSelectedIndex();
      }
    }
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      timer.cancel();
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      //configure an extremum calculation
      String[] otherParams = null;
      String[] otherUnits = null;
      double minVal = JOAConstants.MISSINGVALUE;
      double maxVal = JOAConstants.MISSINGVALUE;
      String extType = "Max";
      String rangeType = "";
      if (mSubRange.isSelected()) {
        try {
          minVal = Double.valueOf(mMinRangeValueField.getText()).doubleValue();
        }
        catch (NumberFormatException ex) {
          minVal = JOAConstants.MISSINGVALUE;
        }

        try {
          maxVal = Double.valueOf(mMaxRangeValueField.getText()).doubleValue();
        }
        catch (NumberFormatException ex) {
          maxVal = JOAConstants.MISSINGVALUE;
        }
        if (minVal != JOAConstants.MISSINGVALUE && maxVal != JOAConstants.MISSINGVALUE) {
          rangeType = " in range " + mMinRangeValueField.getText() + ":" + mMaxRangeValueField.getText();
        }
        if (minVal != JOAConstants.MISSINGVALUE && maxVal == JOAConstants.MISSINGVALUE) {
          rangeType = " >= " + mMinRangeValueField.getText();
        }
        if (minVal == JOAConstants.MISSINGVALUE && maxVal != JOAConstants.MISSINGVALUE) {
          rangeType = " <= " + mMaxRangeValueField.getText();
        }
      }

      int extremumVar = mParamList.getJList().getSelectedIndex();
      int surfVar = mSurfParamCombo.getSelectedIndex();
      String eParam = (String)(mParamList.getJList().getSelectedValue());
      String sParam = (String)(mSurfParamCombo.getSelectedItem());
      if (mMinExtremum.isSelected()) {
        extType = "Min";
      }

      // get any other params to report
      int[] mSelParams = null;
      mSelParams = mOtherParamList.getJList().getSelectedIndices();

      if (mSelParams != null && mSelParams.length > 0) {
        otherParams = new String[mSelParams.length];
        otherUnits = new String[mSelParams.length];
        for (int i = 0; i < mSelParams.length; i++) {
          otherParams[i] = new String(mFileViewer.mAllProperties[mSelParams[i]].getVarLabel());
          otherUnits[i] = new String(mFileViewer.mAllProperties[mSelParams[i]].getUnits());
        }
      }

      String where = new String("");
      if (mSubRange.isSelected()) {
        where = " where " + sParam;
      }

      Calculation calc = new Calculation(extType + " of " + eParam + where + rangeType,
                                         new ExtremumSpecification(mFileViewer, extremumVar, surfVar,
          mMaxExtremum.isSelected(), minVal, maxVal, otherParams, otherUnits), JOAConstants.STN_CALC_TYPE);
      calc.setUnits(mFileViewer.mAllProperties[extremumVar].getUnits());
      mFileViewer.addCalculation(calc);

      try {
        calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
      }
      catch (Exception ex) {}

      mFileViewer.doCalcs();
      timer.cancel();
      this.dispose();
    }
  }

  public void maintainButtons() {
    // maintain the sub controls
    // if pressure is selected then maintain the at surface and bottom controls
    if (mSubRange.isSelected()) {
      if (!mMinRangeValueField.isEnabled()) {
        mMinRangeValueField.setEnabled(true);
      }

      if (!mMaxRangeValueField.isEnabled()) {
        mMaxRangeValueField.setEnabled(true);
      }

      if (!mLTESymbol1.isEnabled()) {
        mLTESymbol1.setEnabled(true);
      }

      if (!mLTESymbol2.isEnabled()) {
        mLTESymbol2.setEnabled(true);
      }

      if (!mSurfParamCombo.isEnabled()) {
        mSurfParamCombo.setEnabled(true);
      }
    }
    else {
      if (mMinRangeValueField.isEnabled()) {
        mMinRangeValueField.setEnabled(false);
      }

      if (mMaxRangeValueField.isEnabled()) {
        mMaxRangeValueField.setEnabled(false);
      }

      if (mLTESymbol1.isEnabled()) {
        mLTESymbol1.setEnabled(false);
      }

      if (mLTESymbol2.isEnabled()) {
        mLTESymbol2.setEnabled(false);
      }

      if (mSurfParamCombo.isEnabled()) {
        mSurfParamCombo.setEnabled(false);
      }
    }

    boolean IntrpOK = false;
    boolean minEmpty = false;
    boolean maxEmpty = false;
    if (mSelParam >= 0) {
      IntrpOK = true;
      minEmpty = false;
      maxEmpty = false;
      if (mSubRange.isSelected()) {
        // test for valid interpolation surface value
        if (mMinRangeValueField.getText().length() == 0) {
          minEmpty = true;
        }
        else {
          try {
            Double.valueOf(mMinRangeValueField.getText()).doubleValue();
          }
          catch (NumberFormatException ex) {
            IntrpOK = false;
          }
        }

        if (mMaxRangeValueField.getText().length() == 0) {
          maxEmpty = true;
        }
        else {
          try {
            Double.valueOf(mMaxRangeValueField.getText()).doubleValue();
          }
          catch (NumberFormatException ex) {
            IntrpOK = false;
          }
        }
      }
    }

    if (!IntrpOK || (minEmpty && maxEmpty)) {
      mOKBtn.setEnabled(false);
    }
    else {
      mOKBtn.setEnabled(true);
    }
  }
}

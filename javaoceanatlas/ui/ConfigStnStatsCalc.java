/*
 * $Id: ConfigStnStatsCalc.java,v 1.7 2005/09/07 18:49:32 oz Exp $
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
public class ConfigStnStatsCalc extends JOAJDialog implements ListSelectionListener, ActionListener, ButtonMaintainer,
    ItemListener {
  protected FileViewer mFileViewer;

  //Interpolation controls
  protected ParameterChooser mParamList;
  protected JCheckBox mCalcMin = null;
  protected JCheckBox mCalcMax = null;
  protected JCheckBox mCalcAvg = null;
  protected JCheckBox mCalcDepthOfMax = null;
  protected JCheckBox mCalcDepthOfMin = null;
  protected JCheckBox mCalcMaxDepthOfNonMissingVal = null;
  protected JCheckBox mCalcMinDepthOfNonMissingVal = null;
  protected JCheckBox mCalcN = null;
  protected int mSelParam = -1;
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mCancelButton = null;
  protected JOAJTextField mNewParamName = null;
  protected JOAJTextField mNewParamUnits = null;
	private Timer timer = new Timer();

  public ConfigStnStatsCalc(JFrame par, FileViewer fv) {
    super(par, "Station Statistics Calculations", false);
    mFileViewer = fv;
    this.init();
  }

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    // create the two parameter chooser lists
    this.getContentPane().setLayout(new BorderLayout(5, 5));

    JPanel upperPanel = new JPanel();
    upperPanel.setLayout(new RowLayout(Orientation.LEFT, Orientation.TOP, 0));

    // build the parameter chooser panels
    mParamList = new ParameterChooser(mFileViewer, new String(b.getString("kParameter2")), this, 10,
                                      "SALT                ", true);
    mParamList.init();
    upperPanel.add(new TenPixelBorder(mParamList, 0, 0, 0, 0));

    JPanel content = new JPanel();
    content.setLayout(new GridLayout(7, 2, 0, 0));

    content.add(new JOAJLabel(""));

    mCalcMin = new JOAJCheckBox(b.getString("kMinimumValue"), false);
    content.add(mCalcMin);

    mCalcMax = new JOAJCheckBox(b.getString("kMaximumValue"), false);
    content.add(mCalcMax);

    mCalcDepthOfMin = new JOAJCheckBox(b.getString("kDepthMinimumValue"), false);
    content.add(mCalcDepthOfMin);

    mCalcDepthOfMax = new JOAJCheckBox(b.getString("kDepthMaximumValue"), false);
    content.add(mCalcDepthOfMax);

    mCalcMaxDepthOfNonMissingVal = new JOAJCheckBox(b.getString("kMaxDepthofNonMissingValue"), false);
    //content.add(mCalcMaxDepthOfNonMissingVal);

    mCalcMinDepthOfNonMissingVal = new JOAJCheckBox(b.getString("kMinDepthofNonMissingValue"), false);
    //content.add(mCalcMinDepthOfNonMissingVal);

    mCalcAvg = new JOAJCheckBox(b.getString("kCalcAverage"), false);
    content.add(mCalcAvg);

    mCalcN = new JOAJCheckBox(b.getString("kNumberNonMissing"), false);
    content.add(mCalcN);

    upperPanel.add(new TenPixelBorder(content, 10, 10, 10, 10));

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

    this.getContentPane().add(new TenPixelBorder(upperPanel, 5, 10, 5, 5), "Center");
    this.getContentPane().add(new TenPixelBorder(dlgBtnsInset, 5, 10, 5, 5), "South");
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
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      timer.cancel();
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      // get any other params to report
      int[] mSelParams = null;
      mSelParams = mParamList.getJList().getSelectedIndices();
      int pPos = mFileViewer.getPRESPropertyPos();

      for (int i = 0; i < mSelParams.length; i++) {
        if (mCalcMin.isSelected()) {
          // configure a min value
          Calculation calc = new Calculation(mFileViewer.mAllProperties[mSelParams[i]].getVarLabel() + "_min",
                                             new
                                             StnStatisticsSpecification(mFileViewer, mSelParams[i], JOAConstants.CALC_MIN),
                                             JOAConstants.STN_CALC_TYPE);
          calc.setUnits(mFileViewer.mAllProperties[mSelParams[i]].getUnits());
          mFileViewer.addCalculation(calc);
          try {
            calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
          }
          catch (Exception ex) {}
        }

        if (mCalcMax.isSelected()) {
          // configure a min value
          Calculation calc = new Calculation(mFileViewer.mAllProperties[mSelParams[i]].getVarLabel() + "_max",
                                             new
                                             StnStatisticsSpecification(mFileViewer, mSelParams[i], JOAConstants.CALC_MAX),
                                             JOAConstants.STN_CALC_TYPE);
          calc.setUnits(mFileViewer.mAllProperties[mSelParams[i]].getUnits());
          mFileViewer.addCalculation(calc);
          try {
            calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
          }
          catch (Exception ex) {}
        }

        if (mCalcAvg.isSelected()) {
          // configure a min value
          Calculation calc = new Calculation(mFileViewer.mAllProperties[mSelParams[i]].getVarLabel() + "_avg",
                                             new
                                             StnStatisticsSpecification(mFileViewer, mSelParams[i], JOAConstants.CALC_AVERAGE),
                                             JOAConstants.STN_CALC_TYPE);
          calc.setUnits(mFileViewer.mAllProperties[mSelParams[i]].getUnits());
          mFileViewer.addCalculation(calc);
          try {
            calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
          }
          catch (Exception ex) {}
        }

        if (mCalcN.isSelected()) {
          // configure a min value
          Calculation calc = new Calculation(mFileViewer.mAllProperties[mSelParams[i]].getVarLabel() + "_n",
                                             new
                                             StnStatisticsSpecification(mFileViewer, mSelParams[i], JOAConstants.CALC_N),
                                             JOAConstants.STN_CALC_TYPE);
          calc.setUnits("none");
          mFileViewer.addCalculation(calc);
          try {
            calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
          }
          catch (Exception ex) {}
        }

        if (mCalcDepthOfMax.isSelected()) {
          // configure a min value
          Calculation calc = new Calculation(mFileViewer.mAllProperties[mSelParams[i]].getVarLabel() + " depth of max",
                                             new
                                             StnStatisticsSpecification(mFileViewer, mSelParams[i], JOAConstants.CALC_DEPTH_OF_MAX),
                                             JOAConstants.STN_CALC_TYPE);
          calc.setUnits(mFileViewer.mAllProperties[pPos].getUnits());
          mFileViewer.addCalculation(calc);
          try {
            calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
          }
          catch (Exception ex) {}
        }

        if (mCalcDepthOfMin.isSelected()) {
          // configure a min value
          Calculation calc = new Calculation(mFileViewer.mAllProperties[mSelParams[i]].getVarLabel() + " depth of min",
                                             new
                                             StnStatisticsSpecification(mFileViewer, mSelParams[i], JOAConstants.CALC_DEPTH_OF_MIN),
                                             JOAConstants.STN_CALC_TYPE);
          calc.setUnits(mFileViewer.mAllProperties[pPos].getUnits());
          mFileViewer.addCalculation(calc);
          try {
            calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
          }
          catch (Exception ex) {}
        }

      }
      mFileViewer.doCalcs();
      timer.cancel();
      this.dispose();
    }
  }

  public void maintainButtons() {
    // maintain the sub controls
    // if pressure is selected then maintain the at surface and bottom controls
    boolean somethingSelected = mCalcMin.isSelected() || mCalcMax.isSelected() || mCalcAvg.isSelected() ||
        mCalcN.isSelected() || mCalcDepthOfMax.isSelected() || mCalcDepthOfMin.isSelected();

    if (!somethingSelected || mSelParam < 0) {
      mOKBtn.setEnabled(false);
    }
    else {
      mOKBtn.setEnabled(true);
    }
  }
}

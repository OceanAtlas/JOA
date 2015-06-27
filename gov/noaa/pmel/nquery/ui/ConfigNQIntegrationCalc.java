/*
 * $Id: ConfigIntegrationCalc.java,v 1.12 2005/10/18 23:43:05 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package gov.noaa.pmel.nquery.ui;

import javax.swing.JDialog;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionListener;
import javax.swing.event.DocumentListener;
import javaoceanatlas.utility.ButtonMaintainer;
import java.awt.event.ItemListener;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import gov.noaa.pmel.nquery.utility.NQueryCalculation;
import javaoceanatlas.utility.DialogClient;
import gov.noaa.pmel.nquery.resources.NQueryConstants;
import gov.noaa.pmel.nquery.specifications.NQIntegrationSpecification;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import gov.noaa.pmel.eps2.ExportVariable;
import java.awt.Container;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import javaoceanatlas.utility.TenPixelBorder;
import javaoceanatlas.utility.ColumnLayout;
import javaoceanatlas.utility.Orientation;
import java.awt.GridLayout;
import javax.swing.event.DocumentEvent;
import java.util.Iterator;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ActionEvent;
import java.awt.Cursor;
import javax.swing.ButtonGroup;
import gov.noaa.pmel.nquery.utility.NQParameterChooser;
import javaoceanatlas.ui.widgets.JOAJDialog;

/**
 * <code>ConfigCalculations</code> UI for configuring an Integration Calculation.
 *
 * @author oz
 * @version 1.0
 */

public class ConfigNQIntegrationCalc extends JOAJDialog implements ListSelectionListener, ActionListener, DocumentListener,
    ButtonMaintainer, ItemListener, ConfigNQCalcDialog {
  protected NQParameterChooser mIntegrandParamList;
  protected NQParameterChooser mWRTParamList;
  protected int mSelIntParam = -1;
  protected int mSelWRTParam = -1;
  protected JTextField mMinOfIntRange = null;
  protected JTextField mMaxOfIntRange = null;
  protected JCheckBox mUseShallowest = null;
  protected JLabel mUseCustomMax = null;
  protected JCheckBox mUseDeepest = null;
  protected JCheckBox mInterpMissing = null;
  protected JCheckBox mComputeWeightedMean = null;
  protected JLabel mToleranceLbl = null;
  protected boolean oldIntState = false;
  protected JRadioButton mTopDown = null;
  protected JRadioButton mBottomUp = null;
  protected JRadioButton mMixed = null;
  protected JLabel mUseCustomMin = null;
  protected JLabel integrationDirecLbl = null;
  protected JButton mOKBtn = null;
  protected JButton mCancelButton = null;
  protected JTextField mNewParamName = null;
  protected JTextField mNewParamUnits = null;
	private Timer timer = new Timer();
  ArrayList mVarList;
  NQueryCalculation mCalc = null;
  DialogClient mClient;
  NQIntegrationSpecification mSpec;
  protected ResourceBundle b = ResourceBundle.getBundle("gov.noaa.pmel.nquery.resources.NQueryResources");

  public ConfigNQIntegrationCalc(JFrame par, ArrayList varList, DialogClient client) {
    super(par, "", false);
    this.setTitle(b.getString("kIntegrationCalculations"));
    mVarList = varList;
    mClient = client;
    this.init();
  }

  public void setSpecification(NQueryCalculation calc, NQIntegrationSpecification inspec) {
    mCalc = calc;
    mSpec = inspec;
    // modify the UI to reflect
    ExportVariable intVar = mSpec.getIntVar();
    mIntegrandParamList.setSelectedLine(intVar);
    ExportVariable wrtVar = mSpec.getWRTVar();
    mWRTParamList.setSelectedLine(wrtVar);
    double min = mSpec.getMinIntVal();
    mMinOfIntRange.setText(String.valueOf(min));
    double max = mSpec.getMaxIntVal();
    mMaxOfIntRange.setText(String.valueOf(max));

    if (mSpec.isComputeMean()) {
      mComputeWeightedMean.setSelected(true);
    }
    else {
      mComputeWeightedMean.setSelected(false);
    }

    if (mSpec.isInterpolateMissing()) {
      mInterpMissing.setSelected(true);
    }
    else {
      mInterpMissing.setSelected(false);
    }

    if (mSpec.isUseDeepest()) {
      mUseDeepest.setSelected(true);
    }
    else {
      mUseDeepest.setSelected(false);
    }

    if (mSpec.isUseShallowest()) {
      mUseShallowest.setSelected(true);
    }
    else {
      mUseShallowest.setSelected(false);
    }
    if (mSpec.getSearchMethod() == NQueryConstants.SEARCH_TOP_DOWN) {
      mTopDown.setSelected(true);
      mBottomUp.setSelected(false);
      mMixed.setSelected(false);
    }
    else if (mSpec.getSearchMethod() == NQueryConstants.SEARCH_BOTTOM_UP) {
      mTopDown.setSelected(false);
      mBottomUp.setSelected(true);
      mMixed.setSelected(false);
    }
    else if (mSpec.getSearchMethod() == NQueryConstants.SEARCH_FROM_BOTTOM_AND_TOP) {
      mTopDown.setSelected(false);
      mBottomUp.setSelected(false);
      mMixed.setSelected(true);
    }

    // set the custom calc name
    mNewParamName.setText(mCalc.getCalcType());
  }

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("gov.noaa.pmel.nquery.resources.NQueryResources");

    // create the two parameter chooser lists
    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(5, 5));

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout(5, 5));

    // Integration
    JPanel integrationPanel = new JPanel();
    integrationPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0)); //(Orientation.LEFT, Orientation.BOTTOM, 5));
    mIntegrandParamList = new NQParameterChooser(mVarList, new String(b.getString("kIntegrand")), this,
                                               "SALT                ");
    mWRTParamList = new NQParameterChooser(mVarList, new String(b.getString("kWRT")), this, "SALT                ");
    mIntegrandParamList.init();
    mWRTParamList.init();
    integrationPanel.add(new TenPixelBorder(mIntegrandParamList, 0, 0, 0, 0));
    integrationPanel.add(new TenPixelBorder(mWRTParamList, 0, 0, 0, 0));

    // range container
    JPanel rangeCont = new JPanel();
    rangeCont.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

    //range panel
    JPanel rangePanel = new JPanel();
    rangePanel.setLayout(new GridLayout(1, 2, 5, 5));

    // min panel containers
    JPanel minCont = new JPanel();
    minCont.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
    JPanel line1 = new JPanel();
    line1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    mUseCustomMin = new JLabel(b.getString("kMinimum"));
    mMinOfIntRange = new JTextField(6);
    mMinOfIntRange.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mMinOfIntRange.getDocument().addDocumentListener(this);
    mUseShallowest = new JCheckBox(b.getString("kSurfaceAsMin"), true);
    line1.add(mUseCustomMin);
    line1.add(mMinOfIntRange);
    minCont.add(line1);
    minCont.add(mUseShallowest);
    rangePanel.add(minCont);

    // max panel containers
    JPanel maxCont = new JPanel();
    maxCont.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
    JPanel line2 = new JPanel();
    line2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    mUseCustomMax = new JLabel(b.getString("kMaximum"));
    mMaxOfIntRange = new JTextField(6);
    mMaxOfIntRange.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mMaxOfIntRange.getDocument().addDocumentListener(this);
    mUseDeepest = new JCheckBox(b.getString("kBottomAsMax"), true);
    line2.add(mUseCustomMax);
    line2.add(mMaxOfIntRange);
    maxCont.add(line2);
    maxCont.add(mUseDeepest);
    rangePanel.add(maxCont);

    JPanel line5 = new JPanel();
    line5.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    mTopDown = new JRadioButton(b.getString("kTopDown"), true);
    mTopDown.addItemListener(this);
    mBottomUp = new JRadioButton(b.getString("kBottomUp"));
    mBottomUp.addItemListener(this);
    mMixed = new JRadioButton(b.getString("kMixedSearch"));
    mMixed.addItemListener(this);
    integrationDirecLbl = new JLabel(b.getString("kIntegrationDirectioncolon"));
    line5.add(integrationDirecLbl);
    line5.add(mTopDown);
    line5.add(mBottomUp);
    line5.add(mMixed);
    ButtonGroup b2 = new ButtonGroup();
    b2.add(mTopDown);
    b2.add(mBottomUp);
    b2.add(mMixed);

    mComputeWeightedMean = new JCheckBox(b.getString("kComputeWeightedMean"), true);
    mInterpMissing = new JCheckBox(b.getString("kInterpMissing"), true);

    rangeCont.add(rangePanel);
    rangeCont.add(mComputeWeightedMean);
    rangeCont.add(mInterpMissing);
    rangeCont.add(line5);
    integrationPanel.add(new TenPixelBorder(rangeCont, 0, 0, 0, 0));

    // construct the dialog
    JPanel mainPanelContents = new JPanel();
    mainPanelContents.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
    mainPanelContents.add(new TenPixelBorder(integrationPanel, 5, 5, 5, 5));
    mainPanel.add("Center", mainPanelContents);
    this.getContentPane().add("Center", new TenPixelBorder(mainPanel, 10, 10, 10, 10));

    // variable name field
    JPanel lastline = new JPanel();
    lastline.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
    lastline.add(new JLabel(b.getString("kVariableName")));
    mNewParamName = new JTextField(40);
    lastline.add(mNewParamName);
    mainPanel.add("South", lastline);

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
    if (NQueryConstants.ISMAC) {
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
    if (evt.getSource() == mIntegrandParamList.getJList()) {
      // get the integrand param
      mSelIntParam = mIntegrandParamList.getJList().getSelectedIndex();
      if (mSelIntParam < 0) {
        return;
      }
      String selParamText = (String)mIntegrandParamList.getJList().getSelectedValue();
    }
    else if (evt.getSource() == mWRTParamList.getJList()) {
      mSelWRTParam = mWRTParamList.getJList().getSelectedIndex();
      if (mSelWRTParam < 0) {
        return;
      }
      String selParamText = (String)mWRTParamList.getJList().getSelectedValue();
    }
    generateVariableName();
  }

  public void itemStateChanged(ItemEvent evt) {
    generateVariableName();
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      timer.cancel();
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      // transfer pending calculations to the fileviewer for processing
      //configure an integration calculation
      double min = -99;
      try {
        min = Double.valueOf(mMinOfIntRange.getText()).doubleValue();
      }
      catch (NumberFormatException ex) {
        min = 0;
      }

      double max = -99;
      try {
        max = Double.valueOf(mMaxOfIntRange.getText()).doubleValue();
      }
      catch (NumberFormatException ex) {
        max = 5;
      }

      String intParamStr = (String)(mIntegrandParamList.getJList().getSelectedValue());
      String wrtParamStr = (String)(mWRTParamList.getJList().getSelectedValue());

      String searchDirecStr = "";
      int searchMethod = NQueryConstants.SEARCH_TOP_DOWN;
      if (mTopDown.isSelected()) {
        searchMethod = NQueryConstants.SEARCH_TOP_DOWN;
        searchDirecStr = "-td";
      }
      else if (mBottomUp.isSelected()) {
        searchMethod = NQueryConstants.SEARCH_BOTTOM_UP;
        searchDirecStr = "-bu";
      }
      else if (mMixed.isSelected()) {
        searchMethod = NQueryConstants.SEARCH_FROM_BOTTOM_AND_TOP;
        searchDirecStr = "-td/bu";
      }

      // get the wrt ExportVariable represented by this selection
      Iterator itor = mVarList.iterator();
      ExportVariable wrtVar = null;
      while (itor.hasNext()) {
        wrtVar = (ExportVariable)itor.next();
        if (wrtVar.getPresentationVarName().equalsIgnoreCase(wrtParamStr)) {
          break;
        }
      }

      // get the wrt ExportVariable represented by this selection
      itor = mVarList.iterator();
      ExportVariable integrandVar = null;
      while (itor.hasNext()) {
        integrandVar = (ExportVariable)itor.next();
        if (integrandVar.getPresentationVarName().equalsIgnoreCase(intParamStr)) {
          break;
        }
      }

      if (mSpec == null) {
        mCalc = new NQueryCalculation(mNewParamName.getText(), "INTEG",
                                      new NQIntegrationSpecification(integrandVar, wrtVar, min, max,
            mUseShallowest.isSelected(), mUseDeepest.isSelected(), mComputeWeightedMean.isSelected(), searchMethod,
            mInterpMissing.isSelected()), NQueryConstants.STN_CALC_TYPE, true, true);
        mCalc.setUnits(integrandVar.getVarUnits());
      }
      else {
        // existing calculation
        // reset title
        mCalc.setCalcType(mNewParamName.getText());
        mSpec = new NQIntegrationSpecification(integrandVar, wrtVar, min, max, mUseShallowest.isSelected(),
                                               mUseDeepest.isSelected(), mComputeWeightedMean.isSelected(),
                                               searchMethod, mInterpMissing.isSelected());
        mCalc.setUnits(integrandVar.getVarUnits());
        mCalc.setArg(mSpec);
      }

      mClient.dialogDismissed(this);
      timer.cancel();
      this.dispose();
    }
  }

  public void maintainButtons() {
    double min = -99;
    try {
      min = Double.valueOf(mMinOfIntRange.getText()).doubleValue();
    }
    catch (NumberFormatException ex) {
    }

    double max = -99;
    try {
      max = Double.valueOf(mMaxOfIntRange.getText()).doubleValue();
    }
    catch (NumberFormatException ex) {
    }
    boolean IntOK = mSelIntParam >= 0 && mSelWRTParam >= 0 && (min != -99 && max != -99);

    if (!IntOK) {
      mOKBtn.setEnabled(false);
    }
    else {
      mOKBtn.setEnabled(true);
    }
  }

  public Object getCalculation() {
    return mCalc;
  }

  public Object getSpecification() {
    return mSpec;
  }

  public void generateVariableName() {
    String minStr = "?";
    String maxStr = "?";
    try {
      double min = Double.valueOf(mMinOfIntRange.getText()).doubleValue();
      minStr = String.valueOf(min);

      // remove the decimal point from string
      if (minStr.indexOf('.') >= 0) {
        minStr = minStr.substring(0, minStr.indexOf('.')) + "_" +
            minStr.substring(minStr.indexOf('.') + 1, minStr.length());
      }

    }
    catch (NumberFormatException ex) {
      minStr = "?";
    }

    try {
      double max = Double.valueOf(mMaxOfIntRange.getText()).doubleValue();
      maxStr = String.valueOf(max);

      // remove the decimal point from string
      if (maxStr.indexOf('.') >= 0) {
        maxStr = maxStr.substring(0, maxStr.indexOf('.')) + "_" +
            maxStr.substring(maxStr.indexOf('.') + 1, maxStr.length());
      }
    }
    catch (NumberFormatException ex) {
      maxStr = "?";
    }

    String intParamStr = (String)(mIntegrandParamList.getJList().getSelectedValue());
    String wrtParamStr = (String)(mWRTParamList.getJList().getSelectedValue());

    String searchDirecStr = "";
    int searchMethod = NQueryConstants.SEARCH_TOP_DOWN;
    if (mTopDown.isSelected()) {
      searchMethod = NQueryConstants.SEARCH_TOP_DOWN;
      searchDirecStr = "_td";
    }
    else if (mBottomUp.isSelected()) {
      searchMethod = NQueryConstants.SEARCH_BOTTOM_UP;
      searchDirecStr = "_bu";
    }
    else if (mMixed.isSelected()) {
      searchMethod = NQueryConstants.SEARCH_FROM_BOTTOM_AND_TOP;
      searchDirecStr = "_td_bu";
    }

    if (intParamStr == null) {
      intParamStr = "?";
    }
    if (wrtParamStr == null) {
      wrtParamStr = "?";
    }

    String nameString = intParamStr + "_wrt_" + wrtParamStr + "_between_" + minStr + "_and_" + maxStr + searchDirecStr;
    mNewParamName.setText(nameString);
  }

  public void changedUpdate(DocumentEvent evt) {
    generateVariableName();
  }

  public void insertUpdate(DocumentEvent evt) {
    generateVariableName();
  }

  public void removeUpdate(DocumentEvent evt) {
    generateVariableName();
  }
}

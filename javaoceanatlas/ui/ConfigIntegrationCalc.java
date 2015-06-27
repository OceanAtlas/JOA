/*
 * $Id: ConfigIntegrationCalc.java,v 1.7 2005/09/07 18:49:29 oz Exp $
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
public class ConfigIntegrationCalc extends JOAJDialog implements ListSelectionListener, ActionListener, ButtonMaintainer,
    ItemListener, DocumentListener {
  protected FileViewer mFileViewer;
  // Integration Controls
  protected ParameterChooser mIntegrandParamList;
  protected ParameterChooser mWRTParamList;
  protected int mSelIntParam = -1;
  protected int mSelWRTParam = -1;
  protected JOAJTextField mMinOfIntRange = null;
  protected JOAJTextField mMaxOfIntRange = null;
  protected JCheckBox mUseShallowest = null;
  protected JOAJLabel mUseCustomMax = null;
  protected JCheckBox mUseDeepest = null;
  protected JOAJCheckBox mComputeWeightedMean = null;
  protected JOAJLabel mToleranceLbl = null;
  protected boolean oldIntState = false;
  protected JOAJRadioButton mTopDown = null;
  protected JOAJRadioButton mBottomUp = null;
  protected JOAJRadioButton mMixed = null;
  protected JOAJLabel mUseCustomMin = null;
  protected JOAJLabel integrationDirecLbl = null;
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mCancelButton = null;
  protected JOAJTextField mNewParamName = null;
  protected JOAJTextField mNewParamUnits = null;
  protected JOAJCheckBox mInterpMissing = null;
  protected JOAJTextField mMaxInterpDist = null;
  protected JOAJLabel mMaxInterpDistLbl = null;
	private Timer timer = new Timer();
	private JOAJTextField mNameField = new JOAJTextField(40);

  public ConfigIntegrationCalc(JFrame par, FileViewer fv) {
    super(par, "Station Integration Calculations", false);
    mFileViewer = fv;
    this.init();
  }

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    // create the two parameter chooser lists
    this.getContentPane().setLayout(new BorderLayout(5, 5));

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout(5, 5));

    // Integration
    JPanel integrationPanel = new JPanel();
    integrationPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0)); //(Orientation.LEFT, Orientation.BOTTOM, 5));
    mIntegrandParamList = new ParameterChooser(mFileViewer, new String(b.getString("kIntegrand")), this,
                                               "SALT                ");
    mWRTParamList = new ParameterChooser(mFileViewer, new String(b.getString("kWRT")), this, "SALT                ");
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
    mUseCustomMin = new JOAJLabel(b.getString("kMinimum"));
    mMinOfIntRange = new JOAJTextField(6);
    mMinOfIntRange.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mUseShallowest = new JOAJCheckBox(b.getString("kSurfaceAsMin"), true);
    mMinOfIntRange.getDocument().addDocumentListener(this);
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
    mUseCustomMax = new JOAJLabel(b.getString("kMaximum"));
    mMaxOfIntRange = new JOAJTextField(6);
    mMaxOfIntRange.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mMaxOfIntRange.getDocument().addDocumentListener(this);
    mUseDeepest = new JOAJCheckBox(b.getString("kBottomAsMax"), true);
    line2.add(mUseCustomMax);
    line2.add(mMaxOfIntRange);
    maxCont.add(line2);
    maxCont.add(mUseDeepest);
    rangePanel.add(maxCont);

    JPanel line5 = new JPanel();
    line5.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    mTopDown = new JOAJRadioButton(b.getString("kTopDown"), true);
    mBottomUp = new JOAJRadioButton(b.getString("kBottomUp"));
    mMixed = new JOAJRadioButton(b.getString("kMixedSearch"));
    integrationDirecLbl = new JOAJLabel(b.getString("kIntegrationDirectioncolon"));
    line5.add(integrationDirecLbl);
    line5.add(mTopDown);
    line5.add(mBottomUp);
    line5.add(mMixed);
    ButtonGroup b2 = new ButtonGroup();
    b2.add(mTopDown);
    b2.add(mBottomUp);
    b2.add(mMixed);
    mTopDown.addItemListener(this);
    mBottomUp.addItemListener(this);
    mMixed.addItemListener(this);
    
    mComputeWeightedMean = new JOAJCheckBox(b.getString("kComputeWeightedMean"), true);

    JPanel line5a = new JPanel();
    mInterpMissing = new JOAJCheckBox(b.getString("kInterpMissing"), true);
    mMaxInterpDistLbl = new JOAJLabel(b.getString("kInterpMissingMaxDist"));
    mMaxInterpDist = new JOAJTextField(4);
    mMaxInterpDist.setText("2");
    line5a.add(mInterpMissing);
    line5a.add(mMaxInterpDistLbl);
    line5a.add(mMaxInterpDist);

    rangeCont.add(rangePanel);
    rangeCont.add(mComputeWeightedMean);
    rangeCont.add(line5a);
    rangeCont.add(line5);
    integrationPanel.add(new TenPixelBorder(rangeCont, 0, 0, 0, 0));

    // construct the dialog
    JPanel mainPanelContents = new JPanel();
    mainPanelContents.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
    mainPanelContents.add(new TenPixelBorder(integrationPanel, 5, 5, 5, 5));

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
    genCalcName();
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
    if (evt.getSource() == mIntegrandParamList.getJList()) {
      // get the integrand param
      mSelIntParam = mIntegrandParamList.getJList().getSelectedIndex();
      if (mSelIntParam < 0) {
        return;
      }
      String selParamText = (String)mIntegrandParamList.getJList().getSelectedValue();

      // make sure value of the param is not missing
      int yerrLine = -1;
      double tempYMin = mFileViewer.mAllProperties[mSelIntParam].getPlotMin();
      double tempYMax = mFileViewer.mAllProperties[mSelIntParam].getPlotMax();
      Triplet newRange = JOAFormulas.GetPrettyRange(tempYMin, tempYMax);
      double yInc = newRange.getVal3();
      if (Double.isNaN(yInc)) {
        yerrLine = mSelIntParam;
      }

      if (yerrLine >= 0) {
        //disable the y param
        JFrame f = new JFrame("Parameter Values Missing Error");
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(f,
                                      "All values for " + selParamText + " are missing. " + "\n" + "Select a new parameter");
        mIntegrandParamList.clearSelection();
        mSelIntParam = 0;
      }
    }
    else if (evt.getSource() == mWRTParamList.getJList()) {
      mSelWRTParam = mWRTParamList.getJList().getSelectedIndex();
      if (mSelWRTParam < 0) {
        return;
      }
      String selParamText = (String)mWRTParamList.getJList().getSelectedValue();

      // make sure value of the param is not missing
      int yerrLine = -1;
      double tempYMin = mFileViewer.mAllProperties[mSelWRTParam].getPlotMin();
      double tempYMax = mFileViewer.mAllProperties[mSelWRTParam].getPlotMax();
      Triplet newRange = JOAFormulas.GetPrettyRange(tempYMin, tempYMax);
      double yInc = newRange.getVal3();
      if (Double.isNaN(yInc)) {
        yerrLine = mSelWRTParam;
      }

      if (yerrLine >= 0) {
        //disable the y param
        JFrame f = new JFrame("Parameter Values Missing Error");
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(f,
                                      "All values for " + selParamText + " are missing. " + "\n" + "Select a new parameter");
        mWRTParamList.clearSelection();
        mSelWRTParam = 0;
      }
    }
  }

  public void itemStateChanged(ItemEvent evt) {
  	genCalcName();
  }
  
  private void genCalcName() {
  	String minStr = "?", maxStr = "?";
  	
    try {
      double min = Double.valueOf(mMinOfIntRange.getText()).doubleValue();
      minStr = mMinOfIntRange.getText();
    }
    catch (NumberFormatException ex) {
      minStr = "?";
    }

    try {
      double max = Double.valueOf(mMaxOfIntRange.getText()).doubleValue();
      maxStr = mMaxOfIntRange.getText();
    }
    catch (NumberFormatException ex) {
    	maxStr = "?";
    }

		String searchDirecStr = "";
		
    if (mTopDown.isSelected()) {
			searchDirecStr = "-td";
    }
    else if (mBottomUp.isSelected()) {
			searchDirecStr = "-bu";
    }
    else {
			searchDirecStr = "-mixed";
    }

      String intParam = (String)(mIntegrandParamList.getJList().getSelectedValue());
      if (intParam == null)
      	intParam = "?";
      String wrtParam = (String)(mWRTParamList.getJList().getSelectedValue());
      if (wrtParam == null)
      	wrtParam = "?";
      
		String nameStr = "INT(" + intParam + "-" + wrtParam + "," + minStr + "-" +
    maxStr + ")" + searchDirecStr;
		
		mNameField.setText(nameStr);
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

      int numObs = 2;
      try {
        numObs = Integer.valueOf(mMaxInterpDist.getText()).intValue();
      }
      catch (NumberFormatException ex) {
        numObs = 2;
      }

      int integrandVar = mIntegrandParamList.getJList().getSelectedIndex();
      int wrtVar = mWRTParamList.getJList().getSelectedIndex();

      int searchMethod;
      if (mTopDown.isSelected()) {
        searchMethod = JOAConstants.SEARCH_TOP_DOWN;
      }
      else if (mBottomUp.isSelected()) {
        searchMethod = JOAConstants.SEARCH_BOTTOM_UP;
      }
      else {
        searchMethod = JOAConstants.SEARCH_FROM_BOTTOM_AND_TOP;
      }
      
      Calculation calc = new Calculation(mNameField.getText(),
                                         new IntegrationSpecification(mFileViewer, integrandVar, wrtVar, min, max,
          mUseShallowest.isSelected(), mUseDeepest.isSelected(), mComputeWeightedMean.isSelected(), searchMethod,
          mInterpMissing.isSelected(), numObs), JOAConstants.STN_CALC_TYPE);
      if (mComputeWeightedMean.isSelected()) {
        calc.setUnits(mFileViewer.mAllProperties[integrandVar].getUnits());
      }
      else {
        calc.setUnits("sum");
      }
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
    if (mInterpMissing.isSelected()) {
      if (!mMaxInterpDist.isEnabled()) {
        mMaxInterpDist.setEnabled(true);
        mMaxInterpDistLbl.setEnabled(true);
      }
    }
    else if (!mInterpMissing.isSelected()) {
      if (mMaxInterpDist.isEnabled()) {
        mMaxInterpDist.setEnabled(false);
        mMaxInterpDistLbl.setEnabled(false);
      }
    }

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

package javaoceanatlas.ui;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
/*
 * $Id: ConfigCustomCalc.java,v 1.6 2005/09/07 18:49:30 oz Exp $
 *
 */

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.events.ParameterAddedEvent;
import javaoceanatlas.events.ObsFilterChangedEvent;

@SuppressWarnings("serial")
public class ConfigSORecode extends JOAJDialog implements ListSelectionListener, ActionListener, ButtonMaintainer,
    DocumentListener {
  protected FileViewer mFileViewer;
  protected ParameterChooser mParamList;
  protected int mSelParam = -1;
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mCancelButton = null;

  protected JOAJTextField mLowerLimitField = null;
  protected JOAJTextField mUpperLimitField = null;
  protected JOAJTextField mNewParamName = null;
  protected JOAJTextField mResultValue = null;

  protected JOAJRadioButton mGT = null;
  protected JOAJRadioButton mGTE = null;
  protected JOAJRadioButton mLT = null;
  protected JOAJRadioButton mLTE = null;
  protected JOAJRadioButton mEQ = null;

  static int GT = 0;
  static int GTE = 1;
  static int LT = 2;
  static int LTE = 3;
  static int EQ = 4;

  protected JOAJCheckBox mReverseY = null;
  protected JOAJRadioButton mNewParameter = null;
  protected JOAJRadioButton mRecodeParameter = null;
  protected JOAJComboBox mValuesPopup;
  protected JOAJComboBox mQCPopup;
  protected JLabel mRecodeQC = null;

  private JList mParamJList;
  private String mSelParamText = "";

  public ConfigSORecode(JFrame par, FileViewer fv) {
    super(par, "Recode Values", false);
    mFileViewer = fv;
    this.init();
  }

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    Vector<String> missingVals = new Vector<String>();
    missingVals.add("JOA Missing Value (-99)");
    missingVals.add("EPIC Missing Value (1e35)");
    missingVals.add("WOCE Missing Value (-999)");
    mValuesPopup = new JOAJComboBox(missingVals);
    mValuesPopup.setEditable(true);

    Vector<String> qcVals = new Vector<String>();
    if (mFileViewer.getQCStd() == JOAConstants.WOCE_QC_STD) {
      qcVals.add("1--Analysis not received/Not calib.");
      qcVals.add("2--Acceptable measurement");
      qcVals.add("3--Questionable measurement");
      qcVals.add("4--Bad measurement");
      qcVals.add("5--Not reported");
      qcVals.add("6--Mean of reps/Interp. >2db int.");
      qcVals.add("7--Manual chrom. peak/Despiked");
      qcVals.add("8--Irreg. chrom. peak/Not assigned");
      qcVals.add("9--Sample not drawn/Not sampled");
    }
    else {
      qcVals.add("0--No qc yet assigned");
      qcVals.add("1--Appears to be correct");
      qcVals.add("2--Probably good");
      qcVals.add("3--Probably bad");
      qcVals.add("4--Appears erroneous");
      qcVals.add("5--Has been changed");
      qcVals.add("6--Future use");
      qcVals.add("7--Future use");
      qcVals.add("8--Future use");
      qcVals.add("9--Missing");
    }
    mQCPopup = new JOAJComboBox(qcVals);
    mQCPopup.setEditable(true);
    if (mFileViewer.getQCStd() == JOAConstants.WOCE_QC_STD) {
      mQCPopup.setSelectedIndex(3);
    }
    else {
      mQCPopup.setSelectedIndex(4);
    }
    // create the two parameter chooser lists
    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(5, 5));
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout(5, 5));

    JPanel upperPanel = new JPanel();
    upperPanel.setLayout(new RowLayout(Orientation.LEFT, Orientation.CENTER, 5));
    mParamList = new ParameterChooser(mFileViewer, new String(b.getString("kRecodeParameter1")), this,
                                      "SALT                ", true);
    mParamList.init();
    mParamJList = mParamList.getJList();
    mParamJList.addListSelectionListener(this);

    // param panel containers
    JPanel recodeParamCont = new JPanel();
    recodeParamCont.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

    // recode parameter
    recodeParamCont.add(mParamList);
    upperPanel.add(recodeParamCont);

    // panel for lower limit radio buttons
    JPanel llRadios = new JPanel();
    llRadios.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

    mGT = new JOAJRadioButton(">", true);
    mGTE = new JOAJRadioButton(">=", false);
    mEQ = new JOAJRadioButton("=", false);
    ButtonGroup bg1 = new ButtonGroup();
    bg1.add(mGT);
    bg1.add(mGTE);
    bg1.add(mEQ);
    llRadios.add(mGT);
    llRadios.add(mGTE);
    llRadios.add(mEQ);
    upperPanel.add(llRadios);

    // lower limit text field
    mLowerLimitField = new JOAJTextField(4);
    mLowerLimitField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mLowerLimitField.getDocument().addDocumentListener(this);
    upperPanel.add(mLowerLimitField);

    //label for "AND"
    upperPanel.add(new JLabel("AND"));

    // upper limits radio
    JPanel ulRadios = new JPanel();
    ulRadios.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

    mLT = new JOAJRadioButton("<", true);
    mLTE = new JOAJRadioButton("<=", false);
    ButtonGroup bg2 = new ButtonGroup();
    bg2.add(mLT);
    bg2.add(mLTE);
    ulRadios.add(mLT);
    ulRadios.add(mLTE);
    upperPanel.add(ulRadios);

    // lower limit text field
    mUpperLimitField = new JOAJTextField(4);
    mUpperLimitField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mUpperLimitField.getDocument().addDocumentListener(this);
    upperPanel.add(mUpperLimitField);

    //label for "becomes"
    upperPanel.add(new JLabel("becomes"));

    //  replace value text field
    mResultValue = new JOAJTextField(4);
    mResultValue.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mResultValue.getDocument().addDocumentListener(this);
    upperPanel.add(mValuesPopup);

    // middle panel holds the options
    JPanel middlePanel = new JPanel();
    middlePanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

    // this panel hold radio and text field
    JPanel line1 = new JPanel();
    line1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));

    mNewParameter = new JOAJRadioButton(b.getString("kNewParameterName"), true);
    mRecodeParameter = new JOAJRadioButton(b.getString("kRecodeExistingParameter"), false);
    ButtonGroup bg3 = new ButtonGroup();
    bg3.add(mNewParameter);
    bg3.add(mRecodeParameter);

    mNewParameter.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent evt) {
        maintainButtons();
      }
    });

    mRecodeParameter.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent evt) {
        maintainButtons();
      }
    });

    line1.add(mNewParameter);
    mNewParamName = new JOAJTextField("NP", 5);
    mNewParamName.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line1.add(mNewParamName);
    mReverseY = new JOAJCheckBox(b.getString("kReverseWhenY"));
    line1.add(mReverseY);

    middlePanel.add(line1);

    JPanel line2 = new JPanel();
    line2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    line2.add(mRecodeParameter);
    middlePanel.add(line2);

    JPanel line3 = new JPanel();
    line3.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    mRecodeQC = new JLabel(b.getString("kRecodeQC"));
    line3.add(mRecodeQC);
    line3.add(mQCPopup);
    middlePanel.add(line3);

    // construct the dialog
    mainPanel.add("Center", upperPanel);
    mainPanel.add("South", middlePanel);
    contents.add("Center", new TenPixelBorder(mainPanel, 10, 10, 10, 10));

    // lower panel
    mOKBtn = new JOAJButton(b.getString("kOK"));
    mOKBtn.setActionCommand("ok");
    this.getRootPane().setDefaultButton(mOKBtn);
    mCancelButton = new JOAJButton(b.getString("kCancel"));
    mCancelButton.setActionCommand("cancel");

    JPanel dlgBtnsInset = new JPanel();
    JPanel dlgBtnsPanel = new JPanel();
    dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
    dlgBtnsPanel.setLayout(new GridLayout(1, 3, 15, 1));
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

    contents.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
    this.pack();

    // show dialog at center of screen
    Rectangle dBounds = this.getBounds();
    Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
    int x = sd.width / 2 - dBounds.width / 2;
    int y = sd.height / 2 - dBounds.height / 2;
    this.setLocation(x, y);

    maintainButtons();
  }

  public void valueChanged(ListSelectionEvent evt) {
    if (evt.getSource() == mParamJList) {
      // get the x param
      mSelParam = mParamJList.getSelectedIndex();
      if (mSelParam < 0) {
        return;
      }
      mSelParamText = (String)mParamJList.getSelectedValue();
      maintainButtons();
    }
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      // Recode
      String units = "na";
      int varPos = mFileViewer.getPropertyPos(mSelParamText, false);
      if (varPos >= 0 && mFileViewer.mAllProperties[varPos].getUnits() != null &&
          mFileViewer.mAllProperties[varPos].getUnits().length() > 0) {
        units = mFileViewer.mAllProperties[varPos].getUnits();
      }

      int op1;
      if (mGT.isSelected()) {
        op1 = GT;
      }
      else if (mGTE.isSelected()) {
        op1 = GTE;
      }
      else {
        op1 = EQ;
      }

      int op2;
      if (mLT.isSelected()) {
        op2 = LT;
      }
      else {
        op2 = LTE;
      }

      float lowVal = -99f;
      float highVal = -99f;
      float replVal = -99f;
      try {
        lowVal = Float.valueOf(mLowerLimitField.getText());
      }
      catch (Exception ex) {}

      try {
        highVal = Float.valueOf(mUpperLimitField.getText());
      }
      catch (Exception ex) {}

      try {
        replVal = Float.valueOf(mResultValue.getText());
      }
      catch (Exception ex) {}

      doRecode(mSelParamText, mNewParamName.getText(), units, op1, op2, lowVal, highVal, replVal, mReverseY.isSelected(),
               mNewParameter.isSelected(), (short)mQCPopup.getSelectedIndex());
      this.dispose();
    }
  }

  public void maintainButtons() {
    if (mNewParameter.isSelected()) {
      if (!mNewParamName.isEnabled()) {
        mNewParamName.setEnabled(true);
        mReverseY.setEnabled(true);
      }
    }
    else if (!mNewParameter.isSelected()) {
      if (mNewParamName.isEnabled()) {
        mNewParamName.setEnabled(false);
        mReverseY.setEnabled(false);
      }
    }

    float lowVal = -99f;
    float highVal = -99f;
    try {
      lowVal = Float.valueOf(mLowerLimitField.getText());
    }
    catch (Exception ex) {}

    try {
      highVal = Float.valueOf(mUpperLimitField.getText());
    }
    catch (Exception ex) {}

    if (mSelParamText.length() == 0 || (lowVal == -99 && highVal == -99)) {
      if (mOKBtn.isEnabled()) {
        mOKBtn.setEnabled(false);
      }
    }
    else if (mSelParamText.length() > 0 || (lowVal != -99 || highVal != -99)) {
      if (!mOKBtn.isEnabled()) {
        mOKBtn.setEnabled(true);
      }
    }
  }

  public void changedUpdate(DocumentEvent evt) {
    maintainButtons();
  }

  public void insertUpdate(DocumentEvent evt) {
    maintainButtons();
  }

  public void removeUpdate(DocumentEvent evt) {
    maintainButtons();
  }

  public void doRecode(String varToRecode, String newName, String units, int op1, int op2, float lowVal, float highVal,
                       float replaceVal, boolean isReverseY, boolean makeNewParamMode, short newQC) {
    double min = 99999999;
    double max = -99999999;

    for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = (Section)of.mSections.elementAt(sec);
        if (sech.mNumCasts == 0) {
          continue;
        }
        int varPos = sech.getVarPos(varToRecode, false);

        if (makeNewParamMode) {
          sech.addNewVarToSection(newName, units);
        }

        for (int stc = 0; stc < sech.mStations.size(); stc++) {
          Station sh = (Station)sech.mStations.elementAt(stc);

          // individual observation calculation
          for (int b = 0; b < sh.mNumBottles; b++) {
            Bottle bh = (Bottle)sh.mBottles.elementAt(b);
            float pval = bh.mDValues[varPos];
            short qcval = bh.mQualityFlags[varPos];

            float d = pval;
            short dqc = qcval;
            boolean con1 = true;
            boolean con2 = true;
            if (pval != JOAConstants.MISSINGVALUE) {
              if (lowVal != JOAConstants.MISSINGVALUE) {
                // test lower limit
                if (op1 == GT) {
                  if (pval <= lowVal) {
                    con1 = false;
                  }
                }
                else if (op1 == GTE) {
                  if (pval < lowVal) {
                    con1 = false;
                  }
                }
                else if (op1 == EQ) {
                  if (pval != lowVal) {
                    con1 = false;
                  }
                }
              }

              if (highVal != JOAConstants.MISSINGVALUE) {
                // test upper limit
                if (op2 == LT) {
                  if (pval >= highVal) {
                    con2 = false;
                  }
                }
                else if (op2 == LTE) {
                  if (pval > highVal) {
                    con2 = false;
                  }
                }
              }
            }

            if (con1 && con2) {
              d = replaceVal;
              dqc = newQC;
            }

            if (makeNewParamMode) {
              min = d != replaceVal && d < min ? d : min;
              max = d != replaceVal && d > max ? d : max;

              // add a parameter and new Value to Bottle
              bh.addParamAndValue(d, dqc);
            }
            else {
              bh.mDValues[varPos] = d;
              bh.mQualityFlags[varPos] = newQC;
            }
          }
        }
      }
    }

    if (makeNewParamMode) {
      // mark the calculations as done
      Parameter tempProp = new Parameter(newName, units);
      int newPos = mFileViewer.addNewProperty(newName, units);
      tempProp.setPlotMin(min);
      tempProp.setPlotMax(max);
      tempProp.setReverseY(isReverseY);
      mFileViewer.mAllProperties[newPos] = tempProp;

      // fire a parameter added event
      OpenDataFile oldof = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
      Section oldsech = (Section)oldof.mSections.currElement();
      Station oldsh = (Station)oldsech.mStations.currElement();
      ParameterAddedEvent pae = new ParameterAddedEvent(mFileViewer);
      pae.setFoundObs(oldsech, oldsh);
      Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(pae);
    }
    else {
      ObsFilterChangedEvent ofce = new ObsFilterChangedEvent(mFileViewer);
      Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(ofce);
    }
  }
}

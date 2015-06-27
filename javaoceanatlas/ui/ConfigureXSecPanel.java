/*
 * $Id: ConfigureXSecPanel.java,v 1.4 2005/06/17 18:08:53 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.classicdatamodel.Bottle;
import javaoceanatlas.classicdatamodel.OpenDataFile;
import javaoceanatlas.classicdatamodel.Section;
import javaoceanatlas.classicdatamodel.Station;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class ConfigureXSecPanel extends JOAJDialog implements ActionListener, ItemListener, DialogClient {
  protected ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
  public static int COLOR_BY_DEFAULT = 0;
  public static int COLOR_BY_STN_QUAL = 1;
  public static int COLOR_BY_PARAM_PRESENCE = 2;
  protected FileViewer mFileViewer;
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mCancelButton = null;
  protected JOAJButton mApplyButton = null;
  protected JOAJRadioButton b1 = null;
  protected JOAJRadioButton b2 = null;
  protected JOAJRadioButton b3 = null;
  protected JOAJRadioButton b4 = null;
  protected JOAJRadioButton b5 = null;
  protected JOAJCheckBox mBlackBG = null;
  protected JOAJCheckBox mReverseStns = null;
  protected JOAJRadioButton mColorSymsByDefault = null;
  protected JOAJRadioButton mColorSymsByQC = null;
  protected JOAJRadioButton mColorSymsByMissingParamValues = null;
  protected DialogClient mClient;
  protected int mOffset;
  protected int mOriginalOffset;
  protected boolean mBlackBGFlag;
  protected boolean mReverseStnsFlag;
  protected int mColorByMode;
  protected boolean mOriginalBlackBG;
  protected boolean mOriginalReverseStns;
  protected int mOriginalColorByMode;
  protected int mOriginalSymbolSize;
  protected int mSymbolSize;
  protected JSpinner mSymSize;
  protected JOAJTextField zMin = new JOAJTextField("");
  protected JOAJTextField zMax = new JOAJTextField("");
  JOAJLabel minLabel = new JOAJLabel(b.getString("kMinimum"));
  JOAJLabel maxLabel = new JOAJLabel(b.getString("kMaximum"));
  protected double mMin;
  protected double mMax;
  protected double mOriginalMin;
  protected double mOriginalMax;
  protected TitledBorder zAxisTitledBorder = null;
  protected JOAJComboBox mParam1Popup = null;
  String mOriginalMissingParam = null;

  public ConfigureXSecPanel(FileViewer fv, DialogClient client, int offset, boolean blackBG, boolean reverseStns,
                            int colorByMode, String missingParam, int symbolSize, double min, double max) {
    super(fv, "Configure Cross Section", false);
    mFileViewer = fv;
    mClient = client;
    mOffset = mOriginalOffset = offset;
    mBlackBGFlag = mOriginalBlackBG = blackBG;
    mReverseStnsFlag = mOriginalReverseStns = reverseStns;
    mOriginalColorByMode = mColorByMode = colorByMode;
    mOriginalSymbolSize = mSymbolSize = symbolSize;
    mOriginalMin = mMin = min;
    mOriginalMax = mMax = max;
    if (missingParam != null) {
      mOriginalMissingParam = new String(missingParam);
    }
    this.init();
  }

  public void init() {
    Container contents = this.getContentPane();
    contents.setLayout(new BorderLayout(5, 5));
    JPanel middle = new JPanel();
    middle.setLayout(new BorderLayout(5, 5));

    // offset
    JPanel controls = new JPanel();
    controls.setLayout(new GridLayout(2, 2, 5, 0));
    TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kOffset"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    controls.setBorder(tb);
    b1 = new JOAJRadioButton(b.getString("kSequence"), mOffset == JOAConstants.OFFSET_SEQUENCE);
    b2 = new JOAJRadioButton(b.getString("kDistance"), mOffset == JOAConstants.OFFSET_DISTANCE);
    b3 = new JOAJRadioButton(b.getString("kLatitude"), mOffset == JOAConstants.OFFSET_LATITUDE);
    b4 = new JOAJRadioButton(b.getString("kLongitude"), mOffset == JOAConstants.OFFSET_LONGITUDE);
    b5 = new JOAJRadioButton("Time", mOffset == JOAConstants.OFFSET_TIME);
    controls.add(b1);
    controls.add(b2);
    controls.add(b3);
    controls.add(b4);
    controls.add(b5);
    ButtonGroup bg = new ButtonGroup();
    bg.add(b1);
    bg.add(b2);
    bg.add(b3);
    bg.add(b4);
    bg.add(b5);
    b1.addItemListener(this);
    b2.addItemListener(this);
    b3.addItemListener(this);
    b4.addItemListener(this);
    b5.addItemListener(this);
    middle.add(controls, "North");

    // Options
    JPanel optionsPanel = new JPanel();
    optionsPanel.setLayout(new RowLayout(Orientation.LEFT, Orientation.CENTER, 3));
    tb = BorderFactory.createTitledBorder(b.getString("kOptions"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    optionsPanel.setBorder(tb);

    JPanel leftPanel = new JPanel();
    optionsPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 3));

    mBlackBG = new JOAJCheckBox(b.getString("kBlackBG"), mBlackBGFlag);
    mReverseStns = new JOAJCheckBox(b.getString("kReverseStations"), mReverseStnsFlag);

    // controls for station coloring
    mColorSymsByDefault = new JOAJRadioButton(b.getString("kColorByDefault"), mColorByMode == COLOR_BY_DEFAULT);
    mColorSymsByQC = new JOAJRadioButton(b.getString("kColorByQC"), mColorByMode == COLOR_BY_STN_QUAL);
    mColorSymsByMissingParamValues = new JOAJRadioButton(b.getString("kColorByParamPres"),
        mColorByMode == COLOR_BY_PARAM_PRESENCE);
    ButtonGroup bg2 = new ButtonGroup();
    bg2.add(mColorSymsByDefault);
    bg2.add(mColorSymsByQC);
    bg2.add(mColorSymsByMissingParamValues);
    mColorSymsByDefault.addItemListener(this);
    mColorSymsByQC.addItemListener(this);
    mColorSymsByMissingParamValues.addItemListener(this);

    Vector<String> listItems1 = new Vector<String>();
    for (int i = 0; i < mFileViewer.gNumProperties; i++) {
      listItems1.addElement(mFileViewer.mAllProperties[i].getVarLabel());
    }

    mParam1Popup = new JOAJComboBox(listItems1);
    if (mOriginalMissingParam != null) {
      mParam1Popup.setSelectedItem(mOriginalMissingParam);
    }

    if (mColorByMode == COLOR_BY_DEFAULT || mColorByMode == COLOR_BY_STN_QUAL) {
      mParam1Popup.setEnabled(false);
    }
    else {
      mParam1Popup.setEnabled(true);
    }

    // a panel for the stn coloring options
    JPanel symbolPanelCont = new JPanel();
    symbolPanelCont.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 2));

    JPanel symbolPanel1 = new JPanel();
    JPanel symbolPanel2 = new JPanel();
    JPanel symbolPanel3 = new JPanel();
    symbolPanel1.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
    symbolPanel2.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
    symbolPanel3.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
    symbolPanel1.add(mColorSymsByDefault);
    symbolPanel2.add(mColorSymsByQC);
    symbolPanel3.add(mColorSymsByMissingParamValues);
    symbolPanel3.add(mParam1Popup);
    symbolPanelCont.add(symbolPanel1);
    symbolPanelCont.add(symbolPanel2);
    symbolPanelCont.add(symbolPanel3);

    leftPanel.add(mBlackBG);
    leftPanel.add(mReverseStns);

    //add symbol size controls
    JPanel symPanel = new JPanel();
    symPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    symPanel.add(new JOAJLabel(b.getString("kSymbolSize")));
    SpinnerNumberModel model = new SpinnerNumberModel(mSymbolSize, 1, 5, 1);
    mSymSize = new JSpinner(model);
    symPanel.add(mSymSize);
    symPanel.add(new JOAJLabel("(pixels)"));
    leftPanel.add(symPanel);

    optionsPanel.add(leftPanel);
    optionsPanel.add(symbolPanelCont);

    middle.add(optionsPanel, "Center");

    // x axis #1 detail
    JPanel line1 = new JPanel();
    line1.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line1.add(minLabel);
    zMin = new JOAJTextField(6);
    zMin.setText(JOAFormulas.formatDouble(String.valueOf(mMin), 3, false));
    zMin.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line1.add(zMin);

    JPanel line2 = new JPanel();
    line2.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line2.add(maxLabel);
    zMax = new JOAJTextField(6);
    zMax.setText(JOAFormulas.formatDouble(String.valueOf(mMax), 3, false));
    zMax.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line2.add(zMax);

    JPanel zAxisContCont = new JPanel();
    zAxisContCont.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    zAxisTitledBorder = BorderFactory.createTitledBorder(b.getString("kZAxisRange"));
    if (JOAConstants.ISMAC) {
      //zAxisTitledBorder.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    zAxisContCont.setBorder(zAxisTitledBorder);

    JPanel zAxisCont = new JPanel();
    zAxisCont.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 2));
    zAxisCont.add(line1);
    zAxisCont.add(line2);
    zAxisContCont.add(zAxisCont);

    JOAJButton mUseDataMaxMin = new JOAJButton(b.getString("kUseDataMaxMin"));
    mUseDataMaxMin.setActionCommand("minmax");
    mUseDataMaxMin.addActionListener(this);
    zAxisContCont.add(mUseDataMaxMin);

    middle.add(zAxisContCont, "South");

    // lower panel
    mOKBtn = new JOAJButton(b.getString("kOK"));
    mOKBtn.setActionCommand("ok");
    this.getRootPane().setDefaultButton(mOKBtn);
    mCancelButton = new JOAJButton(b.getString("kCancel"));
    mCancelButton.setActionCommand("cancel");
    mApplyButton = new JOAJButton(b.getString("kApply"));
    mApplyButton.setActionCommand("apply");
    JPanel dlgBtnsInset = new JPanel();
    JPanel dlgBtnsPanel = new JPanel();
    dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
    dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
    if (JOAConstants.ISMAC) {
      dlgBtnsPanel.add(mCancelButton);
      dlgBtnsPanel.add(mApplyButton);
      dlgBtnsPanel.add(mOKBtn);
    }
    else {
      dlgBtnsPanel.add(mOKBtn);
      dlgBtnsPanel.add(mApplyButton);
      dlgBtnsPanel.add(mCancelButton);
    }
    dlgBtnsInset.add(dlgBtnsPanel);

    mOKBtn.addActionListener(this);
    mCancelButton.addActionListener(this);
    mApplyButton.addActionListener(this);

    contents.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
    contents.add("Center", new TenPixelBorder(middle, 5, 5, 5, 5));
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      //mClient.dialogCancelled(this);
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      mClient.dialogDismissed(this);
      this.dispose();
    }
    else if (cmd.equals("apply")) {
      mClient.dialogApply(this);
      mOriginalBlackBG = getBlackBG();
      mOriginalReverseStns = getReverseStns();
      mOriginalOffset = mOffset;
      mOriginalColorByMode = mColorByMode;
      mOriginalSymbolSize = mSymbolSize;
      mOriginalMissingParam = this.getMissingParam();
    }

    if (cmd.equals("minmax")) {
      // set the ranges to the max min for that parameter
      computeDepthRangeFromData();
      zMin.setText(JOAFormulas.formatDouble(String.valueOf(0), 1, false));
      zMax.setText(JOAFormulas.formatDouble(String.valueOf(mMax), 3, false));
    }
  }

  public void computeDepthRangeFromData() {
    mMax = JOAConstants.MISSINGVALUE;
    for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = (Section)of.mSections.elementAt(sec);
        if (sech.mNumCasts == 0) {
          continue;
        }

        for (int stc = 0; stc < sech.mStations.size(); stc++) {
          Station sh = (Station)sech.mStations.elementAt(stc);
          if (!sh.mUseStn) {
            continue;
          }

          // look to see if bottom is recorded
          if (sh.mBottomDepthInDBARS != JOAConstants.MISSINGVALUE) {
            double bottom = sh.mBottomDepthInDBARS;
            mMax = bottom > mMax ? bottom : mMax;
          }
        }
      }
    }

    if (mMax == JOAConstants.MISSINGVALUE || mMax <= 0.0) {
      // look for maximum pressure
      double presMax = 0.0;
      for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
        OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

        for (int sec = 0; sec < of.mNumSections; sec++) {
          Section sech = (Section)of.mSections.elementAt(sec);
          if (sech.mNumCasts == 0) {
            continue;
          }

          int pPos = sech.getPRESVarPos();
          for (int stc = 0; stc < sech.mStations.size(); stc++) {
            Station sh = (Station)sech.mStations.elementAt(stc);
            for (int b = 0; b < sh.mNumBottles; b++) {
              Bottle bh = (Bottle)sh.mBottles.elementAt(b);
              double val = bh.mDValues[pPos];
              if (val != JOAConstants.MISSINGVALUE) {
                if (val > presMax) {
                  presMax = val;
                }
              }
            }
          }
        }

      }
      mMax = presMax;
    }
  }

  public double getZMax() throws NumberFormatException {
    double out;
    try {
      out = Double.valueOf(zMax.getText()).doubleValue();
    }
    catch (NumberFormatException ex) {
      throw ex;
    }
    return out;
  }

  public double getZMin() throws NumberFormatException {
    double out;
    try {
      out = Double.valueOf(zMin.getText()).doubleValue();
    }
    catch (NumberFormatException ex) {
      throw ex;
    }
    return out;
  }

  public double getOriginalZMax() throws NumberFormatException {
    return mOriginalMax;
  }

  public double getOriginalZMin() throws NumberFormatException {
    return mOriginalMin;
  }

  public boolean getBlackBG() {
    return mBlackBG.isSelected();
  }

  public boolean getOriginalBlackBG() {
    return mOriginalBlackBG;
  }

  public int getColorByMode() {
    if (mColorSymsByDefault.isSelected()) {
      return COLOR_BY_DEFAULT;
    }
    else if (mColorSymsByQC.isSelected()) {
      return COLOR_BY_STN_QUAL;
    }
    else if (mColorSymsByMissingParamValues.isSelected()) {
      return COLOR_BY_PARAM_PRESENCE;
    }
    return COLOR_BY_DEFAULT;
  }

  public int getOriginalColorByMode() {
    return mOriginalColorByMode;
  }

  public String getMissingParam() {
    return (String)mParam1Popup.getSelectedItem();
  }

  public String getOriginalMissingParam() {
    return mOriginalMissingParam;
  }

  public boolean getReverseStns() {
    return mReverseStns.isSelected();
  }

  public boolean getOriginalReverseStns() {
    return mOriginalReverseStns;
  }

  public int getDistanceOffset() {
    return mOffset;
  }

  public int getOriginalDistanceOffset() {
    return mOriginalOffset;
  }

  public int getSymbolSize() {
    return ((Integer)mSymSize.getValue()).intValue();
  }

  public int getOriginalSymbolSize() {
    return mOriginalSymbolSize;
  }

  public void itemStateChanged(ItemEvent evt) {
    if (evt.getSource() instanceof JOAJRadioButton) {
      JOAJRadioButton rb = (JOAJRadioButton)evt.getSource();
      if (rb == b1 && evt.getStateChange() == ItemEvent.SELECTED) {
        mOffset = JOAConstants.OFFSET_SEQUENCE;
      }
      if (rb == b2 && evt.getStateChange() == ItemEvent.SELECTED) {
        mOffset = JOAConstants.OFFSET_DISTANCE;
      }
      if (rb == b3 && evt.getStateChange() == ItemEvent.SELECTED) {
        mOffset = JOAConstants.OFFSET_LATITUDE;
      }
      if (rb == b4 && evt.getStateChange() == ItemEvent.SELECTED) {
        mOffset = JOAConstants.OFFSET_LONGITUDE;
      }
      if (rb == b5 && evt.getStateChange() == ItemEvent.SELECTED) {
        mOffset = JOAConstants.OFFSET_TIME;
      }
      if (rb == mColorSymsByDefault && evt.getStateChange() == ItemEvent.SELECTED) {
        mParam1Popup.setSelectedIndex(0);
        mParam1Popup.setEnabled(false);
      }
      if (rb == mColorSymsByQC && evt.getStateChange() == ItemEvent.SELECTED) {
        mParam1Popup.setSelectedIndex(0);
        mParam1Popup.setEnabled(false);
      }
      if (rb == mColorSymsByMissingParamValues && evt.getStateChange() == ItemEvent.SELECTED) {
        mParam1Popup.setEnabled(true);
      }
    }
    else if (evt.getSource() instanceof JOAJCheckBox) {
      if (evt.getStateChange() == ItemEvent.SELECTED) {
      }
    }
  }

  // OK Button
  public void dialogDismissed(JDialog f) {
  }

  // Cancel button
  public void dialogCancelled(JDialog f) {
  }

  // something other than the OK button
  public void dialogDismissedTwo(JDialog f) {
  }

  // Apply button, OK w/o dismissing the dialog
  public void dialogApply(JDialog f) {
  }

  public void dialogApplyTwo(Object d) {
    mSymbolSize = ((Integer)mSymSize.getValue()).intValue();
  }
}

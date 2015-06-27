/*
 * $Id: ConfigureObsFilter.java,v 1.5 2005/09/07 18:49:31 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import java.io.*;
import javax.swing.border.*;
import org.w3c.dom.*;
import com.ibm.xml.parser.*;
import org.xml.sax.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.events.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.filters.*;
import gov.noaa.pmel.swing.*;

@SuppressWarnings("serial")
public class ConfigureObsFilter extends JOAJDialog implements ActionListener, ButtonMaintainer, ItemListener,
    ParameterAddedListener {
  protected FileViewer mFileViewer;
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mApplyButton = null;
  protected JOAJButton mCancelButton = null;
  protected JOAJButton mNoneButton = null;
  protected JOAJButton mSaveButton = null;
  protected JOAJButton mClearAllButton = null;
  protected JOAJTextField mCrit1MinField = null;
  protected JOAJTextField mCrit2MinField = null;
  protected JOAJTextField mCrit3MinField = null;
  protected JOAJTextField mCrit4MinField = null;
  protected JOAJTextField mCrit1MaxField = null;
  protected JOAJTextField mCrit2MaxField = null;
  protected JOAJTextField mCrit3MaxField = null;
  protected JOAJTextField mCrit4MaxField = null;
  protected JOAJComboBox mParam1Popup = null;
  protected JOAJComboBox mParam2Popup = null;
  protected JOAJComboBox mParam3Popup = null;
  protected JOAJComboBox mParam4Popup = null;
  protected JOAJComboBox mQC1Popup = null;
  protected JOAJComboBox mQC2Popup = null;
  protected JOAJComboBox mQC3Popup = null;
  protected JOAJComboBox mQC4Popup = null;
  protected JOAJRadioButton mCrit1IsAnd = null;
  protected JOAJRadioButton mCrit2IsAnd = null;
  protected JOAJRadioButton mCrit1Crit2IsAnd = null;
  protected JOAJRadioButton mCrit1IsOr = null;
  protected JOAJRadioButton mCrit2IsOr = null;
  protected JOAJRadioButton mCrit1Crit2IsOr = null;
  protected JOAJRadioButton mShowOnly = null;
  protected JOAJRadioButton mHighlight = null;
  protected JOAJRadioButton mEnlargeCurrSymbol = null;
  protected JOAJCheckBox mContrastColor = null;
  protected JOAJRadioButton mUseNewSymbol = null;
  protected ObservationFilter mObsFilter = null;
  protected int mCurrIndex1, mCurrIndex2, mCurrIndex3, mCurrIndex4;
  protected int mCurrPopUpIndex1, mCurrPopUpIndex2, mCurrPopUpIndex3, mCurrPopUpIndex4;
  protected boolean mCrit1IsQC = false, mCrit2IsQC = false, mCrit3IsQC = false, mCrit4IsQC = false;
  protected boolean mIgnoreEvent = false;
  protected JOAJComboBox mSymbolPopup = null;
  protected int mCurrSymbol = JOAConstants.SYMBOL_SQUAREFILLED;
  protected Icon[] symbolData = null;
  protected JSpinner mSizeField = null;
	private Timer timer = new Timer();
  protected JOAJLabel c1Lbl = new JOAJLabel("< =");
  protected JOAJLabel c2Lbl = new JOAJLabel("< =");
  protected JOAJLabel c3Lbl = new JOAJLabel("< =");
  protected JOAJLabel c4Lbl = new JOAJLabel("< =");
  protected boolean isStnQual = false;
  protected boolean isBottleQual = false;
  protected boolean[] isObsQual = null;
  protected boolean hasObsQual = false;
  protected int[] paramIndices = null;
  protected Swatch mContrastSwatch = null;
  protected int mQCStandard;
  protected String[] IGOSSList = null;
  protected String[] WOCEList = null;
  protected Vector<String> listItems1 = null;
  protected Vector<String> listItems2 = null;
  protected Vector<String> listItems3 = null;
  protected Vector<String> listItems4 = null;
  protected JOAJComboBox mSavedFiltersPopup = null;
  protected SmallIconButton mClearCriterion1 = new SmallIconButton(new ImageIcon(getClass().getResource(
      "images/trash.gif")));
  protected SmallIconButton mClearCriterion2 = new SmallIconButton(new ImageIcon(getClass().getResource(
      "images/trash.gif")));
  protected SmallIconButton mClearCriterion3 = new SmallIconButton(new ImageIcon(getClass().getResource(
      "images/trash.gif")));
  protected SmallIconButton mClearCriterion4 = new SmallIconButton(new ImageIcon(getClass().getResource(
      "images/trash.gif")));

  public ConfigureObsFilter(JOAWindow par, FileViewer fv, ObservationFilter obs) {
    super(par, "Observation Filter", false);
		
    mClearCriterion1.setToolTipText("Remove this filter criterion");
    mClearCriterion2.setToolTipText("Remove this filter criterion");
    mClearCriterion3.setToolTipText("Remove this filter criterion");
    mClearCriterion4.setToolTipText("Remove this filter criterion");
		
    mFileViewer = fv;
    if (obs == null) {
      mObsFilter = new ObservationFilter();
      mObsFilter.setNumCriteria(0);
      for (int i = 0; i < 4; i++) {
        mObsFilter.setMinVal(i, -1e10);
        mObsFilter.setMaxVal(i, 1e10);
        mObsFilter.setParamIndex(i, 0);
        mObsFilter.setPopupIndex(i, 0);
      }
      mObsFilter.setCriteria1IsAnd(true);
      mObsFilter.setCriteria2IsAnd(true);
      mObsFilter.setCrit1AndCrit2IsAnd(true);
      mObsFilter.setShowOnlyMatching(true);
      mObsFilter.setEnlargeSymbol(true);
      mObsFilter.setSymbolSize(4);
      mObsFilter.setSymbol(JOAConstants.SYMBOL_SQUAREFILLED);
      mObsFilter.setUseContrastingColor(false);
      mObsFilter.setContrastingColor(Color.red);
      mCurrIndex1 = 0;
      mCurrIndex2 = 0;
      mCurrIndex3 = 0;
      mCurrIndex4 = 0;
      mCurrPopUpIndex1 = 0;
      mCurrPopUpIndex2 = 0;
      mCurrPopUpIndex2 = 0;
      mCurrPopUpIndex3 = 0;
      mCrit1IsQC = false;
      mCrit2IsQC = false;
      mCrit3IsQC = false;
      mCrit4IsQC = false;
      mQCStandard = JOAConstants.IGOSS_QC_STD;
    }
    else {
      mObsFilter = obs;
      mCurrIndex1 = mObsFilter.getPopupIndex(0);
      mCurrIndex2 = mObsFilter.getPopupIndex(1);
      mCurrIndex3 = mObsFilter.getPopupIndex(2);
      mCurrIndex4 = mObsFilter.getPopupIndex(3);
    }
    this.init();
  }

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    symbolData = new Icon[] {new ImageIcon(getClass().getResource("images/sym_square.gif")),
        new ImageIcon(getClass().getResource("images/sym_squarefilled.gif")),
        new ImageIcon(getClass().getResource("images/sym_circle.gif")),
        new ImageIcon(getClass().getResource("images/sym_circlefilled.gif")),
        new ImageIcon(getClass().getResource("images/sym_diamond.gif")),
        new ImageIcon(getClass().getResource("images/sym_diamondfilled.gif")),
        new ImageIcon(getClass().getResource("images/sym_triangle.gif")),
        new ImageIcon(getClass().getResource("images/sym_trianglefilled.gif")),
        new ImageIcon(getClass().getResource("images/sym_cross1.gif")),
        new ImageIcon(getClass().getResource("images/sym_cross2.gif"))
    };

    IGOSSList = new String[] {new String("1 (appears correct)"),
        new String("2 (probably good)"), new String("3 (probably bad)"), new String("4 (appears eroneous)") // added for Argo data
    };

    WOCEList = new String[] {new String("2 (acceptable value)"),
        new String("3 (questionable value)"), new String("4 (bad value)")
    };

    // check whether there is any quality code information in the data collection
    determineQCParams();

    mSymbolPopup = new JOAJComboBox();
    for (int i = 0; i < symbolData.length; i++) {
      mSymbolPopup.addItem(symbolData[i]);
    }
    mSymbolPopup.setSelectedIndex(mObsFilter.getSymbol() - 1);

    mParam1Popup = new JOAJComboBox();
    mParam2Popup = new JOAJComboBox();
    mParam3Popup = new JOAJComboBox();
    mParam4Popup = new JOAJComboBox();
    mParam1Popup.addItemListener(this);
    mParam2Popup.addItemListener(this);
    mParam3Popup.addItemListener(this);
    mParam4Popup.addItemListener(this);

    paramIndices = new int[2 * mFileViewer.gNumProperties];
    int pCount = 0;
    listItems1 = new Vector<String>();
    listItems2 = new Vector<String>();
    listItems3 = new Vector<String>();
    listItems4 = new Vector<String>();
    for (int i = 0; i < mFileViewer.gNumProperties; i++) {
      listItems1.addElement(mFileViewer.mAllProperties[i].getVarLabel());
      listItems2.addElement(mFileViewer.mAllProperties[i].getVarLabel());
      listItems3.addElement(mFileViewer.mAllProperties[i].getVarLabel());
      listItems4.addElement(mFileViewer.mAllProperties[i].getVarLabel());
      paramIndices[pCount++] = i;

      if (isObsQual[i]) {
        paramIndices[pCount++] = i;
        listItems1.addElement(mFileViewer.mAllProperties[i].getVarLabel() + " QC");
        listItems2.addElement(mFileViewer.mAllProperties[i].getVarLabel() + " QC");
        listItems3.addElement(mFileViewer.mAllProperties[i].getVarLabel() + " QC");
        listItems4.addElement(mFileViewer.mAllProperties[i].getVarLabel() + " QC");
      }
    }

    for (int i = 0; i < listItems1.size(); i++) {
      String s = (String)listItems1.elementAt(i);
      mIgnoreEvent = true;
      mParam1Popup.addItem(s);
      mIgnoreEvent = true;
      mParam2Popup.addItem(s);
      mIgnoreEvent = true;
      mParam3Popup.addItem(s);
      mIgnoreEvent = true;
      mParam4Popup.addItem(s);
    }

    // create the QC popups
    mQC1Popup = new JOAJComboBox();
    mQC2Popup = new JOAJComboBox();
    mQC3Popup = new JOAJComboBox();
    mQC4Popup = new JOAJComboBox();

    OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
    Section sech = (Section)of.mSections.currElement();
    mQCStandard = sech.getQCStandard();
    setQCPopUpQCStandard();
    mQC1Popup.addItemListener(this);
    mQC2Popup.addItemListener(this);
    mQC3Popup.addItemListener(this);
    mQC4Popup.addItemListener(this);

    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(5, 5));

    JPanel criteriaCont = new JPanel();
    criteriaCont.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 2));

    JPanel crit1Panel = new JPanel();
    crit1Panel.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 2)); //new GridLayout(3, 1, 2, 2));
    JPanel crit1AndOr2Panel = new JPanel();
    crit1AndOr2Panel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
    JPanel crit2Panel = new JPanel();
    crit2Panel.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 2));

    // criterion #1, condition #1
    JPanel crit1Line1Panel = new JPanel();
    crit1Line1Panel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
    mCrit1MinField = new JOAJTextField(7);
    if (Math.abs(mObsFilter.getMinVal(0)) < 1e10) {
      mCrit1MinField.setText(JOAFormulas.formatDouble(String.valueOf(mObsFilter.getMinVal(0)), 3, false));
    }
    mCrit1MinField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    crit1Line1Panel.add(mCrit1MinField);
    crit1Line1Panel.add(new JOAJLabel("< ="));
    crit1Line1Panel.add(mParam1Popup);

    mParam1Popup.setSelectedIndex(mCurrIndex1);
    //if (mCurrIndex1 > 0) {
    //mParam1Popup.ensureIndexIsVisible(JOAFormulas.correctListOffset(mCurrIndex1));
    //}
    crit1Line1Panel.add(c1Lbl);

    String maxVal = null;
    if (Math.abs(mObsFilter.getMaxVal(0)) == 1e10) {
      maxVal = "";
    }
    else if (mObsFilter.isCriteria1ActiveQC()) {
      maxVal = String.valueOf((int)(mObsFilter.getMaxVal(0)));
    }
    else {
      maxVal = JOAFormulas.formatDouble(String.valueOf(mObsFilter.getMaxVal(0)), 3, false);
    }

    mCrit1MaxField = new JOAJTextField(7);
    mCrit1MaxField.setText(maxVal);
    mCrit1MaxField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    crit1Line1Panel.add(mCrit1MaxField);
    // add the IGOSS popup and make invisible
    if (hasObsQual) {
      crit1Line1Panel.add(mQC1Popup);
    }

    // add the clear button
    crit1Line1Panel.add(mClearCriterion1);

    crit1Panel.add(crit1Line1Panel);

    JPanel crit1Line2Panel = new JPanel();
    crit1Line2Panel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
    ButtonGroup bg1 = new ButtonGroup();
    mCrit1IsAnd = new JOAJRadioButton(b.getString("kAnd"), mObsFilter.isCriteria1IsAnd());
    mCrit1IsOr = new JOAJRadioButton(b.getString("kOr"), !mObsFilter.isCriteria1IsAnd());
    bg1.add(mCrit1IsAnd);
    bg1.add(mCrit1IsOr);
    crit1Line2Panel.add(mCrit1IsAnd);
    crit1Line2Panel.add(mCrit1IsOr);
    crit1Panel.add(crit1Line2Panel);

    // criterion #1, condition #2
    JPanel crit1Line3Panel = new JPanel();
    crit1Line3Panel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
    mCrit2MinField = new JOAJTextField(7);
    if (Math.abs(mObsFilter.getMinVal(1)) < 1e10) {
      mCrit2MinField.setText(JOAFormulas.formatDouble(String.valueOf(mObsFilter.getMinVal(1)), 3, false));
    }
    mCrit2MinField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

    crit1Line3Panel.add(mCrit2MinField);
    crit1Line3Panel.add(new JOAJLabel("< ="));
    crit1Line3Panel.add(mParam2Popup);
    mParam2Popup.setSelectedIndex(mCurrIndex2);
    crit1Line3Panel.add(c2Lbl);

    if (Math.abs(mObsFilter.getMaxVal(1)) == 1e10) {
      maxVal = "";
    }
    else if (mObsFilter.isCriteria2ActiveQC()) {
      maxVal = String.valueOf((int)(mObsFilter.getMaxVal(1)));
    }
    else {
      maxVal = JOAFormulas.formatDouble(String.valueOf(mObsFilter.getMaxVal(1)), 3, false);
    }

    mCrit2MaxField = new JOAJTextField(7);
    mCrit2MaxField.setText(maxVal);
    mCrit2MaxField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    crit1Line3Panel.add(mCrit2MaxField);

    // add the IGOSS popup and make invisible
    if (hasObsQual) {
      crit1Line3Panel.add(mQC2Popup);
    }

    // add the clear button
    crit1Line3Panel.add(mClearCriterion2);

    crit1Panel.add(crit1Line3Panel);
    TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kCondition1"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    crit1Panel.setBorder(tb);
    criteriaCont.add(crit1Panel);

    // And OR for the two conditions
    ButtonGroup bg3 = new ButtonGroup();
    mCrit1Crit2IsAnd = new JOAJRadioButton(b.getString("kAnd"), mObsFilter.isCrit1AndCrit2IsAnd());
    mCrit1Crit2IsOr = new JOAJRadioButton(b.getString("kOr"), !mObsFilter.isCrit1AndCrit2IsAnd());
    bg3.add(mCrit1Crit2IsAnd);
    bg3.add(mCrit1Crit2IsOr);
    crit1AndOr2Panel.add(mCrit1Crit2IsAnd);
    crit1AndOr2Panel.add(mCrit1Crit2IsOr);
    criteriaCont.add(crit1AndOr2Panel);

    // criterion #2, condition 1
    JPanel crit2Line1Panel = new JPanel();
    crit2Line1Panel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
    mCrit3MinField = new JOAJTextField(7);
    if (Math.abs(mObsFilter.getMinVal(2)) < 1e10) {
      mCrit3MinField.setText(JOAFormulas.formatDouble(String.valueOf(mObsFilter.getMinVal(2)), 3, false));
    }
    mCrit3MinField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

    crit2Line1Panel.add(mCrit3MinField);
    crit2Line1Panel.add(new JOAJLabel("< ="));
    crit2Line1Panel.add(mParam3Popup);
    mParam3Popup.setSelectedIndex(mCurrIndex3);
    crit2Line1Panel.add(c3Lbl);

    if (Math.abs(mObsFilter.getMaxVal(2)) == 1e10) {
      maxVal = "";
    }
    else if (mObsFilter.isCriteria3ActiveQC()) {
      maxVal = String.valueOf((int)(mObsFilter.getMaxVal(2)));
    }
    else {
      maxVal = JOAFormulas.formatDouble(String.valueOf(mObsFilter.getMaxVal(2)), 3, false);
    }

    mCrit3MaxField = new JOAJTextField(7);
    mCrit3MaxField.setText(maxVal);
    mCrit3MaxField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    crit2Line1Panel.add(mCrit3MaxField);

    // add the IGOSS popup and make invisible
    if (hasObsQual) {
      crit2Line1Panel.add(mQC3Popup);
    }

    // add the clear button
    crit2Line1Panel.add(mClearCriterion3);

    crit2Panel.add(crit2Line1Panel);

    JPanel crit2Line2Panel = new JPanel();
    crit2Line2Panel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
    ButtonGroup bg2 = new ButtonGroup();
    mCrit2IsAnd = new JOAJRadioButton(b.getString("kAnd"), mObsFilter.isCriteria2IsAnd());
    mCrit2IsOr = new JOAJRadioButton(b.getString("kOr"), !mObsFilter.isCriteria2IsAnd());
    bg2.add(mCrit2IsAnd);
    bg2.add(mCrit2IsOr);
    crit2Line2Panel.add(mCrit2IsAnd);
    crit2Line2Panel.add(mCrit2IsOr);
    crit2Panel.add(crit2Line2Panel);

    JPanel crit2Line3Panel = new JPanel();
    crit2Line3Panel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
    mCrit4MinField = new JOAJTextField(7);
    if (Math.abs(mObsFilter.getMinVal(3)) < 1e10) {
      mCrit4MinField.setText(JOAFormulas.formatDouble(String.valueOf(mObsFilter.getMinVal(3)), 3, false));
    }
    mCrit4MinField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    crit2Line3Panel.add(mCrit4MinField);
    crit2Line3Panel.add(new JOAJLabel("< ="));
    crit2Line3Panel.add(mParam4Popup);
    mParam4Popup.setSelectedIndex(mCurrIndex4);
    crit2Line3Panel.add(c4Lbl);

    if (Math.abs(mObsFilter.getMaxVal(3)) == 1e10) {
      maxVal = "";
    }
    else if (mObsFilter.isCriteria4ActiveQC()) {
      maxVal = String.valueOf((int)(mObsFilter.getMaxVal(3)));
    }
    else {
      maxVal = JOAFormulas.formatDouble(String.valueOf(mObsFilter.getMaxVal(3)), 3, false);
    }

    mCrit4MaxField = new JOAJTextField(7);
    mCrit4MaxField.setText(maxVal);
    mCrit4MaxField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    crit2Line3Panel.add(mCrit4MaxField);

    // add the IGOSS popup
    if (hasObsQual) {
      crit2Line3Panel.add(mQC4Popup);
    }

    // add the clear button
    crit2Line3Panel.add(mClearCriterion4);

    crit2Panel.add(crit2Line3Panel);
    tb = BorderFactory.createTitledBorder(b.getString("kCondition2"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    crit2Panel.setBorder(tb);
    criteriaCont.add(crit2Panel);

    // disable the QC popups
    if (!mObsFilter.isCriteria1ActiveQC()) {
      disableQCPopUp1();
    }
    if (!mObsFilter.isCriteria2ActiveQC()) {
      disableQCPopUp2();
    }
    if (!mObsFilter.isCriteria3ActiveQC()) {
      disableQCPopUp3();
    }
    if (!mObsFilter.isCriteria4ActiveQC()) {
      disableQCPopUp4();
    }

    // display options
    JPanel displayOptPanel = new JPanel();
    displayOptPanel.setLayout(new GridLayout(5, 1, 10, 0));
    tb = BorderFactory.createTitledBorder(b.getString("kDisplayOptions"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    displayOptPanel.setBorder(tb);
    ButtonGroup bg4 = new ButtonGroup();
    mShowOnly = new JOAJRadioButton(b.getString("kShowMatching"), mObsFilter.isShowOnlyMatching());
    mHighlight = new JOAJRadioButton(b.getString("kHighlight"), !mObsFilter.isShowOnlyMatching());
    bg4.add(mShowOnly);
    bg4.add(mHighlight);
    displayOptPanel.add(mShowOnly);
    mShowOnly.addItemListener(this);
    displayOptPanel.add(mHighlight);
    mHighlight.addItemListener(this);
    tb = BorderFactory.createTitledBorder(b.getString("kDisplayOptions"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    displayOptPanel.setBorder(tb);
    ButtonGroup bg5 = new ButtonGroup();
    mEnlargeCurrSymbol = new JOAJRadioButton(b.getString("kEnlargeSymbol"), mObsFilter.isEnlargeSymbol());
    mUseNewSymbol = new JOAJRadioButton(b.getString("kUseNewSymbols"), !mObsFilter.isEnlargeSymbol());
    mContrastColor = new JOAJCheckBox(b.getString("kContrastColor"), mObsFilter.isUseContrastingColor());
    mEnlargeCurrSymbol.addItemListener(this);
    mUseNewSymbol.addItemListener(this);
    mContrastColor.addItemListener(this);
    bg5.add(mEnlargeCurrSymbol);
    bg5.add(mUseNewSymbol);

    // enlarge symbol
    JPanel enlargeSymPanel = new JPanel();
    enlargeSymPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
    enlargeSymPanel.add(new JOAJLabel("     "));
    enlargeSymPanel.add(mEnlargeCurrSymbol);

    // new symbol
    JPanel newSymPanel = new JPanel();
    newSymPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
    newSymPanel.add(new JOAJLabel("     "));
    newSymPanel.add(mUseNewSymbol);
    newSymPanel.add(mSymbolPopup);
    mSymbolPopup.addItemListener(this);
    newSymPanel.add(new JOAJLabel(b.getString("kSize")));

    SpinnerNumberModel model2 = new SpinnerNumberModel(mObsFilter.getSymbolSize(), 1, 100, 1);
    mSizeField = new JSpinner(model2);

    newSymPanel.add(mSizeField);

    // contrast color
    JPanel contrastPanel = new JPanel();
    contrastPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
    contrastPanel.add(new JOAJLabel("     "));
    contrastPanel.add(mContrastColor);
    mContrastSwatch = new Swatch(mObsFilter.getContrastingColor());
    contrastPanel.add(mContrastSwatch);

    displayOptPanel.add(enlargeSymPanel);
    displayOptPanel.add(newSymPanel);
    displayOptPanel.add(contrastPanel);
    criteriaCont.add(displayOptPanel);

    if (mObsFilter.isShowOnlyMatching()) {
      disableHighlightOptions();
    }
    else {
      enableHighlightOptions();

      if (mObsFilter.isEnlargeSymbol()) {
        disableSymbolStuff();
      }
      else {
        enableSymbolStuff();
      }

      if (mObsFilter.isUseContrastingColor()) {
        enableContrastStuff();
      }
      else {
        disableContrastStuff();
      }
    }

    // lower panel
    mNoneButton = new JOAJButton(b.getString("kRemove"));
    mNoneButton.setActionCommand("none");
    mOKBtn = new JOAJButton(b.getString("kOK"));
    mOKBtn.setActionCommand("ok");
    this.getRootPane().setDefaultButton(mOKBtn);
    mCancelButton = new JOAJButton(b.getString("kClose"));
    mCancelButton.setActionCommand("cancel");
    mApplyButton = new JOAJButton(b.getString("kApply"));
    mApplyButton.setActionCommand("apply");
    mSaveButton = new JOAJButton(b.getString("kSave"));
    mSaveButton.setActionCommand("save");
    mClearAllButton = new JOAJButton(b.getString("kClearAll"));
    mClearAllButton.setActionCommand("clearall");
    Vector<String> obsFilters = JOAFormulas.getObsFilterList();
    mSavedFiltersPopup = new JOAJComboBox(obsFilters);
    if (obsFilters.size() == 0) {
      mSavedFiltersPopup.setEnabled(false);
    }
    mSavedFiltersPopup.addItemListener(this);
    JPanel dlgBtnsInset = new JPanel();
    JPanel dlgBtnsPanel = new JPanel();
    dlgBtnsInset.setLayout(new BorderLayout(5, 5));
    dlgBtnsPanel.setLayout(new GridLayout(1, 5, 5, 5));
    JPanel slPanel = new JPanel();
    slPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
    slPanel.add(mSaveButton);
    slPanel.add(new JOAJLabel(b.getString("kSelectFilter")));
    slPanel.add(mSavedFiltersPopup);
    if (JOAConstants.ISMAC) {
      dlgBtnsPanel.add(mCancelButton);
      dlgBtnsPanel.add(mClearAllButton);
      dlgBtnsPanel.add(mNoneButton);
      dlgBtnsPanel.add(mApplyButton);
      dlgBtnsPanel.add(mOKBtn);
    }
    else {
      dlgBtnsPanel.add(mOKBtn);
      dlgBtnsPanel.add(mApplyButton);
      dlgBtnsPanel.add(mNoneButton);
      dlgBtnsPanel.add(mClearAllButton);
      dlgBtnsPanel.add(mCancelButton);
    }
    dlgBtnsInset.add(slPanel, "North");
    JPanel c = new JPanel();
    c.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
    c.add(dlgBtnsPanel);
    dlgBtnsInset.add(c, "South");

    mOKBtn.addActionListener(this);
    mApplyButton.addActionListener(this);
    mCancelButton.addActionListener(this);
    mNoneButton.addActionListener(this);
    mSaveButton.addActionListener(this);
    mClearAllButton.addActionListener(this);
    contents.add(new TenPixelBorder(criteriaCont, 5, 5, 5, 5), "Center");
    contents.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
    mClearCriterion1.addActionListener(this);
    mClearCriterion2.addActionListener(this);
    mClearCriterion3.addActionListener(this);
    mClearCriterion4.addActionListener(this);

    mClearCriterion1.addActionListener(this);
    mClearCriterion1.setActionCommand("clearcrit1");
    mClearCriterion2.addActionListener(this);
    mClearCriterion2.setActionCommand("clearcrit2");
    mClearCriterion3.addActionListener(this);
    mClearCriterion3.setActionCommand("clearcrit3");
    mClearCriterion4.addActionListener(this);
    mClearCriterion4.setActionCommand("clearcrit4");

    mFileViewer.addParameterAddedListener(this);
    runTimer();
    this.pack();

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

  public void determineQCParams() {
    isStnQual = false;
    isBottleQual = false;
    isObsQual = null;
    isObsQual = new boolean[mFileViewer.gNumProperties];
    hasObsQual = false;
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

          if (sh.mVarFlag != JOAConstants.MISSINGVALUE) {
            isStnQual = true;
          }

          for (int bb = 0; bb < sh.mNumBottles; bb++) {
            Bottle bh = (Bottle)sh.mBottles.elementAt(bb);

            if (bh.mQualityFlag != JOAConstants.MISSINGVALUE) {
              isBottleQual = true;
            }

            for (int i = 0; i < mFileViewer.gNumProperties && !isObsQual[i]; i++) {
              if (mFileViewer.mAllProperties[i].getVarLabel().equalsIgnoreCase("PRES")) {
                continue;
              }
              int vPos = sech.getVarPos(mFileViewer.mAllProperties[i].getVarLabel(), false);
              if (vPos == -1) {
                continue;
              }
              if (bh.mQualityFlags[vPos] != JOAConstants.MISSINGVALUE) {
                isObsQual[i] = true;
                hasObsQual = true;
              }
            }
          }
        }
      }
    }
  }

  public void parameterAdded(ParameterAddedEvent evt) {
    mIgnoreEvent = true;

    // check whether there is any quality code information in the data collection
    determineQCParams();
    paramIndices = null;
    paramIndices = new int[2 * mFileViewer.gNumProperties];

    mParam1Popup.removeAll();
    mParam2Popup.removeAll();
    mParam3Popup.removeAll();
    mParam4Popup.removeAll();
    int pCount = 0;
    Vector<String> listItems1 = new Vector<String>();
    Vector<String> listItems2 = new Vector<String>();
    Vector<String> listItems3 = new Vector<String>();
    Vector<String> listItems4 = new Vector<String>();
    for (int i = 0; i < mFileViewer.gNumProperties; i++) {
      listItems1.addElement(mFileViewer.mAllProperties[i].getVarLabel());
      listItems2.addElement(mFileViewer.mAllProperties[i].getVarLabel());
      listItems3.addElement(mFileViewer.mAllProperties[i].getVarLabel());
      listItems4.addElement(mFileViewer.mAllProperties[i].getVarLabel());
      paramIndices[pCount++] = i;

      if (isObsQual[i]) {
        paramIndices[pCount++] = i;
        listItems1.addElement(mFileViewer.mAllProperties[i].getVarLabel() + " QC");
        listItems2.addElement(mFileViewer.mAllProperties[i].getVarLabel() + " QC");
        listItems3.addElement(mFileViewer.mAllProperties[i].getVarLabel() + " QC");
        listItems4.addElement(mFileViewer.mAllProperties[i].getVarLabel() + " QC");
      }
    }

    for (int i = 0; i < listItems1.size(); i++) {
      String s = (String)listItems1.elementAt(i);
      mIgnoreEvent = true;
      mParam1Popup.addItem(s);
      mIgnoreEvent = true;
      mParam2Popup.addItem(s);
      mIgnoreEvent = true;
      mParam3Popup.addItem(s);
      mIgnoreEvent = true;
      mParam4Popup.addItem(s);

    }

    //mParam1Popup.setListData(listItems1);
    mIgnoreEvent = true;
    mParam1Popup.setSelectedIndex(mCurrIndex1);
    mIgnoreEvent = true;
    mParam2Popup.setSelectedIndex(mCurrIndex2);
    mIgnoreEvent = true;
    mParam3Popup.setSelectedIndex(mCurrIndex3);
    mIgnoreEvent = true;
    mParam4Popup.setSelectedIndex(mCurrIndex4);
  }

  public void itemStateChanged(ItemEvent evt) {
    if (mIgnoreEvent) {
      mIgnoreEvent = false;
      return;
    }
    if (evt.getSource() instanceof JOAJComboBox) {
      JOAJComboBox cb = (JOAJComboBox)evt.getSource();
      int state = evt.getStateChange();
      if (cb == mParam1Popup && state == ItemEvent.SELECTED) {
        mCurrIndex1 = paramIndices[cb.getSelectedIndex()];
        mCurrPopUpIndex1 = cb.getSelectedIndex();

        String selText = (String)mParam1Popup.getSelectedItem();
        if (selText.indexOf("QC") >= 0) {
          mCrit1IsQC = true;
          enableQCPopUp1();
        }
        else {
          mCrit1IsQC = false;
          disableQCPopUp1();
        }
      }
      else if (cb == mParam2Popup && state == ItemEvent.SELECTED) {
        mCurrIndex2 = paramIndices[cb.getSelectedIndex()];
        mCurrPopUpIndex2 = cb.getSelectedIndex();

        String selText = (String)mParam2Popup.getSelectedItem();
        if (selText.indexOf("QC") >= 0) {
          mCrit2IsQC = true;
          enableQCPopUp2();
        }
        else {
          mCrit2IsQC = false;
          disableQCPopUp2();
        }
      }
      else if (cb == mParam3Popup && state == ItemEvent.SELECTED) {
        mCurrIndex3 = paramIndices[cb.getSelectedIndex()];
        mCurrPopUpIndex3 = cb.getSelectedIndex();
        String selText = (String)mParam3Popup.getSelectedItem();
        if (selText.indexOf("QC") >= 0) {
          mCrit3IsQC = true;
          enableQCPopUp3();
        }
        else {
          mCrit3IsQC = false;
          disableQCPopUp3();
        }
      }
      else if (cb == mParam4Popup && state == ItemEvent.SELECTED) {
        mCurrIndex4 = paramIndices[cb.getSelectedIndex()];
        mCurrPopUpIndex4 = cb.getSelectedIndex();
        String selText = (String)mParam4Popup.getSelectedItem();
        if (selText.indexOf("QC") >= 0) {
          mCrit4IsQC = true;
          enableQCPopUp4();
        }
        else {
          mCrit4IsQC = false;
          disableQCPopUp4();
        }
      }
      else if (cb == mQC1Popup) {
        int qcCode = cb.getSelectedIndex();
        if (mQCStandard == JOAConstants.WOCE_QC_STD) {
          qcCode++;
        }
        mCrit1MaxField.setText(String.valueOf(qcCode+1));
      }
      else if (cb == mQC2Popup) {
        int qcCode = cb.getSelectedIndex();
        if (mQCStandard == JOAConstants.WOCE_QC_STD) {
          qcCode++;
        }
        mCrit2MaxField.setText(String.valueOf(qcCode+1));
      }
      else if (cb == mQC3Popup) {
        int qcCode = cb.getSelectedIndex();
        if (mQCStandard == JOAConstants.WOCE_QC_STD) {
          qcCode++;
        }
        mCrit3MaxField.setText(String.valueOf(qcCode+1));
      }
      else if (cb == mQC4Popup) {
        int qcCode = cb.getSelectedIndex();
        if (mQCStandard == JOAConstants.WOCE_QC_STD) {
          qcCode++;
        }
        mCrit4MaxField.setText(String.valueOf(qcCode+1));
      }
      else if (cb == mSymbolPopup) {
        mCurrSymbol = cb.getSelectedIndex() + 1;
      }
      else if (cb == mSavedFiltersPopup) {
        /*if (mObsFilter != null && (mObsFilter.mCriteria1Active || mObsFilter.mCriteria2Active ||
            mObsFilter.mCriteria3Active || mObsFilter.mCriteria4Active)) {
         // already have active filter--overwrite?
         Object[] options = { "OK", "CANCEL" };
               int response = JOptionPane.showOptionDialog(this, "An observation filter is already active. " +
                            "\n" +
                            "Overwrite existing filter settings?",
                            "Warning", JOptionPane.DEFAULT_OPTION, 	JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
               if (response != JOptionPane.YES_OPTION)
          return;
             }*/

        String fileToRead = (String)cb.getSelectedItem();
        if (fileToRead == null || fileToRead.equalsIgnoreCase("Select Filter")) {
          return;
        }

        // get the filter file
        File filterFile = null;
        try {
          filterFile = JOAFormulas.getSupportFile(fileToRead);
        }
        catch (IOException ex) {
          JFrame ff = new JFrame("Observation Filter Read Error");
          Toolkit.getDefaultToolkit().beep();
          JOptionPane.showMessageDialog(ff,
                                        "An error occurred reading the observation filter settings. " + "\n" + "File Not Found");
          return;
        }

        try {
          this.getFilter(filterFile);
          if (mObsFilter != null && !updateFilter()) {
            mFileViewer.updateObsFilter(null);
            mFileViewer.updateObsFilter(mObsFilter);
          }
        }
        catch (Exception ex) {
          JFrame ff = new JFrame("Observation Filter Read Error");
          Toolkit.getDefaultToolkit().beep();
          JOptionPane.showMessageDialog(ff,
                                        "An error occurred reading the observation filter settings. " + "\n" + "File was probably not an XML settings file.");
        }

      }
    }
    else if (evt.getSource() instanceof JOAJRadioButton) {
      JOAJRadioButton rb = (JOAJRadioButton)evt.getSource();
      if (evt.getStateChange() == ItemEvent.SELECTED && rb == mShowOnly) {
        disableHighlightOptions();
        disableSymbolStuff();
        disableContrastOptions();
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mHighlight) {
        enableHighlightOptions();
        enableContrastOptions();

        if (mEnlargeCurrSymbol.isSelected()) {
          disableSymbolStuff();
        }
        else if (mUseNewSymbol.isSelected()) {
          enableSymbolStuff();
        }
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mEnlargeCurrSymbol) {
        disableSymbolStuff();
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mUseNewSymbol) {
        enableSymbolStuff();
      }
    }
  }

  public void enableQCPopUp1() {
    if (mQC1Popup == null) {
      return;
    }
    mQC1Popup.setEnabled(true);
    mCrit1MinField.setText("");
    mCrit1MinField.setEnabled(false);
    c1Lbl.setText(" is ");
  }

  public void disableQCPopUp1() {
    if (mQC1Popup == null) {
      return;
    }
    mQC1Popup.setEnabled(false);
    mCrit1MinField.setEnabled(true);
    c1Lbl.setText("< =");
  }

  public void enableQCPopUp2() {
    if (mQC2Popup == null) {
      return;
    }
    mQC2Popup.setEnabled(true);
    mCrit2MinField.setText("");
    mCrit2MinField.setEnabled(false);
    c2Lbl.setText(" is ");
  }

  public void disableQCPopUp2() {
    if (mQC2Popup == null) {
      return;
    }
    mQC2Popup.setEnabled(false);
    mCrit2MinField.setEnabled(true);
    c2Lbl.setText("< =");
  }

  public void enableQCPopUp3() {
    if (mQC3Popup == null) {
      return;
    }
    mQC3Popup.setEnabled(true);
    mCrit3MinField.setText("");
    mCrit3MinField.setEnabled(false);
    c3Lbl.setText(" is ");
  }

  public void disableQCPopUp3() {
    if (mQC3Popup == null) {
      return;
    }
    mQC3Popup.setEnabled(false);
    mCrit3MinField.setEnabled(true);
    c3Lbl.setText("< =");
  }

  public void enableQCPopUp4() {
    if (mQC4Popup == null) {
      return;
    }
    mQC4Popup.setEnabled(true);
    mCrit4MinField.setText("");
    mCrit4MinField.setEnabled(false);
    c4Lbl.setText(" is ");
  }

  public void disableQCPopUp4() {
    if (mQC4Popup == null) {
      return;
    }
    mQC4Popup.setEnabled(false);
    mCrit4MinField.setEnabled(true);
    c4Lbl.setText("< = ");
  }

  public void enableSymbolStuff() {
    mSymbolPopup.setEnabled(true);
    mSizeField.setEnabled(true);
    mSymbolPopup.invalidate();
    mSizeField.invalidate();
    this.validate();
  }

  public void disableSymbolStuff() {
    mSymbolPopup.setEnabled(false);
    mSizeField.setEnabled(false);
    mSymbolPopup.invalidate();
    mSizeField.invalidate();
    this.validate();
  }

  public void enableHighlightOptions() {
    mEnlargeCurrSymbol.setEnabled(true);
    mUseNewSymbol.setEnabled(true);
    enableSymbolStuff();
    enableContrastOptions();
    enableContrastStuff();
    this.validate();
  }

  public void disableHighlightOptions() {
    mEnlargeCurrSymbol.setEnabled(false);
    mUseNewSymbol.setEnabled(false);
    disableSymbolStuff();
    disableContrastOptions();
    disableContrastStuff();
    this.validate();
  }

  public void enableContrastOptions() {
    mContrastColor.setEnabled(true);
    this.validate();
  }

  public void disableContrastOptions() {
    mContrastColor.setEnabled(false);
    this.validate();
  }

  public void enableContrastStuff() {
    mContrastSwatch.setEnabled(true);
    this.validate();
  }

  public void disableContrastStuff() {
    mContrastSwatch.setEnabled(false);
    this.validate();
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      timer.cancel();
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      if (mObsFilter != null && !updateFilter()) {
        mFileViewer.updateObsFilter(mObsFilter);
      }
      timer.cancel();
      this.dispose();
    }
    else if (cmd.equals("apply")) {
      if (mObsFilter != null && !updateFilter()) {
        mFileViewer.updateObsFilter(mObsFilter);
      }
    }
    else if (cmd.equals("clearall")) {
      clearCriterion1();
      clearCriterion2();
      clearCriterion3();
      clearCriterion4();
      mCrit1IsAnd.setSelected(true);
      mCrit2IsAnd.setSelected(true);
      mCrit1Crit2IsAnd.setSelected(true);
      mShowOnly.setSelected(true);
      mEnlargeCurrSymbol.setSelected(true);
      mContrastColor.setSelected(false);
    }
    else if (cmd.equals("none")) {
      // empty out the obs filter and then tell the fileviewer
      mFileViewer.updateObsFilter(null);
      updateFilter();
    }
    else if (cmd.equals("save")) {
      save();
      mSavedFiltersPopup.removeAllItems();
      Vector<String> obsFilters = JOAFormulas.getObsFilterList();
      for (int i = 0; i < obsFilters.size(); i++) {
        mSavedFiltersPopup.addItem((String)obsFilters.elementAt(i));
      }
      if (obsFilters.size() == 0) {
        mSavedFiltersPopup.setEnabled(false);
      }
    }
    else if (cmd.equals("clearcrit1")) {
      mClearCriterion1.setSelected(false);
      clearCriterion1();
    }
    else if (cmd.equals("clearcrit2")) {
      mClearCriterion2.setSelected(false);
      clearCriterion2();
    }
    else if (cmd.equals("clearcrit3")) {
      mClearCriterion3.setSelected(false);
      clearCriterion3();
    }
    else if (cmd.equals("clearcrit4")) {
      mClearCriterion4.setSelected(false);
      clearCriterion4();
    }
  }

  public void save() {
    // get a filename
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        if (name.endsWith("xml")) {
          return true;
        }
        else {
          return false;
        }
      }
    };
    Frame fr = new Frame();
    String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
    FileDialog f = new FileDialog(fr, "Save filter settings as:", FileDialog.SAVE);
    f.setDirectory(directory);
    f.setFilenameFilter(filter);
    f.setFile("untitled_obsf.xml");
    f.setVisible(true);
    directory = f.getDirectory();
    String fs = f.getFile();
    f.dispose();
    if (directory != null && fs != null) {
      File nf = new File(directory, fs);
      try {
        saveFilter(nf);
      }
      catch (Exception ex) {
        ex.printStackTrace();
        JFrame ff = new JFrame("Observation Filter Save Error");
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(ff, "An error occurred saving the observation filter settings");
      }
    }
  }

  public void saveFilter(File file) {
    // save filter as XML
    try {
      // update the observation filter
      updateFilter();

      // create a documentobject
      Document doc = (Document)Class.forName("com.ibm.xml.parser.TXDocument").newInstance();

      // make joaobsfilter the root element
      Element root = doc.createElement("joaobsfilter");

      // set the QC attribute of the tag
      if (mQCStandard == JOAConstants.IGOSS_QC_STD) {
        root.setAttribute("qcstd", "IGOSS");
      }
      else {
        root.setAttribute("qcstd", "WOCE");
      }

      // make criteria element--all individual criterions fit into here
      Element criteriaitem = doc.createElement("criteria");

      if (mObsFilter.isCriteria1Active()) {
        // make first criterion element and add it
        Element crititem = doc.createElement("criterion1");
        if (mObsFilter.isCriteria1ActiveQC()) {
          crititem.setAttribute("isqc", "TRUE");
        }
        else {
          crititem.setAttribute("isqc", "FALSE");
        }

        // add the criterion values
        Element item = doc.createElement("param");
        item.appendChild(doc.createTextNode(mObsFilter.getParam(0)));
        crititem.appendChild(item);

        item = doc.createElement("minval");
        item.appendChild(doc.createTextNode(String.valueOf(mObsFilter.getMinVal(0))));
        crititem.appendChild(item);

        item = doc.createElement("maxval");
        item.appendChild(doc.createTextNode(String.valueOf(mObsFilter.getMaxVal(0))));
        crititem.appendChild(item);
        criteriaitem.appendChild(crititem);
      }

      if (mObsFilter.isCriteria2Active()) {
        // make a boolean item first
        Element item = doc.createElement("crit1isand");
        item.appendChild(doc.createTextNode(String.valueOf(mObsFilter.isCriteria1IsAnd())));
        criteriaitem.appendChild(item);

        // make first criterion element and add it
        Element crititem = doc.createElement("criterion2");
        if (mObsFilter.isCriteria2ActiveQC()) {
          crititem.setAttribute("isqc", "TRUE");
        }
        else {
          crititem.setAttribute("isqc", "FALSE");
        }

        // add the criterion values
        item = doc.createElement("param");
        item.appendChild(doc.createTextNode(mObsFilter.getParam(1)));
        crititem.appendChild(item);

        item = doc.createElement("minval");
        item.appendChild(doc.createTextNode(String.valueOf(mObsFilter.getMinVal(1))));
        crititem.appendChild(item);

        item = doc.createElement("maxval");
        item.appendChild(doc.createTextNode(String.valueOf(mObsFilter.getMaxVal(1))));
        crititem.appendChild(item);
        criteriaitem.appendChild(crititem);
      }

      if (mObsFilter.isCriteria3Active() || mObsFilter.isCriteria4Active()) {
        // write the boolean between groups
        Element item = doc.createElement("crit1and2isand");
        item.appendChild(doc.createTextNode(String.valueOf(mObsFilter.isCrit1AndCrit2IsAnd())));
        criteriaitem.appendChild(item);
      }

      if (mObsFilter.isCriteria3Active()) {
        // make first criterion element and add it
        Element crititem = doc.createElement("criterion3");
        if (mObsFilter.isCriteria3ActiveQC()) {
          crititem.setAttribute("isqc", "TRUE");
        }
        else {
          crititem.setAttribute("isqc", "FALSE");
        }

        // add the criterion values
        Element item = doc.createElement("param");
        item.appendChild(doc.createTextNode(mObsFilter.getParam(2)));
        crititem.appendChild(item);

        item = doc.createElement("minval");
        item.appendChild(doc.createTextNode(String.valueOf(mObsFilter.getMinVal(2))));
        crititem.appendChild(item);

        item = doc.createElement("maxval");
        item.appendChild(doc.createTextNode(String.valueOf(mObsFilter.getMaxVal(2))));
        crititem.appendChild(item);
        criteriaitem.appendChild(crititem);
      }

      if (mObsFilter.isCriteria4Active()) {
        // make a boolean item first
        Element item = doc.createElement("crit2isand");
        item.appendChild(doc.createTextNode(String.valueOf(mObsFilter.isCriteria2IsAnd())));
        criteriaitem.appendChild(item);

        // make first criterion element and add it
        Element crititem = doc.createElement("criterion4");
        if (mObsFilter.isCriteria4ActiveQC()) {
          crititem.setAttribute("isqc", "TRUE");
        }
        else {
          crititem.setAttribute("isqc", "FALSE");
        }

        // add the criterion values
        item = doc.createElement("param");
        item.appendChild(doc.createTextNode(mObsFilter.getParam(3)));
        crititem.appendChild(item);

        item = doc.createElement("minval");
        item.appendChild(doc.createTextNode(String.valueOf(mObsFilter.getMinVal(3))));
        crititem.appendChild(item);

        item = doc.createElement("maxval");
        item.appendChild(doc.createTextNode(String.valueOf(mObsFilter.getMaxVal(3))));
        crititem.appendChild(item);
        criteriaitem.appendChild(crititem);
      }

      root.appendChild(criteriaitem);

      // add the display options
      if (!mObsFilter.isShowOnlyMatching()) {
        if (!mObsFilter.isEnlargeSymbol()) {
          Element eitem = doc.createElement("contrastingsymboloptions");
          Element item = doc.createElement("symbol");
          item.appendChild(doc.createTextNode(String.valueOf(mObsFilter.getSymbol())));
          eitem.appendChild(item);
          item = doc.createElement("symbolsize");
          item.appendChild(doc.createTextNode(String.valueOf(mObsFilter.getSymbolSize())));
          eitem.appendChild(item);
          root.appendChild(eitem);
        }
        else {
          // write enlarge symbol tag
          Element item = doc.createElement("enlargesymbol");
          root.appendChild(item);
        }

        // add highlight options
        if (mObsFilter.isUseContrastingColor()) {
          Element item = doc.createElement("contrastingcolor");
          item.setAttribute("red", String.valueOf(mObsFilter.getContrastingColor().getRed()));
          item.setAttribute("green", String.valueOf(mObsFilter.getContrastingColor().getGreen()));
          item.setAttribute("blue", String.valueOf(mObsFilter.getContrastingColor().getBlue()));
          root.appendChild(item);
        }
      }

      doc.appendChild(root);
      ((TXDocument)doc).setVersion("1.0");
      FileWriter fr = new FileWriter(file);
      ((TXDocument)doc).printWithFormat(fr);
      fr.flush();
      fr.close();
    }
    catch (Exception ex) {
      ex.printStackTrace();
      JFrame f = new JFrame("Observation Filter Save Error");
      Toolkit.getDefaultToolkit().beep();
      JOptionPane.showMessageDialog(f, "An error occurred saving the observation filter settings");
    }
  }

  public void readSettings() {
    // get a filename
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        if (name.endsWith("xml")) {
          return true;
        }
        else {
          return false;
        }
      }
    };
    Frame fr = new Frame();
    String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
    FileDialog f = new FileDialog(fr, "Read filter settings from:", FileDialog.LOAD);
    f.setDirectory(directory);
    f.setFilenameFilter(filter);
    f.setVisible(true);
    directory = f.getDirectory();
    String fs = f.getFile();
    f.dispose();
    if (directory != null && fs != null) {
      File nf = new File(directory, fs);
      try {
        this.getFilter(nf);
      }
      catch (Exception ex) {
        JFrame ff = new JFrame("Observation Filter Read Error");
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(ff,
                                      "An error occurred reading the observation filter settings. " + "\n" + "File was probably not an XML settings file.");
      }
    }
  }

  public boolean updateFilter() {
    boolean error = false;
    String fldText1 = new String();
    fldText1 = mCrit1MinField.getText();
    String fldText2 = new String();
    fldText2 = mCrit1MaxField.getText();

    mObsFilter.setCriteria1Active(false);
    mObsFilter.setCriteria2Active(false);
    mObsFilter.setCriteria3Active(false);
    mObsFilter.setCriteria4Active(false);
    mObsFilter.setCriteria1ActiveQC(mCrit1IsQC);
    mObsFilter.setCriteria2ActiveQC(mCrit2IsQC);
    mObsFilter.setCriteria3ActiveQC(mCrit3IsQC);
    mObsFilter.setCriteria4ActiveQC(mCrit4IsQC);
    for (int i = 0; i < 4; i++) {
      mObsFilter.setMinVal(i, -1e10);
      mObsFilter.setMaxVal(i, 1e10);
      mObsFilter.setParamIndex(i, 0);
    }
    if (fldText1.length() > 0) {
      try {
        mObsFilter.setMinVal(0, Double.valueOf(mCrit1MinField.getText()).doubleValue());
        mObsFilter.setCriteria1Active(true);
        mObsFilter.setParamIndex(0, mCurrIndex1);
        mObsFilter.setPopupIndex(0, mCurrPopUpIndex1);
        mObsFilter.setParam(0, (String)mParam1Popup.getSelectedItem());
      }
      catch (NumberFormatException ex) {
        error = true;
        mCrit1MinField.setText("err");
      }
    }
    if (fldText2.length() > 0) {
      try {
        mObsFilter.setMaxVal(0, Double.valueOf(mCrit1MaxField.getText()).doubleValue());
        mObsFilter.setCriteria1Active(true);
        mObsFilter.setParamIndex(0, mCurrIndex1);
        mObsFilter.setPopupIndex(0, mCurrPopUpIndex1);
        mObsFilter.setParam(0, (String)mParam1Popup.getSelectedItem());
      }
      catch (NumberFormatException ex) {
        error = true;
        mCrit1MaxField.setText("err");
      }
    }

    fldText1 = mCrit2MinField.getText();
    fldText2 = mCrit2MaxField.getText();
    if (fldText1.length() > 0) {
      try {
        mObsFilter.setMinVal(1, Double.valueOf(mCrit2MinField.getText()).doubleValue());
        mObsFilter.setCriteria2Active(true);
        mObsFilter.setParamIndex(1, mCurrIndex2);
        mObsFilter.setPopupIndex(1, mCurrPopUpIndex2);
        mObsFilter.setParam(1, (String)mParam2Popup.getSelectedItem());
      }
      catch (NumberFormatException ex) {
        error = true;
        mCrit2MinField.setText("err");
      }
    }
    if (fldText2.length() > 0) {
      try {
        mObsFilter.setMaxVal(1, Double.valueOf(mCrit2MaxField.getText()).doubleValue());
        mObsFilter.setCriteria2Active(true);
        mObsFilter.setParamIndex(1, mCurrIndex2);
        mObsFilter.setPopupIndex(1, mCurrPopUpIndex2);
        mObsFilter.setParam(1, (String)mParam2Popup.getSelectedItem());
      }
      catch (NumberFormatException ex) {
        error = true;
        mCrit2MaxField.setText("err");
      }
    }

    fldText1 = mCrit3MinField.getText();
    fldText2 = mCrit3MaxField.getText();
    if (fldText1.length() > 0) {
      try {
        mObsFilter.setMinVal(2, Double.valueOf(mCrit3MinField.getText()).doubleValue());
        mObsFilter.setCriteria3Active(true);
        mObsFilter.setParamIndex(2, mCurrIndex3);
        mObsFilter.setPopupIndex(2, mCurrPopUpIndex3);
        mObsFilter.setParam(2, (String)mParam3Popup.getSelectedItem());
      }
      catch (NumberFormatException ex) {
        error = true;
        mCrit3MinField.setText("err");
      }
    }
    if (fldText2.length() > 0) {
      try {
        mObsFilter.setMaxVal(2, Double.valueOf(mCrit3MaxField.getText()).doubleValue());
        mObsFilter.setCriteria3Active(true);
        mObsFilter.setParamIndex(2, paramIndices[mCurrIndex3]);
        mObsFilter.setPopupIndex(2, mCurrPopUpIndex3);
        mObsFilter.setParam(2, (String)mParam3Popup.getSelectedItem());
      }
      catch (NumberFormatException ex) {
        error = true;
        mCrit3MaxField.setText("err");
      }
    }

    fldText1 = mCrit4MinField.getText();
    fldText2 = mCrit4MaxField.getText();
    if (fldText1.length() > 0) {
      try {
        mObsFilter.setMinVal(3, Double.valueOf(mCrit4MinField.getText()).doubleValue());
        mObsFilter.setCriteria4Active(true);
        mObsFilter.setParamIndex(3, mCurrIndex4);
        mObsFilter.setPopupIndex(3, mCurrPopUpIndex4);
        mObsFilter.setParam(3, (String)mParam4Popup.getSelectedItem());
      }
      catch (NumberFormatException ex) {
        error = true;
        mCrit4MinField.setText("err");
      }
    }
    if (fldText2.length() > 0) {
      try {
        mObsFilter.setMaxVal(3, Double.valueOf(mCrit4MaxField.getText()).doubleValue());
        mObsFilter.setCriteria4Active(true);
        mObsFilter.setParamIndex(3, mCurrIndex4);
        mObsFilter.setPopupIndex(3, mCurrPopUpIndex4);
        mObsFilter.setParam(3, (String)mParam4Popup.getSelectedItem());
      }
      catch (NumberFormatException ex) {
        error = true;
        mCrit4MaxField.setText("err");
      }
    }

    mObsFilter.setCriteria1IsAnd(mCrit1IsAnd.isSelected());
    mObsFilter.setCriteria2IsAnd(mCrit2IsAnd.isSelected());
    mObsFilter.setCrit1AndCrit2IsAnd(mCrit1Crit2IsAnd.isSelected());
    mObsFilter.setShowOnlyMatching(mShowOnly.isSelected());
    mObsFilter.setEnlargeSymbol(mEnlargeCurrSymbol.isSelected());
    mObsFilter.setUseContrastingColor(mContrastColor.isSelected());
    mObsFilter.setContrastingColor(mContrastSwatch.getColor());
    mObsFilter.setSymbol(mCurrSymbol);
    mObsFilter.setQCStandard(mQCStandard);
    mObsFilter.setSymbolSize(((Integer)mSizeField.getValue()).intValue());
    return error;
  }

  public void maintainButtons() {
    if (mFileViewer.mObsFilterActive) {
      mNoneButton.setEnabled(true);
    }
    else {
      mNoneButton.setEnabled(false);
    }
  }

  public void clearUI() {
    clearCriterion1();
    clearCriterion2();
    clearCriterion3();
    clearCriterion4();
    mCrit1IsAnd.setSelected(true);
    mCrit2IsAnd.setSelected(true);
    mCrit1Crit2IsAnd.setSelected(true);
    disableHighlightOptions();
  }

  private void clearCriterion1() {
    mIgnoreEvent = true;
    mParam1Popup.setSelectedIndex(0);
    //mParam1Popup.ensureIndexIsVisible(0);
    if (mQCStandard == JOAConstants.IGOSS_QC_STD || mQCStandard == JOAConstants.WOCE_QC_STD) {
      mQC1Popup.setSelectedIndex(0);
    }
    mQC1Popup.setEnabled(false);
    mCrit1MinField.setText("");
    mCrit1MaxField.setText("");
    mCrit1MinField.setEnabled(true);
    mCrit1MaxField.setEnabled(true);
  }

  private void clearCriterion2() {
    mIgnoreEvent = true;
    mParam2Popup.setSelectedIndex(0);
    if (mQCStandard == JOAConstants.IGOSS_QC_STD || mQCStandard == JOAConstants.WOCE_QC_STD) {
      mQC2Popup.setSelectedIndex(0);
    }
    mQC2Popup.setEnabled(false);
    mCrit2MinField.setText("");
    mCrit2MaxField.setText("");
    mCrit2MinField.setEnabled(true);
    mCrit2MaxField.setEnabled(true);
  }

  private void clearCriterion3() {
    mIgnoreEvent = true;
    mParam3Popup.setSelectedIndex(0);
    if (mQCStandard == JOAConstants.IGOSS_QC_STD || mQCStandard == JOAConstants.WOCE_QC_STD) {
      mQC3Popup.setSelectedIndex(0);
    }
    mQC3Popup.setEnabled(false);
    mCrit3MinField.setText("");
    mCrit3MaxField.setText("");
    mCrit3MinField.setEnabled(true);
    mCrit3MaxField.setEnabled(true);
  }

  private void clearCriterion4() {
    mIgnoreEvent = true;
    mParam4Popup.setSelectedIndex(0);
    if (mQCStandard == JOAConstants.IGOSS_QC_STD || mQCStandard == JOAConstants.WOCE_QC_STD) {
      mQC4Popup.setSelectedIndex(0);
    }
    mQC4Popup.setEnabled(false);
    mCrit4MinField.setText("");
    mCrit4MaxField.setText("");
    mCrit4MinField.setEnabled(true);
    mCrit4MaxField.setEnabled(true);
  }

  private static int KEY_STATE = 0;
  private static int CRIT1_BOOLEAN_KEY = 1;
  private static int CRIT2_BOOLEAN_KEY = 2;
  private static int BOTH_CRIT_BOOLEAN_KEY = 3;
  private static int PARAM_KEY = 4;
  private static int MINVAL_KEY = 5;
  private static int MAXVAL_KEY = 6;
  private static int SYMBOL_KEY = 7;
  private static int SYMBOL_SIZE_KEY = 8;
  private Color CONTRASTINGCOLOR = null;
  private int SYMBOL;
  private int SYMBOLSIZE;
  private boolean[] USE_CRITERION = {false, false, false, false};
  private String[] PARAM = new String[4];
  private double[] MINVAL = new double[4];
  private double[] MAXVAL = new double[4];
  private boolean CRITGRP1ISAND = false;
  private boolean CRITGRP2ISAND = false;
  private boolean CRITGRP1AND2ISAND = false;
  private boolean USECONTRASTINGCOLOR = false;
  private boolean ENLARGESYMBOL = false;
  private boolean SHOWONLY = true;
  private int QCSTD;
  private boolean CRIT1ISQC = false;
  private boolean CRIT2ISQC = false;
  private boolean CRIT3ISQC = false;
  private boolean CRIT4ISQC = false;
  private int WHICH_CRITERION = 0;

  private class MapNotifyStr extends HandlerBase {
    public void startDocument() throws SAXException {
      CRITGRP1ISAND = false;
      CRITGRP2ISAND = false;
      CRITGRP1AND2ISAND = false;
      CRIT1ISQC = false;
      CRIT2ISQC = false;
      CRIT3ISQC = false;
      CRIT4ISQC = false;
      CRITGRP1ISAND = false;
      CRITGRP2ISAND = false;
      CRITGRP1AND2ISAND = false;
      USECONTRASTINGCOLOR = false;
      ENLARGESYMBOL = false;
      SHOWONLY = true;

      for (int i = 0; i < 4; i++) {
        MINVAL[i] = -1e10;
        MAXVAL[i] = 1e10;
        PARAM[i] = null;
        USE_CRITERION[i] = false;
      }
    }

    public void startElement(String name, AttributeList amap) throws SAXException {
      KEY_STATE = 0;
      if (name.equals("crit1isand")) {
        KEY_STATE = CRIT1_BOOLEAN_KEY;
      }
      else if (name.equals("crit2isand")) {
        KEY_STATE = CRIT2_BOOLEAN_KEY;
      }
      else if (name.equals("crit1and2isand")) {
        KEY_STATE = BOTH_CRIT_BOOLEAN_KEY;
      }
      else if (name.equals("param")) {
        KEY_STATE = PARAM_KEY;
      }
      else if (name.equals("minval")) {
        KEY_STATE = MINVAL_KEY;
      }
      else if (name.equals("maxval")) {
        KEY_STATE = MAXVAL_KEY;
      }
      else if (name.equals("symbol")) {
        KEY_STATE = SYMBOL_KEY;
      }
      else if (name.equals("symbolsize")) {
        KEY_STATE = SYMBOL_SIZE_KEY;
      }
      else if (name.equals("contrastingcolor")) {
        int red = 255;
        int green = 0;
        int blue = 0;
        for (int i = 0; i < amap.getLength(); i++) {
          try {
            if (amap.getName(i).equals("red")) {
              red = Integer.valueOf(amap.getValue(i)).intValue();
            }
            else if (amap.getName(i).equals("green")) {
              green = Integer.valueOf(amap.getValue(i)).intValue();
            }
            else if (amap.getName(i).equals("blue")) {
              blue = Integer.valueOf(amap.getValue(i)).intValue();
            }
          }
          catch (Exception ex) {}
        }
        CONTRASTINGCOLOR = new Color(red, green, blue);
        USECONTRASTINGCOLOR = true;
      }
      else if (name.equals("joaobsfilter")) {
        for (int i = 0; i < amap.getLength(); i++) {
          if (amap.getName(i).equals("qcstd")) {
          	if (amap.getValue(i).equalsIgnoreCase("WOCE")) {
              QCSTD = JOAConstants.WOCE_QC_STD;
          	}
          	else {
              QCSTD = JOAConstants.IGOSS_QC_STD;
            }
          }
        }
      }
      else if (name.equals("criterion1")) {
        for (int i = 0; i < amap.getLength(); i++) {
          if (amap.getName(i).equals("isqc")) {
            if (amap.getValue(i).equalsIgnoreCase("true")) {
              CRIT1ISQC = true;
            }
          }
        }
        USE_CRITERION[0] = true;
        WHICH_CRITERION = 0;
      }
      else if (name.equals("criterion2")) {
        for (int i = 0; i < amap.getLength(); i++) {
          if (amap.getName(i).equals("isqc")) {
            if (amap.getValue(i).equalsIgnoreCase("true")) {
              CRIT2ISQC = true;
            }
          }
        }
        USE_CRITERION[1] = true;
        WHICH_CRITERION = 1;
      }
      else if (name.equals("criterion3")) {
        for (int i = 0; i < amap.getLength(); i++) {
          if (amap.getName(i).equals("isqc")) {
            if (amap.getValue(i).equalsIgnoreCase("true")) {
              CRIT3ISQC = true;
            }
          }
        }
        USE_CRITERION[2] = true;
        WHICH_CRITERION = 2;
      }
      else if (name.equals("criterion4")) {
        for (int i = 0; i < amap.getLength(); i++) {
          if (amap.getName(i).equals("isqc")) {
            if (amap.getValue(i).equalsIgnoreCase("true")) {
              CRIT4ISQC = true;
            }
          }
        }
        USE_CRITERION[3] = true;
        WHICH_CRITERION = 3;
      }
      else if (name.equals("enlargesymbol")) {
        ENLARGESYMBOL = true;
        SHOWONLY = false;
      }
    }

    public void characters(char[] ch, int start, int len) throws SAXException {
      String strVal = new String(ch, start, len);
      if (KEY_STATE == CRIT1_BOOLEAN_KEY) {
        try {
          CRITGRP1ISAND = Boolean.valueOf(strVal).booleanValue();
        }
        catch (Exception ex) {
          CRITGRP1ISAND = true;
        }
      }
      else if (KEY_STATE == CRIT2_BOOLEAN_KEY) {
        try {
          CRITGRP2ISAND = Boolean.valueOf(strVal).booleanValue();
        }
        catch (Exception ex) {
          CRITGRP1ISAND = true;
        }
      }
      else if (KEY_STATE == BOTH_CRIT_BOOLEAN_KEY) {
        try {
          CRITGRP1AND2ISAND = Boolean.valueOf(strVal).booleanValue();
        }
        catch (Exception ex) {
          CRITGRP1AND2ISAND = true;
        }
      }
      else if (KEY_STATE == PARAM_KEY) {
        PARAM[WHICH_CRITERION] = new String(strVal);
      }
      else if (KEY_STATE == MINVAL_KEY) {
        try {
          MINVAL[WHICH_CRITERION] = Double.valueOf(strVal).doubleValue();
        }
        catch (Exception ex) {
          MINVAL[WHICH_CRITERION] = -1.0E10;
        }
      }
      else if (KEY_STATE == MAXVAL_KEY) {
        try {
          MAXVAL[WHICH_CRITERION] = Double.valueOf(strVal).doubleValue();
        }
        catch (Exception ex) {
          MAXVAL[WHICH_CRITERION] = -1.0E10;
        }
      }
      else if (KEY_STATE == SYMBOL_KEY) {
        try {
          SYMBOL = Integer.valueOf(strVal).intValue();
        }
        catch (Exception ex) {
          SYMBOL = 4;
        }
        SHOWONLY = false;
      }
      else if (KEY_STATE == SYMBOL_SIZE_KEY) {
        try {
          SYMBOLSIZE = Integer.valueOf(strVal).intValue();
        }
        catch (Exception ex) {
          SYMBOLSIZE = 4;
        }
      }
    }

    public void endElement(String name) throws SAXException {}
  }

  @SuppressWarnings("unchecked")
  protected void getFilter(File file) throws Exception {
    try {
      // parse as xml first
      Class c = Class.forName("com.ibm.xml.parser.SAXDriver");
      org.xml.sax.Parser parser = (org.xml.sax.Parser)c.newInstance();
      MapNotifyStr notifyStr = new MapNotifyStr();
      parser.setDocumentHandler(notifyStr);
      parser.parse(file.getPath());
    }
    catch (Exception xmlEx) {
      xmlEx.printStackTrace();
    }

    // update the UI
    clearUI();
    setUIFromParsed();
  }

  protected void setUIFromParsed() {
    boolean error = false;
    // set the qc popups
    //setQCPopUpQCStandard();

    if (USE_CRITERION[0]) {
      // set the value fields
      if (Math.abs(MINVAL[0]) < 1e10) {
        mCrit1MinField.setText(JOAFormulas.formatDouble(String.valueOf(MINVAL[0]), 3, false));
      }
      else {
        mCrit1MinField.setText("");
      }
      if (Math.abs(MAXVAL[0]) < 1e10) {
        mCrit1MaxField.setText(JOAFormulas.formatDouble(String.valueOf(MAXVAL[0]), 3, false));
      }
      else {
        mCrit1MaxField.setText("");
      }

      // set the param if possible
      boolean found = false;
      int fl = 0;
      for (int i = 0; i < listItems1.size(); i++) {
        String listVal = (String)listItems1.elementAt(i);
        if (listVal.equalsIgnoreCase(PARAM[0])) {
          found = true;
          fl = i;
          break;
        }
      }
      if (found) {
        mIgnoreEvent = true;
        mParam1Popup.setSelectedIndex(fl);
        //mParam1Popup.ensureIndexIsVisible(JOAFormulas.correctListOffset(fl));
      }
      else {
        error = true;
      }

      // set the qc state
      if (CRIT1ISQC) {
        enableQCPopUp1();
        int qcval = (int)MAXVAL[0];
        if (mQCStandard == JOAConstants.WOCE_QC_STD && QCSTD == JOAConstants.IGOSS_QC_STD) {
          //translate IGOSS to WOCE;
          qcval = JOAFormulas.translateIGOSSQBToWOCE(qcval);
        }
        else if (mQCStandard == JOAConstants.IGOSS_QC_STD && QCSTD == JOAConstants.WOCE_QC_STD) {
          //translate WOCE to IGOSS;
          qcval = JOAFormulas.translateWOCESampleQBToIGOSS(qcval);
        }

        if (mQCStandard == JOAConstants.WOCE_QC_STD) {
        	setQCPopUpSelection(mQC1Popup, qcval-2);
        }
        else {
        	setQCPopUpSelection(mQC1Popup, qcval-1);
        }
       
        mCrit1MaxField.setText(String.valueOf(qcval));
      }
      else {
        disableQCPopUp1();
      }
    }

    if (USE_CRITERION[1]) {
      if (Math.abs(MINVAL[1]) < 1e10) {
        mCrit2MinField.setText(JOAFormulas.formatDouble(String.valueOf(MINVAL[1]), 3, false));
      }
      else {
        mCrit2MinField.setText("");
      }
      if (Math.abs(MAXVAL[1]) < 1e10) {
        mCrit2MaxField.setText(JOAFormulas.formatDouble(String.valueOf(MAXVAL[1]), 3, false));
      }
      else {
        mCrit2MaxField.setText("");
      }

      // set the param if possible
      boolean found = false;
      int fl = 0;
      for (int i = 0; i < listItems1.size(); i++) {
        String listVal = (String)listItems1.elementAt(i);
        if (listVal.equalsIgnoreCase(PARAM[1])) {
          found = true;
          fl = i;
          break;
        }
      }
      if (found) {
        mIgnoreEvent = true;
        mParam2Popup.setSelectedIndex(fl);
      }
      else {
        error = true;
      }

      // set the qc state
      if (CRIT2ISQC) {
        enableQCPopUp2();
        int qcval = (int)MAXVAL[1];
        if (mQCStandard == JOAConstants.WOCE_QC_STD && QCSTD == JOAConstants.IGOSS_QC_STD) {
          //translate IGOSS to WOCE;
          qcval = JOAFormulas.translateIGOSSQBToWOCE(qcval);
        }
        else if (mQCStandard == JOAConstants.IGOSS_QC_STD && QCSTD == JOAConstants.WOCE_QC_STD) {
          //translate WOCE to IGOSS;
          qcval = JOAFormulas.translateWOCESampleQBToIGOSS(qcval);
        }

        if (mQCStandard == JOAConstants.WOCE_QC_STD) {
        	setQCPopUpSelection(mQC2Popup, qcval-2);
        }
        else {
        	setQCPopUpSelection(mQC2Popup, qcval-1);
        }
        mCrit2MaxField.setText(String.valueOf(qcval));
      }
      else {
        disableQCPopUp2();
      }
    }

    if (USE_CRITERION[2]) {
      if (Math.abs(MINVAL[2]) < 1e10) {
        mCrit3MinField.setText(JOAFormulas.formatDouble(String.valueOf(MINVAL[2]), 3, false));
      }
      else {
        mCrit3MinField.setText("");
      }
      if (Math.abs(MAXVAL[2]) < 1e10) {
        mCrit3MaxField.setText(JOAFormulas.formatDouble(String.valueOf(MAXVAL[2]), 3, false));
      }
      else {
        mCrit3MaxField.setText("");
      }

      // set the param if possible
      boolean found = false;
      int fl = 0;
      for (int i = 0; i < listItems1.size(); i++) {
        String listVal = (String)listItems1.elementAt(i);
        if (listVal.equalsIgnoreCase(PARAM[2])) {
          found = true;
          fl = i;
          break;
        }
      }
      if (found) {
        mIgnoreEvent = true;
        mParam3Popup.setSelectedIndex(fl);
      }
      else {
        error = true;
      }

      // set the qc state
      if (CRIT3ISQC) {
        enableQCPopUp3();
        int qcval = (int)MAXVAL[2];
        if (mQCStandard == JOAConstants.WOCE_QC_STD && QCSTD == JOAConstants.IGOSS_QC_STD) {
          //translate IGOSS to WOCE;
          qcval = JOAFormulas.translateIGOSSQBToWOCE(qcval);
        }
        else if (mQCStandard == JOAConstants.IGOSS_QC_STD && QCSTD == JOAConstants.WOCE_QC_STD) {
          //translate WOCE to IGOSS;
          qcval = JOAFormulas.translateWOCESampleQBToIGOSS(qcval);
        }

        if (mQCStandard == JOAConstants.WOCE_QC_STD) {
        	setQCPopUpSelection(mQC3Popup, qcval-2);
        }
        else {
        	setQCPopUpSelection(mQC3Popup, qcval-1);
        }
        mCrit3MaxField.setText(String.valueOf(qcval));
      }
      else {
        disableQCPopUp3();
      }
    }

    if (USE_CRITERION[3]) {
      if (Math.abs(MINVAL[3]) < 1e10) {
        mCrit4MinField.setText(JOAFormulas.formatDouble(String.valueOf(MINVAL[3]), 3, false));
      }
      else {
        mCrit4MinField.setText("");
      }
      if (Math.abs(MAXVAL[3]) < 1e10) {
        mCrit4MaxField.setText(JOAFormulas.formatDouble(String.valueOf(MAXVAL[3]), 3, false));
      }
      else {
        mCrit4MaxField.setText("");
      }

      // set the param if possible
      boolean found = false;
      int fl = 0;
      for (int i = 0; i < listItems1.size(); i++) {
        String listVal = (String)listItems1.elementAt(i);
        if (listVal.equalsIgnoreCase(PARAM[3])) {
          found = true;
          fl = i;
          break;
        }
      }
      if (found) {
        mIgnoreEvent = true;
        mParam4Popup.setSelectedIndex(fl);
      }
      else {
        error = true;
      }

      // set the qc state
      if (CRIT4ISQC) {
        enableQCPopUp4();
        int qcval = (int)MAXVAL[3];
        if (mQCStandard == JOAConstants.WOCE_QC_STD && QCSTD == JOAConstants.IGOSS_QC_STD) {
          //translate IGOSS to WOCE;
          qcval = JOAFormulas.translateIGOSSQBToWOCE(qcval);
        }
        else if (mQCStandard == JOAConstants.IGOSS_QC_STD && QCSTD == JOAConstants.WOCE_QC_STD) {
          //translate WOCE to IGOSS;
          qcval = JOAFormulas.translateWOCESampleQBToIGOSS(qcval);
        }

        if (mQCStandard == JOAConstants.WOCE_QC_STD) {
        	setQCPopUpSelection(mQC4Popup, qcval-2);
        }
        else {
        	setQCPopUpSelection(mQC4Popup, qcval-1);
        }
        
        mCrit4MaxField.setText(String.valueOf(qcval));
      }
      else {
        disableQCPopUp4();
      }
    }

    if (USE_CRITERION[0] && USE_CRITERION[1]) {
      if (CRITGRP1ISAND) {
        mCrit1IsAnd.setSelected(true);
      }
      else {
        mCrit1IsOr.setSelected(true);
      }
    }

    if ((USE_CRITERION[0] || USE_CRITERION[1]) && (USE_CRITERION[2] || USE_CRITERION[3])) {
      if (CRITGRP1AND2ISAND) {
        mCrit1Crit2IsAnd.setSelected(true);
      }
      else {
        mCrit1Crit2IsOr.setSelected(true);
      }
    }

    if (USE_CRITERION[2] && USE_CRITERION[3]) {
      if (CRITGRP2ISAND) {
        mCrit2IsAnd.setSelected(true);
      }
      else {
        mCrit2IsOr.setSelected(true);
      }
    }

    // set the display options
    if (!SHOWONLY) {
      // highlight option
      mHighlight.setSelected(true);
      enableHighlightOptions();
      if (ENLARGESYMBOL) {
        mEnlargeCurrSymbol.setSelected(true);
        disableSymbolStuff();
      }
      else {
        mUseNewSymbol.setSelected(true);
        enableSymbolStuff();
        mIgnoreEvent = true;
        mSymbolPopup.setSelectedIndex(SYMBOL - 1);
        mSizeField.setValue(new Integer(SYMBOLSIZE));
      }

      if (USECONTRASTINGCOLOR) {
        mContrastColor.setSelected(true);
        mContrastSwatch.setColor(CONTRASTINGCOLOR);
      }

    }
    else {
      // show only option
      mShowOnly.setSelected(true);
      disableHighlightOptions();
    }

    if (error) {
      JFrame f = new JFrame("Observation Filter Load Error");
      Toolkit.getDefaultToolkit().beep();
      JOptionPane.showMessageDialog(f,
                                    "One or more of the saved filter parameters were " + "\n" + "not found in the current dataset.");
    }
  }

  protected void setQCPopUpQCStandard() {
    if (mQCStandard == JOAConstants.IGOSS_QC_STD) {
      for (int i = 0; i < IGOSSList.length; i++) {
        mQC1Popup.addItem(IGOSSList[i]);
        mQC2Popup.addItem(IGOSSList[i]);
        mQC3Popup.addItem(IGOSSList[i]);
        mQC4Popup.addItem(IGOSSList[i]);
      }
    }
    else if (mQCStandard == JOAConstants.WOCE_QC_STD) {
      for (int i = 0; i < WOCEList.length; i++) {
        mQC1Popup.addItem(WOCEList[i]);
        mQC2Popup.addItem(WOCEList[i]);
        mQC3Popup.addItem(WOCEList[i]);
        mQC4Popup.addItem(WOCEList[i]);
      }
    }
  }

  protected void setQCPopUpSelection(JOAJComboBox cb, int qcVal) {
    mIgnoreEvent = true;
    cb.setSelectedIndex(qcVal);
  }
}

/*
 * $Id: ConfigSections.java,v 1.12 2005/09/07 18:49:31 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.border.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import gov.noaa.pmel.eps2.*;
import ucar.multiarray.*;
import javaoceanatlas.PowerOceanAtlas;

@SuppressWarnings("serial")
public class ConfigSections extends JOAJDialog implements ActionListener, ItemListener {
  private FileViewer mFileViewerRef = null;
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mCancelButton = null;
  protected JOAJButton mShowEditorButton = null;
  protected JOAJRadioButton mReverseStations = null;
  protected JOAJRadioButton mCloneSection  = null;
  protected JOAJRadioButton mMergeCasts = null;
  protected JOAJRadioButton mMergeSections = null;
  protected JOAJRadioButton mAddBottom = null;
  protected JOAJRadioButton mAddBottomFromEtopo = null;
  protected JOAJRadioButton mAddBottomFromUser = null;
  protected JOAJRadioButton mApplyToAll = null;
  protected JOAJRadioButton mApplyToMissing = null;
  protected JOAJRadioButton mRenameSection = null;
  protected JOAJCheckBox mMergeObservations = null;
  protected JOAJCheckBox mIncludeStatistics = null;
  protected JOAJComboBox mSurfaces = null;
  protected JOAJComboBox mBathyFiles = null;
  protected JOAJRadioButton mSortStations = null;
  protected JOAJTextField mSectionNameField = null;
  protected JOAJTextField mDepthBelow = null;
  private Vector<SectionStation> mFoundStns = new Vector<SectionStation>();
  private JOAJLabel mNameLbl = null;
  private JOAJLabel mAddBottLbl = null;
  private double mLatMin, mLatMax, mLonLft, mLonRt;
  protected JOAJRadioButton mParamFilter = null;
  protected JOAJButton mParamFilterButton = null;
  protected JOAJRadioButton mApplyTSRelationship = null;
  protected JOAJButton mTSSetupButton = null;
  
  public ConfigSections(JOAWindow par, FileViewer fv) {
    super(par, "File Manager", false);
    this.mFileViewerRef = fv;

    // init the interface
    init();
  }

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(5, 5));
    JPanel mainPanel = new JPanel(); // everything goes in here
    mainPanel.setLayout(new BorderLayout(5, 5));

    JPanel actionsPanel = new JPanel();
    actionsPanel.setLayout(new ColumnLayout(javaoceanatlas.utility.Orientation.LEFT,
                                            javaoceanatlas.utility.Orientation.TOP, 5));
    TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kActions"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    actionsPanel.setBorder(tb);
    mReverseStations = new JOAJRadioButton(b.getString("kReverseStations"), true);
    actionsPanel.add(mReverseStations);
    mCloneSection = new JOAJRadioButton(b.getString("kCloneSection"), true);
    actionsPanel.add(mCloneSection);
    mMergeCasts = new JOAJRadioButton(b.getString("kMergeCasts"));
    actionsPanel.add(mMergeCasts);

    JPanel jp = new JPanel();
    jp.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
    mMergeObservations = new JOAJCheckBox(b.getString("kMergeObservations"), false);
    jp.add(new JOAJLabel("     "));
    jp.add(mMergeObservations);
    actionsPanel.add(jp);

    mMergeSections = new JOAJRadioButton(b.getString("kMergeSections"));
    actionsPanel.add(mMergeSections);

    JPanel namePanel = new JPanel();
    namePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 5));
    mNameLbl = new JOAJLabel(b.getString("kNewSectionName"));
    namePanel.add(mNameLbl);
    mSectionNameField = new JOAJTextField(20);
    mSectionNameField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    namePanel.add(mSectionNameField);

    mSortStations = new JOAJRadioButton(b.getString("kSortAllStns"));
    actionsPanel.add(mSortStations);
    JPanel secEditPanel = new JPanel();
    secEditPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
    secEditPanel.add(new JOAJLabel("          "));
    mShowEditorButton = new JOAJButton(b.getString("kShowSectionEditor"));
    mShowEditorButton.setActionCommand("edit");
    mShowEditorButton.addActionListener(this);
    secEditPanel.add(mShowEditorButton);
    actionsPanel.add(secEditPanel);

    mParamFilter = new JOAJRadioButton(b.getString("kFilterByParameter"));
    actionsPanel.add(mParamFilter);
    JPanel pfPanel = new JPanel();
    pfPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
    pfPanel.add(new JOAJLabel("          "));
    mParamFilterButton = new JOAJButton(b.getString("kBottleFilter"));
    mParamFilterButton.setActionCommand("filter");
    mParamFilterButton.addActionListener(this);
    pfPanel.add(mParamFilterButton);
    actionsPanel.add(pfPanel);

    mApplyTSRelationship = new JOAJRadioButton(b.getString("kAssignTS"));
    actionsPanel.add(mApplyTSRelationship);
    JPanel tsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
    tsPanel.add(new JOAJLabel("          "));
    mTSSetupButton = new JOAJButton(b.getString("kEnterTSModel"));
    mTSSetupButton.setActionCommand("setupts");
    mTSSetupButton.addActionListener(this);
    tsPanel.add(mTSSetupButton);
    actionsPanel.add(tsPanel);

    JPanel bottomOptions = new JPanel();
    bottomOptions.setLayout(new ColumnLayout(javaoceanatlas.utility.Orientation.LEFT,
                                             javaoceanatlas.utility.Orientation.TOP, 5));

    JPanel bottLine0 = new JPanel();
    bottLine0.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));

    JPanel bottLine1 = new JPanel();
    bottLine1.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));

    JPanel bottLine2 = new JPanel();
    bottLine2.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));

    JPanel bottLine3 = new JPanel();
    bottLine3.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));

    mAddBottom = new JOAJRadioButton(b.getString("kAddBottom"));
    bottLine0.add(mAddBottom);
    Vector<String> etopFiles = JOAFormulas.getEtopoList();
    mBathyFiles = new JOAJComboBox(etopFiles);
    actionsPanel.add(bottLine0);

    ButtonGroup bg0 = new ButtonGroup();
    mAddBottomFromEtopo = new JOAJRadioButton(b.getString("kFromEtopo"), true);
    bg0.add(mAddBottomFromEtopo);
    bottLine1.add(new JOAJLabel("          "));
    bottLine1.add(mAddBottomFromEtopo);
    bottLine1.add(mBathyFiles);
    actionsPanel.add(bottLine1);

    mAddBottomFromUser = new JOAJRadioButton(b.getString("kUserDefined"));
    bg0.add(mAddBottomFromUser);
    bottLine2.add(new JOAJLabel("          "));
    bottLine2.add(mAddBottomFromUser);
    mDepthBelow = new JOAJTextField(2);
    mDepthBelow.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mDepthBelow.setText("5");
    bottLine2.add(mDepthBelow);
    mAddBottLbl = new JOAJLabel(b.getString("kmBelowLastObservation"));
    bottLine2.add(mAddBottLbl);
    actionsPanel.add(bottLine2);

    ButtonGroup bg2 = new ButtonGroup();
    mApplyToAll = new JOAJRadioButton(b.getString("kApplyToAll"));
    bg2.add(mApplyToAll);
    mApplyToMissing = new JOAJRadioButton(b.getString("kApplyToMissing"), true);
    bg2.add(mApplyToMissing);
    bottLine3.add(new JOAJLabel("          "));
    bottLine3.add(mApplyToMissing);
    bottLine3.add(mApplyToAll);
    actionsPanel.add(bottLine3);

    actionsPanel.add(bottomOptions);

    mRenameSection = new JOAJRadioButton(b.getString("kRenameSection"));
    //actionsPanel.add(mRenameSection);

    // finally the name for new FVs
    actionsPanel.add(namePanel);

    ButtonGroup bg1 = new ButtonGroup();
    bg1.add(mReverseStations);
    bg1.add(mMergeCasts);
    bg1.add(mSortStations);
    bg1.add(mMergeSections);
    bg1.add(mAddBottom);
    bg1.add(mRenameSection);
    bg1.add(mParamFilter);
    bg1.add(mCloneSection);
    bg1.add(mApplyTSRelationship);
    mReverseStations.addItemListener(this);
    mMergeCasts.addItemListener(this);
    mSortStations.addItemListener(this);
    mMergeSections.addItemListener(this);
    mMergeObservations.addItemListener(this);
    mAddBottom.addItemListener(this);
    mRenameSection.addItemListener(this);
    mParamFilter.addItemListener(this);
    mCloneSection.addItemListener(this);
    mApplyTSRelationship.addItemListener(this);

    mainPanel.add("Center", new TenPixelBorder(actionsPanel, 0, 5, 0, 5));

    enableSortOptions(false);
    enablePFOptions(false);
    enableName(false);
    setMergeOptions(false);
    enableAddBottOptions(false);
    enableTSOptions(false);

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

    // show dialog at center of screen
    this.pack();
    Rectangle dBounds = this.getBounds();
    Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
    int x = sd.width / 2 - dBounds.width / 2;
    int y = sd.height / 2 - dBounds.height / 2;
    this.setLocation(x, y);
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      if (mReverseStations.isSelected()) {
        reverseStns();
      }
      else if (mCloneSection.isSelected()) {
        cloneIt();
      }
      else if (mMergeCasts.isSelected()) {
        mergeCasts(mMergeObservations.isSelected());
      }
      else if (mMergeSections.isSelected()) {
        mergeSections();
      }
      else if (mAddBottom.isSelected()) {
        addOceanBottom();
      }
      else if (mRenameSection.isSelected()) {
        renameSection();
      }
      this.dispose();
    }
    else if (cmd.equals("edit")) {
      // prepare for section editor
      sortCasts();
      SectionEditor mCurrSectionEditor = new SectionEditor(null, mFileViewerRef, mFoundStns, mFileViewerRef.mLonMin,
          mFileViewerRef.mLonMax, (mFileViewerRef.mFileViewerName + " (edited)"));
      mCurrSectionEditor.pack();
      mCurrSectionEditor.setVisible(true);
      this.dispose();
    }
    else if (cmd.equals("filter")) {
      ConfigureParameterFilter config = new ConfigureParameterFilter(null, mFileViewerRef);
      config.pack();
      config.setVisible(true);
      this.dispose();
    }
    else if (cmd.equals("setupts")) {
    	ConfigureTSRelationship config = new ConfigureTSRelationship(mFileViewerRef);
      config.pack();
	    Rectangle dBounds = config.getBounds();
	    Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = sd.width / 2 - dBounds.width / 2;
	    int y = sd.height / 2 - dBounds.height / 2;
	    config.setLocation(x, y);
      config.setVisible(true);
      this.dispose();
    }
  }

  public void itemStateChanged(ItemEvent evt) {
    if (evt.getSource() instanceof JOAJRadioButton) {
      JOAJRadioButton rb = (JOAJRadioButton)evt.getSource();
      if (evt.getStateChange() == ItemEvent.SELECTED && rb == mReverseStations) {
        mSectionNameField.setText("");
        enableName(false);
        enableSortOptions(false);
        enablePFOptions(false);
        setMergeOptions(false);
        enableAddBottOptions(false);
        enableTSOptions(false);
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mMergeCasts) {
        enableSortOptions(false);
        enablePFOptions(false);
        enableName(true);
        setMergeOptions(true);
        enableAddBottOptions(false);
        enableTSOptions(false);
        String name = " (merged)";
        if (mMergeObservations.isSelected()) {
          name = " (merged-averaged)";
        }
        mSectionNameField.setText(mFileViewerRef.mFileViewerName + name);
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mCloneSection) {
        enableSortOptions(false);
        enablePFOptions(false);
        enableName(true);
        setMergeOptions(false);
        enableAddBottOptions(false);
        enableTSOptions(false);
        String name = " (cloned)";
        mSectionNameField.setText(mFileViewerRef.mFileViewerName + name);
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mSortStations) {
        mSectionNameField.setText("");
        enableSortOptions(true);
        enableName(false);
        setMergeOptions(false);
        enableAddBottOptions(false);
        enablePFOptions(false);
        enableTSOptions(false);
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mMergeSections) {
        mSectionNameField.setText(mFileViewerRef.mFileViewerName + " (merged)");
        enableSortOptions(false);
        enablePFOptions(false);
        enableName(true);
        setMergeOptions(false);
        enableAddBottOptions(false);
        enableTSOptions(false);
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mAddBottom) {
        enableSortOptions(false);
        enablePFOptions(false);
        enableName(false);
        setMergeOptions(false);
        enableAddBottOptions(true);
        enableTSOptions(false);
        mSectionNameField.setText("");
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mRenameSection) {
        enableSortOptions(false);
        enablePFOptions(false);
        enableName(true);
        setMergeOptions(false);
        enableAddBottOptions(false);
        enableTSOptions(false);
        mSectionNameField.setText("");
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mParamFilter) {
        enableSortOptions(false);
        enablePFOptions(true);
        enableName(false);
        setMergeOptions(false);
        enableAddBottOptions(false);
        enableTSOptions(false);
        mSectionNameField.setText("");
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mApplyTSRelationship) {
        enableSortOptions(false);
        enablePFOptions(false);
        enableName(false);
        setMergeOptions(false);
        enableAddBottOptions(false);
        enableTSOptions(true);
        mSectionNameField.setText("");
      }
    }
    else if (evt.getSource() instanceof JOAJCheckBox) {
      JOAJCheckBox cb = (JOAJCheckBox)evt.getSource();
      if (evt.getStateChange() == ItemEvent.SELECTED && cb == mMergeObservations) {
        String name = " (merged-averaged)";
        mSectionNameField.setText(mFileViewerRef.mFileViewerName + name);
      }
      else {
        String name = " (merged)";
        mSectionNameField.setText(mFileViewerRef.mFileViewerName + name);
      }
    }
  }

  protected void enableName(boolean flag) {
    mSectionNameField.setEnabled(flag);
    mSectionNameField.invalidate();
    mNameLbl.setEnabled(flag);
    mNameLbl.invalidate();
  }

  protected void setMergeOptions(boolean flag) {
    if (!flag) {
      mMergeObservations.setSelected(false);
    }
    mMergeObservations.setEnabled(flag);
  }

  protected void enableSortOptions(boolean flag) {
    mShowEditorButton.setEnabled(flag);
  }

  protected void enablePFOptions(boolean flag) {
    mParamFilterButton.setEnabled(flag);
  }

  protected void enableTSOptions(boolean flag) {
  	mTSSetupButton.setEnabled(flag);
  }

  protected void enableAddBottOptions(boolean flag) {
    mAddBottLbl.setEnabled(flag);
    mBathyFiles.setEnabled(flag);
    mAddBottomFromEtopo.setEnabled(flag);
    mAddBottomFromUser.setEnabled(flag);
    mDepthBelow.setEnabled(flag);
    mApplyToAll.setEnabled(flag);
    mApplyToMissing.setEnabled(flag);
  }

  public void addOceanBottom() {
    if (mAddBottomFromEtopo.isSelected()) {
      addOceanBottomFromEtopo();
    }
    else {
      addOceanBottomRelativeToDeepest();
    }

    // issue an event to cause all windows to update
    mFileViewerRef.updateAfterSectionManager();
  }

  public void addOceanBottomRelativeToDeepest() {
    double delta = 5.0;
    try {
      delta = Double.valueOf(mDepthBelow.getText()).doubleValue();
    }
    catch (NumberFormatException ex) {
    }

    for (int fc = 0; fc < mFileViewerRef.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewerRef.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = of.getSection(sec);
        if (sech.mNumCasts == 0) {
          continue;
        }

        int pPos = sech.getPRESVarPos();

        for (int stc = 0; stc < sech.mStations.size(); stc++) {
          Station sh = (Station)sech.mStations.elementAt(stc);
          int numBottles = sh.getNumBottles();
          if (sh.getBottom() == JOAConstants.MISSINGVALUE || mApplyToAll.isSelected()) {
            Bottle bh = (Bottle)sh.mBottles.elementAt(numBottles - 1);
            double pres = bh.mDValues[pPos];
            double depth = JOAFormulas.presToZ(pres, sh.getLat());
            sh.setBottom((int)(depth + delta));
          }
        }
      }
    }

  }

  public void addOceanBottomFromEtopo() {
    String bathyFile = (String)mBathyFiles.getSelectedItem();
    ProgressDialog progress = null;
    progress = new ProgressDialog(mFileViewerRef, "Reading Etopo Data in " + bathyFile, Color.blue, Color.white);
    progress.setVisible(true);

    // get the overall lat/lon range of the dataset
    mLatMin = mFileViewerRef.getMinLat() - 2.0;
    mLatMax = mFileViewerRef.getMaxLat() + 2.0;
    mLonLft = mFileViewerRef.getMinLon();
    if (mLonLft > 180) {
      mLonLft = mLonLft - 360;
    }
    else if (mLonLft < -180) {
      mLonLft = mLonLft + 360;
    }
    mLonRt = mFileViewerRef.getMaxLon();
    if (mLonRt > 180) {
      mLonRt = mLonRt - 360;
    }
    else if (mLonRt < -180) {
      mLonRt = mLonRt + 360;
    }

    mLonLft = mLonLft - 2.0;
    mLonRt = mLonRt + 2.0;

    // get the etopo datafile
    File etopoFile = null;
    Dbase mETOPODB = null;
    try {
      etopoFile = JOAFormulas.getSupportFile(bathyFile);
    }
    catch (IOException ex) {
      // present an error dialog
      return;
    }
    String dir = etopoFile.getParent();

    EpicPtrs ptrDB = new EpicPtrs();

    //create a pointer
    EpicPtr epPtr = new EpicPtr(EPSConstants.NETCDFFORMAT, "ETOPO Import", "ETOPO", "na", "na", -99, -99,
                                new gov.noaa.pmel.util.GeoDate(), -99, -99, null, bathyFile, dir, null);

    // set the data of ptrDB to this one entry
    ptrDB.setFile(etopoFile);
    ptrDB.setData(epPtr);

    // create a database
    PointerDBIterator pdbi = ptrDB.iterator();
    EPSDbase etopoDB = new EPSDbase(pdbi, true);

    // get the database
    EPSDBIterator dbItor = etopoDB.iterator(true);

    try {
      mETOPODB = (Dbase)dbItor.getElement(0);
    }
    catch (Exception ex) {}

    // latitude axis
    Axis latAxis = mETOPODB.getAxis("Y");
    if (latAxis == null) {
      latAxis = mETOPODB.getAxis("y");
    }
    if (latAxis == null) {
      latAxis = mETOPODB.getAxis("latitude");
    }
    MultiArray latma = latAxis.getData();

    // longitude axis
    Axis lonAxis = mETOPODB.getAxis("X");
    if (lonAxis == null) {
      lonAxis = mETOPODB.getAxis("x");
    }
    if (lonAxis == null) {
      lonAxis = mETOPODB.getAxis("longitude");
    }
    MultiArray lonma = lonAxis.getData();

    Vector<?> vars = mETOPODB.getMeasuredVariables(false);
    EPSVariable rose = (EPSVariable)vars.elementAt(0);
    MultiArray zma = rose.getData();

//    int totalStns = 0;
//    for (int fc = 0; fc < mFileViewerRef.mNumOpenFiles; fc++) {
//      OpenDataFile of = (OpenDataFile)mFileViewerRef.mOpenFiles.elementAt(fc);
//
//      for (int sec = 0; sec < of.mNumSections; sec++) {
//        Section sech = (Section)of.mSections.elementAt(sec);
//        if (sech.mNumCasts == 0) {
//          continue;
//        }
//
//        for (int stc = 0; stc < sech.mStations.size(); stc++) {
//          totalStns++;
//          Station sh = (Station)sech.mStations.elementAt(stc);
//          sh.setTempDist(1e35);
//          sh.setTempDepth(-99);
//
//        }
//      }
//    }

    double latCtr, lonCtr;
    double[] savedLats = new double[latAxis.getLen() * lonAxis.getLen()];
    double[] savedLons = new double[lonAxis.getLen() * latAxis.getLen()];
    double[] savedVals = new double[lonAxis.getLen() * latAxis.getLen()];
    
    int numSavedPts = 0;
    // loop on the lats and lons
    for (int ln = 0; ln < lonAxis.getLen(); ln++) {
      for (int lt = 0; lt < latAxis.getLen(); lt++) {
        try {
          // array optimization needed
          latCtr = latma.getDouble(new int[] {lt});
        }
        catch (Exception ex) {
          //ex.printStackTrace();
          System.out.println("at P3");
          continue;
        }

        try {
          // array optimization needed
          lonCtr = lonma.getDouble(new int[] {ln});
          if (lonCtr > 180) {
            lonCtr = lonCtr - 360;
          }
          else if (lonCtr < -180) {
            lonCtr = lonCtr + 360;
          }
        }
        catch (Exception ex) {
          System.out.println("at P4");
          continue;
        }

        // test whether in window or not
        if (LLKeep(latCtr, lonCtr)) {
          // compute the distance and
          double zVal = -99;
          try {
            // array optimization needed
            zVal = zma.getDouble(new int[] {lt, ln});
          }
          catch (Exception ex) {
            System.out.println("at P5");
          }
          savedLats[numSavedPts] = latCtr;
          savedLons[numSavedPts] = lonCtr;
          savedVals[numSavedPts] = zVal;
          numSavedPts++;
        }
      }
    }            		
    
//    System.out.println(savedLons.length + "\t" + savedLats.length + "\t" + savedVals.length);


//    for (int fc = 0; fc < mFileViewerRef.mNumOpenFiles; fc++) {
//      OpenDataFile of = (OpenDataFile)mFileViewerRef.mOpenFiles.elementAt(fc);
//
//      for (int sec = 0; sec < of.mNumSections; sec++) {
//        Section sech = (Section)of.mSections.elementAt(sec);
//        if (sech.mNumCasts == 0) {
//          continue;
//        }
//
//        for (int stc = 0; stc < sech.mStations.size(); stc++) {
//          progress.setPercentComplete(100.0 * ((double)stc / (double)totalStns));
//
//          Station sh = (Station)sech.mStations.elementAt(stc);
//          sh.setTempDist(1000);
//
//          double lat = sh.getLat();
//          double lon = sh.getLon();
//          if (lon > 180) {
//            lon = lon - 360;
//          }
//          else if (lon < -180) {
//            lon = lon + 360;
//          }
//          for (int i = 0; i < numSavedPts; i++) {
//            latCtr = savedLats[i];
//            lonCtr = savedLons[i];
//            double zVal = savedVals[i];
//            double dist = JOAFormulas.GreatCircle(latCtr, lonCtr, lat, lon);//Math.sqrt(((lon - lonCtr) * (lon - lonCtr)) + ((lat - latCtr) * (lat - latCtr)));
//            if (dist < sh.getTempDist() && zVal <= 0) {
//              sh.setTempDist(dist);
//              sh.setTempDepth(-zVal);
//            }
//          }
//        }
//      }
//    }

    for (int fc = 0; fc < mFileViewerRef.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewerRef.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = of.getSection(sec);
        if (sech.mNumCasts == 0) {
          continue;
        }

        for (int stc = 0; stc < sech.mStations.size(); stc++) {
          Station sh = (Station)sech.mStations.elementAt(stc);

          if (sh.getBottom() == JOAConstants.MISSINGVALUE || mApplyToAll.isSelected()) {
          	double lat = sh.getLat();
            double lon = sh.getLon();
            if (lon > 180) {
              lon = lon - 360;
            }
            else if (lon < -180) {
              lon = lon + 360;
            }
            
            double tempDist = 1000000000000000000000000000000.;
            for (int i = 0; i < savedVals.length; i++) {
              latCtr = savedLats[i];
              lonCtr = savedLons[i];
              double zVal = savedVals[i];
              double dist = Math.sqrt(((lon - lonCtr) * (lon - lonCtr)) + ((lat - latCtr) * (lat - latCtr)));
              if (dist < tempDist) {
              	if (zVal <= 0) {
              		sh.setBottom((int)-zVal);
//              		System.out.println(i + "\t" + dist + "\t" + zVal + "\t" + latCtr + "\t" +  lonCtr);
              	}
                tempDist = dist;
              }
            }
          }
        }
      }
    }

    progress.setPercentComplete(100.0);
    progress.dispose();
  }

  public boolean LLKeep(double lat, double lon) {
    if ((lat < mLatMin) || (lat > mLatMax)) {
      return false;
    }
    if (lon > 180) {
      lon = lon - 360;
    }
    else if (lon < -180) {
      lon = lon + 360;
    }
    if (mLonLft < mLonRt) {
      if ((lon < mLonLft) || (lon > mLonRt)) {
        return false;
      }
    }
    else {
      if ((lon >= mLonLft) || (lon <= mLonRt)) {
        ;
      }
      else {
        return false;
      }
    }
    return true;
  }

  public void reverseStns() {
    try {
      JOAConstants.LogFileStream.writeBytes(mFileViewerRef.getTitle() + ": Reversed" + "\n");
      JOAConstants.LogFileStream.flush();
    }
    catch (Exception ex) {}

    for (int fc = 0; fc < mFileViewerRef.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewerRef.mOpenFiles.elementAt(fc);

      // first reverse the sections
      Vector<Section> tempSecs = new Vector<Section>();
      for (int sec = of.mNumSections - 1; sec >= 0; sec--) {
        Section sech = of.getSection(sec);
        tempSecs.addElement(sech);
      }

      int ord = 1;
      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = (Section)tempSecs.elementAt(sec);
        sech.setOrdinal(ord++);
        of.setSection(sech, sec);
      }

      int stnOrd = 1;
      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = of.getSection(sec);
        if (sech.mNumCasts == 0) {
          continue;
        }

        Vector<Station> tempStns = new Vector<Station>();
        for (int stc = sech.mStations.size() - 1; stc >= 0; stc--) {
          tempStns.addElement(sech.mStations.elementAt(stc));
        }

        for (int stc = 0; stc < sech.mStations.size(); stc++) {
          Station sh = (Station)tempStns.elementAt(stc);
          sh.setOrdinals(stnOrd++);
          sech.mStations.setElementAt(sh, stc);
        }
        tempStns = null;
      }
    }

    // issue an event to cause all windows to update
    mFileViewerRef.updateAfterSectionManager();
    mFileViewerRef.setSavedState(JOAConstants.MODIFIEDBYUSER, mFileViewerRef.getCurrOutFile());
  }

  public void mergeCasts(boolean avg) {
    try {
      JOAConstants.LogFileStream.writeBytes(mFileViewerRef.getTitle() + ": Merge multiple casts at each station");
      if (avg) {
        JOAConstants.LogFileStream.writeBytes(", average observed values at same pressure");
      }
      JOAConstants.LogFileStream.writeBytes("\n");
      JOAConstants.LogFileStream.flush();
    }
    catch (Exception ex) {}

    int stnOrd = 0;

    // merge multiple casts at a station: creates a new section
    for (int fc = 0; fc < mFileViewerRef.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewerRef.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = of.getSection(sec);
        int pPos = sech.getPRESVarPos();
        if (sech.mNumCasts == 0) {
          continue;
        }

        Vector<Bottle> combinedBottles = new Vector<Bottle>(100);
        int stc = 0;
        while (stc < sech.mStations.size()) {
          combinedBottles.removeAllElements();
          Station currStn = (Station)sech.mStations.elementAt(stc++);
          // add currStn Bottles
          for (int csb = 0; csb < currStn.mNumBottles; csb++) {
            Bottle bh = (Bottle)currStn.mBottles.elementAt(csb);
            combinedBottles.addElement(new Bottle(bh));
          }

          int numMerged = 0;
          Station nextStn = null;
          try {
            nextStn = (Station)sech.mStations.elementAt(stc);
          }
          catch (Exception ex) {
            nextStn = null;
          }
          while (nextStn != null && currStn.mStnNum.equalsIgnoreCase(nextStn.mStnNum)) {
            numMerged++;
            for (int nsb = 0; nsb < nextStn.mNumBottles; nsb++) {
              Bottle bh = (Bottle)nextStn.mBottles.elementAt(nsb);
              combinedBottles.addElement(new Bottle(bh));
            }
            try {
              nextStn = (Station)sech.mStations.elementAt(++stc);
            }
            catch (Exception ex) {
              nextStn = null;
            }
          }

          if (numMerged > 0) {
            // sort combined bottles by pressure
            for (int i = 0; i < combinedBottles.size() - 1; i++) {
              Bottle bh = (Bottle)combinedBottles.elementAt(i);
              for (int j = i + 1; j < combinedBottles.size(); j++) {
                Bottle bhp1 = (Bottle)combinedBottles.elementAt(j);
                double pres = bh.mDValues[pPos];
                double presp1 = bhp1.mDValues[pPos];

                if (pres > presp1) {
                  // swap the bottles
                  // bottle quality
                  short shtemp = bh.mQualityFlag;
                  bh.mQualityFlag = bhp1.mQualityFlag;
                  bhp1.mQualityFlag = shtemp;
                  
                  int itemp = bh.getSampNo();
                  bh.setSampNo(bhp1.getSampNo());
                  bhp1.setSampNo(itemp);
                  
                  float ftemp = bh.getRawCTD();
                  bh.setRawCTD(bhp1.getRawCTD());
                  bhp1.setRawCTD(ftemp);
                  
                  itemp = bh.getBottleNum();
                  bh.setBottleNum(bhp1.getBottleNum());
                  bhp1.setBottleNum(itemp);
                  
                  boolean booltemp = bh.isRawCTDMeasured();
                  bh.setRawCTDMeasured(bhp1.isRawCTDMeasured());
                  bhp1.setRawCTDMeasured(booltemp);
                  
                  booltemp = bh.isSampNoUsed();
                  bh.setSampNoUsed(bhp1.isSampNoUsed());
                  bhp1.setSampNoUsed(booltemp);
                  
                  booltemp = bh.isBottleNumUsed();
                  bh.setBottleNumUsed(bhp1.isBottleNumUsed());
                  bhp1.setBottleNumUsed(booltemp);

                  // variable and qual code values
                  for (int v = 0; v < bh.mNumVars; v++) {
                    short tqc = bh.mQualityFlags[v];
                    bh.mQualityFlags[v] = bhp1.mQualityFlags[v];
                    bhp1.mQualityFlags[v] = tqc;

                    float tval = bh.mDValues[v];
                    bh.mDValues[v] = bhp1.mDValues[v];
                    bhp1.mDValues[v] = tval;
                  }
                }
              }
            }
          }

          // average bottles at same pressure
          if (avg) {
            Vector<Bottle> averagedBottles = new Vector<Bottle>(100);
            double[] varSums = new double[100];
            int[] varCounts = new int[100];

            for (int i = 0; i < combinedBottles.size() - 1; i++) {
              Bottle bh = combinedBottles.elementAt(i);
              Bottle bhp1 = combinedBottles.elementAt(i + 1);
              double pres = bh.mDValues[pPos];
              double pres2 = bhp1.mDValues[pPos];

              // collect up other bottles at this pressure
              int cnt = 0;
              if (pres == pres2) {
                // initialize
                for (int ii = 0; ii < 100; ii++) {
                  varSums[ii] = 0.0;
                  varCounts[ii] = 0;
                }

                // initialize the sums with the first bottle values
                cnt++;
                for (int p = 0; p < bh.mNumVars; p++) {
                  if (bh.mDValues[p] != JOAConstants.MISSINGVALUE) {
                    varSums[p] += bh.mDValues[p];
                    varCounts[p]++;
                  }
                }

                // stay in this loop until no more matching values
                while (true) {
                  // sum up the parameter values
                  for (int p = 0; p < bh.mNumVars; p++) {
                    if (bhp1.mDValues[p] != JOAConstants.MISSINGVALUE) {
                      varSums[p] += bhp1.mDValues[p];
                      varCounts[p]++;
                    }
                  }

                  // go to the next bottle
                  i++;
                  if (i + 1 > combinedBottles.size() - 1) {
                    break;
                  }
                  bhp1 = (Bottle)combinedBottles.elementAt(i + 1);
                  pres2 = bhp1.mDValues[pPos];

                  if (pres2 != pres) {
                    break;
                  }
                  cnt++;
                }
              }

              if (cnt > 0) {
                // compute average bottle
                for (int p = 0; p < bh.mNumVars; p++) {
                  if (varCounts[p] > 0) {
                    bh.mDValues[p] = (float)(varSums[p] / (double)varCounts[p]);
                    
                    if (sech.getQCStandard() == JOAConstants.WOCE_QC_STD) {
                    	bh.mQualityFlags[p] = 6;
                    }
                    else if (sech.getQCStandard() == JOAConstants.IGOSS_QC_STD) {
                    	bh.mQualityFlags[p] = 6;
                    }
                    else {
                    	// do nothing
                    }
                  }
                }
                averagedBottles.addElement(bh);
              }
              else {
                // just assign the combined bottle to averaged bottle
                averagedBottles.addElement(bh);
              }
            }

            // copy the averaged bottle back into combined bottles
            combinedBottles = null;
            combinedBottles = new Vector<Bottle>(100);
            for (int i = 0; i < averagedBottles.size(); i++) {
              Bottle bh = averagedBottles.elementAt(i);
              combinedBottles.addElement(bh);
            }
          }

          //assign new ordinals to bottles
          int ord = 0;
          for (int i = 0; i < combinedBottles.size(); i++) {
            Bottle bh = combinedBottles.elementAt(i);
            bh.mOrdinal = ++ord;
          }

          // make a new station for combined casts;
          Station newsh;
          String ship = null;
          if (currStn.mShipCode != null) {
            ship = new String(currStn.mShipCode);
          }
          String stn = null;
          if (currStn.mStnNum != null) {
            stn = new String(currStn.mStnNum);
          }
          String origName = null;
          if (currStn.mOriginalName != null) {
            origName = new String(currStn.mOriginalName);
          }
          String origPath = null;
          if (currStn.mOriginalName != null) {
            origPath = new String(currStn.mOriginalPath);
          }
          int castNum = currStn.mCastNum;
          if (numMerged > 0) {
            castNum = 99;
          }
          newsh = new Station(++stnOrd, ship, stn, castNum, currStn.mLat, currStn.mLon, combinedBottles.size(),
                              currStn.mYear, currStn.mMonth, currStn.mDay, currStn.mHour, currStn.mMinute,
                              currStn.mBottomDepthInDBARS, currStn.mVarFlag, origName, origPath);
          if (currStn.getType() == null) {
            newsh.setType("BOTTLE");
          }
          else {
            newsh.setType(currStn.getType());
          }

          // add the sorted bottles to the station
          for (int i = 0; i < combinedBottles.size(); i++) {
            Bottle bh = (Bottle)combinedBottles.elementAt(i);
            newsh.mBottles.addElement(bh);
          }

          SectionStation secStn = new SectionStation(newsh, sech, ++ord);
          mFoundStns.addElement(secStn);
        }
      }

      // make a new file viewer
      Frame fr = new Frame();
      FileViewer ff = new FileViewer(fr, mFoundStns, mSectionNameField.getText(), mFileViewerRef, false);
      PowerOceanAtlas.getInstance().addOpenFileViewer(ff);
      ff.pack();
      ff.setVisible(true);
      ff.requestFocus();
      ff.setSavedState(JOAConstants.CREATEDONTHEFLY, null);
    }

    // issue an event to cause all windows to update
    mFileViewerRef.updateAfterSectionManager();
  }

  public void sortCasts() {
    // first create a vector to hold new stations
    mFoundStns = new Vector<SectionStation>(200);

    // stuff the existing stations into this Vector
    int ord = 0;
    for (int fc = 0; fc < mFileViewerRef.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewerRef.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = of.getSection(sec);
        if (sech.mNumCasts == 0) {
          continue;
        }

        for (int stc = 0; stc < sech.mStations.size(); stc++) {
          Station sh = (Station)sech.mStations.elementAt(stc);
          if (!sh.mUseStn) {
            continue;
          }

          mFoundStns.addElement(new SectionStation(sh, sech, ord++));
        }
      }
    }
  }
  
  public void cloneIt() {
    try {
      JOAConstants.LogFileStream.writeBytes(mFileViewerRef.getTitle() + ": Cloned");
      JOAConstants.LogFileStream.writeBytes("\n");
      JOAConstants.LogFileStream.flush();
    }
    catch (Exception ex) {
    	ex.printStackTrace();
    }

    for (int fc = 0; fc < mFileViewerRef.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewerRef.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = of.getSection(sec);
        if (sech.mNumCasts == 0) {
          continue;
        }

        Vector<Bottle> combinedBottles = new Vector<Bottle>(100);
        int stc = 0;
        while (stc < sech.mStations.size()) {
          combinedBottles.removeAllElements();
          Station currStn = (Station)sech.mStations.elementAt(stc++);
          // add currStn Bottles
          for (int csb = 0; csb < currStn.mNumBottles; csb++) {
            Bottle bh = (Bottle)currStn.mBottles.elementAt(csb);
            combinedBottles.addElement(new Bottle(bh));
          }

          //assign new ordinals to bottles
          int ord = 0;
          for (int i = 0; i < combinedBottles.size(); i++) {
            Bottle bh = combinedBottles.elementAt(i);
            bh.mOrdinal = ++ord;
          }

          // make a new station for combined casts;
          Station newsh;
          newsh = new Station(currStn);
          if (currStn.getType() == null) {
            newsh.setType("BOTTLE");
          }
          else {
            newsh.setType(currStn.getType());
          }

          // add the sorted bottles to the station
          for (int i = 0; i < combinedBottles.size(); i++) {
            Bottle bh = (Bottle)combinedBottles.elementAt(i);
            newsh.mBottles.addElement(bh);
          }

          SectionStation secStn = new SectionStation(newsh, sech, ++ord);
          mFoundStns.addElement(secStn);
        }
      }

      // make a new file viewer
      Frame fr = new Frame();
      FileViewer ff = new FileViewer(fr, mFoundStns, mSectionNameField.getText(), mFileViewerRef, false);
      PowerOceanAtlas.getInstance().addOpenFileViewer(ff);
      ff.pack();
      ff.setVisible(true);
      ff.requestFocus();
      ff.setSavedState(JOAConstants.CREATEDONTHEFLY, null);
    }

    // issue an event to cause all windows to update
    mFileViewerRef.updateAfterSectionManager();
  }

  public void mergeSections() {
    try {
      JOAConstants.LogFileStream.writeBytes(mFileViewerRef.getTitle() + ": Merged all sections" + "\n");
      JOAConstants.LogFileStream.flush();
    }
    catch (Exception ex) {}

    // first create a vector to hold new stations
    mFoundStns = new Vector<SectionStation>(200);

    // build up a description, ship, numcasts, numVars of combined section
    int numCasts = 0;
    int qcStd = 0;
    for (int fc = 0; fc < mFileViewerRef.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewerRef.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = of.getSection(sec);
        if (sec == 0) {
          qcStd = sech.getQCStandard();
        }
        if (sech.mNumCasts == 0) {
          continue;
        }

        for (int stc = 0; stc < sech.mStations.size(); stc++) {
          numCasts++;
        }
      }
    }

    // create a new section object for all the stations
    Section newSec = new Section(1, mSectionNameField.getText(), "merged", numCasts, mFileViewerRef.getNumProperties());

    // add the properties to this new Section
    for (int i = 0; i < mFileViewerRef.gNumProperties; i++) {
      newSec.addNewVarToSection(mFileViewerRef.mAllProperties[i].getVarLabel(), mFileViewerRef.mAllProperties[i].getUnits());
    }

    newSec.mNumProperties = mFileViewerRef.getNumProperties();
    newSec.setQCStandard(qcStd);

    // stuff the existing stations into this Vector
    int ord = 0;
    for (int fc = 0; fc < mFileViewerRef.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewerRef.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = of.getSection(sec);
        if (sech.mNumCasts == 0) {
          continue;
        }

        for (int stc = 0; stc < sech.mStations.size(); stc++) {
          Station sh = (Station)sech.mStations.elementAt(stc);
          if (!sh.mUseStn) {
            continue;
          }

          SectionStation newStn = new SectionStation();
          if (sh.mShipCode != null) {
            newStn.mShipCode = new String(sh.mShipCode);
          }
          newStn.mOrdinal = ord++;
          newStn.mUseStn = sh.mUseStn;
          newStn.mLat = sh.mLat;
          newStn.mLon = sh.mLon;
          newStn.mYear = sh.mYear;
          newStn.mMonth = sh.mMonth;
          newStn.mDay = sh.mDay;
          newStn.mHour = sh.mHour;
          newStn.mMinute = sh.mMinute;
          newStn.mStnNum = sh.mStnNum;
          newStn.mCastNum = sh.mCastNum;
          newStn.mNumBottles = sh.mNumBottles;
          newStn.mBottomDepthInDBARS = sh.mBottomDepthInDBARS;
          newStn.mVarFlag = sh.mVarFlag;
          newStn.mCurrColor = sh.mCurrColor;
          newStn.mCurrSymbolSize = sh.mCurrSymbolSize;
          if (sh.mDataType != null) {
            newStn.mDataType = new String(sh.mDataType);
          }
          newStn.mHilitedOnMap = sh.mHilitedOnMap;
          if (sh.mOriginalPath != null) {
            newStn.mOriginalPath = sh.mOriginalPath;
          }
          if (sh.mOriginalName != null) {
            newStn.mOriginalName = sh.mOriginalName;
          }

          int botOrd = 0;
          for (int i = 0; i < sh.getNumBottles(); i++) {
            Bottle oldBot = (Bottle)sh.mBottles.elementAt(i);

            // make a new bottle
            Bottle newBot = new Bottle(botOrd++, mFileViewerRef.gNumProperties, newStn, sech);
            newBot.setRawCTDMeasured(oldBot.isRawCTDMeasured());
            newBot.setBottleNumUsed(oldBot.isBottleNumUsed());
            newBot.setSampNoUsed(oldBot.isSampNoUsed());
            
            if (oldBot.isRawCTDMeasured()) {
            	newBot.setRawCTD(oldBot.getRawCTD());
            }
            
            if (oldBot.isBottleNumUsed()) {
            	newBot.setBottleNum(oldBot.getBottleNum());
            }
            
            if (oldBot.isSampNoUsed()) {
            	newBot.setSampNo(oldBot.getSampNo());
            }

            for (int p = 0; p < mFileViewerRef.gNumProperties; p++) {
              newBot.mDValues[p] = JOAConstants.MISSINGVALUE;
              newBot.mQualityFlags[p] = (short)JOAConstants.MISSINGVALUE;
              newBot.mQCValuesEdited[p] = false;
              newBot.mQCValueAssigned[p] = false;
            }

            // copy the old bottle values into it
            // loop on the variables and get the position of it in the current section
            for (int p = 0; p < mFileViewerRef.gNumProperties; p++) {
              int paramPosSrc = sech.getVarPos(mFileViewerRef.mAllProperties[p].getVarLabel(), false);
              int paramPosDest = newSec.getVarPos(mFileViewerRef.mAllProperties[p].getVarLabel(), false);
              if (paramPosDest >= 0 && paramPosSrc >= 0) {
                newBot.mDValues[paramPosDest] = oldBot.mDValues[paramPosSrc];
                newBot.mQualityFlags[paramPosDest] = oldBot.mQualityFlags[paramPosSrc];
                newBot.mQCValuesEdited[paramPosDest] = oldBot.mQCValuesEdited[paramPosSrc];
                newBot.mQCValueAssigned[paramPosDest] = oldBot.mQCValueAssigned[paramPosSrc];
              }
            }
            newStn.mBottles.addElement(newBot);
          }

          mFoundStns.addElement(newStn);
        }
      }
    }

    // make a new file viewer
    Frame fr = new Frame();
    FileViewer ff = new FileViewer(fr, mFoundStns, mSectionNameField.getText(), newSec, mFileViewerRef);
    PowerOceanAtlas.getInstance().addOpenFileViewer(ff);
    ff.pack();
    ff.setVisible(true);
    ff.requestFocus();
    ff.setSavedState(JOAConstants.CREATEDONTHEFLY, null);
  }

  public void renameSection() {
    String newName = mSectionNameField.getText();

    if (newName != null && newName.length() > 0) {
      mFileViewerRef.resetTitle(newName);
    }
  }
}

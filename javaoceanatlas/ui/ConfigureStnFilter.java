/*
 * $Id: ConfigureStnFilter.java,v 1.9 2005/09/07 18:49:29 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import javax.swing.border.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.events.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.filters.*;
import javaoceanatlas.specifications.*;

@SuppressWarnings("serial")
public class ConfigureStnFilter extends JOAJDialog implements ActionListener, ButtonMaintainer, ItemListener,
    ParameterAddedListener, DataAddedListener, ListSelectionListener {
  protected FileViewer mFileViewer;
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mApplyButton = null;
  protected JOAJButton mCancelButton = null;
  protected JOAJButton mNoneButton = null;
  protected JOAJButton mNoneStnsButton = null;
  protected JOAJTextField mMinLatField = null;
  protected JOAJTextField mMaxLatField = null;
  protected JOAJTextField mMinLonField = null;
  protected JOAJTextField mMaxLonField = null;
  protected JOAJRadioButton mExcludeStns = null;
  protected JOAJRadioButton mIncludeStns = null;
  protected StationFilter mStnFilter = null;
  protected JOAJList mSecList = null;
  protected JOAJList mStnList = null;
  protected JOAJList mParamList = null;
  protected MapSpecification mMapSpec = null;
  protected SimpleMapPlotPanel mMap = null;
  protected boolean mIgnore = false;
  protected int mTotalStns = 0;
  protected int mTotalSecs = 0;
	private Timer timer = new Timer();
  protected SmallIconButton checkAll = null;
  protected SmallIconButton checkNone = null;
  private double mLastMinLat = -9999, mLastMaxLat = -9999, mLastLeftLon = -9999, mLastRtLon = -9999;

  public ConfigureStnFilter(JOAWindow par, FileViewer fv, StationFilter stn) {
    super(par, "Station Filters", false);
    mFileViewer = fv;
    if (stn == null) {
      mStnFilter = new StationFilter(fv);
      mStnFilter.setMinLat( -9999);
      mStnFilter.setMinLon( -9999);
      mStnFilter.setMaxLat( -9999);
      mStnFilter.setMaxLon( -9999);
      mStnFilter.setExcludeStns(true);
    }
    else {
      mStnFilter = stn;
    }
    this.init();
  }

  private void buildParamList() {
    Vector<String> listData = new Vector<String>();
    for (int i = 0; i < mFileViewer.gNumProperties; i++) {
      listData.addElement(mFileViewer.mAllProperties[i].getVarLabel());
    }

    if (mParamList == null) {
      mParamList = new JOAJList(listData);
    }
    else {
      mParamList.setListData(listData);
      mParamList.invalidate();
    }
  }

  private void buildStnList() {
    Vector<String> listData = new Vector<String>();
    for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = (Section)of.mSections.elementAt(sec);
        for (int stc = 0; stc < sech.mStations.size(); stc++) {
          Station sh = (Station)sech.mStations.elementAt(stc);
          String stnStr = sech.mSectionDescription + ":" + sh.mStnNum + "/" + sh.mCastNum + " " +JOAFormulas.formatLat(sh.mLat, 2) + " " +
          JOAFormulas.formatLon(sh.mLon, 2) + "," + JOAFormulas.formatDate(sh, false);
          //stnStr = JOAFormulas.returnMiddleTruncatedString(stnStr, 43);
          listData.addElement(stnStr);
          mTotalStns++;
        }
      }
    }

    if (mStnList == null) {
      mStnList = new JOAJList(listData);
    }
    else {
      mStnList.setListData(listData);
      mStnList.invalidate();
    }
  }

  private void buildSecList() {
    Vector<String> listData = new Vector<String>();
    for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = (Section)of.mSections.elementAt(sec);
        listData.addElement(sech.mSectionDescription);
        mTotalSecs++;
      }
    }

    if (mSecList == null) {
      mSecList = new JOAJList(listData);
    }
    else {
      mSecList.setListData(listData);
      mSecList.invalidate();
    }
  }

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    // fill the sec list
    buildSecList();

    // fill the stn list
    buildStnList();

    // fill the parameter list
    buildParamList();

    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(0, 0));

    JPanel criteriaCont = new JPanel();
    criteriaCont.setLayout(new BorderLayout(5, 5));

    JPanel leftCriteriaCont = new JPanel();
    leftCriteriaCont.setLayout(new GridLayout(2, 1, 5, 5));

    JPanel stnSelPanel = new JPanel();
    stnSelPanel.setLayout(new BorderLayout(5, 0));

    JPanel stnListsPanel = new JPanel();
    stnListsPanel.setLayout(new RowLayout(Orientation.LEFT, Orientation.CENTER, 2));

    JPanel paramSelPanel = new JPanel();
    paramSelPanel.setLayout(new GridLayout(1, 2, 5, 5));

    JPanel locationSelPanel = new JPanel();
    locationSelPanel.setLayout(new BorderLayout(5, 0));

    //location
    JPanel crContPanel = new JPanel();
    JPanel crRegionPanel = new JPanel();
    crContPanel.setLayout(new BorderLayout(0, 0));
    crRegionPanel.setLayout(new GridLayout(3, 3));
    crRegionPanel.add(new JOAJLabel(" "));

    JPanel topLat = new JPanel();
    topLat.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    if (mStnFilter.getMaxLat() == -9999) {
      mMaxLatField = new JOAJTextField(5);
    }
    else {
      mMaxLatField = new JOAJTextField(7);
      mMaxLatField.setText(JOAFormulas.formatDouble(String.valueOf(mStnFilter.getMaxLat()), 2, true));
    }
    mMaxLatField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mMaxLatField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        updateMapRegion();
      }
    });

    mMaxLatField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent me) {
        updateMapRegion();
      }
    });

    topLat.add(mMaxLatField);
    topLat.add(new JOAJLabel("T"));
    crRegionPanel.add(topLat);
    crRegionPanel.add(new JOAJLabel(" "));

    JPanel leftLon = new JPanel();
    leftLon.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    if (mStnFilter.getMinLon() == -9999) {
      mMinLonField = new JOAJTextField(6);
    }
    else {
      mMinLonField = new JOAJTextField(7);
      mMinLonField.setText(JOAFormulas.formatDouble(String.valueOf(mStnFilter.getMinLon()), 2, true));
    }
    mMinLonField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mMinLonField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        updateMapRegion();
      }
    });

    mMinLonField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent me) {
        updateMapRegion();
      }
    });
    leftLon.add(mMinLonField);
    leftLon.add(new JOAJLabel("L"));
    crRegionPanel.add(leftLon);
    crRegionPanel.add(new JOAJLabel(" "));

    JPanel rightLon = new JPanel();
    rightLon.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    if (mStnFilter.getMaxLon() == -9999) {
      mMaxLonField = new JOAJTextField(6);
    }
    else {
      mMaxLonField = new JOAJTextField(7);
      mMaxLonField.setText(JOAFormulas.formatDouble(String.valueOf(mStnFilter.getMaxLon()), 2, true));
    }
    mMaxLonField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mMaxLonField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        updateMapRegion();
      }
    });

    mMaxLonField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent me) {
        updateMapRegion();
      }
    });
    rightLon.add(mMaxLonField);
    rightLon.add(new JOAJLabel("R"));
    crRegionPanel.add(rightLon);
    crRegionPanel.add(new JOAJLabel(" "));

    JPanel bottLat = new JPanel();
    bottLat.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    if (mStnFilter.getMinLat() == -9999) {
      mMinLatField = new JOAJTextField(5);
    }
    else {
      mMinLatField = new JOAJTextField(7);
      mMinLatField.setText(JOAFormulas.formatDouble(String.valueOf(mStnFilter.getMinLat()), 2, true));
    }
    mMinLatField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mMinLatField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        updateMapRegion();
      }
    });

    mMinLatField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent me) {
        updateMapRegion();
      }
    });
    bottLat.add(mMinLatField);
    bottLat.add(new JOAJLabel("B"));
    crRegionPanel.add(bottLat);
    crRegionPanel.add(new JOAJLabel(" "));
    crContPanel.add("Center", new TenPixelBorder(crRegionPanel, 5, 5, 5, 5));
    TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kLocationFilter"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    locationSelPanel.setBorder(tb);
    locationSelPanel.add(crContPanel, "South");

    // add a map panel
    JPanel mapholder = new JPanel();
    mapholder.setLayout(new BorderLayout(0, 0));
    mapholder.setBackground(Color.white);

    // create a new mapspecification first
    mMapSpec = new MapSpecification();
    mMapSpec.setCoastLineRez(JOAConstants.COARSERESOLUTION);
    initMapSpec();
    ObsMarker mObsMarker = null;
    int mWidth = 300, mHeight = 200, mLegendHeight = 0;
    MapLegend mLegend = null;
    mMap = new SimpleMapPlotPanel(mFileViewer, mMapSpec, mWidth, mHeight, mLegendHeight, mObsMarker, null, null,
                                  mLegend, null, false, 0, 0, 1.0, 1.0);
    mMap.init();
    // initialize the map region
    mMap.setFilterRegion(mStnFilter.getMinLat(), mStnFilter.getMaxLat(), mStnFilter.getMinLon(), mStnFilter.getMaxLon());
    mMap.setTextFlds(mMinLatField, mMaxLatField, mMinLonField, mMaxLonField);
    mMap.setRubberbandDisplayObject(null, false);
    mMap.setToolTipText("Drag rectangle for spatial filter. Double click map to configure");

    mapholder.setBorder(BorderFactory.createEtchedBorder());
    mapholder.add(mMap);
    locationSelPanel.add(new TenPixelBorder(mapholder, 5, 5, 5, 5), "Center");

    // station selection
    JPanel allNoneCont = new JPanel();
    allNoneCont.setLayout(new GridLayout(2, 1, 0, 5));
    checkAll = new SmallIconButton(new ImageIcon(getClass().getResource("images/checkall.gif")));
    allNoneCont.add(checkAll);
    checkNone = new SmallIconButton(new ImageIcon(getClass().getResource("images/checknone.gif")));
    allNoneCont.add(checkNone);
    checkAll.addActionListener(this);
    checkNone.addActionListener(this);
    checkAll.setActionCommand("all");
    checkNone.setActionCommand("none");
    JPanel cont1 = new JPanel();
    cont1.setLayout(new BorderLayout(0, 0));
    cont1.add("North", new TenPixelBorder(allNoneCont, 20, 0, 0, 0));
    stnSelPanel.add(new TenPixelBorder(cont1, 0, 0, 0, 10), "East");
		
		checkAll.setToolTipText("Select all stations in the list");
		checkNone.setToolTipText("Remove selected stations from filter");

    JPanel includeExcludeCont = new JPanel();
    includeExcludeCont.setLayout(new GridLayout(1, 4, 0, 0));
    ButtonGroup bg1 = new ButtonGroup();
    mExcludeStns = new JOAJRadioButton(b.getString("kExcludeStations"), mStnFilter.isExcludeStns());
    mIncludeStns = new JOAJRadioButton(b.getString("kIncludeStations"), !mStnFilter.isExcludeStns());
    bg1.add(mExcludeStns);
    bg1.add(mIncludeStns);
    includeExcludeCont.add(mExcludeStns);
    includeExcludeCont.add(mIncludeStns);
    includeExcludeCont.add(new JOAJLabel(""));
    stnSelPanel.add("South", includeExcludeCont);

    mSecList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    mSecList.setPrototypeCellValue("Atlantic 11 S     ");
    mSecList.setVisibleRowCount(6);
    JScrollPane listScroller1 = new JScrollPane(mSecList);
    stnListsPanel.add(new TenPixelBorder(listScroller1, 10, 5, 0, 0));

    mStnList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    mStnList.setPrototypeCellValue("Atlantic 11 S 210/C (11.25 S 180.45 W, 4/1/1999                        ");
    mStnList.setVisibleRowCount(6);
    JScrollPane listScroller = new JScrollPane(mStnList);
    listScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    JPanel scrollerHolder = new JPanel(new BorderLayout(1,1));
    scrollerHolder.add(BorderLayout.CENTER, listScroller);
    stnListsPanel.add(new TenPixelBorder(scrollerHolder, 10, 5, 0, 0));
    mSecList.addListSelectionListener(this);

    tb = BorderFactory.createTitledBorder(b.getString("kStationSelectionFilter"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    stnListsPanel.setBorder(tb);
    stnSelPanel.add("Center", new TenPixelBorder(stnListsPanel, 10, 0, 0, 0));

    // restore any hilighting to station list
    if (mStnFilter.getStnList() != null && mStnFilter.getStnList().length > 0) {
      mStnList.setSelectedIndices(mStnFilter.getStnList());
    }

    leftCriteriaCont.add(stnSelPanel);

    // param selection
    mParamList.setVisibleRowCount(5);
    mParamList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    mParamList.setPrototypeCellValue("SALT         ");
    // restore any highlighting to param list
    if (mStnFilter.getMissingParams() != null && mStnFilter.getMissingParams().length > 0) {
      mParamList.setSelectedIndices(mStnFilter.getMissingParams());
    }
    JScrollPane listScroller2 = new JScrollPane(mParamList);
    paramSelPanel.add(new TenPixelBorder(listScroller2, 0, 5, 0, 0));

    JTextArea ta = new JTextArea(2, 25);
    ta.setLineWrap(true);
    ta.setBackground(this.getBackground());
    ta.setText(b.getString("kParamFilterHelp"));
    int size = 12;
    if (JOAConstants.ISSUNOS) {
      size = 14;
    }
    ta.setFont(new Font("Courier", Font.PLAIN, size));
    ta.setEditable(false);
    paramSelPanel.add(ta);

    tb = BorderFactory.createTitledBorder(b.getString("kMissingParameterFilter"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    paramSelPanel.setBorder(tb);
    leftCriteriaCont.add(paramSelPanel);

    // lower panel
    mNoneButton = new JOAJButton(b.getString("kRemove"));
    mNoneButton.setActionCommand("removefilter");
    mOKBtn = new JOAJButton(b.getString("kOK"));
    mOKBtn.setActionCommand("ok");
    this.getRootPane().setDefaultButton(mOKBtn);
    mCancelButton = new JOAJButton(b.getString("kClose"));
    mCancelButton.setActionCommand("cancel");
    mApplyButton = new JOAJButton(b.getString("kApply"));
    mApplyButton.setActionCommand("apply");
    JPanel dlgBtnsInset = new JPanel();
    JPanel dlgBtnsPanel = new JPanel();
    dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
    dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
    if (JOAConstants.ISMAC) {
      dlgBtnsPanel.add(mCancelButton);
      dlgBtnsPanel.add(mNoneButton);
      dlgBtnsPanel.add(mApplyButton);
      dlgBtnsPanel.add(mOKBtn);
    }
    else {
      dlgBtnsPanel.add(mOKBtn);
      dlgBtnsPanel.add(mApplyButton);
      dlgBtnsPanel.add(mNoneButton);
      dlgBtnsPanel.add(mCancelButton);
    }
    dlgBtnsInset.add(dlgBtnsPanel);

    mOKBtn.addActionListener(this);
    mApplyButton.addActionListener(this);
    mCancelButton.addActionListener(this);
    mNoneButton.addActionListener(this);
    contents.add(new TenPixelBorder(leftCriteriaCont, 5, 5, 0, 0), "Center");
    contents.add(new TenPixelBorder(locationSelPanel, 5, 0, 0, 5), "East");
    contents.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
    this.pack();

    mFileViewer.addParameterAddedListener(this);
    mFileViewer.addDataAddedListener(this);
    
    runTimer();

    // show dialog at center of screen
    this.pack();
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

  public void parameterAdded(ParameterAddedEvent evt) {
    updateFilter();

    // redo the parameter list
    buildParamList();

    // restore the highlighting
    if (mStnFilter.getMissingParams() != null && mStnFilter.getMissingParams().length > 0) {
      mParamList.setSelectedIndices(mStnFilter.getMissingParams());
    }
  }

  public void dataAdded(DataAddedEvent evt) {
    updateFilter();

    // redo the section list
    buildSecList();

    // redo the station list
    buildStnList();

    // redo stn highlighting
    if (mStnFilter.getStnList() != null && mStnFilter.getStnList().length > 0) {
      mStnList.setSelectedIndices(mStnFilter.getStnList());
    }

    // redo the map
    mMap.forceRedraw(false);
  }

  public void itemStateChanged(ItemEvent evt) {
    if (evt.getSource() instanceof JOAJRadioButton) {
      //if (evt.getStateChange() == ItemEvent.SELECTED && rb == mShowOnly) {
    }
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      timer.cancel();
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      if (!updateFilter()) {
        mFileViewer.updateStnFilter(mStnFilter);
      }
      timer.cancel();
      this.dispose();
    }
    else if (cmd.equals("apply")) {
      if (!updateFilter()) {
        mFileViewer.updateStnFilter(mStnFilter);
        mMap.forceRedraw(false);
      }
    }
    else if (cmd.equals("removefilter")) {
      mStnFilter.setMinLat( -9999);
      mStnFilter.setMinLon( -9999);
      mStnFilter.setMaxLat( -9999);
      mStnFilter.setMaxLon( -9999);
      mStnFilter.setExcludeStns(true);
      mExcludeStns.setSelected(mStnFilter.isExcludeStns());
      mIncludeStns.setSelected(!mStnFilter.isExcludeStns());
      mFileViewer.updateStnFilter(null);
      mMap.resetMap();
      mStnList.clearSelection();
      mParamList.clearSelection();
      mMinLatField.setText("");
      mMaxLatField.setText("");
      mMinLonField.setText("");
      mMaxLonField.setText("");
    }
    else if (cmd.equals("none")) {
      checkNone.setSelected(false);
      // unselect all selected stations
      mStnList.clearSelection();
      mSecList.clearSelection();
    }
    else if (cmd.equals("all")) {
      checkAll.setSelected(false);
      // select all stations in list
      //mStnList.setSelectionInterval(0, 100000);
      int[] indices = new int[mTotalSecs];
      for (int i = 0; i < mTotalSecs; i++) {
        indices[i] = i;
      }
      mSecList.setSelectedIndices(indices);
    }
  }

  public boolean updateFilter() {
    boolean error = false;

    // station filter
    mStnFilter.setCriteria1Active(false);
    if (mStnList.getSelectedIndex() >= 0) {
      mStnFilter.setCriteria1Active(true);
      //	if (mStnFilter.getStnList() != null)
      //		mStnFilter.setStnList(0, null);
      mStnFilter.setStnList(mStnList.getSelectedIndices().length, mStnList.getSelectedIndices());
      mStnFilter.setExcludeStns(mExcludeStns.isSelected());

      //	if (mStnFilter.getStnKeepList != null)
      //		mStnFilter.mStnKeepList = null;
      mStnFilter.setStnKeepList(mTotalStns);
      for (int i = 0; i < mTotalStns; i++) {
        if (mStnFilter.isExcludeStns()) {
          mStnFilter.setStnKeepList(i, true);
        }
        else {
          mStnFilter.setStnKeepList(i, false);
        }
      }

      for (int i = 0; i < mStnFilter.getStnList().length; i++) {
        if (mStnFilter.isExcludeStns()) {
          mStnFilter.setStnKeepList(mStnFilter.getStnList(i), false);
        }
        else {
          mStnFilter.setStnKeepList(mStnFilter.getStnList(i), true);
        }
      }
    }

    // parameter filter
    mStnFilter.setCriteria2Active(false);
    if (mParamList.getSelectedIndex() >= 0) {
      mStnFilter.setCriteria2Active(true);
      //if (mStnFilter.mMissingParams != null)
      //	mStnFilter.mMissingParams = null;
      //mStnFilter.mMissingParams = new int[];
      mStnFilter.setMissingParams(mParamList.getSelectedIndices().length, mParamList.getSelectedIndices());
    }

    // location filter
    mStnFilter.setCriteria3Active(false);
    String fldText = mMinLatField.getText();
    if (fldText.length() > 0) {
      try {
        mStnFilter.setMinLat(Double.valueOf(mMinLatField.getText()).doubleValue());
        mStnFilter.setCriteria3Active(true);
      }
      catch (NumberFormatException ex) {
        mMinLatField.setText("err");
        error = true;
      }
    }
    else {
      ; //mMinLatField.setText(JOAFormulas.formatDouble(String.valueOf(minLat), 3, true));
    }

    fldText = mMaxLatField.getText();
    if (fldText.length() > 0) {
      try {
        mStnFilter.setMaxLat(Double.valueOf(mMaxLatField.getText()).doubleValue());
        mStnFilter.setCriteria3Active(true);
      }
      catch (NumberFormatException ex) {
        mMaxLatField.setText("err");
        error = true;
      }
    }
    else {
      ; //mMaxLatField.setText(JOAFormulas.formatDouble(String.valueOf(maxLat), 3, true));
    }

    fldText = mMinLonField.getText();
    if (fldText.length() > 0) {
      try {
        mStnFilter.setMinLon(Double.valueOf(mMinLonField.getText()).doubleValue());
        mStnFilter.setCriteria3Active(true);
      }
      catch (NumberFormatException ex) {
        mMinLonField.setText("err");
        error = true;
      }
    }
    else {
      ; //mMinLonField.setText(JOAFormulas.formatDouble(String.valueOf(leftLon), 3, true));
    }

    fldText = mMaxLonField.getText();
    if (fldText.length() > 0) {
      try {
        mStnFilter.setMaxLon(Double.valueOf(mMaxLonField.getText()).doubleValue());
        mStnFilter.setCriteria3Active(true);
      }
      catch (NumberFormatException ex) {
        mMaxLonField.setText("err");
        error = true;
      }
    }
    else {
      ; //mMaxLonField.setText(JOAFormulas.formatDouble(String.valueOf(rightLon), 3, true));
    }

    return error;
  }

  public void maintainButtons() {
    //if (mFileViewer.mStnFilterActive)
    //	mNoneButton.setEnabled(true);
    //else
    //	mNoneButton.setEnabled(false);
  }

  public void initMapSpec() {
    mMapSpec.setProjection(JOAConstants.MILLERPROJECTION);
    mMapSpec.setConnectStns(false);
    mMapSpec.setSymbolSize(3);
    mMapSpec.setPlotStnSymbols(true);
    mMapSpec.setLineWidth(1);
    mMapSpec.setLatMax(90.0);
    mMapSpec.setLatMin( -90.0);
    mMapSpec.setLonRt(19.99);
    mMapSpec.setLonLft(20.0);
    mMapSpec.setCenLat(0.0);
    mMapSpec.setCenLon( -180 + mMapSpec.getLonRt());
    mMapSpec.setDrawGraticule(true);
    mMapSpec.setLatGratSpacing(30.0);
    mMapSpec.setLonGratSpacing(30.0);
    mMapSpec.setBGColor(Color.white);
    mMapSpec.setCoastColor(Color.black);
    mMapSpec.setGratColor(new Color(0, 50, 50));
    mMapSpec.setSectionColor(Color.red);
    mMapSpec.setCurrBasin(8);
    mMapSpec.setRetainProjAspect(false);
    mMapSpec.setStnColorMode(MapSpecification.COLOR_STNS_BY_JOADEFAULT);
    mMapSpec.setSymbol(JOAConstants.SYMBOL_SQUAREFILLED);
    mMapSpec.setPlotSectionLabels(true);
    mMapSpec.setPlotGratLabels(true);
    mMapSpec.setLabelColor(Color.black);
    mMapSpec.setColorFill(true);
    mMapSpec.setGlobe(false);
    mMapSpec.setEtopoFile("etopo60.nc");
    try {
      NewColorBar cb = new NewColorBar(JOAFormulas.getColorBar("ROSE-map-like_cbr.xml"));
      mMapSpec.setBathyColorBar(cb);
    }
    catch (Exception ex) {
      mMapSpec.setColorFill(false);
    }
  }

  public void updateMapRegion() {
    double minLat = -90, maxLat = 90, leftLon = -180, rightLon = 180;
    boolean error = false;
    boolean changed = false;
    changed = false;
    String fldText = mMinLatField.getText();
    if (fldText.length() > 0) {
      try {
        minLat = Double.valueOf(mMinLatField.getText()).doubleValue();
        if (mLastMinLat == -9999) {
          mLastMinLat = minLat;
        }
        double diff = Math.abs(minLat - mLastMinLat);
        if (diff > 1.0e-5) {
          changed = true;
        }
        mLastMinLat = minLat;
      }
      catch (NumberFormatException ex) {
        mMinLatField.setText("err");
        error = true;
      }
    }
    else {
      mMinLatField.setText(JOAFormulas.formatDouble(String.valueOf(minLat), 3, true));
    }

    fldText = mMaxLatField.getText();
    if (fldText.length() > 0) {
      try {
        maxLat = Double.valueOf(mMaxLatField.getText()).doubleValue();
        if (mLastMaxLat == -9999) {
          mLastMaxLat = maxLat;
        }
        double diff = Math.abs(maxLat - mLastMaxLat);
        if (diff > 1.0e-5) {
          changed = true;
        }
        mLastMaxLat = maxLat;
      }
      catch (NumberFormatException ex) {
        mMaxLatField.setText("err");
        error = true;
      }
    }
    else {
      mMaxLatField.setText(JOAFormulas.formatDouble(String.valueOf(maxLat), 3, true));
    }

    fldText = mMinLonField.getText();
    if (fldText.length() > 0) {
      try {
        leftLon = Double.valueOf(mMinLonField.getText()).doubleValue();
        if (mLastLeftLon == -9999) {
          mLastLeftLon = leftLon;
        }
        double diff = Math.abs(leftLon - mLastLeftLon);
        if (diff > 1.0e-5) {
          changed = true;
        }
        mLastLeftLon = leftLon;
      }
      catch (NumberFormatException ex) {
        mMinLonField.setText("err");
        error = true;
      }
    }
    else {
      mMinLonField.setText(JOAFormulas.formatDouble(String.valueOf(leftLon), 3, true));
    }

    fldText = mMaxLonField.getText();
    if (fldText.length() > 0) {
      try {
        rightLon = Double.valueOf(mMaxLonField.getText()).doubleValue();
        if (mLastRtLon == -9999) {
          mLastRtLon = rightLon;
        }
        double diff = Math.abs(rightLon - mLastRtLon);
        if (diff > 1.0e-5) {
          changed = true;
        }
        mLastRtLon = rightLon;
      }
      catch (NumberFormatException ex) {
        mMaxLonField.setText("err");
        error = true;
      }
    }
    else {
      mMaxLonField.setText(JOAFormulas.formatDouble(String.valueOf(rightLon), 3, true));
    }

    if (!error && changed) {
      mMap.setFilterRegion(minLat, maxLat, leftLon, rightLon);
      mMap.forceRedraw(false);
    }
  }

  public void valueChanged(ListSelectionEvent evt) {
    Object[] mSelSec = mSecList.getSelectedValues();
    hiliteSectionStns(mSelSec);
  }

  private void hiliteSectionStns(Object[] theSections) {
    int count = 0;
    int hits = 0;
    int[] indices;
    ArrayList<Integer> al = new ArrayList<Integer>(100);
    for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = (Section)of.mSections.elementAt(sec);
        for (int stc = 0; stc < sech.mStations.size(); stc++) {
          for (int s = 0; s < theSections.length; s++) {
            if (sech.mSectionDescription.equalsIgnoreCase((String)theSections[s])) {
              al.add(new Integer(count));
              hits++;
            }
          }
          count++;
        }
      }
    }
    indices = new int[hits];
    for (int i = 0; i < hits; i++) {
      indices[i] = ((Integer)al.get(i)).intValue();
    }
    mStnList.setSelectedIndices(indices);
  }
}

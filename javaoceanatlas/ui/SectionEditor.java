/*
 * $Id: SectionEditor.java,v 1.4 2005/06/17 18:08:55 oz Exp $
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
import gov.noaa.pmel.util.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.*;

@SuppressWarnings("serial")
public class SectionEditor extends CloseableFrame implements ActionListener, ButtonMaintainer, ItemListener,
    ListSelectionListener {
  public static int SORT_NONE = 1;
  public static int SORT_NTOS = 2;
  public static int SORT_STON = 3;
  public static int SORT_WTOE = 4;
  public static int SORT_ETOW = 5;
  public static int SORT_STN_DSC = 6;
  public static int SORT_STN_ASC = 7;
  public static int SORT_DATE_DSC = 8;
  public static int SORT_DATE_ASC = 9;
  public static int ASCENDING = 1;
  public static int DESCENDING = 2;
  public static double ALN2I = 1.0 / 0.69314718;
  public static double TINY = 1.0E-5;
  private GeoDate date1 = null;
  private GeoDate date2 = null;

  protected FileViewer mFileViewer;
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mCancelButton = null;
  protected SmallIconButton mMoveUpButton = null;
  protected SmallIconButton mMoveTopButton = null;
  protected SmallIconButton mMoveDownButton = null;
  protected SmallIconButton mMoveBottButton = null;
  protected SmallIconButton mDeleteSelection = null;
  protected JOAJButton mSortSelection = null;
  protected JOAJTextField mSectionNameField = null;
  protected JOAJTextField mMaxDateField = null;
  protected JOAJTextField mMinDateField = null;
  protected JOAJTextField mShipsField = null;
  protected JOAJRadioButton mNone = null;
  protected JOAJRadioButton mStoN = null;
  protected JOAJRadioButton mNtoS = null;
  protected JOAJRadioButton mEtoW = null;
  protected JOAJRadioButton mWtoE = null;
  protected JOAJRadioButton mStnDsc = null;
  protected JOAJRadioButton mStnAsc = null;
  protected JOAJRadioButton mDateAsc = null;
  protected JOAJRadioButton mDateDsc = null;
  protected JOAJList mStnList = null;
  protected boolean mIgnore = false;
  protected Vector<SectionStation> mFoundStns = new Vector<SectionStation>();
  protected int mSortDirection = SORT_NONE;
  protected double mLeftLon;
  protected double mRightLon;
  protected MapPlotPanel mSource;
	private Timer timer = new Timer();
  protected String mSuggestedName = "Untitled";

  public SectionEditor(MapPlotPanel source, FileViewer fv, Vector<SectionStation> foundStns, double leftLon, double rightLon) {
    super(fv, "Section Editor", false);
    mSource = source;
    mFileViewer = fv;
    for (int i = 0; i < foundStns.size(); i++) {
      SectionStation st = new SectionStation((SectionStation)foundStns.elementAt(i));
      mFoundStns.addElement(st);
    }
    mLeftLon = leftLon;
    mRightLon = rightLon;
    this.init();
  }

  public SectionEditor(MapPlotPanel source, FileViewer fv, Vector<SectionStation> foundStns, double leftLon, double rightLon,
                       String suggestedName) {
    this(source, fv, foundStns, leftLon, rightLon);
    mSuggestedName = null;
    mSuggestedName = suggestedName;
    mSectionNameField.setText(mSuggestedName);
  }

  private void buildStnList() {
    Vector<String> listData = new Vector<String>();
    for (int i = 0; i < mFoundStns.size(); i++) {
      SectionStation sh = (SectionStation)mFoundStns.elementAt(i);
      listData.addElement(sh.mFoundSection.mSectionDescription + ":" + sh.mStnNum + "(" + JOAFormulas.formatLat(sh.mLat) +
                          " " + JOAFormulas.formatLon(sh.mLon) + ", " + JOAFormulas.formatDate(sh, false) + ")");
    }

    if (mStnList == null) {
      mStnList = new JOAJList(listData);
    }
    else {
      mStnList.setListData(listData);
      mStnList.invalidate();
    }
  }

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    // fill the stn list
    buildStnList();

    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(5, 5));

    JPanel upperCont = new JPanel();
    upperCont.setLayout(new BorderLayout(5, 5));

    JPanel stnSelPanel = new JPanel();
    stnSelPanel.setLayout(new BorderLayout(5, 5));

    /*if (mStnFilter.mMaxLat == -9999)
     mMaxLatField = new JOAJTextField(4);
          else
     mMaxLatField = new JOAJTextField(JOAFormulas.formatDouble(String.valueOf(mStnFilter.mMaxLat), 3, true), 7);
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
       });*/

    // station moveCont
    JPanel allNoneCont = new JPanel();
    allNoneCont.setLayout(new GridLayout(5, 1, 0, 5));
    mMoveTopButton = new SmallIconButton(new ImageIcon(getClass().getResource("images/movetop.gif")));
    allNoneCont.add(mMoveTopButton);
    mMoveUpButton = new SmallIconButton(new ImageIcon(getClass().getResource("images/moveup.gif")));
    allNoneCont.add(mMoveUpButton);
    mMoveDownButton = new SmallIconButton(new ImageIcon(getClass().getResource("images/movedown.gif")));
    allNoneCont.add(mMoveDownButton);
    mMoveBottButton = new SmallIconButton(new ImageIcon(getClass().getResource("images/movebottom.gif")));
    allNoneCont.add(mMoveBottButton);
    mDeleteSelection = new SmallIconButton(new ImageIcon(getClass().getResource("images/trash.gif")));
    allNoneCont.add(mDeleteSelection);

    mMoveTopButton.addActionListener(this);
    mMoveUpButton.addActionListener(this);
    mMoveDownButton.addActionListener(this);
    mMoveBottButton.addActionListener(this);
    mDeleteSelection.addActionListener(this);

    mMoveTopButton.setActionCommand("movetop");
    mMoveUpButton.setActionCommand("moveup");
    mMoveDownButton.setActionCommand("movedown");
    mMoveBottButton.setActionCommand("movebottom");
    mDeleteSelection.setActionCommand("deleteselection");
		
    mMoveTopButton.setToolTipText("Move selected stations to top of station list");
    mMoveUpButton.setToolTipText("Move selected stations up in station list");	
    mMoveDownButton.setToolTipText("Move selected stations down in station list");
    mMoveBottButton.setToolTipText("Move selected stations to bottom of station list");
    mDeleteSelection.setToolTipText("Remove selected stations from station list");

    stnSelPanel.add("East", allNoneCont);

    mStnList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    mStnList.setPrototypeCellValue("Atlantic 11 S 210 (11.25 S 180.45 W, 4/1/1999");
    mStnList.setVisibleRowCount(6);
    mStnList.addListSelectionListener(this);
    JScrollPane listScroller = new JScrollPane(mStnList);
    stnSelPanel.add(new TenPixelBorder(listScroller, 0, 5, 0, 0), "Center");
    TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kSelectedStations"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    stnSelPanel.setBorder(tb);

    upperCont.add(stnSelPanel, "Center");

    // sort panel
    JPanel sortCont = new JPanel();
    sortCont.setLayout(new GridLayout(6, 1, 0, 5));
    ButtonGroup bg1 = new ButtonGroup();
    mNone = new JOAJRadioButton(b.getString("kNone"), true);
    mStoN = new JOAJRadioButton(b.getString("kStoN"));
    mNtoS = new JOAJRadioButton(b.getString("kNtoS"));
    mEtoW = new JOAJRadioButton(b.getString("kEtoW"));
    mWtoE = new JOAJRadioButton(b.getString("kWtoE"));
    mStnDsc = new JOAJRadioButton(b.getString("kStnDsc"));
    mStnAsc = new JOAJRadioButton(b.getString("kStnAsc"));
    mDateDsc = new JOAJRadioButton(b.getString("kDateDsc"));
    mDateAsc = new JOAJRadioButton(b.getString("kDateAsc"));
    mSortSelection = new JOAJButton(b.getString("kSort"));
    bg1.add(mNone);
    bg1.add(mStoN);
    bg1.add(mNtoS);
    bg1.add(mEtoW);
    bg1.add(mWtoE);
    bg1.add(mStnDsc);
    bg1.add(mStnAsc);
    bg1.add(mDateDsc);
    bg1.add(mDateAsc);
    mNone.addItemListener(this);
    mStoN.addItemListener(this);
    mNtoS.addItemListener(this);
    mEtoW.addItemListener(this);
    mWtoE.addItemListener(this);
    mStnDsc.addItemListener(this);
    mStnAsc.addItemListener(this);
    mDateDsc.addItemListener(this);
    mDateAsc.addItemListener(this);
    mSortSelection.addActionListener(this);
    mSortSelection.setActionCommand("sort");
    sortCont.add(mNone);
    sortCont.add(mStoN);
    sortCont.add(mNtoS);
    sortCont.add(mEtoW);
    sortCont.add(mWtoE);
    sortCont.add(mStnDsc);
    sortCont.add(mStnAsc);
    sortCont.add(mDateDsc);
    sortCont.add(mDateAsc);
    sortCont.add(mSortSelection);

    tb = BorderFactory.createTitledBorder(b.getString("kSorting"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    sortCont.setBorder(tb);
    upperCont.add(sortCont, "East");

    // lower upper panel
    JPanel namePanel = new JPanel();
    namePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
    namePanel.add(new JOAJLabel(b.getString("kNewSectionName")));
    mSectionNameField = new JOAJTextField(20);
    mSectionNameField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mSectionNameField.setText(mSuggestedName);
    namePanel.add(mSectionNameField);

    upperCont.add(namePanel, "South");

    // lower panel
    mOKBtn = new JOAJButton(b.getString("kCreate"));
    mOKBtn.setActionCommand("ok");
    this.getRootPane().setDefaultButton(mOKBtn);
    mCancelButton = new JOAJButton(b.getString("kClose"));
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
    contents.add(new TenPixelBorder(upperCont, 5, 5, 5, 5), "North");
    contents.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
    this.pack();

    WindowListener windowListener = new WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        if (mSource != null) {
          mSource.closeSectionEditor();
        }
      }
    };
    this.addWindowListener(windowListener);

    runTimer();
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
    for (int i = 0; i < mFoundStns.size(); i++) {
      SectionStation st = new SectionStation((SectionStation)mFoundStns.elementAt(i));
      st.mFoundStn.mHilitedOnMap = false;
    }

    // get the selection
    int[] selectedIndices = mStnList.getSelectedIndices();
    for (int i = 0; i < selectedIndices.length; i++) {
      SectionStation outCast = (SectionStation)mFoundStns.elementAt(selectedIndices[i]);
      outCast.mFoundStn.mHilitedOnMap = true;
    }
    if (!evt.getValueIsAdjusting()) {
      if (mSource != null) {
        mSource.forceStnRedraw();
      }
    }
  }

  public void itemStateChanged(ItemEvent evt) {
    if (evt.getSource() instanceof JOAJRadioButton) {
      JOAJRadioButton rb = (JOAJRadioButton)evt.getSource();
      if (evt.getStateChange() == ItemEvent.SELECTED && rb == mNone) {
        mSortDirection = SORT_NONE;
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mStoN) {
        mSortDirection = SORT_STON;
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mNtoS) {
        mSortDirection = SORT_NTOS;
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mEtoW) {
        mSortDirection = SORT_ETOW;
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mWtoE) {
        mSortDirection = SORT_WTOE;
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mStnDsc) {
        mSortDirection = SORT_STN_DSC;
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mStnAsc) {
        mSortDirection = SORT_STN_ASC;
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mDateDsc) {
        mSortDirection = SORT_DATE_DSC;
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mDateAsc) {
        mSortDirection = SORT_DATE_ASC;
      }
    }
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      if (mSource != null) {
        mSource.closeSectionEditor();
      }
      timer.cancel();
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      // create a new section
      Frame fr = new Frame();
      String name = mSectionNameField.getText();
      if (name != null && name.length() == 0) {
        name = "Untitled Section";
      }
      FileViewer ff = new FileViewer(fr, mFoundStns, name, mFileViewer, false);
      if (PowerOceanAtlas.getInstance() != null) {
        PowerOceanAtlas.getInstance().addOpenFileViewer(ff);
      }
      ff.pack();
      ff.setVisible(true);
      ff.requestFocus();
      ff.setSavedState(JOAConstants.CREATEDONTHEFLY, null);
    }
    else if (cmd.equals("movetop")) {
      mMoveTopButton.setSelected(false);
      moveTop();
    }
    else if (cmd.equals("moveup")) {
      mMoveUpButton.setSelected(false);
      moveSelectionUp();
    }
    else if (cmd.equals("movedown")) {
      mMoveDownButton.setSelected(false);
      moveSelectionDown();
    }
    else if (cmd.equals("movebottom")) {
      mMoveBottButton.setSelected(false);
      moveBottom();
    }
    else if (cmd.equals("deleteselection")) {
      mDeleteSelection.setSelected(false);
      deleteSelection();
    }
    else if (cmd.equals("sort")) {
      sortCasts();
    }
  }

  public void maintainButtons() {
    if (mStnList != null && mStnList.getSelectedIndex() >= 0) {
      mMoveUpButton.setEnabled(true);
      mMoveTopButton.setEnabled(true);
      mMoveDownButton.setEnabled(true);
      mMoveBottButton.setEnabled(true);
      mDeleteSelection.setEnabled(true);
      /*mSortSelection.setEnabled(true);
             mNone.setEnabled(true);
             mStoN.setEnabled(true);
             mNtoS.setEnabled(true);
             mEtoW.setEnabled(true);
             mEtoW.setEnabled(true);
             mWtoE.setEnabled(true);*/
    }
    else {
      mMoveUpButton.setEnabled(false);
      mMoveTopButton.setEnabled(false);
      mMoveDownButton.setEnabled(false);
      mMoveBottButton.setEnabled(false);
      mDeleteSelection.setEnabled(false);
      /*mSortSelection.setEnabled(false);
             mNone.setEnabled(false);
             mStoN.setEnabled(false);
             mNtoS.setEnabled(false);
             mEtoW.setEnabled(false);
             mEtoW.setEnabled(false);
             mWtoE.setEnabled(false);*/
    }
  }

  public void moveSelectionDown() {
    try {
      JOAConstants.LogFileStream.writeBytes(mFileViewer.getTitle() + ": Moved stns down: ");
    }
    catch (Exception ex) {}

    int[] moveItems = new int[250];
    int numToMove = 0;
    Vector<SectionStation> moveCasts = new Vector<SectionStation>();
    int newPos = 0;
    int[] selectedIndices = mStnList.getSelectedIndices();

    for (int i = 0; i < selectedIndices.length; i++) {
      SectionStation outCast = (SectionStation)mFoundStns.elementAt(selectedIndices[i]);
      try {
        JOAConstants.LogFileStream.writeBytes(outCast.getStn() + ",");
      }
      catch (Exception ex) {}

      // mark for moving
      moveItems[numToMove++] = selectedIndices[i];
      moveCasts.addElement(outCast);

      newPos = selectedIndices[i] + 1;
    }

    // add cells back at end location
    for (int k = 0; k < numToMove; k++) {
      mFoundStns.insertElementAt(moveCasts.elementAt(k), newPos + k + 1);
    }

    // delete original cells from bottom of selection up
    for (int d = numToMove - 1; d >= 0; d--) {
      mFoundStns.removeElementAt(moveItems[d]);
    }

    buildStnList();

    // rehilte selections
    int[] nhc = new int[numToMove];
    for (int i = 0; i < numToMove; i++) {
      nhc[i] = newPos + i - (numToMove - 1);
    }
    mStnList.setSelectedIndices(nhc);

    try {
      JOAConstants.LogFileStream.writeBytes("\n");
      JOAConstants.LogFileStream.flush();
    }
    catch (Exception ex) {}
  }

  public void moveBottom() {
    try {
      JOAConstants.LogFileStream.writeBytes(mFileViewer.getTitle() + ": Moved stns to bottom: ");
    }
    catch (Exception ex) {}
    int[] moveItems = new int[250];
    int numToMove = 0;
    Vector<SectionStation> moveCasts = new Vector<SectionStation>();
    int newPos = 0;
    int[] selectedIndices = mStnList.getSelectedIndices();
    ;

    newPos = mFoundStns.size() - 1;

    for (int i = 0; i < selectedIndices.length; i++) {
      SectionStation outCast = (SectionStation)mFoundStns.elementAt(selectedIndices[i]);
      try {
        JOAConstants.LogFileStream.writeBytes(outCast.getStn() + ",");
      }
      catch (Exception ex) {}

      // mark for moving
      moveItems[numToMove++] = selectedIndices[i];
      moveCasts.addElement(outCast);
    }

    // add cells back at end location
    for (int k = 0; k < numToMove; k++) {
      mFoundStns.insertElementAt(moveCasts.elementAt(k), newPos + k + 1);
    }

    // delete original cells from bottom of selection up
    for (int d = numToMove - 1; d >= 0; d--) {
      mFoundStns.removeElementAt(moveItems[d]);
    }

    buildStnList();

    // rehilte selections
    int[] nhc = new int[numToMove];
    for (int i = 0; i < numToMove; i++) {
      nhc[i] = newPos + i - (numToMove - 1);
    }
    mStnList.setSelectedIndices(nhc);

    try {
      JOAConstants.LogFileStream.writeBytes("\n");
      JOAConstants.LogFileStream.flush();
    }
    catch (Exception ex) {}
  }

  public void moveSelectionUp() {
    try {
      JOAConstants.LogFileStream.writeBytes(mFileViewer.getTitle() + ": Moved stns up: ");
    }
    catch (Exception ex) {}
    int[] moveItems = new int[250];
    int numToMove = 0;
    Vector<SectionStation> moveCasts = new Vector<SectionStation>();
    int newPos = 0;
    int[] selectedIndices = mStnList.getSelectedIndices();

    newPos = selectedIndices[0] - 1;
    if (newPos < 0) {
      return;
    }

    for (int i = 0; i < selectedIndices.length; i++) {
      SectionStation outCast = (SectionStation)mFoundStns.elementAt(selectedIndices[i]);
      try {
        JOAConstants.LogFileStream.writeBytes(outCast.getStn() + ",");
      }
      catch (Exception ex) {}

      // mark for moving
      moveItems[numToMove++] = selectedIndices[i];
      moveCasts.addElement(outCast);
    }

    // delete original cells from bottom of selection up
    for (int d = numToMove - 1; d >= 0; d--) {
      mFoundStns.removeElementAt(moveItems[d]);
    }

    // add cells back at start location
    for (int k = 0; k < numToMove; k++) {
      mFoundStns.insertElementAt(moveCasts.elementAt(k), newPos + k);
    }

    buildStnList();

    // rehilte selections
    int[] nhc = new int[numToMove];
    for (int i = 0; i < numToMove; i++) {
      nhc[i] = newPos + i;
    }
    mStnList.setSelectedIndices(nhc);

    try {
      JOAConstants.LogFileStream.writeBytes("\n");
      JOAConstants.LogFileStream.flush();
    }
    catch (Exception ex) {}
  }

  public void moveTop() {
    try {
      JOAConstants.LogFileStream.writeBytes(mFileViewer.getTitle() + ": Moved stns to top: ");
    }
    catch (Exception ex) {}
    int[] moveItems = new int[250];
    int numToMove = 0;
    Vector<SectionStation> moveCasts = new Vector<SectionStation>();
    int newPos = 0;
    int[] selectedIndices = mStnList.getSelectedIndices();

    newPos = 0;

    for (int i = 0; i < selectedIndices.length; i++) {
      SectionStation outCast = (SectionStation)mFoundStns.elementAt(selectedIndices[i]);
      try {
        JOAConstants.LogFileStream.writeBytes(outCast.getStn() + ",");
      }
      catch (Exception ex) {}

      // mark for moving
      moveItems[numToMove++] = selectedIndices[i];
      moveCasts.addElement(outCast);
    }

    // delete original cells from bottom of selection up
    for (int d = numToMove - 1; d >= 0; d--) {
      mFoundStns.removeElementAt(moveItems[d]);
    }

    // add cells back at start location
    for (int k = 0; k < numToMove; k++) {
      mFoundStns.insertElementAt(moveCasts.elementAt(k), newPos + k);
    }

    buildStnList();

    // rehilte selections
    int[] nhc = new int[numToMove];
    for (int i = 0; i < numToMove; i++) {
      nhc[i] = newPos + i;
    }
    mStnList.setSelectedIndices(nhc);

    try {
      JOAConstants.LogFileStream.writeBytes("\n");
      JOAConstants.LogFileStream.flush();
    }
    catch (Exception ex) {}
  }

  public void deleteSelection() {
    try {
      JOAConstants.LogFileStream.writeBytes(mFileViewer.getTitle() + ": deleted stns: ");
    }
    catch (Exception ex) {}
    int[] moveItems = new int[250];
    int numToMove = 0;
    Vector<SectionStation> moveCasts = new Vector<SectionStation>();
    int[] selectedIndices = mStnList.getSelectedIndices();

    for (int i = 0; i < selectedIndices.length; i++) {
      SectionStation outCast = (SectionStation)mFoundStns.elementAt(selectedIndices[i]);
      try {
        JOAConstants.LogFileStream.writeBytes(outCast.getStn() + ",");
      }
      catch (Exception ex) {}

      // mark for moving
      moveItems[numToMove++] = selectedIndices[i];
      moveCasts.addElement(outCast);
    }

    // delete original cells from bottom of selection up
    for (int d = numToMove - 1; d >= 0; d--) {
      mFoundStns.removeElementAt(moveItems[d]);
    }

    buildStnList();
    if (mSource != null) {
      mSource.forceStnRedraw();
    }

    try {
      JOAConstants.LogFileStream.writeBytes("\n");
      JOAConstants.LogFileStream.flush();
    }
    catch (Exception ex) {}
  }

  public void sortCasts() {
    try {
      String sortStr = "";
      if (mSortDirection == SORT_NTOS) {
        sortStr = "N to S";
      }
      else if (mSortDirection == SORT_STON) {
        sortStr = "N to S";
      }
      else if (mSortDirection == SORT_ETOW) {
        sortStr = "E to W";
      }
      else if (mSortDirection == SORT_WTOE) {
        sortStr = "W to E";
      }
      else if (mSortDirection == SORT_STN_DSC) {
        sortStr = "Stn. ID (descending)";
      }
      else if (mSortDirection == SORT_STN_ASC) {
        sortStr = "Stn. ID (ascending)";
      }
      else if (mSortDirection == SORT_DATE_DSC) {
        sortStr = "Date (descending)";
      }
      else if (mSortDirection == SORT_STN_ASC) {
        sortStr = "Date (ascending)";
      }
      JOAConstants.LogFileStream.writeBytes(mFileViewer.getTitle() + ": Sorted stns by: " + sortStr);
      JOAConstants.LogFileStream.writeBytes("\n");
      JOAConstants.LogFileStream.flush();
    }
    catch (Exception ex) {}

    int numCasts; //, k, i=0, l;
    SectionStation cast1;
    SectionStation cast2;
    int direction;
    double val1, val2;
    boolean inPacific = false;

    numCasts = mFoundStns.size();
    if (mSortDirection == SORT_NTOS || mSortDirection == SORT_ETOW || mSortDirection == SORT_STN_DSC ||
        mSortDirection == SORT_DATE_DSC) {
      direction = DESCENDING;
    }
    else {
      direction = ASCENDING;
    }

    // determine whether we're in the Pacific ocean
    if (mLeftLon > 0 && mRightLon < 0 || mLeftLon < 0 && mRightLon > 0) {
      inPacific = true;
    }

    // perform the insertion sort
    for (int i = 0; i < numCasts - 1; i++) {
      cast1 = (SectionStation)mFoundStns.elementAt(i);
      for (int j = i + 1; j < numCasts; j++) {
        cast2 = (SectionStation)mFoundStns.elementAt(j);
        if (mSortDirection <= SORT_ETOW) {
          if (mSortDirection == SORT_NTOS || mSortDirection == SORT_STON) {
            // SORT BY LATITUDE;
            val1 = cast1.mLat;
            val2 = cast2.mLat;
          }
          else if (mSortDirection == SORT_ETOW || mSortDirection == SORT_WTOE) {
            // sort by longitude
            val1 = cast1.mLon;
            val2 = cast2.mLon;
            if (val1 > 0 && val2 < 0 && inPacific) {
              // crossed dateline going east
              val2 += 360;
            }
            if (val1 < 0 && val2 > 0 && inPacific) {
              // crossed dateline going east
              val1 += 360;
            }
          }
          else {
            // None: resort by ordinal
            val1 = cast1.mSecOrdinal;
            val2 = cast2.mSecOrdinal;
          }

          if (direction == ASCENDING && val1 > val2) {
            String tShipCode = cast1.mShipCode;
            int tSecOrdinal = cast1.mSecOrdinal;
            int tOrdinal = cast1.mOrdinal;
            boolean tUseStn = cast1.mUseStn;
            double tLat = cast1.mLat;
            double tLon = cast1.mLon;
            int tYear = cast1.mYear;
            int tMonth = cast1.mMonth;
            int tDay = cast1.mDay;
            double tMinute = cast1.mMinute;
            int tHour = cast1.mHour;
            String tStnNum = cast1.mStnNum;
            int tCastNum = cast1.mCastNum;
            int tNumBottles = cast1.mNumBottles;
            int tBottomDepthInDBARS = cast1.mBottomDepthInDBARS;
            int tVarFlag = cast1.mVarFlag;
            LRVector tBottles = cast1.mBottles;
            Color tCurrColor = cast1.mCurrColor;
            int tCurrSymbolSize = cast1.mCurrSymbolSize;
            Section tFoundSection = cast1.mFoundSection;
            Station tFoundStn = cast1.mFoundStn;

            cast1.mShipCode = cast2.mShipCode;
            cast1.mSecOrdinal = cast2.mSecOrdinal;
            cast1.mOrdinal = cast2.mOrdinal;
            cast1.mUseStn = cast2.mUseStn;
            cast1.mLat = cast2.mLat;
            cast1.mLon = cast2.mLon;
            cast1.mYear = cast2.mYear;
            cast1.mMonth = cast2.mMonth;
            cast1.mDay = cast2.mDay;
            cast1.mMinute = cast2.mMinute;
            cast1.mHour = cast2.mHour;
            cast1.mStnNum = cast2.mStnNum;
            cast1.mCastNum = cast2.mCastNum;
            cast1.mNumBottles = cast2.mNumBottles;
            cast1.mBottomDepthInDBARS = cast2.mBottomDepthInDBARS;
            cast1.mVarFlag = cast2.mVarFlag;
            cast1.mBottles = cast2.mBottles;
            cast1.mCurrColor = cast2.mCurrColor;
            cast1.mCurrSymbolSize = cast2.mCurrSymbolSize;
            cast1.mFoundSection = cast2.mFoundSection;
            cast1.mFoundStn = cast2.mFoundStn;

            cast2.mShipCode = tShipCode;
            cast2.mSecOrdinal = tSecOrdinal;
            cast2.mOrdinal = tOrdinal;
            cast2.mUseStn = tUseStn;
            cast2.mLat = tLat;
            cast2.mLon = tLon;
            cast2.mYear = tYear;
            cast2.mMonth = tMonth;
            cast2.mDay = tDay;
            cast2.mHour = tHour;
            cast2.mMinute = tMinute;
            cast2.mStnNum = tStnNum;
            cast2.mCastNum = tCastNum;
            cast2.mNumBottles = tNumBottles;
            cast2.mBottomDepthInDBARS = tBottomDepthInDBARS;
            cast2.mVarFlag = tVarFlag;
            cast2.mBottles = tBottles;
            cast2.mCurrColor = tCurrColor;
            cast2.mCurrSymbolSize = tCurrSymbolSize;
            cast2.mFoundSection = tFoundSection;
            cast2.mFoundStn = tFoundStn;
          }
          else if (direction == DESCENDING && val1 < val2) {
            String tShipCode = cast1.mShipCode;
            int tSecOrdinal = cast1.mSecOrdinal;
            int tOrdinal = cast1.mOrdinal;
            boolean tUseStn = cast1.mUseStn;
            double tLat = cast1.mLat;
            double tLon = cast1.mLon;
            int tYear = cast1.mYear;
            int tMonth = cast1.mMonth;
            int tDay = cast1.mDay;
            double tMinute = cast1.mMinute;
            int tHour = cast1.mHour;
            String tStnNum = cast1.mStnNum;
            int tCastNum = cast1.mCastNum;
            int tNumBottles = cast1.mNumBottles;
            int tBottomDepthInDBARS = cast1.mBottomDepthInDBARS;
            int tVarFlag = cast1.mVarFlag;
            LRVector tBottles = cast1.mBottles;
            Color tCurrColor = cast1.mCurrColor;
            int tCurrSymbolSize = cast1.mCurrSymbolSize;
            Section tFoundSection = cast1.mFoundSection;
            Station tFoundStn = cast1.mFoundStn;

            cast1.mShipCode = cast2.mShipCode;
            cast1.mSecOrdinal = cast2.mSecOrdinal;
            cast1.mOrdinal = cast2.mOrdinal;
            cast1.mUseStn = cast2.mUseStn;
            cast1.mLat = cast2.mLat;
            cast1.mLon = cast2.mLon;
            cast1.mYear = cast2.mYear;
            cast1.mMonth = cast2.mMonth;
            cast1.mDay = cast2.mDay;
            cast1.mMinute = cast2.mMinute;
            cast1.mHour = cast2.mHour;
            cast1.mStnNum = cast2.mStnNum;
            cast1.mCastNum = cast2.mCastNum;
            cast1.mNumBottles = cast2.mNumBottles;
            cast1.mBottomDepthInDBARS = cast2.mBottomDepthInDBARS;
            cast1.mVarFlag = cast2.mVarFlag;
            cast1.mBottles = cast2.mBottles;
            cast1.mCurrColor = cast2.mCurrColor;
            cast1.mCurrSymbolSize = cast2.mCurrSymbolSize;
            cast1.mFoundSection = cast2.mFoundSection;
            cast1.mFoundStn = cast2.mFoundStn;

            cast2.mShipCode = tShipCode;
            cast2.mSecOrdinal = tSecOrdinal;
            cast2.mOrdinal = tOrdinal;
            cast2.mUseStn = tUseStn;
            cast2.mLat = tLat;
            cast2.mLon = tLon;
            cast2.mYear = tYear;
            cast2.mMonth = tMonth;
            cast2.mDay = tDay;
            cast2.mHour = tHour;
            cast2.mMinute = tMinute;
            cast2.mStnNum = tStnNum;
            cast2.mCastNum = tCastNum;
            cast2.mNumBottles = tNumBottles;
            cast2.mBottomDepthInDBARS = tBottomDepthInDBARS;
            cast2.mVarFlag = tVarFlag;
            cast2.mBottles = tBottles;
            cast2.mCurrColor = tCurrColor;
            cast2.mCurrSymbolSize = tCurrSymbolSize;
            cast2.mFoundSection = tFoundSection;
            cast2.mFoundStn = tFoundStn;
          }
        }
        else {
          if (mSortDirection == SORT_STN_DSC || mSortDirection == SORT_STN_ASC) {
            ComparableString stn1 = new ComparableString(cast1.mStnNum);
            ComparableString stn2 = new ComparableString(cast2.mStnNum);
            if ((stn1.compareTo(stn2) < 0 && mSortDirection == SORT_STN_DSC) ||
                (stn1.compareTo(stn2) > 0 && mSortDirection == SORT_STN_ASC)) {
              //swap stations
              String tShipCode = cast1.mShipCode;
              int tSecOrdinal = cast1.mSecOrdinal;
              int tOrdinal = cast1.mOrdinal;
              boolean tUseStn = cast1.mUseStn;
              double tLat = cast1.mLat;
              double tLon = cast1.mLon;
              int tYear = cast1.mYear;
              int tMonth = cast1.mMonth;
              int tDay = cast1.mDay;
              double tMinute = cast1.mMinute;
              int tHour = cast1.mHour;
              String tStnNum = cast1.mStnNum;
              int tCastNum = cast1.mCastNum;
              int tNumBottles = cast1.mNumBottles;
              int tBottomDepthInDBARS = cast1.mBottomDepthInDBARS;
              int tVarFlag = cast1.mVarFlag;
              LRVector tBottles = cast1.mBottles;
              Color tCurrColor = cast1.mCurrColor;
              int tCurrSymbolSize = cast1.mCurrSymbolSize;
              Section tFoundSection = cast1.mFoundSection;
              Station tFoundStn = cast1.mFoundStn;

              cast1.mShipCode = cast2.mShipCode;
              cast1.mSecOrdinal = cast2.mSecOrdinal;
              cast1.mOrdinal = cast2.mOrdinal;
              cast1.mUseStn = cast2.mUseStn;
              cast1.mLat = cast2.mLat;
              cast1.mLon = cast2.mLon;
              cast1.mYear = cast2.mYear;
              cast1.mMonth = cast2.mMonth;
              cast1.mDay = cast2.mDay;
              cast1.mMinute = cast2.mMinute;
              cast1.mHour = cast2.mHour;
              cast1.mStnNum = cast2.mStnNum;
              cast1.mCastNum = cast2.mCastNum;
              cast1.mNumBottles = cast2.mNumBottles;
              cast1.mBottomDepthInDBARS = cast2.mBottomDepthInDBARS;
              cast1.mVarFlag = cast2.mVarFlag;
              cast1.mBottles = cast2.mBottles;
              cast1.mCurrColor = cast2.mCurrColor;
              cast1.mCurrSymbolSize = cast2.mCurrSymbolSize;
              cast1.mFoundSection = cast2.mFoundSection;
              cast1.mFoundStn = cast2.mFoundStn;

              cast2.mShipCode = tShipCode;
              cast2.mSecOrdinal = tSecOrdinal;
              cast2.mOrdinal = tOrdinal;
              cast2.mUseStn = tUseStn;
              cast2.mLat = tLat;
              cast2.mLon = tLon;
              cast2.mYear = tYear;
              cast2.mMonth = tMonth;
              cast2.mDay = tDay;
              cast2.mHour = tHour;
              cast2.mMinute = tMinute;
              cast2.mStnNum = tStnNum;
              cast2.mCastNum = tCastNum;
              cast2.mNumBottles = tNumBottles;
              cast2.mBottomDepthInDBARS = tBottomDepthInDBARS;
              cast2.mVarFlag = tVarFlag;
              cast2.mBottles = tBottles;
              cast2.mCurrColor = tCurrColor;
              cast2.mCurrSymbolSize = tCurrSymbolSize;
              cast2.mFoundSection = tFoundSection;
              cast2.mFoundStn = tFoundStn;
            }
          }
          else if (mSortDirection == SORT_DATE_DSC || mSortDirection == SORT_DATE_ASC) {
            try {
              date1 = null;
              date2 = null;
              int hour1 = cast1.mHour == JOAConstants.MISSINGVALUE ? 0 : cast1.mHour;
              int hour2 = cast2.mHour == JOAConstants.MISSINGVALUE ? 0 : cast2.mHour;
              int min1 = cast1.mMinute == JOAConstants.MISSINGVALUE ? 0 : (int)cast1.mMinute;
              int min2 = cast2.mMinute == JOAConstants.MISSINGVALUE ? 0 : (int)cast2.mMinute;
              date1 = new GeoDate(cast1.mMonth, cast1.mDay, cast1.mYear, hour1, min1, 0, 0);
              date2 = new GeoDate(cast2.mMonth, cast2.mDay, cast2.mYear, hour2, min2, 0, 0);
            }
            catch (Exception ex) {
              System.out.println("threw creating a GeoDate");
            }
            if ((date1 != null && date1.getTime() > date2.getTime() && mSortDirection == SORT_DATE_ASC) ||
                (date2 != null && date1.getTime() < date2.getTime() && mSortDirection == SORT_DATE_DSC)) {
              //swap stations
              String tShipCode = cast1.mShipCode;
              int tSecOrdinal = cast1.mSecOrdinal;
              int tOrdinal = cast1.mOrdinal;
              boolean tUseStn = cast1.mUseStn;
              double tLat = cast1.mLat;
              double tLon = cast1.mLon;
              int tYear = cast1.mYear;
              int tMonth = cast1.mMonth;
              int tDay = cast1.mDay;
              double tMinute = cast1.mMinute;
              int tHour = cast1.mHour;
              String tStnNum = cast1.mStnNum;
              int tCastNum = cast1.mCastNum;
              int tNumBottles = cast1.mNumBottles;
              int tBottomDepthInDBARS = cast1.mBottomDepthInDBARS;
              int tVarFlag = cast1.mVarFlag;
              LRVector tBottles = cast1.mBottles;
              Color tCurrColor = cast1.mCurrColor;
              int tCurrSymbolSize = cast1.mCurrSymbolSize;
              Section tFoundSection = cast1.mFoundSection;
              Station tFoundStn = cast1.mFoundStn;

              cast1.mShipCode = cast2.mShipCode;
              cast1.mSecOrdinal = cast2.mSecOrdinal;
              cast1.mOrdinal = cast2.mOrdinal;
              cast1.mUseStn = cast2.mUseStn;
              cast1.mLat = cast2.mLat;
              cast1.mLon = cast2.mLon;
              cast1.mYear = cast2.mYear;
              cast1.mMonth = cast2.mMonth;
              cast1.mDay = cast2.mDay;
              cast1.mMinute = cast2.mMinute;
              cast1.mHour = cast2.mHour;
              cast1.mStnNum = cast2.mStnNum;
              cast1.mCastNum = cast2.mCastNum;
              cast1.mNumBottles = cast2.mNumBottles;
              cast1.mBottomDepthInDBARS = cast2.mBottomDepthInDBARS;
              cast1.mVarFlag = cast2.mVarFlag;
              cast1.mBottles = cast2.mBottles;
              cast1.mCurrColor = cast2.mCurrColor;
              cast1.mCurrSymbolSize = cast2.mCurrSymbolSize;
              cast1.mFoundSection = cast2.mFoundSection;
              cast1.mFoundStn = cast2.mFoundStn;

              cast2.mShipCode = tShipCode;
              cast2.mSecOrdinal = tSecOrdinal;
              cast2.mOrdinal = tOrdinal;
              cast2.mUseStn = tUseStn;
              cast2.mLat = tLat;
              cast2.mLon = tLon;
              cast2.mYear = tYear;
              cast2.mMonth = tMonth;
              cast2.mDay = tDay;
              cast2.mHour = tHour;
              cast2.mMinute = tMinute;
              cast2.mStnNum = tStnNum;
              cast2.mCastNum = tCastNum;
              cast2.mNumBottles = tNumBottles;
              cast2.mBottomDepthInDBARS = tBottomDepthInDBARS;
              cast2.mVarFlag = tVarFlag;
              cast2.mBottles = tBottles;
              cast2.mCurrColor = tCurrColor;
              cast2.mCurrSymbolSize = tCurrSymbolSize;
              cast2.mFoundSection = tFoundSection;
              cast2.mFoundStn = tFoundStn;
            }
          }
        }
      }
    }
    /*
     // perform the shell short
     for (int nn=1; nn<=lognB2; nn++) {

      m = m/2;
      k = (numCasts-1) - m;
      boolean skip = false;
      for (int j=0; j<k; j++) {
       if (!skip)
        i = j;
       skip = false;
       l = i + m;			// L
       // get the objects
       cast1 = null;
       cast2 = null;
       try {
       cast1 = (SectionStation)mFoundStns.elementAt(l+1);
       cast2 = (SectionStation)mFoundStns.elementAt(i+1);
       }
       catch (Exception ex) {
       System.out.println("getting casts, cast#1 @" + (l+1) +"  cast#2 @" + (i+1));
       ex.printStackTrace();
       }
       if (mSortDirection == SORT_NTOS || mSortDirection == SORT_STON) {
        // SORT BY LATITUDE;
        val1 = cast1.mLat * 0.001;
        val2 = cast2.mLat * 0.001;
       }
       else if (mSortDirection == SORT_ETOW || mSortDirection == SORT_WTOE) {
        // sort by longitude
        val1 = cast1.mLon * 0.001;
        val2 = cast2.mLon * 0.001;
        if (val1 > 0 && val2 < 0 && inPacific) {
         // crossed dateline going east
         val2 += 360;
        }
        if (val1 < 0 && val2 > 0 && inPacific) {
         // crossed dateline going east
         val1 += 360;
        }
       }
       else {
        // None: resort by ordinal
        val1 = cast1.mSecOrdinal;
        val2 = cast2.mSecOrdinal;
       }
       if (direction == ASCENDING && val1 < val2) {
        // exchange
        //foundStns.swapItems(l+1, i+1);
        Object temp = mFoundStns.elementAt(l+1);
        mFoundStns.setElementAt(mFoundStns.elementAt(i+1), l+1);
        mFoundStns.setElementAt(temp, i+1);
        i = i - m;
        if (i >= 0) {
         //goto L;
         skip = true;
        }
       }
       else if (direction == DESCENDING && val1 > val2) {
        // exchange
        //foundStns.swapItems(l+1, i+1);
        Object temp = mFoundStns.elementAt(l+1);
        mFoundStns.setElementAt(mFoundStns.elementAt(i+1), l+1);
        mFoundStns.setElementAt(temp, i+1);
        i = i - m;
        if (i >= 0) {
         //goto L;
         skip = true;
        }
       }
      }
     }*/
    buildStnList();
  }

  private class ComparableString implements Comparable<Object> {
    String mData;

    public ComparableString(String inStr) {
      mData = inStr;
    }

    public int compareTo(Object o) {
      // first try to convert to integer and compare that way
      boolean compareLexical = false;
      int iStn1 = 0;
      int iStn2 = 0;
      try {
        iStn1 = Integer.valueOf(mData).intValue();
        iStn2 = Integer.valueOf(((ComparableString)o).getString()).intValue();
      }
      catch (NumberFormatException ex) {
        compareLexical = true;
      }
      if (compareLexical) {
        return mData.compareTo(((ComparableString)o).getString());
      }
      else {
        if (iStn1 < iStn2) {
          return -1;
        }
        else if (iStn1 == iStn2) {
          return 0;
        }
        else {
          return 1;
        }
      }

    }

    public String getString() {
      return mData;
    }
  }

  public void addStn(SectionStation nst) {
    mFoundStns.addElement(nst);
    buildStnList();
  }

  public int getNumPts() {
    return mFoundStns.size();
  }

  public Vector<SectionStation> getSectionStations() {
    return mFoundStns;
  }
}

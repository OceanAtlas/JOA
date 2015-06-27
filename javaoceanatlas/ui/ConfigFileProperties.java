/*
 * $Id: ConfigFileProperties.java,v 1.6 2005/10/18 23:42:18 oz Exp $
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
import javaoceanatlas.events.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import gov.noaa.pmel.swing.*;
import javaoceanatlas.PowerOceanAtlas;

@SuppressWarnings("serial")
public class ConfigFileProperties extends JOAJDialog implements ActionListener, StnFilterChangedListener, ButtonMaintainer,
    ObsChangedListener, DataAddedListener, ListSelectionListener {
  protected FileViewer mFileViewer;
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mApplyButton = null;
  protected JOAJButton mCancelButton = null;
  protected JOAJTextField mLatField = null;
  protected JOAJTextField mLonField = null;
  protected JOAJTextField mSectionField = null;
  protected JOAJTextField mStnNumField = null;
  protected JOAJTextField mCastNumField = null;
  protected JOAJTextField mShipCodeField = null;
  protected JOAJTextField mYearField = null;
  protected JOAJTextField mMonthField = null;
  protected JOAJTextField mDayField = null;
  protected JOAJTextField mHourField = null;
  protected JOAJTextField mMinField = null;
  protected JOAJTextField mBottomField = null;
  protected JTextArea mFileComments = null;
  protected JOAJList mStnList = null;
  protected int mTotalStns = 0;
	private Timer timer = new Timer();
  protected JDialog mFrame = null;
  protected boolean mIgnore = false;
  protected Swatch mSectionColor = null;

  public ConfigFileProperties(FileViewer fv) {
    super(fv, "File Properties", false);
    mFileViewer = fv;
    this.init();
  }

  public ConfigFileProperties(JFrame fr, FileViewer fv) {
    super(fr, "File Properties", false);
    mFileViewer = fv;
    this.init();
  }

  private void buildStnList() {
    Vector<String> listData = new Vector<String>();
    for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = (Section)of.getSection(sec);;
        for (int stc = 0; stc < sech.mStations.size(); stc++) {
          Station sh = (Station)sech.mStations.elementAt(stc);

          if (sh.mUseStn) {
            listData.addElement(sech.mSectionDescription + ":" + sh.mStnNum + "(" + JOAFormulas.formatLat(sh.mLat) +
                                " " + JOAFormulas.formatLon(sh.mLon) + ", " + JOAFormulas.formatDate(sh, false) + ")");
            mTotalStns++;
          }
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

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    // fill the stn list
    buildStnList();

    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(0, 0));

    JPanel stnDetailPanel = new JPanel();
    stnDetailPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

    JPanel stnSelPanel = new JPanel();
    stnSelPanel.setLayout(new BorderLayout(5, 0));

    OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
    Section sec = (Section)of.getCurrSection();
    Station stn = (Station)sec.mStations.currElement();

    //Section line
    JPanel line1 = new JPanel();
    line1.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line1.add(new JOAJLabel(b.getString("kSection")));
    mSectionField = new JOAJTextField();
    mSectionField.setColumns(15);
    mSectionField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line1.add(mSectionField);
    line1.add(new JOAJLabel(b.getString("kMapColor")));
    mSectionColor = new Swatch(JOAConstants.DEFAULT_CONTENTS_COLOR, 12, 12);
    line1.add(mSectionColor);

    // stn/cast num line
    JPanel line2 = new JPanel();
    line2.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line2.add(new JOAJLabel(b.getString("kStationNumber")));
    mStnNumField = new JOAJTextField();
    mStnNumField.setColumns(6);
    mStnNumField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line2.add(mStnNumField);

    line2.add(new JOAJLabel(b.getString("kCastNumber")));
    mCastNumField = new JOAJTextField();
    mCastNumField.setColumns(2);
    mCastNumField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line2.add(mCastNumField);

    //location line
    JPanel line4 = new JPanel();
    line4.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line4.add(new JOAJLabel(b.getString("kLongitudeColon")));
    mLonField = new JOAJTextField();
    mLonField.setColumns(5);
    mLonField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line4.add(mLonField);
    line4.add(new JOAJLabel(b.getString("kLatitudeColon")));
    mLatField = new JOAJTextField();
    mLatField.setColumns(5);
    mLatField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line4.add(mLatField);

    // date/ship/bottom
    JPanel line5 = new JPanel();
    line5.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line5.add(new JOAJLabel(b.getString("kDate") + "  "));
    line5.add(new JOAJLabel(b.getString("kMM")));
    mMonthField = new JOAJTextField();
    mMonthField.setColumns(2);
    mMonthField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line5.add(mMonthField);
    line5.add(new JOAJLabel(b.getString("kDD")));
    mDayField = new JOAJTextField();
    mDayField.setColumns(2);
    mDayField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line5.add(mDayField);
    line5.add(new JOAJLabel(b.getString("kYYYY")));
    mYearField = new JOAJTextField();
    mYearField.setColumns(4);
    mYearField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line5.add(mYearField);
    line5.add(new JOAJLabel(b.getString("kHH")));
    mHourField = new JOAJTextField();
    mHourField.setColumns(2);
    mHourField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line5.add(mHourField);
    line5.add(new JOAJLabel(b.getString("kMM.M")));
    mMinField = new JOAJTextField();
    mMinField.setColumns(4);
    mMinField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line5.add(mMinField);

    JPanel line6 = new JPanel();
    line6.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line6.add(new JOAJLabel(b.getString("kBottom")));
    mBottomField = new JOAJTextField();
    mBottomField.setColumns(4);
    mBottomField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line6.add(mBottomField);
    line6.add(new JOAJLabel(b.getString("kShipCode")));
    mShipCodeField = new JOAJTextField();
    mShipCodeField.setColumns(10);
    mShipCodeField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line6.add(mShipCodeField);
    stnDetailPanel.add(line1);
    stnDetailPanel.add(line2);
    stnDetailPanel.add(line4);
    stnDetailPanel.add(line5);
    stnDetailPanel.add(line6);

    // station selection
    mStnList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    mStnList.setPrototypeCellValue("Atlantic 11 S 210 (11.25 S 180.45 W, 4/1/1999         ");
    mStnList.setVisibleRowCount(5);
    mStnList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane listScroller = new JScrollPane(mStnList);

    // file comments
    JPanel aboutFile = new JPanel();
    aboutFile.setLayout(new BorderLayout(5, 5));
    mFileComments = new JTextArea(10, 20);
    mFileComments.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mFileComments.setLineWrap(true);
    mFileComments.setWrapStyleWord(true);
    //ta.setBackground(this.getBackground());
    int size = 12;
    if (JOAConstants.ISSUNOS) {
      size = 14;
    }
    mFileComments.setFont(new Font("Courier", Font.PLAIN, size));
    mFileComments.setEditable(true);
    TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kFileComments"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    aboutFile.setBorder(tb);
    JScrollPane commentScroller = new JScrollPane(mFileComments);
    aboutFile.add(commentScroller, "Center");

    // lower panel
    mOKBtn = new JOAJButton(b.getString("kOK"));
    mOKBtn.setActionCommand("ok");
    this.getRootPane().setDefaultButton(mOKBtn);
    mCancelButton = new JOAJButton(b.getString("kDone"));
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
    mApplyButton.addActionListener(this);
    mCancelButton.addActionListener(this);
    JPanel allStnDetail = new JPanel();
    allStnDetail.setLayout(new BorderLayout(5, 5));
    allStnDetail.add(new TenPixelBorder(stnDetailPanel, 5, 5, 5, 0), "East");
    allStnDetail.add(new TenPixelBorder(listScroller, 5, 5, 0, 5), "West");
    allStnDetail.add(new TenPixelBorder(aboutFile, 5, 0, 0, 5), "South");
    contents.add(new TenPixelBorder(allStnDetail, 5, 5, 0, 5), "North");
    contents.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
    this.pack();

    mFileViewer.addDataAddedListener(this);
    mFileViewer.addObsChangedListener(this);
    mStnList.addListSelectionListener(this);
    //mFileViewer.addStnFilterChangedListener(this);

    mFrame = this;
    WindowListener windowListener = new WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        mFileViewer.removeDataAddedListener((DataAddedListener)mFrame);
        //mFileViewer.removeStnFilterChangedListener((StnFilterChangedListener)mFrame);
        mFileViewer.removeObsChangedListener((ObsChangedListener)mFrame);
      }
    };
    this.addWindowListener(windowListener);

		runTimer();
    setStn(sec, stn);

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

  public void stnFilterChanged(StnFilterChangedEvent evt) {
    // redo the station list
    buildStnList();

    OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
    Section sec = (Section)of.getCurrSection();
    Station stn = (Station)sec.mStations.currElement();
    setStn(sec, stn);
  }

  public void dataAdded(DataAddedEvent evt) {
    // redo the station list
    buildStnList();

    OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
    Section sec = (Section)of.getCurrSection();
    Station stn = (Station)sec.mStations.currElement();
    setStn(sec, stn);
  }

  public void obsChanged(ObsChangedEvent evt) {
    // display the current station
    Station sh = evt.getFoundStation();
    Section sech = evt.getFoundSection();
    setStn(sech, sh);
  }

  public void valueChanged(ListSelectionEvent evt) {
    int selStn = mStnList.getSelectedIndex() + 1;
    if (mIgnore) {
      mIgnore = false;
      return;
    }

    // loop through and get matching stn
    for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = (Section)of.getSection(sec);
        if (sech.mNumCasts == 0) {
          continue;
        }
        for (int stc = 0; stc < sech.mStations.size(); stc++) {
          Station sh = (Station)sech.mStations.elementAt(stc);

          if (sh.mUseStn && sh.mOrdinal == selStn) {
            mIgnore = true;
            setStn(sech, sh);

            // fire an obs changed event
            mFileViewer.mOpenFiles.setCurrElement(of);
            of.setCurrentSection(sech);
            sech.mStations.setCurrElement(sh);
            Bottle fb = JOAFormulas.findBottleByPres(mFileViewer, sh);
            sh.mBottles.setCurrElement(fb);

            ObsChangedEvent oce = new ObsChangedEvent(mFileViewer);
            oce.setFoundObs(of, sech, sh, fb);
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(oce);
            break;
          }
        }
      }
    }
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      timer.cancel();
      mFileViewer.removeDataAddedListener(this);
      //mFileViewer.removeStnFilterChangedListener((StnFilterChangedListener)mFrame);
      mFileViewer.removeObsChangedListener(this);
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      applyChangesToStn();
      timer.cancel();
      mFileViewer.removeDataAddedListener(this);
      //mFileViewer.removeStnFilterChangedListener((StnFilterChangedListener)mFrame);
      mFileViewer.removeObsChangedListener(this);

      if (PowerOceanAtlas.getInstance() != null) {
        PrefsChangedEvent pce = new PrefsChangedEvent(PowerOceanAtlas.getInstance());
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(pce);
      }
      this.dispose();
    }
    else if (cmd.equals("apply")) {
      applyChangesToStn();
    }
  }

  public void setStn(Section inSec, Station inStn) {
    mSectionField.setText(inSec.mSectionDescription);
    mSectionColor.setColor(inSec.mSectionColor);
    mStnNumField.setText(inStn.mStnNum);
    mCastNumField.setText(String.valueOf(inStn.mCastNum));
    mLonField.setText(JOAFormulas.formatDouble(String.valueOf(inStn.mLon), 3, false));
    mLatField.setText(JOAFormulas.formatDouble(String.valueOf(inStn.mLat), 3, false));
    mYearField.setText(String.valueOf(inStn.mYear));
    mMonthField.setText(String.valueOf(inStn.mMonth));
    mDayField.setText(String.valueOf(inStn.mDay));
    if (inStn.mHour != JOAConstants.MISSINGVALUE) {
      mHourField.setText(String.valueOf(inStn.mHour));
    }
    else {
      mHourField.setText("");
    }
    if (inStn.mMinute != JOAConstants.MISSINGVALUE) {
      mMinField.setText(String.valueOf(inStn.mMinute));
    }
    else {
      mMinField.setText("");
    }
    mBottomField.setText(String.valueOf(inStn.mBottomDepthInDBARS));
    mShipCodeField.setText(inStn.mShipCode);
    mStnList.setSelectedIndex(inStn.mOrdinal - 1);
    mStnList.ensureIndexIsVisible(inStn.mOrdinal - 1);

    OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
    mFileComments.setText(of.mFileComments);
    mFileComments.select(0, 0);
  }

  public void applyChangesToStn() {
    // apply data in UI to current station
    OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
    Section sec = (Section)of.getCurrSection();
    Station stn = (Station)sec.mStations.currElement();

    try {
      String fldText = mSectionField.getText();
      sec.mSectionDescription = null;
      sec.mSectionDescription = new String(fldText);

      sec.mSectionColor = mSectionColor.getColor();

      fldText = mShipCodeField.getText();
      stn.mShipCode = null;
      stn.mShipCode = new String(fldText);

      fldText = mStnNumField.getText();
      stn.mStnNum = null;
      stn.mStnNum = new String(fldText);

      fldText = mCastNumField.getText();
      if (fldText.length() > 0) {
        stn.mCastNum = Integer.valueOf(fldText).intValue();
      }

      fldText = mLonField.getText();
      if (fldText.length() > 0) {
        stn.mLon = (Double.valueOf(fldText).doubleValue());
      }

      fldText = mLatField.getText();
      if (fldText.length() > 0) {
        stn.mLat = (Double.valueOf(fldText).doubleValue());
      }

      fldText = mYearField.getText();
      if (fldText.length() > 0) {
        stn.mYear = Integer.valueOf(fldText).intValue();
      }

      fldText = mMonthField.getText();
      if (fldText.length() > 0) {
        stn.mMonth = Integer.valueOf(fldText).intValue();
      }

      fldText = mDayField.getText();
      if (fldText.length() > 0) {
        stn.mDay = Integer.valueOf(fldText).intValue();
      }

      fldText = mHourField.getText();
      if (fldText.length() > 0) {
        stn.mHour = Integer.valueOf(fldText).intValue();
      }

      fldText = mMinField.getText();
      if (fldText.length() > 0) {
        stn.mMinute = Double.valueOf(fldText).doubleValue();
      }

      fldText = mBottomField.getText();
      if (fldText.length() > 0) {
        stn.mBottomDepthInDBARS = Integer.valueOf(fldText).intValue();
      }

      fldText = mFileComments.getText();
      of.mFileComments = null;
      of.mFileComments = new String(fldText);
    }
    catch (Exception ex) {

    }
  }

  public void maintainButtons() {
    //if (mFileViewer.mStnFilterActive)
    //	mNoneButton.setEnabled(true);
    //else
    //	mNoneButton.setEnabled(false);
  }
}

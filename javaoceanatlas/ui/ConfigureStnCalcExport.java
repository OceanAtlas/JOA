/*
 * $Id: ConfigureStnCalcExport.java,v 1.5 2005/09/07 18:49:31 oz Exp $
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
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.events.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class ConfigureStnCalcExport extends JOAJDialog implements ActionListener, ButtonMaintainer, ItemListener,
    DataAddedListener {
  protected FileViewer mFileViewer;
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mCancelButton = null;
  protected JOAJList mStnList = null;
  protected boolean mIgnore = false;
  protected int mTotalStns = 0;
  protected SmallIconButton checkAll = null;
  protected SmallIconButton checkNone = null;
  protected ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
  protected JOAJTextField mCustomMissingFld;
  protected JOAJRadioButton mJOADefaultMissing;
  protected JOAJRadioButton mWOCEDefaultMissing;
  protected JOAJRadioButton mEPICDefaultMissing;
  protected JOAJRadioButton mSpaceMissing;
  protected JOAJRadioButton mCustomMissing;
  protected JOAJTextField mCustomDelimFld;
  protected JOAJRadioButton mTabDelim;
  protected JOAJRadioButton mCommaDelim;
  protected JOAJRadioButton mSpaceDelim;
  protected JOAJRadioButton mCustomDelim;
	private Timer timer = new Timer();

  public ConfigureStnCalcExport(JOAWindow par, FileViewer fv) {
    super(par, "Export Station Calculation Results", false);
    mFileViewer = fv;
    this.init();
  }

  private void buildStnList() {
    Vector<String> listData = new Vector<String>();
    for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = (Section)of.mSections.elementAt(sec);
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
    // fill the stn list
    buildStnList();

    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(0, 0));

    JPanel stnSelPanel = new JPanel();
    stnSelPanel.setLayout(new BorderLayout(5, 0));

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
    cont1.add("North", allNoneCont);
    stnSelPanel.add("East", new TenPixelBorder(cont1, 0, 0, 0, 5));
		
		checkAll.setToolTipText("Select all stations in the list to export station calculations");
		checkNone.setToolTipText("Remove selected stations from export");

    mStnList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    mStnList.setPrototypeCellValue("Atlantic 11 S 210 (11.25 S 180.45 W, 4/1/1999");
    mStnList.setVisibleRowCount(6);
    JScrollPane listScroller = new JScrollPane(mStnList);
    stnSelPanel.add(new TenPixelBorder(listScroller, 0, 5, 0, 0), "Center");
    TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kStationSelection"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    stnSelPanel.setBorder(tb);

    // missing value assignment
    JPanel missingValuePanel = new JPanel();
    missingValuePanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
    mJOADefaultMissing = new JOAJRadioButton(b.getString("kJOADefaultMissing"), true);
    mWOCEDefaultMissing = new JOAJRadioButton(b.getString("kWOCEDefaultMissing"));
    mEPICDefaultMissing = new JOAJRadioButton(b.getString("kEPICDefaultMissing"));
    mSpaceMissing = new JOAJRadioButton(b.getString("kSpaceMissing"));
    mCustomMissing = new JOAJRadioButton(b.getString("kCustomMissing"));
    ButtonGroup bg = new ButtonGroup();
    bg.add(mJOADefaultMissing);
    bg.add(mWOCEDefaultMissing);
    bg.add(mEPICDefaultMissing);
    bg.add(mSpaceMissing);
    bg.add(mCustomMissing);
    JPanel customMissingValPanel = new JPanel();
    customMissingValPanel.setLayout(new RowLayout(Orientation.LEFT, Orientation.TOP, 0));
    mCustomMissingFld = new JOAJTextField(6);
    mCustomMissingFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    customMissingValPanel.add(mCustomMissing);
    customMissingValPanel.add(mCustomMissingFld);
    missingValuePanel.add(mJOADefaultMissing);
    missingValuePanel.add(mWOCEDefaultMissing);
    missingValuePanel.add(mEPICDefaultMissing);
    missingValuePanel.add(mSpaceMissing);
    missingValuePanel.add(customMissingValPanel);
    tb = BorderFactory.createTitledBorder(b.getString("kMissingValueSelection"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    missingValuePanel.setBorder(tb);

    // delimiter value assignment
    JPanel delimValuePanel = new JPanel();
    delimValuePanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
    mTabDelim = new JOAJRadioButton(b.getString("kTabDelim"), true);
    mCommaDelim = new JOAJRadioButton(b.getString("kCommaDelim"));
    mSpaceDelim = new JOAJRadioButton(b.getString("kSpaceDelim"));
    mCustomDelim = new JOAJRadioButton(b.getString("kCustomMissing"));
    ButtonGroup bg2 = new ButtonGroup();
    bg2.add(mTabDelim);
    bg2.add(mCommaDelim);
    bg2.add(mSpaceDelim);
    bg2.add(mCustomDelim);
    JPanel customDelimPanel = new JPanel();
    customDelimPanel.setLayout(new RowLayout(Orientation.LEFT, Orientation.TOP, 0));
    customDelimPanel.add(mCustomDelim);
    mCustomDelimFld = new JOAJTextField(6);
    mCustomDelimFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    customDelimPanel.add(mCustomDelimFld);
    delimValuePanel.add(mTabDelim);
    delimValuePanel.add(mCommaDelim);
    delimValuePanel.add(mSpaceDelim);
    delimValuePanel.add(customDelimPanel);
    tb = BorderFactory.createTitledBorder(b.getString("kDelimiterSelection"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    delimValuePanel.setBorder(tb);

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

    // build the dialog
    JPanel upperContentsPanel = new JPanel();
    upperContentsPanel.setLayout(new BorderLayout(5, 0));
    upperContentsPanel.add(new TenPixelBorder(stnSelPanel, 5, 5, 0, 0), "Center");

    JPanel optPanel = new JPanel();
    optPanel.setLayout(new GridLayout(1, 2, 5, 5));
    optPanel.add(missingValuePanel);
    optPanel.add(delimValuePanel);
    upperContentsPanel.add(new TenPixelBorder(optPanel, 5, 5, 0, 0), "South");

    contents.add(new TenPixelBorder(upperContentsPanel, 5, 5, 5, 5), "Center");
    contents.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");

    mFileViewer.addDataAddedListener(this);
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

  public void dataAdded(DataAddedEvent evt) {
    // redo the station list
    buildStnList();
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
      // get the missing value value
      String missingValText = "-99";
      if (mJOADefaultMissing.isSelected()) {
        missingValText = "-99";
      }
      else if (mWOCEDefaultMissing.isSelected()) {
        missingValText = "-999.0";
      }
      else if (mEPICDefaultMissing.isSelected()) {
        missingValText = "1e35";
      }
      else if (mSpaceMissing.isSelected()) {
        missingValText = " ";
      }
      else if (mCustomMissing.isSelected()) {
        missingValText = mCustomMissingFld.getText();

        if (missingValText == null || missingValText.length() == 0) {
          JFrame frm = new JFrame("Custom Missing Value Error");
          Toolkit.getDefaultToolkit().beep();
          JOptionPane.showMessageDialog(frm, "Custom missing value has not been defined.");
          return;
        }
      }

      // get the delimiter value
      String delimText = "\t";
      if (mTabDelim.isSelected()) {
        delimText = "\t";
      }
      else if (mCommaDelim.isSelected()) {
        delimText = ",";
      }
      else if (mSpaceDelim.isSelected()) {
        delimText = " ";
      }
      else if (mCustomDelim.isSelected()) {
        delimText = mCustomDelimFld.getText();

        if (delimText == null || delimText.length() == 0) {
          JFrame frm = new JFrame("Custom Delimiter Error");
          Toolkit.getDefaultToolkit().beep();
          JOptionPane.showMessageDialog(frm, "Custom delimiter has not been defined.");
          return;
        }
      }

      // export a spreadsheet
      Frame fr = new Frame();
      String directory = System.getProperty("user.dir");
      FileDialog f = new FileDialog(fr, "Export station results as:", FileDialog.SAVE);
      f.setDirectory(directory);
      if (mFileViewer.mCurrOutFileName != null) {
        f.setFile(mFileViewer.mCurrOutFileName + ".stncalcs");
      }
      else {
        f.setFile(mFileViewer.getTitle() + ".stncalcs");
      }
      f.setVisible(true);
      directory = f.getDirectory();
      f.dispose();
      if (directory != null && f.getFile() != null) {
        boolean[] stnKeepList = new boolean[mTotalStns];
        int[] stnList = new int[mStnList.getSelectedIndices().length];
        stnList = mStnList.getSelectedIndices();
        for (int i = 0; i < mTotalStns; i++) {
          stnKeepList[i] = false;
        }

        for (int i = 0; i < stnList.length; i++) {
          stnKeepList[stnList[i]] = true;
        }
        File outFile = new File(directory, f.getFile());
        exportStnCalcs(outFile, stnKeepList, delimText, missingValText);

        try {
          JOAConstants.LogFileStream.writeBytes("Exported stations calc file: " + outFile.getCanonicalPath() + "\n");
          JOAConstants.LogFileStream.flush();
        }
        catch (Exception ex) {}
      }

      timer.cancel();
      this.dispose();
    }
    else if (cmd.equals("none")) {
      checkNone.setSelected(false);
      // unselect all selected stations
      mStnList.clearSelection();
    }
    else if (cmd.equals("all")) {
      checkAll.setSelected(false);
      // select all stations in list
      mStnList.setSelectionInterval(0, mTotalStns - 1);
    }
  }

  public void maintainButtons() {
    if (mStnList != null && mStnList.getSelectedIndex() >= 0) {
      mOKBtn.setEnabled(true);
    }
    else {
      mOKBtn.setEnabled(false);
    }
  }

  public void exportStnCalcs(File file, boolean[] stnKeepList, String delim, String missingVal) {
    // create a progress dialog
    ProgressDialog progress = new ProgressDialog(mFileViewer, "Exporting Station Calculation Results...", Color.blue,
                                                 Color.white);
    progress.setVisible(true);
    try {
      FileOutputStream fos = new FileOutputStream(file);
      BufferedOutputStream bos = new BufferedOutputStream(fos, 1000000);
      DataOutputStream out = new DataOutputStream(bos);

      // write the column headers */
     out.writeBytes("Section" + delim + "Stn. Num" + delim + "Cast Num." + delim + "Lat" + delim + "Lon" + delim +
                    "Date" + delim);

      // write the parameter headers
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(0);
      Section sech = (Section)of.mSections.elementAt(0);

      for (int stc = 0; stc < sech.mNumStnVars; stc++) {
        String stnParm = sech.mStnProperties[stc];
        String stnPargetUnits = sech.mStnUnits[stc];
        out.writeBytes(stnParm);
        out.writeBytes(" " + stnPargetUnits);
        if (stc < sech.mNumStnVars - 1) {
          out.writeBytes(delim);
        }
      }
      out.writeBytes("\r");

      int stnCount = 0;

      // write the database records
      for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
        of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

        for (int sec = 0; sec < of.mNumSections; sec++) {
          sech = (Section)of.mSections.elementAt(sec);

          for (int stc = 0; stc < sech.mStations.size(); stc++) {
            progress.setPercentComplete(100.0 * ((double)stnCount / (double)mFileViewer.getTotalNumStations()));
            Station sh = (Station)sech.mStations.elementAt(stc);
            if (!sh.mUseStn || !stnKeepList[stnCount++]) {
              continue;
            }

            // section identifier
            out.writeBytes(sech.mSectionDescription + delim);

            // stn number
            out.writeBytes(sh.mStnNum + delim);

            // cast number
            out.writeBytes(sh.mCastNum + delim);

            // Latitude
            out.writeBytes(sh.mLat + delim);

            // Longitude
            out.writeBytes(sh.mLon + delim);

            // Date
            out.writeBytes(JOAFormulas.formatDate(sh, false) + delim);

            // write out the bottle data
            for (int v = 0; v < sech.mNumStnVars; v++) {
              if (sh.getStnValue(v) == JOAConstants.MISSINGVALUE) {
                out.writeBytes(missingVal + "");
              }
              else {
                out.writeBytes(sh.getStnValue(v) + "");
              }
              if (v < sech.mNumStnVars - 1) {
                out.writeBytes(delim);
              }
            }
            out.writeBytes("\r");
          }
        }
      }
      out.flush();
      out.close();
    }
    catch (Exception ex) {
      System.out.println("An Error occurred exporting the current data");
      ex.printStackTrace();
    }
    finally {
      progress.dispose();
    }
  }
}

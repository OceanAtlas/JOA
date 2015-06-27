/*
 * $Id: ConfigParameterProperties.java,v 1.5 2005/09/07 18:49:31 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.events.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.PowerOceanAtlas;

@SuppressWarnings("serial")
public class ConfigParameterProperties extends JOAJDialog implements ActionListener, ButtonMaintainer, DataAddedListener,
    ListSelectionListener {
  protected FileViewer mFileViewer;
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mApplyButton = null;
  protected JOAJButton mCancelButton = null;
  protected JOAJTextField mNameField = null;
  protected JOAJTextField mPrecField = null;
  protected JOAJTextField mUnitsField = null;
  protected JOAJTextField mDisplayNameField = null; // future
  protected JOAJCheckBox mReverseYCB = null;
  protected JOAJList mParamList = null;
	private Timer timer = new Timer();
  protected JDialog mFrame = null;
  protected boolean mIgnore = false;
  protected int mTotalParams;
  protected int mCurrParam = 0;
  protected boolean mReverseY = false;

  public ConfigParameterProperties(FileViewer fv) {
    super(fv, "Parameter Properties", false);
    mFileViewer = fv;
    this.init();
  }

  public ConfigParameterProperties(JFrame fr, FileViewer fv) {
    super(fr, "Parameter Properties", false);
    mFileViewer = fv;
    this.init();
  }

  private void buildParamList() {
    Vector<String> listData = new Vector<String>();
    for (int v = 0; v < mFileViewer.gNumProperties; v++) {
      listData.addElement(mFileViewer.mAllProperties[v].getVarLabel());
      mTotalParams++;
    }

    if (mParamList == null) {
      mParamList = new JOAJList(listData);
    }
    else {
      mParamList.setListData(listData);
      mParamList.invalidate();
    }
  }

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    // fill the param list
    buildParamList();

    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(0, 0));

    JPanel paramDetailPanel = new JPanel();
    paramDetailPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

    JPanel paramSelPanel = new JPanel();
    paramSelPanel.setLayout(new BorderLayout(5, 0));

    //name line
    JPanel line0 = new JPanel();
    line0.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line0.add(new JOAJLabel(b.getString("kName")));
    mNameField = new JOAJTextField(10);
    mNameField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line0.add(mNameField);
    
    line0.add(new JOAJLabel(b.getString("kPrecision")));
    mPrecField = new JOAJTextField(1);
    mPrecField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line0.add(mPrecField);
    
    mReverseYCB = new JOAJCheckBox(b.getString("kReverseY"), mReverseY);
    line0.add(mReverseYCB);

    //Units line
    JPanel line1 = new JPanel();
    line1.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line1.add(new JOAJLabel(b.getString("kUnits")));
    mUnitsField = new JOAJTextField(10);
    mUnitsField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line1.add(mUnitsField);

    paramDetailPanel.add(line0);
    paramDetailPanel.add(line1);

    // station selection
    mParamList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    mParamList.setPrototypeCellValue("PARAMET");
    mParamList.setVisibleRowCount(5);
    mParamList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane listScroller = new JScrollPane(mParamList);
    mParamList.addListSelectionListener(this);
    setParam();
    mParamList.setSelectedIndex(mCurrParam);

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
    contents.add(new TenPixelBorder(paramDetailPanel, 5, 5, 5, 0), "Center");
    contents.add(new TenPixelBorder(listScroller, 5, 5, 0, 5), "West");
    contents.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
    this.pack();

    mFileViewer.addDataAddedListener(this);
    mParamList.addListSelectionListener(this);

    mFrame = this;
    WindowListener windowListener = new WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        mFileViewer.removeDataAddedListener((DataAddedListener)mFrame);
      }
    };
    this.addWindowListener(windowListener);

    runTimer();
    setParam();

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

  public void dataAdded(DataAddedEvent evt) {
    // redo the parameter list
    buildParamList();
  }

  public void valueChanged(ListSelectionEvent evt) {
    mCurrParam = mParamList.getSelectedIndex();
    setParam();
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      timer.cancel();
      mFileViewer.removeDataAddedListener(this);
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      applyChangesToParam();
      timer.cancel();
      mFileViewer.removeDataAddedListener(this);
      this.dispose();
    }
    else if (cmd.equals("apply")) {
      applyChangesToParam();
    }
  }

  public void setParam() {
    mUnitsField.setText(mFileViewer.mAllProperties[mCurrParam].getUnits());
    mNameField.setText(mFileViewer.mAllProperties[mCurrParam].getVarLabel());
    mPrecField.setText(Integer.toString(mFileViewer.mAllProperties[mCurrParam].getDisplayPrecision()));
    mReverseY = mFileViewer.mAllProperties[mCurrParam].isReverseY();
    mReverseYCB.setSelected(mReverseY);
  }

  public void applyChangesToParam() {
    // apply data in UI to current parameter
    try {
      mFileViewer.mAllProperties[mCurrParam].setUnits(mUnitsField.getText());
    }
    catch (Exception ex) {

    }
    String param = mNameField.getText();
    //param = JOAFormulas.formatParamName(param);

    if (!param.equalsIgnoreCase(mFileViewer.mAllProperties[mCurrParam].getVarLabel())) {
      // have to change parameter name in all sections
      for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
        OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

        for (int sec = 0; sec < of.mNumSections; sec++) {
          Section sech = (Section)of.mSections.elementAt(sec);

          int pos = sech.getVarPos(mFileViewer.mAllProperties[mCurrParam].getVarLabel(), false);
          if (pos >= 0) {
            sech.setIthParamName(pos, param);
          }
        }
      }

      mFileViewer.mAllProperties[mCurrParam].setVarLabel(param);
    }

    int prec = JOAConstants.JOA_DEFAULT_PRECISION;
    try {
    	prec = Integer.valueOf(mPrecField.getText());
    }
    catch (NumberFormatException ex) {
    	
    }
    
    mFileViewer.mAllProperties[mCurrParam].setDisplayPrecision(prec);
    mFileViewer.mAllProperties[mCurrParam].setReverseY(mReverseYCB.isSelected());

    // broadcast an event to cause various plots to redraw
    PrefsChangedEvent pce = new PrefsChangedEvent(PowerOceanAtlas.getInstance());
    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(pce);
  }

  public void maintainButtons() {
  }
}

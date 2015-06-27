/*
 * $Id: ConfigColorbar.java,v 1.7 2005/09/07 18:49:30 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import java.io.*;
import javaoceanatlas.calculations.*;
import javaoceanatlas.events.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.resources.*;
import gov.noaa.pmel.util.*;

@SuppressWarnings("serial")
public class ConfigColorbar extends JOAJDialog implements ActionListener, ButtonMaintainer, ListSelectionListener {
  protected FileViewer mFileViewer = null;
  protected ResourceBundle b = null;
  protected JOAJList mPaletteList = null;
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mCancelButton = null;
  protected JOAJButton mDeleteButton = null;
  protected JOAJButton mApplyButton = null;
  protected JOAJButton mEditButton = null;
  protected JOAJButton mNewButton = null;
  protected int mSelectedPalette = -99;
  protected int mOldSelectedPalette = -99;
  protected boolean mEatValueChanged = false;
  protected NewColorBar mColorBar = null;
  protected int mSelectedCBAR = -99;
  protected ColorBarPanel mColorBarPanel = null;
  protected JPanel mUpperContents = null;
	private Timer timer = new Timer();
  protected JOAWindow mParent;

  public ConfigColorbar(JOAWindow par, FileViewer fv) {
    super(par, "Contour Manager", false);
    mParent = par;
    mFileViewer = fv;
    init();
  }

  public void init() {
    b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
    Container contents = getContentPane();
    contents.setLayout(new BorderLayout(5, 5));

    mUpperContents = new JPanel();
    mUpperContents.setLayout(new BorderLayout(5, 5)); //new RowLayout(Orientation.LEFT, Orientation.CENTER, 5));

    // color palette list
    JPanel palPanel = new JPanel();
    palPanel.setLayout(new BorderLayout(5, 5));
    JOAJLabel l1 = new JOAJLabel(b.getString("kColorbars"), JOAJLabel.LEFT);
    palPanel.add(l1, "North");
    Vector<String> listData = JOAFormulas.getColorBarList();
    mPaletteList = new JOAJList(listData);
    MouseListener mouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          ContourEditor configDialog = new ContourEditor(mParent, mFileViewer, mColorBar);
          configDialog.pack();
          configDialog.setVisible(true);
        }
      }
    };
    mPaletteList.addMouseListener(mouseListener);
    if (JOAConstants.ISMAC) {
      mPaletteList.setVisibleRowCount(15);
    }
    else {
      mPaletteList.setVisibleRowCount(10);
    }
    mPaletteList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane listScroller = new JScrollPane(mPaletteList);
    palPanel.add(listScroller, "Center");
    mPaletteList.addListSelectionListener(this);

    // buttons
    JPanel buttPanel = new JPanel();
    buttPanel.setLayout(new GridLayout(1, 3, 5, 5));
    mEditButton = new JOAJButton(b.getString("kEdit"));
    mNewButton = new JOAJButton(b.getString("kNew"));
    mDeleteButton = new JOAJButton(b.getString("kDelete"));
    mEditButton.setActionCommand("edit");
    mNewButton.setActionCommand("new");
    mDeleteButton.setActionCommand("delete");
    mEditButton.addActionListener(this);
    mNewButton.addActionListener(this);
    mDeleteButton.addActionListener(this);
    buttPanel.add(mEditButton);
    buttPanel.add(mNewButton);
    buttPanel.add(mDeleteButton);
    palPanel.add("South", buttPanel);

    mUpperContents.add("West", palPanel);
    contents.add("Center", new TenPixelBorder(mUpperContents, 5, 5, 5, 5));

    // lower Panel
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
    dlgBtnsPanel.setLayout(new GridLayout(1, 3, 15, 1));
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
    contents.add("South", new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5));

    mOKBtn.addActionListener(this);
    mApplyButton.addActionListener(this);
    mCancelButton.addActionListener(this);

		runTimer();
    this.pack();
    Dimension d = this.getSize();
    this.setSize(d.width + 100, d.height);

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

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      timer.cancel();
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      if (!mColorBar.isMetadataColorBar() && !JOAFormulas.paramExists(mFileViewer, mColorBar.getParam())) {
        if (JOAFormulas.isCalculatable(mColorBar.getParam())) {
          // make a new calculation
          Calculation calc = JOAFormulas.createCalcFromName(mFileViewer, mColorBar.getParam());

          if (calc != null) {
            // do calculation
            mFileViewer.addCalculation(calc);
            mFileViewer.doCalcs();

            // fire color bar changed event
            ColorBarChangedEvent cbce = new ColorBarChangedEvent(mFileViewer);
            cbce.setColorBar(mColorBar);
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(cbce);
            timer.cancel();
            this.dispose();
          }
        }
        else {
          JFrame f = new JFrame("Contour Manager Error");
          Toolkit.getDefaultToolkit().beep();
          JOptionPane.showMessageDialog(f,
                                        "Colorbar parameter does not exist in this file" + "\n" + "and couldn't be calculated");
        }
      }
      else {
        ColorBarChangedEvent cbce = new ColorBarChangedEvent(mFileViewer);
        cbce.setColorBar(mColorBar);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(cbce);
        timer.cancel();
        this.dispose();
      }

      if (mColorBar.isMetadataColorBar()) {
        // add a metadatalistener to this colorbar
        // first clear out any other metadatachangelisteners
        mColorBar.setMetadata(mFileViewer.getMinLat(), mFileViewer.getMaxLat(), mFileViewer.getMinLon(),
                              mFileViewer.getMaxLon(), mFileViewer.getMinDate(), mFileViewer.getMaxDate());

        // add this as a listener
        mFileViewer.addMetadataChangedListener(mColorBar);
      }
    }
    else if (cmd.equals("apply")) {
      if (!mColorBar.isMetadataColorBar() && !JOAFormulas.paramExists(mFileViewer, mColorBar.getParam())) {
        if (JOAFormulas.isCalculatable(mColorBar.getParam())) {
          // make a new calculation
          Calculation calc = JOAFormulas.createCalcFromName(mFileViewer, mColorBar.getParam());

          if (calc != null) {
            // do calculation
            mFileViewer.addCalculation(calc);
            mFileViewer.doCalcs();

            // fire color bar changed event
            ColorBarChangedEvent cbce = new ColorBarChangedEvent(mFileViewer);
            cbce.setColorBar(mColorBar);
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(cbce);
          }
        }
        else {
          JFrame f = new JFrame("Contour Manager Error");
          Toolkit.getDefaultToolkit().beep();
          JOptionPane.showMessageDialog(f,
                                        "Colorbar parameter does not exist in this " + "\n" + "data set and couldn't be calculated.");
        }
      }
      else {
        ColorBarChangedEvent cbce = new ColorBarChangedEvent(mFileViewer);
        cbce.setColorBar(mColorBar);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(cbce);
      }

      if (mColorBar.isMetadataColorBar()) {
        // add a metadatalistener to this colorbar
        // first clear out any other metadatachangelisteners
        mColorBar.setMetadata(mFileViewer.getMinLat(), mFileViewer.getMaxLat(), mFileViewer.getMinLon(),
                              mFileViewer.getMaxLon(), mFileViewer.getMinDate(), mFileViewer.getMaxDate());

        // add this as a listener
        mFileViewer.addMetadataChangedListener(mColorBar);
      }
    }
    else if (cmd.equals("edit")) {
      // edit existing color bar
      ContourEditor configDialog = new ContourEditor(mParent, mFileViewer, mColorBar);
      configDialog.pack();
      configDialog.setVisible(true);
    }
    else if (cmd.equals("new")) {
      // create a new color bar
      ContourEditor configDialog = new ContourEditor(mParent, mFileViewer);
      configDialog.pack();
      configDialog.setVisible(true);
    }
    else if (cmd.equals("delete")) {
      // get a File object for selected cbar
      String cBARName = (String)mPaletteList.getSelectedValue();
      String dir = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
      File nf = new File(dir, cBARName);
      if (nf != null) {
        nf.delete();
        cleanUpAfterDelete();
      }
    }
  }

  public void maintainButtons() {
    if (mColorBar != null) {
      if (mFileViewer != null) {
        mOKBtn.setEnabled(true);
        mApplyButton.setEnabled(true);
      }
      mDeleteButton.setEnabled(true);
      mEditButton.setEnabled(true);
    }
    else {
      mOKBtn.setEnabled(false);
      mDeleteButton.setEnabled(false);
      mApplyButton.setEnabled(false);
      mEditButton.setEnabled(false);
    }
  }

  public void valueChanged(ListSelectionEvent evt) {
    int tempSelectedCBAR = mPaletteList.getSelectedIndex();
    String newCBARName = (String)mPaletteList.getSelectedValue();
    if (newCBARName == null || tempSelectedCBAR == mSelectedCBAR) {
      return;
    }
    mSelectedCBAR = tempSelectedCBAR;

    // read the color bar from disk
    try {
      mColorBar = JOAFormulas.getColorBar(newCBARName);
      if (mColorBar.isMetadataColorBar()) {
        // if a data file is not open set to an arbitrary range
        if (mFileViewer == null) {
          mColorBar.setMetadata( -90, 90, -180, 180, new GeoDate(5, 11, 1955, 0, 0, 0, 0),
                                new GeoDate(5, 11, 2003, 0, 0, 0, 0));
        }
        else {
          mColorBar.setMetadata(mFileViewer.getMinLat(), mFileViewer.getMaxLat(), mFileViewer.getMinLon(),
                                mFileViewer.getMaxLon(), mFileViewer.getMinDate(), mFileViewer.getMaxDate());
        }

      }
    }
    catch (Exception ex) {
      return;
    }

    // display the preview color bar
    if (mColorBarPanel != null) {
      Component[] comps = mUpperContents.getComponents();
      for (int i = 0; i < comps.length; i++) {
        if (comps[i] instanceof TenPixelBorder) {
          mUpperContents.remove(comps[i]);
          comps[i] = null;
        }
      }
      // remove existing color bar component (if there is one)
      mColorBarPanel = null;
    }
    mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mColorBar);
    mColorBarPanel.setLinked(false);
    mColorBarPanel.setEnhanceable(false);
    mUpperContents.add("East", new TenPixelBorder(mColorBarPanel, 15, 0, 0, 0));
    mUpperContents.invalidate();
    this.validate();
  }

  protected void cleanUpAfterDelete() {
    if (mColorBarPanel != null) {
      Component[] comps = mUpperContents.getComponents();
      for (int i = 0; i < comps.length; i++) {
        if (comps[i] instanceof TenPixelBorder) {
          mUpperContents.remove(comps[i]);
          comps[i] = null;
        }
      }
      mColorBarPanel = null;
    }
    mUpperContents.invalidate();
    this.invalidate();
    this.setSize(this.getSize().width, this.getSize().height + 1);
    this.setSize(this.getSize().width, this.getSize().height);

    // redo the list
    Vector <String>listData = JOAFormulas.getColorBarList();
    mPaletteList.setListData(listData);
    mColorBar = null;
  }
}

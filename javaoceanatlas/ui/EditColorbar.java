/*
 * $Id: EditColorbar.java,v 1.3 2005/06/17 18:08:53 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import java.io.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class EditColorbar extends JOAJDialog implements ActionListener, ButtonMaintainer {
  protected FileViewer mFileViewer = null;
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mApplyBtn = null;
  protected JOAJButton mCancelButton = null;
  protected JOAJButton mMakeDefaultButton = null;
  protected NewColorBar mColorBar = null;
  protected double[] mColorBarValues = null;
  protected Color[] mColorBarColors = null;
  protected double[] mOldColorBarValues = null;
  protected Color[] mOldColorBarColors = null;
  protected ColorArrayEditor mArrayEditor = null;
  protected DialogClient mClient;
	private Timer timer = new Timer();
  protected int mNumLevels = 0;
  protected boolean mShowYAxisVals = true;

  public EditColorbar(JOAWindow par, FileViewer fv, DialogClient client, NewColorBar cbar, boolean showYAxis) {
    super(par, "Colorbar Editor", false);
    mFileViewer = fv;
    mColorBarValues = cbar.getValues();
    mColorBarColors = cbar.getColors();
    mColorBar = cbar;
    mNumLevels = cbar.getNumLevels();
    mOldColorBarValues = new double[mNumLevels];
    mOldColorBarColors = new Color[mNumLevels];
    for (int i = 0; i < mNumLevels; i++) {
      mOldColorBarValues[i] = cbar.getDoubleValue(i);
      mOldColorBarColors[i] = new Color(cbar.getColorValue(i).getRGB());
    }
    mShowYAxisVals = showYAxis;
    mClient = client;
    init();
  }

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(5, 5));

    JPanel mainPanel = new JPanel(); // everything goes in here
    mainPanel.setLayout(new BorderLayout(5, 0));

    // Value Editor
    boolean showFlds = !mColorBar.isMetadataColorBar();
    mArrayEditor = new ColorArrayEditor(this.getRootPane(), true, showFlds);
    mArrayEditor.setNumLevels(mColorBar.getNumLevels());
    mArrayEditor.setValueArray(mColorBarValues, mColorBar.getNumLevels());
    mArrayEditor.setColorArray(mColorBarColors, mColorBar.getNumLevels());
    mArrayEditor.setHistogram(mColorBar.getParam(), mFileViewer, mColorBar);
    mArrayEditor.setDisplayYAxis(mShowYAxisVals);
    mainPanel.add("Center", new TenPixelBorder(mArrayEditor, 10, 10, 0, 10));

    // button panel
    mOKBtn = new JOAJButton(b.getString("kOK"));
    mOKBtn.setActionCommand("ok");
    this.getRootPane().setDefaultButton(mOKBtn);
    mOKBtn.addActionListener(this);
    mApplyBtn = new JOAJButton(b.getString("kApply"));
    mApplyBtn.setActionCommand("apply");
    mApplyBtn.addActionListener(this);
    mCancelButton = new JOAJButton(b.getString("kCancel"));
    mCancelButton.setActionCommand("cancel");
    mCancelButton.addActionListener(this);
    mMakeDefaultButton = new JOAJButton(b.getString("kSave"));
    mMakeDefaultButton.setActionCommand("save");
    mMakeDefaultButton.addActionListener(this);
    JPanel dlgBtnsInset = new JPanel();
    JPanel dlgBtnsPanel = new JPanel();
    dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
    dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
    if (JOAConstants.ISMAC) {
      dlgBtnsPanel.add(mCancelButton);
      dlgBtnsPanel.add(mMakeDefaultButton);
      dlgBtnsPanel.add(mApplyBtn);
      dlgBtnsPanel.add(mOKBtn);
    }
    else {
      dlgBtnsPanel.add(mOKBtn);
      dlgBtnsPanel.add(mApplyBtn);
      dlgBtnsPanel.add(mMakeDefaultButton);
      dlgBtnsPanel.add(mCancelButton);
    }
    dlgBtnsInset.add(dlgBtnsPanel);

    // add all the sub panels to main panel
    mainPanel.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
    contents.add("Center", mainPanel);
    this.pack();

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

  public void restoreOriginalValues() {
    for (int i = 0; i < mColorBar.getNumLevels(); i++) {
      mColorBarValues[i] = mOldColorBarValues[i];
    }
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      restoreOriginalValues();
      mClient.dialogCancelled(this);
      timer.cancel();
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      mClient.dialogDismissed(this);
      timer.cancel();
      this.dispose();
    }
    else if (cmd.equals("apply")) {
      mClient.dialogApply(this);
    }
    else if (cmd.equals("save")) {
      save();
    }
  }

  public void maintainButtons() {
    if (mColorBar == null) {
      mOKBtn.setEnabled(false);
    }
    else {
      mOKBtn.setEnabled(true);
    }
  }

  public void save() {
    // save the color palette object to the support directory
    String suggestedName = mColorBar.getParam() + "-" + mColorBar.getTitle() + "_cbr.xml";

    // get a filename
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        if (name.endsWith(".cbr") || name.endsWith("_cbr.xml")) {
          return true;
        }
        else {
          return false;
        }
      }
    };

    Frame fr = new Frame();
    String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
    FileDialog f = new FileDialog(fr, "Save colorbar as:", FileDialog.SAVE);
    f.setDirectory(directory);
    f.setFilenameFilter(filter);
    if (suggestedName != null) {
      f.setFile(suggestedName);
    }
    else {
      f.setFile("untitled_cbr.xml");
    }
    f.setVisible(true);
    directory = f.getDirectory();
    f.dispose();
    if (directory != null && f.getFile() != null) {
      File nf = new File(directory, f.getFile());
      JOAFormulas.saveColorBar(nf, mColorBar);
    }
  }

  public NewColorBar getColorBar() {
    mColorBar.setValues(mArrayEditor.getValueArray());
    mColorBar.setNumLevels(mArrayEditor.getNumLevels());
    return mColorBar;
  }

  public int getNumLevels() {
    return mNumLevels;
  }
}

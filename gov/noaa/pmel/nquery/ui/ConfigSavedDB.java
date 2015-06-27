/*
 * $Id: ConfigSavedDB.java,v 1.4 2004/09/14 19:11:26 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package gov.noaa.pmel.nquery.ui;

import gov.noaa.pmel.nquery.resources.NQueryConstants;
import javax.swing.JDialog;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javaoceanatlas.utility.DialogClient;
import javax.swing.JFrame;
import java.awt.Container;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Cursor;
import javax.swing.JLabel;
import javaoceanatlas.utility.TenPixelBorder;
import javaoceanatlas.utility.ColumnLayout;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javaoceanatlas.utility.ButtonMaintainer;
import javaoceanatlas.utility.Orientation;
import javax.swing.JFileChooser;
import java.io.File;
import javaoceanatlas.ui.widgets.JOAJDialog;

public class ConfigSavedDB extends JOAJDialog implements ActionListener, ButtonMaintainer {
  protected JTextField mDBNameFld = null;
  protected JTextField mDBComment = null;
  protected JTextField mDBSaveDir = null;

  // whole dialog widgets
  private JButton mOKBtn = null;
  private JButton mCancelButton = null;
  private ResourceBundle b = ResourceBundle.getBundle("gov.noaa.pmel.nquery.resources.NQueryResources");
  private DialogClient mClient;
  private JDialog mThis;
	private Timer timer = new Timer();
  private String mDBName;

  public ConfigSavedDB(JFrame par, String dbName, DialogClient client) {
    super(par, "", true);
    mDBName = dbName;

    this.setTitle(b.getString("kSaveDatabaseas"));
    mClient = client;
    mThis = this;
    init();
  }

  public void init() {
    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(5, 5));
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout(5, 5));

    // layout for entire panel
    JPanel thisPanel = new JPanel();
    thisPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 1));

    // make a column of labels
    JPanel labelPanel = new JPanel();
    labelPanel.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 13));
    labelPanel.add(new JLabel(b.getString("kDBName")));
    labelPanel.add(new JLabel(b.getString("kDBComment")));
    labelPanel.add(new JLabel(b.getString("kDBDir")));

    JPanel fieldPanel = new JPanel();
    fieldPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));
    mDBNameFld = new JTextField(15);
    mDBNameFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mDBNameFld.setText(mDBName);

    mDBComment = new JTextField(30);
    mDBComment.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

    JPanel defDir = new JPanel();
    defDir.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
    mDBSaveDir = new JTextField(30);
    mDBSaveDir.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mDBSaveDir.setText(NQueryConstants.DEFAULT_DB_SAVE_DIR);
    defDir.add(mDBSaveDir);
    JButton browseBtn = new JButton();
    browseBtn = new JButton(b.getString("kBrowse2"));
    browseBtn.setActionCommand("browse");
    browseBtn.addActionListener(this);
    defDir.add(browseBtn);

    fieldPanel.add(mDBNameFld);
    fieldPanel.add(mDBComment);
    fieldPanel.add(mDBSaveDir);

    thisPanel.add(labelPanel);
    thisPanel.add(fieldPanel);
    mainPanel.add("Center", new TenPixelBorder(thisPanel, 5, 5, 5, 5));

    // lower panel
    mOKBtn = new JButton(b.getString("kOK"));
    mOKBtn.setActionCommand("ok");
    this.getRootPane().setDefaultButton(mOKBtn);
    mCancelButton = new JButton(b.getString("kCancel"));
    mCancelButton.setActionCommand("cancel");
    JPanel dlgBtnsInset = new JPanel();
    JPanel dlgBtnsPanel = new JPanel();
    dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
    dlgBtnsPanel.setLayout(new GridLayout(1, 3, 15, 1));
    if (NQueryConstants.ISMAC) {
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

    mainPanel.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
    contents.add("Center", mainPanel);
    this.pack();

    // show dialog at center of screen
    Rectangle dBounds = this.getBounds();
    Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
    int x = sd.width / 2 - dBounds.width / 2;
    int y = sd.height / 2 - dBounds.height / 2;
    this.setLocation(x, y);

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

  public String getDirectory() {
    return mDBSaveDir.getText();
  }

  public String getName() {
    return mDBNameFld.getText();
  }

  public String getComment() {
    return mDBComment.getText();
  }

  public void maintainButtons() {
    boolean nameOK = mDBNameFld.getText() != null && mDBNameFld.getText().length() > 0;
    boolean pathOK = mDBSaveDir.getText() != null && mDBSaveDir.getText().length() > 0;

    if (!nameOK || !pathOK) {
      mOKBtn.setEnabled(false);
    }
    else {
      mOKBtn.setEnabled(true);
    }
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("cancel")) {
    	timer.cancel();
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      // save this as an xml file
      mClient.dialogDismissed(this);
      timer.cancel();
      this.dispose();
    }
    else if (cmd.equals("browse")) {
      JFileChooser chooser = new JFileChooser();
      chooser.setCurrentDirectory(new java.io.File("."));
      chooser.setDialogTitle(b.getString("kSelectDirectory"));
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      chooser.setApproveButtonText(b.getString("kSelect"));
      if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        mDBSaveDir.setText(chooser.getCurrentDirectory().getPath() + File.separator +
                             chooser.getSelectedFile().getName());
      }
    }
  }
}

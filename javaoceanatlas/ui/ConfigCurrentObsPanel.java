/*
 * $Id: ConfigCurrentObsPanel.java,v 1.2 2005/06/17 18:08:52 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class ConfigCurrentObsPanel extends JOAJDialog implements ActionListener, ButtonMaintainer {
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mApplyButton = null;
  protected JOAJButton mCancelButton = null;
  protected JOAJCheckBox mDisplayUnits = null;
  protected JOAJCheckBox mDisplayQC = null;
	private Timer timer = new Timer();
  protected JDialog mFrame = null;
  protected boolean mIgnore = false;
  protected DialogClient mClient;
  protected boolean mShowUnits;
  protected boolean mShowQC;
  protected boolean mReverseY = false;

  public ConfigCurrentObsPanel(JOAWindow w, DialogClient client, boolean displayUnits, boolean displayQC) {
    super(w, "Configure Observation Panel", false);
    mClient = client;
    mShowUnits = displayUnits;
    mShowQC = displayQC;
    this.init();
  }

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(0, 0));

    JPanel detailPanel = new JPanel();
    detailPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

    // display units toggle
    JPanel line2 = new JPanel();
    line2.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    mDisplayUnits = new JOAJCheckBox(b.getString("kDisplayUnits"), mShowUnits);
    line2.add(mDisplayUnits);

    // display quality codes
    JPanel line3 = new JPanel();
    line3.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    mDisplayQC = new JOAJCheckBox(b.getString("kDisplayQC"), mShowQC);
    line3.add(mDisplayQC);

    detailPanel.add(line2);
    detailPanel.add(line3);

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
    contents.add(new TenPixelBorder(detailPanel, 5, 5, 5, 0), "Center");
    contents.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
    this.pack();

    mFrame = this;

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

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      //mClient.dialogCancelled(this);
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
  }

  public void maintainButtons() {
  }

  public boolean getDisplayUnits() {
    return mDisplayUnits.isSelected();
  }

  public boolean getDisplayQC() {
    return mDisplayQC.isSelected();
  }
}

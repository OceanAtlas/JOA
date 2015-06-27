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
public class ConfigNewColumn extends JOAJDialog implements ActionListener, ButtonMaintainer {
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mApplyButton = null;
  protected JOAJButton mCancelButton = null;
	private Timer timer = new Timer();
  protected JDialog mFrame = null;
  protected boolean mIgnore = false;
  protected DialogClient mClient;
  protected boolean mReverseY = false;
  private JTextField mColName;
  private JTextField mColUnits;
  private JTextField mFillValue;
  private JTextField mColPrec;

  public ConfigNewColumn(JOAWindow w, DialogClient client) {
    super(w, "Configure New Merge Column", false);
    mClient = client;
    this.init();
  }

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(0, 0));

    JPanel detailPanel = new JPanel();
    detailPanel.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 5));

    JPanel line1 = new JPanel();
    line1.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line1.add(new JLabel(b.getString("kNewParameterName")));
    mColName = new JTextField(10);
    mColName.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    //mColName.getDocument().addDocumentListener(this);
    line1.add(mColName);

    JPanel line2 = new JPanel();
    line2.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line2.add(new JLabel(b.getString("kUnits")));
    mColUnits = new JTextField(10);
    mColUnits.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    //mColName.getDocument().addDocumentListener(this);
    line2.add(mColUnits);

    JPanel line2a = new JPanel();
    line2a.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line2a.add(new JLabel(b.getString("kLabelPrec")));
    mColPrec = new JTextField(5);
    mColPrec.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    //mColName.getDocument().addDocumentListener(this);
    line2a.add(mColPrec);
    
    JPanel line3 = new JPanel();
    line3.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line3.add(new JLabel(b.getString("kFillValue")));
    mFillValue = new JTextField(10);
    mFillValue.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    //mFillValue.getDocument().addDocumentListener(this);
    line3.add(mFillValue);

    detailPanel.add(line1);
    detailPanel.add(line2);
    detailPanel.add(line2a);
    detailPanel.add(line3);

    // lower panel
    mOKBtn = new JOAJButton(b.getString("kOK"));
    mOKBtn.setActionCommand("ok");
    this.getRootPane().setDefaultButton(mOKBtn);
    mCancelButton = new JOAJButton(b.getString("kDone"));
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
    contents.add(new TenPixelBorder(detailPanel, 5, 5, 5, 5), "Center");
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

  public String getParam() {
    return mColName.getText();
  }

  public String getUnits() {
    return mColUnits.getText();
  }

  public int getPrecision() {
  	try{
  		return Integer.parseInt(mColPrec.getText());
  	}
  	catch (Exception ex) {
  		return 2;
  	}
  }

  public String getFill() {
    return mFillValue.getText();
  }

  public void maintainButtons() {
  }

}

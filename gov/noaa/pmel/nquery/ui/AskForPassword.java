/*
 * $Id: AskForPassword.java,v 1.5 2005/04/06 21:19:42 oz Exp $
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
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.util.ResourceBundle;
import javaoceanatlas.utility.DialogClient;
import javax.swing.JFrame;
import java.awt.Container;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import javaoceanatlas.utility.ColumnLayout;
import javax.swing.JLabel;
import javaoceanatlas.utility.Orientation;
import java.awt.Cursor;
import javaoceanatlas.utility.TenPixelBorder;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javaoceanatlas.ui.widgets.JOAJDialog;

public class AskForPassword extends JOAJDialog implements ActionListener {
  protected JPasswordField password = null;

  // whole dialog widgets
  protected JButton mOKBtn = null;
  protected JButton mCancelButton = null;
  protected ResourceBundle b = ResourceBundle.getBundle("gov.noaa.pmel.nquery.resources.NQueryResources");
  DialogClient mClient;
  JDialog mThis;
  String mPass;

  public AskForPassword(JFrame par, DialogClient client) {
    super(par, "", true);
    this.setTitle(b.getString("kEnterServerPassword"));
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
    labelPanel.add(new JLabel(b.getString("kEnterPassword")));

    JPanel fieldPanel = new JPanel();
    fieldPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));
    password = new JPasswordField(15);
    password.setEchoChar('#');
    password.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    fieldPanel.add(password);

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
  }

  public String getPassword() {
    return mPass;
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("cancel")) {
      mPass = null;
      this.dispose();
      mClient.dialogCancelled(this);
    }
    else if (cmd.equals("ok")) {
      mPass = password.getText();
      if (mPass == null) {
        mPass = "";
      }
      mClient.dialogDismissed(this);
      this.dispose();
    }
  }
}

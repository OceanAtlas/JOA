package gov.noaa.pmel.eps2;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;
//import javax.swing.event.HyperlinkListener;
//import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.text.html.HTMLDocument;

/**
 * Title:        netCDF File Browser
 * Description:  General purpose netCDF file Browser.
 * Copyright:    Copyright (c) 2000
 * Company:      NOAA/PMEL/EPIC
 * @author Donald Denbo
 * @version 1.0
 */

public class OPeNDAPSelectionDialog extends JDialog {
  JPanel panel1 = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel buttonPanel = new JPanel();
  JPanel navigationPanel = new JPanel();
  JButton acceptButton = new JButton();
  JButton cancelButton = new JButton();
  JPanel fileURLPanel = new JPanel();
  JLabel jLabel2 = new JLabel();
  JTextField fileURLField = new JTextField();
  ImageIcon backImage_;
  URL currentURL_ = null;

  private String filePath = null;
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  JLabel jLabel1 = new JLabel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();

  public OPeNDAPSelectionDialog(Frame frame, String title, boolean modal) {
    super(frame, title, modal);
    try {
      jbInit();
      pack();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  public OPeNDAPSelectionDialog() {
    this(null, "", false);
  }
  void jbInit() throws Exception {
    //backImage_ = new ImageIcon(getClass().getResource("images/Back16.gif"));
    panel1.setLayout(borderLayout1);
    acceptButton.setText("Accept");
    acceptButton.addActionListener(new OPeNDAPSelectionDialog_acceptButton_actionAdapter(this));
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new OPeNDAPSelectionDialog_cancelButton_actionAdapter(this));
    buttonPanel.setBorder(BorderFactory.createEtchedBorder());
    navigationPanel.setLayout(gridBagLayout1);
    jLabel2.setText("Data File URL:");
    fileURLField.setColumns(40);
    this.setTitle("OPeNDAP Connection Dialog");
//    panel1.setMinimumSize(new Dimension(550, 700));
//    panel1.setPreferredSize(new Dimension(550, 700));
    fileURLPanel.setLayout(gridBagLayout2);
    jLabel1.setFont(new java.awt.Font("Dialog", 0, 14));
    jLabel1.setText("Enter OPeNDAP URL");
    getContentPane().add(panel1);
    panel1.add(buttonPanel, BorderLayout.SOUTH);
    buttonPanel.add(acceptButton, null);
    buttonPanel.add(cancelButton, null);
    panel1.add(navigationPanel, BorderLayout.CENTER);
    navigationPanel.add(fileURLPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    fileURLPanel.add(jLabel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 15, 5, 0), 0, 0));
    fileURLPanel.add(fileURLField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 15), 0, 0));
    navigationPanel.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(15, 10, 10, 10), 0, 0));
  }

  public String getOPeNDAPPath() {
    return filePath;
  }

  void acceptButton_actionPerformed(ActionEvent e) {
    filePath = fileURLField.getText();
    setVisible(false);
  }

  void cancelButton_actionPerformed(ActionEvent e) {
    filePath = null;
    setVisible(false);
  }
}

class OPeNDAPSelectionDialog_acceptButton_actionAdapter implements java.awt.event.ActionListener {
  OPeNDAPSelectionDialog adaptee;

  OPeNDAPSelectionDialog_acceptButton_actionAdapter(OPeNDAPSelectionDialog adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.acceptButton_actionPerformed(e);
  }
}

class OPeNDAPSelectionDialog_cancelButton_actionAdapter implements java.awt.event.ActionListener {
  OPeNDAPSelectionDialog adaptee;

  OPeNDAPSelectionDialog_cancelButton_actionAdapter(OPeNDAPSelectionDialog adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.cancelButton_actionPerformed(e);
  }
}

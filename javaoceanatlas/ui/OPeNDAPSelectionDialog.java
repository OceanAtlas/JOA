/*
 * $Id: OPeNDAPSelectionDialog.java,v 1.2 2005/06/17 18:08:54 oz Exp $
 *
 */

package javaoceanatlas.ui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.net.URL;
import javaoceanatlas.ui.widgets.*;

/**
 * Title:        netCDF File Browser
 * Description:  General purpose netCDF file Browser.
 * Copyright:    Copyright (c) 2000
 * Company:      NOAA/PMEL/EPIC
 * @author Donald Denbo
 * @version 1.0
 */

@SuppressWarnings("serial")
public class OPeNDAPSelectionDialog extends JOAJDialog {
  JPanel panel1 = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel buttonPanel = new JPanel();
  JPanel navigationPanel = new JPanel();
  JOAJButton acceptButton = new JOAJButton();
  JOAJButton cancelButton = new JOAJButton();
  JPanel fileURLPanel = new JPanel();
  JOAJLabel jLabel2 = new JOAJLabel();
  JOAJTextField fileURLField = new JOAJTextField();
  ImageIcon backImage_;
  URL currentURL_ = null;

  private String filePath = null;
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  JOAJLabel jLabel1 = new JOAJLabel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();

  public OPeNDAPSelectionDialog(JFrame frame, String title, boolean modal) {
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
    GridBagConstraints gbc1 = new GridBagConstraints();//(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
    	//GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
    gbc1.gridx = 0;
    gbc1.gridy = 1;
    gbc1.gridwidth = 1;
    gbc1.gridheight = 1;
    gbc1.weightx = 1.0;
    gbc1.weighty = 1.0;
    gbc1.anchor = GridBagConstraints.CENTER;
    gbc1.fill = GridBagConstraints.BOTH;
    gbc1.insets =  new Insets(0, 0, 0, 0);
    gbc1.ipadx = 0;
    gbc1.ipady = 0;
    navigationPanel.add(fileURLPanel, gbc1);

    GridBagConstraints gbc2 = new GridBagConstraints();//0, 0, 1, 1, 0.0, 0.0
            //,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 15, 5, 0), 0, 0);
    gbc2.gridx = 0;
    gbc2.gridy = 0;
    gbc2.gridwidth = 1;
    gbc2.gridheight = 1;
    gbc2.weightx = 0.0;
    gbc2.weighty = 0.0;
    gbc2.anchor = GridBagConstraints.WEST;
    gbc2.fill = GridBagConstraints.NONE;
    gbc2.insets =  new Insets(5, 15, 5, 0);
    gbc2.ipadx = 0;
    gbc2.ipady = 0;
    fileURLPanel.add(jLabel2, gbc2);

    GridBagConstraints gbc3 = new GridBagConstraints();//1, 0, 1, 1, 1.0, 0.0
            //,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 15), 0, 0));
    gbc3.gridx = 1;
    gbc3.gridy = 0;
    gbc3.gridwidth = 1;
    gbc3.gridheight = 1;
    gbc3.weightx = 1.0;
    gbc3.weighty = 0.0;
    gbc3.anchor = GridBagConstraints.WEST;
    gbc3.fill = GridBagConstraints.HORIZONTAL;
    gbc3.insets =  new Insets(5, 0, 5, 15);
    gbc3.ipadx = 0;
    gbc3.ipady = 0;
    fileURLPanel.add(fileURLField, gbc3);

    GridBagConstraints gbc4 = new GridBagConstraints();//0, 0, 1, 1, 0.0, 0.0
            //,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(15, 10, 10, 10), 0, 0)
    gbc4.gridx = 0;
    gbc4.gridy = 0;
    gbc4.gridwidth = 1;
    gbc4.gridheight = 1;
    gbc4.weightx = 0.0;
    gbc4.weighty = 0.0;
    gbc4.anchor = GridBagConstraints.CENTER;
    gbc4.fill = GridBagConstraints.NONE;
    gbc4.insets =  new Insets(15, 10, 10, 10);
    gbc4.ipadx = 0;
    gbc4.ipady = 0;
    navigationPanel.add(jLabel1, gbc4);
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

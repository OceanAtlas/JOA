/*
 * $Id: AboutDialog.java,v 1.6 2005/02/15 18:31:08 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */
package ndEdit;

import javaoceanatlas.ui.widgets.JOAJDialog;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.beans.*;
import java.util.zip.*;
import gov.noaa.pmel.swing.*;


public class AboutDialog extends JOAJDialog implements ActionListener {
  private static final long serialVersionUID = 1345L;
	JLabel label1;
	JLabel label2;
	JLabel label3;
	JLabel label4;
	JLabel label5;
	JLabel label6;
	JLabel label7;
	JLabel label8;
	JLabel label9;
	JLabel label10;
	JButton mOKBtn;

	public AboutDialog(JFrame parent, boolean modal){
		super(parent, "", modal);
		//this.setBackground(new Color(200, 200, 200));
		ResourceBundle b = ResourceBundle.getBundle("ndEdit.NdEditResources");
		setLayout(new BorderLayout(5, 5));
		
		// top panel
		label1 = new JLabel("NdEdit 3.0");
		label1.setFont(new Font("sansserif", Font.ITALIC+Font.BOLD, 24));
		this.add(label1, "North");
		
		// middle panel 
		JPanel middlePanel = new JPanel();
		//middlePanel.setBackground(new Color(200, 200, 200));
        middlePanel.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 2));
		label2 = new JLabel("by");
		label2.setFont(new Font("sansserif", Font.ITALIC, 12));
		
		label3 = new JLabel("John \"oz\" Osborne");
		label3.setFont(new Font("sansserif", Font.PLAIN, 18));
		
		label4 = new JLabel("NOAA/PMEL");
		label4.setFont(new Font("sansserif", Font.PLAIN, 18));
		
		label4 = new JLabel("John.Osbsorne@noaa.gov");
		label4.setFont(new Font("sansserif", Font.PLAIN, 18));
		
		label5 = new JLabel("");
		
		label6 = new JLabel("Don Denbo");
		label6.setFont(new Font("sansserif", Font.PLAIN, 18));
		
		label7 = new JLabel("NOAA/PMEL");
		label7.setFont(new Font("sansserif", Font.PLAIN, 18));
		
		label8 = new JLabel("Donald.W.Denbo@noaa.gov");
		label8.setFont(new Font("sansserif", Font.PLAIN, 18));
		
		//label9 = new JLabel("GIFEncoder is Copyright (c)1996, 1998 by Jef Poskanzer <jef@acme.com>. All rights reserved.");
		//label9.setFont(new Font("sansserif", Font.PLAIN, 12));
		
		String javaVersion = System.getProperty("java.version");
		String javaVendor = System.getProperty("java.vendor");
		String javaClassVersion = System.getProperty("java.class.version");
		
		label10 = new JLabel(javaVendor + ":" + javaVersion + "/" + javaClassVersion);
		label10.setFont(new Font("sansserif", Font.PLAIN, 10));
		
		middlePanel.add(label1);
		middlePanel.add(label2);
		middlePanel.add(label3);
		middlePanel.add(label4);
		middlePanel.add(label5);
		middlePanel.add(label6);
		middlePanel.add(label7);
		middlePanel.add(label8);
		//middlePanel.add(label9);
		middlePanel.add(label10);
		this.add(new NPixelBorder(middlePanel, 5, 5, 5, 5), "Center");
		
    mOKBtn = new JButton(b.getString("kOK"));
    getRootPane().setDefaultButton(mOKBtn);
		mOKBtn.setActionCommand("ok");
 		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
        dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
        dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
        // kludge to make the OK button bigger
        Label ll1 = new Label("               ");
    	ll1.setEnabled(false);
    	dlgBtnsPanel.add(ll1);
	    dlgBtnsPanel.add(mOKBtn);
    	Label l2 = new Label("               ");
    	l2.setEnabled(false);
    	dlgBtnsPanel.add(l2);
        dlgBtnsInset.add(dlgBtnsPanel);
    	this.add("South", new NPixelBorder(dlgBtnsInset, 5, 5, 5, 5));
        
        mOKBtn.addActionListener(this);
		setTitle("About NdEdit");
		mOKBtn.addActionListener(this);
		
		WindowListener windowListener = new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				e.getWindow().dispose();
			}
		};
		this.addWindowListener(windowListener);
	}

	public AboutDialog(JFrame parent, String title, boolean modal) {
		this(parent, modal);
		setTitle(title);
	}

	public void actionPerformed(ActionEvent event) {
		Object object = event.getSource();
		if (object == mOKBtn)
			dispose();
	}
}

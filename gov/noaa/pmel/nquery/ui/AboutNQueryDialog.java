/* * $Id: AboutDialog.java,v 1.10 2005/09/07 18:49:29 oz Exp $ * */package gov.noaa.pmel.nquery.ui;import java.awt.Dialog;import java.awt.event.ActionListener;import javax.swing.JLabel;import javax.swing.JButton;import javax.swing.JFrame;import java.util.ResourceBundle;import java.awt.BorderLayout;import java.awt.Font;import javax.swing.JPanel;import javaoceanatlas.utility.ColumnLayout;import javaoceanatlas.utility.Orientation;import javaoceanatlas.utility.TenPixelBorder;import java.awt.FlowLayout;import java.awt.GridLayout;import java.awt.event.WindowListener;import java.awt.event.WindowAdapter;import java.awt.event.ActionEvent;import java.awt.Frame;import java.awt.Rectangle;import java.awt.Toolkit;import java.awt.Dimension;import gov.noaa.pmel.nquery.resources.NQueryConstants;import gov.noaa.pmel.swing.JSystemPropertiesDialog;public class AboutNQueryDialog extends Dialog implements ActionListener {  JLabel label1;  JLabel label2;  JLabel label3;  JLabel label4;  JLabel label5;  JLabel label6;  JLabel label7;  JLabel label8;  JLabel label10;  JButton mOKBtn;  JButton mAboutNdEditBtn;  JButton mSysPropsBtn;  public AboutNQueryDialog(JFrame parent, boolean modal) {    super(parent, modal);    ResourceBundle b = ResourceBundle.getBundle("gov.noaa.pmel.nquery.resources.NQueryResources");    setLayout(new BorderLayout(5, 5));    // top panel    label1 = new JLabel("NQuery 1.0");    label1.setFont(new Font("sansserif", Font.ITALIC + Font.BOLD, 24));    this.add(label1, "North");    // middle panel    JPanel middlePanel = new JPanel();    middlePanel.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 2));    label2 = new JLabel("by");    label2.setFont(new Font("sansserif", Font.ITALIC, 12));    label3 = new JLabel("John \"oz\" Osborne");    label3.setFont(new Font("sansserif", Font.PLAIN, 18));    label4 = new JLabel("OceanAtlas Software and NOAA/PMEL");    label4.setFont(new Font("sansserif", Font.PLAIN, 18));    label4 = new JLabel("tooz@oceanatlas.com");    label4.setFont(new Font("sansserif", Font.PLAIN, 18));    label5 = new JLabel("");    label6 = new JLabel("Kevin McHugh");    label6.setFont(new Font("sansserif", Font.PLAIN, 18));    label7 = new JLabel("NOAA/PMEL");    label7.setFont(new Font("sansserif", Font.PLAIN, 18));    label8 = new JLabel("kevin.mchugh@noaa.gov");    label8.setFont(new Font("sansserif", Font.PLAIN, 18));    String javaVersion = System.getProperty("java.version");    String javaVendor = System.getProperty("java.vendor");    String javaClassVersion = System.getProperty("java.class.version");    String mrjVersion = System.getProperty("mrj.version");    if (mrjVersion == null || mrjVersion.length() == 0) {      mrjVersion = "";    }    else {      mrjVersion = "MRJ Version = " + mrjVersion;    }    label10 = new JLabel(System.getProperty("os.name") + " " + javaVendor + ":" + javaVersion + "/" + javaClassVersion +                         " " + mrjVersion);    label10.setFont(new Font("sansserif", Font.PLAIN, 10));    middlePanel.add(label1);    middlePanel.add(label2);    middlePanel.add(label3);    middlePanel.add(label4);    middlePanel.add(label5);    middlePanel.add(label6);    middlePanel.add(label7);    middlePanel.add(label8);    middlePanel.add(label10);    this.add(new TenPixelBorder(middlePanel, 5, 5, 5, 5), "Center");    mAboutNdEditBtn = new JButton(b.getString("kAboutNdEdit"));    mAboutNdEditBtn.setActionCommand("aboutndedit");    mSysPropsBtn = new JButton(b.getString("kSystemProperties"));    mSysPropsBtn.setActionCommand("sysprop");    mOKBtn = new JButton(b.getString("kOK"));    mOKBtn.setActionCommand("ok");    JPanel dlgBtnsInset = new JPanel();    JPanel dlgBtnsPanel = new JPanel();    dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));    dlgBtnsPanel.setLayout(new GridLayout(1, 3, 15, 1));    if (NQueryConstants.ISMAC) {      dlgBtnsPanel.add(mAboutNdEditBtn);      dlgBtnsPanel.add(mSysPropsBtn);      dlgBtnsPanel.add(mOKBtn);    }    else {      dlgBtnsPanel.add(mOKBtn);      dlgBtnsPanel.add(mAboutNdEditBtn);      dlgBtnsPanel.add(mSysPropsBtn);    }    dlgBtnsInset.add(dlgBtnsPanel);    this.add("South", new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5));    mOKBtn.addActionListener(this);    mAboutNdEditBtn.addActionListener(this);    mSysPropsBtn.addActionListener(this);    setTitle("About Java OceanAtlas");    WindowListener windowListener = new WindowAdapter() {      public void windowClosing(java.awt.event.WindowEvent e) {        e.getWindow().dispose();      }    };    this.addWindowListener(windowListener);  }  public AboutNQueryDialog(JFrame parent, String title, boolean modal) {    this(parent, modal);    setTitle(title);  }  public void actionPerformed(ActionEvent event) {    Object object = event.getSource();    if (object == mOKBtn) {      dispose();    }    else if (object == mAboutNdEditBtn) {      ndEdit.AboutDialog ff = new ndEdit.AboutDialog(new JFrame(), true);      //ff.init();      ff.pack();      // show dialog at center of screen      Rectangle dBounds = ff.getBounds();      Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();      int x = sd.width / 2 - dBounds.width / 2;      int y = sd.height / 2 - dBounds.height / 2;      ff.setLocation(x, y);      ff.show();    }    else if (object == mSysPropsBtn) {      try {        JSystemPropertiesDialog propDisplay = new JSystemPropertiesDialog();        propDisplay.setModal(true);        // show dialog at center of screen        Rectangle dBounds = propDisplay.getBounds();        Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();        int x = sd.width / 2 - dBounds.width / 2;        int y = sd.height / 2 - dBounds.height / 2;        propDisplay.setLocation(x, y);        propDisplay.show();      }      catch (Exception e) {}    }  }}
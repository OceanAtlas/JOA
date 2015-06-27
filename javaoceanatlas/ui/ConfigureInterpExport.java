/*
 * $Id: ConfigureInterpExport.java,v 1.2 2005/06/17 18:08:53 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class ConfigureInterpExport extends JOAJDialog implements ActionListener, ItemListener {
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mCancelButton = null;
  protected ResourceBundle b = null;
  protected JOAJRadioButton tsv = null;
  protected JOAJRadioButton csv = null;
  protected JOAJRadioButton customDelim = null;
  protected JOAJRadioButton defaultMissing = null;
  protected JOAJRadioButton customMissing = null;
  protected JOAJTextField customMissingVal = null;
  protected JOAJTextField customDelimiter = null;
  protected DialogClient mClient;
  protected JOAJRadioButton mJOAFormat = null;
  protected JOAJRadioButton mSSFormat = null;

  public ConfigureInterpExport(JFrame fv, DialogClient client) {
    super(fv, "Configure Interpolation Value Export", false);
    mClient = client;
    this.init();
  }

  public void init() {
    b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    Container contents = this.getContentPane();
    contents.setLayout(new BorderLayout(5, 5));
    
    JPanel contentsHolder = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 2));
    
    JPanel line1 = new JPanel(new RowLayout(Orientation.LEFT, Orientation.CENTER, 0));
    JPanel line2 = new JPanel(new RowLayout(Orientation.LEFT, Orientation.CENTER, 0));
    JPanel line3 = new JPanel(new RowLayout(Orientation.LEFT, Orientation.CENTER, 0));
    
    contentsHolder.add(new JLabel("Format:"));
    
    JPanel middlePanel = new JPanel();
    middlePanel.setLayout(new GridLayout(2, 1, 5, 0));
    
    // format
    mJOAFormat = new JOAJRadioButton("JOA Spreadsheet Format (.jos)", true);
    mSSFormat = new JOAJRadioButton("Generic Spreadsheet Format (.txt)", true);
    ButtonGroup bg0 = new ButtonGroup();
    bg0.add(mJOAFormat);
    bg0.add(mSSFormat);
    line1.add(new Indent1());
    line1.add(mJOAFormat);
    contentsHolder.add(line1);
    
    line2.add(new Indent1());
    line2.add(mSSFormat);
    contentsHolder.add(line2);
    
    // add listeners
    mJOAFormat.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.DESELECTED) {
					tsv.setEnabled(true);
					csv.setEnabled(true);
					customDelim.setEnabled(true);
					customDelimiter.setEnabled(true);
					defaultMissing.setEnabled(true);
					customMissing.setEnabled(true);
					customMissingVal.setEnabled(true);
				}
				else {
					tsv.setEnabled(false);
					csv.setEnabled(false);
					customDelim.setEnabled(false);
					customDelimiter.setEnabled(false);
					defaultMissing.setEnabled(false);
					customMissing.setEnabled(false);
					customMissingVal.setEnabled(false);
				}
			}
    });
    	
    // delimiter
    JPanel delimcontrols = new JPanel();
    delimcontrols.setLayout(new GridLayout(3, 1, 5, 0));
    TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kDelimiter"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    delimcontrols.setBorder(tb);
    tsv = new JOAJRadioButton(b.getString("kTab"), true);
    csv = new JOAJRadioButton(b.getString("kComma"));
    customDelim = new JOAJRadioButton(b.getString("kUDDelimiter"));
    delimcontrols.add(tsv);
    delimcontrols.add(csv);
    JPanel custdelimp = new JPanel();
    custdelimp.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    custdelimp.add(customDelim);
    customDelimiter = new JOAJTextField(3);
    custdelimp.add(customDelimiter);
    delimcontrols.add(custdelimp);
    ButtonGroup bg = new ButtonGroup();
    bg.add(tsv);
    bg.add(csv);
    bg.add(customDelim);
    middlePanel.add(delimcontrols);

    // missing value
    JPanel msgcontrols = new JPanel();
    msgcontrols.setLayout(new GridLayout(2, 1, 5, 0));
    tb = BorderFactory.createTitledBorder(b.getString("kMissingValue"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    msgcontrols.setBorder(tb);
    defaultMissing = new JOAJRadioButton(b.getString("kDefaultMissing"), true);
    customMissing = new JOAJRadioButton(b.getString("kCustomMissingValue"));
    JPanel custmissing = new JPanel();
    custmissing.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    custmissing.add(customMissing);
    customMissingVal = new JOAJTextField(3);
    custmissing.add(customMissingVal);
    msgcontrols.add(defaultMissing);
    msgcontrols.add(custmissing);
    ButtonGroup bg2 = new ButtonGroup();
    bg2.add(defaultMissing);
    bg2.add(customMissing);
    middlePanel.add(msgcontrols);
    line3.add(new Indent2());
    line3.add(middlePanel);
    contentsHolder.add(line3);

    // lower panel
    mOKBtn = new JOAJButton(b.getString("kOK"));
    mOKBtn.setActionCommand("ok");
    this.getRootPane().setDefaultButton(mOKBtn);
    mCancelButton = new JOAJButton(b.getString("kCancel"));
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

    contents.add(BorderLayout.SOUTH, new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5));
    contents.add(BorderLayout.CENTER, new TenPixelBorder(contentsHolder, 5, 5, 5, 5));

		tsv.setEnabled(false);
		csv.setEnabled(false);
		customDelim.setEnabled(false);
		customDelimiter.setEnabled(false);
		defaultMissing.setEnabled(false);
		customMissing.setEnabled(false);
		customMissingVal.setEnabled(false);
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      mClient.dialogDismissed(this);
      this.dispose();
    }
  }

  public boolean isJOAFormat() {
    return mJOAFormat.isSelected();
  }

  public boolean isTSV() {
    return tsv.isSelected();
  }

  public boolean isCSV() {
    return csv.isSelected();
  }

  public String getCustomDelim() {
    return customDelimiter.getText();
  }

  public boolean isDefaultMissing() {
    return defaultMissing.isSelected();
  }

  public String getCustomMissingVal() {
    return customMissingVal.getText();
  }

  public void itemStateChanged(ItemEvent evt) {
  }
  
  private class Indent1 extends JLabel {

		private static final long serialVersionUID = 1683713875322692772L;

		public Indent1(String lbl) {
			super(lbl);
		}

		public Indent1() {
			this("     ");
		}
	}

	private class Indent2 extends JLabel {

		private static final long serialVersionUID = -1531566360373870938L;

		public Indent2(String lbl) {
			super(lbl);
		}

		public Indent2() {
			this("          ");
		}
	}
}

/*
 * $Id: ConfigureSSImport.java,v 1.2 2005/06/17 18:08:53 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.border.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class ConfigureSSImport extends JOAJDialog implements ActionListener {
  protected FileViewer mFileViewer;
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mCancelButton = null;
  protected JOAJTextField mCustomMissingFld;
  protected JOAJRadioButton mJOADefaultMissing;
  protected JOAJRadioButton mWOCEDefaultMissing;
  protected JOAJRadioButton mEPICDefaultMissing;
  protected JOAJRadioButton mSpaceMissing;
  protected JOAJRadioButton mCustomMissing;
  protected JOAJTextField mCustomDelimFld;
  protected JOAJRadioButton mTabDelim;
  protected JOAJRadioButton mCommaDelim;
  protected JOAJRadioButton mCustomDelim;
  protected File mFile;
  protected JTextArea mTextArea;

  public ConfigureSSImport(FileViewer par, File inFile) {
    super(par, "Spreadsheet Import Options", true);
    mFile = inFile;
    mFileViewer = par;
    this.init();
  }

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(0, 0));

    // preview pane
    String textPreview = "";
    JPanel previewPanel = new JPanel();
    previewPanel.setLayout(new BorderLayout(0, 0));

    try {
      LineNumberReader in = new LineNumberReader(new FileReader(mFile), 10000);
      // read the column header line
      String inLine = in.readLine();
      textPreview += inLine + "\n";
      inLine = in.readLine();
      textPreview += inLine + "\n";
      inLine = in.readLine();
      textPreview += inLine + "\n";
      inLine = in.readLine();
      textPreview += inLine + "\n";
      inLine = in.readLine();
      textPreview += inLine + "\n";
      inLine = in.readLine();
      textPreview += inLine + "\n";
      inLine = in.readLine();
      textPreview += inLine + "\n";
      inLine = in.readLine();
      textPreview += inLine + "\n";
      inLine = in.readLine();
      textPreview += inLine + "\n";
      inLine = in.readLine();
      textPreview += inLine;
    }
    catch (Exception ex) {
      textPreview = "Can't display preview!";
    }

    mTextArea = new JTextArea(textPreview, 6, 60);
    mTextArea.setEditable(false);
    mTextArea.setCaretPosition(0);
    JScrollPane textScroller = new JScrollPane(mTextArea);
    previewPanel.add(textScroller, "Center");
    TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kFilePreview"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    previewPanel.setBorder(tb);
    contents.add(new TenPixelBorder(previewPanel, 5, 5, 0, 0), "North");

    // missing value assignment
    JPanel missingValuePanel = new JPanel();
    missingValuePanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
    mJOADefaultMissing = new JOAJRadioButton(b.getString("kJOADefaultMissing"), true);
    mWOCEDefaultMissing = new JOAJRadioButton(b.getString("kWOCEDefaultMissing"));
    mEPICDefaultMissing = new JOAJRadioButton(b.getString("kEPICDefaultMissing"));
    mSpaceMissing = new JOAJRadioButton(b.getString("kSpaceMissing"));
    mCustomMissing = new JOAJRadioButton(b.getString("kCustomMissing"));
    ButtonGroup bg = new ButtonGroup();
    bg.add(mJOADefaultMissing);
    bg.add(mWOCEDefaultMissing);
    bg.add(mEPICDefaultMissing);
    bg.add(mSpaceMissing);
    bg.add(mCustomMissing);
    JPanel customMissingValPanel = new JPanel();
    customMissingValPanel.setLayout(new RowLayout(Orientation.LEFT, Orientation.TOP, 0));
    customMissingValPanel.add(mCustomMissing);
    mCustomMissingFld = new JOAJTextField(6);
    mCustomMissingFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    customMissingValPanel.add(mCustomMissing);
    customMissingValPanel.add(mCustomMissingFld);
    missingValuePanel.add(mJOADefaultMissing);
    missingValuePanel.add(mWOCEDefaultMissing);
    missingValuePanel.add(mEPICDefaultMissing);
    missingValuePanel.add(mSpaceMissing);
    missingValuePanel.add(customMissingValPanel);
    tb = BorderFactory.createTitledBorder(b.getString("kMissingValueSelection"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    missingValuePanel.setBorder(tb);

    // delimiter value assignment
    JPanel delimValuePanel = new JPanel();
    delimValuePanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
    mTabDelim = new JOAJRadioButton(b.getString("kTabDelim"), true);
    mCommaDelim = new JOAJRadioButton(b.getString("kCommaDelim"));
    mCustomDelim = new JOAJRadioButton(b.getString("kCustomMissing"));
    ButtonGroup bg2 = new ButtonGroup();
    bg2.add(mTabDelim);
    bg2.add(mCommaDelim);
    bg2.add(mCustomDelim);
    JPanel customDelimPanel = new JPanel();
    customDelimPanel.setLayout(new RowLayout(Orientation.LEFT, Orientation.TOP, 0));
    customDelimPanel.add(mCustomDelim);
    mCustomDelimFld = new JOAJTextField(6);
    mCustomDelimFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    customDelimPanel.add(mCustomDelimFld);
    delimValuePanel.add(mTabDelim);
    delimValuePanel.add(mCommaDelim);
    delimValuePanel.add(customDelimPanel);
    tb = BorderFactory.createTitledBorder(b.getString("kDelimiterSelection"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    delimValuePanel.setBorder(tb);

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
    JPanel optPanel = new JPanel();
    optPanel.setLayout(new GridLayout(1, 2, 5, 5));
    optPanel.add(missingValuePanel);
    optPanel.add(delimValuePanel);
    contents.add(new TenPixelBorder(optPanel, 5, 5, 0, 0), "Center");
    contents.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
    this.pack();

    // show dialog at center of screen
    Rectangle dBounds = this.getBounds();
    Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
    int x = sd.width / 2 - dBounds.width / 2;
    int y = sd.height / 2 - dBounds.height / 2;
    this.setLocation(x, y);
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      this.dispose();
      JOAConstants.CANCELIMPORT = true;
    }
    else if (cmd.equals("ok")) {
      if (mTabDelim.isSelected()) {
        JOAConstants.IMPORTDELIMITER = '\t';
      }
      else if (mCommaDelim.isSelected()) {
        JOAConstants.IMPORTDELIMITER = ',';
      }
      else if (mCustomDelim.isSelected()) {
        String valStr = mCustomDelimFld.getText();
        if (valStr.length() == 0) {
          JFrame frm = new JFrame("Custom Delimiter Error");
          Toolkit.getDefaultToolkit().beep();
          JOptionPane.showMessageDialog(frm, "Custom delimiter has not been defined.");
          return;
        }
        JOAConstants.IMPORTDELIMITER = valStr.charAt(0);
      }
      // set the missing value value
      JOAConstants.USECUSTOMMISSINGVALUE = false;
      if (mWOCEDefaultMissing.isSelected()) {
        JOAConstants.CUSTOMMISSINGVALUE = -999.0;
        JOAConstants.USECUSTOMMISSINGVALUE = true;
      }
      else if (mEPICDefaultMissing.isSelected()) {
        JOAConstants.CUSTOMMISSINGVALUE = 1e35;
        JOAConstants.USECUSTOMMISSINGVALUE = true;
      }
      else if (mSpaceMissing.isSelected()) {
        JOAConstants.CUSTOMMISSINGVALUE = -999999999;
        JOAConstants.USECUSTOMMISSINGVALUE = true;
      }
      if (mCustomMissing.isSelected()) {
        String valStr = mCustomMissingFld.getText();
        if (valStr.length() == 0) {
          JFrame frm = new JFrame("Custom Missing Value Error");
          Toolkit.getDefaultToolkit().beep();
          JOptionPane.showMessageDialog(frm, "Custom missing value has not been defined.");
          return;
        }
        else {
          try {
            JOAConstants.CUSTOMMISSINGVALUE = Double.valueOf(valStr).doubleValue();
          }
          catch (NumberFormatException ex) {
            JFrame frm = new JFrame("Custom Missing Value Error");
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(frm, "Illegal custom missing value (must be numeric).");
            return;
          }

        }
        JOAConstants.USECUSTOMMISSINGVALUE = true;
      }
      this.dispose();
    }
  }

}

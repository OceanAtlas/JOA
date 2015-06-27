/*
 * $Id: ConfigureSSExport.java,v 1.4 2005/09/07 18:49:29 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import com.apple.mrj.*;

@SuppressWarnings("serial")
public class ConfigureTextExport extends JOAJDialog implements ActionListener, ItemListener {
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mCancelButton = null;
  protected boolean mIgnore = false;
  protected int mTotalStns = 0;
  protected SmallIconButton checkAll = null;
  protected SmallIconButton checkNone = null;
  protected JOAJTextField mCustomMissingFld;
  protected JOAJRadioButton mJOADefaultMissing;
  protected JOAJRadioButton mWOCEDefaultMissing;
  protected JOAJRadioButton mEPICDefaultMissing;
  protected JOAJRadioButton mSpaceMissing;
  protected JOAJRadioButton mCustomMissing;
  protected JOAJTextField mCustomDelimFld;
  protected JOAJRadioButton mTabDelim;
  protected JOAJRadioButton mCommaDelim;
  protected JOAJRadioButton mSpaceDelim;
  protected JOAJRadioButton mCustomDelim;
  private String mInText, mOutFilename, mInDelim, mSplitLineChar;

  public ConfigureTextExport(JFrame par, String inText, String delimChar, String splitLineChar, String outFileName) {
    super(par, "Export Text File", false);
    mInText = inText;
    mOutFilename = outFileName;
    mInDelim = delimChar;
    mSplitLineChar = splitLineChar;
    this.init();
  }

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(0, 0));

    JPanel upperContentsPanel = new JPanel();
    upperContentsPanel.setLayout(new BorderLayout(5, 0));

    // delimiter value assignment
    JPanel delimValuePanel = new JPanel();
    delimValuePanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
    mTabDelim = new JOAJRadioButton(b.getString("kTabDelim"), true);
    mCommaDelim = new JOAJRadioButton(b.getString("kCommaDelim"));
    mSpaceDelim = new JOAJRadioButton(b.getString("kSpaceDelim"));
    mCustomDelim = new JOAJRadioButton(b.getString("kCustomMissing"));
    ButtonGroup bg2 = new ButtonGroup();
    bg2.add(mTabDelim);
    bg2.add(mCommaDelim);
    bg2.add(mSpaceDelim);
    bg2.add(mCustomDelim);
    JPanel customDelimPanel = new JPanel();
    customDelimPanel.setLayout(new RowLayout(Orientation.LEFT, Orientation.TOP, 0));
    customDelimPanel.add(mCustomDelim);
    mCustomDelimFld = new JOAJTextField(6);
    mCustomDelimFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    customDelimPanel.add(mCustomDelimFld);
    delimValuePanel.add(mTabDelim);
    delimValuePanel.add(mCommaDelim);
    delimValuePanel.add(mSpaceDelim);
    delimValuePanel.add(customDelimPanel);

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
    optPanel.add(delimValuePanel);
    upperContentsPanel.add(new TenPixelBorder(optPanel, 5, 5, 0, 0), "South");
    contents.add(new TenPixelBorder(upperContentsPanel, 5, 5, 5, 5), "Center");
    contents.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
    this.pack();

    // show dialog at center of screen
    Rectangle dBounds = this.getBounds();
    Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
    int x = sd.width / 2 - dBounds.width / 2;
    int y = sd.height / 2 - dBounds.height / 2;
    this.setLocation(x, y);
  }

  public void itemStateChanged(ItemEvent evt) {
    if (evt.getSource() instanceof JOAJRadioButton) {
      //if (evt.getStateChange() == ItemEvent.SELECTED && rb == mShowOnly) {
    }
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      // get the delimiter value
      String delimText = "\t";
      if (mTabDelim.isSelected()) {
        delimText = "\t";
      }
      else if (mCommaDelim.isSelected()) {
        delimText = ",";
      }
      else if (mSpaceDelim.isSelected()) {
        delimText = " ";
      }
      else if (mCustomDelim.isSelected()) {
        delimText = mCustomDelimFld.getText();

        if (delimText == null || delimText.length() == 0) {
          JFrame frm = new JFrame("Custom Delimiter Error");
          Toolkit.getDefaultToolkit().beep();
          JOptionPane.showMessageDialog(frm, "Custom delimiter has not been defined.");
          return;
        }
      }

      // export a text
      Frame fr = new Frame();
      String directory = System.getProperty("user.dir");
      FileDialog f = new FileDialog(fr, "Export data as:", FileDialog.SAVE);
      f.setDirectory(directory);
      if (mOutFilename != null) {
        f.setFile(mOutFilename + ".txt");
      }
      else {
        f.setFile("untitled.txt");
      }

      Rectangle dBounds = f.getBounds();
      Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
      int x = sd.width / 2 - dBounds.width / 2;
      int y = sd.height / 2 - dBounds.height / 2;
      f.setLocation(x, y);
      f.setVisible(true);

      directory = f.getDirectory();
      f.dispose();
      if (directory != null && f.getFile() != null) {
        File outFile = new File(directory, f.getFile());
        this.exportIt(delimText, outFile);

        try {
          JOAConstants.LogFileStream.writeBytes("Exported text of source merge to: " + outFile.getCanonicalPath() +
                                                "\n");
          JOAConstants.LogFileStream.flush();
        }
        catch (Exception ex) {}
      }

      this.dispose();
    }
  }

  @SuppressWarnings("deprecation")
  public void exportIt(String delimStr, File file) {
    LineNumberReader in = new LineNumberReader(new StringReader(mInText), 10000);

    try {
      FileOutputStream fos = new FileOutputStream(file);
      BufferedOutputStream bos = new BufferedOutputStream(fos, 1000000);
      DataOutputStream out = new DataOutputStream(bos);
      String inLine;
      String tokenSplitter = "[" + mInDelim.trim() + "]";

      String headerSplitter = null;
      if (mSplitLineChar != null) {
        headerSplitter = "[" + mSplitLineChar.trim() + "]";
      }

      while (true) {
        inLine = in.readLine();
        if (inLine == null) {
          break;
        }

        // break by :
        String outLine = inLine;
        if (headerSplitter != null) {
          String[] tokens = inLine.split(headerSplitter);
          outLine = tokens[1];
        }

        // make the colheaders
        String[] tokens2 = outLine.split(tokenSplitter);

        for (String ss : tokens2) {
          out.writeBytes(ss);
          out.writeBytes(delimStr);
        }
        out.writeBytes("\r");
      }

      out.flush();
      out.close();

      // type the file if on the Mac
      if (JOAConstants.ISMAC) {
        MRJFileUtils.setFileTypeAndCreator(file, new MRJOSType("TEXT"), new MRJOSType("JOAA"));
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.out.println("An Error occurred exporting the current source text");
    }
    finally {
    }
  }
}

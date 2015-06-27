/*
 * $Id: ConfigSimpleCalculation.java,v 1.6 2004/09/14 19:11:26 oz Exp $
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

import javax.swing.JDialog;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.util.ResourceBundle;
import javaoceanatlas.utility.DialogClient;
import gov.noaa.pmel.nquery.resources.NQueryConstants;
import gov.noaa.pmel.nquery.utility.NQueryCalculation;
import javax.swing.JFrame;
import java.awt.Container;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.Cursor;
import javax.swing.JLabel;
import java.awt.GridLayout;
import javaoceanatlas.utility.TenPixelBorder;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javaoceanatlas.ui.widgets.JOAJDialog;

/**
 * <code>ConfigMLDCalc</code> UI for configuring a "simple" calculation--a calculation with a single .
 *
 * @author oz
 * @version 1.0
 */

public class ConfigNQSimpleCalculation extends JOAJDialog implements ActionListener, ConfigNQCalcDialog {
  protected JButton mOKBtn = null;
  protected JButton mCancelButton = null;
  protected JTextField mCalcArgValue = null;
  protected ResourceBundle b = ResourceBundle.getBundle("gov.noaa.pmel.nquery.resources.NQueryResources");
  DialogClient mClient;
  NQueryCalculation mCalc;
  String mPrompt;
  double mCurrVal;
  protected JTextField mNewParamName = null;
  String mInitialVarName;

  public ConfigNQSimpleCalculation(JFrame par, NQueryCalculation calc, String prompt, double d, DialogClient client) {
    super(par, "", false);
    this.setTitle(b.getString("kEditParameterCalculation"));
    mCalc = calc;
    mClient = client;
    mPrompt = new String(prompt);
    mCurrVal = d;
    init();
  }

  public void init() {
    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(5, 5));
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout(5, 5));
    JPanel line1 = new JPanel();
    line1.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    mCalcArgValue = new JTextField(4);
    mCalcArgValue.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line1.add(new JLabel(mPrompt));
    mCalcArgValue.setText(String.valueOf(mCurrVal));
    line1.add(mCalcArgValue);
    mainPanel.add("North", line1);

    JPanel lastline = new JPanel();
    lastline.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
    lastline.add(new JLabel(b.getString("kVariableName")));
    mNewParamName = new JTextField(20);
    lastline.add(mNewParamName);
    mainPanel.add("Center", lastline);

    // set the custom calc name
    mInitialVarName = mCalc.getCalcType();
    mNewParamName.setText(mInitialVarName);

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

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      double out;
      try {
        out = Double.valueOf(mCalcArgValue.getText()).doubleValue();
        mCalc.setArg(new Double(out));
        if (mNewParamName.getText() != null && mNewParamName.getText().length() > 0) {
          mCalc.setCalcType(mNewParamName.getText());
        }
        else {
          mCalc.setCalcType(mInitialVarName);
        }
        this.dispose();
      }
      catch (NumberFormatException ex) {
        JFrame f = new JFrame("Value Error");
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(f, "The value entered is not a valid number.");
        mCalcArgValue.setText(String.valueOf(mCurrVal));
      }
    }
  }

  public Object getCalculation() {
    return mCalc;
  }

  public Object getSpecification() {
    return null;
  }
}

/*
 * $Id: CriterionPanel.java,v 1.9 2004/09/14 19:11:26 oz Exp $
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

import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JPanel;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import java.awt.event.ActionEvent;
import javaoceanatlas.utility.ButtonMaintainer;
import javax.swing.event.DocumentListener;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javaoceanatlas.ui.widgets.SmallIconButton;
import javax.swing.ImageIcon;
import javax.swing.event.DocumentEvent;
import java.awt.event.ItemEvent;
import java.awt.Color;
import javaoceanatlas.utility.JOAFormulas;

/**
 * <code>CriterionPanel</code> UI for an individual search criterion.
 *
 * @author oz
 * @version 1.0
 */

public class NQCriterionPanel extends JPanel implements ButtonMaintainer, ActionListener, DocumentListener, ItemListener {
  private double mMinVal;
  private double mMaxVal;
  private JTextField mMaxTF;
  private JTextField mMinTF;
  private JCheckBox mNotCB;
  private JComboBox mParamCombo;
  private Vector mParamList;
  private boolean mIsFirst = false;
  private NQCriteriaContainer mParent;
  private SmallIconButton mRemoveConditionButton;
  private SmallIconButton mAddConditionButton;
	private Timer timer = new Timer();
  private boolean mChangedFlag = false;
  private boolean mIgnore = false;
  private boolean mEnabled = true;

  public NQCriterionPanel(Vector params, boolean isfirst, NQCriteriaContainer parent) {
    mIsFirst = isfirst;
    mParamList = params;
    mParent = parent;
    init();
  }

  public void init() {
    try {
      mRemoveConditionButton = new SmallIconButton(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").
          getResource(
              "images/bigminus.gif")));
      mAddConditionButton = new SmallIconButton(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").
          getResource("images/bigplus.gif")));
    }
    catch (Exception ex) {
    	ex.printStackTrace();
    	System.out.println("NQCriterionPanel:init");
    }

    this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

    //mNotCB = new JCheckBox("not", false);
    //this.add(mNotCB);

    mMinTF = new JTextField(8);
    mMinTF.getDocument().addDocumentListener(this);
    this.add(mMinTF);
    JLabel l1 = new JLabel("<=");
    this.add(l1);
    if (mParamList == null) {
      mParamList = new Vector();
      mParamList.addElement("not defined");
    }
    mParamCombo = new JComboBox(mParamList);
    mParamCombo.addItemListener(this);
    this.add(mParamCombo);
    JLabel l2 = new JLabel("<=");
    this.add(l2);
    mMaxTF = new JTextField(8);
    mMaxTF.getDocument().addDocumentListener(this);
    this.add(mMaxTF);

    // the add/remove button panel
    JPanel btnPanel = new JPanel();
    btnPanel.setLayout(new GridLayout(1, 2, 5, 5));
    btnPanel.add(mRemoveConditionButton);
    btnPanel.add(mAddConditionButton);
    this.add(btnPanel);
    mRemoveConditionButton.addActionListener(this);
    mAddConditionButton.addActionListener(this);
    mRemoveConditionButton.setActionCommand("removeme");
    mAddConditionButton.setActionCommand("addnew");

    // start a button maintainer
		runTimer();
  }

	public void runTimer() {
		TimerTask task = new TimerTask() {
			public void run() {
				maintainButtons();
			}
		};
		timer.schedule(task, 0, 1000);
	}

  public void setEnabled(boolean b) {
    mEnabled = b;
    mMaxTF.setEnabled(b);
    mMinTF.setEnabled(b);
    mParamCombo.setEnabled(b);
    if (!mIsFirst) {
      mRemoveConditionButton.setEnabled(b);
    }
    mAddConditionButton.setEnabled(b);
  }

  public boolean isEnabled() {
    return mEnabled;
  }

  public void setParams(Vector params) {
    mIgnore = true;
    mParamList = params;
    mParamCombo.removeAllItems();
    for (int i = 0; i < mParamList.size(); i++) {
      mChangedFlag = false;
      mParamCombo.addItem(mParamList.elementAt(i));
    }
    mParamCombo.invalidate();
    mParamCombo.validate();
    mIgnore = false;
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("removeme")) {
      mRemoveConditionButton.setSelected(false);

      // tell the CriteriaContainer to remove this panel
      mParent.removeCriterion(this);
      mChangedFlag = true;
    }
    else if (cmd.equals("addnew")) {
      mAddConditionButton.setSelected(false);

      // tell the CriteriaContainer to add a new Criterion Panel
      mParent.addCriterion(this);
    }
  }

  public void changedUpdate(DocumentEvent evt) {
    mChangedFlag = true;
  }

  public void insertUpdate(DocumentEvent evt) {
    mChangedFlag = true;
  }

  public void removeUpdate(DocumentEvent evt) {
    mChangedFlag = true;
  }

  public boolean isChanged() {
    return mChangedFlag;
  }

  public void itemStateChanged(ItemEvent evt) {
    if (evt.getSource() instanceof JComboBox) {
      JComboBox cb = (JComboBox)evt.getSource();
      if (cb == mParamCombo && evt.getStateChange() == ItemEvent.SELECTED && !mIgnore) {
        mChangedFlag = true;
      }
    }
  }
  
  public void closeMe() {
  	timer.cancel();
  }

  public String getClause(boolean shortenParam) {
    // get the column name
    String colName = (String)mParamCombo.getSelectedItem();

    if (shortenParam) {
      colName = JOAFormulas.returnMiddleTruncatedString(colName, 10);
    }

    // get the min argument
    double tstVal;
    String minStr = "";
    String tstStr = "";
    try {
      tstStr = mMinTF.getText();
      tstVal = Double.valueOf(tstStr).doubleValue();
      if (tstStr.length() > 0) {
        minStr = mMinTF.getText();
        mMinTF.setForeground(Color.black);
      }
    }
    catch (NumberFormatException ex) {
      // illegal value
      if (tstStr.length() > 0) {
        mMinTF.setForeground(Color.red);
      }
    }

    String maxStr = "";
    try {
      tstStr = mMaxTF.getText();
      tstVal = Double.valueOf(tstStr).doubleValue();
      if (tstStr.length() > 0) {
        maxStr = mMaxTF.getText();
        mMaxTF.setForeground(Color.black);
      }
    }
    catch (NumberFormatException ex) {
      // illegal value
      if (tstStr.length() > 0) {
        mMaxTF.setForeground(Color.red);
      }
    }

    if (minStr.length() == 0 && maxStr.length() == 0) {
      colName = "";
    }

    mChangedFlag = false;

    String lowerTest = new String("");
    if (minStr.length() > 0 && shortenParam) {
      lowerTest = colName + ">=" + minStr;
    }
    else if (minStr.length() > 0) {
      lowerTest = colName + ">=" + "'" + minStr + "'";
    }

    String upperTest = new String("");
    if (maxStr.length() > 0 && shortenParam) {
      upperTest = colName + "<=" + maxStr;
    }
    else if (maxStr.length() > 0) {
      upperTest = colName + "<=" + "'" + maxStr + "'";
    }

    if (lowerTest.length() > 0 && upperTest.length() > 0) {
      return "(" + lowerTest + " and " + upperTest + ")";
    }
    else {
      return lowerTest + upperTest;
    }
  }

  public double getMinVal() {
    return mMinVal;
  }

  public double getMaxVal() {
    return mMaxVal;
  }

  public String getParam() {
    return (String)mParamCombo.getSelectedItem();
  }

  public void setPanel(double minVal, double maxVal, String param) {

  }

  public void setPanel(double minVal, double maxVal, int iparam) {

  }

  public boolean isCriterionComplete() {
    // get the min argument
    double tstVal;
    boolean complete = false;
    String tstStr = "";
    try {
      tstStr = mMinTF.getText();
      tstVal = Double.valueOf(tstStr).doubleValue();
      if (tstStr.length() > 0) {
        complete = true;
      }
    }
    catch (NumberFormatException ex) {
      // illegal value
      if (tstStr.length() > 0) {
        complete = false;
      }
    }

    try {
      tstStr = mMaxTF.getText();
      tstVal = Double.valueOf(tstStr).doubleValue();
      if (tstStr.length() > 0) {
        complete = true;
      }
    }
    catch (NumberFormatException ex) {
      // illegal value
      if (tstStr.length() > 0) {
        complete = false;
      }
    }
    return complete;
  }

  public void maintainButtons() {
    if (!mIsFirst) {
      mRemoveConditionButton.setEnabled(true);
    }
    else {
      mRemoveConditionButton.setEnabled(false);
    }
  }

}

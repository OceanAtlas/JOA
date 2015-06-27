/*
 * $Id: ConfigInterpolationCalc.java,v 1.12 2005/10/18 23:43:05 oz Exp $
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
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionListener;
import javax.swing.event.DocumentListener;
import javaoceanatlas.utility.ButtonMaintainer;
import java.awt.event.ItemListener;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import gov.noaa.pmel.nquery.utility.NQueryCalculation;
import javaoceanatlas.utility.DialogClient;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import gov.noaa.pmel.eps2.ExportVariable;
import java.awt.Container;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import javaoceanatlas.utility.TenPixelBorder;
import javaoceanatlas.utility.ColumnLayout;
import javaoceanatlas.utility.Orientation;
import java.awt.GridLayout;
import javax.swing.event.DocumentEvent;
import java.util.Iterator;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ActionEvent;
import java.awt.Cursor;
import javax.swing.ButtonGroup;
import gov.noaa.pmel.nquery.resources.NQueryConstants;
import gov.noaa.pmel.nquery.specifications.NQInterpolationSpecification;
import gov.noaa.pmel.nquery.utility.NQParameterChooser;
import javaoceanatlas.ui.widgets.JOAJDialog;

/**
 * <code>ConfigInterpolationCalc</code> UI for configuring an Interpolation Calculation.
 *
 * @author oz
 * @version 1.0
 */

public class ConfigNQInterpolationCalc extends JOAJDialog implements ListSelectionListener, ActionListener,
    ButtonMaintainer, ItemListener, ConfigNQCalcDialog, DocumentListener {
  protected JCheckBox mInterpolationCalc = null;
  protected NQParameterChooser mInterpParamList;
  protected NQParameterChooser mInterpWRTParamList;
  protected JRadioButton mInterpAtValue = null;
  protected JRadioButton mInterpAtSurface = null;
  protected JRadioButton mInterpAtBottom = null;
  protected JTextField mInterpAtValueField = null;
  protected JTextField mDepthLimitValue = null;
  protected int mSelInterpParam = -1;
  protected int mSelInterpWRTParam = -1;
  protected JLabel mDepthLimit = null;
  protected JCheckBox mInterpUseDeepest = null;
  protected JRadioButton mInterpTopDown = null;
  protected JRadioButton mInterpBottomUp = null;
  protected JLabel interpDirecLbl = null;
  protected boolean oldInterpState = false;
  protected JLabel mdb4Lbl = null;
  protected JButton mOKBtn = null;
  protected JButton mCancelButton = null;
  protected JTextField mNewParamName = null;
  protected JTextField mNewParamUnits = null;
	private Timer timer = new Timer();
  ArrayList mVarList;
  NQueryCalculation mCalc;
  DialogClient mClient;
  NQInterpolationSpecification mSpec;
  protected ResourceBundle b = ResourceBundle.getBundle("gov.noaa.pmel.nquery.resources.NQueryResources");

  public ConfigNQInterpolationCalc(JFrame par, ArrayList varList, DialogClient client) {
    super(par, "", false);
    this.setTitle(b.getString("kInterpolationCalculations"));
    mVarList = varList;
    mClient = client;
    this.init();
  }

  public void setSpecification(NQueryCalculation calc, NQInterpolationSpecification inspec) {
    mCalc = calc;
    mSpec = inspec;
    // modify the UI to reflect
    ExportVariable intVar = mSpec.getIntVar();
    mInterpParamList.setSelectedLine(intVar);
    ExportVariable wrtVar = mSpec.getWRTVar();
    mInterpWRTParamList.setSelectedLine(wrtVar);
    double val = mSpec.getAtVal();
    mInterpAtValueField.setText(String.valueOf(val));

    if (mSpec.isUseDeepest()) {
      mInterpUseDeepest.setSelected(true);
    }

    if (mSpec.isAtSurface()) {
      mInterpAtSurface.setSelected(true);
    }

    if (mSpec.isAtBottom()) {
      mInterpAtSurface.setSelected(true);
    }

    if (mSpec.getDepthLimit() >= 0) {
      mDepthLimitValue.setText(String.valueOf(mSpec.getDepthLimit()));
    }

    if (mSpec.getSearchMethod() == NQueryConstants.SEARCH_TOP_DOWN) {
      mInterpTopDown.setSelected(true);
      mInterpBottomUp.setSelected(false);
    }
    else if (mSpec.getSearchMethod() == NQueryConstants.SEARCH_BOTTOM_UP) {
      mInterpTopDown.setSelected(false);
      mInterpBottomUp.setSelected(true);
    }

    // set the custom calc name
    mNewParamName.setText(mCalc.getCalcType());
  }

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("gov.noaa.pmel.nquery.resources.NQueryResources");

    // create the two parameter chooser lists
    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(5, 5));

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout(5, 5));

    // Interpolation
    JPanel interpolationPanel = new JPanel();
    interpolationPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0)); //(Orientation.LEFT, Orientation.BOTTOM, 5));
    mInterpParamList = new NQParameterChooser(mVarList, new String(b.getString("kParameter2")), this,
                                            "SALT                ");
    mInterpWRTParamList = new NQParameterChooser(mVarList, new String(b.getString("kOntoSurface")), this,
                                               "SALT                ");
    mInterpParamList.init();
    mInterpWRTParamList.init();
    interpolationPanel.add(new TenPixelBorder(mInterpParamList, 0, 0, 0, 0));
    interpolationPanel.add(new TenPixelBorder(mInterpWRTParamList, 0, 0, 0, 0));

    // range container
    JPanel rangeCont2 = new JPanel();
    rangeCont2.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

    // at value container
    JPanel linei1 = new JPanel();
    linei1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    mInterpAtValue = new JRadioButton(b.getString("kAtValue"), true);
    mInterpAtValue.addItemListener(this);
    mInterpAtValueField = new JTextField(6);
    mInterpAtValueField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mInterpAtValueField.getDocument().addDocumentListener(this);
    linei1.add(mInterpAtValue);
    linei1.add(mInterpAtValueField);
    rangeCont2.add(linei1);

    JPanel linei4 = new JPanel();
    linei4.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    mInterpTopDown = new JRadioButton(b.getString("kTopDown"), true);
    mInterpTopDown.addItemListener(this);
    mInterpBottomUp = new JRadioButton(b.getString("kBottomUp"));
    mInterpBottomUp.addItemListener(this);
    interpDirecLbl = new JLabel(b.getString("kSearchDirection"));
    linei4.add(interpDirecLbl);
    linei4.add(mInterpTopDown);
    linei4.add(mInterpBottomUp);
    ButtonGroup b4 = new ButtonGroup();
    b4.add(mInterpTopDown);
    b4.add(mInterpBottomUp);
    rangeCont2.add(linei4);

    // at surface container
    JPanel linei2 = new JPanel();
    linei2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    mInterpAtSurface = new JRadioButton(b.getString("kAtSurface"), false);
    mInterpAtSurface.addItemListener(this);
    mDepthLimit = new JLabel(b.getString("kDepthLimit"));
    mDepthLimitValue = new JTextField(6);
    mDepthLimitValue.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    linei2.add(mInterpAtSurface);
    linei2.add(mDepthLimit);
    linei2.add(mDepthLimitValue);
    mdb4Lbl = new JLabel(b.getString("kDB"));
    linei2.add(mdb4Lbl);
    rangeCont2.add(linei2);

    // at bottom container
    JPanel linei3 = new JPanel();
    linei3.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    mInterpAtBottom = new JRadioButton(b.getString("kAtBottom"), false);
    mInterpAtBottom.addItemListener(this);
    mInterpUseDeepest = new JCheckBox(b.getString("kUseDeepestIfBottomIsMissing"));
    linei3.add(mInterpAtBottom);
    linei3.add(mInterpUseDeepest);
    rangeCont2.add(linei3);

    ButtonGroup b3 = new ButtonGroup();
    b3.add(mInterpAtValue);
    b3.add(mInterpAtSurface);
    b3.add(mInterpAtBottom);
    interpolationPanel.add(rangeCont2);

    // construct the dialog
    JPanel mainPanelContents = new JPanel();
    mainPanelContents.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
    mainPanelContents.add(new TenPixelBorder(interpolationPanel, 5, 5, 5, 5));
    mainPanel.add("Center", mainPanelContents);

    JPanel lastline = new JPanel();
    lastline.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
    lastline.add(new JLabel(b.getString("kVariableName")));
    mNewParamName = new JTextField(40);
    lastline.add(mNewParamName);
    mainPanel.add("South", lastline);

    this.getContentPane().add("Center", new TenPixelBorder(mainPanel, 10, 10, 10, 10));

    // lower panel
    mOKBtn = new JButton(b.getString("kOK"));
    mOKBtn.setActionCommand("ok");
    this.getRootPane().setDefaultButton(mOKBtn);
    mCancelButton = new JButton(b.getString("kCancel"));
    mCancelButton.setActionCommand("cancel");
    JPanel dlgBtnsInset = new JPanel();
    JPanel dlgBtnsPanel = new JPanel();
    dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
    dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
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

    this.getContentPane().add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
    this.pack();

		runTimer();

    // show dialog at center of screen
    Rectangle dBounds = this.getBounds();
    Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
    int x = sd.width / 2 - dBounds.width / 2;
    int y = sd.height / 2 - dBounds.height / 2;
    this.setLocation(x, y);
  }

	public void runTimer() {
		TimerTask task = new TimerTask() {
			public void run() {
				maintainButtons();
			}
		};
		timer.schedule(task, 0, 1000);
	}

  public void valueChanged(ListSelectionEvent evt) {
    if (evt.getSource() == mInterpParamList.getJList()) {
      // get the interpolation param
      mSelInterpParam = mInterpParamList.getJList().getSelectedIndex();
    }
    else if (evt.getSource() == mInterpWRTParamList.getJList()) {
      mSelInterpWRTParam = mInterpWRTParamList.getJList().getSelectedIndex();
    }
    generateVariableName();
  }

  public void itemStateChanged(ItemEvent evt) {
    generateVariableName();
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      timer.cancel();
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      //configure an integration calculation
      double val = -99;
      try {
        val = Double.valueOf(mInterpAtValueField.getText()).doubleValue();
      }
      catch (NumberFormatException ex) {
        val = 0;
      }

      double depthLimit = -99;
      try {
        depthLimit = Double.valueOf(mDepthLimitValue.getText()).doubleValue();
      }
      catch (NumberFormatException ex) {
        depthLimit = -99;
      }

      String intParamStr = (String)(mInterpParamList.getJList().getSelectedValue());
      String wrtParamStr = (String)(mInterpWRTParamList.getJList().getSelectedValue());

      String atString;
      int searchMethod = NQueryConstants.SEARCH_TOP_DOWN;
      String searchDirecStr = "";

      if (mInterpAtSurface.isSelected()) {
        atString = "Surface";
      }
      else if (mInterpAtBottom.isSelected()) {
        atString = "Bottom";
      }
      else {
        atString = String.valueOf(val);
        if (mInterpTopDown.isSelected()) {
          searchMethod = NQueryConstants.SEARCH_TOP_DOWN;
          searchDirecStr = "-td";
        }
        else if (mInterpBottomUp.isSelected()) {
          searchMethod = NQueryConstants.SEARCH_BOTTOM_UP;
          searchDirecStr = "-bu";
        }
      }

      // get the wrt ExportVariable represented by this selection
      Iterator itor = mVarList.iterator();
      ExportVariable wrtVar = null;
      while (itor.hasNext()) {
        wrtVar = (ExportVariable)itor.next();
        if (wrtVar.getPresentationVarName().equalsIgnoreCase(wrtParamStr)) {
          break;
        }
      }

      // get the wrt ExportVariable represented by this selection
      itor = mVarList.iterator();
      ExportVariable interpVar = null;
      while (itor.hasNext()) {
        interpVar = (ExportVariable)itor.next();
        if (interpVar.getPresentationVarName().equalsIgnoreCase(intParamStr)) {
          break;
        }
      }

      if (mSpec == null) {
        mCalc = new NQueryCalculation(mNewParamName.getText(), "INTERP",
                                new NQInterpolationSpecification(interpVar, wrtVar, val, mInterpAtSurface.isSelected(),
            depthLimit, mInterpAtBottom.isSelected(), mInterpUseDeepest.isSelected(), searchMethod, false),
                                NQueryConstants.STN_CALC_TYPE, true, true);
        mCalc.setUnits(interpVar.getVarUnits());
      }
      else {
        // existing calculation
        // reset title
        mCalc.setCalcType(mNewParamName.getText());
        mSpec = new NQInterpolationSpecification(interpVar, wrtVar, val, mInterpAtSurface.isSelected(), depthLimit,
                                               mInterpAtBottom.isSelected(), mInterpUseDeepest.isSelected(),
                                               searchMethod, false);
        mCalc.setUnits(interpVar.getVarUnits());
        mCalc.setArg(mSpec);
      }

      mClient.dialogDismissed(this);
      timer.cancel();
      this.dispose();
    }
  }

  public void maintainButtons() {
    // maintain the sub controls
    // if pressure is selected then maintain the at surface and bottom controls
    if (mSelInterpWRTParam != 0) {
      mInterpAtValue.setSelected(true);
      mInterpAtSurface.setEnabled(false);
      mInterpAtBottom.setEnabled(false);
    }
    else {
      mInterpAtSurface.setEnabled(true);
      mInterpAtBottom.setEnabled(true);
    }

    if (mInterpAtValue.isSelected()) {
      if (!mInterpAtValueField.isEnabled()) {
        mInterpAtValueField.setEnabled(true);
      }
      if (!mInterpTopDown.isEnabled()) {
        mInterpTopDown.setEnabled(true);
      }
      if (!mInterpBottomUp.isEnabled()) {
        mInterpBottomUp.setEnabled(true);
      }
      if (!interpDirecLbl.isEnabled()) {
        interpDirecLbl.setEnabled(true);
      }
      if (mDepthLimitValue.isEnabled()) {
        mDepthLimitValue.setEnabled(false);
      }
      if (mDepthLimit.isEnabled()) {
        mDepthLimit.setEnabled(false);
      }
      if (mInterpUseDeepest.isEnabled()) {
        mInterpUseDeepest.setEnabled(false);
      }
      if (mdb4Lbl.isEnabled()) {
        mdb4Lbl.setEnabled(false);
      }
    }
    else if (mInterpAtSurface.isSelected()) {
      if (mInterpTopDown.isEnabled()) {
        mInterpTopDown.setEnabled(false);
      }
      if (mInterpBottomUp.isEnabled()) {
        mInterpBottomUp.setEnabled(false);
      }
      if (interpDirecLbl.isEnabled()) {
        interpDirecLbl.setEnabled(false);
      }
      if (mInterpAtValueField.isEnabled()) {
        mInterpAtValueField.setEnabled(false);
      }
      if (!mDepthLimitValue.isEnabled()) {
        mDepthLimitValue.setEnabled(true);
      }
      if (!mDepthLimit.isEnabled()) {
        mDepthLimit.setEnabled(true);
      }
      if (mInterpUseDeepest.isEnabled()) {
        mInterpUseDeepest.setEnabled(false);
      }
      if (!mdb4Lbl.isEnabled()) {
        mdb4Lbl.setEnabled(true);
      }
    }
    else if (mInterpAtBottom.isSelected()) {
      if (mInterpTopDown.isEnabled()) {
        mInterpTopDown.setEnabled(false);
      }
      if (mInterpBottomUp.isEnabled()) {
        mInterpBottomUp.setEnabled(false);
      }
      if (interpDirecLbl.isEnabled()) {
        interpDirecLbl.setEnabled(false);
      }
      if (mInterpAtValueField.isEnabled()) {
        mInterpAtValueField.setEnabled(false);
      }
      if (mDepthLimitValue.isEnabled()) {
        mDepthLimitValue.setEnabled(false);
      }
      if (mDepthLimit.isEnabled()) {
        mDepthLimit.setEnabled(false);
      }
      if (!mInterpUseDeepest.isEnabled()) {
        mInterpUseDeepest.setEnabled(true);
      }
      if (mdb4Lbl.isEnabled()) {
        mdb4Lbl.setEnabled(false);
      }
    }

    boolean IntrpOK = false;
    if (mSelInterpParam >= 0 && mSelInterpWRTParam >= 0) {
      IntrpOK = true;
      if (mInterpAtValue.isSelected()) {
        // test for valid interpolation surface value
        double atval = -99;
        try {
          atval = Double.valueOf(mInterpAtValueField.getText()).doubleValue();
        }
        catch (NumberFormatException ex) {
          IntrpOK = false;
        }
      }
    }

    if (!IntrpOK) {
      if (mOKBtn.isEnabled()) {
        mOKBtn.setEnabled(false);
      }
    }
    else {
      if (!mOKBtn.isEnabled()) {
        mOKBtn.setEnabled(true);
      }
    }
  }

  public Object getCalculation() {
    return mCalc;
  }

  public Object getSpecification() {
    return mSpec;
  }

  public void generateVariableName() {
    double val = -99;
    try {
      val = Double.valueOf(mInterpAtValueField.getText()).doubleValue();
    }
    catch (NumberFormatException ex) {
      val = 0;
    }
    String atString;
    int searchMethod = NQueryConstants.SEARCH_TOP_DOWN;
    String searchDirecStr = "";

    if (mInterpAtSurface.isSelected()) {
      atString = "Surface";
    }
    else if (mInterpAtBottom.isSelected()) {
      atString = "Bottom";
    }
    else {
      atString = String.valueOf(val);
      if (mInterpTopDown.isSelected()) {
        searchMethod = NQueryConstants.SEARCH_TOP_DOWN;
        searchDirecStr = "_td";
      }
      else if (mInterpBottomUp.isSelected()) {
        searchMethod = NQueryConstants.SEARCH_BOTTOM_UP;
        searchDirecStr = "_bu";
      }
    }

    while (atString.indexOf(".") >= 0) {
      atString = atString.substring(0, atString.indexOf('.')) + "_" +
          atString.substring(atString.indexOf('.') + 1, atString.length());
    }

    String intParamStr = (String)(mInterpParamList.getJList().getSelectedValue());
    String wrtParamStr = (String)(mInterpWRTParamList.getJList().getSelectedValue());
    if (intParamStr == null) {
      intParamStr = "?";
    }
    if (wrtParamStr == null) {
      wrtParamStr = "?";
    }
    String nameString = intParamStr + "_on_" + wrtParamStr + "_at_" + atString + searchDirecStr;
    mNewParamName.setText(nameString);
  }

  public void changedUpdate(DocumentEvent evt) {
    generateVariableName();
  }

  public void insertUpdate(DocumentEvent evt) {
    generateVariableName();
  }

  public void removeUpdate(DocumentEvent evt) {
    generateVariableName();
  }
}

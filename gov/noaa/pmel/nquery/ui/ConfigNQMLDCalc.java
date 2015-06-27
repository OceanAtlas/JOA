/*
 * $Id: ConfigMLDCalc.java,v 1.12 2005/09/20 22:06:01 oz Exp $
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
import javax.swing.ButtonGroup;
import gov.noaa.pmel.nquery.resources.NQueryConstants;
import gov.noaa.pmel.nquery.specifications.NQMixedLayerCalcSpec;
import gov.noaa.pmel.nquery.utility.NQParameterChooser;
import javaoceanatlas.ui.widgets.JOAJDialog;

/**
 * <code>ConfigMLDCalc</code> UI for configuring a Mixed-Layer Depth Calculation.
 *
 * @author oz
 * @version 1.0
 */

public class ConfigNQMLDCalc extends JOAJDialog implements ListSelectionListener, ActionListener, DocumentListener,
    ButtonMaintainer, ItemListener, ConfigNQCalcDialog {
  // MLD Controls
  protected NQParameterChooser mMLParamList;
  protected int mSelMLParam = -1;
  protected JTextField mDepthOfDiffMLField = null;
  protected JTextField mDepthOfMLField = null;
  protected JTextField mStartDepthOfMLField = null;
  protected JTextField mStartDepthOfSlopeField = null;
  protected JTextField mDeltaTolerance = null;
  protected JRadioButton mDifferenceMethod = null;
  protected JRadioButton mSurfaceMethod = null;
  protected JRadioButton mSlopeMethod = null;
  ; protected JLabel mDepthOfSurfaceLbl = null;
  protected JLabel mStartDepthOfSurfaceLbl = null;
  protected JLabel mMaxDepthLbl = null;
  protected JLabel mSlopeDepthLbl = null;
  protected JLabel mdb1Lbl = null;
  protected JLabel mdb2Lbl = null;
  protected JLabel mdb3Lbl = null;
  protected JButton mOKBtn = null;
  protected JButton mCancelButton = null;
  protected JTextField mNewParamName = null;
  protected JTextField mNewParamUnits = null;
	private Timer timer = new Timer();
  protected JLabel mToleranceLbl = null;
  int mMethod = NQueryConstants.MIXED_LAYER_DIFFERENCE;
  ArrayList mVarList;
  NQueryCalculation mCalc;
  DialogClient mClient;
  NQMixedLayerCalcSpec mSpec;
  protected ResourceBundle b = ResourceBundle.getBundle("gov.noaa.pmel.nquery.resources.NQueryResources");

  public ConfigNQMLDCalc(JFrame par, ArrayList varList, DialogClient client) {
    super(par, "", false);
    this.setTitle(b.getString("kMixedLayerDepthCalculations"));
    mVarList = varList;
    mClient = client;
    this.init();
  }

  public void setSpecification(NQueryCalculation calc, NQMixedLayerCalcSpec inspec) {
    mCalc = calc;
    mSpec = inspec;

    // modify the UI to reflect
    ExportVariable var = mSpec.getParam();
    mMLParamList.setSelectedLine(var);

    if (mSpec.getMethod() == NQueryConstants.MIXED_LAYER_DIFFERENCE) {
      mDifferenceMethod.setSelected(true);
      mSurfaceMethod.setSelected(false);
      mSlopeMethod.setSelected(false);
    }
    else if (mSpec.getMethod() == NQueryConstants.MIXED_LAYER_SURFACE) {
      mDifferenceMethod.setSelected(false);
      mSurfaceMethod.setSelected(true);
      mSlopeMethod.setSelected(false);
    }
    else if (mSpec.getMethod() == NQueryConstants.MIXED_LAYER_SLOPE) {
      mDifferenceMethod.setSelected(false);
      mSurfaceMethod.setSelected(false);
      mSlopeMethod.setSelected(true);
    }

    double depth = mSpec.getDepth();
    double startDepth = mSpec.getStartDepth();

    if (mSpec.getMethod() == NQueryConstants.MIXED_LAYER_DIFFERENCE) {
      mDepthOfDiffMLField.setText(String.valueOf(depth));
    }
    else if (mSpec.getMethod() == NQueryConstants.MIXED_LAYER_SURFACE) {
      mDepthOfMLField.setText(String.valueOf(depth));
      mStartDepthOfMLField.setText(String.valueOf(startDepth));
    }
    else {
      mStartDepthOfSlopeField.setText(String.valueOf(depth));
    }
    mDeltaTolerance.setText(String.valueOf(mSpec.getTolerance()));

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

    // Mixedlayer
    JPanel mixedLayerPanel = new JPanel();
    mixedLayerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0)); //new RowLayout(Orientation.LEFT, Orientation.BOTTOM, 5));

    mMLParamList = new NQParameterChooser(mVarList, new String(b.getString("kTestParameter")), this, "SALT       ");
    mMLParamList.init();
    mixedLayerPanel.add(new TenPixelBorder(mMLParamList, 0, 0, 0, 0));

    //method
    JPanel methodCont = new JPanel();
    methodCont.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));

    // difference method
    mDifferenceMethod = new JRadioButton(b.getString("kDifferenceMethod"), true);
    mDifferenceMethod.addItemListener(this);
    methodCont.add(mDifferenceMethod);
    JPanel linem1 = new JPanel();
    linem1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    mMaxDepthLbl = new JLabel("     " + b.getString("kDepthOfDifference"));
    linem1.add(mMaxDepthLbl);
    mDepthOfDiffMLField = new JTextField(6);
    mDepthOfDiffMLField.setText("5");
    mDepthOfDiffMLField.getDocument().addDocumentListener(this);
    linem1.add(mDepthOfDiffMLField);
    mdb1Lbl = new JLabel(b.getString("kDB"));
    linem1.add(mdb1Lbl);
    methodCont.add(linem1);

    //surface
    mSurfaceMethod = new JRadioButton(b.getString("kSurfaceMethod"));
    mSurfaceMethod.addItemListener(this);
    methodCont.add(mSurfaceMethod);
    JPanel line0 = new JPanel();
    line0.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    mStartDepthOfSurfaceLbl = new JLabel("     " + b.getString("kStartDepthOfSurface"));
    mDepthOfSurfaceLbl = new JLabel("     " + b.getString("kMaxDepthOfSurface"));
    mDepthOfMLField = new JTextField(6);
    mDepthOfMLField.setText("5");
    mDepthOfMLField.getDocument().addDocumentListener(this);
    mStartDepthOfMLField = new JTextField(6);
    mStartDepthOfMLField.setText("0");
    mStartDepthOfMLField.getDocument().addDocumentListener(this);
    line0.add(mStartDepthOfSurfaceLbl);
    line0.add(mStartDepthOfMLField);
    line0.add(mDepthOfSurfaceLbl);
    line0.add(mDepthOfMLField);
    mdb2Lbl = new JLabel(b.getString("kDB"));
    line0.add(mdb2Lbl);
    methodCont.add(line0);

    // slope
    mSlopeMethod = new JRadioButton(b.getString("kSlopeMethod"));
    mSlopeMethod.addItemListener(this);
    methodCont.add(mSlopeMethod);
    JPanel line00 = new JPanel();
    line00.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    mSlopeDepthLbl = new JLabel("     " + b.getString("kDepthOfSlope"));
    line00.add(mSlopeDepthLbl);
    mStartDepthOfSlopeField = new JTextField(6);
    mStartDepthOfSlopeField.setText("5");
    mStartDepthOfSlopeField.getDocument().addDocumentListener(this);
    line00.add(mStartDepthOfSlopeField);
    mdb3Lbl = new JLabel(b.getString("kDB"));
    line00.add(mdb3Lbl);
    methodCont.add(line00);

    ButtonGroup b1 = new ButtonGroup();
    b1.add(mDifferenceMethod);
    b1.add(mSurfaceMethod);
    b1.add(mSlopeMethod);
    mixedLayerPanel.add(new TenPixelBorder(methodCont, 0, 0, 0, 0));

    //tolerance panel
    JPanel line3 = new JPanel();
    line3.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    mToleranceLbl = new JLabel(b.getString("kTolerance"));
    line3.add(mToleranceLbl);
    mDeltaTolerance = new JTextField(6);
    mDeltaTolerance.setText("0.05");
    mDeltaTolerance.getDocument().addDocumentListener(this);
    line3.add(mDeltaTolerance);
    mixedLayerPanel.add(line3);

    JPanel lastline = new JPanel();
    lastline.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
    lastline.add(new JLabel(b.getString("kVariableName")));
    mNewParamName = new JTextField(40);
    lastline.add(mNewParamName);
    mainPanel.add("South", lastline);

    // construct the dialog
    JPanel mainPanelContents = new JPanel();
    mainPanelContents.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
    mainPanelContents.add(new TenPixelBorder(mixedLayerPanel, 5, 5, 5, 5));
    mainPanel.add("Center", mainPanelContents);
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

    setSlopeState(false);
    setSurfaceState(false);
    setDiffState(true);
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
    if (evt.getSource() == mMLParamList.getJList()) {
      // get the integrand param
      mSelMLParam = mMLParamList.getJList().getSelectedIndex();
      if (mSelMLParam < 0) {
        return;
      }
      String selParamText = (String)mMLParamList.getJList().getSelectedValue();
    }
    generateVariableName();
  }

  public void itemStateChanged(ItemEvent evt) {
    if (evt.getSource() instanceof JRadioButton) {
      JRadioButton rb = (JRadioButton)evt.getSource();
      if (rb == mSlopeMethod && evt.getStateChange() == ItemEvent.SELECTED) {
        setSlopeState(true);
        setSurfaceState(false);
        setDiffState(false);
      }
      else if (rb == mDifferenceMethod && evt.getStateChange() == ItemEvent.SELECTED) {
        setSlopeState(false);
        setSurfaceState(false);
        setDiffState(true);
      }
      else if (rb == mSurfaceMethod && evt.getStateChange() == ItemEvent.SELECTED) {
        setSlopeState(false);
        setSurfaceState(true);
        setDiffState(false);
      }
    }
    generateVariableName();
  }

  public void setSlopeState(boolean state) {
    mStartDepthOfSlopeField.setEnabled(state);
    mSlopeDepthLbl.setEnabled(state);
    mdb3Lbl.setEnabled(state);
  }

  public void setSurfaceState(boolean state) {
    mDepthOfMLField.setEnabled(state);
    mStartDepthOfMLField.setEnabled(state);
    mStartDepthOfSurfaceLbl.setEnabled(state);
    mDepthOfSurfaceLbl.setEnabled(state);
    mdb2Lbl.setEnabled(state);
  }

  public void setDiffState(boolean state) {
    mDepthOfDiffMLField.setEnabled(state);
    mMaxDepthLbl.setEnabled(state);
    mdb1Lbl.setEnabled(state);
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      timer.cancel();
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      // transfer pending calculations to the fileviewer for processing
      //configure a mixedlevel calc
      double surfaceDepth = -99;
      double startSurfaceDepth = -99;
      if (mDifferenceMethod.isSelected()) {
        mMethod = NQueryConstants.MIXED_LAYER_DIFFERENCE;
        try {
          surfaceDepth = Double.valueOf(mDepthOfDiffMLField.getText()).doubleValue();
        }
        catch (NumberFormatException ex) {
          surfaceDepth = 5;
        }
      }
      else if (mSurfaceMethod.isSelected()) {
        mMethod = NQueryConstants.MIXED_LAYER_SURFACE;
        try {
          surfaceDepth = Double.valueOf(mDepthOfMLField.getText()).doubleValue();
        }
        catch (NumberFormatException ex) {
          surfaceDepth = 5;
        }
        try {
          startSurfaceDepth = Double.valueOf(mStartDepthOfMLField.getText()).doubleValue();
        }
        catch (NumberFormatException ex) {
          startSurfaceDepth = 0;
        }
      }
      else {
        mMethod = NQueryConstants.MIXED_LAYER_SLOPE;
        try {
          surfaceDepth = Double.valueOf(mStartDepthOfSlopeField.getText()).doubleValue();
        }
        catch (NumberFormatException ex) {
          surfaceDepth = 0;
        }
      }

      double toln = 0.001;
      try {
        toln = Double.valueOf(mDeltaTolerance.getText()).doubleValue();
      }
      catch (NumberFormatException ex) {
        toln = 0.001;
      }

      String param = (String)(mMLParamList.getJList().getSelectedValue());

      // get the ExportVariable represented by this selection
      Iterator itor = mVarList.iterator();
      ExportVariable theParam = null;
      while (itor.hasNext()) {
        theParam = (ExportVariable)itor.next();
        if (theParam.getPresentationVarName().equalsIgnoreCase(param)) {
          break;
        }
      }

      if (mSpec == null) {
        if (mMethod == NQueryConstants.MIXED_LAYER_DIFFERENCE) {
          mCalc = new NQueryCalculation(mNewParamName.getText(), "MLDF",
                                  new NQMixedLayerCalcSpec(NQueryConstants.MIXED_LAYER_DIFFERENCE, theParam,
              startSurfaceDepth, surfaceDepth, toln), NQueryConstants.STN_CALC_TYPE, true, true);
          mCalc.setUnits("m");
        }
        else if (mMethod == NQueryConstants.MIXED_LAYER_SURFACE) {
          mCalc = new NQueryCalculation(mNewParamName.getText(), "MLSF",
                                  new NQMixedLayerCalcSpec(NQueryConstants.MIXED_LAYER_SURFACE, theParam,
              startSurfaceDepth, surfaceDepth, toln), NQueryConstants.STN_CALC_TYPE, true, true);
          mCalc.setUnits("m");
        }
        else if (mMethod == NQueryConstants.MIXED_LAYER_SLOPE) {
          mCalc = new NQueryCalculation(mNewParamName.getText(), "MLSL",
                                  new NQMixedLayerCalcSpec(NQueryConstants.MIXED_LAYER_SLOPE, theParam, startSurfaceDepth,
              surfaceDepth, toln), NQueryConstants.STN_CALC_TYPE, true, true);
          mCalc.setUnits("m");
        }
      }
      else {
        if (mMethod == NQueryConstants.MIXED_LAYER_DIFFERENCE) {
          mCalc.setCalcType(mNewParamName.getText());
          mSpec = new NQMixedLayerCalcSpec(NQueryConstants.MIXED_LAYER_DIFFERENCE, theParam, startSurfaceDepth,
                                         surfaceDepth, toln);
          mCalc.setUnits("m");
        }
        else if (mMethod == NQueryConstants.MIXED_LAYER_SURFACE) {
          mCalc.setCalcType(mNewParamName.getText());
          mSpec = new NQMixedLayerCalcSpec(NQueryConstants.MIXED_LAYER_SURFACE, theParam, startSurfaceDepth, surfaceDepth,
                                         toln);
          mCalc.setUnits("m");
        }
        else if (mMethod == NQueryConstants.MIXED_LAYER_SLOPE) {
          mCalc.setCalcType(mNewParamName.getText());
          mSpec = new NQMixedLayerCalcSpec(NQueryConstants.MIXED_LAYER_SLOPE, theParam, startSurfaceDepth, surfaceDepth,
                                         toln);
          mCalc.setUnits("m");
        }
        mCalc.setArg(mSpec);
      }
      mClient.dialogDismissed(this);
      timer.cancel();
      this.dispose();
    }
  }

  public void maintainButtons() {

    boolean MLOK = mSelMLParam >= 0;
    if (!MLOK) {
      mOKBtn.setEnabled(false);
    }
    else {
      mOKBtn.setEnabled(true);
    }
  }

  public Object getCalculation() {
    return mCalc;
  }

  public Object getSpecification() {
    return mSpec;
  }

  public void generateVariableName() {
    String surfaceDepthStr = "?";
    String startSurfaceDepthStr = "?";
    int method;
    if (mDifferenceMethod.isSelected()) {
      method = NQueryConstants.MIXED_LAYER_DIFFERENCE;
      try {
        double surfaceDepth = Double.valueOf(mDepthOfDiffMLField.getText()).doubleValue();
        surfaceDepthStr = String.valueOf(surfaceDepth);

        // remove the decimal point from string
        surfaceDepthStr = surfaceDepthStr.substring(0, surfaceDepthStr.indexOf('.'));
      }
      catch (NumberFormatException ex) {
        surfaceDepthStr = "?";
      }
    }
    else if (mSurfaceMethod.isSelected()) {
      method = NQueryConstants.MIXED_LAYER_SURFACE;
      try {
        double surfaceDepth = Double.valueOf(mDepthOfMLField.getText()).doubleValue();
        surfaceDepthStr = String.valueOf(surfaceDepth);

        // remove the decimal point from string
        surfaceDepthStr = surfaceDepthStr.substring(0, surfaceDepthStr.indexOf('.'));
      }
      catch (NumberFormatException ex) {
        surfaceDepthStr = "?";
      }
      try {
        double startSurfaceDepth = Double.valueOf(mStartDepthOfMLField.getText()).doubleValue();
        startSurfaceDepthStr = String.valueOf(startSurfaceDepth);

        // remove the decimal point from string
        startSurfaceDepthStr = startSurfaceDepthStr.substring(0, startSurfaceDepthStr.indexOf('.'));
      }
      catch (NumberFormatException ex) {
        startSurfaceDepthStr = "?";
      }
    }
    else {
      method = NQueryConstants.MIXED_LAYER_SLOPE;
      try {
        double surfaceDepth = Double.valueOf(mStartDepthOfSlopeField.getText()).doubleValue();
        surfaceDepthStr = String.valueOf(surfaceDepth);

        // remove the decimal point from string
        surfaceDepthStr = surfaceDepthStr.substring(0, surfaceDepthStr.indexOf('.'));
      }
      catch (NumberFormatException ex) {
        surfaceDepthStr = "?";
      }
    }

    String tolnStr = "?";
    try {
      double toln = Double.valueOf(mDeltaTolerance.getText()).doubleValue();
      tolnStr = String.valueOf(toln);

      // remove the decimal point from string
      tolnStr = tolnStr.substring(tolnStr.indexOf('.') + 1, tolnStr.length());
    }
    catch (NumberFormatException ex) {
      tolnStr = "?";
    }

    String param = (String)(mMLParamList.getJList().getSelectedValue());
    if (param == null) {
      param = "?";
    }
    String nameString = "?";

    if (method == NQueryConstants.MIXED_LAYER_DIFFERENCE) {
      nameString = "MLDF_" + param + "_" + surfaceDepthStr + "_" + tolnStr;
    }
    else if (method == NQueryConstants.MIXED_LAYER_SURFACE) {
      nameString = "MLSF_" + param + "_" + startSurfaceDepthStr + "_to_" + surfaceDepthStr + "_" + tolnStr;
    }
    else if (method == NQueryConstants.MIXED_LAYER_SLOPE) {
      nameString = "MLSL_" + param + "_" + surfaceDepthStr + "_" + tolnStr;
    }
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

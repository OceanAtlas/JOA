
/*
 * $Id: ConfigCustomCalc.java,v 1.6 2005/09/07 18:49:30 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import javaoceanatlas.calculations.*;
import javaoceanatlas.classicdatamodel.OpenDataFile;
import javaoceanatlas.classicdatamodel.Section;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class ConfigCustomStnCalc extends JOAJDialog implements ListSelectionListener, ActionListener, ButtonMaintainer,
    ItemListener, DocumentListener {
  protected FileViewer mFileViewer;
  protected StationVarChooser mXParamList;
  protected StationVarChooser mYParamList;
  protected int mSelXParam = -1;
  protected int mSelYParam = -1;
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mCancelButton = null;
  protected JOAJButton mGenerateButton = null;
  protected JOAJTextField mXConstantField = null;
  protected JOAJTextField mYConstantField = null;
  protected JOAJTextField mNewParamName = null;
  protected JOAJTextField mNewParamUnits = null;
  protected JOAJRadioButton plus = null;
  protected JOAJRadioButton minus = null;
  protected JOAJRadioButton times = null;
  protected JOAJRadioButton dividedby = null;
//  protected JOAJRadioButton derivative = null;
//  protected JOAJRadioButton integral = null;
  protected JOAJCheckBox reverseY = null;
  protected JOAJRadioButton create = null;
  protected JOAJList mPendingCalcList = null;
  protected JOAJButton mCreateCalc = null;
  protected boolean mUseConstant1 = false;
  protected boolean mUseConstant2 = false;
  protected boolean mIgnoreVC = false;
  protected int mNewParamCnt = 1;
  protected String mOpText = new String(" + ");
  protected String mShortOpText = new String("+");
  protected Vector<CustomStnCalculation> mPendingCalcs = new Vector<CustomStnCalculation>();
  protected int mOperand = JOAConstants.PLUS_OP;
  protected JOAJList mXParamJList;
  protected JOAJList mYParamJList;
	private Timer timer = new Timer();

  public ConfigCustomStnCalc(JFrame par, FileViewer fv) {
    super(par, "Custom Station Calculations", false);
    mFileViewer = fv;
    this.init();
  }

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    // create the two station variable chooser lists
    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(5, 5));
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout(5, 5));
    JPanel paramPanel = new JPanel();
    JPanel upperPanel = new JPanel();
    upperPanel.setLayout(new BorderLayout(5, 5));
    paramPanel.setLayout(new RowLayout(Orientation.LEFT, Orientation.CENTER, 5));
    mXParamList = new StationVarChooser(mFileViewer, new String(b.getString("kSV1")), this,
                                       "SALT                               xxxxxxxxxxxx");
    mYParamList = new StationVarChooser(mFileViewer, new String(b.getString("kSV2")), this,
                                       "SALT                               xxxxxxxxxxxx");
    mXParamList.init();
    mYParamList.init();
    mXParamJList = mXParamList.getJList();
    mYParamJList = mYParamList.getJList();

    mXParamJList.addListSelectionListener(this);
    mYParamJList.addListSelectionListener(this);

    // param panel containers
    JPanel xParamCont = new JPanel();
    xParamCont.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));
    JPanel yParamCont = new JPanel();
    yParamCont.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

    // x parameter
    xParamCont.add(mXParamList);
    JPanel constantLine0 = new JPanel();
    constantLine0.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    constantLine0.add(new JOAJLabel(b.getString("kConstant")));
    mXConstantField = new JOAJTextField(4);
    mXConstantField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mXConstantField.getDocument().addDocumentListener(this);
    mXConstantField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if (mXConstantField.getText().length() > 0) {
          mUseConstant1 = true;
        }
        else {
          mUseConstant1 = false;
        }
//        generateParamName();
      }
    });

    mXConstantField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent me) {
        if (mXConstantField.getText().length() > 0) {
          mUseConstant1 = true;
        }
        else {
          mUseConstant1 = false;
        }
      }
    });
    constantLine0.add(mXConstantField);
    xParamCont.add(constantLine0);
    paramPanel.add(xParamCont);

    // add the operator panel
    JPanel opPanel = new JPanel();
    opPanel.setLayout(new GridLayout(5, 1, 5, 0));
    JPanel plusPanel = new JPanel();
    plusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    plus = new JOAJRadioButton("", true);
    JOAJLabel lbl1 = new JOAJLabel(new ImageIcon(getClass().getResource("images/plus.gif")));
    plusPanel.add(plus);
    plusPanel.add(lbl1);
    JPanel minusPanel = new JPanel();
    minusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    minus = new JOAJRadioButton("");
    JOAJLabel lbl2 = new JOAJLabel(new ImageIcon(getClass().getResource("images/minus.gif")));
    minusPanel.add(minus);
    minusPanel.add(lbl2);
    JPanel timesPanel = new JPanel();
    timesPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    times = new JOAJRadioButton("");
    JOAJLabel lbl3 = new JOAJLabel(new ImageIcon(getClass().getResource("images/mult.gif")));
    timesPanel.add(times);
    timesPanel.add(lbl3);
    JPanel dividePanel = new JPanel();
    dividePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    dividedby = new JOAJRadioButton("");
    JOAJLabel lbl4 = new JOAJLabel(new ImageIcon(getClass().getResource("images/divide.gif")));
    dividePanel.add(dividedby);
    dividePanel.add(lbl4);
//    JPanel derivPanel = new JPanel();
//    derivPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
//    derivative = new JOAJRadioButton("");
//    JOAJLabel lbl5 = new JOAJLabel(new ImageIcon(getClass().getResource("images/derivative.gif")));
//    derivPanel.add(derivative);
//    derivPanel.add(lbl5);
//    JPanel intPanel = new JPanel();
//    intPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
//    integral = new JOAJRadioButton("");
//    JOAJLabel lbl6 = new JOAJLabel(new ImageIcon(getClass().getResource("images/integral.gif")));
//    intPanel.add(integral);
//    intPanel.add(lbl6);
    ButtonGroup bg = new ButtonGroup();
    bg.add(plus);
    bg.add(minus);
    bg.add(times);
    bg.add(dividedby);
//    bg.add(derivative);
//    bg.add(integral);
    opPanel.add(plusPanel);
    opPanel.add(minusPanel);
    opPanel.add(timesPanel);
    opPanel.add(dividePanel);
//    opPanel.add(derivPanel);
//    opPanel.add(intPanel);
    plus.addItemListener(this);
    minus.addItemListener(this);
    times.addItemListener(this);
    dividedby.addItemListener(this);
//    derivative.addItemListener(this);
//    integral.addItemListener(this);
    paramPanel.add(opPanel);

    // y parameter
    yParamCont.add(mYParamList);
    JPanel constantLine1 = new JPanel();
    constantLine1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    constantLine1.add(new JOAJLabel(b.getString("kConstant")));
    mYConstantField = new JOAJTextField(4);
    mYConstantField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mYConstantField.getDocument().addDocumentListener(this);
    mYConstantField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if (mYConstantField.getText().length() > 0) {
          mUseConstant2 = true;
        }
        else {
          mUseConstant2 = false;
        }
//        generateParamName();
      }
    });

    mYConstantField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent me) {
        if (mYConstantField.getText().length() > 0) {
          mUseConstant2 = true;
        }
        else {
          mUseConstant2 = false;
        }
      }
    });
    constantLine1.add(mYConstantField);
    yParamCont.add(constantLine1);
    paramPanel.add(yParamCont);

    JPanel middlePanel = new JPanel();
    middlePanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

    JPanel line1 = new JPanel();
    line1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    line1.add(new JOAJLabel(b.getString("kNewParameterName")));
    mNewParamName = new JOAJTextField("NSV" + mNewParamCnt, 20);
    mNewParamName.setText("NSV" + mNewParamCnt);
    mNewParamName.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line1.add(mNewParamName);
    mGenerateButton = new JOAJButton(b.getString("kGenName"));
    mGenerateButton.setActionCommand("genname");
    mGenerateButton.addActionListener(this);
    line1.add(mGenerateButton);
    middlePanel.add(line1);

    JPanel line1a = new JPanel();
    line1a.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    line1a.add(new JOAJLabel(b.getString("kUnits")));
    mNewParamUnits = new JOAJTextField(6);
    mNewParamUnits.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line1a.add(mNewParamUnits);
    middlePanel.add(line1a);

    JPanel line2 = new JPanel();
    line2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    reverseY = new JOAJCheckBox(b.getString("kReverseWhenY"));
    line2.add(reverseY);
    middlePanel.add(line2);

    // construct the pending calc panel
    JPanel pendingCalcsCont = new JPanel();
    pendingCalcsCont.setLayout(new RowLayout(Orientation.LEFT, Orientation.CENTER, 5));
    mCreateCalc = new JOAJButton(b.getString("kCreateCalculation"));
    pendingCalcsCont.add(mCreateCalc);
    mCreateCalc.setActionCommand("create");
    mCreateCalc.addActionListener(this);
    middlePanel.add(pendingCalcsCont);

    JPanel pendingCalcsListCont = new JPanel();
    pendingCalcsListCont.setLayout(new BorderLayout(5, 5));
    pendingCalcsListCont.add(new JOAJLabel(b.getString("kPendingCalculations"), JOAJLabel.LEFT), "North");
    mPendingCalcList = new JOAJList();
    mPendingCalcList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mPendingCalcList.setVisibleRowCount(3);
    mPendingCalcList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mPendingCalcList.setPrototypeCellValue("SAL1 = SALT/0.000000                               xxxxxxxxxxxxx");
    JScrollPane listScroller = new JScrollPane(mPendingCalcList);
    pendingCalcsListCont.add(listScroller, "Center");
    pendingCalcsCont.add(pendingCalcsListCont);
    middlePanel.add(pendingCalcsCont);

    // construct the dialog
    upperPanel.add("Center", paramPanel);
    upperPanel.add("South", middlePanel);
    mainPanel.add("Center", new TenPixelBorder(upperPanel, 10, 10, 10, 10));

    // lower panel mGenerateButton
    mOKBtn = new JOAJButton(b.getString("kOK"));
    mOKBtn.setActionCommand("ok");
    this.getRootPane().setDefaultButton(mOKBtn);
    mCancelButton = new JOAJButton(b.getString("kCancel"));
    mCancelButton.setActionCommand("cancel");

    JPanel dlgBtnsInset = new JPanel();
    JPanel dlgBtnsPanel = new JPanel();
    dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
    dlgBtnsPanel.setLayout(new GridLayout(1, 3, 15, 1));
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

    mainPanel.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
    contents.add("Center", mainPanel);
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


  private void maintainYList() {
    // unselect anything in the y List
    mYParamJList.clearSelection();
    mSelYParam = -99;
  }

  private void maintainXList() {
    // unselect anything in the x List
    mXParamJList.clearSelection();
    mSelXParam = -99;
  }

  public void valueChanged(ListSelectionEvent evt) {
    if (evt.getSource() == mXParamJList) {
      if (mIgnoreVC == true) {
        mIgnoreVC = false;
        return;
      }
      // get the x param
      mSelXParam = mXParamJList.getSelectedIndex();
      if (mSelXParam < 0) {
        return;
      }
      // String selParamText = (String)mXParamJList.getSelectedValue();

      /* make sure value of the param is not missing
          int yerrLine = -1;
             double tempYMin = mFileViewer.mAllProperties[mSelXParam].getPlotMin();
             double tempYMax = mFileViewer.mAllProperties[mSelXParam].getPlotMax();
             Triplet newRange = JOAFormulas.GetPrettyRange(tempYMin, tempYMax);
             double yMin = newRange.getVal1();
             double yMax = newRange.getVal2();
             double yInc = newRange.getVal3();
             if (Double.isNaN(yInc)) {
              yerrLine = mSelXParam;
             }

             if (yerrLine >= 0) {
       //disable the y param
           JFrame f = new JFrame("Parameter Values Missing Error");
           Toolkit.getDefaultToolkit().beep();
           JOptionPane.showMessageDialog(f, "All values for " + selParamText +  " are missing. " +
                                  "\n" +
                                  "Select a new parameter");
       mXParamList.clearSelection();
       mSelXParam = 0;
                }*/

      // empty out the constant field
      mXConstantField.setText("");
      mUseConstant1 = false;
//      generateParamName();
    }
    else if (evt.getSource() == mYParamJList) {
      if (mIgnoreVC == true) {
        mIgnoreVC = false;
        return;
      }

      // get the y param
      mSelYParam = mYParamJList.getSelectedIndex();
      if (mSelYParam < 0) {
        return;
      }
      //String selParamText = (String)mYParamJList.getSelectedValue();

      /*// make sure value of the param is not missing
         int yerrLine = -1;
            double tempYMin = mFileViewer.mAllProperties[mSelYParam].getPlotMin();
            double tempYMax = mFileViewer.mAllProperties[mSelYParam].getPlotMax();
            Triplet newRange = JOAFormulas.GetPrettyRange(tempYMin, tempYMax);
            double yMin = newRange.getVal1();
            double yMax = newRange.getVal2();
            double yInc = newRange.getVal3();
            if (Double.isNaN(yInc)) {
             yerrLine = mSelYParam;
            }

            if (yerrLine >= 0) {
      //disable the y param
          JFrame f = new JFrame("Parameter Values Missing Error");
          Toolkit.getDefaultToolkit().beep();
          JOptionPane.showMessageDialog(f, "All values for " + selParamText +  " are missing. " +
                                 "\n" +
                                 "Select a new parameter");
      mYParamList.clearSelection();
      mSelYParam = 0;
               }*/

     // empty out the constant field
     mYConstantField.setText("");
     mUseConstant2 = false;
//      generateParamName();
    }
  }

  public void itemStateChanged(ItemEvent evt) {
    if (evt.getSource() instanceof JOAJRadioButton) {
      JOAJRadioButton rb = (JOAJRadioButton)evt.getSource();
      if (evt.getStateChange() == ItemEvent.SELECTED && rb == plus) {
        mOpText = " + ";
        mShortOpText = "+";
        mOperand = JOAConstants.PLUS_OP;
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == minus) {
        mOpText = " - ";
        mShortOpText = "-";
        mOperand = JOAConstants.MINUS_OP;
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == times) {
        mOpText = " * ";
        mShortOpText = "*";
        mOperand = JOAConstants.TIMES_OP;
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == dividedby) {
        mOpText = "/";
        mShortOpText = "/";
        mOperand = JOAConstants.DIVIDE_OP;
      }
//      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == derivative) {
//        mOpText = "d";
//        mShortOpText = "d";
//        mOperand = JOAConstants.DERIVATIVE_OP;
//      }
//      else if (evt.getStateChange() == ItemEvent.SELECTED && rb == integral) {
//        mOpText = "int";
//        mOperand = JOAConstants.INTEGRAL_OP;
//      }
//      generateParamName();
    }
    else if (evt.getSource() instanceof JOAJRadioButton) {

    }
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      timer.cancel();      
      this.dispose();
    }
    else if (cmd.equals("genname")) {
    	generateParamName();
    }
    else if (cmd.equals("ok")) {
      // transfer pending calculations to the fileviewer for processing
      for (int i = 0; i < mPendingCalcs.size(); i++) {
       CustomStnCalculation cc = (CustomStnCalculation)mPendingCalcs.elementAt(i);
       mFileViewer.addCustomStnCalculation(cc);
      }
      mFileViewer.doCustomStnCalcs();
      timer.cancel();      
      this.dispose();
    }
    else if (cmd.equals("create")) {
      String newParam = mNewParamName.getText();
      if (newParam.length() == 0) {
        newParam = "UNTL";
      }
      //else
      //	newParam = JOAFormulas.formatParamName(newParam);
      mNewParamName.setText(newParam);

      String units = mNewParamUnits.getText();
      if (units.length() == 0) {
        units = "none";
      }

      double con1 = -99, con2 = -99;
      String newCalcName = null;
//      if (!integral.isSelected()) {
        String xVarText = null;
        String yVarText = null;
        if (mUseConstant1) {
          xVarText = mXConstantField.getText();
        }
        else {
          xVarText = (String)mXParamJList.getSelectedValue();
        }

        if (mUseConstant2) {
          yVarText = mYConstantField.getText();
        }
        else {
          yVarText = (String)mYParamJList.getSelectedValue();
        }

//        if (mOperand == JOAConstants.DERIVATIVE_OP) {
//          newCalcName = new String(newParam + " = d(" + xVarText + ")/d(" + yVarText + ")");
//        }
//        else {
          newCalcName = new String(newParam + " = " + xVarText + mOpText + yVarText); ;
//        }
        try {
          con1 = Double.valueOf(mXConstantField.getText()).doubleValue();
        }
        catch (Exception ex) {
          con1 = -99.0;
        }

        try {
          con2 = Double.valueOf(mYConstantField.getText()).doubleValue();
        }
        catch (Exception ex) {
          con2 = -99.0;
        }
//      }
//      else {
//        newCalcName = "int" + (String)mXParamJList.getSelectedValue();
//      }

      // create a new calculation object
      CustomStnCalculation cc = new CustomStnCalculation(newCalcName, newParam, units, (String)mXParamJList.getSelectedValue(),
          (String)mYParamJList.getSelectedValue(), mOperand, con1, con2, reverseY.isSelected());
      
//      if (integral.isSelected()) {
//      	cc.setIsTransform();
//      }

      // add this to the pending calc list
      mPendingCalcs.addElement(cc);

      // update the list
      Vector<String> pendingCalcNames = new Vector<String>();
      for (int i = 0; i < mPendingCalcs.size(); i++) {
        cc = mPendingCalcs.elementAt(i);
        String calc = cc.getCalcString();
        pendingCalcNames.addElement(calc);
      }
      mPendingCalcList.setListData(pendingCalcNames);
      mPendingCalcList.invalidate();
      this.validate();

      // add pending parameters to the x and y lists
      mXParamList.addToList(newParam);
      mYParamList.addToList(newParam);
      mXParamList.invalidate();
      mYParamList.invalidate();
      this.validate();

      // update default name stuff
      mNewParamName.setText("NSV" + ++mNewParamCnt);
      mNewParamUnits.setText("");
    }
  }

  public void generateParamName() {
    String nameString = null;
    String param = (String)mXParamJList.getSelectedValue();
    String param2 = (String)mYParamJList.getSelectedValue();
      if (mXParamJList.getSelectedIndex() >= 0 && mYConstantField.getText().length() > 0) {
        nameString = new String(param + mShortOpText + mYConstantField.getText());
      }
      else if (mYParamJList.getSelectedIndex() >= 0 && mXConstantField.getText().length() > 0) {
        nameString = new String(mXConstantField.getText() + mShortOpText + param2);
      }
      else {
        if (param == null && param2 == null) {
          nameString = new String("?");
        }
        else if (param == null && param2 != null) {
          nameString = new String("?" + mShortOpText + param2);
        }
        else if (param != null && param2 == null) {
          nameString = new String(param + mShortOpText + "?");
        }
        else {
          nameString = new String(param + mShortOpText + param2);
        }
      }
    mNewParamName.setText(nameString);
  }

  public void maintainButtons() {
    if (mYConstantField.getText().length() > 0) {
      mSelYParam = -99;
      mIgnoreVC = true;
      maintainYList();
    }
    if (mXConstantField.getText().length() > 0) {
      mSelXParam = -99;
      mIgnoreVC = true;
      maintainXList();
    }

    if ((mXParamJList.getSelectedIndex() >= 0 || mXConstantField.getText().length() > 0) &&
        (mYParamJList.getSelectedIndex() >= 0 || mYConstantField.getText().length() > 0)) {
      mCreateCalc.setEnabled(true);
    }
    else {
      mCreateCalc.setEnabled(false);
    }

    if (mXConstantField.getText().length() > 0 && mYConstantField.getText().length() > 0) {
      mCreateCalc.setEnabled(false);
    }

//    if (mXParamJList.getSelectedIndex() >= 0 && integral.isSelected()) {
//      mCreateCalc.setEnabled(true);
//    }

    if (mPendingCalcList.getModel().getSize() > 0) {
      mOKBtn.setEnabled(true);
    }
    else {
      mOKBtn.setEnabled(false);
    }
  }

  public void changedUpdate(DocumentEvent evt) {
//    generateParamName();
  }

  public void insertUpdate(DocumentEvent evt) {
//    generateParamName();
  }

  public void removeUpdate(DocumentEvent evt) {
//    generateParamName();
  }


	private void buildStnVarList() {
		Vector<String> params = new Vector<String>();
		OpenDataFile of = (OpenDataFile) mFileViewer.mOpenFiles.currElement();
		Section sech = (Section) of.getCurrSection();
		for (int i = 0; i < sech.getNumStnVars(); i++) {
			params.addElement(sech.getStnVar(i));
		}

		if (mXParamJList == null) {
			mXParamJList = new JOAJList(params);
		}
		else {
			mXParamJList.setListData(params);
			mXParamJList.invalidate();
		}

		if (mYParamJList == null) {
			mYParamJList = new JOAJList(params);
		}
		else {
			mYParamJList.setListData(params);
			mYParamJList.invalidate();
		}
	}
}

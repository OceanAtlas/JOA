/*
 * $Id: ConfigUVCalculation.java,v 1.6 2005/09/07 18:49:30 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.calculations.Calculation;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.DirectionCalculationSpecification;
import javaoceanatlas.specifications.SpeedCalculationSpecification;

@SuppressWarnings("serial")
public class ConfigUVCalculation extends JOAJDialog implements ListSelectionListener, ActionListener, ButtonMaintainer,
    ItemListener, DocumentListener {
  protected FileViewer mFileViewer;
  protected StnParameterChooser mUParamList;
  protected StnParameterChooser mVParamList;
  protected int mSelUParam = -1;
  protected int mSelVParam = -1;
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mCancelButton = null;
  protected JOAJTextField mNewSpeedParamName = null;
  protected JOAJTextField mNewDirectionParamName = null;
  protected JOAJTextField mNewSpeedParamUnits = null;
	private Timer timer = new Timer();
  protected JOAJList mUParamJList;
  protected JOAJList mVParamJList;
  protected boolean mIgnoreVC = false;
  private JTextArea convLbl = new JTextArea(5, 40);

  public ConfigUVCalculation(JFrame par, FileViewer fv) {
    super(par, "Configure Speed/Direction Calculation", false);
    mFileViewer = fv;
    this.init();
  }

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
    String l1 = "Uses TAO Coventions: \n";
    String l2 = "u is the East-West component. If it is positive, the East-West component " + "\n" +  "of the wind is blowing towards the East. If it is negative," + "\n" + "this component is blowing towards the West. \n";
    String l3 = "v is the North-South component of the wind. If it is positive, the North-South " + "\n" + "component of the wind is blowing towards the North. If it is" + "\n" +  "negative, this component is blowing towards the South. \n";
    String l4 = "Speed = sqrt(u^2 + v^2) \n";
    String l5 = "Direction in degrees clockwise from North computed using the relation" + "\n" +  "wdir = (180 / 3.14) * arctan(u/v). ";
    convLbl.setText(l1 + l2 + l3 + l4 + l5);
    convLbl.setEditable(false);
    convLbl.setWrapStyleWord(true);
    convLbl.setFont(new java.awt.Font("Dialog", 0, 9));
    
    // create the two parameter chooser lists
    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(5, 5));
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout(5, 5));
    JPanel paramPanel = new JPanel();
    JPanel upperPanel = new JPanel();
    upperPanel.setLayout(new BorderLayout(5, 5));
    paramPanel.setLayout(new RowLayout(Orientation.LEFT, Orientation.CENTER, 5));
    mUParamList = new StnParameterChooser(mFileViewer, new String(b.getString("kUComponent")), this, 5,
                                       "SALT                BBBB     X");
    mVParamList = new StnParameterChooser(mFileViewer, new String(b.getString("kVComponent")), this, 5,
                                       "SALT                CCCC     Y");
    mUParamList.init();
    mVParamList.init();
    mUParamJList = mUParamList.getJList();
    mVParamJList = mVParamList.getJList();
    mUParamJList.addListSelectionListener(this);
    mVParamJList.addListSelectionListener(this);

    // param panel containers
    JPanel xParamCont = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));
    JPanel yParamCont = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

    // x parameter
    xParamCont.add(mUParamList);
    paramPanel.add(xParamCont);

    // y parameter
    yParamCont.add(mVParamList);
    paramPanel.add(yParamCont);

    JPanel middlePanel = new JPanel();
    middlePanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));
    
    middlePanel.add(convLbl);

    JPanel line1 = new JPanel();
    line1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    line1.add(new JOAJLabel(b.getString("kNewSpeedParameterName")));
    mNewSpeedParamName = new JOAJTextField(10);
    mNewSpeedParamName.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line1.add(mNewSpeedParamName);
    middlePanel.add(line1);

    JPanel line1a = new JPanel();
    line1a.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    line1a.add(new JOAJLabel(b.getString("kSpeedUnits")));
    mNewSpeedParamUnits = new JOAJTextField(6);
    mNewSpeedParamUnits.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line1a.add(mNewSpeedParamUnits);
    middlePanel.add(line1a);
    
    JPanel line1b = new JPanel();
    line1b.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    line1b.add(new JOAJLabel(b.getString("kDirectionParameterName")));
    mNewDirectionParamName = new JOAJTextField(10);
    mNewDirectionParamName.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line1b.add(mNewDirectionParamName);
    line1b.add(new JLabel("(degrees)"));
    middlePanel.add(line1b);

    // construct the pending calc panel

    // construct the dialog
    upperPanel.add("Center", paramPanel);
    upperPanel.add("South", middlePanel);
    mainPanel.add("Center", new TenPixelBorder(upperPanel, 10, 10, 10, 10));

    // lower panel
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
  }

  private void maintainXList() {
  }

  public void valueChanged(ListSelectionEvent evt) {
    if (evt.getSource() == mUParamJList) {
      if (mIgnoreVC == true) {
        mIgnoreVC = false;
        return;
      }
      // get the x param
      mSelUParam = mUParamJList.getSelectedIndex();
      if (mSelUParam < 0) {
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

    }
    else if (evt.getSource() == mVParamJList) {
      if (mIgnoreVC == true) {
        mIgnoreVC = false;
        return;
      }

      // get the y param
      mSelVParam = mVParamJList.getSelectedIndex();
      if (mSelVParam < 0) {
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
    }
  }

  public void itemStateChanged(ItemEvent evt) {
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      timer.cancel();      
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      // transfer pending calculations to the fileviewer for processing
      
    	String speedName = mNewSpeedParamName.getText();
    	String speedUnits = mNewSpeedParamUnits.getText();
    	String direcName = mNewDirectionParamName.getText();
    	
    	String uComp = (String)mUParamJList.getSelectedValue();
    	String vComp = (String)mVParamJList.getSelectedValue();

      // create a new object
    	SpeedCalculationSpecification speedSpec = new SpeedCalculationSpecification(speedName, speedUnits, uComp, vComp);
    	DirectionCalculationSpecification direcSpec = new DirectionCalculationSpecification(direcName, uComp, vComp);
    	
    	Calculation speed = new Calculation(speedName, speedSpec, JOAConstants.STN_CALC_TYPE, speedUnits);
    	Calculation direc = new Calculation(direcName, direcSpec, JOAConstants.STN_CALC_TYPE, "degrees");
    	
      try {
      	speed.writeToLog("New Speed Calculation");
      }
      catch (Exception ex) {}
      
      try {
      	direc.writeToLog("New Direction Calculation");
      }
      catch (Exception ex) {}
    	
    	mFileViewer.addCalculation(speed);
    	mFileViewer.addCalculation(direc);
      mFileViewer.doCalcs();
      timer.cancel();      
      this.dispose();
    }
  }

  public void generateParamName() {
  }

  public void maintainButtons() {

    if ((mUParamJList.getSelectedIndex() >= 0) &&
        (mVParamJList.getSelectedIndex() >= 0) &&
        mNewSpeedParamName.getText().length() > 0 &&
        mNewDirectionParamName.getText().length() > 0) {
    	mOKBtn.setEnabled(true);
    }
    else {
    	mOKBtn.setEnabled(false);
    }

  }

  public void changedUpdate(DocumentEvent evt) {
    generateParamName();
  }

  public void insertUpdate(DocumentEvent evt) {
    generateParamName();
  }

  public void removeUpdate(DocumentEvent evt) {
    generateParamName();
  }

}

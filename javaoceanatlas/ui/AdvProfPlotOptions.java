/*
 * $Id: AdvProfPlotOptions.java,v 1.2 2005/06/17 18:08:51 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.specifications.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class AdvProfPlotOptions extends JPanel implements DocumentListener, ActionListener, ChangeListener {
  protected JOAJTextField yMin, yMax, yInc;
  protected JSpinner yTics;
  protected JSpinner mLineWidth;
  protected ProfilePlotSpecification mPlotSpec;
  protected int mSelXParam, mSelYParam;
  boolean mChangedFlag = false;
  protected double mYDataMin, mYDataMax;

  public AdvProfPlotOptions(FileViewer fv, ProfilePlotSpecification plotSpec) {
    mPlotSpec = plotSpec;
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
    this.setLayout(new BorderLayout(5, 5));

    // container for the axes stuff
    JPanel stuffCont = new JPanel();
    stuffCont.setLayout(new GridLayout(1, 2, 5, 5));

    // axis container
    JPanel yAxis = new JPanel();
    yAxis.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 2));
    TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kYAxis"));
    if (JOAConstants.ISMAC) {
      ////tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    yAxis.setBorder(tb);

    // y axis detail
    JPanel line5 = new JPanel();
    line5.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line5.add(new JOAJLabel(b.getString("kMinimum")));
    yMin = new JOAJTextField(6);
    yMin.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getWinYPlotMin()), 3, false));
    yMin.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    yMin.getDocument().addDocumentListener(this);
    line5.add(yMin);

    JPanel line6 = new JPanel();
    line6.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line6.add(new JOAJLabel(b.getString("kMaximum")));
    yMax = new JOAJTextField(6);
    yMax.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getWinYPlotMax()), 3, false));
    yMax.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    yMax.getDocument().addDocumentListener(this);
    line6.add(yMax);

    JPanel line7 = new JPanel();
    line7.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line7.add(new JOAJLabel(b.getString("kIncrement")));
    yInc = new JOAJTextField(6);
    yInc.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getYInc()), 3, false));
    yInc.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    yInc.getDocument().addDocumentListener(this);
    line7.add(yInc);

    JPanel line8 = new JPanel();
    line8.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line8.add(new JOAJLabel(b.getString("kNoMinorTicks")));

    SpinnerNumberModel model = new SpinnerNumberModel(mPlotSpec.getYTics(), 0, 100, 1);
    yTics = new JSpinner(model);
    line8.add(yTics);

    yAxis.add(line5);
    yAxis.add(line6);
    yAxis.add(line7);
    yAxis.add(line8);
    stuffCont.add(yAxis);

    // other options
    JPanel otherCont = new JPanel();
    otherCont.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 2));
    tb = BorderFactory.createTitledBorder(b.getString("kOther"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    otherCont.setBorder(tb);

    // other detail
    // amplitude
    /*JPanel line1 = new JPanel();
          line1.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
          line1.add(new JOAJLabel(b.getString("kAmplitude")));
          mAmplitude = new JOAJTextField(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.mAmplitude), 2, false), 4);
          mAmplitude.getDocument().addDocumentListener(this);
          line1.add(mAmplitude);*/

    // linewidth
    JPanel line3 = new JPanel();
    line3.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 3));
    line3.add(new JOAJLabel(b.getString("kLineWidth") + " " + b.getString("kPixels") + ":"));
    SpinnerNumberModel model3 = new SpinnerNumberModel(mPlotSpec.getLineWidth(), 1, 100, 1);
    mLineWidth = new JSpinner(model3);
    line3.add(mLineWidth);
    mLineWidth.addChangeListener(this);

    //otherCont.add(line1);
    otherCont.add(line3);
    otherCont.add(new JOAJLabel("       "));
    otherCont.add(new JOAJLabel("       "));
    stuffCont.add(otherCont);

    this.add("Center", stuffCont);

    // add the use max/ min button
    JOAJButton mUseDataMaxMin = new JOAJButton(b.getString("kUseDataMaxMin"));
    mUseDataMaxMin.setActionCommand("minmax");
    mUseDataMaxMin.addActionListener(this);
    JPanel dlgBtnsPanel = new JPanel();
    dlgBtnsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 3));
    dlgBtnsPanel.add(mUseDataMaxMin);
    this.add("South", dlgBtnsPanel);
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("minmax")) {
      // set the ranges to the max min for that parameter
      yMin.setText(JOAFormulas.formatDouble(String.valueOf(mYDataMin), 3, false));
      yMax.setText(JOAFormulas.formatDouble(String.valueOf(mYDataMax), 3, false));
      yInc.setText(JOAFormulas.formatDouble(String.valueOf((mYDataMax - mYDataMin) / 5.0), 3, false));
    }
  }

  public void setParameters(int selXParam, int selYParam) {
    mSelXParam = selXParam;
    mSelYParam = selYParam;
    mChangedFlag = false;
  }

  public void setValues(double inyMin, double inyMax, double inyInc, int inyTics, double yDMin, double yDMax) {
    yMin.setText(JOAFormulas.formatDouble(String.valueOf(inyMin), 3, false));
    yMax.setText(JOAFormulas.formatDouble(String.valueOf(inyMax), 3, false));
    yInc.setText(JOAFormulas.formatDouble(String.valueOf(inyInc), 3, false));
    yTics.setValue(new Integer(inyTics));
    mYDataMin = yDMin;
    mYDataMax = yDMax;
  }

  //public void setAmplitude(double inAmp) {
  //	mAmplitude.setText(JOAFormulas.formatDouble(String.valueOf(inAmp), 2, false));
  //}

  public void setLineWidth(int inLineWidth) {
    mLineWidth.setValue(new Integer(inLineWidth));
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

  public double getYMin() throws NumberFormatException {
    double out;
    try {
      out = Double.valueOf(yMin.getText()).doubleValue();
    }
    catch (NumberFormatException ex) {
      throw ex;
    }
    return out;
  }

  public double getYMax() throws NumberFormatException {
    double out;
    try {
      out = Double.valueOf(yMax.getText()).doubleValue();
    }
    catch (NumberFormatException ex) {
      throw ex;
    }
    return out;
  }

  public double getYInc() throws NumberFormatException {
    double out;
    try {
      out = Double.valueOf(yInc.getText()).doubleValue();
    }
    catch (NumberFormatException ex) {
      throw ex;
    }
    if (out == 0.0) {
      out = 1.0;
    }
    return out;
  }

  public int getYTics() throws NumberFormatException {
    int out = ((Integer)yTics.getValue()).intValue();
    return out;
  }

  public int getLineWidth() throws NumberFormatException {
    int out = ((Integer)mLineWidth.getValue()).intValue();
    return out;
  }

  /*public double getAmplitude() throws NumberFormatException {
   double out;
   try {
    out = Double.valueOf(mAmplitude.getText()).doubleValue();
   }
   catch (NumberFormatException ex) {
    throw ex;
   }
   return out;
    }*/

  public void stateChanged(ChangeEvent evt) {
  }

}

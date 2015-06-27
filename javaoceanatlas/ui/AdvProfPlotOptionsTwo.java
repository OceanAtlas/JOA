/*
 * $Id: AdvProfPlotOptionsTwo.java,v 1.4 2005/06/28 14:23:14 oz Exp $
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
public class AdvProfPlotOptionsTwo extends JPanel implements ActionListener, ChangeListener, DocumentListener {
  protected JOAJTextField yMin, yMax, yInc;
  protected ProfilePlotSpecification mPlotSpec;
  protected int mSelXParam, mSelYParam;
  boolean mChangedFlag = false;
  protected double mYDataMin, mYDataMax;
  protected JSpinner mAmplitude;
  protected JSpinner mLineSpacing;
  protected JSpinner mLineWidth;
  protected JSpinner mSectionOrigin, yTics;
  protected DialogClient mClient;

  public AdvProfPlotOptionsTwo(FileViewer fv, ProfilePlotSpecification plotSpec, DialogClient client) {
    mPlotSpec = plotSpec;
    mClient = client;
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
    this.setLayout(new BorderLayout(5, 5));

    // container for the axes stuff
    JPanel stuffCont = new JPanel();
    stuffCont.setLayout(new GridLayout(1, 2, 5, 5));

    // axis container
    JPanel yAxis = new JPanel();
    yAxis.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.TOP, 2));
    TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kYAxis"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }

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
    JPanel subY = new JPanel();
    subY.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    subY.setBorder(tb);
    subY.add(yAxis);
    stuffCont.add(subY);

    // other options
    JPanel otherCont = new JPanel();
    otherCont.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 2));
    tb = BorderFactory.createTitledBorder(b.getString("kOther"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    otherCont.setBorder(tb);

    // other detail
    // offset
    JPanel other1 = new JPanel();
    other1.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 3));
    other1.add(new JOAJLabel(b.getString("kTranslateOrigin")));
    SpinnerNumberModel model2 = new SpinnerNumberModel(mPlotSpec.getSecOrigin(), -500, 500, 5);
    mSectionOrigin = new JSpinner(model2);
    other1.add(mSectionOrigin);
    other1.add(new JOAJLabel("(pixels)"));
    mSectionOrigin.addChangeListener(this);
    otherCont.add(other1);

    // amplitude
    JPanel other2 = new JPanel();
    other2.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 3));
    other2.add(new JOAJLabel(b.getString("kAmplitude")));
    SpinnerNumberModel model3 = new SpinnerNumberModel(mPlotSpec.getAmplitude() * 100, 0, 100, 1);
    mAmplitude = new JSpinner(model3);
    other2.add(mAmplitude);
    other2.add(new JOAJLabel("(%)"));
    mAmplitude.addChangeListener(this);
    otherCont.add(other2);

    // line spacing
    JPanel other3 = new JPanel();
    other3.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 3));
    other3.add(new JOAJLabel(b.getString("kLineSpacing")));
    double offset = 1.0;
    String units = null;
    double mult = 0;
    double min = 1.0;
    double start = mPlotSpec.getTraceOffset();
    if (mPlotSpec.getSectionType() == JOAConstants.PROFSEQUENCE) {
      offset = 1.0;
      units = b.getString("kPixels");
      mult = 1.0;
    }
    else if (mPlotSpec.getSectionType() == JOAConstants.PROFDISTANCE) {
      // make the offset 1% of the
      offset = mPlotSpec.getTraceOffset() / 10.0;
      units = b.getString("kPixPerKM");
      mult = 1.0;
      min = 0.0;
    }
    else if (mPlotSpec.getSectionType() == JOAConstants.PROFTIME) {
      // start time in days (since offsets are stored in seconds)
      // offset is really the increment
      offset = mPlotSpec.getTraceOffset() / 10.0;
      units = b.getString("kPixPerDay");
      mult = 1.0;
      min = 0.0;
    }
    if (start < 1.0) {
      start = 1.0;
    }
    SpinnerNumberModel model4 = new SpinnerNumberModel(start, min, 1000.0, offset);
    mLineSpacing = new JSpinner(model4);
    other3.add(mLineSpacing);
    other3.add(new JOAJLabel(units));
    mLineSpacing.addChangeListener(this);
    otherCont.add(other3);

    // linewidth
    JPanel other4 = new JPanel();
    other4.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 3));
    other4.add(new JOAJLabel(b.getString("kLineWidthColon")));
    SpinnerNumberModel model5 = new SpinnerNumberModel(mPlotSpec.getLineWidth(), 1, 100, 1);
    mLineWidth = new JSpinner(model5);
    other4.add(mLineWidth);
    other4.add(new JOAJLabel(b.getString("kPixels")));
    mLineWidth.addChangeListener(this);
    otherCont.add(other4);

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

  public int getLineWidth() {
    return ((Integer)mLineWidth.getValue()).intValue();
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

  public double getAmplitude() {
    return ((Double)mAmplitude.getValue()).doubleValue() / 100.0;
  }

  public int getTranslation() {
    return ((Integer)mSectionOrigin.getValue()).intValue();
  }

  public double getLineSpacing() {
    double val = ((Double)mLineSpacing.getValue()).doubleValue();
    return val;
  }

  public void stateChanged(ChangeEvent evt) {
    mChangedFlag = true;
    mClient.dialogApply(null);
  }
}

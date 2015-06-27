/*
 * $Id: AdvXYPlotOptions.java,v 1.3 2005/06/17 18:08:51 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import javax.swing.border.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.specifications.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import gov.noaa.pmel.swing.*;

@SuppressWarnings("serial")
public class AdvXYPlotOptions extends JPanel implements DocumentListener, ActionListener, ButtonMaintainer,
    ItemListener {
  protected JOAJTextField xMin = new JOAJTextField("");
  protected JOAJTextField xMax = new JOAJTextField("");
  protected JOAJTextField xInc = new JOAJTextField("");
  protected JSpinner xTics;
  protected JSpinner symbolSize;
  protected JOAJTextField yMin, yMax, yInc;
  protected JSpinner yTics;
  protected XYPlotSpecification mPlotSpec;
  protected int[] mSelXParam = new int[10];
  protected int mSelYParam;
  boolean mChangedFlag = false;
  protected double[] mXDataMin = new double[10], mXDataMax = new double[10];
  protected double mYDataMin, mYDataMax;
  protected int mNumXAxes;
  protected Swatch axisSwatch = new Swatch(Color.black);
  protected JOAJComboBox mSymbolPopup = new JOAJComboBox();
  protected Icon[] symbolData = null;
  protected ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
  JOAJLabel minLabel = new JOAJLabel(b.getString("kMinimum"));
  JOAJLabel maxLabel = new JOAJLabel(b.getString("kMaximum"));
  JOAJLabel incLabel = new JOAJLabel(b.getString("kIncrement"));
  JOAJLabel minorTicLabel = new JOAJLabel(b.getString("kNoMinorTicks"));
  JOAJLabel connectLabel = new JOAJLabel(b.getString("kConnectLineColor"));
  JOAJLabel symbolLabel = new JOAJLabel(b.getString("kSymbol"));
  JOAJLabel sizeLabel = new JOAJLabel(b.getString("kSize"));
  String yAxisLabel = new String(b.getString("kYAxis"));
  protected SmallIconButton mMoveUpButton = null;
  protected SmallIconButton mMoveDownButton = null;
  protected JOAJList mParamList = null;
	private Timer timer = new Timer();
  protected double[] xMinVals = new double[10];
  protected double[] xMaxVals = new double[10];
  protected double[] xIncVals = new double[10];
  protected int[] xTicVals = new int[10];
  protected int[] mSymbols = new int[10];
  protected int[] mSymbolSizes = new int[10];
  protected Color[] mXConnectColors = new Color[10];
  protected String[] mParamNames = new String[10];
  protected JOAJComboBox mAxisPopup = new JOAJComboBox();
  protected int mCurrXAxis = 0;
  protected int mOldXAxis = 0;
  protected TitledBorder yAxisTitledBorder = null;
  protected String mYParamName = null;
  protected double mYMin = 0.0;
  protected double mYMax = 0.0;
  protected double mYInc = 0.0;
  protected int mYTics = 0;

  public AdvXYPlotOptions(FileViewer fv, XYPlotSpecification plotSpec) {
    mPlotSpec = plotSpec;
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    symbolData = new Icon[] {new ImageIcon(getClass().getResource("images/sym_square.gif")),
        new ImageIcon(getClass().getResource("images/sym_squarefilled.gif")),
        new ImageIcon(getClass().getResource("images/sym_circle.gif")),
        new ImageIcon(getClass().getResource("images/sym_circlefilled.gif")),
        new ImageIcon(getClass().getResource("images/sym_diamond.gif")),
        new ImageIcon(getClass().getResource("images/sym_diamondfilled.gif")),
        new ImageIcon(getClass().getResource("images/sym_triangle.gif")),
        new ImageIcon(getClass().getResource("images/sym_trianglefilled.gif")),
        new ImageIcon(getClass().getResource("images/sym_cross1.gif")),
        new ImageIcon(getClass().getResource("images/sym_cross2.gif"))
    };

    // create the symbol popup menu
    for (int ic = 0; ic < symbolData.length; ic++) {
      mSymbolPopup.addItem(symbolData[ic]);
    }

    this.setLayout(new BorderLayout(5, 5));

    // container for the axes stuff
    JPanel axesCont = new JPanel();
    axesCont.setLayout(new GridLayout(2, 1, 5, 5));

    JPanel xAxisCont = new JPanel();
    xAxisCont.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kXAxis"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    xAxisCont.setBorder(tb);

    // x axis container
    JPanel xAxis = new JPanel();
    xAxis.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 2));

    // x axis #1 detail
    JPanel line1 = new JPanel();
    line1.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line1.add(minLabel);
    xMin = new JOAJTextField(6);
    xMin.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getWinXPlotMin(0)), 3, false));
    xMin.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    xMin.getDocument().addDocumentListener(this);
    line1.add(xMin);

    JPanel line2 = new JPanel();
    line2.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line2.add(maxLabel);
    xMax = new JOAJTextField(6);
    xMax.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getWinXPlotMax(0)), 3, false));
    xMax.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    xMax.getDocument().addDocumentListener(this);
    line2.add(xMax);

    JPanel line3 = new JPanel();
    line3.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line3.add(incLabel);
    xInc = new JOAJTextField(6);
    xInc.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getXInc(0)), 3, false));
    xInc.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    xInc.getDocument().addDocumentListener(this);
    line3.add(xInc);

    JPanel line4 = new JPanel();
    line4.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line4.add(minorTicLabel);

    SpinnerNumberModel model = new SpinnerNumberModel(mPlotSpec.getXTics(0), 0, 100, 1);
    xTics = new JSpinner(model);
    line4.add(xTics);

    JPanel axisPopupHolder = new JPanel();
    axisPopupHolder.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    axisPopupHolder.add(new JOAJLabel(b.getString("kXAxes")));
    axisPopupHolder.add(mAxisPopup);
    mAxisPopup.addItemListener(this);

    xAxis.add(axisPopupHolder);
    xAxis.add(line1);
    xAxis.add(line2);
    xAxis.add(line3);

    JPanel xAxisss = new JPanel();
    xAxisss.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 2));

    JPanel line1ss = new JPanel();
    line1ss.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line1ss.add(axisSwatch);
    line1ss.add(connectLabel);

    JPanel line2ss = new JPanel();
    line2ss.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
    line2ss.add(symbolLabel);
    line2ss.add(mSymbolPopup);
    //mSymbolPopup.addItemListener(this);
    line2ss.add(sizeLabel);

    SpinnerNumberModel model2 = new SpinnerNumberModel(4, 1, 100, 1);
    symbolSize = new JSpinner(model2);

    line2ss.add(symbolSize);

    xAxisss.add(line4);
    xAxisss.add(line1ss);
    xAxisss.add(line2ss);

    // y axis container
    JPanel yAxisCont = new JPanel();
    yAxisCont.setLayout(new GridLayout(1, 2, 5, 5));
    JPanel yAxis = new JPanel();
    yAxis.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 2));
    yAxisTitledBorder = BorderFactory.createTitledBorder(b.getString("kYAxis"));
    if (JOAConstants.ISMAC) {
      //yAxisTitledBorder.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    yAxis.setBorder(yAxisTitledBorder);

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

    SpinnerNumberModel model3 = new SpinnerNumberModel(mPlotSpec.getYTics(), 0, 100, 1);
    yTics = new JSpinner(model3);
    line8.add(yTics);

    yAxis.add(line5);
    yAxis.add(line6);
    yAxis.add(line7);
    yAxis.add(line8);

    // variable order stuff goes in yAxisCont
    JPanel paramSelPanel = new JPanel();
    paramSelPanel.setLayout(new BorderLayout(5, 5));
    tb = BorderFactory.createTitledBorder(b.getString("kParameterOrder"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    paramSelPanel.setBorder(tb);

    JPanel moveCont = new JPanel();
    moveCont.setLayout(new GridLayout(3, 1, 0, 5));
    mMoveUpButton = new SmallIconButton(new ImageIcon(getClass().getResource("images/moveup.gif")));
    moveCont.add(mMoveUpButton);
    mMoveDownButton = new SmallIconButton(new ImageIcon(getClass().getResource("images/movedown.gif")));
    moveCont.add(mMoveDownButton);
    mMoveUpButton.addActionListener(this);
    mMoveDownButton.addActionListener(this);
    mMoveUpButton.setActionCommand("moveup");
    mMoveDownButton.setActionCommand("movedown");
    paramSelPanel.add("East", moveCont);
    mParamList = new JOAJList();
    mParamList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mParamList.setPrototypeCellValue("Temperature");
    mParamList.setVisibleRowCount(1);
    JScrollPane listScroller = new JScrollPane(mParamList);
    paramSelPanel.add(new TenPixelBorder(listScroller, 0, 5, 0, 0), "Center");
    
    mMoveUpButton.setToolTipText("Moves the selected parameter higher in the plot order on multiple x-axis plots");
    mMoveDownButton.setToolTipText("Moves the selected parameter lower in the plot order on multiple x-axis plots");

    xAxisCont.add(xAxis);
    xAxisCont.add(xAxisss);
    yAxisCont.add(yAxis);
    yAxisCont.add(paramSelPanel);

    axesCont.add(xAxisCont);
    axesCont.add(yAxisCont);

    JPanel axesContCont = new JPanel();
    axesContCont.setLayout(new BorderLayout(5, 5));
    axesContCont.add("North", axesCont);
    this.add("Center", axesContCont);

    // add the use max/ min button
    JOAJButton mUseDataMaxMin = new JOAJButton(b.getString("kUseDataMaxMin"));
    mUseDataMaxMin.setActionCommand("minmax");
    mUseDataMaxMin.addActionListener(this);
    JPanel dlgBtnsPanel = new JPanel();
    dlgBtnsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 3));
    dlgBtnsPanel.add(mUseDataMaxMin);
    this.add("South", dlgBtnsPanel);

    xMin.setText("");
    xMax.setText("");
    xInc.setText("");
    yMin.setText("");
    yMax.setText("");
    yInc.setText("");

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
	
	public void closeMe() {
		timer.cancel();
	}

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("minmax")) {
      // set the ranges to the max min for that parameter
      for (int i = 0; i < mNumXAxes; i++) {
        xMin.setText(JOAFormulas.formatDouble(String.valueOf(mXDataMin[mCurrXAxis]), 3, false));
        xMax.setText(JOAFormulas.formatDouble(String.valueOf(mXDataMax[mCurrXAxis]), 3, false));
        xInc.setText(JOAFormulas.formatDouble(String.valueOf((mXDataMax[mCurrXAxis] - mXDataMin[mCurrXAxis]) / 5.0), 3, false));
      }
      yMin.setText(JOAFormulas.formatDouble(String.valueOf(mYDataMin), 3, false));
      yMax.setText(JOAFormulas.formatDouble(String.valueOf(mYDataMax), 3, false));
      yInc.setText(JOAFormulas.formatDouble(String.valueOf((mYDataMax - mYDataMin) / 5.0), 3, false));
    }
    else if (cmd.equals("moveup")) {
      mMoveUpButton.setSelected(false);
      moveSelectionUp();
    }
    else if (cmd.equals("movedown")) {
      mMoveDownButton.setSelected(false);
      moveSelectionDown();
    }
  }

  public void itemStateChanged(ItemEvent evt) {
    if (evt.getSource() instanceof JOAJComboBox) {
      JOAJComboBox cb = (JOAJComboBox)evt.getSource();
      if (cb == mAxisPopup && evt.getStateChange() == ItemEvent.SELECTED) {
        mOldXAxis = mCurrXAxis;
        mCurrXAxis = cb.getSelectedIndex();
        if (mCurrXAxis >= 0) {
          // save the current settings
          upDateSettings();

          // setup up the display for selected axis
          xMin.setText(JOAFormulas.formatDouble(String.valueOf(xMinVals[mCurrXAxis]), 3, false));
          xMax.setText(JOAFormulas.formatDouble(String.valueOf(xMaxVals[mCurrXAxis]), 3, false));
          xInc.setText(JOAFormulas.formatDouble(String.valueOf(xIncVals[mCurrXAxis]), 3, false));
          xTics.setValue(new Integer(xTicVals[mCurrXAxis]));
          mSymbolPopup.setSelectedIndex(mSymbols[mCurrXAxis] - 1);
          if (!cb.isShowing()) {
            axisSwatch.setColorSilent(mXConnectColors[mCurrXAxis]);
          }
          else {
            axisSwatch.setColor(mXConnectColors[mCurrXAxis]);
          }
          symbolSize.setValue(new Integer(mSymbolSizes[mCurrXAxis]));
        }
      }
    }
  }

  public void moveSelectionDown() {
    upDateCurrSettings();
    int selectedIndex = mParamList.getSelectedIndex();

    if (selectedIndex < mNumXAxes - 1) {
      //swap index and index + 1
      double temp = xMinVals[selectedIndex];
      xMinVals[selectedIndex] = xMinVals[selectedIndex + 1];
      xMinVals[selectedIndex + 1] = temp;
      temp = xMaxVals[selectedIndex];
      xMaxVals[selectedIndex] = xMaxVals[selectedIndex + 1];
      xMaxVals[selectedIndex + 1] = temp;
      temp = xIncVals[selectedIndex];
      xIncVals[selectedIndex] = xIncVals[selectedIndex + 1];
      xIncVals[selectedIndex + 1] = temp;
      temp = mXDataMin[selectedIndex];
      mXDataMin[selectedIndex] = mXDataMin[selectedIndex + 1];
      mXDataMin[selectedIndex + 1] = temp;
      temp = mXDataMax[selectedIndex];
      mXDataMax[selectedIndex] = mXDataMax[selectedIndex + 1];
      mXDataMax[selectedIndex + 1] = temp;
      int itemp = xTicVals[selectedIndex];
      xTicVals[selectedIndex] = xTicVals[selectedIndex + 1];
      xTicVals[selectedIndex + 1] = itemp;
      String stemp = mParamNames[selectedIndex];
      mParamNames[selectedIndex] = mParamNames[selectedIndex + 1];
      mParamNames[selectedIndex + 1] = stemp;
      itemp = mSelXParam[selectedIndex];
      mSelXParam[selectedIndex] = mSelXParam[selectedIndex + 1];
      mSelXParam[selectedIndex + 1] = itemp;
      itemp = mSymbols[selectedIndex];
      mSymbols[selectedIndex] = mSymbols[selectedIndex + 1];
      mSymbols[selectedIndex + 1] = itemp;
      itemp = mSymbolSizes[selectedIndex];
      mSymbolSizes[selectedIndex] = mSymbolSizes[selectedIndex + 1];
      mSymbolSizes[selectedIndex + 1] = itemp;
      Color cTemp = new Color(mXConnectColors[selectedIndex].getRed(), mXConnectColors[selectedIndex].getGreen(),
                              mXConnectColors[selectedIndex].getBlue());
      mXConnectColors[selectedIndex] = mXConnectColors[selectedIndex + 1];
      mXConnectColors[selectedIndex + 1] = cTemp;
    }

    // make copies of the arrays
    int numxaxes = mNumXAxes;
    int selYParam = mSelYParam;
    String yParamName = new String(mYParamName);
    int[] selXParam = new int[10];
    double[] xDataMin = new double[10];
    double[] xDataMax = new double[10];
    double[] xminvals = new double[10];
    double[] xmaxvals = new double[10];
    double[] xincvals = new double[10];
    int[] xticVals = new int[10];
    int[] symbols = new int[10];
    int[] symbolSizes = new int[10];
    Color[] xConnectColors = new Color[10];
    String[] paramnames = new String[10];
    double ymin = mYMin;
    double ymax = mYMax;
    double yinc = mYInc;
    int ytics = mYTics;
    double ydatamin = mYDataMin;
    double ydatamax = mYDataMax;

    for (int i = 0; i < numxaxes; i++) {
      paramnames[i] = new String(mParamNames[i]);
      selXParam[i] = mSelXParam[i];
      xminvals[i] = xMinVals[i];
      xmaxvals[i] = xMaxVals[i];
      xincvals[i] = xIncVals[i];
      xDataMin[i] = mXDataMin[i];
      xDataMax[i] = mXDataMax[i];
      xticVals[i] = xTicVals[i];
      xConnectColors[i] = new Color(mXConnectColors[i].getRed(), mXConnectColors[i].getGreen(),
                                    mXConnectColors[i].getBlue());
      symbols[i] = mSymbols[i];
      symbolSizes[i] = mSymbolSizes[i];
    }

    this.setParameters(numxaxes, selXParam, paramnames, selYParam, yParamName);
    this.setValues(numxaxes, xminvals, xmaxvals, xincvals, ymin, ymax, yinc, xticVals, ytics, xDataMin, xDataMax,
                   ydatamin, ydatamax, xConnectColors, symbols, symbolSizes);

    // redo the list highlight
    mParamList.setSelectedIndex(selectedIndex + 1);
    mCurrXAxis = 0;
    mOldXAxis = 0;
    axisSwatch.invalidate();
    axisSwatch.validate();
    this.setSize(this.getWidth() + 1, this.getHeight());
    this.setSize(this.getWidth() - 1, this.getHeight());
  }

  public void moveSelectionUp() {
    upDateCurrSettings();
    int selectedIndex = mParamList.getSelectedIndex();

    if (selectedIndex > 0) {
      //swap index and index + 1
      double temp = xMinVals[selectedIndex];
      xMinVals[selectedIndex] = xMinVals[selectedIndex - 1];
      xMinVals[selectedIndex - 1] = temp;
      temp = xMaxVals[selectedIndex];
      xMaxVals[selectedIndex] = xMaxVals[selectedIndex - 1];
      xMaxVals[selectedIndex - 1] = temp;
      temp = xIncVals[selectedIndex];
      xIncVals[selectedIndex] = xIncVals[selectedIndex - 1];
      xIncVals[selectedIndex - 1] = temp;
      temp = mXDataMin[selectedIndex];
      mXDataMin[selectedIndex] = mXDataMin[selectedIndex - 1];
      mXDataMin[selectedIndex - 1] = temp;
      temp = mXDataMax[selectedIndex];
      mXDataMax[selectedIndex] = mXDataMax[selectedIndex - 1];
      mXDataMax[selectedIndex - 1] = temp;
      int itemp = xTicVals[selectedIndex];
      xTicVals[selectedIndex] = xTicVals[selectedIndex - 1];
      xTicVals[selectedIndex - 1] = itemp;
      String stemp = mParamNames[selectedIndex];
      mParamNames[selectedIndex] = mParamNames[selectedIndex - 1];
      mParamNames[selectedIndex - 1] = stemp;
      itemp = mSelXParam[selectedIndex];
      mSelXParam[selectedIndex] = mSelXParam[selectedIndex - 1];
      mSelXParam[selectedIndex - 1] = itemp;
      itemp = mSymbols[selectedIndex];
      mSymbols[selectedIndex] = mSymbols[selectedIndex - 1];
      mSymbols[selectedIndex - 1] = itemp;
      itemp = mSymbolSizes[selectedIndex];
      mSymbolSizes[selectedIndex] = mSymbolSizes[selectedIndex - 1];
      mSymbolSizes[selectedIndex - 1] = itemp;
      Color cTemp = new Color(mXConnectColors[selectedIndex].getRed(), mXConnectColors[selectedIndex].getGreen(),
                              mXConnectColors[selectedIndex].getBlue());
      mXConnectColors[selectedIndex] = mXConnectColors[selectedIndex - 1];
      mXConnectColors[selectedIndex - 1] = cTemp;
    }

    // make copies of the arrays
    int numxaxes = mNumXAxes;
    int selYParam = mSelYParam;
    String yParamName = new String(mYParamName);
    int[] selXParam = new int[10];
    double[] xDataMin = new double[10];
    double[] xDataMax = new double[10];
    double[] xminvals = new double[10];
    double[] xmaxvals = new double[10];
    double[] xincvals = new double[10];
    int[] xticVals = new int[10];
    int[] symbols = new int[10];
    int[] symbolSizes = new int[10];
    Color[] xConnectColors = new Color[10];
    String[] paramnames = new String[10];
    double ymin = mYMin;
    double ymax = mYMax;
    double yinc = mYInc;
    int ytics = mYTics;
    double ydatamin = mYDataMin;
    double ydatamax = mYDataMax;

    for (int i = 0; i < numxaxes; i++) {
      paramnames[i] = new String(mParamNames[i]);
      selXParam[i] = mSelXParam[i];
      xminvals[i] = xMinVals[i];
      xmaxvals[i] = xMaxVals[i];
      xincvals[i] = xIncVals[i];
      xDataMin[i] = mXDataMin[i];
      xDataMax[i] = mXDataMax[i];
      xticVals[i] = xTicVals[i];
      xConnectColors[i] = new Color(mXConnectColors[i].getRed(), mXConnectColors[i].getGreen(),
                                    mXConnectColors[i].getBlue());
      symbols[i] = mSymbols[i];
      symbolSizes[i] = mSymbolSizes[i];
    }

    this.setParameters(numxaxes, selXParam, paramnames, selYParam, yParamName);
    this.setValues(numxaxes, xminvals, xmaxvals, xincvals, ymin, ymax, yinc, xticVals, ytics, xDataMin, xDataMax,
                   ydatamin, ydatamax, xConnectColors, symbols, symbolSizes);

    // redo the list highlight
    mParamList.setSelectedIndex(selectedIndex - 1); ;
    mCurrXAxis = 0;
    mOldXAxis = 0;
    axisSwatch.invalidate();
    axisSwatch.validate();
    this.setSize(this.getWidth() + 1, this.getHeight());
    this.setSize(this.getWidth() - 1, this.getHeight());
  }

  public void setParameters(int numXAxes, int[] selXParam, String[] xParamNames, int selYParam, String yParamName) {
    mNumXAxes = numXAxes;
    Vector<String> listData = new Vector<String>();
    if (mAxisPopup.getItemCount() > 0) {
      mAxisPopup.removeAllItems();
    }
    for (int i = 0; i < numXAxes; i++) {
      mSelXParam[i] = selXParam[i];
      listData.addElement(xParamNames[i]);
      mParamNames[i] = null;
      mParamNames[i] = new String(xParamNames[i]);
      mAxisPopup.addItem(mParamNames[i]);
    }
    mSelYParam = selYParam;
    mYParamName = new String(yParamName);
    yAxisTitledBorder.setTitle(yParamName);
    mChangedFlag = false;

    mParamList.setListData(listData);
    mParamList.invalidate();
  }

  public void setValues(int numXAxes, double[] inxMin, double[] inxMax, double[] inxInc, double inyMin, double inyMax,
                        double inyInc, int[] inxTics, int inyTics, double[] xDMin, double[] xDMax, double yDMin,
                        double yDMax, Color[] connectColors, int[] symbols, int[] symbolSizes) {

    // reset the UI
    xMin.setText("");
    xMax.setText("");
    xInc.setText("");
    mSymbolPopup.setSelectedIndex(0);
    axisSwatch.setColorSilent(Color.black);
    symbolSize.setValue(new Integer(4));
    yMin.setText("");
    yMax.setText("");
    yInc.setText("");

    // store the new axis information
    mNumXAxes = numXAxes;
    for (int i = 0; i < numXAxes; i++) {
      xMinVals[i] = inxMin[i];
      xMaxVals[i] = inxMax[i];
      xIncVals[i] = inxInc[i];
      xTicVals[i] = inxTics[i];
      mSymbols[i] = symbols[i];
      mSymbolSizes[i] = symbolSizes[i];
      mXDataMin[i] = xDMin[i];
      mXDataMax[i] = xDMax[i];
      mXConnectColors[i] = connectColors[i];
    }
    mYMin = inyMin;
    mYMax = inyMax;
    mYInc = inyInc;
    mYTics = inyTics;

    // set the UI to the first XAxis
    xMin.setText(JOAFormulas.formatDouble(String.valueOf(xMinVals[0]), 3, false));
    xMax.setText(JOAFormulas.formatDouble(String.valueOf(xMaxVals[0]), 3, false));
    xInc.setText(JOAFormulas.formatDouble(String.valueOf(xIncVals[0]), 3, false));
    xTics.setValue(new Integer(xTicVals[0]));
    mSymbolPopup.setSelectedIndex(mSymbols[0] - 1);
    axisSwatch.setColorSilent(mXConnectColors[0]);
    symbolSize.setValue(new Integer(mSymbolSizes[0]));

    yMin.setText(JOAFormulas.formatDouble(String.valueOf(mYMin), 3, false));
    yMax.setText(JOAFormulas.formatDouble(String.valueOf(mYMax), 3, false));
    yInc.setText(JOAFormulas.formatDouble(String.valueOf(mYInc), 3, false));
    yTics.setValue(new Integer(mYTics));
    mYDataMin = yDMin;
    mYDataMax = yDMax;
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

  public void upDateSettings() {
    if (mOldXAxis < 0) {
      return;
    }
    // called when a new parameter is chosen from the popup
    try {
      xMinVals[mOldXAxis] = Double.valueOf(xMin.getText()).doubleValue();
    }
    catch (NumberFormatException ex) {}

    try {
      xMaxVals[mOldXAxis] = Double.valueOf(xMax.getText()).doubleValue();
    }
    catch (NumberFormatException ex) {}

    try {
      xIncVals[mOldXAxis] = Double.valueOf(xInc.getText()).doubleValue();
    }
    catch (NumberFormatException ex) {}

    xTicVals[mOldXAxis] = ((Integer)xTics.getValue()).intValue();
    mSymbolSizes[mOldXAxis] = ((Integer)symbolSize.getValue()).intValue();

    mSymbols[mOldXAxis] = mSymbolPopup.getSelectedIndex() + 1;
    mXConnectColors[mOldXAxis] = axisSwatch.getColor();
  }

  public void upDateCurrSettings() {
    if (mCurrXAxis < 0) {
      return;
    }
    // called when a new parameter is chosen from the popup
    try {
      xMinVals[mCurrXAxis] = Double.valueOf(xMin.getText()).doubleValue();
    }
    catch (NumberFormatException ex) {}

    try {
      xMaxVals[mCurrXAxis] = Double.valueOf(xMax.getText()).doubleValue();
    }
    catch (NumberFormatException ex) {}

    try {
      xIncVals[mCurrXAxis] = Double.valueOf(xInc.getText()).doubleValue();
    }
    catch (NumberFormatException ex) {}

    xTicVals[mCurrXAxis] = ((Integer)xTics.getValue()).intValue();
    mSymbolSizes[mCurrXAxis] = ((Integer)symbolSize.getValue()).intValue();

    mSymbols[mCurrXAxis] = mSymbolPopup.getSelectedIndex() + 1;
    mXConnectColors[mCurrXAxis] = axisSwatch.getColor();
  }

  public double getXMin(int i) throws NumberFormatException {
    return xMinVals[i];
  }

  public double getXMax(int i) {
    return xMaxVals[i];
  }

  public double getXInc(int i) {
    return xIncVals[i];
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

  public int getXTics(int i) {
    return xTicVals[i];
  }

  public int getSymbolSize(int i) {
    return mSymbolSizes[i];
  }

  public int[] getXAxesOrder() {
    return mSelXParam;
  }

  public int getSymbol(int i) {
    return mSymbols[i];
  }

  public Color getColor(int i) {
    return mXConnectColors[i];
  }

  public void setSymbol(int sv) {
    mSymbolPopup.setSelectedIndex(sv);
    for (int i = 0; i < mNumXAxes; i++) {
      mSymbols[i] = sv + 1;
    }
  }

  public void setSymbolSize(int ss) {
    symbolSize.setValue(new Integer(ss));
    for (int i = 0; i < mNumXAxes; i++) {
      mSymbolSizes[i] = ss;
    }
  }

  public void maintainButtons() {
    if (mParamList != null && mParamList.getSelectedIndex() >= 0 && mNumXAxes > 1) {
      if (mParamList.getSelectedIndex() == 0) {
        mMoveUpButton.setEnabled(false);
      }
      else {
        mMoveUpButton.setEnabled(true);
      }
      if (mParamList.getSelectedIndex() + 1 == mNumXAxes) {
        mMoveDownButton.setEnabled(false);
      }
      else {
        mMoveDownButton.setEnabled(true);
      }
    }
    else {
      mMoveUpButton.setEnabled(false);
      mMoveDownButton.setEnabled(false);
    }
  }
}

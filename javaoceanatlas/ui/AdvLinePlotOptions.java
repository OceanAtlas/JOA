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

@SuppressWarnings("serial")
public class AdvLinePlotOptions extends JPanel implements DocumentListener, ActionListener, ItemListener {
	protected JOAJTextField xMin = new JOAJTextField("");
	protected JOAJTextField xMax = new JOAJTextField("");
	protected JOAJTextField xInc = new JOAJTextField("");
	protected JSpinner xTics;
	protected JOAJTextField yMin, yMax, yInc;
	protected JSpinner yTics;
	protected LinePlotSpecification mPlotSpec;
	protected int mSelXParam;
	protected int mSelYParam;
	boolean mChangedFlag = false;
	protected double mXDataMin, mXDataMax;
	protected double mYDataMin, mYDataMax;
	protected ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
	JOAJLabel minLabel = new JOAJLabel(b.getString("kMinimum"));
	JOAJLabel maxLabel = new JOAJLabel(b.getString("kMaximum"));
	JOAJLabel incLabel = new JOAJLabel(b.getString("kIncrement"));
	JOAJLabel minorTicLabel = new JOAJLabel(b.getString("kNoMinorTicks"));
	JOAJLabel sizeLabel = new JOAJLabel(b.getString("kSize"));
	String yAxisLabel = new String(b.getString("kYAxis"));
	protected JOAJList mParamList = null;
	private Timer timer = new Timer();
	protected double xMinVal;
	protected double xMaxVal;
	protected double xIncVal;
	protected int xTicVal;
	protected String mParamName;
	protected TitledBorder yAxisTitledBorder = null;
	protected String mYParamName = null;
	protected double mYMin = 0.0;
	protected double mYMax = 0.0;
	protected double mYInc = 0.0;
	protected int mYTics = 0;

	public AdvLinePlotOptions(FileViewer fv, LinePlotSpecification plotSpec) {
		mPlotSpec = plotSpec;
		ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

		this.setLayout(new BorderLayout(5, 5));

		// container for the axes stuff
		JPanel axesCont = new JPanel();
		axesCont.setLayout(new GridLayout(1, 2, 5, 5));

		JPanel xAxisCont = new JPanel();
		xAxisCont.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 1));
		TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kXAxis"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
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
		xMin.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getWinXPlotMin()), 3, false));
		xMin.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		xMin.getDocument().addDocumentListener(this);
		line1.add(xMin);

		JPanel line2 = new JPanel();
		line2.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
		line2.add(maxLabel);
		xMax = new JOAJTextField(6);
		xMax.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getWinXPlotMax()), 3, false));
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

		xAxis.add(line1);
		xAxis.add(line2);
		xAxis.add(line3);
		xAxis.add(line4);

		// y axis container
		JPanel yAxis = new JPanel();
		yAxis.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 2));
		yAxisTitledBorder = BorderFactory.createTitledBorder(b.getString("kYAxis"));
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


		xAxisCont.add(xAxis);
		axesCont.add(xAxisCont);
		axesCont.add(yAxis);

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
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("minmax")) {
			// set the ranges to the max min for that parameter
			xMin.setText(JOAFormulas.formatDouble(String.valueOf(mXDataMin), 3, false));
			xMax.setText(JOAFormulas.formatDouble(String.valueOf(mXDataMax), 3, false));
			xInc.setText(JOAFormulas.formatDouble(String.valueOf((mXDataMax - mXDataMin) / 5.0), 3, false));
			yMin.setText(JOAFormulas.formatDouble(String.valueOf(mYDataMin), 3, false));
			yMax.setText(JOAFormulas.formatDouble(String.valueOf(mYDataMax), 3, false));
			yInc.setText(JOAFormulas.formatDouble(String.valueOf((mYDataMax - mYDataMin) / 5.0), 3, false));
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() instanceof JOAJComboBox) {
			JOAJComboBox cb = (JOAJComboBox)evt.getSource();
		}
	}

	public void setParameters(int selXParam, String xParamNames, int selYParam, String yParamName) {
		mSelYParam = selYParam;
		mYParamName = new String(yParamName);
		mChangedFlag = false;
	}

	public void setValues(double inxMin, double inxMax, double inxInc, double inyMin, double inyMax, double inyInc,
	    int inxTics, int inyTics, double xDMin, double xDMax, double yDMin, double yDMax) {

		// reset the UI
		xMin.setText("");
		xMax.setText("");
		xInc.setText("");
		yMin.setText("");
		yMax.setText("");
		yInc.setText("");

		// store the new axis information
		xMinVal = inxMin;
		xMaxVal = inxMax;
		xIncVal = inxInc;
		xTicVal = inxTics;
		mXDataMin = xDMin;
		mXDataMax = xDMax;

		mYMin = inyMin;
		mYMax = inyMax;
		mYInc = inyInc;
		mYTics = inyTics;

		// set the UI to the first XAxis
		xMin.setText(JOAFormulas.formatDouble(String.valueOf(xMinVal), 3, false));
		xMax.setText(JOAFormulas.formatDouble(String.valueOf(xMaxVal), 3, false));
		xInc.setText(JOAFormulas.formatDouble(String.valueOf(xIncVal), 3, false));
		xTics.setValue(new Integer(xTicVal));

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
		// called when a new parameter is chosen from the popup
		try {
			xMinVal = Double.valueOf(xMin.getText()).doubleValue();
		}
		catch (NumberFormatException ex) {
		}

		try {
			xMaxVal = Double.valueOf(xMax.getText()).doubleValue();
		}
		catch (NumberFormatException ex) {
		}

		try {
			xIncVal = Double.valueOf(xInc.getText()).doubleValue();
		}
		catch (NumberFormatException ex) {
		}

		xTicVal = ((Integer)xTics.getValue()).intValue();
	}

	public double getXMin() throws NumberFormatException {
		return xMinVal;
	}

	public double getXMax() {
		return xMaxVal;
	}

	public double getXInc() {
		return xIncVal;
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

	public int getXTics() {
		return xTicVal;
	}
}

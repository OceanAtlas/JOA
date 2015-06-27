/*
 * $Id: ColorArrayEditor.java,v 1.5 2005/06/17 18:08:51 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import javaoceanatlas.resources.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.ui.widgets.*;

@SuppressWarnings("serial")
public class ColorArrayEditor extends JPanel implements ActionListener, ButtonMaintainer {
	protected double[] mValues = null;
	protected Color[] mColors = null;
	protected ResourceBundle mBundle = null;
	protected graphPanel mGraph = null;
	protected CurrValField mCurrValField = null;
	protected JRootPane mRootPane = null;
	protected int mValueWithFocus = 0;
	protected int mNumLevels = 0;
	protected JOAJButton mApply = null;
	protected JOAJLabel mLabel1 = null;
	protected double mBaseValue = 0;
	protected double mEndValue = 0;
	private Timer timer = new Timer();
	protected int mPreferredHSize = 300;
	protected LargeIconButton mLinear = null;
	protected LargeIconButton mPowerUp = null;
	protected LargeIconButton mPowerDown = null;
	protected LargeIconButton mLogistic = null;
	protected boolean mShowButtons;
	protected int numDecPlaces = 3;
	protected boolean mShowFields = false;
	protected JOAJTextField mStartField = null;
	protected JOAJTextField mEndField = null;
	protected String mParam = null;
	protected FileViewer mFileViewer = null;
	protected NewColorBar mColorBar = null;
	protected int[] mHistoBoundaries;
	protected int[] mHistoValues;
	protected Histogram mHisto;
	protected boolean mDisplayYAxis = true;
	protected boolean mAllowValueEditing = true;

	public ColorArrayEditor(JRootPane rootPane, int nl, double[] inValues, boolean showBtns) {
		mValues = inValues;
		mNumLevels = nl;
		if (mNumLevels > 32) {
			mPreferredHSize = 500;
		}

		mBaseValue = mValues[0];
		mEndValue = mValues[mNumLevels - 1];
		mRootPane = rootPane;
		mShowButtons = showBtns;
		init();
	}

	public ColorArrayEditor(JRootPane rootPane, double[] inValues, Color[] inColors, boolean showBtns) {
		mNumLevels = mValues.length;
		mBaseValue = mValues[0];
		mEndValue = mValues[mNumLevels - 1];
		mValues = inValues;
		mColors = inColors;
		mRootPane = rootPane;
		mShowButtons = showBtns;
		numDecPlaces = JOAFormulas.getPrecision(mBaseValue, mEndValue);
		init();
	}

	public ColorArrayEditor(JRootPane rootPane, boolean showBtns) {
		mRootPane = rootPane;
		mShowButtons = showBtns;
		init();
	}

	public ColorArrayEditor(JRootPane rootPane, boolean showBtns, boolean showFields) {
		mRootPane = rootPane;
		mShowButtons = showBtns;
		mShowFields = showFields;
		init();
	}

	public void finalize() {
		timer.cancel();
	}

	protected void init() {
		mBundle = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
		this.setLayout(new BorderLayout(5, 5));
		mGraph = new graphPanel();
		this.add("Center", new TenPixelBorder(mGraph, 0, 0, 0, 5));
		JPanel buttonsAndFields = new JPanel();
		buttonsAndFields.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 2));

		if (mShowButtons) {
			JPanel line6 = new JPanel();
			line6.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			line6.add(new JOAJLabel(mBundle.getString("kCreateWithShape")));
			JPanel shapePanel = new JPanel();
			shapePanel.setLayout(new GridLayout(1, 4, 5, 0));

			try {
				mLinear = new LargeIconButton(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
				    "images/linear.gif")));
				mPowerUp = new LargeIconButton(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
				    "images/powerup.gif")));
				mPowerDown = new LargeIconButton(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
				    "images/powerdown.gif")));
				mLogistic = new LargeIconButton(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
				    "images/logistic.gif")));
				
				mLinear.setToolTipText(mBundle.getString("kLinearTip"));
				mPowerUp.setToolTipText(mBundle.getString("kIncreasingExpTip"));
				mPowerDown.setToolTipText(mBundle.getString("kDecreasingExpTip"));
				mLogistic.setToolTipText(mBundle.getString("kReverseSTip"));
			}
			catch (Exception ex) {
				ex.printStackTrace();
	    	System.out.println("ColorArrayEditor:init");
			}

			shapePanel.add(mLinear);
			shapePanel.add(mPowerUp);
			shapePanel.add(mPowerDown);
			shapePanel.add(mLogistic);
			mLinear.addActionListener(this);
			mPowerUp.addActionListener(this);
			mPowerDown.addActionListener(this);
			mLogistic.addActionListener(this);
			mLinear.setActionCommand("linear");
			mPowerUp.setActionCommand("powerUp");
			mPowerDown.setActionCommand("powerDown");
			mLogistic.setActionCommand("logistic");
			line6.add(shapePanel);
			buttonsAndFields.add(line6);
		}

		if (mShowFields) {
			JPanel line3 = new JPanel();
			line3.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
			line3.add(new JOAJLabel(mBundle.getString("kStartValue")));
			mStartField = new JOAJTextField(6);
			mStartField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			line3.add(mStartField);
			line3.add(new JOAJLabel("  "));
			line3.add(new JOAJLabel(mBundle.getString("kEndValue")));
			mEndField = new JOAJTextField(6);
			mEndField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			line3.add(mEndField);
			buttonsAndFields.add(line3);

			mStartField.setText(JOAFormulas.formatDouble(String.valueOf(mBaseValue), 2, false));
			mEndField.setText(JOAFormulas.formatDouble(String.valueOf(mEndValue), 2, false));
			mStartField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					try {
						String fldText = mStartField.getText();
						mBaseValue = Double.valueOf(fldText).doubleValue();
					}
					catch (Exception ex) {
						JFrame f = new JFrame("Colorbar Range Error");
						Toolkit.getDefaultToolkit().beep();
						JOptionPane.showMessageDialog(f, "Invalid start level");
						mStartField.setText(JOAFormulas.formatDouble(String.valueOf(mBaseValue), 2, false));
					}
				}
			});
			mEndField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					try {
						mEndField.getText();
					}
					catch (Exception ex) {
						JFrame f = new JFrame("Colorbar Range Error");
						Toolkit.getDefaultToolkit().beep();
						JOptionPane.showMessageDialog(f, "Invalid end level");
						mEndField.setText(JOAFormulas.formatDouble(String.valueOf(mEndValue), 2, false));
					}
				}
			});
		}

		this.add("North", buttonsAndFields);

		JPanel contPanel = new JPanel();
		contPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 1));
		mLabel1 = new JOAJLabel(mBundle.getString("kValue"));
		contPanel.add(mLabel1);
		mCurrValField = new CurrValField(6);
		contPanel.add(mCurrValField);
		mApply = new JOAJButton(mBundle.getString("kApply"));
		mRootPane.setDefaultButton(mApply);
		mApply.setActionCommand("apply");
		mApply.addActionListener(this);
		contPanel.add(mApply);
		this.add("South", contPanel);
		setAllowValueEditing(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
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

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("apply")) {
			try {
				double newVal = Double.valueOf(mCurrValField.getText()).doubleValue();
				if (newVal <= mValues[mNumLevels - 1] && newVal >= mValues[0]) {
					mValues[mValueWithFocus] = newVal;
					Graphics g = mGraph.getGraphics();
					mGraph.invalidate();
					mGraph.paintComponent(g);
				}
				else if (newVal > mValues[mNumLevels - 1] || newVal < mValues[0]) {
					mValues[mValueWithFocus] = newVal;
					Graphics g = mGraph.getGraphics();
					mGraph.calibrateGraph();
					mGraph.invalidate();
					mGraph.paintComponent(g);
				}
				else {
					Toolkit.getDefaultToolkit().beep();
				}
			}
			catch (NumberFormatException ex) {
				Toolkit.getDefaultToolkit().beep();
			}
		}
		else if (cmd.equals("linear")) {
			createColorBar(JOAConstants.LINEAR);
			mLinear.setSelected(false);
		}
		else if (cmd.equals("powerUp")) {
			createColorBar(JOAConstants.EXPONENTIALUP);
			mPowerUp.setSelected(false);
		}
		else if (cmd.equals("powerDown")) {
			createColorBar(JOAConstants.EXPONENTIALDOWN);
			mPowerDown.setSelected(false);
		}
		else if (cmd.equals("logistic")) {
			createColorBar(JOAConstants.LOGISTIC);
			mLogistic.setSelected(false);
		}
	}

	public void createColorBar(int curveShape) {
		// get base and end levels
		double baseLevel = mValues[0];
		double endLevel = mValues[mNumLevels - 1];
		try {
			String fldText = mStartField.getText();
			baseLevel = Double.valueOf(fldText).doubleValue();
		}
		catch (Exception ex) {
		}
		try {
			String fldText = mEndField.getText();
			endLevel = Double.valueOf(fldText).doubleValue();
		}
		catch (Exception ex) {
		}

		// compute new color bar values
		double[] tempValues = new double[(int) mNumLevels];

		if (curveShape == JOAConstants.LINEAR) {
			double increment = (endLevel - baseLevel) / (mNumLevels - 1);
			for (int i = 0; i < (int) mNumLevels; i++) {
				tempValues[i] = baseLevel + (i * increment);
			}
		}
		else if (curveShape == JOAConstants.EXPONENTIALUP || curveShape == JOAConstants.EXPONENTIALDOWN) {
			double shape = JOAFormulas.getShape(baseLevel, endLevel);
			double scaledMax = Math.abs(endLevel - baseLevel);
			double lnScaledMin = Math.log(shape);
			double lnScaledMax = Math.log(scaledMax + shape);
			double increment = (lnScaledMax - lnScaledMin) / (mNumLevels - 1);

			for (int i = 0; i < (int) mNumLevels; i++) {
				if (curveShape == JOAConstants.EXPONENTIALUP) {
					// lower
					if (baseLevel < endLevel) {
						tempValues[i] = baseLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
					}
					else {
						tempValues[i] = baseLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
					}
				}
				else if (curveShape == JOAConstants.EXPONENTIALDOWN) {
					// upper
					if (baseLevel < endLevel) {
						tempValues[(int) mNumLevels - i - 1] = endLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
					}
					else {
						tempValues[(int) mNumLevels - i - 1] = endLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
					}
				}
			}
		}
		else if (curveShape == JOAConstants.LOGISTIC) {
			// logistic is a pieced together from upper and lower asymptote
			int mid = 0;
			int nl = (int) mNumLevels;
			if (nl % 2 > 0) {
				mid = (nl / 2) + 1;
			}
			else {
				mid = nl / 2;
			}

			// upper asymptote from base level to midpoint
			double newEndLevel = (baseLevel + endLevel) / 2;
			double shape = JOAFormulas.getShape(baseLevel, newEndLevel);
			double scaledMax = Math.abs(baseLevel - newEndLevel);
			double lnScaledMin = Math.log(shape);
			double lnScaledMax = Math.log(scaledMax + shape);
			double increment = (lnScaledMax - lnScaledMin) / ((double) mid - 1);

			// lower
			for (int i = 0; i < mid; i++) {
				if (baseLevel < newEndLevel) {
					tempValues[mid - i - 1] = newEndLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
				}
				else {
					tempValues[mid - i - 1] = newEndLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
				}
			}

			// lower asymptote from midpoint to endlevel
			double newBaseLevel = newEndLevel;
			shape = JOAFormulas.getShape(newBaseLevel, endLevel);
			scaledMax = Math.abs(newBaseLevel - endLevel);
			lnScaledMin = Math.log(shape);
			lnScaledMax = Math.log(scaledMax + shape);
			increment = (lnScaledMax - lnScaledMin) / ((double) mid - 1);

			// upper
			int endl = 0;
			if (nl % 2 > 0) {
				endl = mid - 1;
			}
			else {
				endl = mid;
			}
			for (int i = 0; i < endl; i++) {
				if (newBaseLevel < endLevel) {
					tempValues[i + mid] = newBaseLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
				}
				else {
					tempValues[i + mid] = newBaseLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
				}
			}

		}

		tempValues[0] = baseLevel;
		numDecPlaces = JOAFormulas.getPrecision(tempValues[0], tempValues[mNumLevels - 1]);
		this.setValueArray(tempValues, mNumLevels);
		Graphics g = mGraph.getGraphics();
		mGraph.invalidate();
		mGraph.paintComponent(g);
	}

	public void setNumLevels(int nl) {
		mNumLevels = nl;
	}

	public int getNumLevels() {
		return mNumLevels;
	}

	public void setHistogram(String param, FileViewer fv, NewColorBar cb) {
		// Exception ex = new Exception();
		// ex.printStackTrace();

		mParam = new String(param);
		mFileViewer = fv;
		mColorBar = cb;
		setNumLevels(mColorBar.getNumLevels());

		// get a histogram for the parameter
		mHistoBoundaries = new int[mNumLevels];
		mHistoValues = new int[mNumLevels];
		if (mValues == null) {
			mValues = new double[mNumLevels];
		}
		if (mParam != null) {
			mHisto = null;
			mHisto = new Histogram(mParam, mFileViewer, mColorBar);
		}
		mGraph.calibrateGraph();
		mGraph.invalidate();
		mLabel1.invalidate();
	}

	public boolean isHistogramSet() {
		return mHisto == null;
	}

	public void setValueArray(double[] inValues, int nl) {
		mNumLevels = nl;
		if (mValues != null) {
			mValues = null;
			mValues = new double[mNumLevels];
		}
		mValues = inValues;
		if (mNumLevels > 32) {
			mPreferredHSize = 500;
		}
		mBaseValue = mValues[0];
		mEndValue = mValues[mNumLevels - 1];
		numDecPlaces = JOAFormulas.getPrecision(mBaseValue, mEndValue);
		if (mShowFields) {
			mStartField.setText(JOAFormulas.formatDouble(String.valueOf(mBaseValue), 2, false));
			mEndField.setText(JOAFormulas.formatDouble(String.valueOf(mEndValue), 2, false));
		}
		if (mHisto != null) {
			mHistoBoundaries = null;
			mHistoValues = null;
			mHistoBoundaries = new int[mNumLevels];
			mHistoValues = new int[mNumLevels];
			mHisto.setValues(mBaseValue, mEndValue, mNumLevels);
		}
		mGraph.calibrateGraph();
		mGraph.invalidate();
		mLabel1.invalidate();
	}

	public double[] getValueArray() {
		return mValues;
	}

	public void setColorArray(Color[] inColors, int nl) {
		mNumLevels = nl;
		if (mColors != null) {
			mColors = null;
		}
		mColors = inColors;
		mGraph.invalidate();
	}

	public void maintainButtons() {
		if (mValues == null) {
			mApply.setEnabled(false);
			mCurrValField.setEnabled(false);
			mLabel1.setEnabled(false);
		}
		else {
			mApply.setEnabled(true);
			mCurrValField.setEnabled(true);
			mLabel1.setEnabled(true);
		}
	}

	public void setDisplayYAxis(boolean val) {
		mDisplayYAxis = val;

		if (mDisplayYAxis) {
			mGraph.expandLeft();
		}
		else {
			mGraph.contractLeft();
		}
	}

	public void setAllowValueEditing(boolean val) {
		mAllowValueEditing = val;

		if (mAllowValueEditing) {
			mApply.setVisible(true);
			mCurrValField.setVisible(true);
			mLabel1.setVisible(true);
		}
		else {
			mApply.setVisible(false);
			mCurrValField.setVisible(false);
			mLabel1.setVisible(false);
		}
	}

	private class CurrValField extends JOAJTextField {
		public CurrValField(int numCols) {
			super(numCols);
			this.addKeyListener(new KeyListener());
		}

		public boolean isFocusTraversable() {
			return true;
		}

		private class KeyListener extends KeyAdapter {
			public void keyPressed(KeyEvent ev) {
				int key = ev.getKeyCode();

				if (key == KeyEvent.VK_LEFT) {
					mGraph.findByArrowKey(new Integer(JOAConstants.PREVSTN));
					mGraph.displayCurrValue();
					ev.consume();
				}
				else if (key == KeyEvent.VK_RIGHT) {
					mGraph.findByArrowKey(new Integer(JOAConstants.NEXTSTN));
					mGraph.displayCurrValue();
					ev.consume();
				}
				else if (key == KeyEvent.VK_UP) {
					mGraph.findByArrowKey(new Integer(JOAConstants.PREVOBS));
					mGraph.displayCurrValue();
					ev.consume();
				}
				else if (key == KeyEvent.VK_DOWN) {
					mGraph.findByArrowKey(new Integer(JOAConstants.NEXTOBS));
					mGraph.displayCurrValue();
					ev.consume();
				}
				else if (key == KeyEvent.VK_ENTER) {
					try {
						double newVal = Double.valueOf(mCurrValField.getText()).doubleValue();
						if (newVal <= mValues[mNumLevels - 1] && newVal >= mValues[0]) {
							mValues[mValueWithFocus] = newVal;
							Graphics g = mGraph.getGraphics();
							mGraph.invalidate();
							mGraph.paintComponent(g);
						}
						else {
							Toolkit.getDefaultToolkit().beep();
						}
					}
					catch (NumberFormatException ex) {
						Toolkit.getDefaultToolkit().beep();
					}
					ev.consume();
				}
				else {
					super.keyPressed(ev);
				}
			}
		}
	}

	private class graphPanel extends JPanel implements FocusListener {
		double mDiff = 0;
		double mIncrement = 0;
		double mDIncrement = 0;
		double mPixelsPerColorBand = 0;
		double mVOrigin = 0;
		int mLeft = 60;
		int mRight = 0;
		int mBottom = 0;
		int mTop = 0;
		boolean mHasFocus = false;
		Image mOffScreen = null;
		double mWinXScale;
		double mWinYScale;
		double mWinXOrigin = 0;

		public graphPanel() {
			init();
			addMouseListener(new graphMouseHandler());
			addMouseMotionListener(new graphMouseMotionHandler());
			this.addFocusListener(this);
		}

		public void calibrateGraph() {
			Dimension d = this.getPreferredSize();
			mRight = d.width - 1;
			mBottom = d.height - 5;
			mTop = 5;
			mVOrigin = mValues[0];
			mDiff = mValues[mNumLevels - 1] - mValues[0];
			mIncrement = ((double) d.height - 10) / mDiff;
			mDIncrement = mDiff / ((double) d.height - 10);
			mPixelsPerColorBand = ((d.width - 1 - mLeft) / mNumLevels) - 1;
			if (mPixelsPerColorBand <= 0) {
				mPixelsPerColorBand = 1;
			}
			mVOrigin = mValues[0];

			// compute the histogram boundaries
			if (mParam != null) {
				mHisto.setValues(mValues[0], mValues[mNumLevels - 1], mNumLevels);
				mHistoValues = mHisto.getHistoValues();

				// compute the x-scale
				mWinXScale = (double) (d.width - 1 - mLeft) / (double) (mHisto.getMax());
				mWinYScale = ((double) mNumLevels - 1) / (double) mDiff;

				for (int i = 0; i < mNumLevels; i++) {
					double val = mVOrigin + i * mDiff / ((double) mNumLevels - 1);
					int v = (int) (mBottom - mIncrement * (val - mVOrigin));
					mHistoBoundaries[i] = v;
				}
			}
		}

		public void init() {
			// initialize constants
			if (mValues != null) {
				calibrateGraph();
			}

			// add the key listeners
			this.registerKeyboardAction(new RightListener((Object) this, this.getClass()), KeyStroke.getKeyStroke(
			    KeyEvent.VK_RIGHT, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
			this.registerKeyboardAction(new LeftListener((Object) this, this.getClass()), KeyStroke.getKeyStroke(
			    KeyEvent.VK_LEFT, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
			this.registerKeyboardAction(new UpListener((Object) this, this.getClass()), KeyStroke.getKeyStroke(
			    KeyEvent.VK_UP, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
			this.registerKeyboardAction(new DownListener((Object) this, this.getClass()), KeyStroke.getKeyStroke(
			    KeyEvent.VK_DOWN, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
		}

		public Dimension getPreferredSize() {
			return new Dimension(mPreferredHSize, 200);
		}

		public boolean isFocusTraversable() {
			return true;
		}

		public class graphMouseMotionHandler extends MouseMotionAdapter {
			int lastBand = -99;

			public void mouseDragged(MouseEvent me) {
				if (mValues == null) { return; }
				Graphics g = getGraphics();
				// get the current band
				int x = me.getX();
				int y = me.getY();
				if (x < mLeft) {
					x = mLeft;
				}
				if (x > mRight) {
					x = mRight;
				}
				if (y > mBottom) {
					y = mBottom;
				}
				if (y < mTop) {
					y = mTop;
				}

				int newBand = (int) ((x - mLeft) / (mPixelsPerColorBand + 1));
				if (newBand < 0) {
					newBand = 0;
				}
				if (newBand > mNumLevels - 1) {
					newBand = mNumLevels - 1;
				}
				double newVal = (me.getY() - 2 - mBottom) / -mIncrement + mVOrigin;
				if (newVal < mBaseValue) {
					newVal = mBaseValue;
				}
				if (newVal > mEndValue) {
					newVal = mEndValue;
				}
				mValues[newBand] = newVal;
				invalidate();
				paintComponent(g);
			}
		}

		public class graphMouseHandler extends MouseAdapter {
			public void mouseClicked(MouseEvent me) {
				if (mValues == null) { return; }
				mGraph.requestFocus();
				Graphics g = getGraphics();
				// see if this falls with an existing color band
				double upperVal = (me.getY() - 2 - mBottom) / -mIncrement + mVOrigin;
				double lowerVal = (me.getY() + 2 - mBottom) / -mIncrement + mVOrigin;
				for (int i = 0; i < mNumLevels; i++) {
					double val = mValues[i];
					int lowerH = (int) (mLeft + i * (mPixelsPerColorBand + 1));
					int upperH = (int) (lowerH + mPixelsPerColorBand);
					int h = me.getX();
					if ((val >= lowerVal && val <= upperVal) && (h >= lowerH && h <= upperH)) {
						if (i != mValueWithFocus) {
							mValueWithFocus = i;
							invalidate();
							paintComponent(g);
						}
						break;
					}
				}
			}
		}

		public void focusGained(FocusEvent fe) {
			mHasFocus = true;
			Graphics g = this.getGraphics();
			invalidate();
			paintComponent(g);
		}

		public void focusLost(FocusEvent fe) {
			mHasFocus = false;
			Graphics g = this.getGraphics();
			invalidate();
			paintComponent(g);
		}

		public boolean findByArrowKey(Integer direction) {
			// if (mHasFocus) {
			switch (direction.intValue()) {
				case 1: // JOAConstants.NEXTSTN:

					// go to next level
					if (mValueWithFocus + 1 <= mNumLevels - 1) {
						mValueWithFocus++;

						Graphics g = this.getGraphics();
						invalidate();
						paintComponent(g);
					}
					else {
						Toolkit.getDefaultToolkit().beep();
					}
					break;
				case 2: // JOAConstants.PREVSTN:

					// go to prev level
					if (mValueWithFocus - 1 >= 0) {
						mValueWithFocus--;

						Graphics g = this.getGraphics();
						invalidate();
						paintComponent(g);
					}
					else {
						Toolkit.getDefaultToolkit().beep();
					}
					break;
				case 3: // JOAConstants.NEXTOBS:

					// decrease value
					if (mValues[mValueWithFocus] - mDIncrement >= mValues[0]) {
						mValues[mValueWithFocus] -= mDIncrement;
						Graphics g = this.getGraphics();
						invalidate();
						paintComponent(g);
					}
					else {
						Toolkit.getDefaultToolkit().beep();
					}
					break;
				case 4: // JOAConstants.PREVOBS:

					// increase value
					if (mValues[mValueWithFocus] + mDIncrement <= mValues[mNumLevels - 1]) {
						mValues[mValueWithFocus] += mDIncrement;
						Graphics g = this.getGraphics();
						invalidate();
						paintComponent(g);
					}
					else {
						Toolkit.getDefaultToolkit().beep();
					}
					break;
			}
			// }
			return true;
		}

		public void invalidate() {
			super.invalidate();
			mOffScreen = null;
		}

		public void paintComponent(Graphics g) {
			if (mOffScreen == null) {
				mOffScreen = createImage(getSize().width, getSize().height);

				Graphics og = mOffScreen.getGraphics();
				super.paintComponent(og);

				if (mValues != null) {
					// calibrateGraph();

					// draw the histogram
					if (mParam != null) {
						drawHistogram(og);
					}

					// draw the graph
					drawGrid(og);
					drawValues(og);
					hiliteCurrentValue(og);
					if (mHasFocus) {
						displayCurrValue();
					}
					else {
						eraseCurrValue();
					}
				}
				else {
					og.setColor(this.getBackground());
					int width = getSize().width;
					int height = getSize().height;
					og.fillRect(0, 0, width, height);
				}

				g.drawImage(mOffScreen, 0, 0, null);
				og.dispose();
			}
			else {
				g.drawImage(mOffScreen, 0, 0, null);
			}
		}

		protected void drawGrid(Graphics g) {
			int size = 12;
			if (JOAConstants.ISSUNOS) {
				size = 14;
			}
			g.setFont(new Font("Courier", Font.PLAIN, size));
			for (int i = 0; i < 11; i++) {
				double myVal = mVOrigin + i * mDiff / 10;
				String label = JOAFormulas.formatDouble(myVal, numDecPlaces, true);
				int v = (int) (mBottom - mIncrement * (myVal - mVOrigin));
				if (v > mTop) {
					g.setColor(Color.getHSBColor((float) 0.63, (float) 0.38, (float) 0.89));
					g.drawLine(mLeft - 2, v, mRight, v);
				}
				g.setColor(Color.black);
				if (mDisplayYAxis) {
					g.drawString(label, 0, v + 4);
				}
			}

			g.setColor(Color.black);

			// draw the x axis
			g.drawLine(mLeft, mBottom, mRight, mBottom);
			g.drawLine(mLeft, mTop, mRight, mTop);

			// draw the y axes
			g.drawLine(mRight, mBottom, mRight, mTop);
			g.drawLine(mLeft, mBottom, mLeft, mTop);
		}

		public void drawHistogram(Graphics g) {
			// compute the histogram boundaries
			if (mParam != null) {
				g.setColor(new Color(125, 125, 125));
				for (int i = 0; i < mNumLevels; i++) {
					int x = (int) ((mHistoValues[i]) * mWinXScale);
					int ybott;
					int ytop;
					if (i == 0) {
						ybott = mBottom;
						ytop = mHistoBoundaries[i];
					}
					else {
						ytop = mHistoBoundaries[i];
						ybott = mHistoBoundaries[i - 1];
					}
					g.fillRect(mLeft, ytop, x, ybott - ytop);
				}
			}
		}

		public void drawValues(Graphics g) {
			for (int i = 0; i < mNumLevels; i++) {
				double val = mValues[i];
				int v = (int) (mBottom - mIncrement * (val - mVOrigin));
				int h = (int) (mLeft + i * (mPixelsPerColorBand + 1));
				if (mColors != null) {
					g.setColor(mColors[i]);
				}
				else {
					g.setColor(this.getBackground());
				}
				g.fillRect(h + 2, v - 2, (int) (mPixelsPerColorBand - 1), 4);
				g.setColor(Color.black);
				g.drawRect(h + 2, v - 2, (int) (mPixelsPerColorBand - 1), 4);
			}
		}

		public void hiliteCurrentValue(Graphics g) {
			if (mHasFocus) {
				g.setColor(Color.black);
			}
			else {
				g.setColor(Color.white);
			}
			double val = mValues[mValueWithFocus];
			int v = (int) (mBottom - mIncrement * (val - mVOrigin));
			int h = (int) (mLeft + mValueWithFocus * (mPixelsPerColorBand + 1));
			g.drawRect(h + 1, v - 3, (int) (mPixelsPerColorBand + 1), 6);
		}

		public void displayCurrValue() {
			if (!mAllowValueEditing) { return; }
			mCurrValField.setText(JOAFormulas.formatDouble(String.valueOf(mValues[mValueWithFocus]), numDecPlaces, false));
			if (!mHasFocus) {
				mCurrValField.selectAll();
			}
		}

		public void eraseCurrValue() {
			; // mCurrValField.setText("");
		}

		public void expandLeft() {
			mLeft = 60;
		}

		public void contractLeft() {
			mLeft = 10;
		}

		/*
		 * public void textValueChanged(TextEvent te) { TextField field =
		 * (TextField)te.getSource(); String newVal = field.getText(); try { double
		 * val = Double.valueOf(newVal).doubleValue(); if (val >=mValues[0] && val <=
		 * mValues[mNumLevels-1]) { mValues[mValueWithFocus] = val; Graphics g =
		 * this.getGraphics(); invalidate(); paintComponent(g); } else
		 * Toolkit.getDefaultToolkit().beep(); } catch (Exception ex) {
		 * Toolkit.getDefaultToolkit().beep(); } }
		 */
	}
}

/*
 * $Id: Spinner.java,v 1.3 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;
import javaoceanatlas.resources.*;
import javaoceanatlas.utility.*;

@SuppressWarnings("serial")
public class Spinner extends JPanel implements ActionListener, ButtonMaintainer, ChangeListener {
  protected JOAJTextField mValFld = null;
  protected double[] mValues = null;
  protected int mPrecision = 3;
  protected int mCurrIndex = 0;
  protected int mMode;
  public static int SPINNER_HORIZONTAL = 1;
  public static int SPINNER_VERTICAL = 2;
  protected SmallIconButton upBtn = null;
  protected SmallIconButton downBtn = null;
  protected boolean mIsDisabled = true;
  protected boolean mIsReversed = false;
  protected boolean mDisplayInteger = false;
	private Timer timer = new Timer();

  public Spinner(int mode, double minVal, double maxVal, double inc, boolean reversed, boolean integerSpinner) {
    mMode = mode;
    int numPts = (int)((maxVal - minVal) / inc);
    mValues = new double[numPts];
    for (int i = 0; i < numPts; i++) {
      mValues[i] = minVal + ((double)i * inc);
    }
    mIsReversed = reversed;
    mDisplayInteger = integerSpinner;

    // init the interface
    init();
  }

  public Spinner(int mode, boolean integerSpinner) {
    this(mode, 0.0, 1.0, 0.1, false, integerSpinner);
  }

  public Spinner(int mode, double[] valArray, int nl, boolean reversed, boolean integerSpinner) {
    mMode = mode;
    mValues = new double[nl];
    int numPts = nl;
    mIsReversed = reversed;
    mDisplayInteger = integerSpinner;
    for (int i = 0; i < numPts; i++) {
      mValues[i] = valArray[i];
    }
    // init the interface
    init();
  }

  public void finalize() {
    timer.cancel();
  }

  public void setValues(double[] valArray, int nl) {
    mValues = new double[nl];
    int numPts = nl;
    for (int i = 0; i < numPts; i++) {
      mValues[i] = valArray[i];
    }
  }

  public void init() {
    JPanel mainPanel = new JPanel();

    if (mDisplayInteger) {
      mValFld = new JOAJTextField(4);
    }
    else {
      mValFld = new JOAJTextField(4);
    }

    mValFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mValFld.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        double val;
        try {
          val = Double.valueOf(mValFld.getText()).doubleValue();
        }
        catch (Exception e) {
          val = -99;
        }

        setValue(val);
      }
    });

    mValFld.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent me) {
        double val;
        try {
          val = Double.valueOf(mValFld.getText()).doubleValue();
        }
        catch (Exception e) {
          val = -99;
        }

        setValue(val);
      }
    });

    // display initial value
    displayValue(mValues[0]);

    try {
      upBtn = new SmallIconButton(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/spinnerup.gif")));
      downBtn = new SmallIconButton(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/spinnerdown.gif")));
    }
    catch (Exception ex) {
      upBtn = new SmallIconButton(new ImageIcon(getClass().getResource("/images/spinnerup.gif")));
      downBtn = new SmallIconButton(new ImageIcon(getClass().getResource("/images/spinnerdown.gif")));
    }

    upBtn.setFocusPainted(false);
    downBtn.setFocusPainted(false);
    if (mMode == Spinner.SPINNER_VERTICAL) {
      if (JOAConstants.ISMAC) {
        upBtn.setBtnSize(32, 22);
        downBtn.setBtnSize(32, 22);
      }
      else {
        upBtn.setBtnSize(20, 10);
        downBtn.setBtnSize(20, 10);
      }
    }
    upBtn.addActionListener(this);
    downBtn.addActionListener(this);
    upBtn.setActionCommand("up");
    downBtn.setActionCommand("down");

    if (mMode == Spinner.SPINNER_HORIZONTAL) {
      mainPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
      mainPanel.add(mValFld);
      mainPanel.add(upBtn);
      mainPanel.add(downBtn);
    }
    else {
      mainPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
      JPanel btnPanel = new JPanel();
      btnPanel.setLayout(new GridLayout(2, 1, 0, 0));
      mainPanel.add(mValFld);
      btnPanel.add(upBtn);
      btnPanel.add(downBtn);
      mainPanel.add(btnPanel);
    }
    this.add(mainPanel, "Center");

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

  public void stateChanged(ChangeEvent evt) {
    SmallIconButton src = (SmallIconButton)evt.getSource();
    String cmd = src.getActionCommand();

    if (cmd.equals("up")) {
      if (mCurrIndex + 1 < mValues.length) {
        displayValue(mValues[++mCurrIndex]);
      }
    }
    else if (cmd.equals("down")) {
      if (mCurrIndex - 1 >= 0) {
        displayValue(mValues[--mCurrIndex]);
      }
    }
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (!mIsReversed) {
      if (cmd.equals("up")) {
        if (mCurrIndex + 1 < mValues.length) {
          displayValue(mValues[++mCurrIndex]);
        }
        upBtn.setSelected(false);
      }
      else if (cmd.equals("down")) {
        if (mCurrIndex - 1 >= 0) {
          displayValue(mValues[--mCurrIndex]);
        }
        downBtn.setSelected(false);
      }
    }
    else {
      if (cmd.equals("up")) {
        if (mCurrIndex - 1 >= 0) {
          displayValue(mValues[--mCurrIndex]);
        }
        upBtn.setSelected(false);
      }
      else if (cmd.equals("down")) {
        if (mCurrIndex + 1 < mValues.length) {
          displayValue(mValues[++mCurrIndex]);
        }
        downBtn.setSelected(false);
      }
    }
  }

  public void setValue(double inVal) {
    // find the closest value in the array to set the index
    double delta = 10000000;
    for (int i = 0; i < mValues.length; i++) {
      double d = Math.abs(inVal - mValues[i]);
      if (d < delta) {
        delta = d;
        mCurrIndex = i;
      }
    }
    displayValue(inVal);
  }

  public double getValue() throws NumberFormatException {
    double out;
    try {
      out = Double.valueOf(mValFld.getText()).doubleValue();
    }
    catch (NumberFormatException ex) {
      throw ex;
    }
    return out;
  }

  public void setReversed(boolean flag) {
    mIsReversed = flag;
  }

  public void displayValue(double inVal) {
    if (mDisplayInteger) {
      mValFld.setText(String.valueOf((int)inVal));
    }
    else {
      if (mPrecision == 0) {
        mValFld.setText(String.valueOf((int)inVal));
      }
      else {
        String strVal = null;
        if (mPrecision == 1) {
          strVal = JOAFormulas.formatDouble(String.valueOf(inVal), 1, false);
        }
        else if (mPrecision == 2) {
          strVal = JOAFormulas.formatDouble(String.valueOf(inVal), 2, false);
        }
        else if (mPrecision == 3) {
          strVal = JOAFormulas.formatDouble(String.valueOf(inVal), 3, false);
        }
        else if (mPrecision == 4) {
          strVal = JOAFormulas.formatDouble(String.valueOf(inVal), 4, false);
        }
        else if (mPrecision == 5) {
          strVal = JOAFormulas.formatDouble(String.valueOf(inVal), 5, false);
        }
        else if (mPrecision == 6) {
          strVal = JOAFormulas.formatDouble(String.valueOf(inVal), 6, false);
        }
        else if (mPrecision == 7) {
          strVal = JOAFormulas.formatDouble(String.valueOf(inVal), 7, false);
        }
        mValFld.setText(strVal);
      }
    }
  }

  public void setPrecision(int precision) {
    mPrecision = precision;
  }

  public void maintainButtons() {
    if (mIsDisabled) {
      upBtn.setEnabled(false);
      downBtn.setEnabled(false);
      mValFld.setEnabled(false);
      return;
    }
    else {
      mValFld.setEnabled(true);
    }

    if (mCurrIndex == 0) {
      if (mIsReversed) {
        upBtn.setEnabled(false);
        downBtn.setEnabled(true);
      }
      else {
        upBtn.setEnabled(true);
        downBtn.setEnabled(false);
      }
    }
    else if (mCurrIndex + 1 == mValues.length) {
      if (mIsReversed) {
        upBtn.setEnabled(true);
        downBtn.setEnabled(false);
      }
      else {
        upBtn.setEnabled(false);
        downBtn.setEnabled(true);
      }
    }
    else {
      upBtn.setEnabled(true);
      downBtn.setEnabled(true);
    }
  }

  public void setEnabled(boolean state) {
    if (state) {
      mIsDisabled = false;
    }
    else {
      mIsDisabled = true;
    }
  }

  public void setNewValues(double[] inValues, int numLevels, boolean reversed) {
    mValues = null;
    mValues = new double[numLevels];
    mIsReversed = reversed;
    int numPts = numLevels;
    for (int i = 0; i < numPts; i++) {
      mValues[i] = inValues[i];
    }
    mCurrIndex = 0;
    displayValue(mValues[0]);
  }

  public boolean isFocusTraversable() { // here to prevent focus aquisition
    return false;
  }

  public void requestFocus() {} // stubbed to prevent focus aquisition
}

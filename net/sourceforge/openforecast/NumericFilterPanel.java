/**
 * 
 */
package net.sourceforge.openforecast;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javaoceanatlas.ui.NumericFilter;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author oz
 *
 */
public class NumericFilterPanel extends JPanel {
	private String mRangeStr;
	private JTextField mMinTF;
	private JTextField mMaxTF;
	private JLabel mRangeLbl;
  JCheckBox mCB;
	
	public NumericFilterPanel(String btnLbl, String label, double min, double max, boolean initialState) {
	  mCB = new JCheckBox(btnLbl, false);
		mRangeStr = label;
		mRangeLbl = new JLabel(" <= " + mRangeStr + " <= ");

		this.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		mMinTF = new JTextField(5);
		mMaxTF = new JTextField(5);
		
		if (min != -999) {
			mMinTF.setText(String.valueOf(min));
		}

		if (max != -999) {
			mMaxTF.setText(String.valueOf(min));
		}
		
		mMinTF.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		mMaxTF.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		this.add(mCB);
		this.add(mMinTF);
		this.add(mRangeLbl);
		this.add(mMaxTF);

		mCB.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					setEnabled(true);
				}
				else {
					setEnabled(false);
				}
			}
		});
		mCB.setSelected(initialState);
	}
	
	public void setSelected(boolean b) {
		mCB.setSelected(b);
	}
	
	public boolean isSelected() {
		return mCB.isSelected();
	}
	
	public boolean isMinDefined() {
		return mMinTF.getText().length() > 0;
	}
	
	public boolean isMaxDefined() {
		return mMaxTF.getText().length() > 0;
	}
	
	public void setEnabled(boolean b) {
			mMinTF.setEnabled(b);
			mRangeLbl.setEnabled(b);
			mMaxTF.setEnabled(b);
		}

	/**
   * @param mMin the mMin to set
   */
  public void setMin(double min) {
  	mMinTF.setText(String.valueOf(min));
  }
  
	/**
   * @param mMax the mMax String to set
   */
  public void setMinText(String minStr) {
  	if (!(minStr.indexOf("-999") >= 0)) {
  		mMinTF.setText(minStr);
  	}
  }


	/**
   * @return the mMin
   */
  public double getMin() throws Exception {
		double min = 1.0;
		if (mMinTF.getText().length() == 0) {
			return -999.0;
		}
		
		try {
			min = Double.parseDouble(mMinTF.getText());
		}
		catch (Exception ex) {
			mMinTF.setText("");
			throw ex;
		}
	  return min;
  }

	/**
   * @param mMax the mMax to set
   */
  public void setMax(double max) {
  	mMaxTF.setText(String.valueOf(max));
  }
  
	/**
   * @param mMax the mMax String to set
   */
  public void setMaxText(String maxStr) {
  	if (!(maxStr.indexOf("-999") >= 0)) {
  		mMaxTF.setText(maxStr);
  	}
  }

	/**
   * @return the mMax
   */
  public double getMax() throws Exception {
		double max = 1.0;
		if (mMaxTF.getText().length() == 0) {
			return -999.0;
		}
		try {
			max = Double.parseDouble(mMaxTF.getText());
		}
		catch (Exception ex) {
			mMaxTF.setText("");
			throw ex;
		}
	  return max;
  }
  
  public NumericFilter getFilter() {
  	double min = -999;
  	double max = -999;
  	
  	if (isMinDefined()) {
  		try {
  			min = Double.parseDouble(mMinTF.getText());
  		} 
  		catch (Exception ex) {
  			min = -999;
  		}
  	}
  	
  	if (isMaxDefined()) {
  		try {
  			max = Double.parseDouble(mMaxTF.getText());
  		} 
  		catch (Exception ex) {
  			max = -999;
  		}
  	}
  	return new NumericFilter(min, max, isMinDefined(), isMaxDefined());
  }
}

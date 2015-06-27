/**
 * 
 */
package net.sourceforge.openforecast;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javaoceanatlas.ui.JOATimeFilter;
import javaoceanatlas.ui.JulianDayFilter;
import javaoceanatlas.ui.MonthRangeFilter;
import javaoceanatlas.ui.YearDayFilter;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author oz
 * 
 */
public class IntegerFilterPanel extends JPanel implements JOATimeFilterPanel {
	private String mRangeStr;
	private JTextField mMinTF;
	private JTextField mMaxTF;
	private JLabel mRangeLbl;
	private String mTag;
  JCheckBox mCB;

	public IntegerFilterPanel(String btnLbl, String label, String tag, int min, int max, boolean initialState) {
		mCB = new JCheckBox(btnLbl, false);
		mRangeStr = label;
		mTag = tag;
		mRangeLbl = new JLabel(" <= " + mRangeStr + " <= ");

		this.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		mMinTF = new JTextField(5);
		mMaxTF = new JTextField(5);

		if (min != -999) {
			mMinTF.setText(String.valueOf(min));
		}

		if (max != -999) {
			mMaxTF.setText(String.valueOf(max));
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
	 * @param mMin
	 *          the mMin to set
	 */
	public void setMin(int min) {
		mMinTF.setText(String.valueOf(min));
	}

	/**
	 * @param mMax
	 *          the mMax String to set
	 */
	public void setMinText(String minStr) {
		mMinTF.setText(minStr);
	}

	/**
	 * @return the mMin
	 */
	public int getMin() {
		int min = -999;
		try {
			min = Integer.parseInt(mMinTF.getText());
		}
		catch (Exception ex) {
			mMinTF.setText("");
		}
		return min;
	}

	/**
	 * @param mMax
	 *          the mMax to set
	 */
	public void setMax(int max) {
		mMaxTF.setText(String.valueOf(max));
	}

	/**
	 * @param mMax
	 *          the mMax String to set
	 */
	public void setMaxText(String mMax) {
		mMaxTF.setText(mMax);
	}

	/**
	 * @return the mMax
	 */
	public int getMax() {
		int max = -999;
		try {
			max = Integer.parseInt(mMaxTF.getText());
		}
		catch (Exception ex) {
			mMaxTF.setText("");
		}
		return max;
	}
	
	public String getTagName() {
		return mTag;
	}

	public Element getTag(Document doc) {
			Element item = doc.createElement(getTagName());
			item.setAttribute("min", String.valueOf(getMin()));
			item.setAttribute("max", String.valueOf(getMax()));
			return item;
	}

  public JOATimeFilter getFilter() {
  	int min = getMin();
  	int max = getMax();
  	boolean minDefined = min != -999;
  	boolean maxDefined = max != -999;
  	YearDayFilter ydf = new YearDayFilter(min, max, minDefined, maxDefined);
  	JulianDayFilter jdf = new JulianDayFilter(min, max, minDefined, maxDefined);
  	
  	if (mTag.equalsIgnoreCase("juliandayfilter")) {
  		return jdf;
  	}

  	return ydf;
  }
}

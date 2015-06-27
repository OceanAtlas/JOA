package net.sourceforge.openforecast;

import gov.noaa.pmel.util.GeoDate;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;
import javaoceanatlas.ui.DateRangeFilter;
import javaoceanatlas.ui.MonthRangeFilter;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author oz
 *
 */
public class MonthFilterPanel extends JPanel implements JOATimeFilterPanel {
	private JLabel mRangeLbl1, mRangeLbl2;
	JComboBox mStartMonthPopup;
	JComboBox mEndMonthPopup;
  JCheckBox mMonthFilter = new JCheckBox("Month range:", false);

	public MonthFilterPanel(int startMonth, int endMonth, boolean initialState) {
    Vector<String> mMonthChoices = new Vector<String>();
    mMonthChoices.add("January");
    mMonthChoices.add("February");
    mMonthChoices.add("March");
    mMonthChoices.add("April");
    mMonthChoices.add("May");
    mMonthChoices.add("June");
    mMonthChoices.add("July");
    mMonthChoices.add("August");
    mMonthChoices.add("September");
    mMonthChoices.add("October");
    mMonthChoices.add("November");
    mMonthChoices.add("December");
    
    mStartMonthPopup = new JComboBox(mMonthChoices);
    mStartMonthPopup.setSelectedIndex(startMonth - 1);
    
    mEndMonthPopup = new JComboBox(mMonthChoices);
    mEndMonthPopup.setSelectedIndex(endMonth - 1);
    
		mRangeLbl1 = new JLabel("Start month:");
		mRangeLbl2 = new JLabel("End month:");

		this.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		this.add(mMonthFilter);
		this.add(mRangeLbl1);
		this.add(mStartMonthPopup);
		this.add(mRangeLbl2);
		this.add(mEndMonthPopup);

		mMonthFilter.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					setEnabled(true);
				}
				else {
					setEnabled(false);
				}
			}
		});
		mMonthFilter.setSelected(initialState);
	}
	
	public void setSelected(boolean b) {
		mMonthFilter.setSelected(b);
	}
	
	public boolean isSelected() {
		return mMonthFilter.isSelected();
	}
	
	public boolean isMinDefined() {
		return true;
	}
	
	public boolean isMaxDefined() {
		return true;
	}
	
	public void setEnabled(boolean b) {
		mStartMonthPopup.setEnabled(b);
		mEndMonthPopup.setEnabled(b);
			mRangeLbl1.setEnabled(b);
			mRangeLbl2.setEnabled(b);
		}

	/**
   * @param 
   */
  public void setStartMonth(int m) {
  	mStartMonthPopup.setSelectedIndex(m - 1);
  }
  
  public int getStartMonth() {
  	return mStartMonthPopup.getSelectedIndex() + 1;
  }
  
  public void setEndMonth(int m) {
  	mEndMonthPopup.setSelectedIndex(m - 1);
  }
  
  public int getEndMonth() {
  	return mEndMonthPopup.getSelectedIndex() + 1;
  }  

  public String getTagName() {
	  return "monthfilter";
  }

	public Element getTag(Document doc) {
		try {
			Element item = doc.createElement(getTagName());
			item.setAttribute("min", String.valueOf(getStartMonth()));
			item.setAttribute("max", String.valueOf(getEndMonth()));
			return item;
		}
		catch (Exception ex) {
			return null;
		}
	}
  
  public MonthRangeFilter getFilter() {
  	int minMonth = getStartMonth();
  	int maxMonth = getEndMonth();
  	boolean minDefined = true;
  	boolean maxDefined = true;
  	MonthRangeFilter mrf = new MonthRangeFilter(minMonth, maxMonth, minDefined, maxDefined);
  	return mrf;
  }
}

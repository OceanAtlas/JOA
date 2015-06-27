
/**
 * 
 */
package net.sourceforge.openforecast;

import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;
import javaoceanatlas.ui.JOATimeFilter;
import javaoceanatlas.ui.JulianDayFilter;
import javaoceanatlas.ui.SeasonFilter;
import javaoceanatlas.ui.Seasons;
import javaoceanatlas.ui.YearDayFilter;
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
public class SeasonFilterPanel extends JPanel implements JOATimeFilterPanel {
	private JLabel mRangeLbl;
	JComboBox mSeasonPopup;
  JCheckBox mSeasonFilter = new JCheckBox("Season:", false);

	public SeasonFilterPanel(Seasons inSeasons, boolean initialState) {
    Vector<String> mSeasonChoices = new Vector<String>();
    mSeasonChoices.add("Spring");
    mSeasonChoices.add("Summer");
    mSeasonChoices.add("Autumn");
    mSeasonChoices.add("Winter");
    mSeasonPopup = new JComboBox(mSeasonChoices);
    mSeasonPopup.setSelectedIndex(inSeasons.mOrd);
    
		mRangeLbl = new JLabel("Select season:");

		this.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		this.add(mSeasonFilter);
		this.add(mRangeLbl);
		this.add(mSeasonPopup);

		mSeasonFilter.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					setEnabled(true);
				}
				else {
					setEnabled(false);
				}
			}
		});
		mSeasonFilter.setSelected(initialState);
	}
	
	public void setSelected(boolean b) {
		mSeasonFilter.setSelected(b);
	}
	
	public boolean isSelected() {
		return mSeasonFilter.isSelected();
	}
	
	public boolean isMinDefined() {
		return true;
	}
	
	public boolean isMaxDefined() {
		return true;
	}

	public void setEnabled(boolean b) {
			mSeasonPopup.setEnabled(b);
			mRangeLbl.setEnabled(b);
		}

	/**
   * @param 
   */
  public void setSeason(Seasons s) {
  	mSeasonPopup.setSelectedIndex(s.mOrd);
  }
  

  public Seasons getSeason() {
  	if (mSeasonPopup.getSelectedIndex() == 0) {
  		return Seasons.SPRING;
  	}
  	else if (mSeasonPopup.getSelectedIndex() == 1) {
  		return Seasons.SUMMER;
  	}
  	else if (mSeasonPopup.getSelectedIndex() == 2) {
  		return Seasons.AUTUMN;
  	}
  	else if (mSeasonPopup.getSelectedIndex() == 3) {
  		return Seasons.WINTER;
  	}
  	else
  		return Seasons.WINTER;
  }   

  public String getTagName() {
	  return "seasonfilter";
  }

	public Element getTag(Document doc) {
		try {
			Element item = doc.createElement(getTagName());
			item.setAttribute("ord", String.valueOf(getSeason().mOrd));
			return item;
		}
		catch (Exception ex) {
			return null;
		}
	}

  public SeasonFilter getFilter() {
  	SeasonFilter sf = new SeasonFilter(getSeason());
  	return sf;
  }
}

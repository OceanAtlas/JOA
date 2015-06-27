/**
 * 
 */
package javaoceanatlas.ui;

import gov.noaa.pmel.util.GeoDate;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import net.sourceforge.openforecast.BlankFilterPanel;
import net.sourceforge.openforecast.DateFilterPanel;
import net.sourceforge.openforecast.IntegerFilterPanel;
import net.sourceforge.openforecast.JOATimeFilterPanel;
import net.sourceforge.openforecast.MonthFilterPanel;
import net.sourceforge.openforecast.SeasonFilterPanel;
import javaoceanatlas.utility.Orientation;
import javaoceanatlas.utility.RowLayout;

/**
 * @author oz
 *
 */
public class TimeFilterHolderPanel extends JPanel {
	private JComboBox mFilterChoices;
	private JPanel mBtnPanel;
	private FilterUIHolder mParent;
	private boolean mIgnore = false;
	
	public TimeFilterHolderPanel(FilterUIHolder parent, JPanel btns, boolean addBlank) {
		mParent = parent;
		mBtnPanel = btns;
    this.setLayout(new RowLayout(Orientation.LEFT, Orientation.CENTER, 5));

		final Vector<String> filterNames = new Vector<String>();
		filterNames.add("Select Filter");
		filterNames.add("Date Range:");
		filterNames.add("Month Range:");
		filterNames.add("Season:");
		filterNames.add("Year Day:");
		filterNames.add("Julian Day:");
		mFilterChoices = new JComboBox(filterNames);

		mFilterChoices.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (mIgnore) {
					mIgnore = false;
					return;
				}
        JComboBox jcb = (JComboBox)e.getSource();
        if (((String)(jcb.getSelectedItem())).equalsIgnoreCase("Date Range:")) {
        	addComponent(new DateFilterPanel("Date"));
        }
        else if (((String)(jcb.getSelectedItem())).equalsIgnoreCase("Month Range:")) {
        	addComponent(new MonthFilterPanel(1, 12, false));
        }
        else if (((String)(jcb.getSelectedItem())).equalsIgnoreCase("Season:")) {
        	addComponent(new SeasonFilterPanel(Seasons.SPRING, false));
        }
        else if (((String)(jcb.getSelectedItem())).equalsIgnoreCase("Year Day:")) {
        	addComponent(new IntegerFilterPanel("Year Day:", "Year Day", "yeardayfilter", 1, 364, false));
        }
        else if (((String)(jcb.getSelectedItem())).equalsIgnoreCase("Julian Day:")) {
        	addComponent(new IntegerFilterPanel("Julian Day:", "Julian Day", "juliandayfilter", 1, 364, false));
        }
      }
		});
		
		this.add(mFilterChoices);
		if (addBlank) {
			BlankFilterPanel bfp = new BlankFilterPanel();
			this.add(bfp);
		}
	}
	
	public Element getTag(Document doc) {
		return ((JOATimeFilterPanel)(this.getComponent(1))).getTag(doc);
	}
	
	public void addComponent(JOATimeFilterPanel c) {
		this.removeAll();
		this.add(mFilterChoices);
		this.add((JPanel)c);
		this.add(mBtnPanel);
		mParent.updateUIAfterFilterChange();
	}
	
	public void setMenu(int item) {
		mIgnore = true;
		mFilterChoices.setSelectedIndex(item);
	}

	public JOATimeFilterPanel getFilterPanel() {
		return (JOATimeFilterPanel)this.getComponent(1);
	}
}

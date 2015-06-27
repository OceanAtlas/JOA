/**
 * 
 */
package net.sourceforge.openforecast;

import gov.noaa.pmel.util.GeoDate;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import javaoceanatlas.ui.DateRangeFilter;
import javaoceanatlas.utility.JOAFormulas;
import javaoceanatlas.utility.DateTimeGetter;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author oz
 *
 */
public class DateFilterPanel extends JPanel implements JOATimeFilterPanel {
	private String mRangeStr;
	private JSpinner mStartSpinner = null;
	private JSpinner mEndSpinner = null;
	private JLabel mRangeLbl;
	private boolean mMinDefined = false;
	private boolean mMaxDefined = false;
	private JButton mSetStartDate = new javax.swing.JButton("...");
	private JButton mSetEndDate = new javax.swing.JButton("...");
	JCheckBox mDateFilter = new JCheckBox("Date range", false);
	
	public DateFilterPanel(String label) {
		this(label, new GeoDate(), new GeoDate(), false);
	}
	
	public void setSelected(boolean b) {
		mDateFilter.setSelected(b);
	}
	
	public boolean isSelected() {
		return mDateFilter.isSelected();
	}
	
	public DateFilterPanel(String label, GeoDate stDate, GeoDate endDate, boolean initialState) {
		mRangeStr = label;
		mRangeLbl = new JLabel(" <= " + mRangeStr + " <= ");

		this.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		add(mDateFilter);

		mDateFilter.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					setEnabled(true);
				}
				else {
					setEnabled(false);
				}
			}
		});
		mDateFilter.setSelected(initialState);

		GeoDate maxDate;
		GeoDate earliestDate;
		GeoDate currentDate;
		
		if (stDate != null) {
			mMinDefined = true;
			 currentDate = new GeoDate(stDate);
		}
		else {
			 currentDate = new GeoDate();
		}

		earliestDate = new GeoDate(currentDate);
		earliestDate.decrement(100, GeoDate.YEARS);
		maxDate = new GeoDate();
		maxDate.increment(100, GeoDate.YEARS);

		final SpinnerDateModel mStartDateModel = new SpinnerDateModel(stDate, earliestDate, maxDate, Calendar.DAY_OF_YEAR);
		mStartSpinner = new JSpinner(mStartDateModel);
		JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(mStartSpinner, "yyyy-MM-dd");
		mStartSpinner.setEditor(dateEditor);
		
		final SpinnerDateModel mEndDateModel = new SpinnerDateModel(endDate, earliestDate, maxDate, Calendar.DAY_OF_YEAR);
		mEndSpinner = new JSpinner(mEndDateModel);
		JSpinner.DateEditor edateEditor = new JSpinner.DateEditor(mEndSpinner, "yyyy-MM-dd");
		mEndSpinner.setEditor(edateEditor);
		mSetStartDate.putClientProperty("JButton.buttonType", "square");
		mSetEndDate.putClientProperty("JButton.buttonType", "square");

		this.add(mStartSpinner);
		this.add(mSetStartDate);
		this.add(mRangeLbl);
		this.add(mEndSpinner);
		this.add(mSetEndDate);

		mSetStartDate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final DateTimeGetter dateTimeGetter = new DateTimeGetter(true, true);
				SimpleDateFormat sdfGMT = new SimpleDateFormat("yyyy-MM-dd");
				dateTimeGetter.setOutputDateFormatter(sdfGMT);
				dateTimeGetter.setTimeZone(TimeZone.getTimeZone("GMT"));
				dateTimeGetter.setHideTime(false);
				dateTimeGetter.setDate(mStartDateModel.getDate());
				JButton jdtgOK = dateTimeGetter.getOkButn();

				final JDialog jdtgDialog = new JDialog();
				jdtgDialog.getRootPane().setDefaultButton(jdtgOK);
				jdtgDialog.setTitle("Set Start Date");
				jdtgDialog.getContentPane().add(dateTimeGetter);
	        	jdtgDialog.pack();
				Rectangle dBounds = jdtgDialog.getBounds();
				java.awt.Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
				int x = sd.width/2 - dBounds.width/2;
				int y = sd.height/2 - dBounds.height/2;
				jdtgDialog.setLocation(x, y);
				jdtgDialog.setVisible(true);
				
				JButton jdtgCancel = dateTimeGetter.getCancelButn();

				jdtgOK.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						setMin(new GeoDate(dateTimeGetter.getDate().getTime()));
						jdtgDialog.dispose();
					}
				});

				jdtgCancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						jdtgDialog.dispose();
					}
				});	
			}
		});

		mSetEndDate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final DateTimeGetter dateTimeGetter = new DateTimeGetter(true, true);
				SimpleDateFormat sdfGMT = new SimpleDateFormat("yyyy-MM-dd");
				dateTimeGetter.setOutputDateFormatter(sdfGMT);
				dateTimeGetter.setTimeZone(TimeZone.getTimeZone("GMT"));
				dateTimeGetter.setDate(mEndDateModel.getDate());
				dateTimeGetter.setHideTime(false);
				JButton jdtgOK = dateTimeGetter.getOkButn();
				
				final JDialog jdtgDialog = new JDialog();
				jdtgDialog.getRootPane().setDefaultButton(jdtgOK);
				jdtgDialog.setTitle("Set End Date");
				jdtgDialog.getContentPane().add(dateTimeGetter);
	        	jdtgDialog.pack();
				Rectangle dBounds = jdtgDialog.getBounds();
				java.awt.Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
				int x = sd.width/2 - dBounds.width/2;
				int y = sd.height/2 - dBounds.height/2;
				jdtgDialog.setLocation(x, y);
				jdtgDialog.setVisible(true);
				JButton jdtgCancel = dateTimeGetter.getCancelButn();

				jdtgOK.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						setMax(new GeoDate(dateTimeGetter.getDate().getTime()));
						jdtgDialog.dispose();
					}
				});			

				jdtgCancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						// set the value
						jdtgDialog.dispose();
					}
				});
			}
		});
	}
	
	public boolean isMinDefined() {
		return mMinDefined;
	}
	
	public boolean isMaxDefined() {
		return mMaxDefined;
	}

	public void setEnabled(boolean b) {
			mStartSpinner.setEnabled(b);
			mRangeLbl.setEnabled(b);
			mEndSpinner.setEnabled(b);
		}


	/**
   * @param mMin the mMin to set
   */
  public void setMin(GeoDate min) {
  	mStartSpinner.setValue(min);
  }
  

	/**
   * @return the mMin
   */
  public long getMin() {
  	return ((Date)mStartSpinner.getValue()).getTime();
  }

	/**
   * @param mMax the mMax to set
   */
  public void setMax(GeoDate max) {
  	mEndSpinner.setValue(max);
  }
  
	/**
   * @return the mMax
   */
  public long getMax() {
  	return ((Date)mEndSpinner.getValue()).getTime();
  }
  

	public String getTagName() {
		return "datefilter";
	}

  public Element getTag(Document doc) {
		Element item = doc.createElement(getTagName());
		item.setAttribute("min", String.valueOf(getMin()));
		item.setAttribute("max", String.valueOf(getMax()));
	  
	  return item;
  }
  
  public DateRangeFilter getFilter() {
  	GeoDate minDate = new GeoDate(getMin());
  	GeoDate maxDate = new GeoDate(getMax());
  	boolean minDefined = true;
  	boolean maxDefined = true;
  	DateRangeFilter drf = new DateRangeFilter( minDate, maxDate, minDefined, maxDefined);
  	return drf;
  }
}

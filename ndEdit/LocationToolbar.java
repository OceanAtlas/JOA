/*
 * $Id: LocationToolbar.java,v 1.11 2005/02/15 18:31:09 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.beans.*;
import java.text.*;
import gov.noaa.pmel.text.*;

 /**
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 */
public class LocationToolbar extends JToolBar implements PropertyChangeListener {
	private JTextField latTF = null;
	private JTextField lonTF = null;
	private JTextField zTF = null;
	private JTextField tTF = null;;
	private JLabel invTF = null;
	private JTextField latCtrTF = null;
	private JTextField lonCtrTF = null;
	private JTextField zCtrTF = null;
	private JTextField tCtrTF = null;
	protected DecimalFormat depthDM = new DecimalFormat();
	protected int timeDisplayFormat = Constants.DATE_TIME_DISPLAY;
	protected int geoDisplayFormat = Constants.DEC_MINUTES_GEO_DISPLAY;
	protected double depth = Float.NaN, prevDepth = Float.NaN;
	protected double lat = Float.NaN, prevLat = Float.NaN;
	protected double lon = Float.NaN, prevLon = Float.NaN;
   	protected Date date = null, prevDate = null;
	protected double ctrDepth = Float.NaN, prevCtrDepth = Float.NaN;
	protected double ctrLat = Float.NaN, prevCtrLat = Float.NaN;
	protected double ctrLon = Float.NaN, prevCtrLon = Float.NaN;
   	protected Date ctrDate = null, prevCtrDate = null;
	protected LatitudeFormat latDM;
	protected LongitudeFormat lonDM;
	protected String latFormatString = "###.###N;###.###S";
	protected String lonFormatString = "###.###E;###.###W";
	protected String formatString = "###.###;###.###";
	protected SimpleDateFormat sdfGMT = new SimpleDateFormat("dd MMM yyyy");
	protected JLabel progressLbl;
	protected JLabel inventoryLbl;
	static int progressState = 0;
	Icon[] mChasingArrows;
	
	// constructor
	public LocationToolbar() {
		//mChasingArrows = new Icon[] {
		//	new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/arrows1.gif")),
		//	new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/arrows2.gif")),
		//	new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/arrows3.gif")),
		//	new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/arrows4.gif")),
		//};
		
		// main toolbar (top)
		//this.setBackground(java.awt.Color.lightGray);
		//this.setBounds(15,11,379,43);
		//this.setSize(new Dimension(379,43));
		//this.setPreferredSize(new Dimension(379,43));
      
		JPanel fldPanel = new JPanel();
		fldPanel.setLayout(new FlowLayout(3));
		//fldPanel.setBackground(new java.awt.Color(200,200,200));
		
		JPanel ctrFldPanel = new JPanel();
		ctrFldPanel.setLayout(new FlowLayout(3));
		//ctrFldPanel.setBackground(new java.awt.Color(200,200,200));

		ResourceBundle b = ResourceBundle.getBundle("ndEdit.NdEditResources");
		boolean windows = false;
		Font tff = BeanFonts.textFieldFont;
		if (-1 != System.getProperty("os.name").indexOf("Windows")) 
			windows = true;
		if (windows) 
			tff = new Font("SansSerif", Font.PLAIN, 10);
			
		JLabel inventoryLbl = new JLabel(b.getString("kInvLbl"));
			
		JLabel wl = new JLabel(b.getString("kLat"));
		wl.setFont(tff);
      	wl.setForeground(java.awt.Color.black);
		fldPanel.add(wl);
		latTF = new JTextField(8);
		latTF.setFont(tff);
		latTF.setEditable(false);
		fldPanel.add(latTF);
			
		wl = new JLabel(b.getString("kLon"));
		wl.setFont(tff);
      	wl.setForeground(java.awt.Color.black);
		fldPanel.add(wl);
		lonTF = new JTextField(8);
		lonTF.setFont(tff);
		lonTF.setEditable(false);
		fldPanel.add(lonTF);
			
		wl = new JLabel(b.getString("kDepth:"));
		wl.setFont(tff);
      	wl.setForeground(java.awt.Color.black);
		fldPanel.add(wl);
		zTF = new JTextField(8);
		zTF.setFont(tff);
		zTF.setEditable(false);
		fldPanel.add(zTF);
			
		wl = new JLabel(b.getString("kDate:"));
		wl.setFont(tff);
      	wl.setForeground(java.awt.Color.black);
		fldPanel.add(wl);
		tTF = new JTextField(8);
		tTF.setFont(tff);
		tTF.setEditable(false);
		fldPanel.add(tTF);
			
		inventoryLbl = new JLabel(b.getString("kInvLbl"));
		inventoryLbl.setFont(tff);
      	inventoryLbl.setForeground(java.awt.Color.black);
		fldPanel.add(inventoryLbl);
		invTF = new JLabel("0 / 0 / 0");
		invTF.setFont(tff);
		fldPanel.add(invTF);
		
     	//if (null != mChasingArrows[0]) {
		//	progressLbl = new JLabel(mChasingArrows[0]);
			//fldPanel.add(progressLbl);
     	//}
		
		this.add(fldPanel);
		//this.add(ctrFldPanel);
		
      	createTimeFormatter(timeDisplayFormat);
      	createLatFormatter(geoDisplayFormat);
      	createLonFormatter(geoDisplayFormat);
      	createDepthFormatter();

		this.invalidate();
		this.validate();
	}

   public void setTimeDisplayFormat(int timeDisplayFormat) {
      this.timeDisplayFormat = timeDisplayFormat;
      createTimeFormatter(timeDisplayFormat);
      if (this.date != null) {
      	if (timeDisplayFormat == Constants.SEASON_TIME_DISPLAY) {
      		tTF.setText(new String(sdfGMT.format(this.date, null, null)));
      		//tCtrTF.setText(new String(sdfGMT.format(this.ctrDate, null, null)));
      	}
      	else {
      		tTF.setText(sdfGMT.format(this.date));
      		//tCtrTF.setText(sdfGMT.format(this.ctrDate));
      	}
      }
   }
   
   public void setGeoDisplayFormat(int geoDisplayFormat) {
      this.geoDisplayFormat = geoDisplayFormat;
      createLatFormatter(geoDisplayFormat);
      if (latTF != null) {
      	latTF.setText(latDM.format((float)this.lat)); 
      	latCtrTF.setText(latDM.format((float)this.ctrLat)); 
      }
      createLonFormatter(geoDisplayFormat);
      if (lonTF != null) {
      	lonTF.setText(lonDM.format((float)this.lon)); 
      	lonCtrTF.setText(lonDM.format((float)this.ctrLon)); 
      }
   }

   public void createTimeFormatter(int timeDisplayFormat) {
      if (timeDisplayFormat == Constants.DATE_TIME_DISPLAY) {
        sdfGMT = new SimpleDateFormat("yyyy-MM-dd");
      }
      else if (timeDisplayFormat == Constants.YEAR_DAY_TIME_DISPLAY) {
        sdfGMT = new SimpleDateFormat("yyyy-DDD");
      }
      else if (timeDisplayFormat == Constants.MONTH_TIME_DISPLAY) {
        sdfGMT = new SimpleDateFormat("MMMMMMMMMMMMMMM");
      }
      else if (timeDisplayFormat == Constants.SEASON_TIME_DISPLAY) {
        sdfGMT = new SeasonFormatter();
      }
      sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	public void createLatFormatter(int geoDisplayFormat) {
		if (geoDisplayFormat == Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
			latDM = new LatitudeFormat();
		}
		else {
			latDM = new LatitudeFormat(formatString);
		}
	}
	
	public void createLonFormatter(int geoDisplayFormat) {
		if (geoDisplayFormat == Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
			lonDM = new LongitudeFormat();
		}
		else {
			lonDM = new LongitudeFormat(formatString);
		}
	}

   public void createDepthFormatter() {
      depthDM = new DecimalFormat(formatString);
   }

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals("GeoDisplayFormat")) {
			geoDisplayFormat = ((Integer) e.getNewValue()).intValue();
			createLatFormatter(geoDisplayFormat);
			if (!Double.isNaN(this.lat)) {
				latTF.setText(latDM.format((float)this.lat)); 
			}
			if (!Double.isNaN(this.ctrLat)) {
				latCtrTF.setText(latDM.format((float)this.ctrLat)); 
			}
			createLonFormatter(geoDisplayFormat);
			if (!Double.isNaN(this.lon)) 
				lonTF.setText(lonDM.format((float)correctLongitude(this.lon)));
			if (!Double.isNaN(this.ctrLon)) 
				lonCtrTF.setText(lonDM.format((float)correctLongitude(this.ctrLon)));
		}
		else if (e.getPropertyName().equals("TimeDisplayFormat")) {
			timeDisplayFormat = ((Integer)e.getNewValue()).intValue();
			setTimeDisplayFormat(timeDisplayFormat);
	      if (this.date != null) {
	      	if (timeDisplayFormat == Constants.SEASON_TIME_DISPLAY)
	      		tTF.setText(new String(sdfGMT.format(this.date, null, null)));
	      	else
	      		tTF.setText(sdfGMT.format(this.date));
	      }
	      if (this.ctrDate != null) {
	      	if (timeDisplayFormat == Constants.SEASON_TIME_DISPLAY)
	      		tCtrTF.setText(new String(sdfGMT.format(this.ctrDate, null, null)));
	      	else
	      		tCtrTF.setText(sdfGMT.format(this.ctrDate));
	      }
		}
	}
	
   public void setLon(double lon) {
		if (lon != prevLon) {
			if (!Double.isNaN(lon)) 
				lonTF.setText(lonDM.format((float)correctLongitude(lon)));
			this.lon = lon;
			this.prevLon = lon;
		}
   }
	
	public void setLat(double lat) {
		if (lat != prevLat) {
			if (!Double.isNaN(lat)) 
				latTF.setText(latDM.format((float)lat));
			this.lat = lat;
			this.prevLat = lat;
		}
	}

	public void setDate(Date indate) {
		this.date = indate;
		if (this.date != null) {
			if (timeDisplayFormat == Constants.SEASON_TIME_DISPLAY)
				tTF.setText(new String(sdfGMT.format(this.date, null, null)));
			else
				tTF.setText(sdfGMT.format(this.date));
		}
	}
	
	public void setDepth(double depth) {
		if (!Double.isNaN(depth)) 
			zTF.setText(depthDM.format(depth));
		this.depth = depth;
		this.prevDepth = depth;
	}
	
   public void setCtrLon(double lon) {
		if (lon != prevCtrLon) {
			if (!Double.isNaN(lon)) 
				lonCtrTF.setText(lonDM.format((float)correctLongitude(lon)));
			this.ctrLon = lon;
			this.prevCtrLon = lon;
		}
   }
	
	public void setCtrLat(double lat) {
		if (lat != prevCtrLat) {
			if (!Double.isNaN(lat)) 
				latCtrTF.setText(latDM.format((float)lat));
			this.ctrLat = lat;
			this.prevCtrLat = lat;
		}
	}

	public void setCtrDate(Date indate) {
		this.ctrDate = indate;
		if (this.ctrDate != null) {
			if (timeDisplayFormat == Constants.SEASON_TIME_DISPLAY)
				tCtrTF.setText(new String(sdfGMT.format(this.ctrDate, null, null)));
			else
				tCtrTF.setText(sdfGMT.format(this.ctrDate));
		}
	}
	
	public void setCtrDepth(double depth) {
		if (!Double.isNaN(depth)) 
			zCtrTF.setText(depthDM.format(depth));
		this.ctrDepth = depth;
		this.prevCtrDepth = depth;
	}

	private double correctLongitude(double lon) {
		if (lon > 180)
			return lon - Constants.LONGITUDE_CONV_FACTOR;
		return lon;
	}
	
	public void setLocation(double x, double y, double z, double t) {
		if (!Double.isNaN(x))
			setLon(x);
		else
			lonTF.setText("");
			
		if (!Double.isNaN(y))
			setLat(y);
		else
			latTF.setText("");
			
		if (!Double.isNaN(z))
			setDepth(z);
		else
			zTF.setText("");
			
		if (!Double.isNaN(t))
			setDate(new Date((long)t));
		else
			tTF.setText("");
	}
	
	public void setCtrLocation(double x, double y, double z, double t) {
		if (!Double.isNaN(x))
			setCtrLon(x);
		else
			lonCtrTF.setText("");
			
		if (!Double.isNaN(y))
			setCtrLat(y);
		else
			latCtrTF.setText("");
			
		if (!Double.isNaN(z))
			setCtrDepth(z);
		else
			zCtrTF.setText("");
			
		if (!Double.isNaN(t))
			setCtrDate(new Date((long)t));
		else
			tCtrTF.setText("");
	}
	
	public void setInventory(long num, long vis, long sel) {
		invTF.setText(num + " / " + vis + " / " + sel);
	}
	
	public void updateProgress() {
		progressState++;
		if (progressState > 3)
			progressState = 0;
			
		//progressLbl.setIcon(mChasingArrows[progressState]);
	}

}

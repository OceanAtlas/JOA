/*
 * $Id: LocationToolbar.java,v 1.3 2005/06/17 18:08:54 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.text.*;
import gov.noaa.pmel.text.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.events.*;

 /**
 * @author  oz 
 * @version 1.0 01/13/00
 */
@SuppressWarnings("serial")
public class LocationToolbar extends JPanel implements PrefsChangedListener{
	private JOAJTextField latTF = null;
	private JOAJTextField lonTF = null;
	private JTextField zTF = null;
	private JTextField tTF = null;
	protected DecimalFormat depthDM = new DecimalFormat();
	protected int timeDisplayFormat = JOAConstants.DATE_TIME_DISPLAY;
	protected int geoDisplayFormat = JOAConstants.DEC_MINUTES_GEO_DISPLAY;
	protected double depth = Double.NaN, prevDepth = Double.NaN;
	protected double lat = Double.NaN, prevLat = Double.NaN;
	protected double lon = Double.NaN, prevLon = Double.NaN;
   	protected Date date = null, prevDate = null;
	protected LatitudeFormat latDM;
	protected LongitudeFormat lonDM;
	protected String formatString = "###.###;###.###";
	protected SimpleDateFormat sdfGMT = new SimpleDateFormat("dd MMM yyyy");
	
	// constructor
	public LocationToolbar() {
		// main toolbar (top)
      
		JPanel fldPanel = new JPanel();
		fldPanel.setLayout(new FlowLayout(3));

		ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
		Font tff = new Font("SansSerif", Font.PLAIN, 10);
			
		JOAJLabel wl = new JOAJLabel(b.getString("kLat"));
		wl.setFont(tff);
      	wl.setForeground(java.awt.Color.black);
		fldPanel.add(wl);
		latTF = new JOAJTextField(8);
		latTF.setFocusTraversable(false);
		latTF.setFont(tff);
		fldPanel.add(latTF);
			
		wl = new JOAJLabel(b.getString("kLon"));
		wl.setFont(tff);
      	wl.setForeground(java.awt.Color.black);
		fldPanel.add(wl);
		lonTF = new JOAJTextField(8);
		lonTF.setFocusTraversable(false);
		lonTF.setFont(tff);
		fldPanel.add(lonTF);
			
		this.add(fldPanel);
		
      	createTimeFormatter(timeDisplayFormat);
      	createLatFormatter();
      	createLonFormatter();
      	createDepthFormatter();

		this.invalidate();
		this.validate();
	}

   public void setTimeDisplayFormat(int timeDisplayFormat) {
      this.timeDisplayFormat = timeDisplayFormat;
      createTimeFormatter(timeDisplayFormat);
      if (this.date != null) {
      	if (timeDisplayFormat == JOAConstants.SEASON_TIME_DISPLAY)
      		tTF.setText(new String(sdfGMT.format(this.date, null, null)));
      	else
      		tTF.setText(sdfGMT.format(this.date));
      }
   }
   
   public void createTimeFormatter(int timeDisplayFormat) {
      if (timeDisplayFormat == JOAConstants.DATE_TIME_DISPLAY) {
        sdfGMT = new SimpleDateFormat("yyyy-MM-dd");
      }
      else if (timeDisplayFormat == JOAConstants.YEAR_DAY_TIME_DISPLAY) {
        sdfGMT = new SimpleDateFormat("yyyy-DDD");
      }
      else if (timeDisplayFormat == JOAConstants.MONTH_TIME_DISPLAY) {
        sdfGMT = new SimpleDateFormat("MMMMMMMMMMMMMMM");
      }
     // else if (timeDisplayFormat == JOAConstants.SEASON_TIME_DISPLAY) {
      //  sdfGMT = new SeasonFormatter();
     // }
      sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	public void createLatFormatter() {
		if (JOAConstants.DEFAULT_POSITION_FORMAT == JOAConstants.DEG_MINUTES_SEC_GEO_DISPLAY) {
			latDM = new LatitudeFormat();
		}
		else {
			latDM = new LatitudeFormat(JOAConstants.latFormatString);
		}
	}
	
	public void createLonFormatter() {
		if (JOAConstants.DEFAULT_POSITION_FORMAT == JOAConstants.DEG_MINUTES_SEC_GEO_DISPLAY) {
			lonDM = new LongitudeFormat();
		}
		else {
			lonDM = new LongitudeFormat(JOAConstants.lonFormatString);
		}
	}

   public void createDepthFormatter() {
      depthDM = new DecimalFormat(formatString);
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
			if (timeDisplayFormat == JOAConstants.SEASON_TIME_DISPLAY)
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

	private double correctLongitude(double lon) {
		if (lon > 180)
			return lon;// - JOAConstants.LONGITUDE_CONV_FACTOR;
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
			
		/*if (!Double.isNaN(z))
			setDepth(z);
		else
			zTF.setText("");
			
		if (!Double.isNaN(t))
			setDate(new Date((long)t));
		else
			tTF.setText("");*/
	}
	
	public void prefsChanged(PrefsChangedEvent evt) {
		createLatFormatter();
		createLonFormatter();
	}
}

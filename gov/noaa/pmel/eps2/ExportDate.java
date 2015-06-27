/*
 * $Id: ExportDate.java,v 1.5 2005/10/18 23:44:20 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */
package gov.noaa.pmel.eps2;

import gov.noaa.pmel.util.*;
import java.util.*;

/**
 * <code>ExportVariable</code> Stores variable information written to XML files.
 *
 *
 * @author oz
 * @version 1.0
 */

public class ExportDate {
	int mYear;
	int mMonth;
	int mDay;
	int mHour;
	int mMin;
	double mSecs;
	String mLocation = null;
		
	public ExportDate(GeoDate gd, String loc) {
		mYear = gd.getGMTYear();
		mMonth = gd.getGMTMonth();
		mDay = gd.getGMTDay();
		mHour = gd.getGMTHours();
		mMin = gd.getGMTMinutes();
		mSecs = gd.getGMTSeconds();
		if (loc != null)
			mLocation = new String(loc);
		else
			mLocation = "point";
	}
		
	public ExportDate(int yr, int mnth, int day, int hr, int min, double secs, String loc) {
			mYear = yr;
			mMonth = mnth;
			mDay = day;
			mHour = hr;
			mMin = min;
			mSecs = secs;
		if (loc != null)
			mLocation = new String(loc);
		else
			mLocation = "point";
	}
	
	public String getLocation() {
		return mLocation;
	}
	
	public int getYear() {
		return mYear;
	}
		
	public void setYear(int yr) {
		mYear = yr;
	}
	
	public int getMonth() {
		return mMonth;
	}
		
	public void setMonth(int mn) {
		mMonth = mn;
	}
	
	public int getDay() {
		return mDay;
	}
		
	public void setDay(int dy) {
		mDay = dy;
	}
	
	public int getHour() {
		return mHour;
	}
		
	public void setHour(int hr) {
		mHour = hr;
	}
	
	public int getMinutes() {
		return mMin;
	}
		
	public void setMinutes(int min) {
		mMin = min;
	}
	
	public double getSecs() {
		return mSecs;
	}
		
	public void setSecs(double secs) {
		mSecs = secs;
	}
		
	public boolean isStart() {
		return mLocation.equalsIgnoreCase("start");
	}
		
	public boolean isEnd() {
		return mLocation.equalsIgnoreCase("end");
	}
	
	public GeoDate getGeoDate() {
		try {	
			return new GeoDate(this.getMonth(), this.getDay(), this.getYear(), this.getHour(), this.getMinutes(), (int)this.getSecs(), 0);
		}
		catch (Exception ex) {
			return new GeoDate();
		}
	}
	
	public void setDate(GeoDate gd) {
		mYear = gd.getGMTYear();
		mMonth = gd.getGMTMonth();
		mDay = gd.getGMTDay();
		mHour = gd.getGMTHours();
		mMin = gd.getGMTMinutes();
		mSecs = gd.getGMTSeconds();
	}
		
	public void setLocation(String loc) {
		if (loc != null)
			mLocation = new String(loc);
		else
			mLocation = "point";
	}
	
	public String toString(int tabs) {
		String retVal = "";
		for (int i=0; i<tabs; i++)
			retVal += "\t";
		return retVal +  mYear + " " + mMonth + " " + " " + mDay + " " + mHour + " " + mMin + " " + mSecs + " " + mLocation;
	}
}

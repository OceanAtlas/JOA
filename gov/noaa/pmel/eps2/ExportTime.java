/*
 * $Id: ExportTime.java,v 1.4 2005/04/05 21:44:05 oz Exp $
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

public class ExportTime {
	long mTime;
	String mLocation = null;
	String mUnits = null;
	
	public ExportTime(long t) {
		this(t, "point", "days since the beginning of time");
	}
	
	public ExportTime(GeoDate gd, String loc, String units) {
		mTime = gd.getTime();
		if (loc != null)
			mLocation = new String(loc);
		else
			mLocation = "point";
		if (units != null)
			mUnits = new String(units);
		else
			mUnits = "days since the beginning of time";
	}
		
	public ExportTime(long t, String loc, String units) {
		mTime = t;
		if (loc != null)
			mLocation = new String(loc);
		else
			mLocation = "point";
		if (units != null)
			mUnits = new String(units);
		else
			mUnits = "days since the beginning of time";
	}
	
	public String getLocation() {
		return mLocation;
	}
	
	public String getTimeUnits() {
		return mUnits;
	}
		
	public long getT() {
		return mTime;
	}
		
	public void setT(long t) {
		mTime = t;
	}
		
	public void setT(GeoDate gd) {
		mTime = gd.getTime();
	}
		
	public boolean isStart() {
		return mLocation.equalsIgnoreCase("start");
	}
		
	public boolean isEnd() {
		return mLocation.equalsIgnoreCase("end");
	}
	
	public GeoDate getGeoDate() {
		//TODO: need to turn a time into a GeoDate
		return new GeoDate();
	}
	
	public void setTimeUnits(String units) {
		if (units != null)
			mUnits = new String(units);
		else
			mUnits = "days since the beginning of time";
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
		return retVal +  mTime + " " + mUnits + " " + mLocation;
	}
}

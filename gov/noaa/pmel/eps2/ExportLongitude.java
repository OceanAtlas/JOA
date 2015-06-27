/*
 * $Id: ExportLongitude.java,v 1.4 2005/04/05 21:44:05 oz Exp $
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

public class ExportLongitude {
	double mLon;
	String mLocation = null;
	String mUnits = null;
	
	public ExportLongitude(double lon) {
		this(lon, "point", "degrees_east");
	}
	
	public ExportLongitude(double lon, String loc) {
		this(lon, loc, "degrees_east");
	}
		
	public ExportLongitude(double lon, String loc, String units) {
		mLon = lon;
		if (loc != null)
			mLocation = new String(loc);
		else
			mLocation = "point";
		if (units != null)
			mUnits = new String(units);
		else
			mUnits = "degrees_east";
	}
	
	public String getLocation() {
		return mLocation;
	}
	
	public String getLonUnits() {
		return mUnits;
	}
	
	public double getLon() {
		return mLon;
	}
		
	public void setLon(double lon) {
		mLon = lon;
	}
	
	public void setLonUnits(String units) {
		if (units != null)
			mUnits = new String(units);
		else
			mUnits = "degrees_east";
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
		return retVal + EPS_Util.formatDouble(mLon, 5, false) + " " + mUnits + " " + mLocation;
	}
}

/*
 * $Id: ExportLatitude.java,v 1.3 2005/04/05 21:44:05 oz Exp $
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

public class ExportLatitude {
	double mLat;
	String mLocation = null;
	String mUnits = null;
	
	public ExportLatitude(double lat) {
		this(lat, "point", "degrees_north");
	}
	
	public ExportLatitude(double lat, String loc) {
		this(lat, loc, "degrees_north");
	}
		
	public ExportLatitude(double lat, String loc, String units) {
		mLat = lat;
		if (loc != null)
			mLocation = new String(loc);
		else
			mLocation = "point";
		if (units != null)
			mUnits = new String(units);
		else
			mUnits = "degrees_north";
	}
	
	public String getLocation() {
		return mLocation;
	}
	
	public String getLatUnits() {
		return mUnits;
	}
	
	public double getLat() {
		return mLat;
	}
		
	public void setLat(double lat) {
		mLat = lat;
	}
	
	public void setLatUnits(String units) {
		if (units != null)
			mUnits = new String(units);
		else
			mUnits = "degrees_north";
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
		return retVal + EPS_Util.formatDouble(mLat, 5, false) + " " + mUnits + " " + mLocation;
	}
}

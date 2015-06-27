/*
 * $Id: ExportVertical.java,v 1.5 2005/04/05 21:44:05 oz Exp $
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

public class ExportVertical {
	double mZ;
	String mLocation = null;
	String mUnits = null;
	String mPositive = null;
	
	public ExportVertical(double z) {
		this(z, "point", "m", "down");
	}
	
	public ExportVertical(double z, String units) {
		this(z, "point", units, "down");
	}
	
	public ExportVertical(double z, String loc, String units) {
		this(z, loc, units, "down");
	}
		
	public ExportVertical(double z, String loc, String units, String pos) {
		mZ = z;
		if (loc != null)
			mLocation = new String(loc);
		else
			mLocation = "point";
		if (units != null)
			mUnits = new String(units);
		else
			mUnits = "m";
		if (pos != null)
			mPositive = new String(pos);
		else
			mPositive = "down";
	}
	
	public String getLocation() {
		return mLocation;
	}
	
	public String getVerticalUnits() {
		return mUnits;
	}
	
	public String getPositive() {
		return mPositive;
	}
	
	public double getZ() {
		return mZ;
	}
		
	public void setZ(double z) {
		mZ = z;
	}
	
	public boolean isTop() {
		return mLocation.equalsIgnoreCase("top");
	}
	
	public boolean isBottom() {
		return mLocation.equalsIgnoreCase("bottom");
	}
	
	public void setVerticalUnits(String units) {
		if (units != null)
			mUnits = new String(units);
		else
			mUnits = "m";
	}
	
	public void setLocation(String loc) {
		if (loc != null)
			mLocation = new String(loc);
		else
			mLocation = "point";
	}
	
	public void setPositive(String pos) {
		if (pos != null)
			mPositive = new String(pos);
		else
			mPositive = "down";
	}
	
	public String toString(int tabs) {
		String retVal = "";
		for (int i=0; i<tabs; i++)
			retVal += "\t";
		return retVal + EPS_Util.formatDouble(mZ, 5, false) + " " + mUnits + " " + mLocation + " " + mPositive;
	}
}

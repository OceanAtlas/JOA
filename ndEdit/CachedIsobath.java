/*
 * $Id: CachedIsobath.java,v 1.4 2005/02/15 18:31:08 oz Exp $
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

import java.awt.event.*;
import java.awt.*;
import gov.noaa.pmel.text.*;
import gov.noaa.pmel.swing.*;
import java.beans.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import javax.swing.border.*;

 /**
 *
 *
 * @author  oz 
 * @version 1.0 01/13/00
 */

public class CachedIsobath {
	private double[] mLats;
	private double[] mLons;
	
	public CachedIsobath(double[] inLats, double[] inLons) {
		mLats = inLats;
		mLons = inLons;
	}
	
	public double[] getLats() {
		return mLats;
	}
	
	public double[] getLons() {
		return mLons;
	}
}
/*
 * $Id: Triplet.java,v 1.4 2005/02/15 18:31:11 oz Exp $
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

public class Triplet {
	double mVal1, mVal2, mVal3;
	
    public Triplet(double val1, double val2, double val3) {
    	mVal1 = val1;
    	mVal2 = val2;
    	mVal3 = val3;
	}
    
    public double getVal1() {
    	return mVal1;
    }
    
    public double getVal2() {
    	return mVal2;
    }
    
    public double getVal3() {
    	return mVal3;
    }
}
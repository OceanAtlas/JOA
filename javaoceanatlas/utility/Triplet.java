/*
 * $Id: Triplet.java,v 1.2 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

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
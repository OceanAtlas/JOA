/*
 * $Id: CachedIsobath.java,v 1.2 2005/06/17 18:10:58 oz Exp $
 *
 */

package javaoceanatlas.utility;

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
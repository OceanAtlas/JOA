/*
 * $Id: UVCoordinate.java,v 1.3 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

public class UVCoordinate {
    public double u, v, w;
    public boolean mInWind = true;
    
    public UVCoordinate(double uu, double vv) {
    	u = uu;
    	v = vv;
    }
    
    public UVCoordinate(double uu, double vv, double ww) {
    	u = uu;
    	v = vv;
    	w = ww;
    }
    
    public UVCoordinate(double uu, double vv, boolean inWind) {
    	u = uu;
    	v = vv;
    	mInWind = inWind;
    }
    
    public double getU() {
    	return u;
    }
    
    public double getV() {
    	return v;
    }
    
    public double getW() {
    	return w;
    }
}
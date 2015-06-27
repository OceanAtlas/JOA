package gov.noaa.pmel.eps2;

import javaoceanatlas.resources.JOAConstants;

/**
 * <code>JOAStation</code> A wrapper class for metadata that defines a parameter
 * in Java OceanAtlas.
 *
 * @see JOASectionReader
 *
 * @author oz
 * @version 1.0
 */
public class JOAParameter {
    protected double mActScale, mActOrigin, mPlotMin, mPlotMax;
    protected String mVarLabel = null;
    protected boolean mWasCalculated, mReverseY;
    protected int mCastOrObs;
    protected int mVariant;
    protected String mUnits = null;
    int mDisplayPrecision = JOAConstants.JOA_DEFAULT_PRECISION;
    int mSignificantDigits = 0;
    
    public JOAParameter(String varLabel) {
    	mVarLabel = new String(varLabel);
	}
    
    public JOAParameter(String varLabel, String units) {
    	mVarLabel = new String(varLabel);
    	if (units != null)
    		mUnits = new String(units);
	}
    
    public JOAParameter(String varLabel, double actScale, double actOrigin) {
    	mVarLabel = new String(varLabel);
    	mActScale = actScale;
    	mActOrigin = actOrigin;
	}
    
    public JOAParameter(String varLabel, double actScale, double actOrigin, String units) {
    	mVarLabel = new String(varLabel);
    	mActScale = actScale;
    	mActOrigin = actOrigin;
    	if (units != null)
    		mUnits = new String(units);
	}
    
    public int getDisplayPrecision() {
    	return mDisplayPrecision;
    }
    
    public void setDisplayPrecision(int i) {
    	mDisplayPrecision = i;
    }
    
    public int getSignificantDigits() {
    	return mSignificantDigits;
    }
    
    public void setSignificantDigits(int sd) {
    	if (sd > mSignificantDigits) {
    		mSignificantDigits = sd;
    	}
    }
}
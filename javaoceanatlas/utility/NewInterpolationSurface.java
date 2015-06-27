/*
 * $Id: NewInterpolationSurface.java,v 1.3 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.io.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class NewInterpolationSurface implements Serializable {
	protected String mParam;
	protected String mTitle;
	protected String mDescription;
	protected int mNumLevels;
	protected double[] mValues = new double[128];
	protected double mBaseLevel, mEndLevel;
	
	public NewInterpolationSurface(double[] inVals, int numLevels, String inParam, String title, String description) {
		mParam = inParam;
		mTitle = title;
		mDescription = description;
		mNumLevels = numLevels;
		mValues = inVals;
		setBaseLevel(inVals[0]);
		setEndLevel(inVals[(int)numLevels-1]);
	}
	
	public NewInterpolationSurface(NewInterpolationSurface inSurf) {
		// copy ctor
		mParam = new String(inSurf.mParam);
		mTitle =  new String(inSurf.mTitle);
		if (inSurf.mDescription != null)
			mDescription =  new String(inSurf.mDescription);
		mNumLevels = inSurf.mNumLevels;
		for (int i=0; i<mNumLevels; i++)
			mValues[i] = inSurf.mValues[i];
		setBaseLevel(mValues[0]);
		setEndLevel(mValues[(int)mNumLevels-1]);
	}
	
	public NewInterpolationSurface(InterpolationSurface inSurf) {
		// copy from old resource ctor
		mParam = new String(inSurf.mParam);
		mTitle =  new String(inSurf.mTitle);
		if (inSurf.mDescription != null)
			mDescription =  new String(inSurf.mDescription);
		mNumLevels = inSurf.mNumLevels;
		for (int i=0; i<mNumLevels; i++)
			mValues[i] = inSurf.mValues[i];
		setBaseLevel(mValues[0]);
		setEndLevel(mValues[(int)mNumLevels-1]);
	}
	
	public String getParam() {
		return mParam;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public String getDescrip() {
		return mDescription;
	}
	
	public int getNumLevels() {
		return mNumLevels;
	}
	
	public void setNumLevels(int nl) {
		mNumLevels = nl;
	}
	
	public double[] getValues() {
		return mValues;
	}

	public double[] getReversedValues() {
		double[] rVals = new double[mValues.length];
		int c = 0;
		for (int i = mValues.length-1; i>=0; i--) {
			rVals[c++] = mValues[i];
		}
		return rVals;
	}
	
	public double getValue(int i) {
		return mValues[i];
	}
	
	public double getBaseLevel() {
		return mBaseLevel;
	}
	
	public double getEndLevel() {
		return mEndLevel;
	}
	
    public void setBaseLevel(double base) {
    	mBaseLevel = base;
    }
    
    public void setEndLevel(double end) {
    	mEndLevel = end;
    }
    
    public void setTitle(String inTitle) {
    	mTitle = inTitle;
    }
    
    public void setParam(String inParam) {
    	mParam = inParam;
    }
    
    public void setDescription(String inDescrip) {
    	mDescription = inDescrip;
    }
    
    public void setValues(double[] inValues) {
    	mValues = inValues;
    }
    
    public boolean equalGrids(double[] testLevels) {
    	if (testLevels.length != mNumLevels)
    		return false;
    	else {
    		for (int i=0; i<mNumLevels; i++) {
    			if (Math.abs(testLevels[i] - mValues[i]) > 0.001)
    				return false;
    		}
    	}
    	return true;
    }
    
    public boolean equalGrids(double[] testLevels, int numtestlevels) {
    	if (numtestlevels != mNumLevels)
    		return false;
    	else {
    		for (int i=0; i<mNumLevels; i++) {
    			if (Math.abs(testLevels[i] - mValues[i]) > 0.001)
    				return false;
    		}
    	}
    	return true;
    }
    
    public double[] reGrid(double[] inMCSurfLevels, double[] inMCValues, int numBottles) {
    	double[] resultArray = new double[mNumLevels];
    	double physHigh, physLow, interpLow, interpHigh, delta;
    	int newSign, oldSign;
		boolean doIt = false;
    	int MINUS = -1;
    	int PLUS = +1;
    	int ZERO = 0;
    	
    	for (int iz=0; iz<mNumLevels; iz++)
    		resultArray[iz] = JOAConstants.MISSINGVALUE;
    		
		// interpolate
    	// loop over each level
    	for (int iz=0; iz<mNumLevels; iz++) {
			// initialize the sign variable from first bottle
			physHigh = inMCSurfLevels[0];
			delta = physHigh - mValues[iz];
			if (delta > 0)
				oldSign = PLUS;
			else if (delta < 0)
				oldSign = MINUS;
			else
				oldSign = ZERO;
			doIt = false;
			
    		// loop over the bottles
			for (int b=0; b<numBottles; b++) {
				double val = inMCSurfLevels[b];
				double ival = inMCValues[b];
				if (val != JOAConstants.MISSINGVALUE) {
					physHigh = val;
					doIt = true;
				}
				
				if (doIt) {
					delta = physHigh - mValues[iz];
					if (delta > 0)
						newSign = PLUS;
					else if (delta < 0)
						newSign = MINUS;
					else
						newSign = ZERO;
						
					if (newSign == ZERO) {
						// bottle and level are the same
						if (val != JOAConstants.MISSINGVALUE)
					    	resultArray[iz] = ival;
					}
					else if (oldSign != newSign && newSign != ZERO && b > 0) {
						double prevVal = inMCValues[b - 1];
						double prevSurfVal = inMCSurfLevels[b - 1];
						// sign changes
						if (val != JOAConstants.MISSINGVALUE && prevVal != JOAConstants.MISSINGVALUE) {
							physLow = prevSurfVal;
							interpLow = ival;
							interpHigh = prevVal;
							double iVal = (delta/(physHigh - physLow)) * (interpHigh - interpLow) + interpLow;
					    	resultArray[iz] = iVal;

						}
						break;
					}
				}
			}
    	}
    	return resultArray;
    }
}
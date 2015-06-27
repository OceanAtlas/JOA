/*
 * $Id: InterpolationSurface.java,v 1.2 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.io.*;

@SuppressWarnings("serial")
public class InterpolationSurface implements Serializable {
	protected String mParam;
	protected String mTitle;
	protected String mDescription;
	protected int mNumLevels;
	protected double[] mValues = new double[128];
	protected double mBaseLevel, mEndLevel;
	
	public InterpolationSurface(double[] inVals, int numLevels, String inParam, String title, String description) {
		mParam = inParam;
		mTitle = title;
		mDescription = description;
		mNumLevels = numLevels;
		mValues = inVals;
		setBaseLevel(inVals[0]);
		setEndLevel(inVals[(int)numLevels-1]);
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
}
/*
 * $Id: ColorBar.java,v 1.2 2005/06/17 18:10:58 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.awt.*;
import java.io.*;
import javaoceanatlas.ui.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class ColorBar implements Serializable {
    protected String mParam, mTitle, mDescription;
    protected int mNumColorLevels;
    protected double mBaseLevel, mEndLevel;
    protected double[] mContourValues = new double[128];
    protected Color[] mColorValues = new Color[128];
    
    public ColorBar(Color[] inColors, double[] inVals, int numLevels, String inParam, 
    	String title, String description) {
    	mContourValues = inVals;
    	mColorValues = inColors;
    	mNumColorLevels = numLevels;
    	mBaseLevel = inVals[0];
    	mEndLevel = inVals[numLevels-1];
    	mParam = inParam;
    	mTitle = title;
    	mDescription = description;
    }
    
    public ColorBar(FileViewer fv, int cbParam) {
    	double base = fv.mAllProperties[cbParam].mPlotMin;
    	double end = fv.mAllProperties[cbParam].mPlotMax;
    	String var = fv.mAllProperties[cbParam].mVarLabel;
    	createAutoscaledColorBar(base, end, var);
    }
    
    public ColorBar(double base, double end, String inVar) {
    	createAutoscaledColorBar(base, end, inVar);
    }
    
    public void dumpCB() {
    	System.out.println("mParam = " + mParam);
    	System.out.println("mContourValues length= " + mContourValues.length);
    	System.out.println("mNumColorLevels = " + mNumColorLevels);
    }
    
    private void createAutoscaledColorBar(double base, double end, String inVar) {
    	// Create the default autoscaled color bar: linear from start to end, 15 colors
    	mNumColorLevels = 15;
    	mBaseLevel = base;
    	mEndLevel = end;
    	//mMethod = JOAConstants.LINEAR;
    	mParam = new String(inVar);
    	mTitle = new String(inVar + ": Autoscaled");
    	double red = 1266./65535.;
		double green = 1223./65535.;
		double blue = 64768./65535.;
    	mColorValues[0] = new Color((float)red, (float)green, (float)blue);
		
		red = 512./65535.;
		green = 23296./65535.;
		blue = 64768./65535.;
    	mColorValues[1] = new Color((float)red, (float)green, (float)blue);
		
		red = 9216./65535.;
		green = 42496./65535.;
		blue = 64768./65535.;
    	mColorValues[2] = new Color((float)red, (float)green, (float)blue);
		
		red = 16896./65535.;
		green = 57344./65535.;
		blue = 64768./65535.;
    	mColorValues[3] = new Color((float)red, (float)green, (float)blue);
		
		red = 25856./65535.;
		green = 64768./65535.;
		blue = 61440./65535.;
    	mColorValues[4] = new Color((float)red, (float)green, (float)blue);
		
		red = 33024./65535.;
		green = 64768./65535.;
		blue = 54528./65535.;
    	mColorValues[5] = new Color((float)red, (float)green, (float)blue);
		
		red = 47872./65535.;
		green = 64768./65535.;
		blue = 55296./65535.;
    	mColorValues[6] = new Color((float)red, (float)green, (float)blue);
		
		red = 58112./65535.;
		green = 64768./65535.;
		blue = 59392./65535.;
    	mColorValues[7] = new Color((float)red, (float)green, (float)blue);
		
		red = 64768./65535.;
		green = 61696./65535.;
		blue = 52992./65535.;
    	mColorValues[8] = new Color((float)red, (float)green, (float)blue);
		
		red = 64768./65535.;
		green = 57600./65535.;
		blue = 44800./65535.;
    	mColorValues[9] = new Color((float)red, (float)green, (float)blue);
		
		red = 64768./65535.;
		green = 51713./65535.;
		blue = 31863./65535.;
    	mColorValues[10] = new Color((float)red, (float)green, (float)blue);
		
		red = 64768./65535.;
		green = 41984./65535.;
		blue = 29952./65535.;
    	mColorValues[11] = new Color((float)red, (float)green, (float)blue);
		
		red = 64768./65535.;
		green = 32512./65535.;
		blue = 23040./65535.;
    	mColorValues[12] = new Color((float)red, (float)green, (float)blue);
		
		red = 64768./65535.;
		green = 21504./65535.;
		blue = 15872./65535.;
    	mColorValues[13] = new Color((float)red, (float)green, (float)blue);
		
		red = 64768./65535.;
		green = 1292./65535.;
		blue = 1808./65535.;
    	mColorValues[14] = new Color((float)red, (float)green, (float)blue);
    	
    	// compute the levels
    	double increment = (end - base)/((double)mNumColorLevels - 1);
    	for (int i=0; i<mNumColorLevels; i++) {
    		mContourValues[i] = base + (i * increment);
    	}
    	
    	// create the default contour lines
    }
    
    public Color getColorIndex(int i) {
    	
    	return Color.black;
    }
    
    public int getColorIndex(double inVal) {
		int end = this.mNumColorLevels;
	    if (this.mContourValues[1] > this.mContourValues[0]) {             // increasing color bar:
	        if (inVal < this.mContourValues[0]) {
	            return 0;
	        }
	        if (inVal >= this.mContourValues[end-2]) {
	            return end - 1;
	        }
	        for (int i=0; i<end-2; i++)
	            if (inVal >= this.mContourValues[i] && inVal < this.mContourValues[i+1]) {
	                return i+1;
	            }
	    }
	    else {                              // decreasing color bar:
	        if (inVal > this.mContourValues[0]) {
	            return 0;
	        }
	        if (inVal <= this.mContourValues[end-2]) {
	            return end - 1;
	        }
	        for (int i=0; i<end-2; i++)
	            if (inVal <= this.mContourValues[i] && inVal > this.mContourValues[i+1]) {
	                return i+1;
	            }
	    }
	    return JOAConstants.MISSINGVALUE;
    }
    
    public Color getColorValue(int i) {
    	int ii = i < 128 ? i : 127;
    	try {
    		return mColorValues[ii];
    	}
    	catch (Exception ex) {
    		//System.out.println("index=" + ii);
    		//System.out.println("Value length=" + mColorValues.length);
    	}
    	return Color.white;
    }
    
    public Color getColor(double inVal) {
		int end = this.mNumColorLevels;
	    if (this.mContourValues[1] > this.mContourValues[0]) {             // increasing color bar:
	        if (inVal < this.mContourValues[0]) {
	            return mColorValues[0];
	        }
	        if (inVal >= this.mContourValues[end-2]) {
	            return mColorValues[end - 1];
	        }
	        for (int i=0; i<end-2; i++)
	            if (inVal >= this.mContourValues[i] && inVal < this.mContourValues[i+1]) {
	                return mColorValues[i+1];
	            }
	    }
	    else {                              // decreasing color bar:
	        if (inVal > this.mContourValues[0]) {
	            return mColorValues[0];
	        }
	        if (inVal <= this.mContourValues[end-2]) {
	            return mColorValues[end -1];
	        }
	        for (int i=0; i<end-2; i++)
	            if (inVal <= this.mContourValues[i] && inVal > this.mContourValues[i+1]) {
	                return mColorValues[i+1];
	            }
	    }
	    return JOAConstants.DEFAULT_MISSINGVAL_COLOR;
    }
    
    public int getNumLevels() {
    	return mNumColorLevels;
    }
    
    public void setNumLevels(int nl) {
    	mNumColorLevels = nl;
    }
        
    public double getDoubleValue(int i) {
    	try {
    		return mContourValues[i];
    	}
    	catch (Exception ex) {
    		//System.out.println("index=" + i);
    		//System.out.println("mContourValues length=" + mContourValues.length);
    	}
    	return 0.0;
    }
    
    public double getBaseLevel() {
    	return mBaseLevel;
    }
    
    public double getEndLevel() {
    	return mEndLevel;
    }
    
    public String getTitle() {
    	return mTitle;
    }
    
    public String getParam() {
    	return mParam;
    }
    
    public String getDescription() {
    	return mDescription;
    }
    
    public double[] getValues() {
    	return mContourValues;
    }
    
    public Color[] getColors() {
    	return mColorValues;
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
    	mContourValues = inValues;
    	setNumLevels(inValues.length);
    }
    
    public void setColors(Color[] inColors) {
    	mColorValues = inColors;
    }
}
    
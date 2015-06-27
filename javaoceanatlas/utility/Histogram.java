/*
 * $Id: Histogram.java,v 1.2 2005/06/17 18:10:58 oz Exp $
 *
 */

package javaoceanatlas.utility;

import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.ui.*;
import javaoceanatlas.resources.*;

public class Histogram {
    private int[] mHistValues;
    private int mNumBins;
    private String mParam;
    private FileViewer mFileViewer;
    private NewColorBar mColorBar;
    private int mMax = 0;
    private double[] mValues;

    public Histogram(String param, FileViewer fv, NewColorBar cb) {
        mParam = param;
        mFileViewer = fv;
        mColorBar = cb;
        
        // dimension and initialize the arrays
        mNumBins = mColorBar.getNumLevels();
        mHistValues = new int[mNumBins];
        mValues = new double[mNumBins];
        double[] vals = mColorBar.getValues();
        double min = vals[0];
        double max = vals[mNumBins-1];
        double inc = (max - min)/((double)mNumBins - 1);
        for (int i=0; i<mNumBins; i++) {
        	mHistValues[i] = 0;
        	mValues[i] = min + (double)i * inc;
        }
        computeHistogram();
    }
    
    public void computeHistogram() {
        // divide the values into bins
		for (int fc=0; fc<mFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);
			
			for (int sec=0; sec<of.mNumSections; sec++) {
				Section sech = (Section)of.mSections.elementAt(sec);
			
				int pos = sech.getVarPos(mParam, false);
				if (sech.mNumCasts == 0 || pos == -1)
					continue;

				for (int stc=0; stc<sech.mStations.size(); stc++) {
			    	Station sh = (Station)sech.mStations.elementAt(stc);
			    	if (!sh.mUseStn)
			    		continue;
		    		
					for (int b=0; b<sh.mNumBottles; b++) {
    					Bottle bh = (Bottle)sh.mBottles.elementAt(b);
    					double val = bh.mDValues[pos];
    					int bin = getColorIndex(val);
    					if (bin != JOAConstants.MISSINGVALUE)
    						mHistValues[bin]++;
					}
				}
			}
		}
        
        // compute the maximum value
        for (int i=0; i<mNumBins; i++)
        	mMax = mHistValues[i] > mMax ? mHistValues[i] : mMax;
    }
    
    public int getColorIndex(double inVal) {
		int end = mNumBins;
	    if (mValues[1] > mValues[0]) {             // increasing color bar:
	        if (inVal < mValues[0]) {
	            return 0;
	        }
	        if (inVal >= mValues[end-2]) {
	            return end - 1;
	        }
	        for (int i=0; i<end-2; i++)
	            if (inVal >= mValues[i] && inVal < mValues[i+1]) {
	                return i+1;
	            }
	    }
	    else {                              // decreasing color bar:
	        if (inVal > mValues[0]) {
	            return 0;
	        }
	        if (inVal <= mValues[end-2]) {
	            return end - 1;
	        }
	        for (int i=0; i<end-2; i++)
	            if (inVal <= mValues[i] && inVal > mValues[i+1]) {
	                return i+1;
	            }
	    }
	    return JOAConstants.MISSINGVALUE;
    }
    
    public void setValues(double min, double max, int nl) {
		// redimension the arrays
        mHistValues = null;
        mValues = null;
        mHistValues = new int[nl];
        mValues = new double[nl];
        mNumBins = nl;
        double inc = (max - min)/((double)mNumBins - 1);
        for (int i=0; i<mNumBins; i++) {
        	mHistValues[i] = 0;
        	mValues[i] = min + (double)i * inc;
        }
        mMax = 0;
        computeHistogram();
    }
    
    public int getNumBins() {
    	return mNumBins;
    }
    
    public int getMax() {
    	return mMax;
    }
    
    public int[] getHistoValues() {
    	return mHistValues;
    }
    
}

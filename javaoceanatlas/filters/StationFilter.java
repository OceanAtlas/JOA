/*
 * $Id: StationFilter.java,v 1.3 2005/06/17 18:03:16 oz Exp $
 *
 */

package javaoceanatlas.filters;

import java.util.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.*;
import javaoceanatlas.ui.*;
import javaoceanatlas.resources.*;
import java.io.DataOutputStream;
import java.io.IOException;
import javaoceanatlas.utility.*;

public class StationFilter {
	boolean mCriteria1Active = false;
	boolean mCriteria2Active = false;
	boolean mCriteria3Active = false;
	double mMinLat;
	double mMaxLat;
	double mMinLon;
	double mMaxLon;
	int[] mMissingParams = null;
	int[] mStnList = null;
	boolean[] mStnKeepList = null;
	boolean mExcludeStns = true;
	FileViewer mFileViewer;
	    
    public StationFilter(FileViewer fv) {
    	mFileViewer = fv;
    }
    
    public boolean testObservation(FileViewer fv, Section sech, Station sh) {
    	boolean keep1 = true, keep2 = true, keep3 = true;
    	
    	if (mCriteria1Active) {
    		// test whether this station is in station list
    		keep1 = mStnKeepList[sh.mOrdinal-1];
		}
		
    	if (mCriteria2Active) {
    		// test whether this section has all the test parameters
    		for (int v=0; v<mMissingParams.length; v++) {
				int pos = sech.getVarPos(fv.mAllProperties[mMissingParams[v]].getVarLabel(), false);
				if (pos == -1) {
					// this sech does not have this parameter--don't need to look any further
					keep2 = false;
					break;
				}
			}
			
			if (keep2) {
				// station has all parameters now test whether they're non-missing for 
				// the test station
				for (int v=0; v<mMissingParams.length; v++) {
					int pos = sech.getVarPos(fv.mAllProperties[mMissingParams[v]].getVarLabel(), false);
					for (int b=0; b<sh.mNumBottles; b++) {
    					Bottle bh = (Bottle)sh.mBottles.elementAt(b);
						double x = bh.mDValues[pos];
						if (x == JOAConstants.MISSINGVALUE)
							keep2 = false;
						else {
							keep2 = true;
							break;
						}
    				}
    				if (!keep2)
    					break;
				}
			}
		}
    	
    	if (mCriteria3Active) {
    		// test whether this station is in designated area
    		double lat = sh.mLat;
    		double lon = sh.mLon;
    		if ((mMaxLon < mMinLon) && mMinLon > 0 && mMaxLon < 0) {
    			// selection crosses 180
    			boolean inRgn = true;
    			double sr1West = mMinLon;
    			double sr1East = 180;
    			double sr2West = -180;
    			double sr2East = mMaxLon;
    			
    			if (lon > 0) 
    				inRgn = (lon >= sr1West && lon <= sr1East);
    			if (lon < 0)
    				inRgn = (lon >= sr2West && lon <= sr2East);
    			keep3 = inRgn && (lat >= mMinLat) && (lat <= mMaxLat);
    		}
    		else
    			keep3 = (lat >= mMinLat) && (lat <= mMaxLat) &&
    			        (lon >= mMinLon) && (lon <= mMaxLon);
    			
		}
		return keep1 && keep2 && keep3;
    }
    
    public double getMaxLon() {
    	return mMaxLon;
    }
    
    public void setMaxLon(double d) {
    	mMaxLon = d;
    }
    
    public double getMinLon() {
    	return mMinLon;
    }
    
    public void setMinLon(double d) {
    	mMinLon = d;
    }
    
    public double getMaxLat() {
    	return mMaxLat;
    }
    
    public void setMaxLat(double d) {
    	mMaxLat = d;
    }
    
    public double getMinLat() {
    	return mMinLat;
    }
    
    public void setMinLat(double d) {
    	mMinLat = d;
    }
    
    public boolean isCriteria1Active() {
    	return mCriteria1Active;
    }
    
    public void setCriteria1Active(boolean b) {
    	mCriteria1Active = b;
    }
    
    public boolean isCriteria2Active() {
    	return mCriteria2Active;
    }
    
    public void setCriteria2Active(boolean b) {
    	mCriteria2Active = b;
    }
    
    public boolean isCriteria3Active() {
    	return mCriteria3Active;
    }
    
    public void setCriteria3Active(boolean b) {
    	mCriteria3Active = b;
    }
    
    public boolean isExcludeStns() {
    	return mExcludeStns;
    }
    
    public void setExcludeStns(boolean b) {
    	mExcludeStns = b;
    }
    
    public boolean[] getStnKeepList() {
    	return mStnKeepList;
    }
    
    public int[] getStnList() {
    	return mStnList;
    }
    
    public int getStnList(int i) {
    	return mStnList[i];
    }
    
    public int[] getMissingParams() {
    	return mMissingParams;
    }
    
    public void setStnList(int s, int[] a) {
    	mStnList = null;
    	mStnList = new int[s];
    	for (int i=0; i<s; i++)
    		mStnList[i] = a[i];
    }
    
    public void setStnKeepList(int s, boolean[] a) {
    	mStnKeepList = null;
    	mStnKeepList = new boolean[s];
    	for (int i=0; i<s; i++)
    		mStnKeepList[i] = a[i];
    }
    
    public void setStnKeepList(int s) {
    	mStnKeepList = null;
    	mStnKeepList = new boolean[s];
    }
    
    public void setStnKeepList(int i, boolean b) {
    	mStnKeepList[i] = b;
    }
    
    public void setMissingParams(int s, int[] a) {
    	mMissingParams = null;
    	mMissingParams = new int[s];
    	for (int i=0; i<s; i++)
    		mMissingParams[i] = a[i];
    }
        
    public void writeToLog(String preamble) throws IOException {
    	try {
	    	JOAConstants.LogFileStream.writeBytes(preamble);
			JOAConstants.LogFileStream.writeBytes("Station Filter:");
					JOAConstants.LogFileStream.writeBytes("\n");
			
			if (mCriteria1Active) {
				//station list
				if (mExcludeStns) {
					JOAConstants.LogFileStream.writeBytes("\t" + "Exclude stns: ");
					for (int i=0; i<mStnList.length; i++) {
						if (mStnKeepList[i]) {
							JOAConstants.LogFileStream.writeBytes(String.valueOf(mStnList) + "," );
						}
					}
					JOAConstants.LogFileStream.writeBytes("\n");
				}
				else {
					JOAConstants.LogFileStream.writeBytes("\t" + "Include stns: ");
					for (int i=0; i<mStnList.length; i++) {
						if (mStnKeepList[i]) {
							JOAConstants.LogFileStream.writeBytes(String.valueOf(mStnList) + "," );
						}
					}
					JOAConstants.LogFileStream.writeBytes("\n");
				}
			}
			else if (mCriteria2Active) {
				// missing params
				JOAConstants.LogFileStream.writeBytes("\t" + "Filter by missing values for params: ");
				for (int i=0; i<mMissingParams.length; i++) {
					JOAConstants.LogFileStream.writeBytes(mFileViewer.mAllProperties[mMissingParams[i]].getVarLabel() + ",");
				}
				JOAConstants.LogFileStream.writeBytes("\n");
			}
			else if (mCriteria3Active) {
				// lat/lon
				String iStr = "(include)";
				if (mExcludeStns)
					iStr = "(exclude)";
				JOAConstants.LogFileStream.writeBytes("\t" + "Filter by spatial domain" + iStr + ": Min Lat = " + mMinLat + ", Max Lat = " + mMaxLat + 
					", Min Lon = " + mMinLon + ", Max Lon = " + mMaxLon);
				JOAConstants.LogFileStream.writeBytes("\n");
			}

			JOAConstants.LogFileStream.flush();
		}
		catch (IOException ex) {
			throw ex;
		}
    }
}
    
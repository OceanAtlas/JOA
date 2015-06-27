package gov.noaa.pmel.eps2;

import java.util.*;
import java.awt.*;

/**
 * <code>JOAStation</code> A wrapper class for metadata that defines a station
 * in Java OceanAtlas.
 * @see JOASectionReader
 *
 * @author oz
 * @version 1.0
 */
public class JOAStation {
    public int mOrdinal;
    String mShipCode;
    public boolean mUseStn;
    //public LRVector mBottles = new LRVector();
    protected double mLat, mLon;
    protected int mYear;
    protected int mMonth;
    protected int mDay;
    protected int mHour;
    protected double mMinute;
    protected String mStnNum;
    protected int mCastNum;
    protected int mNumBottles;
    protected int mBottomDepthInDBARS;
    protected int mVarFlag;
	protected boolean mDateError = false;
	protected String mDataType;
    
    public JOAStation() {
    
    }
    public JOAStation(int ord, String ship, String stnNum, int castNum, double lat, double lon,
                   int numBottles, int year, int month, int day, int hour, double min, int bottom, int varFlag) {
		mOrdinal = ord;
		mShipCode = new String(ship);
    	mUseStn = true;
    	mLat= lat;
    	mLon = lon;
    	
		if (month > 12 || day > 31) {
	    	mYear = year;
	    	mMonth = month;
	    	mDay = day;
			mDateError = true;
		}
		else {
	    	mYear = year;
	    	mMonth = month;
	    	mDay = day;
			if (mYear > 30 && mYear <=99)
				mYear += 1900;
	    }
	    
    	mHour = hour;
    	mMinute = min;
    	mStnNum = new String(stnNum);
    	mCastNum = castNum;
    	mNumBottles = numBottles;
    	mBottomDepthInDBARS = bottom;
    	mVarFlag = varFlag;
	}
		
	public void setType(String typ) {
		mDataType = new String(typ);
	}
	
	public String getType() {
		return mDataType;
	}
}
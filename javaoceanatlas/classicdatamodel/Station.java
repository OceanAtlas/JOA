/*
 * $Id: Station.java,v 1.7 2005/06/17 18:02:04 oz Exp $
 *
 */

package javaoceanatlas.classicdatamodel;

import java.awt.*;
import gov.noaa.pmel.util.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.utility.*;

public class Station {
	public int mOrdinal, mMasterOrdinal;
	public String mShipCode;
	public boolean mUseStn;
	public boolean mSkipStn = false;
	public LRVector mBottles = new LRVector();
	public double mLat;
	public double mLon;
	public int mYear = JOAConstants.MISSINGVALUE;
	public int mMonth = JOAConstants.MISSINGVALUE;
	public boolean mMonthIsBad = false;
	public boolean mDayIsBad = false;
	public int mDay = JOAConstants.MISSINGVALUE;
	public int mHour = JOAConstants.MISSINGVALUE;
	public double mMinute = JOAConstants.MISSINGVALUE;
	public double mCumDist, mCumMidDist, mMasterCumDistance;
	public String mStnNum;
	public int mCastNum;
	public int mNumBottles;
	public int mBottomDepthInDBARS;
	public int mVarFlag;
	public Color mCurrColor;
	public int mCurrSymbolSize;
	public boolean mHilitedOnMap = false;
	public boolean mDateError = false;
	public String mDataType;
	public boolean mCastIsEvenlySpaced = false;
	public String mOriginalPath;
	public String mOriginalName;
	protected int mNumStnValues = 0;
	protected double mStnValues[] = new double[100];
	protected boolean[] mStnHitShallowest = new boolean[100];
	protected boolean[] mStnHitDeepest = new boolean[100];
	protected double mTempDist;
	protected double mTempDepth;
	protected Station mNearestOverlayStn = null;
	protected boolean mOverlayStnIsAssigned = false;

	public Station() {
	}

	public Station(Station inStn) {
		mOrdinal = inStn.mOrdinal;
		mMasterOrdinal = inStn.mMasterOrdinal;
		mShipCode = inStn.mShipCode;
		mUseStn = inStn.mUseStn;
		mSkipStn = inStn.mSkipStn;
		mLat = inStn.mLat;
		mLon = inStn.mLon;
		mYear = inStn.mYear;
		mMonth = inStn.mMonth;
		mDay = inStn.mDay;
		mHour = inStn.mHour;
		mMinute = inStn.mMinute;
		mCumDist = inStn.mCumDist;
		mCumMidDist = inStn.mCumMidDist;
		mMasterCumDistance = inStn.mMasterCumDistance;
		mStnNum = inStn.mStnNum;
		mCastNum = inStn.mCastNum;
		mNumBottles = inStn.mNumBottles;
		mBottomDepthInDBARS = inStn.mBottomDepthInDBARS;
		mVarFlag = inStn.mVarFlag;
		if (inStn.mCurrColor != null) {
			mCurrColor = new Color(inStn.mCurrColor.getRed(), inStn.mCurrColor.getGreen(), inStn.mCurrColor.getBlue());
		}
		
		mCurrSymbolSize = inStn.mCurrSymbolSize;
		mDataType = new String(inStn.mDataType);
		mCastIsEvenlySpaced = inStn.mCastIsEvenlySpaced;
		mOriginalPath = inStn.mOriginalPath;
		mOriginalName = inStn.mOriginalName;
		mNumStnValues = inStn.mNumStnValues;
		for (int i=0; i<inStn.mNumStnValues; i++) {
			mStnValues[i] = inStn.mStnValues[i];
			mStnHitShallowest[i] = inStn.mStnHitShallowest[i];
			mStnHitDeepest[i] = inStn.mStnHitDeepest[i];
		}
//
//    for (int b = 0; b < inStn.mNumBottles; b++) {
//    	Bottle newBH = new Bottle((Bottle)(inStn.mBottles.elementAt(b)));
//    	mBottles.add(newBH);
//    }
	}

	public Station(int ord, String ship, String stnNum, int castNum, double lat, double lon, int numBottles, int year,
	    int month, int day, int hour, double min, int bottom, int varFlag, String origName, String path) {
		mOrdinal = ord;
		if (ship != null) {
			mShipCode = new String(ship);
		}
		mUseStn = true;
		mLat = lat;
		mLon = lon;
		if (path != null) {
			mOriginalPath = new String(path);
		}
		if (origName != null) {
			mOriginalName = new String(origName);
		}

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
			if (mYear > 30 && mYear <= 99) {
				mYear += 1900;
			}
		}

		mHour = hour;
		mMinute = min;
		if (stnNum != null) {
			mStnNum = new String(stnNum);
		}
		else {
			mStnNum = "UNK";
		}
		mCastNum = castNum;
		mNumBottles = numBottles;
		mBottomDepthInDBARS = bottom;
		mVarFlag = varFlag;

		for (int i = 0; i < 100; i++) {
			mStnHitShallowest[i] = false;
			mStnHitDeepest[i] = false;
		}
	}

	public String getStn() {
		return mStnNum;
	}

	public double getLat() {
		return mLat;
	}

	public double getLon() {
		return mLon;
	}

	public double getBottom() {
		return (double) mBottomDepthInDBARS;
	}

	public void setType(String typ) {
		if (typ != null) {
			mDataType = new String(typ);
		}
	}

	public String getType() {
		return mDataType;
	}

	public String getOriginalPath() {
		return mOriginalPath;
	}

	public String getOriginalName() {
		return mOriginalName;
	}

	public void addStnVal(int i, double val) {
		mStnValues[i] = val;
	}

	public void setStnValHitShallowest(int i, boolean val) {
		mStnHitShallowest[i] = val;
	}

	public void setStnValHitDeepest(int i, boolean val) {
		mStnHitDeepest[i] = val;
	}

	public boolean hitShallowest(int i) {
		return mStnHitShallowest[i];
	}

	public boolean hitDeepest(int i) {
		return mStnHitDeepest[i];
	}

	public int getNumStnParams() {
		return mStnValues.length;
	}

	public double getStnValue(int i) {
		return mStnValues[i];
	}

	public void setOrdinals(int ord) {
		mOrdinal = ord;
		mMasterOrdinal = ord;
	}

	public int getYear() {
		return mYear;
	}

	public int getMonth() {
		return mMonth;
	}

	public int getDay() {
		return mDay;
	}

	public int getHour() {
		return mHour;
	}

	public double getMinute() {
		return mMinute;
	}

	public GeoDate getDate() {
		GeoDate theDate = null;
		int min = 0;
		int msecs = 0;
		int secs = 0;
		int hour = this.mHour;
		try {
			if (this.mMinute != JOAConstants.MISSINGVALUE) {
				min = (int) this.mMinute;
				secs = (int) ((this.mMinute - min) * 60);
			}
			if (this.mHour == JOAConstants.MISSINGVALUE) {
				hour = 0;
			}

			if (min == 60) {
				min = 59;
				secs = 59;
			}
			theDate = new GeoDate(this.mMonth, this.mDay, this.mYear, hour, min, secs, msecs);
		}
		catch (IllegalTimeValue ex) {
			try {
				theDate = new GeoDate(this.mDay, this.mMonth, this.mYear, hour, min, secs, msecs);
			}
			catch (IllegalTimeValue exx) {
				System.out.println("Station couldn't create a geodate");
				System.out.println("Stn = " + getStn());
				System.out.println("mMonth = " + mMonth);
				System.out.println("mDay = " + mDay);
				System.out.println("mYear = " + mYear);
				return null;
			}
		}

		return theDate;

	}

	public int getNumBottles() {
		return mNumBottles;
	}

	public Bottle getCurrBottle() {
		return (Bottle) mBottles.getCurrElement();
	}

	public double getTempDist() {
		return mTempDist;
	}

	public void setTempDist(double d) {
		mTempDist = d;
	}

	public double getTempDepth() {
		return mTempDepth;
	}

	public void setTempDepth(double d) {
		mTempDepth = d;
	}

	public void setBottom(int i) {
		mBottomDepthInDBARS = i;
	}

	public Station getNearestOverlayStn() {
		return mNearestOverlayStn;
	}

	public void setNearestOverlayStn(Station s) {
		mNearestOverlayStn = s;
	}

	public boolean isOverlayStnAssigned() {
		return mOverlayStnIsAssigned;
	}

	public boolean isSkipStn() {
		return mSkipStn;
	}

	public void setSkipStn(boolean b) {
		mSkipStn = b;
	}

	public void setOverlayStnAssigned(boolean b) {
		mOverlayStnIsAssigned = b;
	}

	public int getCast() {
		return mCastNum;
	}
	
	public double getCumDistInKM() {
		return mCumDist * 1.852;
	}
}

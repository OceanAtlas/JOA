/*
 * $Id: SectionStation.java,v 1.2 2005/06/17 18:02:04 oz Exp $
 *
 */

package javaoceanatlas.classicdatamodel;

import java.util.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;
import javaoceanatlas.utility.*;

public class SectionStation extends Station {
    public int mSecOrdinal;
    public boolean mCurrVisible;
    public Section mFoundSection;
    public Station mFoundStn;
    
    public SectionStation() {  	
    }
    
    public SectionStation(Station sh, Section sec, int ord) {
    	super();
    	mFoundStn = sh;
    	if (sh.mShipCode != null)
    		mShipCode = new String(sh.mShipCode);
    	mSecOrdinal = ord;
		  mOrdinal = sh.mOrdinal;
    	mUseStn = sh.mUseStn;
    	mLat= sh.mLat;
    	mLon = sh.mLon;
    	mYear = sh.mYear;
    	mMonth = sh.mMonth;
    	mDay = sh.mDay;
    	mHour = sh.mHour;
    	mMinute = sh.mMinute;
    	mStnNum = sh.mStnNum;
    	mCastNum = sh.mCastNum;
    	mNumBottles = sh.mNumBottles;
    	mBottomDepthInDBARS = sh.mBottomDepthInDBARS;
    	mVarFlag = sh.mVarFlag;
    	mBottles = sh.mBottles;
    	mCurrColor = sh.mCurrColor;
    	mCurrSymbolSize = sh.mCurrSymbolSize;
    	mFoundSection = sec;
    	if (sh.mDataType != null)
    		mDataType = new String(sh.mDataType);
    	sh.mHilitedOnMap = false;
    	if (sh.mOriginalPath != null)
    		mOriginalPath = sh.mOriginalPath;
    	if (sh.mOriginalName != null)
    		mOriginalName = sh.mOriginalName;
	}
    
    public SectionStation(SectionStation inStn) {
    	super();
    	mShipCode = inStn.mShipCode;
    	mFoundStn = inStn.mFoundStn;
    	mSecOrdinal = inStn.mSecOrdinal;
		mOrdinal = inStn.mOrdinal;
    	mUseStn = inStn.mUseStn;
    	mLat= inStn.mLat;
    	mLon = inStn.mLon;
    	mYear = inStn.mYear;
    	mMonth = inStn.mMonth;
    	mDay = inStn.mDay;
    	mHour = inStn.mHour;
    	mMinute = inStn.mMinute;
    	mStnNum = inStn.mStnNum;
    	mCastNum = inStn.mCastNum;
    	mNumBottles = inStn.mNumBottles;
    	mBottomDepthInDBARS = inStn.mBottomDepthInDBARS;
    	mVarFlag = inStn.mVarFlag;
    	mBottles = inStn.mBottles;
    	mCurrColor = inStn.mCurrColor;
    	mCurrSymbolSize = inStn.mCurrSymbolSize;
    	mFoundSection = inStn.mFoundSection;
    	if (inStn.mDataType != null)
    		mDataType = new String(inStn.mDataType);
    	if (inStn.mOriginalPath != null)
    		mOriginalPath = new String(inStn.mOriginalPath);
    	if (inStn.mOriginalName != null)
    		mOriginalName = new String(inStn.mOriginalName);
	}
	
	public String getType() {
		return mDataType;
	}
	
	public Section getSection() {
		return mFoundSection;
	}
}
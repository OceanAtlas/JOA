/*
 * $Id: Bottle.java,v 1.2 2005/06/17 18:02:04 oz Exp $
 *
 */

package javaoceanatlas.classicdatamodel;

import java.util.*;
import net.sourceforge.openforecast.DataPoint;
import javaoceanatlas.*;
import javaoceanatlas.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;
import javaoceanatlas.ui.*;

public class Bottle implements DataPoint {
	public int mOrdinal;
	public int mNumVars;
	public short mQualityFlag = (short) JOAConstants.MISSINGVALUE;
	public float mDValues[] = null;
	public short mQualityFlags[] = null;
	private int mSampNo = (short) JOAConstants.MISSINGVALUE;
	private boolean mUseBottle = true;
	private int mBottleNum = (short) JOAConstants.MISSINGVALUE;
	private float mRawCTDValue;
	private boolean[] mDValuesEdited;
	public boolean[] mQCValuesEdited;
	public boolean[] mQCValueAssigned;
	private boolean mIsRawCTDMeasured = false;
	private boolean mIsSampNoUsed = false;
	private boolean mIsSBottleNumUsed = false;
	private Station mParentStation;
	private Section mParentSection;
	private String[] mIndVarNames = { "TEMP", "LON", "LAT" };
	private double mSaltVal = Double.NaN;
	private double mTempVal = Double.NaN;
	private double mLonVal = Double.NaN;
	private double mLatVal = Double.NaN;
	private String mIndVarName = "TEMP";

	// TODO: Implement these flags

	public Bottle(int ord, int numVars, Station sh, Section sec) {
		mOrdinal = ord;
		mParentStation = sh;
		mParentSection = sec;
		mDValues = new float[numVars + 35];
		mQualityFlags = new short[numVars + 35];
		mDValuesEdited = new boolean[numVars + 35];
		mQCValuesEdited = new boolean[numVars + 35];
		mQCValueAssigned = new boolean[numVars + 35];

		for (int i = 0; i < numVars + 35; i++) {
			mDValuesEdited[i] = false;
			mQCValuesEdited[i] = false;
			mQCValueAssigned[i] = false;
			mQualityFlags[i] = 9;
		}
		mNumVars = numVars;
	}

	public Bottle(Bottle bh) {
		mOrdinal = bh.mOrdinal;
		mNumVars = bh.mNumVars;
		mDValues = new float[mNumVars + 35];
		mQualityFlags = new short[mNumVars + 35];
		mDValuesEdited = new boolean[mNumVars + 35];
		mQCValuesEdited = new boolean[mNumVars + 35];
		mQCValueAssigned = new boolean[mNumVars + 35];
		mQualityFlag = bh.mQualityFlag;
		mSampNo = bh.getSampNo();
		mRawCTDValue = bh.getRawCTD();
		for (int i = 0; i < mNumVars; i++) {
			mDValues[i] = bh.mDValues[i];
			mQualityFlags[i] = bh.mQualityFlags[i];
			mDValuesEdited[i] = bh.mDValuesEdited[i];
			mQCValuesEdited[i] = bh.mQCValuesEdited[i];
			mQCValueAssigned[i] = bh.mQCValueAssigned[i];
		}
		mUseBottle = bh.isUseBottle();
		mBottleNum = bh.getBottleNum();

		mIsRawCTDMeasured = bh.isRawCTDMeasured();
		mIsSampNoUsed = bh.isSampNoUsed();
		mIsSBottleNumUsed = bh.isBottleNumUsed();
	}

	public void addParamAndValue(float inVal) {
		mDValues[mNumVars] = inVal;
		mDValuesEdited[mNumVars] = false;
		mNumVars++;
	}

	public Bottle extrapolate(int iPos, int pPos, float slope, float toPres, float delta) {
		Bottle returnedBottle = new Bottle(this);
		returnedBottle.mDValues[pPos] = toPres;
		returnedBottle.mDValues[iPos] = slope * delta + mDValues[iPos];
		returnedBottle.mQualityFlags[iPos] = -99;
		return returnedBottle;
	}

	public void addParamAndValue(float inVal, short flag) {
		mDValues[mNumVars] = inVal;
		mQualityFlags[mNumVars] = flag;
		mQCValueAssigned[mNumVars] = true;
		mDValuesEdited[mNumVars] = false;
		mQCValuesEdited[mNumVars] = false;
		mNumVars++;
	}

	public boolean isValueEdited(int i) {
		return mDValuesEdited[i];
	}

	public boolean isQCValueEdited(int i) {
		return mQCValuesEdited[i];
	}

	public void setValueEdited(int i, boolean b) {
		mDValuesEdited[i] = b;
	}

	public void setQCValueEdited(int i, boolean b) {
		mQCValuesEdited[i] = b;
		mQCValueAssigned[i] = true;
	}

	public boolean isUseBottle() {
		return mUseBottle;
	}

	public void setUseBottle(boolean b) {
		mUseBottle = b;
	}

	public int getSampNo() {
		return mSampNo;
	}

	public float getRawCTD() {
		return mRawCTDValue;
	}

	public void setSampNo(int s) {
		mSampNo = s;
	}

	public void setRawCTD(float f) {
		mRawCTDValue = f;
	}

	public int getBottleNum() {
		return mBottleNum;
	}

	public void setBottleNum(int s) {
		mBottleNum = s;
	}

	public void setRawCTDMeasured(boolean b) {
		mIsRawCTDMeasured = b;
	}

	public boolean isRawCTDMeasured() {
		return mIsRawCTDMeasured;
	}

	public void setSampNoUsed(boolean b) {
		mIsSampNoUsed = b;
	}

	public boolean isSampNoUsed() {
		return mIsSampNoUsed;
	}

	public void setBottleNumUsed(boolean b) {
		mIsSBottleNumUsed = b;
	}

	public boolean isBottleNumUsed() {
		return mIsSBottleNumUsed;
	}

	/**
	 * Compares the given DataPoint object to the current DataPoint object, and
	 * returns true if, and only if, the two data points represent the same data
	 * point. That is, the dependent value matches for the matching independent
	 * values.
	 * 
	 * @param dp
	 *          the DataPoint to compare this DataPoint object to.
	 * @return true if the given DataPoint object represents the same data point
	 *         as this DataPoint object.
	 */
	public boolean equals(DataPoint dp) {
		return getIndependentValue(mIndVarName) == dp.getIndependentValue(mIndVarName)
		    && getDependentValue() == dp.getDependentValue();
	}

	public double getDependentValue() {
		// assign the canonical salinity
		if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.BOTTLE_SALINITY) {
			int sPos = mParentSection.getVarPos("SALT", true);

			if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
				sPos = mParentSection.getVarPos("CTDS", true);
				if (sPos >= 0) {
					mSaltVal = mDValues[sPos];

					if (mSaltVal == JOAConstants.MISSINGVALUE) {
						mSaltVal = Double.NaN;
					}
				}
				else {
					mSaltVal = Double.NaN;
				}
			}
			else {
				mSaltVal = mDValues[sPos];

				if (mSaltVal == JOAConstants.MISSINGVALUE) {
					mSaltVal = Double.NaN;
				}
			}
		}
		else if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.CTD_SALINITY) {
			int sPos = mParentSection.getVarPos("CTDS", true);

			if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
				sPos = mParentSection.getVarPos("SALT", true);
				if (sPos >= 0) {
					mSaltVal = mDValues[sPos];

					if (mSaltVal == JOAConstants.MISSINGVALUE) {
						mSaltVal = Double.NaN;
					}
				}
				else {
					mSaltVal = Double.NaN;
				}
			}
			else {
				mSaltVal = mDValues[sPos];

				if (mSaltVal == JOAConstants.MISSINGVALUE) {
					mSaltVal = Double.NaN;
				}
			}
		}
		if (mSaltVal == JOAConstants.MISSINGVALUE) {
			System.out.println("-99 leaked");
		}
		return mSaltVal;
	}

	public double getIndependentValue(String name) {
		if (name.equalsIgnoreCase("temp")) {
			int tPos = mParentSection.getVarPos("TEMP", true);
			if (tPos >= 0) {
				mTempVal = mDValues[tPos];

				if (mTempVal == JOAConstants.MISSINGVALUE) {
					mTempVal = Double.NaN;
				}
			}
			else {
				mTempVal = Double.NaN;
			}
			return mTempVal;
		}
		else if (name.equalsIgnoreCase("sqtemp")) {
			int tPos = mParentSection.getVarPos("TEMP", true);
			if (tPos >= 0) {
				if (mDValues[tPos] != JOAConstants.MISSINGVALUE) {
					mTempVal = mDValues[tPos] * mDValues[tPos];
				}
				else {
					mTempVal = Double.NaN;
				}
			}
			else {
				mTempVal = Double.NaN;
			}
			return mTempVal;
		}
		else if (name.equalsIgnoreCase("lat")) {
			mLatVal = mParentStation.getLat();

			if (mLatVal == JOAConstants.MISSINGVALUE) {
				mLatVal = Double.NaN;
			}
			return mLatVal;
		}
		else if (name.equalsIgnoreCase("lon")) {
			mLonVal = mParentStation.getLon();

			if (mLonVal == JOAConstants.MISSINGVALUE) {
				mLonVal = Double.NaN;
			}

			// todo make sure lon is -180 to 180 (or 0 to 360)
			return mLonVal;
		}
		return Double.NaN;
	}

	public double getIndependentValue(TSModelTermParameter param) {
		if (param == TSModelTermParameter.TEMPERATURE) {
			int tPos = mParentSection.getVarPos("TEMP", true);
			if (tPos >= 0) {
				mTempVal = mDValues[tPos];

				if (mTempVal == JOAConstants.MISSINGVALUE) {
					mTempVal = Double.NaN;
				}
			}
			else {
				mTempVal = Double.NaN;
			}
			return mTempVal;
		}
		else if (param == TSModelTermParameter.SQTEMPERATURE) {
			int tPos = mParentSection.getVarPos("TEMP", true);
			if (tPos >= 0) {
				if (mDValues[tPos] != JOAConstants.MISSINGVALUE) {
					mTempVal = mDValues[tPos] * mDValues[tPos];
				}
				else {
					mTempVal = Double.NaN;
				}
			}
			else {
				mTempVal = Double.NaN;
			}
			return mTempVal;
		}
		else if (param == TSModelTermParameter.LATITUDE) {
			mLatVal = mParentStation.getLat();
			if (mLatVal == JOAConstants.MISSINGVALUE) {
				mLatVal = Double.NaN;
			}
			return mLatVal;
		}
		else if (param == TSModelTermParameter.LONGITUDE) {
			mLonVal = mParentStation.getLon();
			
			if (mLonVal == JOAConstants.MISSINGVALUE) {
				mLonVal = Double.NaN;
			}

			// todo make sure lon is -180 to 180 (or 0 to 360)
			return mLonVal;
		}
		else if (param == TSModelTermParameter.LONxLAT) {
			mLonVal = mParentStation.getLon();
			// todo make sure lon is -180 to 180 (or 0 to 360)
			mLatVal = mParentStation.getLat();
			if (mLonVal == JOAConstants.MISSINGVALUE || mLatVal == JOAConstants.MISSINGVALUE) {
				return Double.NaN;
			}
			else {
				return mLonVal * mLatVal;
			}
		}
		return Double.NaN;
	}

	public String[] getIndependentVariableNames() {
		return mIndVarNames;
	}

	public void setDependentValue(double value) {
		mSaltVal = value;
	}

	public void setIndependentValue(String name, double value) {
		mIndVarName = name;
		if (name.equalsIgnoreCase("temp")) {
			mTempVal = value;
		}
		else if (name.equalsIgnoreCase("lat")) {
			mLatVal = value;
		}
		else if (name.equalsIgnoreCase("lon")) {
			mLonVal = value;
		}
	}
}

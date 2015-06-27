/*
 * $Id: Parameter.java,v 1.3 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

import javaoceanatlas.resources.JOAConstants;

public class Parameter {
  double mActScale, mActOrigin, mPlotMin = 9999999e99, mPlotMax = -9999999e99;
  String mVarLabel = null;
  boolean mWasCalculated, mReverseY;
  int mCastOrObs;
  int mVariant;
  String mUnits = null;
  double mMean;
  double mSdev;
  double mVar;
  int mNumValidValues;
  int mEPICCode;
  boolean mIsEdited = false;
  int mDisplayPrecision = JOAConstants.JOA_DEFAULT_PRECISION;
  int mSignificantDigits = 0;
  boolean mRespectSigDigits = false;

  public Parameter(String varLabel) {
    mVarLabel = new String(varLabel).trim();
  };

  public Parameter(Parameter p) {
    mVarLabel = new String(p.mVarLabel);
    if (p.mUnits != null) {
      mUnits = new String(p.mUnits);
    }
    mActScale = p.mActScale;
    mActOrigin = p.mActOrigin;
    mPlotMin = p.mPlotMin;
    mPlotMax = p.mPlotMax;
    mReverseY = p.mReverseY;
    mWasCalculated = p.mWasCalculated;
    mCastOrObs = p.mCastOrObs;
    mVariant = p.mVariant;
    mEPICCode = p.mEPICCode;
    mDisplayPrecision = p.mDisplayPrecision;
    mSignificantDigits = p.mSignificantDigits;
    mRespectSigDigits = p.mRespectSigDigits;
  }

  public Parameter(String varLabel, String units) {
    mVarLabel = new String(varLabel);
    if (units != null) {
      mUnits = new String(units);
    }
  }

  public Parameter(String varLabel, double actScale, double actOrigin) {
    mVarLabel = new String(varLabel);
    mActScale = actScale;
    mActOrigin = actOrigin;
  }

  public Parameter(String varLabel, double actScale, double actOrigin, String units) {
    mVarLabel = new String(varLabel);
    mActScale = actScale;
    mActOrigin = actOrigin;
    if (units != null) {
      mUnits = new String(units);
    }
  }
  
  public void setEdited(boolean b) {
  	mIsEdited = b;
  }
  
  public boolean isEdited() {
  	return mIsEdited;
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
  
  public boolean isRespectSignificantDigits() {
  	return mRespectSigDigits;
  }
  
  public void setRespectSignificantDigits(boolean b) {
  	mRespectSigDigits = b;
  }

  public String getVarLabel() {
    return mVarLabel;
  }

  public void setVarLabel(String s) {
    mVarLabel = new String(s);
  }

  public String getUnits() {
    return mUnits;
  }

  public void setUnits(String s) {
    mUnits = new String(s);
  }

  public double getPlotMin() {
    return mPlotMin;
  }

  public double getPlotMax() {
    return mPlotMax;
  }

  public void setPlotMin(double d) {
    mPlotMin = d;
  }

  public void setPlotMax(double d) {
    mPlotMax = d;
  }

  public boolean isCalculated() {
    return mWasCalculated;
  }

  public boolean isReverseY() {
    return mReverseY;
  }

  public void setReverseY(boolean b) {
    mReverseY = b;
  }

  public int getCastOrObs() {
    return mCastOrObs;
  }

  public int getVariant() {
    return mVariant;
  }

  public double getMean() {
    return mMean;
  }

  public double getSdev() {
    return mSdev;
  }

  public double getVar() {
    return mVar;
  }

  public int getNumValidValues() {
    return mNumValidValues;
  }

  public double getActScale() {
    return mActScale;
  }

  public double getActOrigin() {
    return mActOrigin;
  }

  public void setActScale(double d) {
    mActScale = d;
  }

  public void setActOrigin(double d) {
    mActOrigin = d;
  }

  public void setCastOrObs(int i) {
    mCastOrObs = i;
  }

  public void setWasCalculated(boolean b) {
    mWasCalculated = b;
  }

  public boolean isWasCalculated() {
    return mWasCalculated;
  }

  public void setEPICCode(int ec) {
    mEPICCode = ec;
  }

  public int getEPICCode() {
    return mEPICCode;
  }
}

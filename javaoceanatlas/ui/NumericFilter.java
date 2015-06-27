/**
 * 
 */
package javaoceanatlas.ui;

import javaoceanatlas.classicdatamodel.Bottle;
import javaoceanatlas.resources.JOAConstants;

/**
 * @author oz
 *
 */
public class NumericFilter {
	private double mMax;
	private double mMin;
	private boolean mMinUsed = true;
	private boolean mMaxUsed = true;
	
	public NumericFilter(double min, double max, boolean minDefined, boolean maxDefined) {
		mMin = min;
		mMax = max;
		mMinUsed = minDefined;
		mMaxUsed = maxDefined;
	}
	
	public boolean test(Bottle bh, int vPos) {
		double tstVal = bh.mDValues[vPos];
		if (tstVal == JOAConstants.MISSINGVALUE)
			return false;
		
		if (mMinUsed && tstVal < mMin) {
			return false;
		}
		
		if (mMaxUsed && tstVal > mMax) {
			return false;
		}
		
		return true;
	}
	
	public String toLblString(String param) {
		String outStr = "";
		
		if (mMinUsed) {
			outStr = String.valueOf(mMin)  + "<= ";
		}
		
		outStr += param;
		
		if (mMaxUsed) {
			outStr += " <= " + String.valueOf(mMax);
		}
		return outStr;
	}

	public void setMin(double mMin) {
	  this.mMin = mMin;
  }

	public double getMin() {
	  return mMin;
  }

	public void setMax(double mMax) {
	  this.mMax = mMax;
  }

	public double getMax() {
	  return mMax;
  }

	public void setMinUsed(boolean mMinUsed) {
	  this.mMinUsed = mMinUsed;
  }

	public boolean isMinUsed() {
	  return mMinUsed;
  }

	public void setMaxUsed(boolean mMaxUsed) {
	  this.mMaxUsed = mMaxUsed;
  }

	public boolean isMaxUsed() {
	  return mMaxUsed;
  }
}

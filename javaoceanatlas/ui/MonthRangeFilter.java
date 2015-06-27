/**
 * 
 */
package javaoceanatlas.ui;

import javaoceanatlas.classicdatamodel.Station;

/**
 * @author oz
 *
 */
public class MonthRangeFilter implements JOATimeFilter {
	private int mMaxMonth;
	private int mMinMonth;
	boolean mMinUsed = true;
	boolean mMaxUsed = true;
	
	public MonthRangeFilter(int minDate, int mMaxDate, boolean minDefined, boolean maxDefined) {
		mMinMonth = minDate;
		mMaxMonth = mMaxDate;
		mMinUsed = minDefined;
		mMaxUsed = maxDefined;
	}

	public boolean test(Station sh) {
		long tstMonth = sh.getMonth();
		if (mMinUsed && tstMonth < mMinMonth) {
			return false;
		}
		
		if (mMaxUsed && tstMonth > mMaxMonth) {
			return false;
		}
		
		return true;
	}

	public void setMinMonth(int mMinMonth) {
	  this.mMinMonth = mMinMonth;
  }

	public int getMinMonth() {
	  return mMinMonth;
  }

	public void setMaxMonth(int mMaxMonth) {
	  this.mMaxMonth = mMaxMonth;
  }

	public int getMaxMonth() {
	  return mMaxMonth;
  }
}

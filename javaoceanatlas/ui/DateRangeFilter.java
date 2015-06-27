/**
 * 
 */
package javaoceanatlas.ui;

import gov.noaa.pmel.util.GeoDate;
import javaoceanatlas.classicdatamodel.Station;

/**
 * @author oz
 *
 */
public class DateRangeFilter implements JOATimeFilter {
	private long mMax;
	private long mMin;
	boolean mMinUsed = true;
	boolean mMaxUsed = true;
	
	public DateRangeFilter(GeoDate minDate, GeoDate mMaxDate, boolean minDefined, boolean maxDefined) {
		mMin = minDate.getTime();
		setMax(mMaxDate.getTime());
		mMinUsed = minDefined;
		mMaxUsed = maxDefined;
	}

	public boolean test(Station sh) {
		long tstTime = sh.getDate().getTime();
		if (mMinUsed && tstTime < mMin) {
			return false;
		}
		
		if (mMaxUsed && tstTime > getMax()) {
			return false;
		}
		
		return true;
	}

	public void setMin(long mMin) {
	  this.mMin = mMin;
  }

	public long getMin() {
	  return mMin;
  }

	void setMax(long mMax) {
	  this.mMax = mMax;
  }

	long getMax() {
	  return mMax;
  }
}

/**
 * 
 */
package javaoceanatlas.ui;

import javaoceanatlas.classicdatamodel.Station;

/**
 * @author oz
 *
 */
public class YearDayFilter implements JOATimeFilter {
	int mMinDay, mMaxDay;
	boolean mMinUsed = true;
	boolean mMaxUsed = true;
	
	public YearDayFilter(int minDate, int mMaxDate, boolean minDefined, boolean maxDefined) {
		mMinDay = minDate;
		mMaxDay = mMaxDate;
		mMinUsed = minDefined;
		mMaxUsed = maxDefined;
	}

	public boolean test(Station sh) {
		int yearDay = sh.getDate().getYearday();
		if (mMinUsed && yearDay < mMinDay) {
			return false;
		}
		
		if (mMaxUsed && yearDay > mMaxDay) {
			return false;
		}
		
		return true;
	}
}

/**
 * 
 */
package javaoceanatlas.ui;

import gov.noaa.pmel.util.GeoDate;

/**
 * @author oz
 *
 */
public enum Seasons {
	SPRING("Spring", 0, 80, 171),
	SUMMER("Summer", 1, 172, 263),
	AUTUMN("Autumn", 2, 264, 355),
	WINTER("Winter", 3, 356, 444);

  public final String mSeason;
  public final int mOrd;
  public final int mStartYearDay;
  public final int mEndYearDay;

  Seasons(String season, int ord, int syd, int eyd) {
  	mSeason = season;
  	mOrd = ord;
  	mStartYearDay = syd;
  	mEndYearDay = eyd;
  }
  public String toString() {
    return mSeason;
  }
  
  public static Seasons getSeasonFromOrd(int ord) {
  	if (ord == 0) {
  		return Seasons.SPRING;
  	}
  	else if (ord == 1) {
  		return Seasons.SUMMER;
  	}
  	else if (ord == 2) {
  		return Seasons.AUTUMN;
  	} 
  	else {
  		return Seasons.WINTER;
  	}
  }
  
  public static Seasons getSeason(int yd) {
  	if (isSpring(yd)) {
  		return Seasons.SPRING;
  	}
  	else if (isSummer(yd)) {
  		return Seasons.SUMMER;
  	}
  	else if (isAutumn(yd)) {
  		return Seasons.AUTUMN;
  	} 
  	else {
  		return Seasons.WINTER;
  	}
  }
  
  public static boolean isSpring(int yd) {
  	if (yd >= SPRING.mStartYearDay && yd < SPRING.mEndYearDay) {
  		return true;
  	}
  	return false;
  }
  
  public static boolean isSummer(int yd) {
  	if (yd >= SUMMER.mStartYearDay && yd < SUMMER.mEndYearDay) {
  		return true;
  	}
  	return false;
  }
  
  public static boolean isAutumn(int yd) {
  	if (yd >= AUTUMN.mStartYearDay && yd < AUTUMN.mEndYearDay) {
  		return true;
  	}
  	return false;
  }
  
  public static boolean isWinter(int yd) {
  	int cyd = yd < 80 ? yd + 365 : yd;
  	if (cyd >= WINTER.mStartYearDay && cyd < WINTER.mEndYearDay) {
  		return true;
  	}
  	return false;
  }
  
  public int getOrd() {
  	return mOrd;
  }
}

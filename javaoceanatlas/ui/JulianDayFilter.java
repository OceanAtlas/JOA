package javaoceanatlas.ui;

import java.util.Calendar;
import gov.noaa.pmel.util.GeoDate;
import javaoceanatlas.classicdatamodel.Station;

/**
 * @author oz
 *
 */
public class JulianDayFilter implements JOATimeFilter {
	int mMinMonth, mMaxMonth;
	boolean mMinUsed = true;
	boolean mMaxUsed = true;
	
	public JulianDayFilter(int minDate, int mMaxDate, boolean minDefined, boolean maxDefined) {
		mMinMonth = minDate;
		mMaxMonth = mMaxDate;
		mMinUsed = minDefined;
		mMaxUsed = maxDefined;
	}

	public boolean test(Station sh) {
		// todo convert to Julian date
		GeoDate gd = sh.getDate();
		double jd = toJulian(gd);
		long tstMonth = sh.getMonth();
		if (mMinUsed && tstMonth < mMinMonth) {
			return false;
		}
		
		if (mMaxUsed && tstMonth > mMaxMonth) {
			return false;
		}
		
		return true;
	}
	 /**
	  * Returns the Julian day number that begins at noon of
	  * this day, Positive year signifies A.D., negative year B.C.
	  * Remember that the year after 1 B.C. was 1 A.D.
	  *
	  * ref :
	  *  Numerical Recipes in C, 2nd ed., Cambridge University Press 1992
	  */
	  // Gregorian Calendar adopted Oct. 15, 1582 (2299161)
	  public static int JGREG= 15 + 31*(10+12*1582);
	  public static double HALFSECOND = 0.5;
	  
	  public static double toJulian(GeoDate gd) {
	   int year=gd.getYear();
	   int month=gd.getMonth(); // jan=1, feb=2,...
	   int day=gd.getDay();    
	   int julianYear = year;
	   if (year < 0) julianYear++;
	   int julianMonth = month;
	   if (month > 2) {
	     julianMonth++;
	   }
	   else {
	     julianYear--;
	     julianMonth += 13;
	   }
	   
	   double julian = (java.lang.Math.floor(365.25 * julianYear)
	        + java.lang.Math.floor(30.6001*julianMonth) + day + 1720995.0);
	   if (day + 31 * (month + 12 * year) >= JGREG) {
	     // change over to Gregorian calendar
	     int ja = (int)(0.01 * julianYear);
	     julian += 2 - ja + (0.25 * ja);
	   }
	   return java.lang.Math.floor(julian);
	 }
	 
	 /**
	 * Converts a Julian day to a calendar date
	 * ref :
	 * Numerical Recipes in C, 2nd ed., Cambridge University Press 1992
	 */
	 public static GeoDate fromJulian(double injulian) {
	   int jalpha,ja,jb,jc,jd,je,year,month,day;
	   double julian = injulian + HALFSECOND / 86400.0;
	   ja = (int) injulian;
	   if (ja>= JGREG) {    
	     jalpha = (int) (((ja - 1867216) - 0.25) / 36524.25);
	     ja = ja + 1 + jalpha - jalpha / 4;
	   }
	   
	   jb = ja + 1524;
	   jc = (int) (6680.0 + ((jb - 2439870) - 122.1) / 365.25);
	   jd = 365 * jc + jc / 4;
	   je = (int) ((jb - jd) / 30.6001);
	   day = jb - jd - (int) (30.6001 * je);
	   month = je - 1;
	   if (month > 12) month = month - 12;
	   year = jc - 4715;
	   if (month > 2) year--;
	   if (year <= 0) year--;
	   
	   try {
	   GeoDate gd = new GeoDate(month, day, year, 0, 0, 0, 0);
	   
	   return new GeoDate(month, day, year, 0, 0, 0, 0);
	   }
	   catch (Exception ex) {
	  	 ex.printStackTrace();
	  	 return null;
	   }
	  }

public static void main(String args[]) {
	try {
 // FIRST TEST reference point
 System.out.println("Julian date for May 23, 1968 : "
    + toJulian(new GeoDate(5, 23, 1968, 0, 0, 0, 0)));//new int[] {1968, 5, 23 } ));
 // output : 2440000
 GeoDate result = fromJulian(toJulian(new GeoDate(5, 23, 1968, 0, 0, 0, 0)));
 System.out.println
   ("... back to calendar : " + result.getMonth() + " "
     + result.getDay() + " " + result.getYear());
     
 // SECOND TEST today    
 Calendar today = Calendar.getInstance();
 GeoDate todaygd = new GeoDate(today.get(Calendar.MONTH)+1, today.get(Calendar.DATE), today.get(Calendar.YEAR), 0, 0, 0, 0);
 double todayJulian = toJulian(todaygd);
 System.out.println("Julian date for today : " + todayJulian);
 result = fromJulian(todayJulian);
 System.out.println
 ("... back to calendar : " + result.getMonth() + " "
   + result.getDay() + " " + result.getYear());
     
 // THIRD TEST
 double date1 = toJulian(new GeoDate(1, 1, 2005, 0, 0, 0, 0));//new int[]{2005,1,1});
 double date2 = toJulian(new GeoDate(1, 31, 2005, 0, 0, 0, 0));//new int[]{2005,1,31});    
 System.out.println("Between 2005-01-01 and 2005-01-31 : "
    + (date2 - date1) + " days");
 
 GeoDate startSpring = new GeoDate(3, 21, 1970, 1, 0, 0, 0);
 System.out.println("Spring starts: " + startSpring.getYearday());
 GeoDate startSummer = new GeoDate(6, 21, 1970, 1, 0, 0, 0);
 System.out.println("summer starts: " + startSummer.getYearday());
 GeoDate startFall = new GeoDate(9, 21, 1970, 1, 0, 0, 0);
 System.out.println("fall starts: " + startFall.getYearday());
 GeoDate startWinter = new GeoDate(12, 21, 1970, 1, 0, 0, 0);
 System.out.println("winter starts: " + startWinter.getYearday());
 
 GeoDate ozBD = new GeoDate(5, 11, 2005, 0, 0, 0, 0);
Seasons tstSeason = Seasons.getSeason(ozBD.getYearday());
System.out.println("oz born in: " + tstSeason);

 GeoDate mdoBD = new GeoDate(1, 28, 2005, 0, 0, 0, 0);
tstSeason = Seasons.getSeason(mdoBD.getYearday());
System.out.println("MDO born in: " + tstSeason);

 GeoDate mkoBD = new GeoDate(7, 21, 2005, 0, 0, 0, 0);
tstSeason = Seasons.getSeason(mkoBD.getYearday());
System.out.println("MKO born in: " + tstSeason);

 GeoDate mattBD = new GeoDate(10, 24, 2005, 0, 0, 0, 0);
tstSeason = Seasons.getSeason(mattBD.getYearday());
System.out.println("Matt born in: " + tstSeason);
 
 /*
    expected output :
       Julian date for May 23, 1968 : 2440000.0
       ... back to calendar 1968 5 23
       Julian date for today : 2453487.0
       ... back to calendar 2005 4 26
       Between 2005-01-01 and 2005-01-31 : 30.0 days
 */
	}
	catch (Exception ex) {
		ex.printStackTrace();
	}
 }
}

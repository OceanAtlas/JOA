/**
 *
 */
package gov.noaa.pmel.eps2;

import ucar.nc2.*;
import ucar.ma2.Array;
import java.io.IOException;
import gov.noaa.pmel.util.GeoDate;
import gov.noaa.pmel.util.IllegalTimeValue;

/**
 * supplies common functions for DODSNcFile and LocalNcFile
 *
 * @author Donald Denbo
 * @version $Revision: 1.2 $, $Date: 2004/06/30 04:40:30 $
 */
public class NcUtil {
  private boolean is624_;
  private int[] time2_ = null;
  private GeoDate refDate_ = null;
  private int increment_;

  /**
   * @label ncFile_ 
   */
  NcFile ncFile_;
  static String tFormat_ = "yyyy-MM-dd HH:mm:ss";

  public NcUtil(NcFile ncFile) {
    ncFile_ = ncFile;
  }

  public boolean isVariableTime(Variable var) {
    Attribute attr;
    Attribute epic_code;
    //
    // if the units include "since" assume time
    //
    attr = var.findAttribute("units");
    if(attr != null) {
      if(attr.getStringValue().indexOf("since") != -1) return true;
    }
    //
    // if the epic_code is 624 it's time
    //
    epic_code = var.findAttribute("epic_code");
    if(epic_code != null) {
      if(epic_code.getNumericValue().intValue() == 624) return true;
    }
    return false;
  }

  public Object getArray(Variable var, int[] origin, int[] shape) {
    Object array = null;

    return array;
  }

  public Object getArrayValue(Variable var, int index) {
    Attribute attr;
    String ref;
    String units;
    int pos;
    GeoDate date;
    Object value = null;
    Object anArray = null;
    try {
      Array arr = var.read();
      anArray = arr.copyTo1DJavaArray();
    } catch (IOException e) {
      System.out.println(e);
      return null;
    }
    if(isVariableTime(var)) {
      is624_ = false;
      attr = var.findAttribute("units");
      if((attr != null) &&
         ((pos = attr.getStringValue().indexOf("since")) != -1)) {

        units = attr.getStringValue();
        ref = units.substring(pos + 5).trim();
        int len = Math.min(ref.length(), tFormat_.length());
        try {
          refDate_ = new GeoDate(ref, tFormat_.substring(0, len));
        } catch (IllegalTimeValue e) {
          System.out.println(e);
          try {
            refDate_ = new GeoDate("1970-01-01 00:00:00",
                                   "yyyy-MM-dd HH:mm:ss");
          } catch (IllegalTimeValue ee) {}
          System.out.println("   Setting default reference date: " +
                             refDate_.toString());
        }

        date = new GeoDate(refDate_);

        if(units.indexOf("second") != -1) {
          increment_ = GeoDate.SECONDS;
        } else if(units.indexOf("min") != -1) {
          increment_ = GeoDate.MINUTES;
        } else if(units.indexOf("hour") != -1) {
          increment_ = GeoDate.HOURS;
        } else if(units.indexOf("day") != -1) {
          increment_ = GeoDate.DAYS;
        } else if(units.indexOf("month") != -1) {
          increment_ = GeoDate.MONTHS;
        } else if(units.indexOf("year") != -1) {
          increment_ = GeoDate.YEARS;
        } else if(units.indexOf("msec") != -1) {
          increment_ = GeoDate.MSEC;
        } else {
          increment_ = GeoDate.SECONDS;
        }

        if(anArray instanceof long[]) {
          date.increment((double)((long[])anArray)[index], increment_);
        } else if(anArray instanceof int[]) {
          date.increment((double)((int[])anArray)[index], increment_);
        } else if(anArray instanceof short[]) {
          date.increment((double)((short[])anArray)[index], increment_);
        } else if(anArray instanceof float[]) {
          date.increment((double)((float[])anArray)[index], increment_);
        } else if(anArray instanceof double[]) {
          date.increment(((double[])anArray)[index], increment_);
        } else {
          return null;
        }
        return date;
      }
      attr = var.findAttribute("epic_code");
      if(attr != null) {
        if(attr.getNumericValue().intValue() == 624) {
          is624_ = true;
          //
          // time is a double integer
          //
          Variable time2 = ncFile_.findVariable(var.getName() + "2");
          try {
            Array arr = time2.read();
            time2_ = (int[])arr.copyTo1DJavaArray();
          } catch (IOException e) {
            System.out.println(e);
            return null;
          }
          int jday = ((int[])anArray)[index];
          int msec = time2_[index];
          return new GeoDate(jday, msec);
        }
      }
      value = null;
    } else {
      if(anArray instanceof long[]) {
        value = new Long(((long[])anArray)[index]);
      } else if(anArray instanceof int[]) {
        value = new Integer(((int[])anArray)[index]);
      } else if(anArray instanceof short[]) {
        value = new Short(((short[])anArray)[index]);
      } else if(anArray instanceof float[]) {
        value = new Float(((float[])anArray)[index]);
      } else if(anArray instanceof double[]) {
        value = new Double(((double[])anArray)[index]);
      } else {
        value = null;
      }
    }
    return value;
  }

  public boolean is624() {
    return is624_;
  }

  public int[] getTime2() {
    return time2_;
  }

  public GeoDate getRefDate() {
    return refDate_;
  }

  public int getIncrement() {
    return increment_;
  }

  static public String valueAsString(Object value) {
    if(value == null) {
      return "decode error";
    } else if(value instanceof Long) {
      return ((Long)value).toString();
    } else if(value instanceof GeoDate) {
      return ((GeoDate)value).toString(tFormat_);
    } else if(value instanceof Integer) {
      return ((Integer)value).toString();
    } else if(value instanceof Short) {
      return ((Short)value).toString();
    } else if(value instanceof Float) {
      return ((Float)value).toString();
    } else if(value instanceof Double) {
      return ((Double)value).toString();
    } else {
      return "unsupported type";
    }
  }

}


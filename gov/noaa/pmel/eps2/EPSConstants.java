package gov.noaa.pmel.eps2;

import java.awt.*;

/**
 * <code>EPSConstants</code> 
 * Various symbolic constants used by EPS
 *
 * @author oz
 * @version 1.0
 */
public interface EPSConstants {
  public static int MISSINGVALUE = -99;
  public static char COLON_DELIMITER = ':';
  public static char TAB_DELIMITER = '\t';
  public static char COMMA_DELIMITER = ',';
  public static String SCOLON_DELIMITER = ":";
  public static String STAB_DELIMITER = "\t";
  public static String SCOMMA_DELIMITER = ",";
  public static String SHYPHEN_DELIMITER = "-";
  public static String SSPACE_DELIMITER = " ";
  public static String SPERIOD_DELIMITER = ".";
  public static String SSLASH_DELIMITER = "/";
  public static String SBACKSLASH_DELIMITER = "\\";
  public static String SEQUAL_DELIMITER = "=";
  /**
   * 8 bit integer
   */
  public static int EPBYTE = 0;
  /**
   * Sring data
   */
  public static int EPCHAR = 1;
  /**
   * 16 bit integer
   */
  public static int EPSHORT = 2;
  /**
   * 32 bit integer
   */
  public static int EPINT = 3;
  /**
   * 32 bit floating point
   */
  public static int EPREAL = 4;
  /**
   * 64 bit floating point
   */
  public static int EPDOUBLE = 5;
  public static int MAX_NC_DIMS = 100;
  public static int EPNONEAXIS = -1;
  public static int EPSPACE = 0;
  public static int EPTIME = 1;
  public static int EPXAXIS = 2;
  public static int EPYAXIS = 3;
  public static int EPZAXIS = 4;
  public static int EPTAXIS = 5;
  public static int EPINTT = 6;
  public static int EPREALT = 7;
  public static int EPTDUMMY = 8;
  public static int EPXDUMMY = 9;
  public static int EPYDUMMY = 10;
  public static int EPZDUMMY = 11;
  public static int CANONICALTIME = 12;
  public static int PROFILEPTRS = 1;
  public static int TSPTRS = 2;
  public static int SIMPLEPTRS = 3;
  public static int POASECTION = 4;
  public static int WOCESECTION = 5;
  public static int SD2SECTION = 6;
  public static int SD3SECTION = 7;
  public static int SSSECTION = 8;
  public static int XMLPTRS = 9;
  public static int ARGOPTRS = 10;
  public static int GTSPPPTRS = 11;
  /**
   * Sort criteria<br>
   */
  public static int X_ASC = 1;
  public static int X_DSC = 2;
  public static int Y_ASC = 3;
  public static int Y_DSC = 4;
  public static int Z_ASC = 5;
  public static int Z_DSC = 6;
  public static int T_ASC = 7;
  public static int T_DSC = 8;
  public static boolean NC_NOWRITE = true;  
  public static boolean NC_WRITE = false;
  public static int NC_UNSPECIFIED = 0;/* private */
  public static int NC_BYTE = 1;
  public static int NC_CHAR = 2;
  public static int NC_SHORT = 3;
  public static int NC_LONG = 4;
  public static int NC_FLOAT = 5;
  public static int NC_DOUBLE = 6;
  public static int NC_BITFIELD = 7;
  public static int NC_STRING = 8;
  public static int NC_IARRAY = 9;
  public static int NC_DIMENSION = 10;
  public static int NC_VARIABLE = 11;
  public static int NC_ATTRIBUTE = 12;
 	
  public static long GREGORIAN = (15+31*(10+12*1582));
  public static long JULGREG = 299161;
 	
  /**
   * "CRUISE" CTD attribute<br>
   * Cruise number
   */
  public static String EPCRUISE = "CRUISE";
  /**
   * "CAST" CTD attribute<br>
   * cast number
   */
  public static String EPCAST = "CAST";
  /**
   * "INST_TYPE" required EPIC attribute
   * instrument type
   */
  public static String EPINSTTYPE = "INST_TYPE";
  /**
   * "CREATION_DATE"
   * file creation date
   */
  public static String EPCRDATE = "CREATION_DATE";
  /**
   * "BOTTLE" CTD attribute<br>
   * Bottle data flag (=1 if bottle)
   */
  public static String EPBOTTLE = "BOTTLE";
  /**
   * 
   */
  public static String EPCASTNUM = "CAST_NUMBER";
  /**
   * "DATA_TYPE" required EPIC attribute<br>
   * legal values include "CTD", "TIME", "TRACK",
   * and "GRID"
   */
  public static String EPDATATYPE = "DATA_TYPE";
  /**
   * "DATA_SUBTYPE" required EPIC attribute
   */
  public static String EPSUBTYPE = "DATA_SUBTYPE";
  /**
   * "EXPERIMENT" Time Series attribute<br>
   * experiment name
   * legal values include "CTD" and "BOTTLE"
   */
  public static String EPEXPERIMENT = "EXPERIMENT";
  /**
   * "PROJECT" Time Series attribute <br>
   * Project name
   */
  public static String EPPROJECT = "PROJECT";
  /**
   * "MOORING" Time Series attribute<br>
   * Mooring identification
   */
  public static String EPMOORING = "MOORING";
  /**
   * "DELTA_T" Time Series attribute<br>
   * series delta-t description
   */
  public static String EPDELTAT = "DELTA_T";
  /**
   * "COMPOSITE" Time Series attribute<br>
   */
  public static String EPCOMPOSITE = "COMPOSITE";
  /**
   * "POS_CONST" Time Series attribute<br>
   * consistent position flag (=1 not consistent)
   */
  public static String EPPOSCONST = "POS_CONST";
  /**
   * "DEPTH_CONST" Time Series attribute<br>
   * consistent depth flag (=1 not consistent)
   */
  public static String EPDEPTHCONST = "DEPTH_CONST";
  /**
   * "DATA_ORIGIN" required EPIC attribute
   */
  public static String EPDATAORIGIN = "DATA_ORIGIN";
  /**
   * "DESCRIPT" Time Series attribute<br>
   * text description field
   */
  public static String EPDESCRIPT = "DESCRIPT";
  /**
   * "DATA_CMNT" Time Series attribute<br>
   * data comment
   */
  public static String EPDATACMNT = "DATA_CMNT";
  /**
   * "COORD_SYSTEM" required EPIC attribute<br>
   * value of "GEOGRAPHICAL" for classic EPIC data
   */
  public static String EPCOORDSYS = "COORD_SYSTEM";
  /**
   * "WATER_MASS" CTD and Time Series attribute <br>
   * Water Mass flag <br>
   * <p align="left">The WATER MASS attribute is used 
   * by EPIC contouring programs to decide the default
   * contouring level. Refering to following table for valid 
   * Water Mass flags:</p>
   *  <table border="1">
   *<tr> 
   *  <td>FLAG</td>
   *  <td>Default contouring level used for water mass at region</td>
   *</tr>
   *<tr> 
   *  <td> 
   *    <div align="center">"B"</div>
   *  </td>
   *  <td> 
   *    <div align="center">Bering Sea</div>
   *  </td>
   *</tr>
   *<tr> 
   *  <td> 
   *    <div align="center">"G"</div>
   *  </td>
   *  <td> 
   *    <div align="center">Gulf of Alaska</div>
   *  </td>
   *</tr>
   *<tr> 
   *  <td> 
   *    <div align="center">"S" </div>
   *  </td>
   *  <td> 
   *    <div align="center">Shelikof </div>
   *  </td>
   *</tr>
   *<tr> 
   *  <td> 
   *    <div align="center">"V"</div>
   *  </td>
   *  <td> 
   *    <div align="center">Vents </div>
   *  </td>
   *</tr>
   *<tr> 
   *  <td> 
   *    <div align="center">"P"</div>
   *  </td>
   *  <td> 
   *    <div align="center">Puget Sound</div>
   *  </td>
   *</tr>
   *<tr> 
   *  <td> 
   *    <div align="center">"E"</div>
   *  </td>
   *  <td> 
   *    <div align="center">Equatorial</div>
   *  </td>
   *</tr>
   *<tr> 
   *  <td> 
   *    <div align="center">" " </div>
   *  </td>
   *  <td> 
   *    <div align="center">Equatorial</div>
   *  </td>
   *</tr>
   *  </table>
   *
   */
  public static String EPWATERMASS  = "WATER_MASS";
  /**
   * "DRIFTER" Time Series attribute<br>
   * drifter flag (=1 if drifter)
   */
  public static String EPDRIFTER = "DRIFTER";
  /**
   * "WEATHER" CTD attribute<br>
   * Weather code (0-9)
   */
  public static String EPWEATHER = "WEATHER";
  /**
   * "SEA_STATE" CTD attribute<br>
   * Sea state code (0-9)
   */
  public static String EPSEASTATE = "SEA_STATE";
  /**
   * "BAROMETER" CTD attribute<br>
   * Atmospheric pressure in millibars
   */
  public static String EPBAROMETER = "BAROMETER";
  /**
   * "WIND_DIR" CTD attribute<br>
   * Wind direction in degrees from north
   */
  public static String EPWINDDIR = "WIND_DIR";
  /**
   * "WIND_SPEED" CTD attribute<br>
   * Wind speed in knots
   */
  public static String EPWINDSPEED = "WIND_SPEED";
  /**
   * "VISIBILITY" CTD attribute<br>
   * Visibility code (0-9)
   */
  public static String EPVISIBILITY = "VISIBILITY";
  /** 
   * "CLOUD_TYPE" CTD attribute<br>
   * Cloud type code (0-9, X)
   */
  public static String EPCLOUDTYPE = "CLOUD_TYPE";
  /**
   * "CLOUD_AMOUNT" CTD attribute<br>
   * Cloud amount code (0-9)
   */
  public static String EPCLOUDAMOUNT = "CLOUD_AMOUNT";
  /**
   * "AIR_TEMP" CTD attribute<br>
   * Dry air temperature
   */
  public static String EPAIRTEMP = "AIR_TEMP";
  /**
   * "WET_BULB" CTD attribute<br>
   * Web bulb temperature
   */
  public static String EPWETBULB = "WET_BULB";
  /**
   * "WATER_DEPTH" CTD and Time Series attribute<br>
   * Water depth to nearest meter
   */
  public static String EPWATERDEPTH = "WATER_DEPTH";
  /**
   * "VAR_DESC" Time Series attribute<br>
   * text field describing variables
   */
  public static String EPVARDESC = "VAR_DESC";
  /**
   * "FILL_FLAG" Time Series attribute<br>
   * data fill flag (=1 if data has fill values)
   */
  public static String EPFILLFLAG = "FILL_FLAG";
  /**
   * "VAR_FILL" Time Series attribute<br>
   * character string
   */
  public static String EPVARFILL = "VAR_FILL";
  /**
   * "PROG_CMNT1" latest program comment
   */
  public static String EPPROGCMNT1 = "PROG_CMNT1";
  /**
   * "PROG_CMNT2" next program comment
   */
  public static String EPPROGCMNT2 = "PROG_CMNT2";
  /**
   * "PROG_CMNT3" next program comment
   */
  public static String EPPROGCMNT3 = "PROG_CMNT3";
  /**
   * "PROG_CMNT4" oldest program comment
   */
  public static String EPPROGCMNT4 = "PROG_CMNT4";
  /**
   * "LAT_PC" optional Time Series attribute<br>
   * piece measurement latitude
   */
  public static String EPLATPC = "LAT_PC";
  /**
   * "LONG_PC" optional Time Series attribute<br>
   * piece measurement longitude
   */
  public static String EPLONGPC = "LONG_PC";
  /**
   * "START_PC" optional Time Series attribute<br>
   * piece start time and date
   */
  public static String EPSTARTPC = "START_PC";
  /**
   * "END_PC" optional Time Series attribute<br>
   * piece stop time and date
   */
  public static String EPENDPC = "END_PC";
  /**
   * "INST_DEPTH_PC" optional Time Series attribute<br>
   * piece measurement depth (m)
   */
  public static String EPINSTDEPTHPC = "INST_DEPTH_PC";
  /**
   * "INDENT_PC" optional Time Series attribute<br>
   * piece identification
   */
  public static String EPIDENTPC = "IDENT_PC";
  /**
   * "IMAGE" card image from classic EPIC data file
   */
  public static String EPIMAGE = "IMAGE";
  /**
   * Evenly spaced data values in the axis
   */
  public static String EPEVEN = "EVEN";
  /**
   * Unevenly spaced data values in the axis
   */
  public static String EPUNEVEN = "UNEVEN";
  /**
   * Evenly spaced data values in the axis
   */
  public static String EPEVEN_INT = "EVENI";
  /**
   * Evenly spaced data values in the axis
   */
  public static String EPEVEN_REAL = "EVENR";
  /**
   * Unevenly spaced data values in the axis
   */
  public static String EPUNEVEN_INT = "UNEVENI";
  /**
   * Unevenly spaced data values in the axis
   */
  public static String EPUNEVEN_REAL = "UNEVENR";
  /**
   * File open for read only
   */
  public static int EPREAD = 0;
  /**
   * File open for create
   */
  public static int EPCREATE = 1;
  /**
   * File open for edit (append or overwrite)
   */
  public static int EPEDIT = 2;
  /**
   * Synonyms for the x axis
   */
  public static String[] xname = {"x",
				  "longitude",
				  null};
  /**
   * Synonyms for the y axis
   */
  public static String[] yname = {"y",
				  "latitude",
				  null};
  /**
   * Synonyms for the z axis
   */
  public static String[] zname = {"z",
				  "depth",
				  "elevation",
				  null};
  /**
   * Synonyms for the y axis units
   */
  public static String[] yunit = {"degree_north",
				  "degrees_north",
				  "degreeN", 
				  "degrees_N",
				  "degreesN",
				  null};
  /**
   * Synonyms for the x axis units
   */
  public static String[] xunit = {"degree_east",
				  "degrees_east",
				  "degree_E",
				  "degreeE", 
				  "degrees_E",
				  "degreesE",
				  null};
  /**
   * Synonyms for the z axis units
   */
  public static String[] zunit = {"z",
				  "depth",
				  "elevation",
				  "dbar",
				  "mbar",
				  "Pa",
				  null};
  /**
   * Conversions for time in various units to seconds
   */
  public static TimeToSecond[] time_units = {
    new TimeToSecond("year",        3.1536e+7, false),
    new TimeToSecond("yr"  ,        3.1536e+7, false),
    new TimeToSecond("a"  ,         3.1536e+7, true),
    new TimeToSecond("day",         8.64e+04,   false),
    new TimeToSecond("d",           8.64e+04,   true),
    new TimeToSecond("hour",        3.6e+03,    false),
    new TimeToSecond("hr",          3.6e+03,    false),
    new TimeToSecond("h",           3.6e+03,    true),
    new TimeToSecond("minute",      60,       false),
    new TimeToSecond("min",         60,       false),
    new TimeToSecond("second",      1,       false),
    new TimeToSecond("sec",         1,       false),
    new TimeToSecond("s",           1,       true),
    new TimeToSecond("msec",        1e-3,       false),
    new TimeToSecond("msecs",       1e-3,       false),
    new TimeToSecond("millisecond", 1e-3,       false),
    new TimeToSecond("millisec",    1e-3,       false),
    new TimeToSecond("millis",      1e-3,       true),
    new TimeToSecond("shake",       1e-8,       false),
    new TimeToSecond("sidereal_day",8.616409e4, false),
    new TimeToSecond("sidereal_minute",5.983617e1,false),
    new TimeToSecond("sidereal_second",0.9972696, false),
    new TimeToSecond("sidereal_year",3.155815e7, false),
    new TimeToSecond("tropical_year",3.155693e7, false),
    new TimeToSecond("eon",         3.1536e+16, false),
    new TimeToSecond("fortnight",   1.2096e+06, false),
    new TimeToSecond("0", 0, false)
      };
  static String[] LongMonths = {
    "January", "February", "March",
    "April",   "May",      "June",
    "July",    "August",   "September",
    "October", "November", "December"
  };
  static String[] LongWeekDays = {
    "Sunday", "Monday", "Tuesday", "Wednesday",
    "Thursday", "Friday", "Saturday"
  };
  static int[] max_day = {31,28,31,30,31,30,31,31,30,31,30,31};
  static String[] dummyName = {"tdummy", "zdummy", "ydummy", "xdummy"};
  	
  public static int PTRFILEFORMAT = 0;
  public static int JOAFORMAT = 1;
  public static int POAFORMAT = 2;
  public static int SSFORMAT = 3;
  public static int SD2FORMAT = 4;
  public static int SD3FORMAT = 5;
  public static int WOCEHYDFORMAT = 6;
  public static int WOCECTDFORMAT = 7;
  public static int NETCDFFORMAT = 8;
  public static int ZIPSECTIONFILEFORMAT = 9;
  public static int NETCDFXBTFORMAT = 10;
  public static int DODSNETCDFFORMAT = 11;
  public static int JOPIFORMAT = 12;
  public static int XMLPTRFILEFORMAT = 13;
  public static int GZIPTARSECTIONFILEFORMAT = 14;
  public static int GZIPFILEFORMAT = 15;
  public static int ARGOINVENTORYFORMAT = 16;
  public static int GTSPPINVENTORYFORMAT = 17;
  public static int ARGONODCNETCDFFORMAT = 18;
  public static int XYZFORMAT = 19;
  public static int TEXTFORMAT = 20;
  public static int NQDBFORMAT = 21;
  public static int WODCSVFORMAT = 22;
  public static int ARGOGDACNETCDFFORMAT = 23;
  public static int UNKNOWNFORMAT = 99;
  public static int MATCHES = 0;
  public static int STARTSWITH = 1;
  public static int CONTAINS = 2;
  public static int ALL_OBS = 1;
}

/*
 * $Id: NQueryConstants.java,v 1.19 2005/11/01 21:48:21 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package gov.noaa.pmel.nquery.resources;

import java.awt.Color;
import java.awt.Font;
import gov.noaa.pmel.eps2.Lexicon;
import java.util.Vector;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;

/**
 * <code>NQueryConstants</code> Constants used by NQuery
 *
 * @author oz and Kevin McHugh
 * @version 1.0
 */

public class NQueryConstants {
  // preferences constants
  public static final String formatString = "###.###;###.###";
  public static final String latFormatString = "###.###N;###.###S";
  public static final String lonFormatString = "###.###E;###.###W";
  public static final char DEG = '\u00B0';
  public static final String degreeSymbol = new String(new StringBuffer(0x0000B0));
  public static char IMPORTDELIMITER = '\t';
  public static final int VERTICAL_ORIENTATION = 0;
  public static final int HORIZONTAL_ORIENTATION = 1;
  public static final int DEC_MINUTES_GEO_DISPLAY = 0;
  public static final int DEG_MINUTES_SEC_GEO_DISPLAY = 1;
  public static final int DATE_TIME_DISPLAY = 0;
  public static final int YEAR_DAY_TIME_DISPLAY = 1;
  public static final int MONTH_TIME_DISPLAY = 2;
  public static final int SEASON_TIME_DISPLAY = 3;
  public static String DEFAULT_DATE_FORMAT = "dd-mm-yy";
  public static int DEFAULT_POSITION_FORMAT = DEC_MINUTES_GEO_DISPLAY;
  public static double currTestPres = 0.0;
  public static Color DEFAULT_FRAME_COLOR = Color.white;
  public static Color DEFAULT_CONTENTS_COLOR = Color.lightGray;
  public static Color DEFAULT_MISSINGVAL_COLOR = Color.gray;
  public static Color DEFAULT_BOTTOM_COLOR = Color.black;
  public static boolean DEFAULT_NO_CTD_DECIMATION = true;
  public static boolean DEFAULT_CONSTANT_CTD_DECIMATION = false;
  public static boolean DEFAULT_STD_LEVEL_CTD_DECIMATION = false;
  public static boolean DEFAULT_CUSTOM_CTD_DECIMATION = false;
  public static double DEFAULT_DECIMATE_CONSTANT = 5.0;
  public static int DEFAULT_NUMBER_OF_CUSTOM_TRIPLETS = 0;
  public static String DEFAULT_DECIMATE_STD_LEVEL = new String("");
  public static int CONNECT_LINE_WIDTH = 1;
  public static double SECTION_WIDTH = 200.0;
  public static boolean DEFAULT_CONVERT_WOCE_TEMPS = true;
  public static boolean DEFAULT_CONVERT_QCS = true;
  public static boolean DEFAULT_CONVERT_WOCE_MASS_TO_VOL = true;
  public static boolean DEFAULT_SET_MSG_QBEQ3 = false; //Questionable
  public static boolean DEFAULT_SET_MSG_QBEQ4 = false; //Bad measurement
  public static boolean DEFAULT_SET_MSG_QBEQ7 = false; //Manual CFC
  public static boolean DEFAULT_SET_MSG_QBEQ8 = false; //Irregular CFC
  public static boolean DEFAULT_SET_ALL_PARAMS_MSG_BQBEQ4 = false;
  public static boolean DEFAULT_SET_GAS_PARAMS_MSG_BQBEQ3_AND_O2QBEQ4 = false;
  //public static boolean DEFAULT_MERGE_WOCE_CASTS = false;
  public static boolean DEFAULT_TRANSLATE_PARAM_NAMES = true;
  public static int DEFAULT_AUTOSCALE_COLOR_SCHEME = 0;
  public static boolean DEFAULT_ENHANCE_ENLARGE_CURRENT_SYMBOL = true;
  public static boolean DEFAULT_ENHANCE_REPLACE_CURRENT_SYMBOL = false;
  public static double DEFAULT_ENHANCE_ENLARGE_CURRENT_SYMBOL_BY = 0.5;
  public static double DEFAULT_ENHANCE_ENLARGE_CONTRASTING_SYMBOL_BY = 0.5;
  public static boolean DEFAULT_ENHANCE_USE_CONTRASTING_COLOR = false;
  public static Color DEFAULT_ENHANCE_CONTRASTING_COLOR = Color.red;
  public static int DEFAULT_ENHANCE_CONTRASTING_SYMBOL = 5;
  public static PageFormat DEFAULT_PAGEFORMAT = null;
  public static PrinterJob DEFAULT_PRINTERJOB = null;

  public static String DEFAULT_AXIS_VALUE_FONT = "serif";
  public static String DEFAULT_AXIS_LABEL_FONT = "serif";
  public static String DEFAULT_ISOPYCNAL_LABEL_FONT = "serif";
  public static String DEFAULT_COLORBAR_LABEL_FONT = "serif";
  public static String DEFAULT_PLOT_TITLE_FONT = "sansserif";
  public static String DEFAULT_MAP_VALUE_FONT = "serif";

  public static int DEFAULT_AXIS_VALUE_SIZE = 12;
  public static int DEFAULT_AXIS_LABEL_SIZE = 12;
  public static int DEFAULT_ISOPYCNAL_LABEL_SIZE = 12;
  public static int DEFAULT_COLORBAR_LABEL_SIZE = 12;
  public static int DEFAULT_PLOT_TITLE_SIZE = 16;
  public static int DEFAULT_MAP_VALUE_SIZE = 12;

  public static int DEFAULT_AXIS_VALUE_STYLE = Font.PLAIN;
  public static int DEFAULT_AXIS_LABEL_STYLE = Font.PLAIN;
  public static int DEFAULT_ISOPYCNAL_LABEL_STYLE = Font.PLAIN;
  public static int DEFAULT_COLORBAR_LABEL_STYLE = Font.PLAIN;
  public static int DEFAULT_PLOT_TITLE_STYLE = Font.BOLD + Font.ITALIC;
  public static int DEFAULT_MAP_VALUE_STYLE = Font.PLAIN;

  public static Color DEFAULT_AXIS_VALUE_COLOR = Color.black;
  public static Color DEFAULT_AXIS_LABEL_COLOR = Color.black;
  public static Color DEFAULT_ISOPYCNAL_LABEL_COLOR = Color.black;
  public static Color DEFAULT_COLORBAR_LABEL_COLOR = Color.black;
  public static Color DEFAULT_PLOT_TITLE_COLOR = Color.black;
  public static Color DEFAULT_MAP_VALUE_COLOR = Color.black;

  public static Color DEFAULT_SELECTION_REGION_BG_COLOR = new Color(255, 255, 255, 150);
  public static Color DEFAULT_SELECTION_REGION_OUTLINE_COLOR = new Color(255, 255, 255, 240);

  public static boolean ISMAC = false;
  public static boolean ISMACOSX = false;
  public static boolean ISSUNOS = false;
  public static long BIGLONG = Long.MAX_VALUE;
  public static int ALL_OBS = 1;
  public static int MISSINGVALUE = -99;
  public static double MULTPLECROSSINGS = -99.2;
  public static double OUTSIDERANGEOFEOS = -99.1;
  public static double WOCEMISSINGVALUE = -999.0;
  public static double EPICMISSINGVALUE = 1e35;
  public static boolean USECUSTOMMISSINGVALUE = false;
  public static double CUSTOMMISSINGVALUE = MISSINGVALUE;
  public static boolean CANCELIMPORT = false;
  public static int DISTANCE = 1;
  public static int SEQUENCE = 2;
  public static int LATERAL = 10;
  public static double F = Math.PI / 180.0;
  public static int NEXTSTN = 1;
  public static int PREVSTN = 2;
  public static int NEXTOBS = 3;
  public static int PREVOBS = 4;
  public static int LINEAR = 1;
  public static int EXPONENTIALUP = 2;
  public static int EXPONENTIALDOWN = 3;
  public static int LOGISTIC = 4;
  public static int ASSIGNJUSTCOLORS = 5;
  public static int PROFSEQUENCE = 1;
  public static int PROFDISTANCE = 2;
  public static int PROFLATITUDE = 3;
  public static int PROFLONGITUDE = 4;
  public static int MERCATORPROJECTION = 1;
  public static int MILLERPROJECTION = 2;
  public static int ORTHOGRAPHICPROJECTION = 3;
  public static int MOLLWEIDEPROJECTION = 4;
  public static int LAMBERTEAPROJECTION = 5;
  public static int STEREOPROJECTION = 10;
  public static int NORTHPOLEPROJECTION = 11;
  public static int SOUTHPOLEPROJECTION = 12;
  public static int GCSPROJECTION = 13;
  public static double OFFMAP = 1.E12;
  public static int IOFFMAPN = 32000;
  public static int UP = 1;
  public static int DN = 2;
  public static int NOCOAST = 0;
  public static int COARSERESOLUTION = 1;
  public static int FINERESOLUTION = 2;
  public static int CUSTOMCOAST = 3;
  public static long MAXLONG = 2147483647;
  public static double DBLLONG = 4.61168601e18;
  public static double EPSLN = 1.0e-10;
  public static double HALFPI = (Math.PI * 0.5);
  public static double TWOPI = (Math.PI * 2.0);
  public static double RE_M = 6371228.0;
  public static double RE_KM = 6366.70702;
  public static int MAXVAL = 4;
  public static double GRAVITY = 9.8;
  public static double HOUR = 3600;
  public static double DAY = 86400;
  public static double LAT2DEG = .001;
  public static double DB2PASCALSI = 1.0e4; // decibars to pascals
  public static double SVA2SI = 1.0e-8; // specific volume anomaly to cubic meters per kg
  public static double GIGAJOULES = 1.0e-9; // report heat storage in units of 10^9 Joules.
  public static double THOUSAND = 1.0e-3; // report PEA in units of 10^3
  public static double MILLION = 1.0e-6; // report PEA in units of 10^6
  public static int STYLE_CONTOURS = 1;
  public static int STYLE_FILLED = 2;
  public static int STYLE_FILLED_CONTOURS = 3;
  public static int OFFSET_SEQUENCE = 1;
  public static int OFFSET_DISTANCE = 2;
  public static int OFFSET_LATITUDE = 3;
  public static int OFFSET_LONGITUDE = 4;
  public static int OFFSET_TIME = 5;
  public static int MARKERS_NONE = 1;
  public static int MARKERS_OBSERVATIONS = 2;
  public static int MARKERS_SURFACE_LEVELS = 3;
  public static int OBS_CALC_TYPE = 1;
  public static int INT_CALC_TYPE = 2;
  public static int STN_CALC_TYPE = 3;
  public static int PLUS_OP = 1;
  public static int MINUS_OP = 2;
  public static int TIMES_OP = 3;
  public static int DIVIDE_OP = 4;
  public static int DERIVATIVE_OP = 5;
  public static int LN_OP = 6;
  public static int LOG_OP = 7;
  public static int XSQRD_OP = 8;
  public static int SIN_OP = 9;
  public static int RECIP_OP = 10;
  public static int EXP_OP = 11;
  public static int TENX_OP = 12;
  public static int SQRT_OP = 13;
  public static int COS_OP = 14;
  public static int INTEGRAL_OP = 15;
  public static int PTOZ = 16;
  public static int ZTOP = 17;
  public static int MIXED_LAYER_DIFFERENCE = 1;
  public static int MIXED_LAYER_SURFACE = 2;
  public static int MIXED_LAYER_SLOPE = 3;
  public static int STATION_INTEGRATION = 4;
  public static int NEUTRAL_DENSITY = 5;
  public static int SYMBOL_SQUARE = 1;
  public static int SYMBOL_SQUAREFILLED = 2;
  public static int SYMBOL_CIRCLE = 3;
  public static int SYMBOL_CIRCLEFILLED = 4;
  public static int SYMBOL_DIAMOND = 5;
  public static int SYMBOL_DIAMONDFILLED = 6;
  public static int SYMBOL_TRIANGLE = 7;
  public static int SYMBOL_TRIANGLEFILLED = 8;
  public static int SYMBOL_CROSS1 = 9;
  public static int SYMBOL_CROSS2 = 10;
  public static int CIRCLE_SYMBOL = 11;
  public static int SYMBOL_HORIZONTAL_BAR_SYMBOL = 12;
  public static int SYMBOL_DOWN_ARROW_SYMBOL = 13;
  public static int SYMBOL_RIGHT_ARROW_SYMBOL = 14;
  public static int SYMBOL_VERTICAL_BAR_SYMBOL = 15;
  public static int SYMBOL_UP_ARROW_SYMBOL = 16;
  public static int DEFAULT_CURSOR_SIZE = 8;
  public static Color DEFAULT_CURSOR_COLOR = Color.black;
  public static short TWOBYTES = 32767;
  public static int MATCHES = 0;
  public static int STARTSWITH = 1;
  public static int CONTAINS = 2;
  public static int NONE_QC_STD = 0;
  public static int IGOSS_QC_STD = 1;
  public static int WOCE_QC_STD = 2;
  public static int SEARCH_TOP_DOWN = 1;
  public static int SEARCH_BOTTOM_UP = 2;
  public static int SEARCH_FROM_BOTTOM_AND_TOP = 3;

  public static char TAB_DELIMITER = '\t';
  public static char COMMA_DELIMITER = ',';
  public static char DELIMITER = TAB_DELIMITER;
  public static String MISSING_VALUE_STRING = new String("------");

  public static String DEFAULT_DB_URI = new String("jdbc:mysql://localhost");
  public static String DEFAULT_DB_PORT = new String("3306");
  public static String DEFAULT_DB_PASSWORD = new String("");
  public static String DEFAULT_DB_USERNAME = new String("");
  public static String DEFAULT_DB_SAVE_DIR = System.getProperty("user.dir");
  public static String DEFAULT_DB_NAME = new String("nqueryone");
  public static boolean DEFAULT_CALC_MIN = true;
  public static boolean DEFAULT_CALC_MAX = true;
  public static boolean DEFAULT_CALC_DEPTH_OF_MIN = true;
  public static boolean DEFAULT_CALC_DEPTH_OF_MAX = true;
  public static boolean DEFAULT_MAX_DEPTH_OF_NONMISSING_VAL = true;
  public static boolean DEFAULT_MIN_DEPTH_OF_NONMISSING_VAL = true;
  public static boolean DEFAULT_CALC_AVERAGE = true;
  public static boolean DEFAULT_CALC_N = true;
  public static boolean DEFAULT_CALC_MIN_DEPTH_OF_NONMISSING = true;
  public static boolean DEFAULT_CALC_MAX_DEPTH_OF_NONMISSING = true;
  public static int CALC_MIN = 0;
  public static int CALC_MAX = 1;
  public static int CALC_DEPTH_OF_MIN = 2;
  public static int CALC_DEPTH_OF_MAX = 3;
  public static int MAX_DEPTH_OF_NONMISSING = 4;
  public static int MIN_DEPTH_OF_NONMISSING = 5;
  public static int CALC_AVERAGE = 6;
  public static int CALC_N = 7;
  public static boolean DEFAULT_APPLY_CALCS_TO_USER_CALCS = true;
  public static boolean DEFAULT_TRANSLATE_LEXICON = true;
  public static int DEFAULT_LEXICON = Lexicon.EPIC_LEXICON;
  public static boolean DEFAULT_CONVERT_DEPTH = true;
  public static boolean DEFAULT_CONVERT_O2 = true;
  public static int BOTTLE_SALINITY = 0;
  public static int CTD_SALINITY = 1;
  public static int NO_SALINITY_SUBSTITUTION = 2;
  public static int DEFAULT_SALINITY_VARIABLE = BOTTLE_SALINITY;
  public static int DEFAULT_SALINITY_SUBSTITUTION = CTD_SALINITY;
  public static int BOTTLE_O2 = 0;
  public static int CTD_O2 = 1;
  public static int NO_O2_SUBSTITUTION = 2;
  public static int DEFAULT_O2_VARIABLE = BOTTLE_O2;
  public static int DEFAULT_O2_SUBSTITUTION = CTD_O2;
  public static Lexicon LEXICON = null;
  public static boolean DEFAULT_DEBUG_MODE = false;
  public static Vector DEFAULT_DAPPER_SERVERS = new Vector();
}

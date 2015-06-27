/*
 * $Id: JOAConstants.java,v 1.20 2005/10/18 23:42:18 oz Exp $
 *
 */

package javaoceanatlas.resources;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javaoceanatlas.io.CastIDRule;
import javaoceanatlas.io.CastNumberRule;
import javaoceanatlas.io.ConvertParameterNamesRule;
import javaoceanatlas.io.DepthConversionRule;
import javaoceanatlas.io.DestinationQCRule;
import javaoceanatlas.io.PreferPressureParameterRule;
import javaoceanatlas.io.QCConversionRule;
import javaoceanatlas.io.SectionIDRule;
import javaoceanatlas.io.TempConversionRule;
import javaoceanatlas.specifications.MapSpecification;
import javaoceanatlas.utility.*;
import java.awt.print.*;
import gov.noaa.pmel.eps2.Lexicon;

public class JOAConstants {
  // preferences constants
  public static final String formatString ="###.###;###.###";
  public static final String latFormatString ="###.###N;###.###S";
  public static final String lonFormatString ="###.###E;###.###W";
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
  public static String DEFAULT_DATE_FORMAT ="dd-mm-yy";
  public static int DEFAULT_POSITION_FORMAT = DEC_MINUTES_GEO_DISPLAY;
  public static double currTestPres = 0.0;
  public static Color DEFAULT_FRAME_COLOR = Color.white;
  public static Color DEFAULT_CONTENTS_COLOR = Color.lightGray;
  public static Color DEFAULT_MISSINGVAL_COLOR = Color.gray;
  public static Color DEFAULT_BOTTOM_COLOR = Color.black;
  public static boolean THICKEN_CONTOUR_LINES = false;
  public static boolean THICKEN_OVERLAY_CONTOUR_LINES = false;
  public static boolean DEFAULT_NO_CTD_DECIMATION = true;
  public static boolean DEFAULT_CONSTANT_CTD_DECIMATION = false;
  public static boolean DEFAULT_STD_LEVEL_CTD_DECIMATION = false;
  public static boolean DEFAULT_CUSTOM_CTD_DECIMATION = false;
  public static double DEFAULT_DECIMATE_CONSTANT = 5.0;
  public static Triplet[] DEFAULT_CUSTOM_DECIMATE_TRIPLETS = new Triplet[25];
  public static int DEFAULT_NUMBER_OF_CUSTOM_TRIPLETS = 0;
  public static String DEFAULT_DECIMATE_STD_LEVEL = new String("");
  public static int CONNECT_LINE_WIDTH = 1;
  public static double SECTION_WIDTH = 200.0;
  public static boolean DEFAULT_CONVERT_WOCE_TEMPS = true;
  public static boolean DEFAULT_CONVERT_QCS = true;
  public static boolean DEFAULT_CONVERT_MASS_TO_VOL = true;
  public static boolean DEFAULT_SET_MSG_QBEQ3 = false; //Questionable
  public static boolean DEFAULT_SET_MSG_QBEQ4 = false; //Bad measurement
  public static boolean DEFAULT_SET_MSG_QBEQ7 = false; //Manual CFC
  public static boolean DEFAULT_SET_MSG_QBEQ8 = false; //Irregular CFC
  public static boolean DEFAULT_SET_ALL_PARAMS_MSG_BQBEQ4 = false;
  public static boolean DEFAULT_SET_GAS_PARAMS_MSG_BQBEQ3_AND_O2QBEQ4 = false;
  //public static boolean DEFAULT_MERGE_WOCE_CASTS = false;
  public static boolean DEFAULT_TRANSLATE_PARAM_NAMES = true;
  public static int DEFAULT_AUTOSCALE_COLOR_SCHEME = 0;
  public static String DEFAULT_LINE_PLOT_PALETTE = "";
  public static boolean DEFAULT_ENHANCE_ENLARGE_CURRENT_SYMBOL = true;
  public static boolean DEFAULT_ENHANCE_REPLACE_CURRENT_SYMBOL = false;
  public static double DEFAULT_ENHANCE_ENLARGE_CURRENT_SYMBOL_BY = 0.5;
  public static double DEFAULT_ENHANCE_ENLARGE_CONTRASTING_SYMBOL_BY = 0.5;
  public static boolean DEFAULT_ENHANCE_USE_CONTRASTING_COLOR = false;
  public static Color DEFAULT_ENHANCE_CONTRASTING_COLOR = Color.red;
  public static int DEFAULT_ENHANCE_CONTRASTING_SYMBOL = 5;
  public static PageFormat DEFAULT_PAGEFORMAT = null;
  public static PrinterJob DEFAULT_PRINTERJOB = null;
  public static boolean DEFAULT_PLOT_TITLES = true;
  public static boolean DEFAULT_TRANSFER_TO_STATION_VAR = true;

  public static String DEFAULT_AXIS_VALUE_FONT ="serif";
  public static String DEFAULT_AXIS_LABEL_FONT ="serif";
  public static String DEFAULT_ISOPYCNAL_LABEL_FONT ="serif";
  public static String DEFAULT_COLORBAR_LABEL_FONT ="serif";
  public static String DEFAULT_PLOT_TITLE_FONT ="sansserif";
  public static String DEFAULT_MAP_VALUE_FONT ="serif";
  public static String DEFAULT_MAP_STN_LABEL_FONT ="serif";
  public static String DEFAULT_CONTOUR_XSEC_LABEL_FONT ="serif";
  public static String DEFAULT_CONTOUR_XSEC_VALUE_FONT ="serif";
  public static String DEFAULT_REGRESSION_FONT ="serif";

  public static int DEFAULT_AXIS_VALUE_SIZE = 12;
  public static int DEFAULT_AXIS_LABEL_SIZE = 12;
  public static int DEFAULT_ISOPYCNAL_LABEL_SIZE = 12;
  public static int DEFAULT_COLORBAR_LABEL_SIZE = 12;
  public static int DEFAULT_PLOT_TITLE_SIZE = 16;
  public static int DEFAULT_MAP_VALUE_SIZE = 12;
  public static int DEFAULT_MAP_STN_LABEL_SIZE = 9;
  public static int DEFAULT_CONTOUR_XSEC_LABEL_SIZE = 10;
  public static int DEFAULT_CONTOUR_XSEC_VALUE_SIZE = 10;
  public static int DEFAULT_REGRESSION_FONT_SIZE = 14;

  public static int DEFAULT_AXIS_VALUE_STYLE = Font.PLAIN;
  public static int DEFAULT_AXIS_LABEL_STYLE = Font.PLAIN;
  public static int DEFAULT_ISOPYCNAL_LABEL_STYLE = Font.PLAIN;
  public static int DEFAULT_COLORBAR_LABEL_STYLE = Font.PLAIN;
  public static int DEFAULT_PLOT_TITLE_STYLE = Font.BOLD + Font.ITALIC;
  public static int DEFAULT_MAP_VALUE_STYLE = Font.PLAIN;
  public static int DEFAULT_MAP_STN_LABEL_STYLE = Font.PLAIN;
  public static int DEFAULT_CONTOUR_XSEC_LABEL_STYLE = Font.PLAIN;
  public static int DEFAULT_CONTOUR_XSEC_VALUE_STYLE = Font.PLAIN;
  public static int DEFAULT_REGRESSION_FONT_STYLE = Font.BOLD;

  public static Color DEFAULT_AXIS_VALUE_COLOR = Color.black;
  public static Color DEFAULT_AXIS_LABEL_COLOR = Color.black;
  public static Color DEFAULT_ISOPYCNAL_LABEL_COLOR = Color.black;
  public static Color DEFAULT_COLORBAR_LABEL_COLOR = Color.black;
  public static Color DEFAULT_PLOT_TITLE_COLOR = Color.black;
  public static Color DEFAULT_MAP_VALUE_COLOR = Color.black;
  public static Color DEFAULT_MAP_STN_LABEL_COLOR = Color.black;
  public static Color DEFAULT_CONTOUR_XSEC_LABEL_COLOR = Color.black;
  public static Color DEFAULT_CONTOUR_XSEC_VALUE_COLOR = Color.black;
  public static Color DEFAULT_REGRESSION_FONT_COLOR = Color.black;

  public static Color DEFAULT_SELECTION_REGION_BG_COLOR = new Color(255, 255, 255, 150);
  public static Color DEFAULT_SELECTION_REGION_OUTLINE_COLOR = new Color(255, 255, 255, 240);
  
  public static CastIDRule DEFAULT_CAST_ID_RULE = CastIDRule.ORIG_STN_ID;
  public static CastNumberRule DEFAULT_CAST_NUMBER_RULE = CastNumberRule.JOA_SUBSTITUTION;
  public static SectionIDRule DEFAULT_SECTION_ID_RULE = SectionIDRule.ORIG_CRUISE_ID;
  public static DepthConversionRule DEFAULT_DEPTH_CONVERSION_RULE = DepthConversionRule.CONVERT_DEPTH_TO_PRESSURE;
  public static PreferPressureParameterRule DEFAULT_PRES_PARAM_RULE = PreferPressureParameterRule.PREFER_PRESSURE_PARAMETER;
  public static TempConversionRule DEFAULT_TEMP_CONV_RULE = TempConversionRule.ITS90_TO_IPTS68;
  public static DestinationQCRule DEFAULT_DEST_QC_RULE = DestinationQCRule.WOCE;
  public static ConvertParameterNamesRule DEFAULT_CONVERT_PARAM_NAMES_RULE = ConvertParameterNamesRule.CONVERT_TO_JOA_LEXICON;
  public static boolean DEFAULT_COLLECT_METADATA_RULE = true;
  public static QCConversionRule DEFAULT_QC_PROCESSING_RULE = QCConversionRule.READ_ORIG_QC_FLAGS;
  
  public static MapSpecification DEFAULT_MAP_SPECIFICATION = null;
  public static NewColorBar DEFAULT_COLORBAR = null;
  
  public static boolean ISMAC = false;
  public static boolean ISMACOSX = false;
  public static boolean ISSUNOS = false;
  public static boolean ISJAVA14 = false;
  public static long BIGLONG = Long.MAX_VALUE;
  public static int ALL_OBS = 1;
  public static int MISSINGVALUE = -99;
  public static double MULTPLECROSSINGS = -99.2;
  public static double OUTSIDERANGEOFEOS = -99.1;
  public static double WOCEMISSINGVALUE = -999.0;
  public static double EPICMISSINGVALUE = 1e35;
  public static double FERRETMISSINGVALUE = -1e33;
  public static double UOTMISSINGVALUE = 99999;
  public static boolean USECUSTOMMISSINGVALUE = false;
  public static double CUSTOMMISSINGVALUE = MISSINGVALUE;
  public static boolean CANCELIMPORT = false;
  public static int DISTANCE = 1;
  public static int SEQUENCE = 2;
  public static int LATERAL = 10;
  public static double F = Math.PI / 180.0;
  public static double TWOOMEGA = .0001454441043328;
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
  public static int PROFTIME = 5;
  public static int PROFXY = 6;
  public static int MERCATORPROJECTION = 1;
  public static int MILLERPROJECTION = 2;
  public static int ORTHOGRAPHICPROJECTION = 3;
  public static int MOLLWEIDEPROJECTION = 4;
  public static int LAMBERTEAPROJECTION = 5;
  public static int STEREOPROJECTION = 10;
  public static int NORTHPOLEPROJECTION = 11;
  public static int SOUTHPOLEPROJECTION = 12;
  public static int ECKERT4PROJECTION = 13;
  public static int ROBINSONPROJECTION = 14;
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
  public static int SYMBOL_LEFT_ARROW_SYMBOL = 17;
  public static int DEFAULT_CURSOR_SIZE = 8;
  public static int DEFAULT_CURSOR_SYMBOL = JOAConstants.SYMBOL_CIRCLEFILLED;
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
  public static int REGRID_MODE_LAT = 1;
  public static int REGRID_MODE_LON = 2;
  public static int JOA_DEFAULT_PRECISION = 3;
  public static int JOA_DEFAULT_SIG_DIGITS = 6;
  public static HashMap<String, FeatureGroup> JOA_FEATURESET = new HashMap<String, FeatureGroup>();

  public static String cArray[] = {"c500","c1000","c1500","c2000","c2500","c3000","c3500","c4000","c4500",
   "c5000","c5500","c6000"};
  public static String bArray[] = {".npac",".spac",".natl",".satl",".med",".indian",".arctic",".antarc"};

  public static double BASINLIMITS[][] = { 
  	{ -0.21, 60.76, 110.00, -80.70}, 
  	{0.01, 60.78, 110.00, -80.55}, 
  	{ -0.28, 60.30, 110.00, -81.43}, 
  	{ -0.02, 59.97, 110.00, -80.81}, 
  	{ -0.18, 59.78, 110.00, -82.50}, 
  	{ -0.04, 59.64, 110.00, -80.99}, 
  	{ -0.01, 59.22, 112.00, -84.79}, 
  	{ -0.06, 58.08, 116.75, -86.01}, 
  	{ -0.04, 50.93, 121.14, -120.44},
    { -0.02, 58.14, 118.99, -98.31}, 
    {-0.02, 50.95, 122.83, -137.00}, 
    {0.08, 55.32, 124.65, -93.15}, 
    { -71.97, 0.02, 110.00, -65.09}, 
    { -72.00, -0.02, 110.03, -62.01}, 
    { -72.00, 0.06, 109.86, -65.07}, 
    { -72.01, 0.04, 110.03, -5.47},
      { -70.91, 0.05, 109.84, -65.05}, 
      { -71.22, -0.04, 110.03, -62.08}, { -70.23, 0.09, 109.86, -65.07}, { -72.01,
      0.02, 88.58, -61.99}, { -71.26, 0.04, 109.84, -69.90}, { -64.07, 0.02, 110.02, -74.82}, { -64.39, 0.01, 109.85,
      -76.17}, {90.00, -90.00, 360.00, -360.00}, {0.10, 72.01, -81.25, 9.52}, { -0.03, 72.05, -87.55, 8.78}, {16.82,
      72.05, -97.00, 0.11}, {0.04, 72.03, -88.64, 8.45}, { -0.00, 72.03, -84.74, 8.10}, { -0.00, 72.00, -87.72, 6.62},
      {0.01, 67.91, -88.96, 5.65}, { -0.09, 50.92, -86.86, 4.46}, {0.03, 46.76, -83.26, -5.79}, {0.01, 42.40, -83.20,
      -1.02}, {10.43, 32.46, -72.50, -25.92}, {90.00, -90.00, 360.00, -360.00}, { -72.00, -0.02, -65.01, 10.18},
      { -71.98, -1.55, -65.11, 20.03}, { -71.98, -0.03, -59.18, 17.12}, { -72.01, -1.59, -65.06, 19.97}, { -72.03, 0.02,
      -60.98, 18.62}, { -72.01, 0.09, -65.01, 19.95}, { -72.05, 0.03, -65.05, 19.99}, { -72.01, -0.04, -65.02, 20.21},
      { -69.14, 0.04, -64.07, 19.97}, { -68.31, 2.58, -60.11, 20.02}, { -60.95, -2.91, -56.16, 12.78}, { -60.99, -47.54,
      -50.04, -20.32}, {18.81, 43.99, -4.47, 51.52}, {15.27, 42.60, 0.33, 51.62}, {19.28, 43.46, -4.37, 51.87}, {19.12,
      43.24, 8.78, 39.19}, {32.66, 40.47, 4.21, 32.80}, {32.73, 40.64, 11.89, 30.08}, {34.54, 40.13, 12.13, 28.82},
      {34.37, 37.29, 18.21, 28.75}, {35.64, 36.41, 21.28, 22.41}, {90.00, -90.00, 360.00, -360.00}, {90.00, -90.00,
      360.00, -360.00}, {90.00, -90.00, 360.00, -360.00}, { -70.19, 25.10, 19.93, 109.96}, { -70.14, 25.11, 20.02,
      109.99}, { -70.03, 22.96, 19.99, 105.58}, { -69.97, 24.46, 20.06, 110.01}, { -69.79, 24.09, 19.96, 110.05},
      { -69.55, 23.71, 20.00, 110.01}, { -68.88, 23.24, 19.96, 110.06}, { -68.32, 22.14, 20.01, 110.03}, { -67.34,
      26.00, 19.94, 110.11}, { -65.84, 8.45, 19.99, 110.04}, { -47.60, -5.27, 24.62, 110.11}, { -22.50, -10.39, 95.01,
      110.00}, {63.99, 86.11, -352.64, -5.27}, {64.02, 87.87, -350.03, -8.68}, {67.89, 88.84, -349.91, -7.84}, {64.03,
      89.07, -359.65, -4.09}, {63.99, 89.12, -359.86, -0.11}, {64.07, 89.14, -359.91, -0.04}, {66.75, 89.46, -359.90,
      -0.26}, {73.72, 89.63, -359.40, -0.59}, {85.07, 86.73, -349.55, -311.32}, {85.34, 86.87, -346.06, -302.93},
      {90.00, -90.00, 360.00, -360.00}, {90.00, -90.00, 360.00, -360.00}, { -77.99, -60.20, -355.91, -17.71}, { -75.92,
      -59.88, -342.40, -19.39}, { -73.21, -59.99, -359.86, -33.06}, { -74.90, -59.91, -353.19, -24.49}, { -73.37,
      -59.99, -359.83, -23.24}, { -72.88, -59.87, -359.02, -22.96}, { -71.88, -59.86, -359.84, -23.46}, { -73.19,
      -59.85, -359.60, -0.05}, { -68.18, -59.88, -353.21, -1.65}, { -65.76, -59.91, -359.83, -0.42}, {90.00, -90.00,
      360.00, -360.00}, {90.00, -90.00, 360.00, -360.00}
  };

  public static String paramNames[] = {"THTA","SIG0","SIG1","SIG2","SIG3","SIG4","SPCY","SVAN","SVEL","O2%",
   "AOU","NO","PO","HTST","ALPH","BETA","ADRV","BDRV","GPOT","PE ","HEAT","ACTT","GAMMA"};
  public static int IMPORTSS = 1;
  public static int IMPORTSD2 = 2;
  public static int IMPORTWOCE = 3;
  public static int IMPORTP3 = 4;
  public static char TAB_DELIMITER = '\t';
  public static char COMMA_DELIMITER = ',';
  public static char DELIMITER = TAB_DELIMITER;
  public static String MISSING_VALUE_STRING = new String("------");

  public static Parameter defaultProperties[] = {new Parameter("PRES", 1, 0,"db"),
      new Parameter("SALT", 0.001, 15.0,"psu"), new Parameter("TEMP", 0.001, 0,"deg C"),
      new Parameter("O2", 0.01, 200.0,"ml/l"), new Parameter("PO4", 0.01, 0,"um/l"),
      new Parameter("NO3", 0.01, 0,"um/l"), new Parameter("NO2", 0.01, 0,"um/l"),
      new Parameter("SIO3", 0.01, 0,"um/l"), new Parameter("THTA", 0.001, 0.0,"deg C"),
      new Parameter("SIG0", 0.001, 15.0,"kg/m^3"), new Parameter("SIG1", 0.001, 15.0,"kg/m^3"),
      new Parameter("SIG2", 0.001, 15.0,"kg/m^3"), new Parameter("SIG3", 0.001, 15.0,"kg/m^3"),
      new Parameter("SIG4", 0.001, 15.0,"kg/m^3"), new Parameter("AOU", 0.01, -580.0,"um/kg"),
      new Parameter("O2%", 0.01, 0.0,"none"), new Parameter("NO ", 0.01, 890.0,"um/kg"),
      new Parameter("PO", 0.01, 890.0,"um/kg"), new Parameter("SPCY", 0.001, 20.0,"none"),
      new Parameter("SVAN", 0.1, -11.0,"m^3/kg"), new Parameter("SVEL", 0.01, 124.28,"m/s"),
      new Parameter("GPOT", 0.01, -39.0,"m^2/s^2"), new Parameter("ACTT", 0.001, -5.0,"sec"),
      new Parameter("PE", 0.01, -96.0,"10^6 J/m^2"), new Parameter("HEAT", 0.01, -116.0,"10^9 J/m^2"),
      new Parameter("HTST", 0.01, -10.0,"10^6 J/kg"), new Parameter("ALPH", 0.0001, 0,"1/degC*10^2"),
      new Parameter("ADRV", 0.001, -9.0,"1/db*10^3"), new Parameter("BETA", 0.0001, 2.5536,"none"),
      new Parameter("BDRV", 0.001, -8.0,"1/db*10^3"), new Parameter("BV ", 0.001, 0,"Hz"),
      new Parameter("SB", 0.1, 0,"Hz"), new Parameter("VT ", 0.001, -1.0,"Hz"),
      new Parameter("CTDS", 0.001, 15.0,"ndy"), new Parameter("CTDO", 0.01, 200.0,"ndy"),
      new Parameter("WTHT", 0.001, 0,"ndy"), new Parameter("NO2", 0.01, 0,"ndy"),
      new Parameter("NO2+", 0.01, 0,"ndy"), new Parameter("F11", 0.001, 0,"ndy"),
      new Parameter("F12", 0.001, 0,"ndy"), new Parameter("F113", 0.001, 0,"ndy"),
      new Parameter("CCL4", 0.001, 0,"ndy"), new Parameter("TRIT", 0.001, 0,"ndy"),
      new Parameter("DELH", 0.01, 0,"ndy"), new Parameter("HELI", 0.001, 0,"ndy"),
      new Parameter("C14", 0.01, 0,"ndy"), new Parameter("TCO2", 0.1, 0,"ndy"), new Parameter("PCO2", 0.1, 0,"ndy"),
      new Parameter("ALKI", 0.1, 0,"ndy"), new Parameter("PH ", 0.001, 0,"ndy"),
      new Parameter("C13", 0.01, 0,"ndy"), new Parameter("NEON", 0.001, 0,"ndy"),
      new Parameter("TRER", 0.001, 0,"ndy"), new Parameter("DH3E", 0.01, 0,"ndy"),
      new Parameter("HEER", 0.001, 0,"ndy"), new Parameter("C14E", 0.01, 0,"ndy"),
      new Parameter("C13E", 0.01, 0,"ndy"), new Parameter("PCOT", 0.01, 0,"ndy"),
      new Parameter("NEER", 0.001, 0,"ndy"), new Parameter("ARGO", 0.001, 0,"ndy"),
      new Parameter("KR85", 0.001, 0,"ndy"), new Parameter("AR39", 0.01, 0,"ndy"),
      new Parameter("R228", 0.01, 0,"ndy"), new Parameter("R226", 0.01, 0,"ndy"),
      new Parameter("SR90", 0.01, 0,"ndy"), new Parameter("C137", 0.01, 0,"ndy"),
      new Parameter("NH4", 0.001, 0,"ndy"), new Parameter("BARI", 0.1, 0,"ndy"),
      new Parameter("O18R", 0.001, 0,"ndy"), new Parameter("PREV", 1, 0,"ndy"), new Parameter("TREV", 0.001, 0,"ndy"),
      new Parameter("IDAT", 0.01, 0,"ndy"), new Parameter("IDID", 0.01, 0,"ndy"),
      new Parameter("CH4", 0.001, 0,"ndy"), new Parameter("DON", 0.01, 0,"ndy"),
      new Parameter("N20", 0.001, 0,"ndy"), new Parameter("CHLA", 0.01, 0,"ndy"),
      new Parameter("POC", 0.01, 0,"ndy"), new Parameter("DOC", 0.01, 0,"ndy"),
      new Parameter("PON", 0.01, 0,"ndy"), new Parameter("CO ", 0.001, 0,"ndy"),
      new Parameter("CHCL", 0.001, 0,"ndy"), new Parameter("KRER", 0.001, 0,"ndy"),
      new Parameter("ARGE", 0.01, 0,"ndy"), new Parameter("R8ER", 0.01, 0,"ndy"),
      new Parameter("R6ER", 0.01, 0,"ndy"), new Parameter("BTLN", 1.0, 0,"ndy")
  };

  public static boolean ISOINBASIN[][] = {
      // north pacific
      {true, true, true, true, true, true, true, true, true, true, true, true},
      // south pacific
      {true, true, true, true, true, true, true, true, true, true, true, false},
      // north atlantic
      {true, true, true, true, true, true, true, true, true, true, true, false},
      // south atlantic
      {true, true, true, true, true, true, true, true, true, true, true, true},
      // med
      {true, true, true, true, true, true, true, true, true, false, false, false},
      //indian
      {true, true, true, true, true, true, true, true, true, true, true, true},
      //arctic
      {true, true, true, true, true, true, true, true, true, true, false, false},
      //antarc
      {true, true, true, true, true, true, true, true, true, true, false, false}
  };
  public static Hashtable mIsobathCache = new Hashtable();

  public static long currTime;
  public static GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
  public static GraphicsDevice[] gs = ge.getScreenDevices();
  public static GraphicsConfiguration[] gc = null;

  // constants for station statistic calculations
  public static int CALC_MIN = 0;
  public static int CALC_MAX = 1;
  public static int CALC_DEPTH_OF_MIN = 2;
  public static int CALC_DEPTH_OF_MAX = 3;
  public static int MAX_DEPTH_OF_NONMISSING_VAL = 4;
  public static int MIN_DEPTH_OF_NONMISSING_VAL = 5;
  public static int CALC_AVERAGE = 6;
  public static int CALC_N = 7;

  public static DataOutputStream LogFileStream;

  public static int UNCHANGED = 0;
  public static int HASADDEDDATA = 1;
  public static int CREATEDONTHEFLY = 2;
  public static int ISCOLLECTION = 4;
  public static int MODIFIEDBYUSER = 8;

  public static Vector<String>  DEFAULT_DAPPER_SERVERS = new Vector<String> ();
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
  public static double NAUT2KM = 1.852;
  public static double NAUT2METERS = 1852;
  public static String DEFAULT_DB_URI = new String("jdbc:mysql://localhost:");
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
  public static int MAX_DEPTH_OF_NONMISSING = 4;
  public static int MIN_DEPTH_OF_NONMISSING = 5;
  public static boolean DEFAULT_APPLY_CALCS_TO_USER_CALCS = true;
  public static boolean DEFAULT_TRANSLATE_LEXICON = true;
  public static int DEFAULT_LEXICON = Lexicon.JOA_LEXICON;
  public static boolean DEFAULT_CONVERT_DEPTH = true;
  public static boolean DEFAULT_CONVERT_O2 = true;
  public static Lexicon LEXICON = null;
  public static boolean DEFAULT_DEBUG_MODE = false;
  public static boolean CONNECTED_TO_DB = true;
  public static ExecutorService BACKGROUND_EXEC = Executors.newCachedThreadPool();
  public static Direction COLORBARDIRECTION = Direction.UP;
}

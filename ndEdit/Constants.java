/*
 * $Id: Constants.java,v 1.12 2005/02/15 18:31:08 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit;

import java.awt.Font; 
import java.awt.Color; 
import gov.noaa.pmel.util.GeoDate;

 /**
 *
 *
 * @author  Chris Windsor and OZ 
 * @version 1.0 01/13/00
 */
public final class Constants {
  public static final int LAT_LON = 0;
  public static final int LAT_DEPTH = 1;
  public static final int LAT_TIME = 2;
  public static final int LON_DEPTH = 3;
  public static final int LON_TIME = 4;
  public static final int DEPTH_TIME = 5; 
  //public static final int SELECTED_VIEW = 6; 
  public static final int LAT_AXIS = 0;  
  public static final int LON_AXIS = 1;  
  public static final int DEPTH_AXIS = 2;  
  public static final int TIME_AXIS = 3;   
  public static final int X_AXIS = 0;  
  public static final int Y_AXIS = 1;  
  public static final int ZOOM_MODE = 1;
  public static final int SELECT_MODE = 2;
  public static final int SECTION_MODE = 3;
  public static final int POLYGON_MODE = 4;
  public static double F = (double)Math.PI/180.0f;
  public static double SECTION_WIDTH = 200.0f;
  public static double LONGITUDE_CONV_FACTOR = 0.0f;
  public static UserSettings USERSETTINGS;
  
  public static final int MAX_CUT_PANEL_TYPES = 6;

  public static final String[] cpNames = {"LatLon",
					"LatDepth",
					"LatTime",
					"LonDepth",
					"LonTime",
					"DepthTime"};


  public static final int TIME_FIELD = 0;
  public static final int LAT_FIELD = 1;
  public static final int LON_FIELD = 2;
  public static final int DEPTH_FIELD = 3;

  public static final int START_BOUNDARY = 1;
  public static final int STOP_BOUNDARY = 2;
  public static final int DEG_MINUTES_SEC_GEO_DISPLAY = 0;
  public static final int DEC_MINUTES_GEO_DISPLAY = 1;
  public static final int DATE_TIME_DISPLAY = 0;
  public static final int YEAR_DAY_TIME_DISPLAY = 1;
  public static final int MONTH_TIME_DISPLAY = 2;
  public static final int SEASON_TIME_DISPLAY = 3;
  public static boolean ISMAC = false;
  public static boolean ISMACOSX = false;
  public static String SUPPORT_DIR = null;
	
	public static String cArray[] = {"c500", "c1000","c1500", "c2000", "c2500","c3000",
							"c3500", "c4000","c4500", "c5000", "c5500","c6000"};
	public static String bArray[] = {".npac", ".spac",".natl", ".satl", ".med",".indian",
							".arctic", ".antarc"};
 	
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
	{true, true, true, true, true, true, true, true, true, true, false, false}};
	
	
	public static double BASINLIMITS[][] = {
	//north pacific
	{-0.21f,60.76f,110.00f,-80.70f},	//500
	{0.01f,60.78f,110.00f,-80.55f},		//1000
	{-0.28f,60.30f,110.00f,-81.43f},	//1500
	{-0.02f,59.97f,110.00f,-80.81f},	//2000
	{-0.18f,59.78f,110.00f,-82.50f},	//2500
	{-0.04f,59.64f,110.00f,-80.99f},	//3000
	{-0.01f,59.22f,112.00f,-84.79f},	//3500
	{-0.06f,58.08f,116.75f,-86.01f},	//4000
	{-0.04f,58.93f,121.14f,-120.44f},	//4500
	{-0.02f,58.14f,118.99f,-98.31f},	//5000
	{-0.02f,50.95f,122.83f,-137.00f},	//5500
	{0.08f,55.32f,124.65f,-93.15f},		//6000
	//south pacific
	{-71.97f,0.02f,110.00f,-65.09f},
	{-72.00f,-0.02f,110.03f,-62.01f},
	{-72.00f,0.06f,109.86f,-65.07f},
	{-72.01f,0.04f,110.03f,-5.47f},
	{-70.91f,0.05f,109.84f,-65.05f},
	{-71.22f,-0.04f,110.03f,-62.08f},
	{-70.23f,0.09f,109.86f,-65.07f},
	{-72.01f,0.02f,88.58f,-61.99f},
	{-71.26f,0.04f,109.84f,-69.90f},
	{-64.07f,0.02f,110.02f,-74.82f},
	{-64.39f,0.01f,109.85f,-76.17f},
	{90.00f,-90.00f,360.00f,-360.00f},
	// north atlantic
	{0.10f,72.01f,-81.25f,9.52f},		//500
	{-0.03f,72.05f,-87.55f,8.78f},		//1000
	{16.82f,72.05f,-97.00f,0.11f},		//1500
	{0.04f,72.03f,-88.64f,8.45f},		//2000
	{-0.00f,72.03f,-84.74f,8.10f},		//2500
	{-0.00f,72.00f,-87.72f,6.62f},		//3000
	{0.01f,67.91f,-88.96f,5.65f},		//3500
	{-0.09f,50.92f,-86.86f,4.46f},		//4000
	{0.03f,46.76f,-83.26f,-5.79f},		//4500
	{0.01f,42.40f,-83.20f,-1.02f},		//5000
	{10.43f,32.46f,-72.50f,-25.92f},	//5500
	{90.00f,-90.00f,360.00f,-360.00f},	//6000
	//south atlantic
	{-72.00f,-0.02f,-65.01f,10.18f},
	{-71.98f,-1.55f,-65.11f,20.03f},
	{-71.98f,-0.03f,-59.18f,17.12f},
	{-72.01f,-1.59f,-65.06f,19.97f},
	{-72.03f,0.02f,-60.98f,18.62f},
	{-72.01f,0.09f,-65.01f,19.95f},
	{-72.05f,0.03f,-65.05f,19.99f},
	{-72.01f,-0.04f,-65.02f,20.21f},
	{-69.14f,0.04f,-64.07f,19.97f},
	{-68.31f,2.58f,-60.11f,20.02f},
	{-60.95f,-2.91f,-56.16f,12.78f},
	{-60.99f,-47.54f,-50.04f,-20.32f},
	// med
	{18.81f,43.99f,-4.47f,51.52f},
	{15.27f,42.60f,0.33f,51.62f},
	{19.28f,43.46f,-4.37f,51.87f},
	{19.12f,43.24f,8.78f,39.19f},
	{32.66f,40.47f,4.21f,32.80f},
	{32.73f,40.64f,11.89f,30.08f},
	{34.54f,40.13f,12.13f,28.82f},
	{34.37f,37.29f,18.21f,28.75f},
	{35.64f,36.41f,21.28f,22.41f},
	{90.00f,-90.00f,360.00f,-360.00f},
	{90.00f,-90.00f,360.00f,-360.00f},
	{90.00f,-90.00f,360.00f,-360.00f},
	//indian
	{-70.19f,25.10f,19.93f,109.96f},
	{-70.14f,25.11f,20.02f,109.99f},
	{-70.03f,22.96f,19.99f,105.58f},
	{-69.97f,24.46f,20.06f,110.01f},
	{-69.79f,24.09f,19.96f,110.05f},
	{-69.55f,23.71f,20.00f,110.01f},
	{-68.88f,23.24f,19.96f,110.06f},
	{-68.32f,22.14f,20.01f,110.03f},
	{-67.34f,26.00f,19.94f,110.11f},
	{-65.84f,8.45f,19.99f,110.04f},
	{-47.60f,-5.27f,24.62f,110.11f},
	{-22.50f,-10.39f,95.01f,110.00f},
	// arctic
	{63.99f,86.11f,-352.64f,-5.27f},
	{64.02f,87.87f,-350.03f,-8.68f},
	{67.89f,88.84f,-349.91f,-7.84f},
	{64.03f,89.07f,-359.65f,-4.09f},
	{63.99f,89.12f,-359.86f,-0.11f},
	{64.07f,89.14f,-359.91f,-0.04f},
	{66.75f,89.46f,-359.90f,-0.26f},
	{73.72f,89.63f,-359.40f,-0.59f},
	{85.07f,86.73f,-349.55f,-311.32f},
	{85.34f,86.87f,-346.06f,-302.93f},
	{90.00f,-90.00f,360.00f,-360.00f},
	{90.00f,-90.00f,360.00f,-360.00f},
	//antarc
	{-77.99f,-60.20f,-355.91f,-17.71f},
	{-75.92f,-59.88f,-342.40f,-19.39f},
	{-73.21f,-59.99f,-359.86f,-33.06f},
	{-74.90f,-59.91f,-353.19f,-24.49f},
	{-73.37f,-59.99f,-359.83f,-23.24f},
	{-72.88f,-59.87f,-359.02f,-22.96f},
	{-71.88f,-59.86f,-359.84f,-23.46f},
	{-73.19f,-59.85f,-359.60f,-0.05f},
	{-68.18f,-59.88f,-353.21f,-1.65f},
	{-65.76f,-59.91f,-359.83f,-0.42f},
	{90.00f,-90.00f,360.00f,-360.00f},
	{90.00f,-90.00f,360.00f,-360.00f}};
	
	public static String DEFAULT_AXIS_VALUE_FONT = "serif";
	public static String DEFAULT_AXIS_LABEL_FONT = "serif";
	
	public static int DEFAULT_AXIS_VALUE_SIZE = 12;
	public static int DEFAULT_AXIS_LABEL_SIZE = 12;
	
	public static int DEFAULT_AXIS_VALUE_STYLE = Font.PLAIN;
	public static int DEFAULT_AXIS_LABEL_STYLE = Font.PLAIN;
	
	public static Color DEFAULT_AXIS_VALUE_COLOR = Color.black;
	public static Color DEFAULT_AXIS_LABEL_COLOR = Color.black;
	public static boolean USE_METAL = false;
	public static final int EPIC_FORMAT = 0;
	public static final int ARGO_FORMAT = 1;
	public static final int GTSPP_FORMAT = 2;
	public static final TuplePointerCollection DEFAULT_PC = new TuplePointerCollection(
			  -90.0,
			   90.0,
			 -179.0,
			  170.0,
			    0.0,
			 1000.0,
			 new GeoDate(0L),
			 new GeoDate(System.currentTimeMillis()));
}







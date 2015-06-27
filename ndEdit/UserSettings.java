/*
 * $Id: UserSettings.java,v 1.11 2005/06/17 17:24:17 oz Exp $
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

  import java.awt.*;
  import java.util.*;
  import java.io.*;
  import javax.swing.*;
//
//
// The User Settings object encapsulates user preferences.  See also
//  the User Settings Model, which publishes the user preferences to 
//  listeners.
//
 /**
 *
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 *
 *
  * @note Default properties will specify:
  * - which views to show/hide on initial startup
  * - user-definable ranges, like perhaps depth contours
  * - Definition of what a "season" is.
  * 
  * For each view:
  * - which overlays are automatically displayed
  *
  * Listens for the cut panel resize event.
  *
  * @stereotype Singleton
  */
public class UserSettings {
  private Vector visibleViewIds;
  private Dimension cutPanelSize = new Dimension(325, 325);
  private Dimension cutPanelMinSize = new Dimension(225, 225);
  private Dimension cutPanelMaxSize = new Dimension(800, 800);
  private double lonReference;
  private int geoDisplayFormat = Constants.DEC_MINUTES_GEO_DISPLAY;
  private int useCenterWidthOrMinMax;
  private int timeDisplayFormat = Constants.DATE_TIME_DISPLAY;
  private int timeAxisMode;
  private double timeAxisReference;
  private String timeSinceUnits;
  private boolean displayPanelAxes = true;
  private boolean independentHandles = false;


  // Defaults are defined here as statics:
  static public Dimension defaultCutPanelSize = new Dimension(325, 325);
  static int[] defaultVisibleViewIds = new int[] { 
  		Constants.LAT_LON,
  		Constants.LAT_DEPTH,
  		Constants.LAT_TIME,
  		Constants.LON_DEPTH,
  		Constants.LON_TIME,
  		Constants.DEPTH_TIME };


	/**
	* 
	*/
	public UserSettings() {
	}

	/**
	* 
	*/
	public UserSettings(String filename) {
		visibleViewIds = new Vector();
		for (int i = 0; i < defaultVisibleViewIds.length; i++) {
			visibleViewIds.addElement(new Integer(defaultVisibleViewIds[i]));
		}
		load(filename);
	}

  //
  /**
  * 
  */
  //public UserSettings(int[] viewIds, Dimension cutPanelSize) {
  //   this.setVisibleViewIds(viewIds);
//this.setCutPanelSize(cutPanelSize);
  //}

  //
  /**
  * 
  */
  /*public UserSettings(int[] viewIds, Dimension cutPanelSize, double lonReference, int geoDisplayFormat,
  					  int useCenterWidthOrMinMax, int timeDisplayFormat, boolean displayAxes, boolean indHandles) {
     
     setVisibleViewIds(viewIds);
     this.cutPanelSize = cutPanelSize;
     this.lonReference = lonReference;
     this.geoDisplayFormat = geoDisplayFormat;
     this.useCenterWidthOrMinMax = useCenterWidthOrMinMax;
     this.displayPanelAxes = displayAxes;
     this.independentHandles = indHandles;
  }*/

	//
	/**
	* 
	*/
	public Object clone() {
		UserSettings usn = new UserSettings();
		usn.setVisibleViewIds(this.getVisibleViewIds());
		usn.cutPanelSize = this.cutPanelSize;
		usn.lonReference = this.lonReference;
		usn.geoDisplayFormat = this.geoDisplayFormat;
		usn.timeDisplayFormat = this.timeDisplayFormat;
		usn.useCenterWidthOrMinMax = this.useCenterWidthOrMinMax;
		usn.displayPanelAxes = this.displayPanelAxes;
		usn.independentHandles = this.independentHandles;
		return usn;
	}

	/**
	* 
	*/
	public void addDefaultOverlayForView(int viewName, String overlayName){
	}

	public void setDisplayAxes(boolean displayAxes) {
		displayPanelAxes = displayAxes;
	}
	
	public boolean isDisplayAxes() {
		return displayPanelAxes;
	}

	public void setIndependentHandle(boolean b) {
		independentHandles = b;
	}
	
	public boolean isIndependentHandles() {
		return independentHandles;
	}

	/**
	*
	*/
	public int getCutPanelMinSize() {
		return cutPanelMinSize.width;
	}
	
	/**
	*
	* @param minSize smallest allowable size of a side of the cut panel
	*/
	public void setCutPanelMinSize(int minSize) {
		this.cutPanelMinSize = new Dimension(minSize, minSize);
	}
	
	/**
	*
	*/
	public int getCutPanelMaxSize() {
		return cutPanelMaxSize.width;
	}
	
	/**
	*
	* @param maxSize maximum allowable size of a side of the cut panel
	*/
	public void setCutPanelMaxSize(int maxSize) {
		this.cutPanelMaxSize = new Dimension(maxSize, maxSize);
	}
	
	/**
	* 
	*/
	public void setDefaultContouringIntervalForOverlay(String overlayName, double contouringInterval){
	}
	
	/**
	* 
	*/
	
	public Dimension getCutPanelSize() {
		if (cutPanelSize == null) {
			cutPanelSize = new Dimension(defaultCutPanelSize);
		}
		return cutPanelSize;
	}

  /**
  *
  * @param cutPanelSize
  */

  public void setCutPanelSize(Dimension cutPanelSize){
    this.cutPanelSize = cutPanelSize;
  }

  /**
  * 
  */
  public int[] getVisibleViewIds() {
    // translate to int array
    int[] v = new int[visibleViewIds.size()];
    for (int i = 0; i < v.length; i++) {
       v[i] = ((Integer)visibleViewIds.elementAt(i)).intValue();
    }
    return v;
  }


  /**
  * 
  */
  public void setVisibleViewIds(int[] viewEnums){
    visibleViewIds = new Vector();
    for (int i = 0; i < viewEnums.length; i++) {
	 visibleViewIds.addElement(new Integer(viewEnums[i]));
    }
  }


  /**
  * 
  */
  public double getLonReference(){
    return lonReference;
  }


  /**
  * LonReference: either 0 or -180    
  */
  public void setLonReference(double lonReference){
    this.lonReference = lonReference;
  }

  /**
  * 
  */
  public int getGeoDisplayFormat(){
    return geoDisplayFormat;
  }


  /**
  * GeoDisplayFormat: 0 = deg min; 1 = decimal deg
  */
  public void setGeoDisplayFormat(int geoDisplayFormat){
    this.geoDisplayFormat = geoDisplayFormat;
  }

  /**
  * 
  */
  public int getUseCenterWidthOrMinMax(){
    return useCenterWidthOrMinMax;
  }

  //
  // HandlebarParadigm: CENTER_WIDTH or MIN_MAX
  //  0 = top and bottom handlebars move dependently
  //  1 = top and bottom handlebars move independently
  //
  /**
  * 
  */
  public void setUseCenterWidthOrMinMax(int useCenterWidthOrMinMax){
    this.useCenterWidthOrMinMax = useCenterWidthOrMinMax;
  }

  /**
  * 
  */
  public int getTimeDisplayFormat(){
    return timeDisplayFormat;
  }

  /**
  * TimeDisplayFormat: if 0 = YYYY-MM-DD; if 1 = YYYY-NNN
  */
  public void setTimeDisplayFormat(int timeDisplayFormat){
    this.timeDisplayFormat = timeDisplayFormat;
  }

  /**
  * 
  */
  public int getTimeAxisMode(){
    return timeAxisMode;
  }


  /**
  * TimeAxisMode: if 0 = since reference data; if 1 = date
  */
  public void setTimeAxisMode(int timeAxisMode){
    this.timeAxisMode = timeAxisMode;
  }

  /* 
  */
  public double getTimeAxisReference(){
    return timeAxisReference;
  }

  /**
  * TimeAxisReference: ?
  */
  public void setTimeAxisReference(double timeAxisReference){
    this.timeAxisReference = timeAxisReference;
  }

  /**
  * 
  */
  public String getTimeSinceUnits(){
    return timeSinceUnits;
  }

  /**
  * TimeSinceUnits: Years
  */
  public void setTimeSinceUnits(String timeSinceUnits){
    this.timeSinceUnits = timeSinceUnits;
  }

  /**
  * 
  */
  public Properties internalToProperties() {
    Properties props = new Properties();

    StringBuffer st = new StringBuffer();
    st.append(" ");
    for (int i = 0; i < visibleViewIds.size(); i++) {
       st.append(" " + Constants.cpNames[
		((Integer)visibleViewIds.elementAt(i)).intValue()]);
    }
    //System.out.println(" visibleViewIds: " + st.toString());

    props.setProperty("ndedit.visibleViewIds", st.toString());

    props.setProperty("ndedit.cutPanelSizeW",
				(new Integer(cutPanelSize.width).toString()));
    props.setProperty("ndedit.cutPanelSizeH",
				(new Integer(cutPanelSize.height).toString()));
    props.setProperty("ndedit.cutPanelWMin",
				(new Integer(cutPanelMinSize.width).toString()));
    props.setProperty("ndedit.cutPanelHMin",
				(new Integer(cutPanelMinSize.height).toString()));
    props.setProperty("ndedit.cutPanelWMax", 
				(new Integer(cutPanelMaxSize.width).toString()));
    props.setProperty("ndedit.cutPanelHMax", 
				(new Integer(cutPanelMaxSize.height).toString()));
    props.setProperty("ndedit.lonReference", 
				(new Double(lonReference).toString()));
    props.setProperty("ndedit.geoDisplayFormat", 
				(new Integer(geoDisplayFormat).toString()));
    props.setProperty("ndedit.timeDisplayFormat",
				(new Integer(timeDisplayFormat).toString()));
    props.setProperty("ndedit.timeAxisMode", 
				(new Integer(timeAxisMode).toString()));
    props.setProperty("ndedit.timeAxisReference", (new Double(timeAxisReference).toString()));
    String dpa = new Boolean(displayPanelAxes).toString();
    props.setProperty("ndedit.displayPanelAxes", dpa);
    dpa = new Boolean(independentHandles).toString();
    props.setProperty("ndedit.independentHandles", dpa);
    return props;
  }

  /**
  * 
  */
  public void propertiesToInternal(Properties props) {

    int ival1, ival2;
    String s1, s2;

    visibleViewIds = new Vector();
    String vv = props.getProperty("ndedit.visibleViewIds");
    if (vv != null && vv.length() > 0) {
	    for (int i = 0; i < Constants.cpNames.length; i++) {
	       if (vv.indexOf(Constants.cpNames[i]) >= 0) {
		  		visibleViewIds.addElement(new Integer(i));
	       }
	    }
	}

    // Cut Panel Size
    s1 = props.getProperty("ndedit.cutPanelSizeW");
    s2 = props.getProperty("ndedit.cutPanelSizeH");
    if (s1 != null && s2 != null) {
       ival1 = Integer.parseInt(s1);
       ival2 = Integer.parseInt(s2);
       cutPanelSize = new Dimension(ival1, ival2);
    }

    // Cut Panel Minimum Size
    s1 = props.getProperty("ndedit.cutPanelWMin");
    s2 = props.getProperty("ndedit.cutPanelHMin");
    if (s1 != null || s2 != null) {
       ival1 = Integer.parseInt(s1);
       ival2 = Integer.parseInt(s2);
       cutPanelMinSize = new Dimension(ival1, ival2);
    }

    // Cut Panel Maximum Size
    s1 = props.getProperty("ndedit.cutPanelWMax");
    s2 = props.getProperty("ndedit.cutPanelHMax");
    if (s1 != null || s2 != null) {
       ival1 = Integer.parseInt(s1);
       ival2 = Integer.parseInt(s2);
       cutPanelMaxSize = new Dimension(ival1, ival2);
    }

    s1 = props.getProperty("ndedit.lonReference");
    if (s1 != null) {
       lonReference = (double)Double.parseDouble(s1); 
    }

    s1 = props.getProperty("ndedit.geoDisplayFormat");
    if (s1 != null) {
       geoDisplayFormat = Integer.parseInt(s1);
    }

    s1 = props.getProperty("ndedit.timeDisplayFormat");
    if (s1 != null) {
       timeDisplayFormat = Integer.parseInt(s1);
    }

    s1 = props.getProperty("ndedit.timeAxisMode");
    if (s1 != null) {
       timeAxisMode = Integer.parseInt(s1);
    }

    s1 = props.getProperty("timeAxisReference");
    if (s1 != null) {
       timeAxisReference = Double.parseDouble(s1);
    }

    s1 = props.getProperty("ndedit.displayPanelAxes");
    if (s1 == null) {
       displayPanelAxes = true;
    }
    else {
       displayPanelAxes = Boolean.valueOf(s1).booleanValue();
    }

    s1 = props.getProperty("ndedit.independentHandles");
    if (s1 == null) {
       independentHandles = true;
    }
    else {
       independentHandles = Boolean.valueOf(s1).booleanValue();
    }
}

  /**
  * 
  */
  public void save(String filename) {
     try {
     	File propFile = new File(NdEditFormulas.getSupportPath(), filename);
        FileOutputStream f = new FileOutputStream(propFile);
        internalToProperties().save(f, "ndEdit User Settings");
     }
     catch (Exception e) {
        String errmsg = new String("Cannot open FileOutputStream: " + filename);
        JOptionPane.showMessageDialog(null, errmsg,"ERROR",JOptionPane.ERROR_MESSAGE);
     }
  }

  /**
  * 
  */
  public void load(String filename) {
     FileInputStream f;
     try {
     	File propFile = new File(NdEditFormulas.getSupportPath(), filename);
        f = new FileInputStream(propFile);
     }
     catch (Exception e) {
        System.out.println("Exception: " + e.toString());
        String errmsg = new String("Cannot open input stream (or load properties): " + filename);
        JOptionPane.showMessageDialog(null, errmsg,"ERROR",JOptionPane.ERROR_MESSAGE);
		return;
     }
     Properties props = new Properties();
     try {
        props.load(f);
     }
     catch (Exception e) {
        System.out.println("Exception: " + e.toString());
        String errmsg = new String("Cannot or load properties from file: " + filename);
        JOptionPane.showMessageDialog(null, errmsg,"ERROR",JOptionPane.ERROR_MESSAGE);
		return;
     }
     propertiesToInternal(props);
  }

  /**
  * 
  */
  public String toString() {

    StringBuffer st = new StringBuffer();

    st.append(" Cut Panels: ");
    for (int i = 0; i < visibleViewIds.size(); i++) {
       st.append(" " + Constants.cpNames[
		((Integer)visibleViewIds.elementAt(i)).intValue()]);
    }
    st.append(" \nCutPanelSize: "); st.append(cutPanelSize);
    st.append(" \nlonReference: "); 	st.append(lonReference);
    st.append(" \ngeoDisplayFormat: "); 	st.append(geoDisplayFormat);
    st.append(" \nuseCenterWidthOrMinMax: "); st.append(useCenterWidthOrMinMax);
    st.append(" \ntimeDisplayFormat: "); 	st.append(timeDisplayFormat);
    st.append(" \ntimeAxisMode:  "); 	st.append(timeAxisMode);
    st.append(" \ntimeAxisReference:  ");   st.append(timeAxisReference);
    st.append(" \ntimeSinceUnits:  "); 	st.append(timeSinceUnits);
    st.append(" \ndisplayPanelAxes:  "); 	st.append(displayPanelAxes);
    st.append(" \nindependentHandles:  "); 	st.append(independentHandles);
    return st.toString();
  }


   static public void main(String[] args) {
      UserSettings us = new UserSettings("e:/windsor/together/samples/ndEdit/uset.txt");
   }
}

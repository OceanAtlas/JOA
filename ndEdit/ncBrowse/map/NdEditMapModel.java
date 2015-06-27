/*
 * $Id: NdEditMapModel.java,v 1.4 2005/02/15 18:31:11 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit.ncBrowse.map;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.Iterator;
import java.util.List;
import java.util.Enumeration;
import java.util.Vector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ndEdit.ncBrowse.NcFile;
import ndEdit.ncBrowse.LocalNcFile;
import ndEdit.Debug;

import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.Attribute;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import gov.noaa.pmel.util.Range2D;
import gov.noaa.pmel.util.GeoDate;
import gov.noaa.pmel.util.GeoDateArray;
import gov.noaa.pmel.util.SoTRange;
import gov.noaa.pmel.util.SoTValue;
import gov.noaa.pmel.util.Point2D;

/**
 * <pre>
 * Title:        netCDF File Browser
 * Description:  General purpose netCDF file Browser.
 * Copyright:    Copyright (c) 2003
 * Company:      NOAA/PMEL/EPIC
 * </pre>
 * @author John Osborne from ncBrowse by Donald Denbo
 * @version $Revision: 1.4 $, $Date: 2005/02/15 18:31:11 $
 */
public class NdEditMapModel /*implements Runnable, ChangeListener*/ {
	public static final int LONGITUDE = 0;
	public static final int LATITUDE = 1;
	public static final int Z = 2;
	public static final int TIME = 3;
	public static final int ELEMENT_COUNT = 4;

	public static String[] names_ = {"Latitude",
	                               "Longitude",
	                               "Z",
	                               "Time"};
	String errorMessage_ = "";
	private String name = null;
	/**
	* Object array is treated like an associative array
	*/
	Object[] dimensionElements = new Object[ELEMENT_COUNT];
	Object[] ndEditParameters = new Object[ELEMENT_COUNT];
	boolean[] mAxisExistsInOriginalFile = new boolean[ELEMENT_COUNT];
	boolean[] mAxisAdded = new boolean[ELEMENT_COUNT];

	/**
	*@link aggregation
	*     @associates <{NdEditMapParameter}>
	* @supplierCardinality *
	* @label ndEditParameter
	*/
	private Vector changeListeners_ = new Vector();
	private ChangeEvent event_ = new ChangeEvent(this);
	private NcFile ncFile;
	private boolean changed = false;
	private GeoDate refDate;
	private boolean is624;
	private int[] time2;
	private int increment;
	Vector globalAttributes = new Vector();

	public NdEditMapModel(NcFile ncfile) {
		//reset();
		ncFile = ncfile;
		for (int i=0; i<ELEMENT_COUNT; i++) {
			mAxisExistsInOriginalFile[i] = false;
			mAxisAdded[i] = false;
		}
	}
	
	public void addAttribute(Attribute a) {
		globalAttributes.addElement(a);
	}
	
	public Vector getAttributes() {
		return globalAttributes;
	}

	public void setElement(Object obj, int type) {
		dimensionElements[type] = obj;
		NdEditMapParameter param = new NdEditMapParameter(ncFile, obj, true);
		ndEditParameters[type] = param;
		mAxisExistsInOriginalFile[type] = true;
	}

	public void setElement(NcFile nr, Object obj, int type) {
		dimensionElements[type] = obj;
		NdEditMapParameter param = new NdEditMapParameter(nr, obj, true);
		ndEditParameters[type] = param;
		mAxisAdded[type] = true;
	}
	
	public boolean[] getOriginalShape() {
		return mAxisExistsInOriginalFile;
	}
	
	public boolean[] getAddedAxes() {
		return mAxisAdded;
	}
	
	public NcFile getNcFile(int axis) {
		return ((NdEditMapParameter)ndEditParameters[axis]).getNcFile();
	}

	public boolean isSet(int type) {
		return dimensionElements[type] != null;
	}

	public NdEditMapParameter getParamElement(int i) {
		return (NdEditMapParameter)ndEditParameters[i];
	}

	public Object getDimElement(int type) {
		return dimensionElements[type];
	}

	public int getDimCount() {
		int c = 0;
		for (int i=0; i<ELEMENT_COUNT; i++) {
			if (dimensionElements[i] != null)
				c++;
		}
		return c;
	}
  
	public void reset() {
		for(int i=0; i<ELEMENT_COUNT; i++) {
			dimensionElements[i] = null;
			ndEditParameters[i] = null;
		}
	}
	
	public boolean isMapCorrect() {
		// return a true if the map's dimensions match NdEdit's requirements
		if (ndEditParameters[LONGITUDE] != null && ndEditParameters[LATITUDE] != null
		     && ndEditParameters[Z] != null && ndEditParameters[TIME] != null)
			return true;
		else
			return false;
	}
}
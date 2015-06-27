/*
 * $Id: NdEditMapParameter.java,v 1.7 2005/02/15 18:31:11 oz Exp $
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

import ndEdit.ncBrowse.NcFile;
import ndEdit.Debug;

import java.util.Vector;
import java.util.Enumeration;
import java.io.IOException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import gov.noaa.pmel.util.SoTRange;
import gov.noaa.pmel.util.SoTValue;
import gov.noaa.pmel.util.GeoDate;

import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriteable;
import ucar.ma2.Array;
import ndEdit.NdEditFormulas;

/**
 * <pre>
 * Title:        netCDF File Browser
 * Description:  General purpose netCDF file Browser.
 * Copyright:    Copyright (c) 2000
 * Company:      NOAA/PMEL/EPIC
 * </pre>
 * @author Donald Denbo as modified by John Osborne
 * @version $Revision: 1.7 $, $Date: 2005/02/15 18:31:11 $
 */
public class NdEditMapParameter implements Comparable {
	/**
	* True if this parameter can be a range
	*/
	private boolean rangeAllowed;
	/**
	* True if this parameter is sin
	*/
	private boolean single;
	/**
	* Group number.  For grouped parameters.  At least one of the group must
	* have a range.
	*/
	private int group;
	private int length;
	private Object element;
	private int[] indexValues;
	private SoTRange range;
	private Variable ncVar;
	private SoTRange fullRange = null;
	private boolean time;
	private NcFile ncFile;
	private Vector changeListeners = new Vector();
	private ChangeEvent event = new ChangeEvent(this);
	private Vector newVals = new Vector();
	private Vector oldVals = new Vector();
	private boolean[] valChanged;
	private String newUnits;
	private String newDimName;
	private boolean mIsClimatology = false;
	private int mClimatologyLength;

	/**
	* NdEditMapParameter constuctor.
	* @param ncFile netCDF file
	* @param elem parameter element, always a Dimension
	* @param rangeAllowed true if the parameter can be a range
	*/
	public NdEditMapParameter(NcFile inncFile, Object elem, boolean rangeallowed) {
		ncFile = inncFile;
		element = elem;
		ncVar = ((Dimension)elem).getCoordinateVariable();
		ncVar = inncFile.findVariable(((ucar.nc2.Dimension)elem).getName());
		rangeAllowed = rangeallowed;
		single = !rangeallowed;
		this.computeFullRange();
		range = getFullRange();
		int len = ((Dimension)element).getLength();
		indexValues = new int[len];
		valChanged = new boolean[len];
		for (int i=0; i < len; i++) {
			indexValues[i] = i;
			valChanged[i] = false;
		}
		initExistingValues();
	}
	
	public NcFile getNcFile() {
		return ncFile;
	}
	
	/**
	* NdEditMapParameter constuctor.
	* @param ncFile netCDF file
	* @param elem parameter element, always a Dimension
	* @param rangeAllowed true if the parameter can be a range
	*/
	/*public NdEditMapParameter(NetcdfFileWriteable inncFile, Object elem, boolean rangeallowed) {
		ncFile = inncFile;
		element = elem;
		System.out.println("elem = " + elem);
		ncVar = inncFile.findVariable(((ucar.nc2.Dimension)elem).getName());
		rangeAllowed = rangeallowed;
		single = !rangeallowed;
		this.computeFullRange();
		range = getFullRange();
		int len = ((Dimension)element).getLength();
		indexValues = new int[len];
		valChanged = new boolean[len];
		for (int i=0; i < len; i++) {
			indexValues[i] = i;
			valChanged[i] = false;
		}
		initExistingValues();
	}*/
	
	public void setNewDimName(String name) {
		newDimName = new String(name);
	}
	
	public void setUnits(String units) {
		newUnits = new String(units);
	}
	
	public void setNewValue(Object obj, int i, boolean isNew) {
		if (isNew)
			newVals.addElement(obj);
		else
			newVals.setElementAt(obj, i);
		valChanged[i] = true;
	}
	
	public void setIsClimatology(boolean f) {
		mIsClimatology = f;
	}
	
	public boolean isClimatology() {
		return mIsClimatology;
	}
	
	public void setClimatologyLength(int i) {
		mClimatologyLength = i;
	}
	
	public int getClimatologyLength() {
		return mClimatologyLength;
	}
	
	public Vector expandtoVector() {
		valChanged = new boolean[2];
		indexValues = new int[2];
		for (int i=0; i < 2; i++) {
			indexValues[i] = i;
			valChanged[i] = false;
		}
		oldVals.removeAllElements();
		
		// this will add the one existing value based upon the dimension variable;
		initExistingValues();
		
		// copy existing value into new position
		oldVals.addElement(oldVals.elementAt(0));
		length = 2;
		fullRange = new SoTRange.GeoDate(new GeoDate(), new GeoDate());
		range = fullRange.copy();
		return oldVals;
	}
	
	public Object getNewValue(int i) {
		if (newVals.size() > 0 && newVals.elementAt(i) != null)
			return newVals.elementAt(i);
		return oldVals.elementAt(i);
	}
	
	public double getNewDoubleValue(int i) {
		if (valChanged[i]) {
			if (newVals.elementAt(i) instanceof Double)
				return ((Double)newVals.elementAt(i)).floatValue();
			else  {
				// try a String
				try {
					double fval = Double.valueOf((String)newVals.elementAt(i)).floatValue();
					return fval;
				}
				catch (Exception ex) {
					return ((Double)oldVals.elementAt(i)).floatValue();
				}
			}
		}
		else {
			if (oldVals.elementAt(i) instanceof Double)
				return ((Double)oldVals.elementAt(i)).floatValue();
			else  {
				// try a String
				try {
					double fval = Double.valueOf((String)oldVals.elementAt(i)).floatValue();
					return fval;
				}
				catch (Exception ex) {
					return ((Double)oldVals.elementAt(i)).floatValue();
				}
			}
		}
	}
	
	public int getNumNewVals() {
		return newVals.size();
	}
	
	public Variable getCoordinateVariable() {
		return ncVar;
	}

	public int compareTo(Object obj) {
		if (obj instanceof NdEditMapParameter) {
			NdEditMapParameter param = (NdEditMapParameter)obj;
			if (rangeAllowed) {
				if (!param.isRangeAllowed()) return -1;
			} 
			else {
				if(param.isRangeAllowed()) return 1;
			}
			if(group > param.getGroup()) return 1;
			if(group < param.getGroup()) return -1;
			return getName().compareTo(param.getName());
		}
		throw new ClassCastException();
	}

	public int[] getValues() {
		return indexValues;
	}

	public boolean isRangeAllowed(){
		return rangeAllowed;
	}

	public void setRangeAllowed(boolean rangeAllowed){
		// don't track for ChangeEvent
		rangeAllowed = rangeAllowed;
		single = !rangeAllowed;
	}

	public void setGroup(int group) {
		// don't track for ChangeEvent
		group = group;
	}

	public int getGroup() {
		return group;
	}

	public void setSingle(boolean single) {
		if (rangeAllowed && single != single) {
			single = single;
			fireChangeEvent();
		}
	}

	public boolean isSingle() {
		if (length == 1) return true;
		return single;
	}

	public Object getElement() {
		return element;
	}

	public void setSoTRange(SoTRange irange) {
		if (!range.equals(irange)) {
			range = irange;
			fireChangeEvent();
		}
	}
	
	public void initExistingValues() {
    	// need to get the existing values for this variable
    	long numVals = ncVar.getSize();
        int[] origin = new int[ncVar.getRank()];
        int[] extent = ncVar.getShape();
        try {
       		Array ma = (Array)ncVar.read(origin, extent);
          	Object anArray = ma.copyTo1DJavaArray();
          	int count = 0;
          	if (time) {
	          	for (int i=0; i<numVals; i++) {
	          		// convert lval into a GeoDate
	          		GeoDate gd = NdEditFormulas.convertToGeoDate(ncFile, ncVar, i);
					oldVals.addElement(gd);
	        	}
          	}
          	else {
	          	for (int i=0; i<numVals; i++) {
		          	if (anArray instanceof float[]) {
		          		double val = ((float[])anArray)[i];
						oldVals.addElement(new Double(val));
		          	}
		          	else if (anArray instanceof double[]) {
		          		double fval = ((double[])anArray)[i];
						oldVals.addElement(new Double(fval));
		          	}
		          	else if (anArray instanceof long[]) {
		          		long lval = ((long[])anArray)[i];
						oldVals.addElement(new Long(lval));
		          	}
		          	else if (anArray instanceof int[]) {
		          		int ival = ((int[])anArray)[i];
						oldVals.addElement(new Integer(ival));
		          	}
		        }
			}
       	}
       	catch (Exception ex) {}
  	}

	public SoTRange getSoTRange() {
		SoTRange nrange = range.copy();
		//      if (isSingle()) {
		//        range.setEnd(range.getStart());
		//      }
		return nrange;
	}

	public void setSoTValue(SoTValue value) {
		SoTValue start = range.getStart();
		if(!start.equals(value)) {
			range.setStart(value);
			range.setEnd(value);
			fireChangeEvent();
		}
	}

	public void setValueIndex(int index) {
		int ind = index;
		if(ind < 0) ind = 0;
		if(ind >= length) ind = length - 1;
		SoTValue sValue = null;
		sValue = new SoTValue.Integer(ind);
		if(Debug.DEBUG)
			System.out.println(" index = " + ind + ", value = " + ind);
		SoTValue start = range.getStart();
		if (!start.equals(sValue)) {
			range.setStart(sValue);
			range.setEnd(sValue);
			fireChangeEvent();
		}
	}

	public SoTValue getSoTValue() {
		return range.getStart();
	}

	public String getName() {
		return ((Dimension)element).getName();
	}

	public String getUnits() {
		/*Attribute attr;
		if (!dimension) {
			attr = ((Variable)element).findAttribute("units");
			if (attr != null) return attr.getStringValue();
		}*/
		return "";
	}

	public boolean isTime() {
		return time;
	}

	public boolean isDimension() {
		return true;
	}

	public boolean isVariable() {
		return false;
	}

	public SoTRange getFullRange() {
		return fullRange.copy();
	}

	private void computeFullRange() {
		if (fullRange != null) return;
		time = false;
		time = ncFile.isVariableTime(ncVar);
		length = (int)ncVar.getSize();
		Object start = ncFile.getArrayValue(ncVar, 0);
		Object end = ncFile.getArrayValue(ncVar, length-1);
		
		if (start instanceof GeoDate) {
			fullRange = new SoTRange.GeoDate((GeoDate)start, (GeoDate)end);
		} 
		else if(start instanceof Integer) {
			fullRange = new SoTRange.Integer(((Integer)start).intValue(),((Integer)end).intValue());
		} 
		else if(start instanceof Short) {
			fullRange = new SoTRange.Short(((Short)start).shortValue(),((Short)end).shortValue());
		} 
		else if(start instanceof Float) {
			fullRange = new SoTRange.Float(((Float)start).floatValue(),((Float)end).floatValue());
		} 
		else if(start instanceof Double) {
			fullRange = new SoTRange.Double(((Double)start).doubleValue(), ((Double)end).doubleValue());
		}
	}

	public int getLength() {
		return length;
	}

	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(getName()+" ");
		sbuf.append(range);
		sbuf.append(", range=" + rangeAllowed);
		sbuf.append(", single=" + single);
		//sbuf.append(", group=" + VMapModel.getTitle(group));
		return sbuf.toString();
	}

	public void addChangeListener(ChangeListener l) {
		changeListeners.add(l);
	}

	public void removeChangeListener(ChangeListener l) {
		changeListeners.remove(l);
	}

	private void fireChangeEvent() {
		for(Enumeration e = changeListeners.elements(); e.hasMoreElements();) {
			((ChangeListener)e.nextElement()).stateChanged(event);
		}
	}
}
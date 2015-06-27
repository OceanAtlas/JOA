/*
 * $Id: NdEditEnumValsTableModel.java,v 1.4 2005/02/15 18:31:11 oz Exp $
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

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.event.TableModelEvent;
import java.util.Vector;
import java.util.Enumeration;
import java.util.ArrayList;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ndEdit.ncBrowse.NcFile;
import ucar.nc2.Attribute;
import java.util.Iterator;
import java.util.*;
import ucar.ma2.Array;
import gov.noaa.pmel.util.GeoDate;
import gov.noaa.pmel.util.IllegalTimeValue;
import java.io.IOException;
import ndEdit.NdEditFormulas;

/**
 * <pre>
 * Title:        netCDF File Browser
 * Description:  General purpose netCDF file Browser.
 * Copyright:    Copyright (c) 2000
 * Company:      NOAA/PMEL/EPIC
 * </pre>
 * @author Donald Denbo
 * @version $Revision: 1.4 $, $Date: 2005/02/15 18:31:11 $
 */

public class NdEditEnumValsTableModel implements TableModel {
	ResourceBundle b = ResourceBundle.getBundle("ndEdit.NdEditResources");
	String[] colNames = {b.getString("kIndex"),b.getString("kValue2"), b.getString("kNewValue")};
	Vector indices;
	Vector values;
	Vector newValues;
	Vector listeners = new Vector();
	Vector indexMap;
	int rowCount = 0;
	Vector dims = new Vector();
	private boolean mIsTime = false;
  	private ndEdit.ncBrowse.NcFile mNcfile;
  	private boolean mTableValuesChanged = false;
  	
  	public NdEditEnumValsTableModel(Vector objs, boolean istime) {
		indices = new Vector();		// names of dimensions
		values = new Vector();		// existing values
		newValues = new Vector();		// existing values
    	indexMap = new Vector();
    	mIsTime = istime;
    	
    	// need to get the values for this variable
    	long numVals = objs.size();
    	int count = 0;
      	if (mIsTime) {
          	for (int i=0; i<numVals; i++) {
          		// convert lval into a GeoDate
          		GeoDate gd = (GeoDate)(objs.elementAt(i));
				values.add(gd.toString());
	          	newValues.add(new String(" "));
				indices.addElement(new Integer(i));
        		rowCount++;
        		indexMap.add(new Integer(count++));
        	}
      	}
      	else {
          	/*for (int i=0; i<numVals; i++) {
          		Object o = objs.elementAt(i);
	          	if (anArray instanceof float[]) {
	          		double val = ((float[])anArray)[i];
					values.add(new Double(val));
	          	}
	          	else if (anArray instanceof double[]) {
	          		double fval = ((double[])anArray)[i];
					values.add(new Double(fval));
	          	}
	          	else if (anArray instanceof long[]) {
	          		long lval = ((long[])anArray)[i];
					values.add(new Long(lval));
	          	}
	          	else if (anArray instanceof int[]) {
	          		int ival = ((int[])anArray)[i];
					values.add(new Integer(ival));
	          	}
	          	newValues.add(new String(""));
				indices.addElement(new Integer(i));
        		rowCount++;
        		indexMap.add(new Integer(count++));
	        }*/
		}
  	
  	}

	public NdEditEnumValsTableModel(NcFile ncfile, Variable inVar, boolean istime) {
		mNcfile = ncfile;
		Attribute attr = null;
		StringBuffer line;
		indices = new Vector();		// names of dimensions
		values = new Vector();		// existing values
		newValues = new Vector();		// existing values
    	indexMap = new Vector();
    	Variable ncVar = inVar;
    	mIsTime = istime;
    	
    	// need to get the values for this variable
    	long numVals = inVar.getSize();
        int[] origin = new int[ncVar.getRank()];
        int[] extent = ncVar.getShape();
        try {
       		Array ma = (Array)ncVar.read(origin, extent);
          	Object anArray = ma.copyTo1DJavaArray();
          	int count = 0;
          	if (mIsTime) {
	          	for (int i=0; i<numVals; i++) {
	          		// convert lval into a GeoDate
	          		GeoDate gd = NdEditFormulas.convertToGeoDate(mNcfile, inVar, i);
					values.add(gd.toString());
		          	newValues.add(new String(" "));
					indices.addElement(new Integer(i));
	        		rowCount++;
	        		indexMap.add(new Integer(count++));
	        	}
          	}
          	else {
	          	for (int i=0; i<numVals; i++) {
		          	if (anArray instanceof float[]) {
		          		double val = ((float[])anArray)[i];
						values.add(new Double(val));
		          	}
		          	else if (anArray instanceof double[]) {
		          		double fval = ((double[])anArray)[i];
						values.add(new Double(fval));
		          	}
		          	else if (anArray instanceof long[]) {
		          		long lval = ((long[])anArray)[i];
						values.add(new Long(lval));
		          	}
		          	else if (anArray instanceof int[]) {
		          		int ival = ((int[])anArray)[i];
						values.add(new Integer(ival));
		          	}
		          	newValues.add(new String(""));
					indices.addElement(new Integer(i));
	        		rowCount++;
	        		indexMap.add(new Integer(count++));
		        }
			}
       	}
       	catch (Exception ex) {}
  	}
  	
	
	public Object getValAt(int rowIndex) {
		try {
			int index = ((Integer)indexMap.elementAt(rowIndex)).intValue();
			return values.elementAt(index);
		}
		catch (Exception ex) {
			return null;
		}
	}

	public int getRowCount() {
		return rowCount;
	}

	public int getColumnCount() {
		return 3;
	}

	public String getColumnName(int columnIndex) {
		return colNames[columnIndex];
	}

	public Class getColumnClass(int columnIndex) {
		return colNames[columnIndex].getClass();
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex > 1)
			return true;
		return false;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		int index = ((Integer)indexMap.elementAt(rowIndex)).intValue();
		if (columnIndex == 0) {
			return indices.elementAt(index);
		} 
		else if (columnIndex == 1) {
			return values.elementAt(index);
		}
		else {
			return newValues.elementAt(index);
		}
	}

	public Vector getIndexMap() {
		return indexMap;
	}
	
	public boolean isTableChanged() {
		return mTableValuesChanged;
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		int index = ((Integer)indexMap.elementAt(rowIndex)).intValue();
		if (columnIndex > 1) {
			newValues.setElementAt(aValue, index);
			mTableValuesChanged = true;
		}
		fireTableModelChange(rowIndex, rowIndex, columnIndex, TableModelEvent.UPDATE);
	}

	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

	private void fireTableModelChange(int firstRow, int lastRow, int column, int type) {
		TableModelEvent tme = new TableModelEvent(this, firstRow, lastRow, column, type);
		Enumeration iter = listeners.elements();
		while(iter.hasMoreElements()) {
			((TableModelListener)iter.nextElement()).tableChanged(tme);
		}
	}
}

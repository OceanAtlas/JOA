/*
 * $Id: NdEditTableModel.java,v 1.3 2005/02/15 18:31:11 oz Exp $
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
import java.util.List;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ndEdit.ncBrowse.NcFile;
import ucar.nc2.Attribute;
import java.util.Iterator;
import java.util.*;
import ucar.ma2.Array;

/**
 * <pre>
 * Title:        netCDF File Browser
 * Description:  General purpose netCDF file Browser.
 * Copyright:    Copyright (c) 2000
 * Company:      NOAA/PMEL/EPIC
 * </pre>
 * @author Donald Denbo
 * @version $Revision: 1.3 $, $Date: 2005/02/15 18:31:11 $
 */

public class NdEditTableModel implements TableModel {
	ResourceBundle b = ResourceBundle.getBundle("ndEdit.NdEditResources");
	String[] colNames = {b.getString("kName2"),b.getString("kDescription")};
	Vector names;
	Vector desc;
	Vector listeners = new Vector();
	Vector indexMap;
	int rowCount = 0;
	Vector dims = new Vector();
	private boolean requiresRegridding = false;
	private int whichVar = 0;

	public NdEditTableModel(Vector inVars, NcFile ncFile, int whichvar) {
		whichVar = whichvar;
		Attribute attr = null;
		StringBuffer line;
		names = new Vector();		// names of dimensions
		desc = new Vector();		// units
    	indexMap = new Vector();

		requiresRegridding = false;
		if (inVars.size() > 1) {
			requiresRegridding = !isSameGrid(inVars);
		}

		List al = ((Variable)inVars.elementAt(whichVar)).getDimensions();
		int count = 0;
		for (int i=0; i<al.size(); i++) {
			// get the ith dimension
			ucar.nc2.Dimension vdim = (ucar.nc2.Dimension)al.get(i);
			if (vdim == null) {
				System.out.println("error getting dimension #1");
				continue;
			}
			dims.addElement(vdim);

			// store the name
			names.addElement(vdim.getName());
			Iterator dimIter = ncFile.getDimensionIterator();

			// look for the units by looking through the dimension variables for a units attribute
			while (dimIter.hasNext()) {
				ucar.nc2.Dimension dim = (ucar.nc2.Dimension)dimIter.next();
				Variable ncVar = dim.getCoordinateVariable();
				if (ncVar != null && !vdim.getName().equalsIgnoreCase(ncVar.getName()))
					continue;
				line = new StringBuffer("length = " + vdim.getLength());
				if (ncVar == null) {
					line.append("; (index)");
					desc.addElement(line.toString());
					break;
				}
				else {
					attr = ncVar.findAttribute("units");
					if (attr == null) {
						line.append("; No Units Available!");
						desc.addElement(line.toString());
						break;
					}
					else {
						line.append(": (" + attr.getStringValue() + ")");
						desc.addElement(line.toString());
						break;
					}
				}
			}
        	rowCount++;
        	indexMap.add(new Integer(count++));
		}
	}

	public NdEditTableModel(Vector inVars, NcFile ncFile) {
		this(inVars, ncFile, 0);
	}

  	public boolean isSameGrid() {
  		return !requiresRegridding;
  	}

	private boolean isSameGrid(Vector inVars) {
		Vector currNames = new Vector();
		Vector lastNames = new Vector();

		for (int v=1; v<inVars.size(); v++) {
			// test the ranks
   			int rank = ((Variable)inVars.elementAt(v)).getRank();
   			int rankm1 = ((Variable)inVars.elementAt(v - 1)).getRank();

   			if (rank != rankm1) {
   				return false;
   			}
   				
   			// test the shape of the variables
   			int[] shape = ((Variable)inVars.elementAt(v)).getShape();
   			int[] shapem1 = ((Variable)inVars.elementAt(v)).getShape();

			if (shape.length != shapem1.length) {
   				return false;
   			}
   				
			for (int i=0; i<shape.length; i++) {
				if (shape[i] != shapem1[i]) {
	   				return false;
	   			}
			}

			// same number of dimensions
			List al = ((Variable)inVars.elementAt(v)).getDimensions();
			List alm1 = ((Variable)inVars.elementAt(v - 1)).getDimensions();

			if (al.size() != alm1.size()) {
   				return false;
   			}

			// dimensions have the same names?
			for (int i=0; i< alm1.size(); i++) {
				// get the ith dimension
				ucar.nc2.Dimension vdim = (ucar.nc2.Dimension)alm1.get(i);
				lastNames.addElement(vdim.getName());
			}

			for (int i=0; i< al.size(); i++) {
				// get the ith dimension
				ucar.nc2.Dimension vdim = (ucar.nc2.Dimension)al.get(i);
				currNames.addElement(vdim.getName());
			}

			for (int i=0; i< al.size(); i++) {
				String name = (String)currNames.elementAt(i);
				String namem1 = (String)lastNames.elementAt(i);
				if (!name.equalsIgnoreCase(namem1)) {
   					// names of the dimensions are not the same but could still be the same grid
   					// look at the actual values
					ucar.nc2.Dimension vdim1 = (ucar.nc2.Dimension)alm1.get(i);
					ucar.nc2.Dimension vdim2 = (ucar.nc2.Dimension)al.get(i);
					Variable var1 = (Variable)vdim1.getCoordinateVariable();
					Variable var2 = (Variable)vdim2.getCoordinateVariable();
			        int[] origin1 = new int[var1.getRank()];
			        int[] extent1 = var1.getShape();
			        int[] origin2 = new int[var2.getRank()];
			        int[] extent2 = var2.getShape();
    				long numVals1 = var1.getSize();
    				long numVals2 = var2.getSize();
    				if (numVals1 != numVals2) {
    					return false;
    				}
    				
			        try {
			       		Array ma1 = (Array)var1.read(origin1, extent1);
			          	Object anArray1 = ma1.copyTo1DJavaArray();
			       		Array ma2 = (Array)var2.read(origin2, extent2);
			          	Object anArray2 = ma2.copyTo1DJavaArray();
			          	double val1 = Double.NaN;
			          	double val2 = Double.NaN;
		          		for (int vv=0; vv<numVals1; vv++) {
				          	if (anArray1 instanceof float[]) {
				          		val1 = (double)((float[])anArray1)[vv];
				          	}
				          	else if (anArray1 instanceof double[]) {
				          		val1 = ((double[])anArray1)[vv];
				          	}
				          	else if (anArray1 instanceof long[]) {
				          		val1 = (double)((long[])anArray1)[vv];
				          	}
				          	else if (anArray1 instanceof int[]) {
				          		val1 = (double)((int[])anArray1)[vv];
				          	}
				          	if (anArray2 instanceof float[]) {
				          		val2 = (double)((float[])anArray2)[vv];
				          	}
				          	else if (anArray2 instanceof double[]) {
				          		val2 = ((double[])anArray2)[vv];
				          	}
				          	else if (anArray2 instanceof long[]) {
				          		val2 = (double)((long[])anArray2)[vv];
				          	}
				          	else if (anArray2 instanceof int[]) {
				          		val2 = (double)((int[])anArray2)[vv];
				          	}
				          	if (val1 != val2) {
				          		return false;
				          	}
		          		}
			        }
       				catch (Exception ex) {return false;}
				}
			}
		}
		return true;
	}

	public Object getDimAt(int rowIndex) {
		try {
			int index = ((Integer)indexMap.elementAt(rowIndex)).intValue();
			return dims.elementAt(index);
		}
		catch (Exception ex) {
			return null;
		}
	}

	public int getRowCount() {
		return rowCount;
	}

	public int getColumnCount() {
		return 2;
	}

	public String getColumnName(int columnIndex) {
		return colNames[columnIndex];
	}

	public Class getColumnClass(int columnIndex) {
		return colNames[columnIndex].getClass();
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		int index = ((Integer)indexMap.elementAt(rowIndex)).intValue();
		if (columnIndex == 0) {
			return names.elementAt(index);
		}
		else {
			return desc.elementAt(index);
		}
	}

	public Vector getIndexMap() {
		return indexMap;
	}

  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    int index = ((Integer)indexMap.elementAt(rowIndex)).intValue();
    if (columnIndex == 0) {
      names.setElementAt(aValue, index);
    }
    else {
      desc.setElementAt(aValue, index);
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

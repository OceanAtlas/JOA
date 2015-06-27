/*
 * $Id: PointerCollection.java,v 1.19 2005/06/27 23:25:28 oz Exp $
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

import java.util.*;
import gov.noaa.pmel.util.*;
import java.beans.*;

 /**
 * @author John Osborne
 * @version 1.0 01/13/00
 */
public interface PointerCollection {
	// get the size
	public int getSize();
	public int getNumLats();
	public int getNumLons();
	public int getNumDepths();
	public int getNumTimes();
	public String getPCTitle();
	public void setPCTitle(String t);
	
	// methods for getting arrays of values
	// in the tuple array these are as long the number of tuples
	// xArr1.length == xArr2.length
	public double[] getTimeArr1();
	public double[] getTimeArr2();
	public double[] getDepthArr1();
	public double[] getDepthArr2();
	public double[] getLonArr1();
	public double[] getLonArr2();
	public double[] getLatArr1();
	public double[] getLatArr2();
	public String[] getFileNameArr();
	public String[] getPathArr();
	public boolean[] getPathIsRelativeArr();
	public String[] getDataTypeArr();
	public String[] getCastArr();
	public String[] getCruiseArr();
	public double[] getDeltaArr();
	public double getMinWLon();
	public double getMaxWLon();
	public double getMinELon();
	public double getMaxELon();
	public String[] getExtraArr1();
	public String[] getExtraArr2();
	public String[] getExtraArr3();
	public String[] getExtraArr4();
	public String[] getExtraArr5();
	public String[] getExtraArr6();
	public String[] getExtraArr7();
	public String[] getExtraArr8();
	public String[] getExtraArr9();
	public String[] getExtraArr10();
	
	// here are the virtual getters that will replace the 
	// above array getters
	public double getLat1(int i);
	public double getLat2(int i);
	public double getLon1(int i);
	public double getLon2(int i);
	public double getZ1(int i);
	public double getZ2(int i);
	public double getT1(int i);
	public double getT2(int i);
	
	// get the limits of the major axes
	public double getMinLon();
	public double getMaxLon();
	public double getMinLat();
	public double getMaxLat();
	public double getMinTime();
	public double getMaxTime();
	public double getMinDepth();
	public double getMaxDepth();
	public double[] getMinMaxLat();
	public double[] getMinMaxLon();
	public double[] getMinMaxDepth();
	public double[] getMinMaxTime();
	
	// get sorted versions of the arrays
	public double[] getTimeArr1Sorted();
	public int[] getTimeArr1SortedIndices();
	public double[] getDepthArr1Sorted();
	public int[] getDepthArr1SortedIndices();
	public double[] getLonArr1Sorted();
	public int[] getLonArr1SortedIndices();
	public double[] getLatArr1Sorted();
	public int[] getLatArr1SortedIndices();
	public boolean isSomethingSelected();
	
	// utility methods
	public void resetSizes();
	public void unDeleteAll();
	public void unselectAll();
	public int getCurrDeletionIndex();
	public void setCurrDeletionIndex(int delOrd);
	public boolean crosses180();
	public void delete(int index, int delOrd);
	public void addPointers(PointerCollection pc);
	public boolean isSelected(int i);
	public boolean isDeleted(int i);
	public void select(int i);
	public int[] getIsDeletedArr();
	public boolean[] getIsSelectedArr();
	public void setBatchModeOn();
	public void setBatchModeOff();
	public boolean isBatchMode();
	public void addPropertyChangeListener(PropertyChangeListener l);
	public void removePropertyChangeListener(PropertyChangeListener l);
	public int getNumPtrs();
	public int getNumVisiblePtrs();
	public int getNumSelectedPtrs();
	public void updateStats();
	public boolean isArgo();
	public boolean isLatScaler();
	public boolean isLonScaler() ;
	public boolean isDepthScaler();
	public boolean isTimeScaler();
	public String getString(int i);
}



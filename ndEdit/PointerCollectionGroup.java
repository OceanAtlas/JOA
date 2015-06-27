/*
 * $Id: PointerCollectionGroup.java,v 1.19 2005/08/22 21:25:16 oz Exp $
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
import java.awt.*;
import gov.noaa.pmel.util.*;
	

 /**
 * @author John Osborne
 */
public class PointerCollectionGroup extends Vector {
	int mSize = 0;
	double[] mTimeArr1, mTimeArr2; 
	double[] mLatArr1, mLatArr2, mLonArr1, mLonArr2, mDepthArr1, mDepthArr2;
	double[] mMinMaxLat = new double[2];
	double[] mMinMaxLon = new double[2];
	double[] mMinMaxDepth = new double[2];
	double[] mMinMaxTime = new double[2];
	double mMinTime, mMaxTime;
	double mMinLat, mMaxLat, mMinLon, mMaxLon, mMinDepth, mMaxDepth;
	double mMinWLon, mMaxWLon, mMinELon, mMaxELon;
	int[] mIsDeleted;
	boolean[] mIsSelected;
	boolean[] mIsSelectable;
	double[] mDepthArr1Sorted, mLonArr1Sorted, mLatArr1Sorted;
	double[] mTimeArr1Sorted;
	int[] mTimeArr1SortedIndices, mDepthArr1SortedIndices, mLonArr1SortedIndices, mLatArr1SortedIndices;
	Color[] mLayerColors = new Color[100];
	int[] mRangeStarts = new int[100];
	int[] mRangeEnds = new int[100];
	private boolean mLatIsScaler;
	private boolean mLonIsScaler;
	private boolean mTimeIsScaler;
	private boolean mDepthIsScaler;
	
	public PointerCollectionGroup() {
		mLayerColors[0] = Color.red;
		mLayerColors[1] = Color.green.darker();
		mLayerColors[2] = Color.yellow;
		mLayerColors[3] = Color.cyan;
		mLayerColors[4] = Color.magenta;
		mLayerColors[5] = Color.orange;
		for (int i=6; i<100; i++)
			mLayerColors[i] = Color.black;
	}
	
	// override the add operator so that all the arrays get rebuilt 
	// whenever another PointerCollection is added to the group
	public void addElement(Object o) {
		super.addElement(o);
		
		// is this the first element?
		if (this.size() == 1) {
			for (int i=0; i<this.getSize(); i++)
				mIsSelectable[i] = true;
		}
		
		// compute the local arrays
		mSize = 0;
		for (int i=0; i<this.size(); i++) {
			if (i == 0)
				mRangeStarts[i] = 0;
			else
				mRangeStarts[i] = mSize;
			mSize += ((PointerCollection)this.elementAt(i)).getSize();
			mRangeEnds[i] = mSize;
		}
		
		// set the master flags that tell whether  an axis is scaler for all PCs
		mLatIsScaler = isLatScaler();
		mLonIsScaler = isLonScaler();
		mTimeIsScaler = isTimeScaler();
		mDepthIsScaler = isDepthScaler();

		// get the arrays
		mTimeArr1 = new double[mSize];
		int c = 0;
		for (int i=0; i<this.size(); i++) {
			double[] a = ((PointerCollection)this.elementAt(i)).getTimeArr1();
			for (int j=0; j<a.length; j++) {
				mTimeArr1[c] = a[j];
				c++;
			}
		}
		
		if (!mTimeIsScaler) {
			mTimeArr2 = new double[mSize];
			for (int i=0; i<mSize; i++) {
				mTimeArr2[i] = mTimeArr1[i];
			}
			c = 0;
			for (int i=0; i<this.size(); i++) {
				int sz = ((PointerCollection)this.elementAt(i)).getSize();
				double[] a = ((PointerCollection)this.elementAt(i)).getTimeArr2();
				if (a == null) {
					for (int j=0; j<sz; j++) {
						c++;
					}
				}
				else {
					for (int j=0; j<a.length; j++) {
						mTimeArr2[c] = a[j];
						c++;
					}
				}
			}
		}
		else
			mTimeArr2 = null; 
		
		mLatArr1 = new double[mSize];
		c = 0;
		for (int i=0; i<this.size(); i++) {
			double[] a = ((PointerCollection)this.elementAt(i)).getLatArr1();
			for (int j=0; j<a.length; j++) {
				mLatArr1[c] = a[j];
				c++;
			}
		}
		
		if (!mLatIsScaler) {
			mLatArr2 = new double[mSize];
			for (int i=0; i<mSize; i++) {
				mLatArr2[i] = mLatArr1[i];
			}
			c = 0;
			for (int i=0; i<this.size(); i++) {
				int sz = ((PointerCollection)this.elementAt(i)).getSize();
				double[] a = ((PointerCollection)this.elementAt(i)).getLatArr2();
				if (a == null) {
					for (int j=0; j<sz; j++) {
						c++;
					}
				}
				else {
					for (int j=0; j<a.length; j++) {
						mLatArr2[c] = a[j];
						c++;
					}
				}
			}
		}
		else
			mLatArr2 = null; 
		
		mLonArr1 = new double[mSize];
		c = 0;
		for (int i=0; i<this.size(); i++) {
			double[] a = ((PointerCollection)this.elementAt(i)).getLonArr1();
			for (int j=0; j<a.length; j++) {
				mLonArr1[c] = a[j];
				c++;
			}
		}
		
		if (!mLonIsScaler) {
			mLonArr2 = new double[mSize];
			for (int i=0; i<mSize; i++) {
				mLonArr2[i] = mLonArr1[i];
			}
			c = 0;
			for (int i=0; i<this.size(); i++) {
				int sz = ((PointerCollection)this.elementAt(i)).getSize();
				double[] a = ((PointerCollection)this.elementAt(i)).getLonArr2();
				if (a == null) {
					for (int j=0; j<sz; j++) {
						c++;
					}
				}
				else {
					for (int j=0; j<a.length; j++) {
						mLonArr2[c] = a[j];
						c++;
					}
				}
			}
		}
		else
			mLonArr2 = null; 
		
		mDepthArr1 = new double[mSize];
		c = 0;
		for (int i=0; i<this.size(); i++) {
			double[] a = ((PointerCollection)this.elementAt(i)).getDepthArr1();
			for (int j=0; j<a.length; j++) {
				mDepthArr1[c] = a[j];
				c++;
			}
		}
		
		if (!mDepthIsScaler) {
			mDepthArr2 = new double[mSize];
			for (int i=0; i<mSize; i++) {
				mDepthArr2[i] = mDepthArr1[i];
			}
			c = 0;
			for (int i=0; i<this.size(); i++) {
				int sz = ((PointerCollection)this.elementAt(i)).getSize();
				double[] a = ((PointerCollection)this.elementAt(i)).getDepthArr2();
				if (a == null) {
					for (int j=0; j<sz; j++) {
						c++;
					}
				}
				else {
					for (int j=0; j<a.length; j++) {
						mDepthArr2[c] = a[j];
						c++;
					}
				}
			}
		}
		else
			mDepthArr2 = null;
						
		// compute the extrema values by interogating the individual
		// pointer collections that make up the group
		mMinMaxLat[0] = 9e32f;
		mMinMaxLat[1] = -9e32f;
		mMinMaxLon[0] = 9e32f;
		mMinMaxLon[1] = -9e32f;
		mMinMaxDepth[0] = 9e32f;
		mMinMaxDepth[1] = -9e32f;
		mMinMaxTime[0] = 9e32f;
		mMinMaxTime[1] = -9e32f;
		for (int i=0; i<this.size(); i++) {
			double[] mm = ((PointerCollection)this.elementAt(i)).getMinMaxLat();
			mMinMaxLat[0] = mm[0] < mMinMaxLat[0] ? mm[0] : mMinMaxLat[0];
			mMinMaxLat[1] = mm[1] > mMinMaxLat[1] ? mm[1] : mMinMaxLat[1];
			mm = ((PointerCollection)this.elementAt(i)).getMinMaxLon();
			mMinMaxLon[0] = mm[0] < mMinMaxLon[0] ? mm[0] : mMinMaxLon[0];
			mMinMaxLon[1] = mm[1] > mMinMaxLon[1] ? mm[1] : mMinMaxLon[1];
			mm = ((PointerCollection)this.elementAt(i)).getMinMaxDepth();
			mMinMaxDepth[0] = mm[0] < mMinMaxDepth[0] ? mm[0] : mMinMaxDepth[0];
			mMinMaxDepth[1] = mm[1] > mMinMaxDepth[1] ? mm[1] : mMinMaxDepth[1];
			mm = ((PointerCollection)this.elementAt(i)).getMinMaxTime();
			mMinMaxTime[0] = mm[0] < mMinMaxTime[0] ? mm[0] : mMinMaxTime[0];
			mMinMaxTime[1] = mm[1] > mMinMaxTime[1] ? mm[1] : mMinMaxTime[1];
		}
		
		mMinLat = 9e32f;
		mMaxLat = -9e32f;
		mMinLon = 9e32f;
		mMaxLon = -9e32f;
		mMinTime = 9e32f;
		mMaxTime = -9e32f;
		mMinDepth = 9e32f;
		mMaxDepth = -9e32f;
		mMinWLon = 9000;
		mMaxWLon = -9000;
		mMinELon = 9000;
		mMaxELon = -9000;
		for (int i=0; i<this.size(); i++) {
			double l = ((PointerCollection)this.elementAt(i)).getMinLat();
			mMinLat = l < mMinLat ? l : mMinLat;
			l = ((PointerCollection)this.elementAt(i)).getMaxLat();
			mMaxLat = l > mMaxLat ? l : mMaxLat;
			l = ((PointerCollection)this.elementAt(i)).getMinLon();
			mMinLon = l < mMinLon ? l : mMinLon;
			l = ((PointerCollection)this.elementAt(i)).getMaxLon();
			mMaxLon = l > mMaxLon ? l : mMaxLon;
			l = ((PointerCollection)this.elementAt(i)).getMinTime();
			mMinTime = l < mMinTime ? l : mMinTime;
			l = ((PointerCollection)this.elementAt(i)).getMaxTime();
			mMaxTime = l > mMaxTime ? l : mMaxTime;
			l = ((PointerCollection)this.elementAt(i)).getMinDepth();
			mMinDepth = l < mMinDepth ? l : mMinDepth;
			l = ((PointerCollection)this.elementAt(i)).getMaxDepth();
			mMaxDepth = l > mMaxDepth ? l : mMaxDepth;
			l = ((PointerCollection)this.elementAt(i)).getMinWLon();
			mMinWLon = l < mMinWLon ? l : mMinWLon;
			l = ((PointerCollection)this.elementAt(i)).getMaxWLon();
			mMaxWLon = l > mMaxWLon ? l : mMaxWLon;
			l = ((PointerCollection)this.elementAt(i)).getMinELon();
			mMinELon = l < mMinELon ? l : mMinELon;
			l = ((PointerCollection)this.elementAt(i)).getMaxELon();
			mMaxELon = l > mMaxELon ? l : mMaxELon;
		}
		
		// need to deal with selections for existing elements
		// right now just turn off all deletions and selections
		mIsDeleted = new int[mSize];
		unDeleteAll();
		mIsSelected = new boolean[mSize];
		unselectAll();
		mIsSelectable = new boolean[mSize];
		
		// build the sorted arrays
		sortTime();
		sortDepth();
		sortLat();
		sortLon();
		
		// is this the first element?
		if (this.size() == 1) {
			for (int i=0; i<mSize; i++)
				mIsSelectable[i] = true;
		}
		else {
		
		}
	}
	
	public void dumpPointerCollectionGroup() {
		System.out.println("mSize = " + mSize);
		System.out.println("mMinELon = " + mMinELon);
		System.out.println("mMaxELon = " + mMaxELon);
		System.out.println("mMaxWLon = " + mMaxWLon);	
		System.out.println("mMinWLon = " + mMinWLon);	
		System.out.println("mMaxDepth = " + mMaxDepth);	
		System.out.println("mMinDepth = " + mMinDepth);
		System.out.println("mMaxTime = " + mMaxTime);
		System.out.println("mMinTime = " + mMinTime);
		System.out.println("mMinLon = " + mMinLon);
		System.out.println("mMaxLon = " + mMaxLon);
		System.out.println("mMinLat = " + mMinLat);
		System.out.println("MinTime = " + mMinMaxTime[0] + " MaxTime = " + mMinMaxTime[1]);
		System.out.println("MinDepth = " + mMinMaxDepth[0] + " MaxDepth = " + mMinMaxDepth[1]);
		System.out.println("MinLon = " + mMinMaxLon[0] + " MaxLon = " + mMinMaxLon[1]);
		System.out.println("MinLat = " + mMinMaxLat[0] + " MaxLat = " + mMinMaxLat[1]);
	}
	
	private void sortTime() {
		mTimeArr1Sorted = new double[mSize];
		mTimeArr1SortedIndices = new int[mSize];
		for (int i=0; i<mSize; i++) {
			mTimeArr1Sorted[i] = mTimeArr1[i];
			mTimeArr1SortedIndices[i] = i;
		}
		try {
			QSortAlgorithm.sort(mTimeArr1Sorted, mTimeArr1SortedIndices);
		} 
		catch (Exception e) {
			System.out.println("ERROR: QSort failure in ortTime");
		}
	}
	
	private void sortDepth() {
		mDepthArr1Sorted = new double[mSize];
		mDepthArr1SortedIndices = new int[mSize];
		for (int i=0; i<mSize; i++) {
			mDepthArr1Sorted[i] = mDepthArr1[i];
			mDepthArr1SortedIndices[i] = i;
		}
		try {
			QSortAlgorithm.sort(mDepthArr1Sorted, mDepthArr1SortedIndices);
		} 
		catch (Exception e) {
			System.out.println("ERROR: QSort failure in sortDepth");
		}
	}
	
	private void sortLat() {
		mLatArr1Sorted = new double[mSize];
		mLatArr1SortedIndices = new int[mSize];
		for (int i=0; i<mSize; i++) {
			mLatArr1Sorted[i] = mLatArr1[i];
			mLatArr1SortedIndices[i] = i;
		}
		try {
			QSortAlgorithm.sort(mLatArr1Sorted, mLatArr1SortedIndices);
		} 
		catch (Exception e) {
			System.out.println("ERROR: QSort failure in sortLat");
		}
	}
	
	private void sortLon() {
		mLonArr1Sorted = new double[mSize];
		mLonArr1SortedIndices = new int[mSize];
		for (int i=0; i<mSize; i++) {
			mLonArr1Sorted[i] = mLonArr1[i];
			mLonArr1SortedIndices[i] = i;
		}
		try {
			QSortAlgorithm.sort(mLonArr1Sorted, mLonArr1SortedIndices);
		} 
		catch (Exception e) {
			System.out.println("ERROR: QSort failure in sortLon");
		}
	}
	
	// these routines check whether all the lat axes of all pointer collections
	// are scaler--if not storage for vectors is allocated for all pointer collections
	public boolean isLatScaler() {
		for (int i=0; i<this.size(); i++) {
			boolean s = ((PointerCollection)this.elementAt(i)).isLatScaler();
			if (!s)
				return false;
		}
		return true;
	}
	
	public boolean isLonScaler() {
		for (int i=0; i<this.size(); i++) {
			boolean s = ((PointerCollection)this.elementAt(i)).isLonScaler();
			if (!s)
				return false;
		}
		return true;
	}
	
	public boolean isDepthScaler() {
		for (int i=0; i<this.size(); i++) {
			boolean s = ((PointerCollection)this.elementAt(i)).isDepthScaler();
			if (!s)
				return false;
		}
		return true;
	}
	
	public boolean isTimeScaler() {
		for (int i=0; i<this.size(); i++) {
			boolean s = ((PointerCollection)this.elementAt(i)).isTimeScaler();
			if (!s)
				return false;
		}
		return true;
	}
	
	public int vPtToPC(int v) {
		int c = 0;
		for (int i=0; i<this.size() - 1; i++) {
			int start = mRangeStarts[i];
			int end = mRangeStarts[i + 1];
			if (v >= start && v < end)
				break;
			c++;
		}
		return c;
	}

	public boolean isLatScaler(int v) {
		if (mLatIsScaler)
			return true;
		return ((PointerCollection)this.elementAt(vPtToPC(v))).isLatScaler();
	}
	
	public boolean isLonScaler(int v) {
		if (mLonIsScaler)
			return true;
		return ((PointerCollection)this.elementAt(vPtToPC(v))).isLonScaler();
	}
	
	public boolean isDepthScaler(int v) {
		if (mDepthIsScaler)
			return true;
		return ((PointerCollection)this.elementAt(vPtToPC(v))).isDepthScaler();
	}
	
	public boolean isTimeScaler(int v) {
		if (mTimeIsScaler)
			return true;
		return ((PointerCollection)this.elementAt(vPtToPC(v))).isTimeScaler();
	}
	
	// get the size
	public int getSize() {
		return mSize;
	}
		
	public double[] getTimeArr1() {
		return mTimeArr1;
	}
	
	public double[] getTimeArr2() {
		return mTimeArr2;
	}
		
	public double[] getDepthArr1() {
		return mDepthArr1;
	}
	
	public double[] getDepthArr2() {
		return mDepthArr2;
	}
	
	public double[] getLonArr1() {
		return mLonArr1;
	}
	
	public double[] getLonArr2() {
		return mLonArr2;
	}
	
	public double[] getLatArr1() {
		return mLatArr1;
	}
	
	public double[] getLatArr2() {
		return mLatArr2;
	}
	
	public String[] getFileNameArr() {
		return null;
	}
	
	public String[] getPathArr() {
		return null;
	}
	
	public String[] getDataTypeArr() {
		return null;
	}
	
	public String[] getCastArr() {
		return null;
	}
	
	public String[] getCruiseArr() {
		return null;
	}
	
	public double[] getDeltaArr() {
		return null;
	}

	public double getMinWLon() {
		return mMinWLon;
	}
	
	public double getMaxWLon() {
		return mMaxWLon;
	}
	
	public double getMinELon() {
		return mMinELon;
	}
	
	public double getMaxELon() {
		return mMaxELon;
	}
	
	public double getLat1(int i) {
		return mLatArr1[i];
	}
	
	public double getLat2(int i) {
		return mLatArr2[i];
	}
	
	public double getLon1(int i) {
		return mLonArr1[i];
	}
	
	public double getLon2(int i) {
		return mLonArr2[i];
	}
	
	public double getZ1(int i) {
		return mDepthArr1[i];
	}
	
	public double getZ2(int i) {
		return  mDepthArr2[i];
	}
	
	public double getT1(int i) {
		return  mTimeArr1[i];
	}
	
	public double getT2(int i) {
		return mTimeArr2[i];
	}
	
	public double getMinLon() {
		return mMinLon;
	}
	
	public double getMaxLon() {
		return mMaxLon;
	}
	
	public double getMinLat() {
		return mMinLat;
	}
	
	public double getMaxLat() {
		return mMaxLat;
	}
	
	public double getMinTime() {
		return mMinTime;
	}
	
	public double getMaxTime() {
		return mMaxTime;
	}
	
	public double getMinDepth() {
		return mMinDepth;
	}
	
	public double getMaxDepth() {
		return mMaxDepth;
	}

	public double[] getMinMaxLat() {
		return mMinMaxLat;
	}
	
	public double[] getMinMaxLon() {
		return mMinMaxLon;
	}
	
	public double[] getMinMaxDepth() {
		return mMinMaxDepth;
	}
	
	public double[] getMinMaxTime() {
		return mMinMaxTime;
	}
	
	public double[] getTimeArr1Sorted() {
		return mTimeArr1Sorted;//((PointerCollection)this.elementAt(0)).getTimeArr1Sorted();
	}
	
	public int[] getTimeArr1SortedIndices() {
		return mTimeArr1SortedIndices;//((PointerCollection)this.elementAt(0)).getTimeArr1SortedIndices();
	}
	
	public double[] getDepthArr1Sorted() {
		return mDepthArr1Sorted;//((PointerCollection)this.elementAt(0)).getDepthArr1Sorted();
	}
	
	public int[] getDepthArr1SortedIndices() {
		return mDepthArr1SortedIndices; //((PointerCollection)this.elementAt(0)).getDepthArr1SortedIndices();
	}
	
	public double[] getLonArr1Sorted() {
		return mLonArr1Sorted; //PointerCollection)this.elementAt(0)).getLonArr1Sorted();
	}
	
	public int[] getLonArr1SortedIndices() {
		return mLonArr1SortedIndices; //((PointerCollection)this.elementAt(0)).getLonArr1SortedIndices();
	}
	
	public double[] getLatArr1Sorted() {
		return mLatArr1Sorted; //((PointerCollection)this.elementAt(0)).getLatArr1Sorted();
	}
	
	public int[] getLatArr1SortedIndices() {
		return mLatArr1SortedIndices; //((PointerCollection)this.elementAt(0)).getLatArr1SortedIndices();
	}
	
	public boolean isSomethingSelected() {
		if (this.size() == 0)
			return false;
		for (int i=0; i<mSize; i++) {
			if (mIsSelected[i])
				return true;
		}
		return false;
	}
	
	public long[] getMonthArr1Sorted() {
		return null;
	}
	
	public int[] getMonthArr1SortedIndices() {
		return null;
	}
	
	public void resetSizes() {
		for (int i=0; i<this.size(); i++) {
			((PointerCollection)this.elementAt(i)).resetSizes();
		}
	}
	
	public void unDeleteAll() {
		for (int i=0; i<mSize; i++) {
			mIsDeleted[i] = 0;
		}
		for (int i=0; i<this.size(); i++) {
			((PointerCollection)this.elementAt(i)).unDeleteAll();
		}
	}
	
	public void unselectAll() {
		for (int i=0; i<mSize; i++) {
			mIsSelected[i] = false;
		}
		for (int i=0; i<this.size(); i++) {
			((PointerCollection)this.elementAt(i)).unselectAll();
		}
	}
	
	public int getCurrDeletionIndex() {
		return ((PointerCollection)this.elementAt(0)).getCurrDeletionIndex();
	}
	
	public void setCurrDeletionIndex(int delOrd) {
		for (int i=0; i<this.size(); i++) {
			((PointerCollection)this.elementAt(i)).setCurrDeletionIndex(delOrd);
		}
	}
	
	public boolean crosses180() {
		boolean result = true;
		for (int i=0; i<this.size(); i++) {
			result = result && ((PointerCollection)this.elementAt(i)).crosses180();
		}
		return result;
	}
	
	public void delete(int i, int delOrd) {
		mIsDeleted[i] = delOrd;
		
		//set deletion in underlying pointercollection
		((PointerCollection)this.elementAt(this.getPCIndex(i))).delete(getPCElemIndex(i), delOrd);
	}
		
	public boolean isSelected(int i) {
		return mIsSelected[i];
	}
	
	public boolean isDeleted(int i) {
		return mIsDeleted[i] != 0;
	}
	
	public void select(int i) {
		mIsSelected[i] = true;
		
		//set selection in underlying pointercollection
		((PointerCollection)this.elementAt(this.getPCIndex(i))).select(getPCElemIndex(i));
	}
	
	public String getString(int i) {
		int whichPC = this.getPCIndex(i);
		PointerCollection pc = (PointerCollection)this.elementAt(whichPC);
		int whichPCElem = this.getPCElemIndex(i);
		return pc.getString(whichPCElem);
	}
	
	public int[] getIsDeletedArr() {
		return mIsDeleted;
	}
	
	public boolean[] getIsSelectedArr() {
		return mIsSelected;
	}
	
	public int getPCIndex(int indx) {
		for (int i=0; i<this.size(); i++) {
			if (indx >= mRangeStarts[i] && indx < mRangeEnds[i]) {
				return i;
			}
		}
		return 0;
	}
	
	public int getPCElemIndex(int indx) {
		int sumIndices = 0;
		for (int i=0; i<this.size(); i++) {
			if (indx >= mRangeStarts[i] && indx < mRangeEnds[i]) {
				// it is the ith PC--subtract out the indices for the PC's before this
				return indx - sumIndices;
			}
			sumIndices += ((PointerCollection)this.elementAt(i)).getSize();
		}
		return 0;
	}
	
	public Color getColor(int indx) {
		// return a different color for each pointer collection
		for (int i=0; i<this.size(); i++) {
			if (indx >= mRangeStarts[i] && indx < mRangeEnds[i])
				return mLayerColors[i];
		}
		return Color.black;
	}
	
	public void setBatchModeOn() {
		for (int i=0; i<this.size(); i++) {
			((PointerCollection)this.elementAt(i)).setBatchModeOn();
		}
	}

	public void setBatchModeOff() {
		for (int i=0; i<this.size(); i++) {
			((PointerCollection)this.elementAt(i)).setBatchModeOff();
			((PointerCollection)this.elementAt(i)).updateStats();
		}
	}
	
	public boolean isBatchMode() {
		for (int i=0; i<this.size(); i++) {
			if (((PointerCollection)this.elementAt(i)).isBatchMode())
				return true;
		}
		return false;
	}
	
	public int getNumPtrs() {
		int sum = 0;
		for (int i=0; i<this.size(); i++) {
			sum += ((PointerCollection)this.elementAt(i)).getNumPtrs();
		}
		return sum;
	}
	
	public int getNumVisiblePtrs() {
		int sum = 0;
		for (int i=0; i<this.size(); i++) {
			sum += ((PointerCollection)this.elementAt(i)).getNumVisiblePtrs();
		}
		return sum;
	}
	
	public int getNumSelectedPtrs() {
		int sum = 0;
		for (int i=0; i<this.size(); i++) {
			sum += ((PointerCollection)this.elementAt(i)).getNumSelectedPtrs();
		}
		return sum;
	}
	
	public boolean isSelectedLayer(int indx) {
		return mIsSelectable[indx];
	}
	
	public void clearSelectedLayer() {
		for (int i=0; i<this.getSize(); i++)
			mIsSelectable[i] = false;
	}
	
	public boolean isSelectedLayer(PointerCollection ipc) {
		// find the index of this pc in the group
		int foundPC = -99;
		for (int i=0; i<this.size(); i++) {
			PointerCollection pc = (PointerCollection)this.elementAt(i);
			if (ipc == pc) {
				foundPC = i;
				break;
			}
		}
			
		if (foundPC >= 0) {
			return mIsSelectable[mRangeStarts[foundPC]];
		}
		else
			return true;
	}

	public void setSelectedLayer(PointerCollection ipc) {
		// find the index of this pc in the group
		int foundPC = -99;
		for (int i=0; i<this.size(); i++) {
			PointerCollection pc = (PointerCollection)this.elementAt(i);
			if (ipc == pc) {
				foundPC = i;
				break;
			}
		}
		
		if (foundPC >= 0) {
			for (int i=mRangeStarts[foundPC]; i<mRangeEnds[foundPC]; i++) {
				mIsSelectable[i] = true;
			}
		}
		else {
			for (int i=0; i<this.getSize(); i++)
				mIsSelectable[i] = true;
		}
	}
}

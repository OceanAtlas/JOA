/*
 * $Id: GridPointerCollection.java,v 1.27 2005/06/27 23:25:28 oz Exp $
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
import ucar.nc2.Variable;
import java.beans.*;

/**
 * @author oz
 */
public class GridPointerCollection implements PointerCollection {
	private String title;
	private String additionalField_Ids;
	private double[] latArr1;
	private double[] latArrAllVals = null;
	private double[] latArr2;
	private double[] lonArr1;
	private double[] lonArrAllVals = null;
	private double[] lonArr2;
	private double[] depthArr1;
	private double[] depthAxis;
	private double[] depthArrAllVals = null;
	private double[] depthArr2;
	private double[] depthArr2AllVals = null;
	private double[] timeArr1;
	private double[] timeArrAllVals = null;
	private double[] timeArr2;
	private double[] timeArr2AllVals = null;
	private double[] minMaxDepth;
	private double[] minMaxLat = null;
	private double[] minMaxLon = null;
	private double[] minMaxELon = null;
	private double[] minMaxWLon = null;
	private double[] minMaxTime;
	// Arrays can be sorted if needed
	private double[] latArr1Sorted;
	private double[] lonArr1Sorted;
	private double[] depthArr1Sorted;
	private double[] timeArr1Sorted;
	private long[] monthArr1Sorted;
	private int[] latArr1SortedIndices;
	private int[] lonArr1SortedIndices;
	private int[] depthArr1SortedIndices;
	private int[] timeArr1SortedIndices;
	private int[] timeArr2SortedIndices;
	private int[] monthArr1SortedIndices;
	private Boolean mCrossed180 = null;
	private int[] isDeleted;
	private boolean[] mIsSelected;
	private int currDeletionLevel = 0;
	private Object[] mReferences;
	private Class mRefType;
	int numLats;
	int numLons;
	int numZs;
	int numTs;
	int numPoints;
	int nLatxnLon;
	int nLatxnLonxnZ;
	int nLatxnLonxnZxnT;
	Vector mValArrays = new Vector(); // this will contain references to the value
																		// arrays
	Vector mVars = new Vector();
	Vector mArrays = new Vector();
	Vector mAttributes = new Vector();
	boolean mapMissingValue = false;
	double missingValue = -99.0;
	boolean mBatchMode = false;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private int mVisible = 0;
	private int mSelected = 0;
	private boolean mIsClimatology = false;
	private int mClimatologyLength = 0;
	private boolean[] mOrigShape = new boolean[4];
	private boolean[] mAddedAxes = new boolean[4];
	int origNumZs;
	Double[] keys;
	private int mTPos;
	private int mZPos;
	private int mLatPos;
	private int mLonPos;

	public GridPointerCollection() {
	}

	public GridPointerCollection(double[] latArr, double[] lonArr, double[] depthArr, double[] timeArr,
	    boolean vectorizeZ, double zmin, double zmax, boolean isclimatology, int clen, boolean[] origshape,
	    boolean[] addedaxes, Vector vals, Vector attrs, int tPos, int zPos, int ltPos, int lnPos) {
		mTPos = tPos;
		mZPos = zPos;
		mLatPos = ltPos;
		mLonPos = lnPos;

		mIsClimatology = isclimatology;
		mClimatologyLength = clen;
		for (int i = 0; i < 4; i++) {
			mOrigShape[i] = origshape[i];
			mAddedAxes[i] = addedaxes[i];

		}
		numLats = latArr.length;
		numLons = lonArr.length;
		numZs = depthArr.length;
		depthAxis = depthArr;
		origNumZs = numZs;
		numTs = timeArr.length;
		int origNumTs = numTs;

		if (mIsClimatology) {
			this.timeArr2 = new double[numTs];
			for (int i = 0; i < numTs; i++) {
				GeoDate st = new GeoDate((long)timeArr[i]);
				GeoDate end = new GeoDate(st.increment((double)mClimatologyLength, GeoDate.YEARS));
				timeArr2[i] = (double)end.getTime();
			}
			numTs *= 2;
		}

		if (vectorizeZ) {
			this.depthArr1 = new double[1];
			this.depthArr1[0] = zmin;
			this.depthArr2 = new double[1];
			this.depthArr2[0] = zmax;
			numZs = 1;

		}
		else {
			this.depthArr1 = depthArr;
			this.depthArr2 = depthArr;
		}

		nLatxnLon = numLons * numLats;
		nLatxnLonxnZ = nLatxnLon * numZs;
		nLatxnLonxnZxnT = nLatxnLonxnZ * numTs;
		numPoints = nLatxnLonxnZxnT;

		this.latArr1 = latArr;
		this.latArr2 = latArr;
		this.lonArr1 = lonArr;
		this.lonArr2 = lonArr;
		this.timeArr1 = timeArr;
		isDeleted = new int[numPoints];
		keys = new Double[numPoints];
		mIsSelected = new boolean[numPoints];

		// initialize the all value arrays
		lonArrAllVals = new double[numPoints];
		latArrAllVals = new double[numPoints];
		depthArrAllVals = new double[numPoints];
		timeArrAllVals = new double[numPoints];
		if (mIsClimatology)
			timeArr2AllVals = new double[numPoints];
		depthArr2AllVals = new double[numPoints];

		int c = 0;
		for (int i = 0; i < numPoints / numLons; i++) {
			for (int ln = 0; ln < numLons; ln++) {
				lonArrAllVals[c++] = lonArr1[ln];
			}
		}

		c = 0;
		for (int t = 0; t < numTs; t++) {
			for (int z = 0; z < numZs; z++) {
				for (int lt = 0; lt < numLats; lt++) {
					for (int ln = 0; ln < numLons; ln++) {
						latArrAllVals[c++] = latArr1[lt];
					}
				}
			}
		}

		c = 0;
		for (int t = 0; t < numTs; t++) {
			// for (int z=0; z<numZs; z++) {
			for (int l = 0; l < nLatxnLon; l++) {
				depthArrAllVals[c] = zmin;
				depthArr2AllVals[c++] = zmax;
			}
			// }
		}

		c = 0;
		for (int t = 0; t < origNumTs; t++) {
			for (int l = 0; l < numLats * numLons * numZs; l++) {
				timeArrAllVals[c] = timeArr1[t];
				if (mIsClimatology)
					timeArr2AllVals[c] = timeArr2[t];
				c++;
			}
		}

		for (int i = 0; i < attrs.size(); i++) {
			mAttributes.addElement(attrs.elementAt(i));
		}

		int np = nLatxnLon * origNumZs * origNumTs;
		for (int i = 0; i < vals.size(); i++) {
			// get the ith variable
			Variable vv = (Variable)vals.elementAt(i);
			mVars.addElement(vv);

			// create an array object
			float[] varVals = new float[np];
			try {
				ucar.ma2.Array arry = vv.read();
				int[] shape = vv.getShape();
				mArrays.addElement(arry);
				// need to store the name and units for the measured variable
				mValArrays.addElement(varVals);
			}
			catch (Exception ex) {

			}
		}

		for (int i = 0; i < this.getSize(); i++) {
			// need to get all the values of the variables at each depth
			double lat = this.getLat1(i);
			double lon = this.getLon1(i);
			double t = this.getT1(i); // what if time is a vector?
			double t2 = 0;
			if (this.getTimeArr2() != null)
				t2 = this.getT2(i); // what if time is a vector?

			// z is ignored--we want all the z values
			double z = this.getZ1(i);

			// create the key object
			Double key = null;
			key = new Double(Math.random());
			/*
			 * if (Math.abs(t) > 0.0 && Math.abs(lat) > 0.0 && Math.abs(lon) > 0.0) {
			 * key = new Double(Math.random((t/lat/lon))); } else if (t == 0.0 &&
			 * Math.abs(lat) > 0.0 && Math.abs(lon) > 0.0) { key = new
			 * Double(Math.random(lat/lon)); } else if (Math.abs(t) > 0.0 &&
			 * Math.abs(lat) == 0.0 && Math.abs(lon) > 0.0) { key = new
			 * Double(Math.random(t/lon)); } else if (Math.abs(t) > 0.0 &&
			 * Math.abs(lat) > 0.0 && Math.abs(lon) == 0.0) { key = new
			 * Double(Math.random(t/lat)); }
			 */

			if (key == null) {
				System.out.println("lat = " + lat);
				System.out.println("lon = " + lon);
				System.out.println("t = " + t);
				keys[i] = new Double(Double.NaN);
			}
			else {
				keys[i] = key;
			}

		}

		// get the missing value
		ucar.nc2.Variable vv = (ucar.nc2.Variable)mVars.elementAt(0);
		Iterator ai = vv.getAttributeIterator();
		while (ai.hasNext()) {
			ucar.nc2.Attribute at = (ucar.nc2.Attribute)ai.next();
			String name = at.getName().toLowerCase();
			if (name.indexOf("missing") >= 0) {
				// found a missing value attribute--get the value of the attribute
				String type = at.getValueType().getName();
				if (type.equalsIgnoreCase("short")) {
					short mv = ((Short)at.getNumericValue()).shortValue();
					missingValue = (float)mv;
					mapMissingValue = true;
				}
				else if (type.equalsIgnoreCase("int")) {
					int mv = ((Integer)at.getNumericValue()).intValue();
					missingValue = (float)mv;
					mapMissingValue = true;
				}
				else if (type.equalsIgnoreCase("long")) {
					int mv = ((Integer)at.getNumericValue()).intValue();
					missingValue = (float)mv;
					mapMissingValue = true;
				}
				else if (type.equalsIgnoreCase("float")) {
					missingValue = (double)(((Float)at.getNumericValue()).floatValue());
					mapMissingValue = true;
				}
				else if (type.equalsIgnoreCase("double")) {
					double mv = ((Double)at.getNumericValue()).doubleValue();
					mapMissingValue = true;
					missingValue = (double)mv;
				}
			}
		}
		if (missingValue > 1E20 && missingValue < 1.1E20)
			missingValue = 1E20;

	}

	public Vector getAttributes() {
		return mAttributes;
	}

	public int getNumMeasuredVars() {
		return mValArrays.size();
	}

	public Variable getMeasuredVar(int v) {
		return (Variable)mVars.elementAt(v);
	}

	public double getMeasuredVal(int i, int v) {
		double[] fa = (double[])mValArrays.elementAt(v);
		if (mapMissingValue && fa[i] == missingValue)
			return -99f;
		else
			return fa[i];
	}

	public double[][] getMeasuredVals(double lat, double lon, double time) {
		double[][] ret = new double[origNumZs][mVars.size()];
		// find the index of the lat and lon values
		int ltIndx = -99;
		for (int i = 0; i < latArr1.length; i++) {
			if (latArr1[i] == lat) {
				ltIndx = i;
				break;
			}
		}

		int lnIndx = -99;
		for (int i = 0; i < lonArr1.length; i++) {
			if (lonArr1[i] == lon) {
				lnIndx = i;
				break;
			}
		}

		int timeIndx = -99;
		for (int i = 0; i < timeArr1.length; i++) {
			if (timeArr1[i] == time) {
				timeIndx = i;
				break;
			}
		}

		int xIndx = lnIndx;
		int yIndx = ltIndx;

		// test whether the lat and lon axes are reversed
		if (this.getLatPos() == 2 && this.getLonPos() == 1) {
			 xIndx = ltIndx;
			 yIndx = lnIndx;
		}

		for (int v = 0; v < mVars.size(); v++) {
			Variable vv = (Variable)mVars.elementAt(v);
			try {
				ucar.ma2.Array arry = (ucar.ma2.Array)mArrays.elementAt(v);

				boolean arrayIsFloat = arry instanceof ucar.ma2.ArrayFloat;
				boolean arrayIsDouble = arry instanceof ucar.ma2.ArrayDouble;

				int[] shape = vv.getShape();
				double val = -99;

				for (int z = 0; z < origNumZs; z++) {
					if (shape.length == 4) {
						// 4 dimensions to variable
						if (arrayIsFloat) {
							val = ((ucar.ma2.ArrayFloat.D4)arry).get(timeIndx, z, yIndx, xIndx);
						}
						else if (arrayIsDouble) {
							val = ((ucar.ma2.ArrayDouble.D4)arry).get(timeIndx, z, yIndx, xIndx);
						}
						if (mapMissingValue && val == missingValue)
							ret[z][v] = -99;
						else
							ret[z][v] = val;
					}
					else if (shape.length == 3) {
						// 3 dimensions to variable
						if (!mOrigShape[3]) {
							// missing the time axis
							if (arrayIsFloat) {
								val = ((ucar.ma2.ArrayFloat.D3)arry).get(z, yIndx, xIndx);
							}
							else if (arrayIsDouble) {
								val = ((ucar.ma2.ArrayDouble.D3)arry).get(z, yIndx, xIndx);
							}
							if (mapMissingValue && val == missingValue)
								ret[z][v] = -99;
							else
								ret[z][v] = val;
						}
						else if (!mOrigShape[2]) {
							// missing the z axis
							if (arrayIsFloat) {
								val = ((ucar.ma2.ArrayFloat.D3)arry).get(timeIndx, yIndx, xIndx);
							}
							else if (arrayIsDouble) {
								val = ((ucar.ma2.ArrayDouble.D3)arry).get(timeIndx, yIndx, xIndx);
							}
							if (mapMissingValue && val == missingValue)
								ret[z][v] = -99;
							else
								ret[z][v] = val;
						}
						else if (!mOrigShape[1]) {
							// missing the lat axis
							if (arrayIsFloat) {
								val = ((ucar.ma2.ArrayFloat.D3)arry).get(timeIndx, z, xIndx);
							}
							else if (arrayIsDouble) {
								val = ((ucar.ma2.ArrayDouble.D3)arry).get(timeIndx, z, xIndx);
							}
							if (mapMissingValue && val == missingValue)
								ret[z][v] = -99;
							else
								ret[z][v] = val;
						}
						else if (!mOrigShape[0]) {
							// missing the lon axis
							if (arrayIsFloat) {
								val = ((ucar.ma2.ArrayFloat.D3)arry).get(timeIndx, z, yIndx);
							}
							else if (arrayIsDouble) {
								val = ((ucar.ma2.ArrayDouble.D3)arry).get(timeIndx, z, yIndx);
							}
							if (mapMissingValue && val == missingValue)
								ret[z][v] = -99;
							else
								ret[z][v] = val;
						}
					}
				}
			}
			catch (Exception ex) {

			}
		}

		return ret;
	}

	public GridPointerCollection(GridPointerCollection pc) {
	}

	public GridPointerCollection getFilteredPointerCollection() {
		GridPointerCollection pc = new GridPointerCollection();
		int size = this.getSize();
		int c = 0;
		for (int i=0; i<size; i++) {
			if (this.isDeleted[i] == 0)
				c++;
		}
		
		if (c == 0)
			return null;
		
		pc.latArr1 = new double[latArr1.length];
		pc.latArr2 = new double[latArr1.length];
		pc.lonArr1 = new double[lonArr1.length];
		pc.lonArr2 = new double[lonArr1.length];
		pc.depthArr1 = new double[depthArr1.length];
		pc.depthArr2 = new double[depthArr1.length];
		pc.timeArr1 = new double[timeArr1.length];
		pc.timeArr2 = new double[timeArr1.length];
		pc.isDeleted = new int[numPoints];
		pc.mIsSelected = new boolean[numPoints];
		
		c = 0;
		for (int i=0; i<numPoints; i++) {
			if (this.isDeleted[i] == 0) {
				pc.latArr1[c] = this.latArr1[i];
				pc.latArr2[c] = this.latArr2[i];
				pc.lonArr1[c] = this.lonArr1[i];
				pc.lonArr2[c] = this.lonArr2[i];
				pc.depthArr1[c] = this.depthArr1[i];
				pc.depthArr2[c] = this.depthArr2[i];
				pc.timeArr1[c] = this.timeArr1[i];
				pc.timeArr2[c] = this.timeArr2[i];
				pc.isDeleted[c] = this.isDeleted[i];
				pc.mIsSelected[c] = this.mIsSelected[i];
				c++;
			}
		}
		pc.setPCTitle(this.getPCTitle());

		 pc.setTPos(this.getTPos()) ;
		 pc.setZPos(this.getZPos());
		 pc.setLatPos(this.getLatPos());
		 pc.setLonPos(this.getLonPos());
		return pc;
	}

	public GridPointerCollection getSelectedPointerCollection() {
		GridPointerCollection pc = new GridPointerCollection();
		int size = this.getSize();
		int c = 0;
		for (int i = 0; i < size; i++) {
			if (this.isDeleted[i] == 0 && mIsSelected[i])
				c++;
		}

		if (c == 0)
			return null;

		pc.latArr1 = new double[c];
		pc.latArr2 = new double[c];
		pc.lonArr1 = new double[c];
		pc.lonArr2 = new double[c];
		pc.depthArr1 = new double[c];
		pc.depthArr2 = new double[c];
		pc.timeArr1 = new double[c];
		pc.timeArr2 = new double[c];
		pc.isDeleted = new int[c];
		pc.mIsSelected = new boolean[c];
		pc.mReferences = new Object[c];

		c = 0;
		for (int i = 0; i < size; i++) {
			if (this.isDeleted[i] == 0 && mIsSelected[i]) {
				pc.latArr1[c] = this.latArr1[i];
				pc.latArr2[c] = this.latArr2[i];
				pc.lonArr1[c] = this.lonArr1[i];
				pc.lonArr2[c] = this.lonArr2[i];
				pc.depthArr1[c] = this.depthArr1[i];
				pc.depthArr2[c] = this.depthArr2[i];
				pc.timeArr1[c] = this.timeArr1[i];
				pc.timeArr2[c] = this.timeArr2[i];
				pc.isDeleted[c] = this.isDeleted[i];
				pc.mIsSelected[c] = this.mIsSelected[i];
				pc.mReferences[c] = this.mReferences[i];
				c++;
			}
		}
		pc.setPCTitle(this.getPCTitle());
		return pc;
	}

	public void addPointers(PointerCollection pc) {
	}

	public int getSize() {
		return numPoints;
	}

	public String getPCTitle() {
		return title;
	}

	public void setPCTitle(String t) {
		title = new String(t);
	}

	public void resetSizes() {
		minMaxDepth = null;
		minMaxLat = null;
		minMaxLon = null;
		minMaxTime = null;
		minMaxWLon = null;
		minMaxELon = null;
		mCrossed180 = null;
		mVisible = 0;
		mSelected = 0;
	}

	// public String toString() {
	// return("Num Pointers: " + getSize());
	// }

	public double[] getLatArr1() {
		/*
		 * System.out.println("getLatArr1"); // this has to return ALL the latitudes
		 * in the whole grid if (latArrAllVals == null) { latArrAllVals = new
		 * double[numPoints]; for (int i=0; i< numPoints; i++) { latArrAllVals[i] =
		 * this.getLat1(i); } }
		 */
		return latArrAllVals;
	}

	public double[] getLatArr2() {
		return null;
	}

	public double[] getLonArr1() {
		/*
		 * System.out.println("getLonArr1"); // this has to return ALL the latitudes
		 * in the whole grid if (lonArrAllVals == null) { lonArrAllVals = new
		 * double[numPoints]; for (int i=0; i< numPoints; i++) lonArrAllVals[i] =
		 * this.getLon1(i); }
		 */
		return lonArrAllVals;
	}

	public double[] getLonArr2() {
		return null;
	}

	public double[] getDepthArr1() {
		return depthArrAllVals;
	}

	public double[] getDepthArr2() {
		return depthArr2AllVals;
	}

	public double[] getDepthAxis() {
		return depthAxis;
	}

	public double[] getTimeArr1() {
		/*
		 * System.out.println("getTimeArr1"); // this has to return ALL the
		 * latitudes in the whole grid if (timeArrAllVals == null) { timeArrAllVals
		 * = new double[numPoints]; for (int i=0; i< numPoints; i++)
		 * timeArrAllVals[i] = this.getT1(i); }
		 */
		return timeArrAllVals;
	}

	public double[] getTimeArr2() {
		return timeArr2AllVals;
	}

	public boolean isLatScaler() {
		if (getLatArr2() != null)
			return false;
		return true;
	}

	public boolean isLonScaler() {
		if (getLonArr2() != null)
			return false;
		return true;
	}

	public boolean isDepthScaler() {
		if (getDepthArr2() != null)
			return false;
		return true;
	}

	public boolean isTimeScaler() {
		if (getTimeArr2() != null)
			return false;
		return true;
	}

	public int[] getIsDeletedArr() {
		return isDeleted;
	}

	public boolean[] getIsSelectedArr() {
		return mIsSelected;
	}

	public int getNumSelected() {
		int sum = 0;
		for (int i = 0; i < numPoints; i++) {
			if (mIsSelected[i])
				sum++;
		}
		return sum;
	}

	public double getMinLat() {
		double[] m = getMinMaxLat();
		return m[0];
	}

	public double getMaxLat() {
		double[] m = getMinMaxLat();
		return m[1];
	}

	public double[] getMinMaxLat() {
		double mn = 90;
		double mx = -90;
		if (minMaxLat == null) {
			minMaxLat = new double[2];
			if (latArr1 != null) {
				int len = latArr1.length;
				for (int i = 0; i < len; i++) {
					mx = Math.max(latArr1[i], mx);
					mn = Math.min(latArr1[i], mn);
				}
			}
			if (latArr2 != null) {
				int len = latArr1.length;
				for (int i = 0; i < len; i++) {
					mx = Math.max(latArr2[i], mx);
					mn = Math.min(latArr2[i], mn);
				}
			}
			minMaxLat[0] = mn;
			minMaxLat[1] = mx;
		}
		return minMaxLat;
	}

	public double getMinLon() {
		double[] m = getMinMaxLon();
		return m[0];
	}

	public double getMaxLon() {
		double[] m = getMinMaxLon();
		return m[1];
	}

	public double getMinWLon() {
		double[] m = getMinMaxWLon();
		return m[0];
	}

	public double getMaxWLon() {
		double[] m = getMinMaxWLon();
		return m[1];
	}

	public double getMinELon() {
		double[] m = getMinMaxELon();
		return m[0];
	}

	public double getMaxELon() {
		double[] m = getMinMaxELon();
		return m[1];
	}

	public double[] getMinMaxLon() {
		double mn = 360;
		double mx = -360;
		if (minMaxLon == null) {
			// minMaxLon = null;
			minMaxLon = new double[2];
			if (lonArr1 != null) {
				for (int i = 0; i < lonArr1.length; i++) {
					mx = Math.max(lonArr1[i], mx);
					mn = Math.min(lonArr1[i], mn);
				}
			}
			if (lonArr2 != null) {
				for (int i = 0; i < lonArr2.length; i++) {
					mx = Math.max(lonArr2[i], mx);
					mn = Math.min(lonArr2[i], mn);
				}
			}
			minMaxLon[0] = mn;
			minMaxLon[1] = mx;
		}
		return minMaxLon;
	}

	public double[] getMinMaxWLon() {
		double mn = 0;
		double mx = -180;
		if (minMaxWLon == null) {
			minMaxWLon = null;
			minMaxWLon = new double[2];
			if (lonArr1 != null) {
				for (int i = 0; i < lonArr1.length; i++) {
					if (lonArr1[i] < 0) {
						mx = Math.max(lonArr1[i], mx);
						mn = Math.min(lonArr1[i], mn);
					}
				}
			}
			if (lonArr2 != null) {
				for (int i = 0; i < lonArr2.length; i++) {
					if (lonArr2[i] < 0) {
						mx = Math.max(lonArr2[i], mx);
						mn = Math.min(lonArr2[i], mn);
					}
				}
			}
			minMaxWLon[0] = mn;
			minMaxWLon[1] = mx;
		}
		return minMaxWLon;
	}

	public double[] getMinMaxELon() {
		double mn = 360;
		double mx = 0;
		if (minMaxELon == null) {
			minMaxELon = null;
			minMaxELon = new double[2];
			if (lonArr1 != null) {
				for (int i = 0; i < lonArr1.length; i++) {
					if (lonArr1[i] >= 0) {
						mx = Math.max(lonArr1[i], mx);
						mn = Math.min(lonArr1[i], mn);
					}
				}
			}
			if (lonArr2 != null) {
				for (int i = 0; i < lonArr2.length; i++) {
					if (lonArr2[i] >= 0) {
						mx = Math.max(lonArr2[i], mx);
						mn = Math.min(lonArr2[i], mn);
					}
				}
			}
			minMaxELon[0] = mn;
			minMaxELon[1] = mx;
		}
		return minMaxELon;
	}

	public boolean crosses180() {
		if (mCrossed180 != null)
			return mCrossed180.booleanValue();
		if (lonArr1 != null) {
			for (int i = 1; i < lonArr1.length - 1; i++) {
				if ((lonArr1[i] >= -180 && lonArr1[i] < -178) && (lonArr1[i - 1] >= 178 && lonArr1[i - 1] <= 180)) {
					mCrossed180 = new Boolean(true);
					return true;
				}
				else if ((lonArr1[i] >= 178 && lonArr1[i] <= 180) && (lonArr1[i - 1] >= -180 && lonArr1[i - 1] < -178)) {
					mCrossed180 = new Boolean(true);
					return true;
				}
			}
		}

		if (lonArr2 != null) {
			for (int i = 0; i < lonArr2.length; i++) {
				if ((lonArr2[i] >= -180 && lonArr2[i] < -178) && (lonArr2[i - 1] >= 178 && lonArr2[i - 1] <= 180)) {
					mCrossed180 = new Boolean(true);
					return true;
				}
				else if ((lonArr2[i] >= 178 && lonArr2[i] <= 180) && (lonArr2[i - 1] >= -180 && lonArr2[i - 1] < -178)) {
					mCrossed180 = new Boolean(true);
					return true;
				}
			}
		}
		mCrossed180 = new Boolean(false);
		return false;
	}

	// ---------------------------------------------------------
	//
	public double getMinDepth() {
		double[] m = getMinMaxDepth();
		return m[0];
	}

	// ---------------------------------------------------------
	//
	public double getMaxDepth() {
		double[] m = getMinMaxDepth();
		return m[1];
	}

	// ---------------------------------------------------------
	//
	public double[] getMinMaxDepth() {
		double mn = 10000;
		double mx = -10000;
		if (minMaxDepth == null) {
			minMaxDepth = new double[2];
			if (depthArr1 != null) {
				for (int i = 0; i < depthArr1.length; i++) {
					mx = Math.max(depthArr1[i], mx);
					mn = Math.min(depthArr1[i], mn);
				}
			}
			if (depthArr2 != null) {
				for (int i = 0; i < depthArr2.length; i++) {
					mx = Math.max(depthArr2[i], mx);
					mn = Math.min(depthArr2[i], mn);
				}
			}
			minMaxDepth[0] = mn;
			minMaxDepth[1] = mx;
		}
		return minMaxDepth;
	}

	// ---------------------------------------------------------
	//
	public double getMinTime() {
		double[] m = getMinMaxTime();
		return m[0];
	}

	// ---------------------------------------------------------
	//
	public double getMaxTime() {
		double[] m = getMinMaxTime();
		return m[1];
	}

	// ---------------------------------------------------------
	public double[] getMinMaxTime() {
		double mn = 0;
		double mx = 0;
		if (minMaxTime == null) {
			minMaxTime = new double[2];
			if (timeArr1 != null) {
				mn = timeArr1[0];
				mx = timeArr1[0];
				for (int i = 0; i < timeArr1.length; i++) {
					mx = Math.max(timeArr1[i], mx);
					mn = Math.min(timeArr1[i], mn);
				}
			}
			if (timeArr2 != null) {
				for (int i = 0; i < timeArr2.length; i++) {
					mx = Math.max(timeArr2[i], mx);
					mn = Math.min(timeArr2[i], mn);
				}
			}
			minMaxTime[0] = mn;
			minMaxTime[1] = mx;
		}
		return minMaxTime;
	}

	// -----------------------------------------------------------
	// SORT Section
	// -----------------------------------------------------------
	// ---------------------------------------------------------
	//
	public double[] getLatArr1Sorted() {
		if (latArr1Sorted == null)
			sortLat();
		return latArr1Sorted;
	}

	// ---------------------------------------------------------
	//
	public int[] getLatArr1SortedIndices() {
		if (latArr1Sorted == null)
			sortLat();
		return latArr1SortedIndices;
	}

	// ---------------------------------------------------------
	//
	public void sortLat() {
		latArr1Sorted = new double[numPoints];
		latArr1SortedIndices = new int[numPoints];
		for (int i = 0; i < numPoints; i++) {
			latArr1Sorted[i] = latArrAllVals[i];
			latArr1SortedIndices[i] = i;
		}
		try {
			QSortAlgorithm.sort(latArr1Sorted, latArr1SortedIndices);
		}
		catch (Exception e) {
			System.out.println("ERROR: QSort failure");
		}
	}

	// ---------------------------------------------------------
	//
	public double[] getLonArr1Sorted() {
		if (lonArr1Sorted == null)
			sortLon();
		return lonArr1Sorted;
	}

	// ---------------------------------------------------------
	//
	public int[] getLonArr1SortedIndices() {
		if (lonArr1Sorted == null)
			sortLon();
		return lonArr1SortedIndices;
	}

	// ---------------------------------------------------------
	//
	public void sortLon() {
		lonArr1Sorted = new double[numPoints];
		lonArr1SortedIndices = new int[numPoints];
		getLonArr1();
		for (int i = 0; i < numPoints; i++) {
			lonArr1Sorted[i] = lonArrAllVals[i];
			lonArr1SortedIndices[i] = i;
		}
		try {
			QSortAlgorithm.sort(lonArr1Sorted, lonArr1SortedIndices);
		}
		catch (Exception e) {
			System.out.println("ERROR: QSort failure");
		}
	}

	// ---------------------------------------------------------
	//
	public double[] getDepthArr1Sorted() {
		if (depthArr1Sorted == null)
			sortDepth();
		return depthArr1Sorted;
	}

	// ---------------------------------------------------------
	//
	public int[] getDepthArr1SortedIndices() {
		if (depthArr1Sorted == null)
			sortDepth();
		return depthArr1SortedIndices;
	}

	// ---------------------------------------------------------
	//
	public void sortDepth() {
		depthArr1Sorted = new double[numPoints];
		depthArr1SortedIndices = new int[numPoints];
		getDepthArr1();
		for (int i = 0; i < numPoints; i++) {
			depthArr1Sorted[i] = depthArrAllVals[i];
			depthArr1SortedIndices[i] = i;
		}
		try {
			QSortAlgorithm.sort(depthArr1Sorted, depthArr1SortedIndices);
		}
		catch (Exception e) {
			System.out.println("ERROR: QSort failure");
		}
	}

	public double[] getTimeArr1Sorted() {
		if (timeArr1Sorted == null)
			sortTime();
		return timeArr1Sorted;
	}

	public int[] getTimeArr1SortedIndices() {
		if (timeArr1Sorted == null)
			sortTime();
		return timeArr1SortedIndices;
	}

	public void sortTime() {
		timeArr1Sorted = new double[numPoints];
		timeArr1SortedIndices = new int[numPoints];
		getTimeArr1();
		for (int i = 0; i < numPoints; i++) {
			timeArr1Sorted[i] = timeArrAllVals[i];
			timeArr1SortedIndices[i] = i;
		}
		try {
			QSortAlgorithm.sort(timeArr1Sorted, timeArr1SortedIndices);
		}
		catch (Exception e) {
			System.out.println("ERROR: QSort failure");
		}
	}

	public long[] getMonthArr1Sorted() {
		return null;
	}

	// ---------------------------------------------------------
	//
	public int[] getMonthArr1SortedIndices() {
		return null;
	}

	// ---------------------------------------------------------
	//
	public void sortTimeByMonth() {
	}

	public int getCurrDeletionIndex() {
		return currDeletionLevel;
	}

	public void setCurrDeletionIndex(int cd) {
		currDeletionLevel = cd;
	}

	public void delete(int i, int ord) {
		isDeleted[i] = ord;
	}

	public void unDelete(int i, int ord) {
		isDeleted[i] = ord;
	}

	public void unDeleteAll() {
		for (int i = 0; i < numPoints; i++) {
			isDeleted[i] = 0;
		}
		mVisible = this.getSize();
	}

	public boolean isDeleted(int i) {
		return isDeleted[i] != 0;
	}

	public void select(int i) {
		mIsSelected[i] = true;
	}

	public void unselect(int i) {
		mIsSelected[i] = false;
	}

	public void unselectAll() {
		for (int i = 0; i < numPoints; i++) {
			mIsSelected[i] = false;
		}
		mSelected = 0;
	}

	public boolean isSelected(int i) {
		return mIsSelected[i];
	}

	public boolean isSomethingSelected() {
		for (int i = 0; i < numPoints; i++) {
			if (mIsSelected[i])
				return true;
		}
		return false;
	}

	public void setReferences(Object[] objs) {
		mReferences = objs;
	}

	public void setReferences(int i, Object obj) throws ArrayIndexOutOfBoundsException {
		mReferences[i] = obj;
	}

	public Object[] getReferences() {
		return mReferences;
	}

	public Object getReference(int i) throws ArrayIndexOutOfBoundsException {
		return mReferences[i];
	}

	public void dumpPointers() {
		for (int i = 0; i < this.getSize(); i++) {
			System.out.println(latArr1[i] + " " + latArr2[i] + " " + lonArr1[i] + " " + lonArr2[i] + " " + depthArr1[i] + " "
			    + depthArr2[i] + " " + timeArr1[i] + " " + timeArr2[i]);
		}
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

	public int getNumLats() {
		return latArr1.length;
	}

	public int getNumLons() {
		return lonArr1.length;
	}

	public int getNumDepths() {
		return depthArr1.length;
	}

	public int getNumTimes() {
		return timeArr1.length;
	}

	public double getLat1(int i) {
		return latArrAllVals[i];
	}

	public Double getKey(int i) {
		return keys[i];
	}

	public int getLatIndx(int i) {
		int index = i;
		int grp = i / (numLons * numLats);
		index = i - (grp * (numLons * numLats));
		return index / numLats;
	}

	public double getLat2(int i) {
		return latArrAllVals[i];
	}

	public double getLon1(int i) {
		return lonArrAllVals[i];
	}

	public int getLonIndx(int i) {
		int index = i;
		int grp = i / numLons;
		index = i - (grp * numLons);
		return index;
	}

	public double getLon2(int i) {
		return lonArrAllVals[i];
	}

	public double getZ1(int i) {
		int index = i;
		int grp = i / nLatxnLonxnZ;
		index = i - (grp * nLatxnLonxnZ);
		return depthArr1[index / nLatxnLon];
	}

	public int getZIndx(int i) {
		int index = i;
		int grp = i / nLatxnLonxnZ;
		index = i - (grp * nLatxnLonxnZ);
		return index / nLatxnLon;
	}

	public double getZ2(int i) {
		int index = i;
		int grp = i / nLatxnLonxnZ;
		index = i - (grp * nLatxnLonxnZ);
		return depthArr2[index / nLatxnLon];
	}

	public double getT1(int i) {
		int grp = i / nLatxnLonxnZ;
		if (mIsClimatology)
			grp = 0;
		return timeArr1[grp];
	}

	public double getT2(int i) {
		int grp = i / nLatxnLonxnZ;
		if (mIsClimatology)
			grp = 0;
		return timeArr2[grp];
	}

	public int getTIndx(int i) {
		int grp = i / nLatxnLonxnZ;
		return grp;
	}

	public void setBatchModeOn() {
		mBatchMode = true;
	}

	public void setBatchModeOff() {
		mBatchMode = false;
	}

	public boolean isBatchMode() {
		return mBatchMode;
	}

	public void fireSelectionChange() {
		// has the selection changed?
		pcs.firePropertyChange("selectionchange", null, null);
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	public int getNumPtrs() {
		return this.getSize();
	}

	public int getNumVisiblePtrs() {
		return mVisible;
	}

	public int getNumSelectedPtrs() {
		return mSelected;
	}

	private void computeVisible() {
		int c = 0;
		for (int i = 0; i < this.getSize(); i++) {
			if (this.isDeleted[i] == 0)
				c++;
		}
		mVisible = c;
	}

	private void computeSelected() {
		int c = 0;
		for (int i = 0; i < this.getSize(); i++) {
			if (this.mIsSelected[i])
				c++;
		}
		mSelected = c;
	}

	public void updateStats() {
		this.computeVisible();
		this.computeSelected();
	}

	public boolean isArgo() {
		return false;
	}

	public String[] getExtraArr1() {
		return null;
	}

	public String[] getExtraArr2() {
		return null;
	}

	public String[] getExtraArr3() {
		return null;
	}

	public String[] getExtraArr4() {
		return null;
	}

	public String[] getExtraArr5() {
		return null;
	}

	public String[] getExtraArr6() {
		return null;
	}

	public String[] getExtraArr7() {
		return null;
	}

	public String[] getExtraArr8() {
		return null;
	}

	public String[] getExtraArr9() {
		return null;
	}

	public String[] getExtraArr10() {
		return null;
	}

	public String getString(int i) {
		String outStr = new String("");
		return outStr;
	}

	public boolean[] getPathIsRelativeArr() {
		return null;
	}

	/**
	 * @param mTPos
	 *          the mTPos to set
	 */
	public void setTPos(int mTPos) {
		this.mTPos = mTPos;
	}

	/**
	 * @return the mTPos
	 */
	public int getTPos() {
		return mTPos;
	}

	/**
	 * @param mZPos
	 *          the mZPos to set
	 */
	public void setZPos(int mZPos) {
		this.mZPos = mZPos;
	}

	/**
	 * @return the mZPos
	 */
	public int getZPos() {
		return mZPos;
	}

	/**
	 * @param mLatPos
	 *          the mLatPos to set
	 */
	public void setLatPos(int mLatPos) {
		this.mLatPos = mLatPos;
	}

	/**
	 * @return the mLatPos
	 */
	public int getLatPos() {
		return mLatPos;
	}

	/**
	 * @param mLonPos
	 *          the mLonPos to set
	 */
	public void setLonPos(int mLonPos) {
		this.mLonPos = mLonPos;
	}

	/**
	 * @return the mLonPos
	 */
	public int getLonPos() {
		return mLonPos;
	}
}
/*
 * $Id: Interpolation.java,v 1.8 2005/06/17 18:10:58 oz Exp $
 *
 */

package javaoceanatlas.utility;

import org.w3c.dom.*;
import gov.noaa.pmel.sgt.contour.ZGrid;
import java.util.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.ui.*;
import java.io.File;
import java.io.IOException;

public class ZGridInterpolation implements Interpolation {
	protected int mInterpolationType = ZGRID_INTERPOLATION;
	protected double[][] mValues = null;
	protected double[] mDistValues = null;
	protected double[] mMeanValues = null;
	protected double[] mBottomDepths = null;
	private double[] mGriddedValues;
	protected int mNumLevels;
	protected int mNumStns;
	protected JOAVariable mInterpParam;
	protected int mSurfaceParamNum;
	protected NewInterpolationSurface mSurface;
	protected NewInterpolationSurface mZgridSurface;
	protected String mName;
	protected String mParam;
	protected double mRefLevel;
	protected FileViewer mFileViewer;
	public static int MINUS = -1;
	public static int PLUS = +1;
	public static int ZERO = 0;
	protected int mTotalStns;
	protected double[] mSurfaceValues = null;
	protected int mNumBottles;
	protected int mNumCasts;
	protected double mMin = JOAConstants.MISSINGVALUE;
	protected double mMax = JOAConstants.MISSINGVALUE;
	protected boolean[] mMeanCastStnList = null;
	protected boolean mIsResidualInterp = false;
	protected String mSurfaceFileName;
	private UVCoordinate[] mMeanCastValuesFromFile;
	private boolean mUseMeanCastValuesFromFile = false;
	private double[] mLatValues;
	private double[] mLonValues;
	private String[] mStnValues;
	String mMeanCastFV;
	private int mX;
	private int mY;
	private double mCay;
	private int mGridSpacing;
	private boolean mMaskBottom;
	private double mDX, mDY;
	private double mTotDist;
	double[] mGridZlevels;

	public ZGridInterpolation(ZGridInterpolation interpIn) {
		mInterpolationType = interpIn.mInterpolationType;
		mFileViewer = interpIn.mFileViewer;
		mTotalStns = mFileViewer.mTotalStations;
		mSurface = interpIn.mSurface;
		mSurfaceFileName = new String(interpIn.mSurfaceFileName);
		mInterpParam = interpIn.mInterpParam;
		mSurfaceParamNum = interpIn.mSurfaceParamNum;
		mName = new String(interpIn.mName);
		mParam = new String(interpIn.mParam);
		mRefLevel = interpIn.mRefLevel;
		mNumLevels = interpIn.mSurface.getNumLevels();
		mSurfaceValues = new double[mNumLevels];
//		double[] inVals = mSurface.getValues();
//		for (int i = 0; i < mNumLevels; i++) {
//			mSurfaceValues[i] = inVals[i];
//		}
		mNumCasts = interpIn.mNumCasts;
		mNumBottles = interpIn.mNumBottles;
		mIsResidualInterp = interpIn.mIsResidualInterp;
		mMeanCastStnList = interpIn.mMeanCastStnList;
		mMeanCastValuesFromFile = interpIn.mMeanCastValuesFromFile;
		if (mMeanCastValuesFromFile != null) {
			mUseMeanCastValuesFromFile = true;
		}

		mX = interpIn.getNumX();
		mY = interpIn.getNumY();
		mCay = interpIn.getCay();
		mGridSpacing = interpIn.getGridSpacing();
		mMaskBottom = interpIn.isMaskBottom();

		// create the arrays
		mValues = new double[128][mTotalStns];
		mDistValues = new double[mTotalStns];
		mBottomDepths = new double[mTotalStns];
		mLatValues = new double[mTotalStns];
		mLonValues = new double[mTotalStns];
		mStnValues = new String[mTotalStns];

		// compute the distances between casts
		computeDistances();

			doInterp(false);
	}
	
	public void doInterp(boolean redim) {
		doZgrid();
	}

	public ZGridInterpolation(FileViewer fv, String surfaceFileName, NewInterpolationSurface surf, JOAVariable interpParam,
	    int surfaceParamNum, String name, String param, double refLevel, boolean isResidualInterp, boolean[] meanStnList,
	    UVCoordinate[] meanCastValuesFromFile, String meanCastFV, int numx, int numy, double cay, int gridspc,
	    boolean mask) {

		mFileViewer = fv;
		mSurfaceFileName = surfaceFileName;
		mTotalStns = mFileViewer.mTotalStations;
		mInterpolationType = ZGRID_INTERPOLATION;
		mInterpParam = interpParam;
		mSurfaceParamNum = surfaceParamNum;
		mName = new String(name);
		mParam = new String(param);
		mRefLevel = refLevel;
		mSurface = surf;
		mNumLevels = mSurface.getNumLevels();
		mSurfaceValues = new double[mNumLevels];
		double[] inVals = mSurface.getValues();
		for (int i = 0; i < mNumLevels; i++) {
			mSurfaceValues[i] = inVals[i];
		}
		mIsResidualInterp = isResidualInterp;
		mMeanCastStnList = meanStnList;
		mMeanCastValuesFromFile = meanCastValuesFromFile;
		if (mMeanCastValuesFromFile != null) {
			mUseMeanCastValuesFromFile = true;
			mMeanCastValuesFromFile = meanCastValuesFromFile;
			mMeanCastFV = meanCastFV;
		}
		mX = numx;
		mY = numy;
		mCay = cay;
		mGridSpacing = gridspc;
		mMaskBottom = mask;

		// create the arrays
		mValues = new double[128][mTotalStns];
		mDistValues = new double[mTotalStns];
		mMeanValues = new double[128];
		mBottomDepths = new double[mTotalStns];
		mLatValues = new double[mTotalStns];
		mLonValues = new double[mTotalStns];
		mStnValues = new String[mTotalStns];

		// compute the distances between casts
		computeDistances();
		doInterp(false);
	}

	public void saveAsXML(FileViewer fv, Document doc, Element root, Element item) {
		item.setAttribute("intertype", String.valueOf(mInterpolationType));
		item.setAttribute("interpname", mName);
		item.setAttribute("surfacepname", mSurfaceFileName);
		item.setAttribute("interpvariable", mInterpParam.getVarName());
		item.setAttribute("surfacevariable", fv.mAllProperties[mSurfaceParamNum].mVarLabel);
		item.setAttribute("parameter", mParam);
		item.setAttribute("reflevel", String.valueOf(mRefLevel));
		item.setAttribute("numcasts", String.valueOf(mNumCasts));
		item.setAttribute("isresidualinterp", String.valueOf(mIsResidualInterp));
		root.appendChild(item);
		if (mIsResidualInterp) {
			Element item2 = doc.createElement("meancastvalues");
			for (int i = 0; i < mMeanValues.length; i++) {
				Element valItem = doc.createElement("value");
				valItem.appendChild(doc.createTextNode(String.valueOf(mMeanValues[i])));
				item2.appendChild(valItem);

			}
			root.appendChild(item2);
		}
	}

	public void redimensionInterpolation() {
		mValues = null;
		mDistValues = null;
		mTotalStns = mFileViewer.mTotalStations;
		mValues = new double[128][mTotalStns];
		mDistValues = new double[mTotalStns];
		mBottomDepths = new double[mTotalStns];
		mLatValues = new double[mTotalStns];
		mLonValues = new double[mTotalStns];
		mStnValues = new String[mTotalStns];
		computeDistances();
	}

	//this has to create lats/lons etc on the zGrid
	public void computeDistances() {
		double startLat;
		double latDelta;
		double startLon;
		double lonDelta;
		
		//mFileViewer.getLonRange()
		int stCount = 0;
		for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile) mFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section) of.mSections.elementAt(sec);
				if (sech.mNumCasts == 0) {
					continue;
				}

				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station) sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}
					mDistValues[stCount] = sh.mCumDist;
					mBottomDepths[stCount] = sh.mBottomDepthInDBARS;
					mLatValues[stCount] = sh.getLat();
					mLonValues[stCount] = sh.getLon();
					mStnValues[stCount] = sh.getStn();
					stCount++;
				}
			}
		}
	}

	public boolean isBelowBottom(int lvl, double stnDepth) {
		// set everything in the interpolation
		if (stnDepth != JOAConstants.MISSINGVALUE) {
			if (mSurfaceValues[lvl] >= stnDepth) { return true; }
		}
		return false;
	}

	public void doZgrid() {
		int c1Pos = 0, c2Pos = 0, c3Pos = 0, c4Pos = 0;
		double c1Val = 0.0, c2Val = 0.0, c3Val = 0.0, c4Val = 0.0;

		Vector<Double> vals = new Vector<Double>();
		Vector<Double> surfVals = new Vector<Double>();
		Vector<Double> distVals = new Vector<Double>();
		Vector<Double> latVals = new Vector<Double>();
		Vector<Double> lonVals = new Vector<Double>();

		// compute the number of z pts (valid observations of the contour parameter
		// in surf-distance space)
		for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile) mFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section) of.mSections.elementAt(sec);
				if (sech.mNumCasts == 0) {
					continue;
				}

				int iPos = sech.getVarPos(mInterpParam.getVarName(), false);
				int sPos = sech.getVarPos(mFileViewer.mAllProperties[mSurfaceParamNum].mVarLabel, true);
				int pPos = sech.getPRESVarPos();

				if (iPos < 0 || sPos < 0 || pPos < 0) {
					continue;
				}

				if (mFileViewer.mObsFilterActive) {
					if (mFileViewer.mCurrObsFilter.isCriteria1Active()) {
						c1Pos = sech.getVarPos(mFileViewer.mAllProperties[mFileViewer.mCurrObsFilter.getParamIndex(0)]
						    .getVarLabel(), false);
					}
					if (mFileViewer.mCurrObsFilter.isCriteria2Active()) {
						c2Pos = sech.getVarPos(mFileViewer.mAllProperties[mFileViewer.mCurrObsFilter.getParamIndex(1)]
						    .getVarLabel(), false);
					}
					if (mFileViewer.mCurrObsFilter.isCriteria3Active()) {
						c3Pos = sech.getVarPos(mFileViewer.mAllProperties[mFileViewer.mCurrObsFilter.getParamIndex(2)]
						    .getVarLabel(), false);
					}
					if (mFileViewer.mCurrObsFilter.isCriteria4Active()) {
						c4Pos = sech.getVarPos(mFileViewer.mAllProperties[mFileViewer.mCurrObsFilter.getParamIndex(3)]
						    .getVarLabel(), false);
					}
				}

				// draw the station points
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station) sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}

					double bottom = sh.getBottom();
					double dist = sh.mCumDist;
					latVals.add(sh.getLat());
					lonVals.add(sh.getLon());

					for (int b = 0; b < sh.mNumBottles; b++) {
						Bottle bh = (Bottle) sh.mBottles.elementAt(b);
						double sval = bh.mDValues[sPos];
						double ival = bh.mDValues[iPos];
						if (sval == JOAConstants.MISSINGVALUE || ival == JOAConstants.MISSINGVALUE) {
							continue;
						}

						if (mFileViewer.mObsFilterActive) {
							if (mFileViewer.mCurrObsFilter.isCriteria1Active()) {
								c1Val = bh.mDValues[c1Pos];
							}
							if (mFileViewer.mCurrObsFilter.isCriteria2Active()) {
								c2Val = bh.mDValues[c2Pos];
							}
							if (mFileViewer.mCurrObsFilter.isCriteria3Active()) {
								c3Val = bh.mDValues[c3Pos];
							}
							if (mFileViewer.mCurrObsFilter.isCriteria4Active()) {
								c4Val = bh.mDValues[c4Pos];
							}

							if (!mFileViewer.mCurrObsFilter.testValues(c1Val, c2Val, c3Val, c4Val, bh, c1Pos, c2Pos, c3Pos, c4Pos)) {
								continue;
							}
						}

						// do the bottom masking here
						if (mMaskBottom && bottom != JOAConstants.MISSINGVALUE) {
							double depth = bh.mDValues[pPos];
							if (depth >= bottom) {
								vals.add(Double.NaN);
							}
							else {
								vals.add(new Double(ival));
							}
						}
						else {
							vals.add(new Double(ival));
						}
						distVals.add(dist);
						surfVals.add(new Double(sval));
					}
				}
			}
		}
		
		double[] zp = new double[vals.size()];
		double[] yp = new double[surfVals.size()];
		double[] xp = new double[distVals.size()];
		
		for (int i = 0; i < vals.size(); i++) {
			zp[i] = vals.elementAt(i);
		}
		
		double yMin = 1.0e30;
		double yMax = -1.0e30;
		
		for (int i = 0; i < surfVals.size(); i++) {
			yp[i] = surfVals.elementAt(i);
			if (yp[i] < yMin)
				yMin = yp[i];
			if (yp[i] > yMax)
				yMax = yp[i];
		}
		
		for (int i = 0; i < distVals.size(); i++) {
			xp[i] = distVals.elementAt(i);
		}
		/*
		 * 
		 * @param nx int Number of X grid points @param ny int Number of Y grid
		 * points @param x1 double X coordinate of Z[0,0] @param y1 double Y
		 * coordinate of Z[0,0] @param dx double X grid spacing @param dy double Y
		 * grid spacing @param xp double[] X coordinates of random data @param yp
		 * double[] Y coordinates of random data @param zp double[] Z coordinates of
		 * random data
		 */
		int xLen = xp.length;
		int yLen = yp.length;

		double x1 = xp[0];
		double y1 = yp[0];
		mTotDist = (xp[xLen - 1] - xp[0]);
		mDX = mTotDist / (double) mX; // maxdist/mX
		mDY = (yMax - yMin) / (double) mY; // y axis range/mY
		
//		System.out.println("mCay = " + mCay);
//		System.out.println("mGridSpacing = " + mGridSpacing);
//		System.out.println("mX = " + mX);
//		System.out.println("mY = " + mY);

		ZGrid grid = new ZGrid(mX, mY, x1, y1, mDX, mDY, xp, yp, zp, mCay, mGridSpacing, false);
		
		mGriddedValues = grid.getZArray();
		
		// need to fill in mValues
		mValues = new double[mY][mX];
		for (int j=0; j<mY; j++) {
			for (int i=0; i<mX; i++) {
				mValues[j][i] = z(i, j);
			}
		}
		
		// zvals are the mY points 
		mGridZlevels = new double[mY];
		for (int yy=0; yy<mY; yy++) {
			mGridZlevels[yy] = (double)yy * mDY;
		}
		

		mZgridSurface = new NewInterpolationSurface(mGridZlevels, mY, mSurface.getParam(), mSurface.getTitle(), "");
		
		// mDistValues
		
		//Bottom Depths
		
		// Lat values of zgrid
		
		// lon values of zgrid
		
		//stnvalues
		
		grid.toString();
	}
	
	public double[] getGridLevels() {
		return mGridZlevels;
	}


	private double z(int i, int j) {
		return mGriddedValues[j + i * mY];
	}

	public Bottle findClosestBottle(Vector<Bottle> botts, double testVal, int spos) {
		double dist = 6000;
		Bottle retBot = null;
		for (int b = 0; b < botts.size(); b++) {
			Bottle bh = (Bottle) botts.elementAt(b);
			double sval = bh.mDValues[spos];
			double del = Math.abs(sval - testVal);
			if (del < dist) {
				dist = del;
				retBot = bh;
			}
			else if (del > dist) { return retBot; }
		}
		return null;
	}

	public int getLevels() {
		return mY;
	}

	public int getNumStns() {
		return mX;
	}

	public JOAVariable getParam() {
		return mInterpParam;
	}

	public int getSurfParamNum() {
		return mSurfaceParamNum;
	}

	public String getName() {
		return mName;
	}

	public void setName(String s) {
		mName = s;
	}

	public String getParamName() {
		return mParam;
	}

	public void setParamName(String s) {
		mParam = s;
	}

	public NewInterpolationSurface getSurface() {
		return mZgridSurface;
	}

	public double getMinValue() {
		if (mMin != JOAConstants.MISSINGVALUE) { return mMin; }
		double min = 1e10;
		for (int i = 0; i < mY; i++) {
			for (int j = 0; j < mX; j++) {
				min = mValues[i][j] != JOAConstants.MISSINGVALUE && mValues[i][j] < min ? mValues[i][j] : min;
			}
		}
		mMin = min;
		// System.out.println("mMin=" + mMin);
		return mMin;
	}

	public double getMaxValue() {
		if (mMax != JOAConstants.MISSINGVALUE) { return mMax; }
		double max = -1e10;
		for (int i = 0; i < mY; i++) {
			for (int j = 0; j < mX; j++) {
				max = mValues[i][j] != JOAConstants.MISSINGVALUE && mValues[i][j] > max ? mValues[i][j] : max;
				// if(max > 2.0) {
				// System.out.println(i + " " + j + " max=" + max);
				// }
			}
		}
		mMax = max;
		// System.out.println("mMax=" + mMax);
		return mMax;
	}

	public double[][] getValues() {
		return mValues;
	}

	public double[] getDistValues() {
		return mDistValues;
	}

	public double[] getBottomDepths() {
		return mBottomDepths;
	}

	public double[] getLatValues() {
		return mLatValues;
	}

	public double[] getLonValues() {
		return mLonValues;
	}

	public String[] getStnValues() {
		return mStnValues;
	}

	public void dereference() {
		if (mRefLevel == JOAConstants.MISSINGVALUE) { return; }

		// compute dereferenced interpolation
		int row1 = 0, row2 = 0, iSame = 0;
		double datum, delta1 = 0.0, delta2 = 0.0;
		double part1, part2, theRef, theRef1, theRef2;
		boolean flag1, flag2;

		// Test for refLevel matching an interpolation surface.
		flag1 = false;
		for (int i = 0; i < mNumLevels; i++) {
			if (mSurfaceValues[i] == JOAConstants.MISSINGVALUE) {
				continue;
			}
			flag1 = (mRefLevel - mSurfaceValues[i]) == 0;
			if (flag1) {
				iSame = i;
				break;
			}
		}
		flag2 = false;
		for (int i = 0; i < mNumLevels - 1; i++) {
			if (mSurfaceValues[i] == JOAConstants.MISSINGVALUE) {
				continue;
			}
			if (mSurfaceValues[i + 1] == JOAConstants.MISSINGVALUE) {
				continue;
			}
			delta1 = mRefLevel - mSurfaceValues[i];
			delta2 = mRefLevel - mSurfaceValues[i + 1];
			flag2 = ((delta1 > 0) ^ (delta2 > 0));
			if (flag2) {
				row1 = i;
				row2 = i + 1;
				break;
			}
		}

		if (!flag2 && !flag1) {
			;// return -1; // Error: invalid reference level.
		}

		if (flag1) {
			flag2 = false;
			for (int j = 0; j < mTotalStns; j++) {
				theRef = mValues[iSame][j];
				for (int i = 0; i < mNumLevels; i++) {
					datum = mValues[i][j];
					if (datum == JOAConstants.MISSINGVALUE) {
						continue;
					}
					datum = (theRef == JOAConstants.MISSINGVALUE) ? theRef : datum - theRef;
					mValues[i][j] = datum;
				}
			}
		}

		if (flag2) {
			delta1 = Math.abs(delta1);
			delta2 = Math.abs(delta2);
			part1 = delta2 / (delta1 + delta2);
			part2 = delta1 / (delta1 + delta2);
			for (int j = 0; j < mTotalStns; j++) {
				theRef1 = mValues[row1][j];
				theRef2 = mValues[row2][j];
				theRef = part1 * theRef1 + part2 * theRef2;
				theRef = (theRef1 == JOAConstants.MISSINGVALUE) ? JOAConstants.MISSINGVALUE : theRef;
				theRef = (theRef2 == JOAConstants.MISSINGVALUE) ? JOAConstants.MISSINGVALUE : theRef;
				for (int i = 0; i < mNumLevels; i++) {
					datum = mValues[i][j];
					if (datum == JOAConstants.MISSINGVALUE) {
						continue;
					}
					datum = (theRef == JOAConstants.MISSINGVALUE) ? theRef : datum - theRef;
					mValues[i][j] = datum;
				}
			}
		}
	}

	public boolean isResidualInterp() {
		return mIsResidualInterp;
	}

	public int getInterpolationType() {
		return mInterpolationType;
	}

	/*
	 * Regridding assumes that both interpolations are on the same y grid the
	 * grids overlap (otherwise routine wouldn't be called)
	 * 
	 */
	public Vector<Object>[] regridToUnionandDifference(LinearInterpolation otherGrid, int horzGridMode) {
		return null;
	}

	@SuppressWarnings("unchecked")
	public Vector<Object>[] regridAndDifference(LinearInterpolation bGrid, int horzGridMode, boolean diffFlag, double maxDist) {
		/*
		 * this regridding works by interpolating the values in this interpolation
		 * to the lat or lon locations of the bGrid The results are a vector of new
		 * profiles for the variable represented by this interpolation
		 */

		Vector[] results = new Vector[2];
		results[0] = new Vector<double[]>();
		results[1] = new Vector<UVCoordinate>();
		int newStnCnt = 0;
		if (horzGridMode == JOAConstants.REGRID_MODE_LAT) {
			double[][] bVals = bGrid.getValues();
			double[] bLats = bGrid.getLatValues();
			double[] bLons = bGrid.getLonValues();
			double[] bBottoms = bGrid.getBottomDepths();
			double[] aLats = this.getLatValues();
			int numBStns = bGrid.getNumStns();
			int numAStns = this.getNumStns();

			// loop on the profiles in the 'b' interpolation (the interp based to this return)
			// interpolating to the locations in the 'b' interpolation
			for (int i = 0; i < numBStns; i++) {
				// want to interpolate a value to this location
				double bLat = bLats[i];
				double bLon = bLons[i];
				double bBottom = bBottoms[i];

				// this is the destination profile
				double[] newProfile = null;

				// search the 'a' interpolation (this object) for bounding stations
				double prevLat = aLats[0];
				for (int j = 1; j < numAStns; j++) {
					double aLat = aLats[j];
					if (aLat > bLat && prevLat < bLat && 
							(Math.abs(aLat - bLat) < maxDist && Math.abs(prevLat - bLat) < maxDist)) {
						// found bounding stations in 'a' around the location in 'b'

						// this is where the new profile is going to go
						newProfile = new double[mNumLevels];
						for (int z = 0; z < mNumLevels; z++) {
							newProfile[z] = JOAConstants.MISSINGVALUE;
						}

						// interpolate and make new difference cast
						double deltaLat = aLat - prevLat;
						double del = bLat - prevLat;
						for (int z = 0; z < mNumLevels; z++) {
							double prevInterpVal = mValues[z][j - 1];
							double interpVal = mValues[z][j];
							if (prevInterpVal == JOAConstants.MISSINGVALUE || interpVal == JOAConstants.MISSINGVALUE
							    || bVals[z][i] == JOAConstants.MISSINGVALUE) {
								newProfile[z] = JOAConstants.MISSINGVALUE;
								continue;
							}
							double iSlope = (interpVal - prevInterpVal) / deltaLat;
							double predictedInterpVal = iSlope * del + prevInterpVal;
							
							if (diffFlag) {
								// compute the difference
								newProfile[z] = predictedInterpVal - bVals[z][i];
							}
							else {
								newProfile[z] = predictedInterpVal;
							}
						}
						double newStnLat = bLat;
						double newStnLon = bLon;
						UVCoordinate loc = new UVCoordinate(newStnLon, newStnLat, bBottom);
						results[0].add(newProfile);
						results[1].add(loc);
						newStnCnt++;
						break;
					}
					prevLat = aLat;
				}
			}
			return results;
		}
		else {
			double[][] bVals = bGrid.getValues();
			double[] bLats = bGrid.getLatValues();
			double[] bLons = bGrid.getLonValues();
			double[] bBottoms = bGrid.getBottomDepths();
			double[] aLons = this.getLonValues();
			int numBStns = bGrid.getNumStns();
			int numAStns = this.getNumStns();

			// loop on the profiles in the 'b' interpolation (the interp based to this
			// return)
			// interpolating to the locations in the 'b' interpolation
			for (int i = 0; i < numBStns; i++) {
				// want to interpolate a value to this location
				double bLon = bLons[i];
				double bLon0 = bLons[i];
				double bLat = bLats[i];
				double bBottom = bBottoms[i];

				// this is the destination profile
				double[] newProfile = null;

				// search the 'a' interpolation (this object) for bounding stations
				double prevLon = aLons[0];
				if (prevLon < 0) {
					prevLon += 360;
				}

				for (int j = 1; j < numAStns; j++) {
					double aLon = aLons[j];

					if (aLon < 0) {
						aLon += 360;
					}
					if (bLon < 0) {
						bLon += 360;
					}
					if (aLon > bLon && prevLon < bLon &&
							(Math.abs(aLon - bLon) < maxDist && Math.abs(prevLon - bLon) < maxDist)) {
						// found bounding stations in 'a' around the location in 'b'
						
						// this is where the new profile is going to go
						newProfile = new double[mNumLevels];
						for (int z = 0; z < mNumLevels; z++) {
							newProfile[z] = JOAConstants.MISSINGVALUE;
						}

						// interpolate and make new difference cast
						double deltaLon = aLon - prevLon;
						double del = bLon - prevLon;
						for (int z = 0; z < mNumLevels; z++) {
							double prevInterpVal = mValues[z][j - 1];
							double interpVal = mValues[z][j];
							if (prevInterpVal == JOAConstants.MISSINGVALUE || interpVal == JOAConstants.MISSINGVALUE
							    || bVals[z][i] == JOAConstants.MISSINGVALUE) {
								newProfile[z] = JOAConstants.MISSINGVALUE;
								continue;
							}
							double iSlope = (interpVal - prevInterpVal) / deltaLon;
							double predictedInterpVal = iSlope * del + prevInterpVal;
							// System.out.println(prevInterpVal + " " + predictedInterpVal + "
							// " + interpVal);

							// compute the difference
							newProfile[z] = predictedInterpVal - bVals[z][i];
						}
						UVCoordinate loc = new UVCoordinate(bLon0, bLat, bBottom);
						results[0].add(newProfile);
						results[1].add(loc);
						newStnCnt++;
						break;
					}
					prevLon = aLon;
				}
			}
			return results;
		}
	}

	public void dump() {
		System.out.println("mNumLevels = " + mNumLevels);
		System.out.println("mNumStns= " + mTotalStns);
		System.out.println("mInterpParam = " + mInterpParam);
		System.out.println("mSurfaceParamNum = " + mSurfaceParamNum);
		System.out.println("mSurface = " + mSurface.getParam());
		System.out.println("mName = " + mName);
		System.out.println("mParam = " + mParam);
		System.out.println("mValues = " + mValues.length);
	}

	public void writeToLog(String preamble) throws IOException {
		try {

			JOAConstants.LogFileStream.writeBytes(preamble + "\n");
			JOAConstants.LogFileStream.writeBytes("\t" + "Param = " + mInterpParam + ", Surf. = " + mSurface.getTitle() + " "
			    + mSurface.getDescrip() + "\n");

			// reference stuff
			if (mIsResidualInterp) {
				if (mUseMeanCastValuesFromFile) {
					// mean cast from file
					JOAConstants.LogFileStream.writeBytes("\t" + "Referenced to mean cast in file = " + mMeanCastFV);
				}
				else if (mRefLevel != JOAConstants.MISSINGVALUE) {
					// residual from reference value
					JOAConstants.LogFileStream.writeBytes("\t" + "Referenced to value = "
					    + JOAFormulas.formatDouble(mRefLevel, 3, false));
				}
				else {
					// computed mean cast
					JOAConstants.LogFileStream.writeBytes("\t" + "Referenced to computed mean cast");
				}
				JOAConstants.LogFileStream.writeBytes("\n");
			}
			JOAConstants.LogFileStream.flush();
		}
		catch (IOException ex) {
			throw ex;
		}
	}

	public void computeHorzGradient(boolean isVelocity) {
		// makes a new array based at the mid points between casts

		double[][] gradientMatrix = new double[mNumLevels][mTotalStns];
		double[] midDistances = new double[mTotalStns];

		for (int i = 0; i < mNumLevels; i++) {
			for (int j = 0; j < mTotalStns; j++) {
				gradientMatrix[i][j] = JOAConstants.MISSINGVALUE;
			}
		}

		int numMidStns = 0;
		for (int i = 0; i < mNumLevels; i++) {
			double oldMid = 0.0;
			double midLat = 0.0;
			int iCross = -1;
			int c = 0;
			for (int j0 = 0, j1 = 1; j1 < mTotalStns; j0++, j1++) {
				double datum0 = mValues[i][j0];
				double datum1 = mValues[i][j1];
				double dist0 = JOAConstants.NAUT2METERS * mDistValues[j0];
				double dist1 = JOAConstants.NAUT2METERS * mDistValues[j1];
				if (datum0 == JOAConstants.MISSINGVALUE || datum1 == JOAConstants.MISSINGVALUE) {
					c++;
					continue;
				}

				double grad = (datum1 - datum0) / (dist1 - dist0);
				midDistances[c] = dist0 + (0.5 * (dist1 - dist0));

				if (isVelocity) {
					midLat = (0.5 * (mLatValues[j0] + mLatValues[j1])) * JOAConstants.F;
					double f = JOAConstants.TWOOMEGA * Math.sin(midLat);
					if (midLat * oldMid < 0.0) {
						iCross = j0;
					}
					gradientMatrix[i][c] = grad / f;
				}
				else {
					gradientMatrix[i][c] = grad;
				}
				oldMid = midLat;
				c++;
			}
			if (i == 0) {
				numMidStns = c;
			}

			if (iCross != -1) {
				// crossed the equator
				double datum0 = gradientMatrix[i][iCross];
				double datum1 = gradientMatrix[i][iCross + 1];
				datum1 = (datum0 == JOAConstants.MISSINGVALUE) ? JOAConstants.MISSINGVALUE : datum1;
				datum0 = (datum1 == JOAConstants.MISSINGVALUE) ? JOAConstants.MISSINGVALUE : datum0;
				datum0 = 0.5 * (datum0 + datum1);
				gradientMatrix[i][iCross] = datum0;
				gradientMatrix[i][iCross + 1] = datum0;
			}
		}

		// set the first and last columns to missing
		for (int i = 0; i < mNumLevels; i++) {
			mValues[i][0] = JOAConstants.MISSINGVALUE;
			mValues[i][mTotalStns - 1] = JOAConstants.MISSINGVALUE;
		}

		// interpolate the velocity back onto the original grid
		for (int i = 0; i < mNumLevels; i++) {
			for (int c = 0; c < numMidStns; c++) {
				double gradVal0 = gradientMatrix[i][c];
				double gradVal1 = gradientMatrix[i][c + 1];

				if (gradVal0 == JOAConstants.MISSINGVALUE || gradVal1 == JOAConstants.MISSINGVALUE) {
					mValues[i][c] = JOAConstants.MISSINGVALUE;
					continue;
				}
				double gradDist0 = midDistances[c];
				double gradDist1 = midDistances[c + 1];
				double deltaGradVal = gradVal0 - gradVal1;
				double ratio = ((JOAConstants.NAUT2METERS * mDistValues[c]) - gradDist0) / (gradDist1 - gradDist0);
				mValues[i][c] = gradVal0 + (ratio * deltaGradVal);
			}
		}
	}

	public boolean isOnSameGrid(Interpolation inGrid) {
		NewInterpolationSurface inSurf = inGrid.getSurface();

		double[] tSurfLevels = inSurf.getValues();
		return this.getSurface().equalGrids(tSurfLevels, inSurf.getNumLevels());
	}

	public void multiplyBy(LinearInterpolation inGrid) {
		// on the same grid?
		if (!isOnSameGrid(inGrid)) { return; }

		// multiply
		double[][] inVals = inGrid.getValues();
		for (int i = 0; i < mNumLevels; i++) {
			for (int j = 0; j < mTotalStns; j++) {
				if (inVals[i][j] != JOAConstants.MISSINGVALUE && mValues[i][j] != JOAConstants.MISSINGVALUE) {
					mValues[i][j] = mValues[i][j] * inVals[i][j];
				}
				else {
					mValues[i][j] = JOAConstants.MISSINGVALUE;
				}
			}
		}
	}

	public void integrate(int direc, boolean up) {
	}

	public int getNumX() {
		return mX;
	}

	public int getNumY() {
		return mY;
	}

	public double getCay() {
		return mCay;
	}

	public int getGridSpacing() {
		return mGridSpacing;
	}

	public boolean isMaskBottom() {
		return mMaskBottom;
	}

  public double getTotalDistance() {
	  return mFileViewer.mTotMercDist;
  }

  public double getTotalLatitude() {
	  return 0;
  }

  public double getTotalLongitude() {
	  return 0;
  }

	/* (non-Javadoc)
   * @see javaoceanatlas.utility.Interpolation#getTotalTime()
   */
  public double getTotalTime() {
	  // TODO Auto-generated method stub
	  return 0;
  }

	 public int getNumUsedStations() {
	  return mX;
  }
  
  public double getDX() {
  	return mDX;
  }
  
  public double getDY() {
  	return mDY;
  }

  public double getClosestValue(int level, Station sh) {
	  // TODO Auto-generated method stub
	  return 0;
  }

	/* (non-Javadoc)
   * @see javaoceanatlas.utility.Interpolation#isLocked()
   */
  public boolean isLocked() {
	  // TODO Auto-generated method stub
	  return false;
  }

	/* (non-Javadoc)
   * @see javaoceanatlas.utility.Interpolation#setLocked(boolean)
   */
  public void setLocked(boolean b) {
	  // TODO Auto-generated method stub
	  
  }

	/* (non-Javadoc)
   * @see javaoceanatlas.utility.Interpolation#exportJSON(java.io.File)
   */
  public void exportJSON(File f) {
	  // TODO Auto-generated method stub
	  
  }
}


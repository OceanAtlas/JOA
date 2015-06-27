/**
 * $Id: ZGrid.java,v 1.6 2005/03/31 18:05:35 dwd Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package gov.noaa.pmel.sgt.contour;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import gov.noaa.pmel.sgt.SGLabel;
import gov.noaa.pmel.sgt.dm.*;
import gov.noaa.pmel.util.GeoDate;
import gov.noaa.pmel.util.GeoDateArray;
import gov.noaa.pmel.util.Range2D;
import gov.noaa.pmel.util.SoTRange;
import java.io.IOException;
import javaoceanatlas.utility.JOAFormulas;
import java.io.File;
import gov.noaa.pmel.eps2.Dbase;
import java.awt.Color;
import javaoceanatlas.ui.ProgressDialog;
import java.awt.Frame;
import gov.noaa.pmel.eps2.EpicPtr;
import gov.noaa.pmel.eps2.EPSConstants;
import gov.noaa.pmel.eps2.PointerDBIterator;
import ucar.multiarray.MultiArray;
import gov.noaa.pmel.eps2.EPSDbase;
import javaoceanatlas.utility.UVCoordinate;
import gov.noaa.pmel.eps2.EPSVariable;
import gov.noaa.pmel.eps2.Axis;
import java.util.Vector;
import gov.noaa.pmel.eps2.EpicPtrs;
import gov.noaa.pmel.eps2.EPSDBIterator;

/**
 * <pre>
 * Sets up square grid for contouring , given arbitrarily placed
 * data points. Laplace interpolation is used.
 * The method used here was lifted directly from notes left by
 * Mr Ian Crain formerly with the comp.science div.
 * Info on relaxation soln of laplace eqn supplied by Dr T Murty.
 * Fortran II   oceanography/emr   Dec/68   jdt
 * z = 2-d array of hgts to be set up. points outside region to be
 * contoured should be initialized to 10**35 . the rest should be 0.0
 * nx,ny = max subscripts of z in x and y directions .
 * x1,y1 = coordinates of z(1,1)
 * dx,dy = x and y increments .
 * xp,yp,zp = arrays giving position and hgt of each data point.
 * n = size of arrays xp,yp and zp .
 * Modification feb/69  To get smoother results a portion of the
 * beam eqn  was added to the laplace eqn giving
 * delta2x(z)+delta2y(z) - k(delta4x(z)+delta4y(z)) = 0 .
 * k=0 gives pure laplace solution.  k=inf. gives pure spline solution.
 * cayin = k = amount of spline eqn (between 0 and inf.)
 * nrng...grid points more than nrng grid spaces from the nearest
 *       data point are set to undefined.
 * Modification Dec23/69   Data pts no longer moved to grid pts.
 * Modification May 5 79  Common blocks work1 and work2 must
 * be dimension at least n points long by the user.  Common
 * block work3 must be dimensioned at least ny points long.
 * Modification June 17,1985 - Handles data values of 1e35. If at
 * least one data value near a grid point is equal to 1e35, the z
 * array is initialized to 1e35 at that grid point
 * - by G.R. Halliwell
 * Modification March 31, 2005 - Arithmetic ifs converted to block ifs.
 * 1e35 changed to Double.NaN. Ported to Java. - by D.W. Denbo
 * </pre>
 * 
 * @since 3.1
 */

public class ZGrid implements SGTGrid, Cartesian, Cloneable, Serializable {
	protected double[] xloc_;
	protected double[] yloc_;
	protected GeoDateArray tloc_;
	protected double[] grid_;
	protected double[] xEdges_;
	protected double[] yEdges_;
	protected GeoDateArray tEdges_;
	protected boolean hasXEdges_;
	protected boolean hasYEdges_;
	protected String title_;
	protected SGLabel keyTitle_ = null;
	protected String id_ = null;
	protected boolean xTime_;
	protected boolean yTime_;
	protected SGTMetaData xMetaData_ = null;
	protected SGTMetaData yMetaData_ = null;
	protected SGTMetaData zMetaData_ = null;
	protected SGTGrid associatedData_;
	protected SoTRange xRange_ = null;
	protected SoTRange yRange_ = null;
	protected SoTRange xEdgesRange_ = null;
	protected SoTRange yEdgesRange_ = null;
	protected Range2D zRange_ = null;
	private PropertyChangeSupport changes_ = new PropertyChangeSupport(this);
	static boolean ptsHaveBeenCached = false;
	static float[] savedLats = null;
	static float[][] savedVals = null;
	static float[] savedLons = null;
	private double cay_;
	private int nrng_;
	private double x1_;
	private double y1_;
	private double dx_;
	private double dy_;
	private int nx_;
	private int ny_;

	/**
	 * Create a regular square grid from random data.
	 * 
	 * @param nx
	 *          int Number of X grid points
	 * @param ny
	 *          int Number of Y grid points
	 * @param x1
	 *          double X coordinate of Z[0,0]
	 * @param y1
	 *          double Y coordinate of Z[0,0]
	 * @param dx
	 *          double X grid spacing
	 * @param dy
	 *          double Y grid spacing
	 * @param xp
	 *          double[] X coordinates of random data
	 * @param yp
	 *          double[] Y coordinates of random data
	 * @param zp
	 *          double[] Z coordinates of random data
	 * @param cay
	 *          double amount of spline eqn (between 0 and inf.)
	 * @param nrng
	 *          int grid points more than nrng grid spaces from the nearest data
	 *          point are set to undefined.
	 */
	public ZGrid(int nx, int ny, double x1, double y1, double dx, double dy, double[] xp, double[] yp, double[] zp,
	    double cay, int nrng, boolean maskCoast) {
		nx_ = nx;
		ny_ = ny;
		x1_ = x1;
		y1_ = y1;
		dx_ = dx;
		dy_ = dy;
		cay_ = cay;
		nrng_ = nrng;

		xloc_ = new double[nx];
		yloc_ = new double[ny];
		grid_ = new double[nx * ny];

		for (int i = 0; i < nx; i++) {
			xloc_[i] = x1 + i * dx;
		}
		for (int j = 0; j < ny; j++) {
			yloc_[j] = y1 + j * dy;
		}
		xRange_ = computeSoTRange(xloc_);
		yRange_ = computeSoTRange(yloc_);

		createGrid(xp, yp, zp, maskCoast);

		zRange_ = computeRange2D(grid_);
	}
	
	public void setTransformedValues(double[] xVal, double yVal[]) {	
		for (int i = 0; i < nx_; i++) {
			xloc_[i] = xVal[i];
		}
		for (int j = 0; j < ny_; j++) {
			yloc_[j] = yVal[j];
		}
		xRange_ = computeSoTRange(xloc_);
		yRange_ = computeSoTRange(yloc_);
	}

	/**
	 * Get the array of X values.
	 * 
	 * @return double[]
	 */
	public double[] getXArray() {
		return xloc_;
	}

	/**
	 * Get the length of X value array.
	 * 
	 * @return int
	 */
	public int getXSize() {
		return xloc_.length;
	}

	/**
	 * Get the array of Y values.
	 * 
	 * @return double[]
	 */
	public double[] getYArray() {
		return yloc_;
	}

	/**
	 * Get the length of Y value array.
	 * 
	 * @return int
	 */
	public int getYSize() {
		return yloc_.length;
	}

	/**
	 * Get the array of Z values.
	 * 
	 * @return double[]
	 */
	public double[] getZArray() {
		return grid_;
	}

	/**
	 * Get the range of Z values.
	 * 
	 * @return Range2D
	 */
	public Range2D getZRange() {
		return zRange_;
	}

	/**
	 * Get the array of temporal values.
	 * 
	 * @return GeoDate[]
	 */
	public GeoDate[] getTimeArray() {
		return tloc_.getGeoDate();
	}

	/**
	 * Get the <code>GeoDateArray</code> object.
	 * 
	 * @return GeoDateArray
	 */
	public GeoDateArray getGeoDateArray() {
		return tloc_;
	}

	/**
	 * Get the length of temporal value array.
	 * 
	 * @return int
	 */
	public int getTSize() {
		return tloc_.getLength();
	}

	/**
	 * Get the Z SGTMetaData.
	 * 
	 * @return SGTMetaData
	 */
	public SGTMetaData getZMetaData() {
		return zMetaData_;
	}

	/**
	 * Get the associated data.
	 * 
	 * @return SGTGrid
	 */
	public SGTGrid getAssociatedData() {
		return associatedData_;
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean hasAssociatedData() {
		return (associatedData_ != null);
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean hasXEdges() {
		return hasXEdges_;
	}

	/**
	 * Get the X coordinate edges.
	 * 
	 * @return double[]
	 */
	public double[] getXEdges() {
		return xEdges_;
	}

	/**
	 * Get the range of X coordinate edges.
	 * 
	 * @return SoTRange
	 */
	public SoTRange getXEdgesRange() {
		return xEdgesRange_;
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean hasYEdges() {
		return hasYEdges_;
	}

	/**
	 * Get the Y coordinate edges.
	 * 
	 * @return double[]
	 */
	public double[] getYEdges() {
		return yEdges_;
	}

	/**
	 * Get the range of Y coordinate edges.
	 * 
	 * @return SoTRange
	 */
	public SoTRange getYEdgesRange() {
		return yEdgesRange_;
	}

	/**
	 * Get the Time edges.
	 * 
	 * @return GeoDate[]
	 */
	public GeoDate[] getTimeEdges() {
		return tEdges_.getGeoDate();
	}

	/**
	 * Get the <code>GeoDateArray</code> object.
	 * 
	 * @return GeoDateArray
	 */
	public GeoDateArray getGeoDateArrayEdges() {
		return tEdges_;
	}

	/**
	 * Get the title.
	 * 
	 * @return String
	 */
	public String getTitle() {
		return title_;
	}

	/**
	 * Get a title formatted for a Key.
	 * 
	 * @return SGLabel
	 */
	public SGLabel getKeyTitle() {
		return null;
	}

	/**
	 * Get the unique identifier.
	 * 
	 * @return unique identifier
	 */
	public String getId() {
		return id_;
	}

	/**
	 * Create a shallow copy.
	 * 
	 * @return shallow copy
	 */
	public SGTData copy() {
		SGTGrid newGrid;
		try {
			newGrid = (SGTGrid) clone();
		}
		catch (CloneNotSupportedException e) {
			newGrid = new SimpleGrid();
		}
		return (SGTData) newGrid;
	}

	/**
	 * Returns true if the X coordinate is Time.
	 * 
	 * @return boolean
	 */
	public boolean isXTime() {
		return xTime_;
	}

	/**
	 * Returns true if the Y coordinate is Time.
	 * 
	 * @return boolean
	 */
	public boolean isYTime() {
		return yTime_;
	}

	/**
	 * Returns the X SGTMetaData.
	 * 
	 * @return SGTMetaData
	 */
	public SGTMetaData getXMetaData() {
		return xMetaData_;
	}

	/**
	 * Returns the Y SGTMetaData.
	 * 
	 * @return SGTMetaData
	 */
	public SGTMetaData getYMetaData() {
		return yMetaData_;
	}

	/**
	 * Returns the range of the X coordinates.
	 * 
	 * @return SoTRange
	 */
	public SoTRange getXRange() {
		return xRange_.copy();
	}

	/**
	 * Returns the range of the Y coordinates.
	 * 
	 * @return SoTRange
	 */
	public SoTRange getYRange() {
		return yRange_.copy();
	}

	/**
	 * Add a PropertyChangeListener to the listener list.
	 * 
	 * @param l
	 *          PropertyChangeListener
	 */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		changes_.addPropertyChangeListener(l);
	}

	/**
	 * Remove a PropertyChangeListener from the listener list.
	 * 
	 * @param l
	 *          PropertyChangeListener
	 */
	public void removePropertyChangeListener(PropertyChangeListener l) {
		changes_.removePropertyChangeListener(l);
	}

	/**
	 * Set the <code>SGTMetaData</code> associated with the x coordinate.
	 */
	public void setXMetaData(SGTMetaData md) {
		xMetaData_ = md;
	}

	/**
	 * Set the <code>SGTMetaData</code> associated with the y coordinate.
	 */
	public void setYMetaData(SGTMetaData md) {
		yMetaData_ = md;
	}

	/**
	 * Set the <code>SGTMetaData</code> associated with the z coordinate.
	 */
	public void setZMetaData(SGTMetaData md) {
		zMetaData_ = md;
	}

	/**
	 * Set the grid title
	 */
	public void setTitle(String title) {
		title_ = title;
	}

	/** Set the title formatted for the <code>VectorKey</code>. */
	public void setKeyTitle(SGLabel title) {
		keyTitle_ = title;
	}

	/**
	 * Set the unique identifier.
	 */
	public void setId(String ident) {
		id_ = ident;
	}

	/**
	 * Set the associated data grid. <BR>
	 * <B>Property Change:</B> <code>associatedDataModified</code>.
	 * 
	 * @since 2.0
	 */
	public void setAssociatedData(SGTGrid assoc) {
		associatedData_ = assoc;
		changes_.firePropertyChange("associatedDataModified", null, assoc);
	}

	private SoTRange computeSoTRange(double[] array) {
		Range2D range = computeRange2D(array);
		return new SoTRange.Double(range.start, range.end);
	}

	private SoTRange computeSoTRange(GeoDateArray tarray) {
		if (tarray == null) { return new SoTRange.Time(Long.MAX_VALUE, Long.MAX_VALUE); }

		long start = Long.MAX_VALUE;
		long end = Long.MIN_VALUE;
		long[] tar = tarray.getTime();
		int count = 0;
		for (int i = 0; i < tar.length; i++) {
			if (!(tar[i] == Long.MAX_VALUE)) {
				start = Math.min(start, tar[i]);
				end = Math.max(end, tar[i]);
				count++;
			}
		}
		if (count == 0) {
			return new SoTRange.Time(Long.MAX_VALUE, Long.MAX_VALUE);
		}
		else {
			return new SoTRange.Time(start, end);
		}
	}

	private Range2D computeRange2D(double[] array) {
		if (array == null) { return new Range2D(Double.NaN, Double.NaN); }

		double start = Double.POSITIVE_INFINITY;
		double end = Double.NEGATIVE_INFINITY;
		int count = 0;
		for (int i = 0; i < array.length; i++) {
			if (!Double.isNaN(array[i])) {
				start = Math.min(start, array[i]);
				end = Math.max(end, array[i]);
				count++;
			}
		}
		if (count == 0) {
			return new Range2D(Double.NaN, Double.NaN);
		}
		else {
			return new Range2D(start, end);
		}
	}

	private double z(int i, int j) {
		return grid_[j + i * ny_];
	}

	private double absZ(int i, int j) {
		double abs = z(i, j);
		if (Double.isInfinite(abs) || Double.isNaN(abs)) { 
			return Double.NaN;
		}
		return Math.abs(abs);
	}

	public void maskCoast() {
		Dbase mETOPODB = null;
		float delta = 0.5f;

		if (!ptsHaveBeenCached) {
			String mFilename = "etopo20.nc";
			File etopoFile;
			// look at the lat lon of each grid point and determine if it is on land
			// or not
			// use the default etopo60
			try {
				etopoFile = JOAFormulas.getSupportFile(mFilename);
			}
			catch (IOException ex) {
				// present an error dialog
				return;
			}

			mETOPODB = null;

			if (mFilename.indexOf("60") >= 0) {
				delta = 0.5f;
			}
			else if (mFilename.indexOf("20") >= 0) {
				delta = 0.1667f;
			}
			else if (mFilename.indexOf("5") >= 0) {
				delta = 0.0417f;
			}
			else if (mFilename.indexOf("2") >= 0) {
				delta = 0.01667f;
			}
			else if (mFilename.indexOf(".5") >= 0) {
				delta = 4.1667e-03f;
			}

			if (mETOPODB == null) {
				// get the etopo datafile
				try {
					etopoFile = JOAFormulas.getSupportFile(mFilename);
				}
				catch (IOException ex) {
					// present an error dialog
					return;
				}
				String dir = etopoFile.getParent();

				EpicPtrs ptrDB = new EpicPtrs();

				// create a pointer
				EpicPtr epPtr = new EpicPtr(EPSConstants.NETCDFFORMAT, "ETOPO Import", "ETOPO", "na", "na", -99, -99,
				    new gov.noaa.pmel.util.GeoDate(), -99, -99, null, mFilename, dir, null);

				// set the data of ptrDB to this one entry
				ptrDB.setFile(etopoFile);
				ptrDB.setData(epPtr);

				// create a database
				PointerDBIterator pdbi = ptrDB.iterator();
				EPSDbase etopoDB = new EPSDbase(pdbi, true);

				// get the database
				EPSDBIterator dbItor = etopoDB.iterator(true);

				try {
					mETOPODB = (Dbase) dbItor.getElement(0);
				}
				catch (Exception ex) {
				}
			}

			// latitude axis
			Axis latAxis = mETOPODB.getAxis("Y");
			if (latAxis == null) {
				latAxis = mETOPODB.getAxis("y");
			}
			MultiArray latma = latAxis.getData();

			// longitude axis
			Axis lonAxis = mETOPODB.getAxis("X");
			if (lonAxis == null) {
				lonAxis = mETOPODB.getAxis("x");
			}
			MultiArray lonma = lonAxis.getData();

			Vector vars = mETOPODB.getMeasuredVariables(false);
			EPSVariable rose = (EPSVariable) vars.elementAt(0);
			MultiArray zma = rose.getData();

			savedLats = new float[latAxis.getLen()];
			savedLons = new float[lonAxis.getLen()];
			savedVals = new float[lonAxis.getLen()][latAxis.getLen()];

			// loop on latitude and longitude
			for (int ln = 0; ln < lonAxis.getLen(); ln++) {
				for (int lt = 0; lt < latAxis.getLen(); lt++) {
					float latCtr = 0.0f;
					float lonCtr = 0.0f;

					try {
						// array optimization needed
						latCtr = (float) latma.getDouble(new int[] { lt });
						savedLats[lt] = latCtr;
					}
					catch (Exception ex) {
						System.out.println("at P3");
						continue;
					}

					try {
						// array optimization needed
						lonCtr = (float) lonma.getDouble(new int[] { ln });
						savedLons[ln] = lonCtr;
					}
					catch (Exception ex) {
						System.out.println("at P4");
						continue;
					}
					// get the value
					float zVal = 0.0f;
					try {
						// array optimization needed
						zVal = (float) zma.getDouble(new int[] { lt, ln });
						savedVals[ln][lt] = zVal;
					}
					catch (Exception ex) {
						System.out.println("at P5");
					}
				}

			}
		}
		// System.out.println("num polys = " + cnt);

		// check whether elevation at grid point is positive or negative
		float lat0 = savedLats[0];
		float latn = savedLats[savedLats.length - 1];
		float delLat = (latn - lat0) / (float) savedLats.length;
		float lon0 = savedLons[0];
		float lonn = savedLons[savedLons.length - 1];
		float delLon = (lonn - lon0) / (float) savedLons.length;

		for (int i = 0; i < nx_; i++) {
			double lon = xloc_[i];
			if (lon  < 0)
				lon += 360.0;
			for (int j = 0; j < ny_; j++) {
				double lat = yloc_[j];

				// have to get the elevation at closest lat and lon in the saved lats
				// and lons
				int latIndx = (int) Math.round((lat - lat0) / delLat);
				int lonIndx = (int) Math.round((lon - lon0) / delLon);

				if (latIndx < 0 || latIndx > savedLats.length - 1)
					continue;

				if (lonIndx < 0 || lonIndx > savedLons.length - 1)
					continue;

				if (savedVals[lonIndx][latIndx] >= 0) {
//						System.out.println(i + " " + j + " Mask Coast setting to NaN");
//					setZ(i, j, Double.NaN);
				}
			}
		}

	}

	private void setZ(int i, int j, double val) {
		grid_[j + i * ny_] = val;
	}

	private void createGrid(double[] xp, double[] yp, double[] zp, boolean maskCoast) {
		int n = xp.length;
		double[] zpij = new double[n];
		int[] knxt = new int[n];
		int[] imnew = new int[ny_];
		int[] kksav = new int[1000];

		int itmax, k, kk, i, j, npt, imask, iter, nnew;
		int jmnew, npg, im, jm;
		double eps, zmin, zmax, zrange, zbase, hrange, derzm, zsum;
		double zijn, abz, dzrmsp, relax, dzrms, dzmax, z00, wgt, zim, zimm, zip;
		double zjm, zjmm, zjp, zjpp, dz, x, y, zpxy, zw, ze, zs, zn, delzm, delz;
		double dzrms8, rootgs, relaxn, zipp, a, b, c, d, zxy, root, dzmaxf, tpy;

		dzrms8 = 1.0;

		itmax = 100;
		eps = .002;
		
		/**
		 * get zbase which will make all zp values positive by 20*(zmax-zmin) grh
		 * modification
		 */
		zmin = Double.MAX_VALUE;
		zmax = -Double.MAX_VALUE;
		for (k = 0; k < n; k++) {
			if (!Double.isNaN(zp[k])) {
				if (zp[k] > zmax) {
					zmax = zp[k];
				}
				if (zp[k] < zmin) {
					zmin = zp[k];
				}
			}
		}

		zrange = zmax - zmin;
		zbase = zrange * 20. - zmin;
		hrange = Math.min(dx_ * (nx_ - 1), dy_ * (ny_ - 1));
		derzm = 2. * zrange / hrange;
		/**
		 * set pointer array knxt
		 */
		for (kk = 1; kk <= n; kk++) {
			k = n - kk;
			knxt[k] = 0;
			i = (int) ((xp[k] - x1_) / dx_ + 0.5);
			if (i >= 0 && i < nx_) {
				j = (int) ((yp[k] - y1_) / dy_ + 0.5);
				if (j >= 0 && j < ny_) {
					if (!Double.isNaN(z(i, j))) {
						knxt[k] = n + 1;
						if (z(i, j) > 0) {
							knxt[k] = (int) (z(i, j) + .5);
						}
						setZ(i, j, k);
					}
				}
			}
		}
		/**
		 * affix each data point zp to its nearby grid point. take avg zp if more
		 * than one zp nearby the grid point. add zbase and complement.
		 */
		for (k = 0; k < n; k++) {
			if (knxt[k] > 0) {
				npt = 0;
				// grh modification
				imask = 0;
				zsum = 0.;
				i = (int) ((xp[k] - x1_) / dx_ + 0.5);
				j = (int) ((yp[k] - y1_) / dy_ + 0.5);
				kk = k;
				do {
					npt++;
					// grh modification
					kksav[npt] = kk;
					if (Double.isNaN(zp[kk])) {
						imask = 1;
					}

					zsum = zsum + zp[kk];
					knxt[kk] = -knxt[kk];
					kk = -knxt[kk];
				} while (kk < n);
				// grh modification
				if (imask == 0) {
					setZ(i, j, -zsum / npt - zbase);
				}
				else {
					setZ(i, j, Double.NaN);
					for (i = 0; i < npt; i++) {
						knxt[kksav[i]] = 0;
					}
				}

			}
		}
		/**
		 * initially set each unset grid point to value of nearest known pt.
		 */
		for (i = 0; i < nx_; i++) {
			for (j = 0; j < ny_; j++) {
				if (z(i, j) == 0) {
					setZ(i, j, Double.POSITIVE_INFINITY);
				}
			}
		}
		jmnew = 0;
		for (iter = 1; iter <= nrng_; iter++) {
			nnew = 0;
			for (i = 0; i < nx_; i++) {
				for (j = 0; j < ny_; j++) {
					if (Double.isInfinite(z(i, j))) {
						if (j >= 1) {
							if (jmnew <= 0) {
								zijn = absZ(i, j - 1);
								if (!Double.isNaN(zijn)) {
									imnew[j] = 1;
									jmnew = 1;
									setZ(i, j, zijn);
									nnew = nnew + 1;
									continue;
								}
							}
						}
						if (i >= 1) {
							if (imnew[j] <= 0) {
								zijn = absZ(i - 1, j);
								if (!Double.isNaN(zijn)) {
									imnew[j] = 1;
									jmnew = 1;
									setZ(i, j, zijn);
									nnew = nnew + 1;
									continue;
								}
							}
						}
						if (j < ny_ - 1) {
							zijn = absZ(i, j + 1);
							if (!Double.isNaN(zijn)) {
								imnew[j] = 1;
								jmnew = 1;
								setZ(i, j, zijn);
								nnew = nnew + 1;
								continue;
							}
						}
						if (i < nx_ - 1) {
							zijn = absZ(i + 1, j);
							if (!Double.isNaN(zijn)) {
								imnew[j] = 1;
								jmnew = 1;
								setZ(i, j, zijn);
								nnew = nnew + 1;
								continue;
							}
						}
					}
					else {
						imnew[j] = 0;
						jmnew = 0;
					}
				}
			}
			if (nnew <= 0) {
				break;
			}
		}

		for (i = 0; i < nx_; i++) {
			for (j = 0; j < ny_; j++) {
				abz = absZ(i, j);
				if (Double.isNaN(abz)) {
					setZ(i, j, Double.NaN);
				}
			}
		}
		/**
		 * improve the non-data points by applying point over-relaxation using the
		 * laplace-spline equation (carres method is used)
		 */
		dzrmsp = zrange;
		relax = 1.0;
		zim = 0.0;
		zjm = 0.0;
		for (iter = 1; iter <= itmax; iter++) {
			dzrms = 0.;
			dzmax = 0.;
			npg = 0;
			for (i = 0; i < nx_; i++) {
				for (j = 0; j < ny_; j++) {
					z00 = z(i, j);
					if (!Double.isNaN(z00) && z00 >= 0) {
						wgt = 0.;
						zsum = 0.;

						im = 0;
						if (i >= 1) {
							zim = absZ(i - 1, j);
							if (!Double.isNaN(zim)) {
								im = 1;
								wgt = wgt + 1.;
								zsum = zsum + zim;
								if (i >= 2) {
									zimm = absZ(i - 2, j);
									if (!Double.isNaN(zimm)) {
										wgt = wgt + cay_;
										zsum = zsum - cay_ * (zimm - 2. * zim);
									}
								}
							}
						}

						if (nx_ - 1 > i) {
							zip = absZ(i + 1, j);
							if (!Double.isNaN(zip)) {
								wgt = wgt + 1.;
								zsum = zsum + zip;
								if (im > 0) {
									wgt = wgt + 4. * cay_;
									zsum = zsum + 2. * cay_ * (zim + zip);
								}
								if (nx_ - 2 > i) {
									zipp = absZ(i + 2, j);
									if (!Double.isNaN(zipp)) {
										wgt = wgt + cay_;
										zsum = zsum - cay_ * (zipp - 2. * zip);
									}
								}
							}
						}

						jm = 0;
						if (j >= 1) {
							zjm = absZ(i, j - 1);
							if (!Double.isNaN(zjm)) {
								jm = 1;
								wgt = wgt + 1.;
								zsum = zsum + zjm;
								if (j >= 2) {
									zjmm = absZ(i, j - 2);
									if (!Double.isNaN(zjmm)) {
										wgt = wgt + cay_;
										zsum = zsum - cay_ * (zjmm - 2. * zjm);
									}
								}
							}
						}

						if (ny_ - 1 > j) {
							zjp = absZ(i, j + 1);
							if (!Double.isNaN(zjp)) {
								wgt = wgt + 1.;
								zsum = zsum + zjp;
								if (jm > 0) {
									wgt = wgt + 4. * cay_;
									zsum = zsum + 2. * cay_ * (zjm + zjp);
								}
								if (ny_ - 2 > j) {
									zjpp = absZ(i, j + 2);
									if (!Double.isNaN(zjpp)) {
										wgt = wgt + cay_;
										zsum = zsum - cay_ * (zjpp - 2. * zjp);
									}
								}
							}
						}

						dz = zsum / wgt - z00;
						npg = npg + 1;
						dzrms = dzrms + dz * dz;
						dzmax = Math.max(Math.abs(dz), dzmax);
						setZ(i, j, z00 + dz * relax);
					}
				}
			}
			/**
			 * shift data points zp progressively back to their proper places as the
			 * shape of surface z becomes evident.
			 */
			if (iter == (iter / 10) * 10) {
				for (k = 0; k < n; k++) {
					knxt[k] = Math.abs(knxt[k]);
					if (knxt[k] > 0) {
						x = (xp[k] - x1_) / dx_;
						i = (int) (x + 0.5);
						x = x - i;
						y = (yp[k] - y1_) / dy_;
						j = (int) (y + 0.5);
						y = y - j;
						zpxy = zp[k] + zbase;
						z00 = absZ(i, j);

						zw = Double.NaN;
						if (i >= 1) {
							zw = absZ(i - 1, j);
						}
						ze = Double.NaN;
						if (i < nx_ - 1) {
							ze = absZ(i + 1, j);
						}
						if (Double.isNaN(ze)) {
							if (Double.isNaN(zw)) {
								ze = z00;
								zw = z00;
							}
							else {
								ze = 2. * z00 - zw;
							}
						}
						else {
							if (Double.isNaN(zw)) {
								zw = 2. * z00 - ze;
							}
						}

						zs = Double.NaN;
						if (j >= 1) {
							zs = absZ(i, j - 1);
						}
						zn = Double.NaN;
						if (j < ny_ - 1) {
							zn = absZ(i, j + 1);
						}
						if (Double.isNaN(zn)) {
							if (Double.isNaN(zs)) {
								zs = z00;
								zn = z00;
							}
							else {
								zn = 2. * z00 - zs;
							}
						}
						else {
							if (Double.isNaN(zs)) {
								zs = 2. * z00 - zn;
							}
						}

						a = (ze - zw) * .5;
						b = (zn - zs) * .5;
						c = (ze + zw) * .5 - z00;
						d = (zn + zs) * .5 - z00;
						zxy = z00 + a * x + b * y + c * x * x + d * y * y;
						delz = z00 - zxy;
						delzm = derzm * (Math.abs(x) * dx_ + Math.abs(y) * dy_) * .80;
						if (delz > delzm) {
							delz = delzm;
						}
						if (delz < -delzm) {
							delz = -delzm;
						}
						zpij[k] = zpxy + delz;
					}
				}

				for (k = 0; k < n; k++) {
					if (knxt[k] > 0) {
						npt = 0;
						zsum = 0.;
						i = (int) ((xp[k] - x1_) / dx_ + 0.5);
						j = (int) ((yp[k] - y1_) / dy_ + 0.5);
						kk = k;
						do {
							npt++;
							zsum = zsum + zpij[kk];
							knxt[kk] = -knxt[kk];
							kk = -knxt[kk];
						} while (kk <= n);
						setZ(i, j, -zsum / npt);
					}
				}
			}
			/**
			 * test for convergence
			 */
			if (npg == 0) {
				break;
			}
			dzrms = Math.sqrt(dzrms / npg);
			root = dzrms / dzrmsp;
			dzrmsp = dzrms;
			dzmaxf = dzmax / zrange;

			if (iter - (iter / 10) * 10 == 2) {
				dzrms8 = dzrms;
			}
			if (iter == (iter / 10) * 10) {
				root = Math.sqrt(Math.sqrt(Math.sqrt(dzrms / dzrms8)));
				if (root < .9999) {
					if (dzmaxf / (1. - root) <= eps) {
						break;
					}
					/**
					 * improve the relaxation factor.
					 */
					if ((iter - 20) * (iter - 40) * (iter - 60) == 0) {
						if (relax - 1. < root) {
							tpy = (root + relax - 1.) / relax;
							rootgs = tpy * tpy / root;
							relaxn = 2. / (1. + Math.sqrt(1. - rootgs));
							if (iter != 60) {
								relaxn = relaxn - .25 * (2. - relaxn);
							}
							relax = Math.max(relax, relaxn);
						}
					}
				}
			}
		}
		/**
		 * remove zbase from array z and return.
		 */
		for (i = 0; i < nx_; i++) {
			for (j = 0; j < ny_; j++) {
				if (!Double.isNaN(z(i, j))) {
					setZ(i, j, absZ(i, j) - zbase);
				}
			}
		}
		if (maskCoast) {
			maskCoast();
		}
	}

}

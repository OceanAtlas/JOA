package gov.noaa.pmel.eps2;

import gov.noaa.pmel.util.*;
import java.util.*;

/**
 * <code>EpicPtr</code> base class for epic ptr objects.
 *
 *
 * @author oz
 * @version 1.0
 */

public class ExportStation implements EPSConstants, Comparable {
	/**
	* format of this EPIC pointer. Can be PROFILEPTRS or TSPTRS
	*/
	int mFormat;
	/**
	* netCDF, JOA, POA, etc...
	*/
	int mFileFormat;
	/**
	* Database name parsed from pointer file
	*/
	String mDatabase;
	/**
	* time series or profile
	*/
	String mDataType;
	/**
	* Latitude
	*/
	ExportLatitude mLat;
	/**
	* Longitude
	*/
	ExportLongitude mLon;
	/**
	* Pointer file name
	*/
	String mFilename;
	/**
	* file path: base path to a particular file or reference
	*/
	String mPath;
	/**
	* maximum attribute number in system
	*/
	ArrayList mTimesOrDates = new ArrayList();
	/**
	* Delta time for time series records
	*/
	double mDelTime = -99.0;
	/**
	* Delta time units for time series records
	*/
	String mDelTimeUnits = null;
	/**
	* Cruise name
	*/
	String mFileSet;
	/**
	* Station identifier
	*/
	String mID;
	/**
	* Cast identifier for profiles
	*/
	String mCast;
	/**
	* minimum and maximum depth are stored in an arraylist
	*/
	ArrayList mVerticals = new ArrayList();
	/**
	* Bottom depth of a profile
	*/
	double mBottomDepth;
	/**
	* maximum attribute number in system
	*/
	int mSortKey;
	/**
	* Parameter list: collection of export parameters
	*/
	ArrayList mParamList = null;
	/**
	* mProgressStr: String to display in progress dialog
	*/
	String mProgressStr = null;
	
	/**
	* mIsURL: This flags whether file is actually a distant netCDF file (DODS)
	*/
	boolean mIsURL = false;
	/**
	* Collection of comments for this pointer
	*/
	ArrayList mComments = new ArrayList();
	
	/**
	* Collection of attributes for this pointer
	*/
	Hashtable mAttributes = new Hashtable();

	/**
	* Collection of stattion values calculated bu JOA
	*/
	ArrayList mStnCalcs;
	
	/**
	* Zero argument constructor
	*/
	public ExportStation() {
	}

    /**
	* Convenience constructor for a time series file
	*
	* @param frmt Format
	* @param db Source database
	* @param type File type (ts or profile)
	* @param dep Depth of instrument
	* @param lat Latitude
	* @param lon Longitude
	* @param stTime Start time in GeoDate
	* @param eTime End time in GeoDate
	* @param delT delta time
	* @param deltunits delta time units
	* @param paramList list a parameters in file in a comma-delimited string
	* @param filename filename
	* @param path file path
	*/
	public ExportStation(int frmt, String db, String type, ExportVertical dep, ExportLatitude lat, ExportLongitude lon,
		                  ArrayList timesordates, double delT, String deltunits, ArrayList paramList, String filename, String path) {
		initCommon(TSPTRS, frmt, db, type, lat, lon, filename, path);
		if (paramList != null) {
			Iterator vitor = paramList.iterator();
			while (vitor.hasNext()) {
				mParamList.add(vitor.next());
			}
		}
		mVerticals.add(dep);
		Iterator titor = timesordates.iterator();
		while (titor.hasNext()) {
			mTimesOrDates.add(titor.next());
		}
		mDelTime = delT;
		mDelTimeUnits = new String(deltunits);
	}
	
    /**
	* Convenience constructor for a time series file
	*
	* @param frmt Format
	* @param db Source database
	* @param type File type (ts or profile)
	* @param dep Depth of instrument
	* @param lat Latitude
	* @param lon Longitude
	* @param stTime Start time in GeoDate
	* @param eTime End time in GeoDate
	* @param delT delta time
	* @param filename filename
	* @param path file path
	*/
	public ExportStation(int frmt, String db, String type, ExportVertical dep, ExportLatitude lat, ExportLongitude lon,
		                  ArrayList timesordates, double delT, String deltunits, String filename, String path) {
		initCommon(TSPTRS, frmt, db, type, lat, lon, filename, path);
		mVerticals.add(dep);
		Iterator titor = timesordates.iterator();
		while (titor.hasNext()) {
			mTimesOrDates.add(titor.next());
		}
		mDelTime = delT;
		mDelTimeUnits = new String(deltunits);
	}

    /**
	* Convenience constructor for a simple ptr file
	*
	* @param frmt Format
	* @param filename filename
	* @param path file path
	*/
	public ExportStation(int frmt, String filename, String path) {
		initCommon(SIMPLEPTRS, frmt, "", "", null, null, filename, path);
	}

    /**
	* Convenience constructor for a simple ptr file with progress string
	*
	* @param frmt Format
	* @param filename filename
	* @param path file path
	*/
	public ExportStation(int frmt, String filename, String path, String progressStr) {
		initCommon(SIMPLEPTRS, frmt, "", "", null, null, filename, path);
		mProgressStr = new String(progressStr);
	}

    /**
	* Convenience constructor for a profile file
	*
	* @param frmt Format
	* @param db Source database
	* @param type File type (ts or profile)
	* @param crs Cruise name
	* @param cst Cast ID
	* @param lat Latitude
	* @param lon Longitude
	* @param inDate Observation time in GeoDate
	* @param zMn Min depth
	* @param zMx Max depth
	* @param paramList list a parameters in an array
	* @param filename filename
	* @param path file path
	* @param stncalcs ArrayList of anything calculated at the individual profile, e.g., MLD
	*/
	public ExportStation(int frmt, String db, String type, String crs, String id, String cst, ExportLatitude lat, ExportLongitude lon,
		                  Object inDate, ArrayList verts, ArrayList paramList, String filename, String path,
		                  ArrayList stncalcs, double bottom) {
		initCommon(PROFILEPTRS, frmt, db, type, lat, lon, filename, path);
		mFileSet = new String(crs);
		mID = new String(id);
		mCast = new String(cst);
		if (paramList != null) {
			Iterator vitor = paramList.iterator();
			while (vitor.hasNext()) {
				mParamList.add(vitor.next());
			}
		}
		mTimesOrDates.add(inDate);
		Iterator vitor = verts.iterator();
		while (vitor.hasNext()) {
			mVerticals.add(vitor.next());
		}
		mStnCalcs = stncalcs;
		mBottomDepth = bottom;
	}
	
    /**
	* Convenience constructor for a profile file
	*
	* @param frmt Format
	* @param db Source database
	* @param type File type (ts or profile)
	* @param crs Cruise name
	* @param cst Cast ID
	* @param lat Latitude
	* @param lon Longitude
	* @param inDate Observation time in GeoDate
	* @param zMn Min depth
	* @param zMx Max depth
	* @param filename filename
	* @param path file path
	*/
	public ExportStation(int frmt, String db, String type, String crs, String id, String cst, ExportLatitude lat, ExportLongitude lon,
		                  Object inDate, ArrayList verts, String filename, String path, double bottom) {
		initCommon(PROFILEPTRS, frmt, db, type, lat, lon, filename, path);
		mFileSet = new String(crs);
		mID = new String(id);
		mCast = new String(cst);
		mTimesOrDates.add(inDate);
		Iterator vitor = verts.iterator();
		while (vitor.hasNext()) {
			mVerticals.add(vitor.next());
		}
		mBottomDepth = bottom;
	}

    /**
    * Set the common fields
    *
	* @param ifrmt Format code whether profile or time series data
	* @param frmt Format what kind of profile data is it? netcdf, joa, poa, etc...
	* @param db Source database
	* @param type File type (ts or profile)
	* @param lat Latitude
	* @param lon Longitude
	* @param filename filename
	* @param path file path
    */
	public void initCommon(int ifrmt, int filefrmt, String db, String typ, ExportLatitude lt, ExportLongitude ln, String fn, String pth) {
		mFormat = ifrmt;
		mFileFormat = filefrmt;
		mDatabase = new String(db);
		mDataType = new String(typ);
		mLat = lt;
		mLon = ln;
		if (fn != null)
			mFilename = new String(fn);
		if (pth != null)
			mPath = new String(pth);
		else
			mPath = "missing";
	}

    /**
     * get the file format
     *
     * @return file format
     */
	public int getFormat() {
		return mFileFormat;
	}

    /**
     * get the data type
     *
     * @return data type
     */
	public String getDataType() {
		return mDataType;
	}

    /**
     * get the fileset
     *
     * @return cruise
     */
	public String getFileSet() {
		return mFileSet;
	}

    /**
     * get the station ID string
     *
     * @return id
     */
	public String getID() {
		return mID;
	}

    /**
     * get the cast number for a profile
     *
     * @return cast
     */
	public String getCast() {
		return mCast;
	}

    /**
     * get the filename
     *
     * @return filename
     */
	public String getFileName() {
		return mFilename;
	}

    /**
     * Return true if file is a profile
     *
     * @return whether ptr is CTD
     */
	public boolean isProfile() {
		return mFormat == PROFILEPTRS;
	}

    /**
     * Return true if file is a time series
     *
     * @return whether ptr is a time series
     */
	public boolean isTimeSeries() {
		return mFormat == TSPTRS;
	}

    /**
     * Get longitude
     *
     * @return longitude
     */
	public ExportLongitude getLon() {
		return mLon;
	}

    /**
     * Get latitude
     *
     * @return latitude
     */
	public ExportLatitude getLat() {
		return mLat;
	}

    /**
     * Get Time
     *
     * @return arraylist of start or end times
     */
	public ArrayList getTimesOrDates() {
		return mTimesOrDates;
	}
	
    /**
     * Is Time Axis Scaler?
     *
     * @return true if axis is scaler, false if vector
     */
	public boolean isTimeScaler() {
		return (mTimesOrDates.size() == 1);
	}

    /**
     * Get min depth
     *
     * @return zmin
     */
	public ArrayList getVerticals() {
		return mVerticals;
	}

    /**
     * Get bottom depth
     *
     * @return mBottomDepth
     */
	public double getBottomDepth() {
		return mBottomDepth;
	}

    /**
     * Set bottom depth
     *
     * @param bottom
     */
	public void setBottomDepth(double ibd) {
		mBottomDepth = ibd;
	}
	
    /**
     * Is Z Axis Scaler?
     *
     * @return true if axis is scaler, false if vector
     */
	public boolean isZScaler() {
		return (mVerticals.size() == 1);
	}

    /**
     * Get delta T
     *
     * @return delta t for time series
     */
	public double getDeltaT() {
		return mDelTime;
	}	
	
	public String getDeltaTUnits() {
		return mDelTimeUnits;
	}


    /**
     * Set the sort keys
     *
     * @param inKey set the sort key for this record (X_DSC, X_ASC, Y_DSC, ...)
     */
	public void setSortKey(int inKey) {
		mSortKey = inKey;
	}

    /**
     * Return a string representation of this ptr object
     *
     * @return string of ptr
     */
	public String toString() {
		return "file=" + mFilename + " lat= " + mLat.getLat() + " lon= " + mLon.getLon();
	}

    /**
     * Compares EpicPtr objects for sorting
     *
     * @param o The object to compare this pointer to
     *
     * @return <code>int</code> 0 => ==, < 0 => <, > 0 => >
     */
	public int compareTo(Object o) {
		ExportStation ep = (ExportStation)o;
		if (mSortKey == X_ASC) {
			// sort ascending by longitude
			double val0 = mLat.getLat();
			double val1 = ep.getLon().getLon();
			if (val0 < val1)
				return -1;
			else if (val0 > val1)
				return 1;
			else
				return 0;
		}
		else if (mSortKey == X_DSC) {
			// sort ascending by longitude
			double val0 = mLon.getLon();
			double val1 = ep.getLon().getLon();
			if (val0 < val1)
				return 1;
			else if (val0 > val1)
				return -1;
			else
				return 0;
		}

		if (mSortKey == Y_ASC) {
			// sort ascending by latitude
			double val0 = mLat.getLat();
			double val1 = ep.getLat().getLat();
			if (val0 < val1)
				return -1;
			else if (val0 > val1)
				return 1;
			else
				return 0;
		}
		else if (mSortKey == Y_DSC) {
			// sort ascending by latitude
			double val0 = mLat.getLat();
			double val1 = ep.getLat().getLat();
			if (val0 < val1)
				return 1;
			else if (val0 > val1)
				return -1;
			else
				return 0;
		}

		// depth
		/*if (mSortKey == Z_ASC) {
			// sort ascending by depth
			double val0 = this.getZ();
			double val1 = ep.getZ();
			if (val0 < val1)
				return -1;
			else if (val0 > val1)
				return 1;
			else
				return 0;
		}
		else if (mSortKey == Z_DSC) {
			// sort ascending by depth
			double val0 = this.getZ();
			double val1 = ep.getZ();
			if (val0 < val1)
				return 1;
			else if (val0 > val1)
				return -1;
			else
				return 0;
		}

		// time axis
		if (mSortKey == T_ASC) {
			// sort ascending by depth
			GeoDate val0 = this.getT();
			GeoDate val1 = ep.getT();
			double delta = val1.subtract(val0).getGMTMinutes();
			if (delta < 0)
				return 1;
			else if (delta > 0)
				return -1;
			else
				return 0;
		}
		else if (mSortKey == T_DSC) {
			// sort ascending by depth
			GeoDate val0 = this.getT();
			GeoDate val1 = ep.getT();
			GeoDate gdelta = val1.subtract(val0);
			double delta = val0.subtract(val1).getGMTMinutes();
			if (delta < 0)
				return -1;
			else if (delta > 0)
				return 1;
			else
				return 0;
		}*/
		return 0;
	}

    /**
     * Get Progress dialog string
     *
     * @return progress string
     */
	public String getProgressStr() {
		return mProgressStr;
	}
	
	public void setIsURL(boolean bval) {
		mIsURL = bval;
	}
	
	public String getURI() {
		return mPath;
	}

	public int getNumStnCalcs() {
		if (mStnCalcs != null)
			return mStnCalcs.size();
		else
			return 0;
	}
	
	public ArrayList getStnCalcs() {
		return mStnCalcs;
	}
	
	public ArrayList getParameters() {
		return mParamList;
	}
	
	public void addComment(String comment) {
		if (mComments == null)
			mComments = new ArrayList();
		mComments.add(comment);
	}
	
	public ArrayList getComments() {
		return mComments;
	}
	
	public void addAttribute(String name, String atval) {
		mAttributes.put(name, atval);
	}
	
	public Hashtable getAttributes() {
		return mAttributes;
	}
	
	public int getNumAttributes() {
		return mAttributes.size();
	}
}
